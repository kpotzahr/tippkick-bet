package com.capgemini.csd.tippkick.tippabgabe.configuration;

import lombok.extern.slf4j.Slf4j;
import org.h2.tools.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.sql.SQLException;

@Configuration
@ConditionalOnProperty(prefix = "h2.tcp", name = "enabled")
@Slf4j
public class H2ServerConfiguration {

    @Value("${h2.tcp.port:9093}")
    private String h2TcpPort;

    @Bean(destroyMethod = "stop")
    @ConditionalOnMissingBean
    public Server server() throws SQLException {
        log.info("Start H2 TCP Server on port {}", h2TcpPort);
        return Server.createTcpServer("-tcpPort", h2TcpPort, "-tcpAllowOthers").start();
    }

}
