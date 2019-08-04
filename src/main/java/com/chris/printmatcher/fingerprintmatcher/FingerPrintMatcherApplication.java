package com.chris.printmatcher.fingerprintmatcher;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class FingerPrintMatcherApplication {

    public static void main(String[] args) {
//        SpringApplication.run(FingerPrintMatcherApplication.class, args);

        new SpringApplicationBuilder(FingerPrintMatcherApplication.class).headless(false).run(args);
    }

}
