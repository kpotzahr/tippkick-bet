package com.capgemini.csd.tippkick.tippabgabe.adapter.rest;

import com.capgemini.csd.tippkick.tippabgabe.adapter.rest.to.TippabgabeTo;
import com.capgemini.csd.tippkick.tippabgabe.application.GameBetService;
import com.capgemini.csd.tippkick.tippabgabe.values.MatchIsClosedException;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(TippabgabeController.class)
@ExtendWith(SpringExtension.class)
class TippabgabeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GameBetService gameBetService;

    @Test
    void shouldInsertBet() {
        when(gameBetService.betForMatch(anyLong(), anyLong(), anyInt(), anyInt())).thenReturn(99L);

        given().mockMvc(mockMvc)
                .contentType(ContentType.JSON)
                .body(TippabgabeTo.builder().ownerId(1).hometeamScore(2).foreignteamScore(3).build())
                .when()
                .post("/tippabgabe/{matchId}", 4711)
                .then()
                .statusCode(HttpStatus.CREATED.value());

        verify(gameBetService).betForMatch(4711L, 1L, 2, 3);
    }

    @Test
    public void shouldInsertBetIfMatchIsClosed() {
        when(gameBetService.betForMatch(anyLong(), anyLong(), anyInt(), anyInt())).thenThrow(new MatchIsClosedException(4711L));

        given().mockMvc(mockMvc)
                .contentType(ContentType.JSON)
                .body(TippabgabeTo.builder().ownerId(1).hometeamScore(2).foreignteamScore(3).build())
                .when()
                .post("/tippabgabe/{matchId}", 4711)
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());

    }
}