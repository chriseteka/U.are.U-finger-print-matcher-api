package com.chris.printmatcher.fingerprintmatcher.Services.Implementations;

import com.chris.printmatcher.fingerprintmatcher.Configuration.ReaderSupportConfig.Capture;
import com.chris.printmatcher.fingerprintmatcher.Configuration.ReaderSupportConfig.MessageBox;
import com.chris.printmatcher.fingerprintmatcher.Model.AppUsers;
import com.chris.printmatcher.fingerprintmatcher.Model.UsersDetails;
import com.chris.printmatcher.fingerprintmatcher.Repositories.AppUsersRepo;
import com.chris.printmatcher.fingerprintmatcher.Repositories.UsersPrintTemplatesRepo;
import com.chris.printmatcher.fingerprintmatcher.Services.AppServices;
import com.digitalpersona.uareu.Reader;
import com.digitalpersona.uareu.ReaderCollection;
import com.digitalpersona.uareu.UareUException;
import com.digitalpersona.uareu.UareUGlobal;
import com.machinezoo.sourceafis.FingerprintMatcher;
import com.machinezoo.sourceafis.FingerprintTemplate;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

@Service
public class AppServicesImpl implements AppServices {

    private ReaderCollection m_collection;
    private List m_readerList;
    private BufferedImage scannedImage = null;
    private int indexOfSupportedReader = -1;

    @Autowired
    private AppUsersRepo usersRepo;

    @Autowired
    private UsersPrintTemplatesRepo printRepo;

    //Make this list global, and static, load up all print details to the list on startup
    public static List<UsersDetails> usersDetailsList = new ArrayList<>();

    @Override
    public String createUser(AppUsers user) {

        if (null != user) {

            user.setLeftThumb(Base64.decodeBase64(user.getLeftThumbString().getBytes()));
            user.setRightThumb(Base64.decodeBase64(user.getRightThumbString().getBytes()));

            //Check if same finger print was entered
            if (sameFingerEntered(user.getRightThumb(), user.getLeftThumb()))
                return "Same finger entered, please register two different fingers.";


            //Check if the user id exist in the DB
            if (null != usersRepo.findDistinctByUserId(user.getUserId()))
                return "A user with same id was found in the database";

            //Check the two prints to be sure it does not exist before in the database
            if (null != verifyUser(user.getLeftThumb()) || null != verifyUser(user.getRightThumb()))
                return "A user was found with the same finger print";

            AppUsers savedUser = usersRepo.save(user);

            if (null != savedUser) {

                List<UsersDetails> userPrintDetails = new ArrayList<>();

                userPrintDetails.add(new UsersDetails(savedUser.getUserId(), getTemplateFromPrint(savedUser.getLeftThumb()).serialize()));
                userPrintDetails.add(new UsersDetails(savedUser.getUserId(), getTemplateFromPrint(savedUser.getRightThumb()).serialize()));

                //Mark with at cacheable using redis implementation
                if(!printRepo.saveAll(userPrintDetails).isEmpty()) {

                    userPrintDetails.forEach(userPrint -> usersDetailsList
                            .add(new UsersDetails(userPrint.getName(), new FingerprintTemplate().deserialize(userPrint.getFingerPrintString()))));
                    return "User Created Successfully";
                }
            }
        }
        return "An Unknown Error Occurred, Try Again";
    }

    //Method to check if same finger was entered.
    private boolean sameFingerEntered(byte[] rightThumb, byte[] leftThumb) {

        FingerprintMatcher matcher = new FingerprintMatcher().index(getTemplateFromPrint(rightThumb));
        double matchDegree = matcher.match(getTemplateFromPrint(leftThumb));

        return matchDegree > 75;
    }

    @Override
    public Boolean getFingerPrintReader(ReaderCollection collection) {

        m_collection = collection;

        m_readerList = new ArrayList();

        //acquire available readers
        try {
            m_collection.GetReaders();
        } catch (UareUException e) {
            MessageBox.DpError("ReaderCollection.GetReaders()", e);
        }

        //list reader names
        Vector<String> strNames = new Vector<>();

        for (Reader reader : m_collection) {

            //Check if connected device is a U.are.U fingerprint reader before you add it to the list
            Reader.Description readerDescription = reader.GetDescription();

            if (readerDescription.id.vendor_name.equalsIgnoreCase("DigitalPersona, Inc.")) {
                strNames.add(readerDescription.name);
                indexOfSupportedReader = m_collection.indexOf(reader);
            }
        }
        m_readerList.add(strNames);

        if (null != m_readerList && m_readerList.size() == 1 && !strNames.isEmpty()) {

            int selectedIndex = m_readerList.indexOf(strNames);
            Reader selectedFingerPrintReader = m_collection.get(selectedIndex);

            return null != selectedFingerPrintReader;

        }

        return false;
    }

    @Override
    public BufferedImage scanFinger(String fingerToScan) {

        if (-1 == indexOfSupportedReader) {

            try {
                m_collection = UareUGlobal.GetReaderCollection();
            } catch (UareUException e) {

                MessageBox.DpError("UareUGlobal.getReaderCollection()", e);
                return null;
            }

            getFingerPrintReader(m_collection);
        }

        Reader reader = m_collection.get(indexOfSupportedReader);

        Capture.Run(reader, false, fingerToScan);

        scannedImage = Capture.getScannedImage();

        if (null != scannedImage) return scannedImage;

        return null;
    }

    @Override
    public AppUsers verifyUser(byte[] fingerPrintData) {

        if (usersDetailsList.isEmpty()) {

            usersRepo.findAll().forEach(user -> {

                usersDetailsList.add(new UsersDetails(user.getUserId(), getTemplateFromPrint(user.getRightThumb()).serialize()));
                usersDetailsList.add(new UsersDetails(user.getUserId(), getTemplateFromPrint(user.getLeftThumb()).serialize()));
            });

            printRepo.saveAll(usersDetailsList);

            usersDetailsList.clear();

            printRepo.findAll()
                    .forEach(usersDetails -> usersDetailsList
                            .add(new UsersDetails(usersDetails.getName(), new FingerprintTemplate().deserialize(usersDetails.getFingerPrintString()))));

        }

        UsersDetails matchedUser = null;
        double high = 0;

        FingerprintMatcher matcher = new FingerprintMatcher().index(getTemplateFromPrint(fingerPrintData));

        for (UsersDetails candidate : usersDetailsList) {

            double score = matcher.match(candidate.template);

            if (score > high) {
                high = score;
                matchedUser = candidate;
            }
        }

        double threshold = 75;

        if (null != matchedUser && high > threshold) return usersRepo.findDistinctByUserId(matchedUser.getName());

        return null;
    }

    @Override
    public AppUsers scanThenVerifyUser() throws IOException {

        BufferedImage scanImage = scanFinger("ANY FINGER");

        if (null != scanImage) {

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ImageIO.write(scanImage, "jpg", byteArrayOutputStream);
            return verifyUser(byteArrayOutputStream.toByteArray());
        }

        return null;
    }

    private FingerprintTemplate getTemplateFromPrint(byte[] thumbPrint) {

        return new FingerprintTemplate().dpi(500).create(thumbPrint);
    }
}
