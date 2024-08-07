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
class ClosedMatchJpaTest {


    @Autowired
    private TestEntityManager entityManager;

    @Test
    void shouldSaveAndLoadEntity() {
        ClosedMatch entity = new ClosedMatch(99);

        ClosedMatch persisted = entityManager.persistFlushFind(entity);

        assertEquals(persisted.getMatchId(), 99L);
        assertNotNull(persisted.getCreated());
    }
}