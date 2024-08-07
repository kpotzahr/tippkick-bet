package com.capgemini.csd.tippkick.tippabgabe.adapter.rest;

import com.capgemini.csd.tippkick.tippabgabe.adapter.rest.to.TippabgabeTo;
import com.capgemini.csd.tippkick.tippabgabe.application.GameBetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("tippabgabe")
@RequiredArgsConstructor
public class TippabgabeController {


    private final GameBetService service;

    @PostMapping("{matchId}")
    @ResponseStatus(HttpStatus.CREATED)
    public void tippAbgabe(@PathVariable("matchId") long matchId, @RequestBody TippabgabeTo tipp) {
        service.betForMatch(matchId, tipp.getOwnerId(), tipp.getHometeamScore(), tipp.getForeignteamScore());
    }


    @PostMapping("{matchId}/close")
    @ResponseStatus(HttpStatus.CREATED)
    public void closeMatch(@PathVariable("matchId") long matchId) {
        service.closeBetPeriod(matchId);
    }
}
