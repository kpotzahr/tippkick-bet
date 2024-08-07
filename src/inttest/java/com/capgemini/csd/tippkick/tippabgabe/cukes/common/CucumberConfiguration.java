package com.capgemini.csd.tippkick.tippabgabe.cukes.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.util.DefaultUriBuilderFactory;

@Configuration
@ComponentScan("com.capgemini.csd.tippkick.tippabgabe.cukes.*")
public class CucumberConfiguration {
    @Bean
    public KafkaReceiver kafkaReceiver(@Value("${spring.embedded.kafka.brokers:localhost:9092}") String kafkaUrl) {
        return new KafkaReceiver(kafkaUrl, "tipp");
    }

    @Bean
    public KafkaSender kafkaSender(@Value("${spring.embedded.kafka.brokers:localhost:9092}") String kafkaUrl) {
        return new KafkaSender(kafkaUrl, "match-started");
    }

    @Bean
    public TestRestTemplate testRestTemplate(@Value("${application.url:http://localhost:7081}") String appUrl) {
        TestRestTemplate restTemplate = new TestRestTemplate();
        restTemplate.setUriTemplateHandler(new DefaultUriBuilderFactory(appUrl));
        return restTemplate;
    }

}
