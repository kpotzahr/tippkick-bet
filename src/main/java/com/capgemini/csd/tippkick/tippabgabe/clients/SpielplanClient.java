package com.capgemini.csd.tippkick.tippabgabe.clients;

import com.capgemini.csd.tippkick.tippabgabe.clients.to.MatchTo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Optional;

@FeignClient(name = "spielplan", url = "${clients.spielplan.url}", decode404 = true)
public interface SpielplanClient {


    @GetMapping("match/{matchId}")
    Optional<MatchTo> findMatch(@PathVariable("matchId") long matchId);
}
