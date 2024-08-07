package com.capgemini.csd.tippkick.tippabgabe;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@EmbeddedKafka(partitions = 1)
class TippabgabeApplicationTest {

    @Test
    void contextLoads() {
    }

}