package com.capgemini.csd.tippkick.tippabgabe.domain;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface GameBetRepository extends CrudRepository<GameBet, Long> {

    List<GameBet> findAllByMatchId(long matchId);
}
