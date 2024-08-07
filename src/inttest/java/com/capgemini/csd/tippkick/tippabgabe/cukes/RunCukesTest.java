package com.capgemini.csd.tippkick.tippabgabe.cukes;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.springframework.kafka.test.rule.EmbeddedKafkaRule;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = "src/inttest/resources",
        tags = {"not @skip"},
        glue = {"com.capgemini.csd.tippkick.tippabgabe.cukes"},
        plugin = {"pretty", "html:build/cucumber-reports"}
)
public class RunCukesTest {
    @ClassRule
    public static EmbeddedKafkaRule embeddedKafka = new EmbeddedKafkaRule(1, true, "tippkick");
}
