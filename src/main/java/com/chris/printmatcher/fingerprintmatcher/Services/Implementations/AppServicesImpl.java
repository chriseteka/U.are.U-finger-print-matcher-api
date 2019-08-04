package com.chris.printmatcher.fingerprintmatcher.Services.Implementations;

import com.chris.printmatcher.fingerprintmatcher.Configuration.ReaderSupportConfig.Capture;
import com.chris.printmatcher.fingerprintmatcher.Configuration.ReaderSupportConfig.MessageBox;
import com.chris.printmatcher.fingerprintmatcher.Model.AppUsers;
import com.chris.printmatcher.fingerprintmatcher.Model.UsersDetails;
import com.chris.printmatcher.fingerprintmatcher.Repositories.AppUsersRepo;
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
import java.util.Optional;
import java.util.Vector;

@Service
public class AppServicesImpl implements AppServices {

    private ReaderCollection m_collection;
    private List m_readerList;
    private BufferedImage scannedImage = null;
    private int indexOfSupportedReader = -1;

    @Autowired
    private AppUsersRepo usersRepo;

    @Override
    public String createUser(AppUsers user) {

        if (null != user){

            user.setLeftThumb(Base64.decodeBase64(user.getLeftThumbString().getBytes()));
            user.setRightThumb(Base64.decodeBase64(user.getRightThumbString().getBytes()));

            //Check if the user id exist in the DB
            if (null != usersRepo.findDistinctByUserId(user.getUserId()))
                return "A user with same id was found in the database";

            //Check the two prints to be sure it does not exist before in the database
            if (null != verifyUser(user.getLeftThumb()) || null != verifyUser(user.getRightThumb()))
                return "A user was found with the same finger print";

            AppUsers savedUser = usersRepo.save(user);

            if (null != savedUser) return "User Created Successfully";
        }
        return "An Unknown Error Occurred, Try Again";
    }

    @Override
    public Boolean getFingerPrintReader(ReaderCollection collection) {

        m_collection = collection;

        m_readerList = new ArrayList();

        //acquire available readers
        try{
            m_collection.GetReaders();
        }
        catch(UareUException e) {
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

        if (-1 == indexOfSupportedReader){

            try{
                m_collection = UareUGlobal.GetReaderCollection();
            }
            catch(UareUException e) {

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

        //SOURCE AFIS FINGER PRINT COMPARING ALGORITHM WAS USED HERE FOR 1:N FINGERS COMPARISON
        List<UsersDetails> usersDetailsList = new ArrayList<>();
        for(AppUsers users : usersRepo.findAll()){

            usersDetailsList.add(new UsersDetails(users.getId(), users.getUserId(), getTemplateFromPrint(users.getLeftThumb())));
            usersDetailsList.add(new UsersDetails(users.getId(), users.getUserId(), getTemplateFromPrint(users.getRightThumb())));
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

        if (null != matchedUser){

            Optional<AppUsers> userFound = usersRepo.findById(matchedUser.getId());

            if (userFound.isPresent()) return high >= threshold ? userFound.get() : null;
        }
        return null;
    }

    @Override
    public AppUsers scanThenVerifyUser() throws IOException {

        BufferedImage scanImage = scanFinger("ANY FINGER");

        if (null != scanImage){

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
