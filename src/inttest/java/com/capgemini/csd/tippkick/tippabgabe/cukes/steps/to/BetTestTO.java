package com.capgemini.csd.tippkick.tippabgabe.cukes.steps.to;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BetTestTO {
    private int foreignteamScore;
    private int hometeamScore;
    private long ownerId;
    private long matchId;

}
