package com.chris.printmatcher.fingerprintmatcher;

import com.chris.printmatcher.fingerprintmatcher.Model.UsersDetails;
import com.chris.printmatcher.fingerprintmatcher.Repositories.UsersPrintTemplatesRepo;
import com.chris.printmatcher.fingerprintmatcher.Services.Implementations.AppServicesImpl;
import com.machinezoo.sourceafis.FingerprintTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class FingerPrintMatcherApplication implements CommandLineRunner {

    public static void main(String[] args) {
//        SpringApplication.run(FingerPrintMatcherApplication.class, args);

        new SpringApplicationBuilder(FingerPrintMatcherApplication.class).headless(false).run(args);
    }

    @Autowired
    private UsersPrintTemplatesRepo printRepo;

    @Override
    public void run(String... args) throws Exception {

        //New Function which should make iteration faster
        printRepo.findAll()
                .forEach(usersDetails -> AppServicesImpl.usersDetailsList
                        .add(new UsersDetails(usersDetails.getName(), new FingerprintTemplate().deserialize(usersDetails.getFingerPrintString()))));
    }
}
