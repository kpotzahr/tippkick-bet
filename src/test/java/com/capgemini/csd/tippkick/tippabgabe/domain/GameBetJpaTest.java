package com.capgemini.csd.tippkick.tippabgabe.domain;

import com.capgemini.csd.tippkick.tippabgabe.configuration.AuditConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
@Import(AuditConfiguration.class)
@ExtendWith(SpringExtension.class)
class GameBetJpaTest {


    @Autowired
    private TestEntityManager entityManager;

    @Test
    void shouldSaveAndLoadEntity() {
        GameBet entity = GameBet.builder()
                .matchId(1)
                .ownerId(2)
                .hometeamScore(3)
                .foreignteamScore(4)
                .build();

        GameBet persisted = entityManager.persistFlushFind(entity);

        assertEquals(persisted.getMatchId(), 1L);
        assertEquals(persisted.getOwnerId(), 2L);
        assertEquals(persisted.getHometeamScore(), 3);
        assertEquals(persisted.getForeignteamScore(), 4);
        assertNotNull(persisted.getCreated());
    }
}