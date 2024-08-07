package com.capgemini.csd.tippkick.tippabgabe.cukes.steps;

import com.capgemini.csd.tippkick.tippabgabe.cukes.common.DbAccess;
import com.capgemini.csd.tippkick.tippabgabe.cukes.common.KafkaReceiver;
import com.capgemini.csd.tippkick.tippabgabe.cukes.common.KafkaSender;
import com.capgemini.csd.tippkick.tippabgabe.cukes.common.SpielplanMockServer;
import com.capgemini.csd.tippkick.tippabgabe.cukes.steps.to.BetTestTO;
import com.capgemini.csd.tippkick.tippabgabe.cukes.steps.to.TippTestTO;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@RequiredArgsConstructor
public class TippabgabeSteps {
    private static final String DEFAULT_USER = "Ich";

    private static final Map<String, Long> USERNAME2ID = ImmutableMap.of(DEFAULT_USER, 1L,
            "Ronaldo", 2L);
    private static final Map<String, Long> MATCH2ID = ImmutableMap.of("Deutschland-Brasilien", 1L,
            "Argentinien-Niederlande", 2L);

    private final TestRestTemplate restTemplate;
    private final DbAccess dbAccess;
    private final KafkaReceiver kafkaReceiver;
    private final KafkaSender kafkaSender;
    private final SpielplanMockServer mockServer;

    private HttpStatus statusFromBet;
    private long currentMatch;

    @Before
    public void initSpielplan() {
        mockServer.reset();
        for (Long matchId : MATCH2ID.values()) {
            mockServer.mockMatchExists(matchId);
        }
    }

    @Given("^Spielplan ist nicht verfügbar$")
    public void spielplanNotAvailable() {
        mockServer.mockSpielplanNotAvailable();
    }

    @Given("^\"([^\"]*)\" wurde noch nicht gestartet$")
    public void prepareMatchNotStarted(String matchName) throws SQLException {
        if (MATCH2ID.containsKey(matchName)) {
            dbAccess.deleteClosedGame(MATCH2ID.get(matchName));
        }
    }

    @Given("^\"([^\"]*)\" wurde bereits gestartet$")
    public void prepareMatchStarted(String matchName) throws SQLException {
        dbAccess.insertClosedGame(MATCH2ID.get(matchName));
    }

    @Given("^Abgegebene Tipps:$")
    public void abgegebeneTipps(List<TippTestTO> givenBets) throws SQLException {
        for (TippTestTO givenBet : givenBets) {
            BetTestTO bet = BetTestTO.builder()
                    .hometeamScore(givenBet.getHometeamScore())
                    .foreignteamScore(givenBet.getForeignteamScore())
                    .ownerId(USERNAME2ID.get(givenBet.getTipper()))
                    .matchId(MATCH2ID.get(givenBet.getSpiel()))
                    .build();
            dbAccess.insertBet(bet);
        }
    }

    @When("^ich tippe für \"([^\"]*)\" (\\d+):(\\d+)$")
    public void createBet(String matchName, int hometeamScore, int foreignteamScore) {
        BetTestTO bet = BetTestTO.builder()
                .hometeamScore(hometeamScore)
                .foreignteamScore(foreignteamScore)
                .ownerId(USERNAME2ID.get(DEFAULT_USER))
                .build();
        currentMatch = MATCH2ID.get(matchName);

        ResponseEntity<Void> response = restTemplate.postForEntity("/tippabgabe/" + currentMatch, bet, Void.class);
        statusFromBet = response.getStatusCode();
    }

    @When("^das Spiel \"([^\"]*)\" wird gestartet$")
    public void startGame(String matchName) {
        Long matchId = MATCH2ID.get(matchName);
        Map<String, Object> eventData = ImmutableMap.of("matchId", matchId);
        kafkaSender.sendMessage(eventData, matchId);
    }


    @Then("^mein gültiger Tipp ist (\\d+):(\\d+)$")
    public void checkMyBet(int hometeamScore, int foreignteamScore) throws Throwable {
        String expectedResult = hometeamScore + ":" + foreignteamScore;
        assertThat(dbAccess.selectBetResult(currentMatch, USERNAME2ID.get(DEFAULT_USER))).isEqualTo(expectedResult);
    }

    @Then("^Folgende Tipps werden bekanntgegeben:$")
    public void betsHavenBeenPublished(List<TippTestTO> expectedBets) throws IOException {
        List<String> receivedEvents = kafkaReceiver.getReceivedEventsWithTimeout(expectedBets.size());
        assertThat(receivedEvents).hasSameSizeAs(expectedBets);

        Map<String, String> matchUser2result = new HashMap<>();
        for (TippTestTO expectedBet : expectedBets) {
            String key = getMatchUserKey(MATCH2ID.get(expectedBet.getSpiel()), USERNAME2ID.get(expectedBet.getTipper()));
            matchUser2result.put(key, expectedBet.getErgebnis());
        }

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        for (String eventBody : receivedEvents) {
            BetTestTO event = objectMapper.readValue(eventBody, BetTestTO.class);
            assertThat(event.getHometeamScore() + ":" + event.getForeignteamScore())
                    .isEqualTo(matchUser2result.get(getMatchUserKey(event.getMatchId(), event.getOwnerId())));
        }
    }

    private String getMatchUserKey(long matchId, long userId) {
        return matchId + "_" + userId;
    }

    @Then("^es wurde kein Tipp bekanntgegeben$")
    public void noBetPublished() {
        List<String> receivedEvents = kafkaReceiver.getReceivedEventsWithTimeout(0, 3000);
        assertThat(receivedEvents).hasSize(0);
    }

    @Then("^der Tipp wird nicht angenommen$")
    public void betNotAccepted() {
        assertThat(statusFromBet).isEqualTo(HttpStatus.BAD_REQUEST);
    }


}
