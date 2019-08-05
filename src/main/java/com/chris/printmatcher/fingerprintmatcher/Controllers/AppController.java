package com.chris.printmatcher.fingerprintmatcher.Controllers;

import com.chris.printmatcher.fingerprintmatcher.Configuration.ReaderSupportConfig.MessageBox;
import com.chris.printmatcher.fingerprintmatcher.Model.AppUsers;
import com.chris.printmatcher.fingerprintmatcher.Services.AppServices;
import com.digitalpersona.uareu.ReaderCollection;
import com.digitalpersona.uareu.UareUException;
import com.digitalpersona.uareu.UareUGlobal;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

@RestController
@RequestMapping("/fingerprint")
@Api(value = "FIG_MATCHER", description = "Chris Finger Print Matcher API for Desktop and Web App Developers")
public class AppController {

    private ReaderCollection m_collection = null;

    @Autowired
    private AppServices appServices;


    @GetMapping(path = "/")
    public String index(){
        return "index";
    }

    @GetMapping(path = "/connect/reader")
    @ApiOperation(value = "Try's To Connect To A Connected Finger Print Reader", response = Boolean.class, httpMethod = "GET",
            notes = "Finger Print can only be scanned from a U.are.U Scanner")
    public Boolean connectToFingerPrintReader(){

        try{
            m_collection = UareUGlobal.GetReaderCollection();
        }
        catch(UareUException e) {

            MessageBox.DpError("UareUGlobal.getReaderCollection()", e);
            return null;
        }

        return appServices.getFingerPrintReader(m_collection);
    }


    @GetMapping(path = "/scan/{fingerToScan}")
    @ApiOperation(value = "Scan Finger Print API", response = Base64.Encoder.class, httpMethod = "GET",
            notes = "Finger Print can only be scanned from a U.are.U Scanner")
    public String scanFingerPrint(@PathVariable("fingerToScan") String fingerToScan) throws IOException {

        BufferedImage scanImage = appServices.scanFinger(fingerToScan);

        if (null != scanImage){

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ImageIO.write(scanImage, "jpg", byteArrayOutputStream);
            return Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray());

        }

        return null;
    }


    @PostMapping(path = "/create/{usersUniqueId}")
    @ApiOperation(value = "Create New User API", response = String.class, httpMethod = "POST",
            notes = "Finger Print can only be scanned from a U.are.U Scanner")
    public String createUser(@PathVariable("usersUniqueId") String userId, @RequestBody AppUsers user){

        if (userId != null){

            user.setUserId(userId);
            return appServices.createUser(user);
        }
        return "User not saved due to null value in the URL";
    }


    @PostMapping(path = "/verify/print")
    @ApiOperation(value = "Compares Input Finger Print With already Existing ones", response = String.class, httpMethod = "POST",
            notes = "Finger Print can only be scanned from a U.are.U Scanner")
    public String verifyPrint(@RequestBody AppUsers user){

        if (null != user) {

            byte[] printToVerify = Base64.getDecoder().decode(user.getFingerPrintToVerify().getBytes());
            AppUsers userRetrieved = appServices.verifyUser(printToVerify);
            if (null != userRetrieved) return "Match Found, User Id: " + userRetrieved.getUserId();
            return "No Match Found";
        }

        return "Incorrect data parsed to the scan API";
    }


    @GetMapping(path = "/scan/verify")
    @ApiOperation(value = "Scan Finger Print And Runs Comparison API", response = String.class, httpMethod = "GET",
            notes = "Finger Print can only be scanned from a U.are.U Scanner")
    public String scanThenVerifyFingerPrint() throws IOException {

        AppUsers verifiedUser = appServices.scanThenVerifyUser();

        if (null != verifiedUser) return "Match Found, UserId: " + verifiedUser.getUserId();

        return "No Match Found";
    }

}
