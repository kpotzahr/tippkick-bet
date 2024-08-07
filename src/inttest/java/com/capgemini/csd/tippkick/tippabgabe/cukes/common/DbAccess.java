package com.capgemini.csd.tippkick.tippabgabe.cukes.common;

import com.capgemini.csd.tippkick.tippabgabe.cukes.steps.to.BetTestTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Component
public class DbAccess {
    private static final String CLOSED_MATCH_TABLE = "CLOSED_MATCH";
    private static final String GAME_BET_TABLE = "GAME_BET";
    private static final String[] TABLES = {GAME_BET_TABLE, CLOSED_MATCH_TABLE};

    private int betCounter = 10000;

    @Value("${spring.datasource.url:jdbc:h2:tcp://localhost:7091/file:~/tippabgabe}")
    private String dbUrl;

    @Value("${spring.datasource.username:sa}")
    private String dbUser;

    @Value("${spring.datasource.password:}")
    private String dbPassword;

    void cleanupData() throws SQLException {
        for (String table : TABLES) {
            executeSQL("delete from " + table);
        }
    }

    public void deleteClosedGame(long id) throws SQLException {
        executeSQL("delete from " + CLOSED_MATCH_TABLE + " where MATCH_ID =" + id);
    }

    public void insertClosedGame(long id) throws SQLException {
        executeSQL("insert into " + CLOSED_MATCH_TABLE + " (MATCH_ID) values (" + id + ")");
    }

    public void insertBet(BetTestTO bet) throws SQLException {
        executeSQL(String.format("insert into " + GAME_BET_TABLE
                        + " (ID, CREATED, MATCH_ID, OWNER_ID, HOMETEAM_SCORE, FOREIGNTEAM_SCORE) "
                        + " values (%d, current_timestamp, %d, %d, %d, %d)"
                , betCounter++, bet.getMatchId(), bet.getOwnerId(), bet.getHometeamScore(), bet.getForeignteamScore()));
    }

    public String selectBetResult(long matchId, long userId) throws SQLException {
        Map<String, Object> result = executeSelectSQL(String.format("select HOMETEAM_SCORE, FOREIGNTEAM_SCORE from %s where OWNER_ID = %d AND MATCH_ID = %d",
                GAME_BET_TABLE, userId, matchId));
        return result.get("HOMETEAM_SCORE") + ":" + result.get("FOREIGNTEAM_SCORE");
    }

    private void executeSQL(String sql) throws SQLException {
        Properties connectionProps = new Properties();
        connectionProps.put("user", this.dbUser);
        connectionProps.put("password", this.dbPassword);

        try (Connection connection = DriverManager.getConnection(dbUrl, connectionProps);
             Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }

    private Map<String, Object> executeSelectSQL(String sql) throws SQLException {
        Properties connectionProps = new Properties();
        connectionProps.put("user", this.dbUser);
        connectionProps.put("password", this.dbPassword);

        try (Connection connection = DriverManager.getConnection(dbUrl, connectionProps);
             Statement stmt = connection.createStatement()) {
            ResultSet resultSet = stmt.executeQuery(sql);
            Map<String, Object> result = new HashMap<>();
            if (resultSet.next()) {
                int columnCount = resultSet.getMetaData().getColumnCount();
                for (int col = 1; col <= columnCount; col++) {
                    result.put(resultSet.getMetaData().getColumnName(col), resultSet.getObject(col));
                }
            }
            return result;
        }
    }


}
