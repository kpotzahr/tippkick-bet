package com.capgemini.csd.tippkick.tippabgabe.adapter.message;

import com.capgemini.csd.tippkick.tippabgabe.adapter.message.to.MatchStartedMessageTo;
import com.capgemini.csd.tippkick.tippabgabe.application.GameBetService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@KafkaListener(topics = "match-started")
@RequiredArgsConstructor
@Slf4j
public class IncommingMessageHandler {

    private final GameBetService gameBetService;
    private final ObjectMapper objectMapper;

    @KafkaHandler
    public void handle(String body) throws IOException {
        MatchStartedMessageTo message = objectMapper.readValue(body, MatchStartedMessageTo.class);
        log.info("Retrieve match started message for {}", message.getMatchId());
        gameBetService.closeBetPeriod(message.getMatchId());
    }
}
