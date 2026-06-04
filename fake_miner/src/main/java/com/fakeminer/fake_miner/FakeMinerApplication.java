package com.fakeminer.fake_miner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class FakeMinerApplication {

    public static void main(String[] args) {
        SpringApplication.run(FakeMinerApplication.class, args);
    }

}
