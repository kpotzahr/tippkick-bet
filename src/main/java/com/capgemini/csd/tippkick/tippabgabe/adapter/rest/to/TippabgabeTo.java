package com.capgemini.csd.tippkick.tippabgabe.adapter.rest.to;

import lombok.*;

import javax.validation.constraints.Min;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class TippabgabeTo {

    private long ownerId;
    @Min(0)
    private int hometeamScore;
    @Min(0)
    private int foreignteamScore;

}
