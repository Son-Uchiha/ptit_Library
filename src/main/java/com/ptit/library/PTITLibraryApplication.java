package com.ptit.library;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PTITLibraryApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(PTITLibraryApplication.class, args);
    }
}
