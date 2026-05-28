package fr.cnrs.opentheso;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


import org.springframework.context.annotation.Bean;
import org.springframework.boot.CommandLineRunner;
import org.springframework.mail.javamail.JavaMailSender;

@SpringBootApplication
public class OpenthesoApplication {

    public static void main(String[] args) {
        SpringApplication.run(OpenthesoApplication.class, args);
    }
}
