package com.capgemini.csd.tippkick.tippabgabe.pact.consumer;

import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import com.capgemini.csd.tippkick.tippabgabe.clients.SpielplanClient;
import com.capgemini.csd.tippkick.tippabgabe.clients.to.MatchTo;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "spielplan", port = "7080")
@SpringBootTest( properties = {"h2.tcp.enabled=false"})
@EmbeddedKafka(partitions = 1)
public class SpielplanConsumerTest {
    @Autowired
    private SpielplanClient spielplanClient;

   @Pact(consumer = "tippabgabe", provider = "spielplan")
    public RequestResponsePact matchExists(PactDslWithProvider builder) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        return builder
                .given("match with id 1 exists")
                .uponReceiving("request to return match with id")
                .path("/match/1")
                .method("GET")
                .willRespondWith()
                .status(200)
                .headers(headers)
                .body(new PactDslJsonBody()
                        .numberValue("matchId", 1)
                        .stringMatcher("hometeam", "[A-Z]{3}", "GER")
                        .stringMatcher("foreignteam", "[A-Z]{3}", "POR")
                )
                .toPact();
    }

    @Pact(consumer = "tippabgabe", provider = "spielplan")
    public RequestResponsePact matchDoesNotExist(PactDslWithProvider builder) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        return builder
                .given("match with id 2 does not exist")
                .uponReceiving("request to return match with id")
                .path("/match/2")
                .method("GET")
                .willRespondWith()
                .status(404)
                .headers(headers)
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "matchExists")
    void verifyCaseMatchExists() {
        Optional<MatchTo> match = spielplanClient.findMatch(1);
        Assertions.assertThat(match.isPresent()).isTrue();

    }

    @Test
    @PactTestFor(pactMethod = "matchDoesNotExist")
    void verifyCaseMatchDoesNorExist() {
        Optional<MatchTo> match = spielplanClient.findMatch(2);
        Assertions.assertThat(match.isPresent()).isFalse();

    }
}