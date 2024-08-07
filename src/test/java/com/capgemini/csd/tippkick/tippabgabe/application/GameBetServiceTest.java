package com.capgemini.csd.tippkick.tippabgabe.application;

import com.capgemini.csd.tippkick.tippabgabe.clients.SpielplanClient;
import com.capgemini.csd.tippkick.tippabgabe.clients.to.MatchTo;
import com.capgemini.csd.tippkick.tippabgabe.domain.ClosedMatch;
import com.capgemini.csd.tippkick.tippabgabe.domain.ClosedMatchRepository;
import com.capgemini.csd.tippkick.tippabgabe.domain.GameBet;
import com.capgemini.csd.tippkick.tippabgabe.domain.GameBetRepository;
import com.capgemini.csd.tippkick.tippabgabe.domain.events.FinalizeGameBetEvent;
import com.capgemini.csd.tippkick.tippabgabe.values.MatchIsClosedException;
import com.capgemini.csd.tippkick.tippabgabe.values.MatchNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameBetServiceTest {

    @Mock
    private GameBetRepository gameBetRepository;
    @Mock
    private ClosedMatchRepository closedMatchRepository;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private SpielplanClient spielplanClient;

    @InjectMocks
    private GameBetService service;

    @Captor
    private ArgumentCaptor<ClosedMatch> closedMatchCaptor;
    @Captor
    private ArgumentCaptor<GameBet> gameBetCaptor;
    @Captor
    private ArgumentCaptor<FinalizeGameBetEvent> finalizeGameBetCaptor;


    @Test
    void shouldBetForGame() {
        MatchTo match = new MatchTo();
        when(gameBetRepository.save(any())).then(returnsFirstArg());
        when(spielplanClient.findMatch(anyLong())).thenReturn(Optional.of(match));

        service.betForMatch(1L, 2L, 3, 4);

        verify(gameBetRepository).save(gameBetCaptor.capture());
        GameBet createdEntity = gameBetCaptor.getValue();

        assertEquals(1L, createdEntity.getMatchId());
        assertEquals(2L, createdEntity.getOwnerId());
        assertEquals(3, createdEntity.getHometeamScore());
        assertEquals(4, createdEntity.getForeignteamScore());
        verify(spielplanClient).findMatch(1L);
    }

    @Test
    void shouldThrowExceptionIfGameIsClosed() {
        when(closedMatchRepository.findById(any())).thenReturn(Optional.of(new ClosedMatch(4711)));

        assertThrows(MatchIsClosedException.class, () ->
                service.betForMatch(4711L, 2L, 3, 4)
        );
    }

    @Test
    void shouldThrowExceptionIfMatchDoesNotExist() {
        assertThrows(MatchNotFoundException.class, () ->
                service.betForMatch(4711L, 2L, 3, 4)
        );
    }

    @Test
    void shouldCloseMatch() {
        when(gameBetRepository.findAllByMatchId(anyLong())).thenReturn(Arrays.asList(
                GameBet.builder().matchId(4711).ownerId(1).hometeamScore(2).foreignteamScore(3).build(),
                GameBet.builder().matchId(4711).ownerId(2).hometeamScore(0).foreignteamScore(0).build()
        ));

        service.closeBetPeriod(4711L);

        verify(closedMatchRepository).save(closedMatchCaptor.capture());
        assertEquals(4711, closedMatchCaptor.getValue().getMatchId());
        verify(eventPublisher, times(2)).publishEvent(finalizeGameBetCaptor.capture());
        assertThat(finalizeGameBetCaptor.getAllValues())
                .extracting(
                        FinalizeGameBetEvent::getMatchId,
                        FinalizeGameBetEvent::getOwnerId,
                        FinalizeGameBetEvent::getHometeamScore,
                        FinalizeGameBetEvent::getForeignteamScore
                )
                .contains(
                        tuple(4711L, 1L, 2, 3),
                        tuple(4711L, 2L, 0, 0)
                );
    }
}