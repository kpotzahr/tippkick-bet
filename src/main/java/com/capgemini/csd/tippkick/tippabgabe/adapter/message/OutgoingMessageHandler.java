package com.capgemini.csd.tippkick.tippabgabe.adapter.message;

import com.capgemini.csd.tippkick.tippabgabe.domain.events.FinalizeGameBetEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutgoingMessageHandler {

    private final KafkaTemplate<Long, FinalizeGameBetEvent> sender;

    @EventListener
    public void handle(FinalizeGameBetEvent event) {
        log.info("Send final bet of owner {} for match {}", event.getOwnerId(), event.getMatchId());
        sender.send("tipp", event.getOwnerId(), event);
    }
}
