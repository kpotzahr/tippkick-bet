package com.capgemini.csd.tippkick.tippabgabe.domain.events;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class FinalizeGameBetEvent {

    private long matchId;
    private long ownerId;
    private int hometeamScore;
    private int foreignteamScore;

}
