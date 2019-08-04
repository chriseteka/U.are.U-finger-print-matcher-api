package com.chris.printmatcher.fingerprintmatcher.Services;

import com.chris.printmatcher.fingerprintmatcher.Model.AppUsers;
import com.digitalpersona.uareu.ReaderCollection;

import java.awt.image.BufferedImage;
import java.io.IOException;

public interface AppServices {

    /**
     * Returns SUCCESS MESSAGE on successful creation of a user, and FAILURE OTHERWISE otherwise
     * Takes AppUsers as input
     * */
    String createUser(AppUsers user);

    /**
     * Returns TRUE on successful communication with the finger print reader, and FALSE otherwise
     * Takes U.are.U finger print reader collection as input
     * */
    Boolean getFingerPrintReader(ReaderCollection collection);

    /**
     * Returns a BUFFERED IMAGE on successful scan of a finger, and NULL otherwise
     * Takes the name of the finger to be scanned as input
     * */
    BufferedImage scanFinger(String fingerToScan);

    /**
     * Returns USER FOUND IN DB on successful verification of a finger print, and NULL otherwise
     * Takes as input a binary array of the finger print image
     * */
    AppUsers verifyUser(byte[] fingerPrintData);

    /**
     * Returns TRUE on successful verification of a user, and FALSE otherwise
     * */
    AppUsers scanThenVerifyUser() throws IOException;
}
