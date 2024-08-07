package com.capgemini.csd.tippkick.tippabgabe.values;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class MatchNotFoundException extends RuntimeException {

    public MatchNotFoundException(long matchId) {
        super(String.format("Match with id %d does not exist.", matchId));
    }

}
