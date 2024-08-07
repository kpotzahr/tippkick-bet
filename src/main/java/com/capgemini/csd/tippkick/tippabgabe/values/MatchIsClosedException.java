package com.capgemini.csd.tippkick.tippabgabe.values;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class MatchIsClosedException extends RuntimeException {

    public MatchIsClosedException(long matchId) {
        super(String.format("Match %d is already closed.", matchId));
    }

}
