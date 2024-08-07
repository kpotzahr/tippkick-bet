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
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class GameBetService {

    private final GameBetRepository gameBetRepository;
    private final ClosedMatchRepository closedMatchRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final SpielplanClient spielplanClient;

    public long betForMatch(long matchId, long ownerId, int hometeamScore, int foreignteamScore) {
        verifyMatch(matchId);

        return gameBetRepository.save(GameBet.builder()
                .matchId(matchId)
                .ownerId(ownerId)
                .hometeamScore(hometeamScore)
                .foreignteamScore(foreignteamScore)
                .build())
                .getId();
    }

    private MatchTo verifyMatch(long matchId) {
        if (closedMatchRepository.findById(matchId).isPresent()) {
            throw new MatchIsClosedException(matchId);
        }
        return spielplanClient.findMatch(matchId).
                orElseThrow(() -> new MatchNotFoundException(matchId));

    }

    public void closeBetPeriod(long matchId) {
        closedMatchRepository.save(new ClosedMatch(matchId));

        gameBetRepository.findAllByMatchId(matchId).forEach(this::finalizeGameBet);
    }

    private void finalizeGameBet(GameBet gameBet) {
        eventPublisher.publishEvent(FinalizeGameBetEvent.builder()
                .matchId(gameBet.getMatchId())
                .ownerId(gameBet.getOwnerId())
                .hometeamScore(gameBet.getHometeamScore())
                .foreignteamScore(gameBet.getForeignteamScore())
                .build());
    }

}
