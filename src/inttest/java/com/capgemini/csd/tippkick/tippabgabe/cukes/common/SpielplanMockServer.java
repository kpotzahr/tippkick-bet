package com.capgemini.csd.tippkick.tippabgabe.cukes.common;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.FatalStartupException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.net.MalformedURLException;
import java.net.URL;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

@Component
@Slf4j
public class SpielplanMockServer {
    private WireMockServer wireMockServer;
    private final String spielplanUrl;

    public SpielplanMockServer(@Value("${clients.spielplan.url:http://localhost:7080}") String spielplanUrl) {
        this.spielplanUrl = spielplanUrl;
    }

    @PostConstruct
    public void initialize() throws MalformedURLException {
        URL url = new URL(spielplanUrl);
        wireMockServer = new WireMockServer(wireMockConfig().port(url.getPort()));
        try {
            wireMockServer.start();
        } catch (FatalStartupException exc) {
            log.warn("Wiremock already running");
            log.trace("Cannot start Wiremock", exc);

        }
        WireMock.configureFor(url.getHost(), url.getPort());
    }

    public void mockMatchExists(long matchId) {
        stubFor(WireMock.get(urlEqualTo("/match/" + matchId))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\":" + matchId + "}")
                        .withStatus(200)));
    }

    public void mockSpielplanNotAvailable() {
        stubFor(WireMock.get(anyUrl())
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(404)));

    }

    public void reset() {
        WireMock.reset();
    }

    @PreDestroy
    public void shutdown() {
        if (null != wireMockServer) {
            wireMockServer.stop();
        }
    }
}
