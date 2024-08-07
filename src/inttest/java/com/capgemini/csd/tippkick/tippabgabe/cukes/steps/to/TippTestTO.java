package com.capgemini.csd.tippkick.tippabgabe.cukes.steps.to;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TippTestTO {
    private String tipper;
    private String spiel;
    private String ergebnis;

    public int getHometeamScore() {
        return Integer.valueOf(ergebnis.split(":")[0]);
    }

    public int getForeignteamScore() {
        return Integer.valueOf(ergebnis.split(":")[1]);
    }
}
