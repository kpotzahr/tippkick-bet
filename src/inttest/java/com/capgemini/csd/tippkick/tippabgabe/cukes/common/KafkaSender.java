package com.capgemini.csd.tippkick.tippabgabe.cukes.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class KafkaSender {
    private static final int MEMORY_BUFFER = 3_3554_432;
    private static final int LINGER_MS = 1;
    private static final int BATCH_SIZE = 16_384;
    private static final int RETRIES_COUNT = 0;
    private static final String ACKS_CONFIG = "all";
    private static final int EVENT_PROCESS_TIME_OUT = 2000;

    private String url;
    private String topic;
    private ObjectMapper objectMapper;

    KafkaSender(String url, String topic) {
        this.topic = topic;
        this.url = url;
        this.objectMapper = new ObjectMapper();
    }

    public void sendMessage(Object message, long key) {
        sendMessage(message, key, EVENT_PROCESS_TIME_OUT);
    }

    public void sendMessage(Object message, long key, int timeout) {
        try {
            KafkaProducer<Long, String> producer = producerTestFactory();
            String payloadString = objectMapper.writeValueAsString(message);
            producer.send(new ProducerRecord<>(topic, key, payloadString));
            producer.flush();
            producer.close();

            if (timeout > 0) {
                Thread.sleep(timeout);
            }
        } catch (JsonProcessingException | InterruptedException exc) {
            throw new IllegalStateException("Something went wrong when sending generic Kafka-message " + message, exc);
        }
    }

    private KafkaProducer<Long, String> producerTestFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, url);
        props.put(ProducerConfig.RETRIES_CONFIG, RETRIES_COUNT);
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, BATCH_SIZE);
        props.put(ProducerConfig.LINGER_MS_CONFIG, LINGER_MS);
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, MEMORY_BUFFER);
        props.put(ProducerConfig.ACKS_CONFIG, ACKS_CONFIG);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return new KafkaProducer<>(props);
    }

}
