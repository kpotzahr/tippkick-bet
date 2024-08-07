package com.capgemini.csd.tippkick.tippabgabe.cukes.common;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.requests.IsolationLevel;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;

import javax.annotation.PreDestroy;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class KafkaReceiver implements Runnable {
    private static final int STANDARD_RETRY_COUNTER = 1000;
    private static final int STANDARD_RETRY_TIMEOUT = 10;
    private static final int STANDARD_TIME_OUT = STANDARD_RETRY_COUNTER * STANDARD_RETRY_TIMEOUT;
    private static final int STANDARD_SHORT_TIMEOUT = 1000;
    private static final int AUTO_COMMIT_INTERVAL = 100;
    private static final int SESSION_TIMEOUT_MS = 15_000;
    private static final int INIT_TIMEOUT = 3000;

    private KafkaConsumer<Long, String> consumer;
    private List<String> receivedEvents = Collections.synchronizedList(new ArrayList<>());
    private String[] topics;

    private static final IsolationLevel isolationLevel = IsolationLevel.READ_UNCOMMITTED;

    private String url;
    private String group;
    private Class deserializerClass = StringDeserializer.class;

    /**
     * Construct a new receiver for Kafka events. The listening to the events is not started, yet. It needs to be
     * started/ stopped by the corresponding methods start() and stop().
     *
     * @param url    url of the kafka broker to which the receiver will connect itself on start()
     * @param topics topics for which the listener will consume events
     */
    public KafkaReceiver(String url, String... topics) {
        this.topics = Arrays.copyOf(topics, topics.length);
        this.url = url;
        this.group = UUID.randomUUID().toString();
    }


    @Override
    public void run() {
        try {
            while (true) {
                ConsumerRecords<Long, String> records = consumer.poll(0);
                for (ConsumerRecord<Long, String> record : records) {
                    log.debug("Event Consumed: {}", record.value());
                    receivedEvents.add(record.value());
                }
            }
        } catch (WakeupException e) {
            // ignore for shutdown
            log.debug("error found", e);
        } catch (Exception e) {
            log.warn("Exception stopped event polling", e);
        } finally {
            consumer.close();
            consumer = null;
        }
    }

    /**
     * Starts the listening for events of the topics that have been passed on construction.
     */
    public void start() {
        receivedEvents.clear();
        if (null == consumer) {
            log.debug("Start Kafka Receiver: {} {} {}", url, group, topics);
            Properties props = new Properties();
            List<String> topicsToListenTo = Arrays.asList(topics);
            props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, url);
            props.put(ConsumerConfig.GROUP_ID_CONFIG, group);
            props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
            props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, AUTO_COMMIT_INTERVAL);
            props.put(ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG, SESSION_TIMEOUT_MS + 1);
            props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, SESSION_TIMEOUT_MS);
            props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class);
            props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, deserializerClass);
            // support kafka exactly once semantic
            props.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, isolationLevel.name().toLowerCase(Locale.ROOT));
            consumer = new KafkaConsumer<>(props);
            consumer.subscribe(topicsToListenTo);

            ExecutorService executor = Executors.newFixedThreadPool(1);
            executor.submit(this);

            try {
                Thread.sleep(INIT_TIMEOUT);
            } catch (InterruptedException exc) {
                log.warn("error found", exc);
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Stops the listening for events and cleans up all resources.
     */
    @PreDestroy
    public void stop() {
        log.info("Received Events on shutdown for topic {}: {}", topics, receivedEvents.size());
        if (null != consumer) {
            consumer.wakeup();
        }
        receivedEvents.clear();
    }


    /**
     * Returns the events that have been consumed since starting the receiver that match the filter . If the expected
     * number of such events has not been received, yet, the method waits a standard timeout. During this time it will
     * be checked regularly for new events.
     *
     * @param expectedNumber expected minimum number of events
     * @return the consumed events or an empty list, if no events of the given type have been processed.
     */
    public List<String> getReceivedEventsWithTimeout(int expectedNumber) {
        int timeout = expectedNumber == 0 ? STANDARD_SHORT_TIMEOUT : STANDARD_TIME_OUT;
        return getReceivedEventsWithTimeout(expectedNumber, timeout);
    }

    /**
     * Returns the events that have been consumed since starting the receiver that match the filter . If the expected
     * number of such events has not been received, yet, the method waits a the given timeout. During this time it will
     * be checked regularly for new events.
     *
     * @param expectedNumber expected minimum number of events
     * @param retryTimeOut   milliseconds to wait at maximum until reurning, even if the expectedNumber of events has not
     *                       been consumed.
     * @return the consumed events or an empty list, if no events of the given type have been processed. The
     * returned list my contain more or less than expectedNumber entries. The expectedNumber only regulates
     * the waiting time.
     */
    public List<String> getReceivedEventsWithTimeout(int expectedNumber, int retryTimeOut) {
        long time = System.currentTimeMillis();
        while (System.currentTimeMillis() < time + retryTimeOut) {
            List<String> messages = new ArrayList<>(receivedEvents);
            if (finishCheckForEvents(messages.size(), expectedNumber)) {
                return messages;
            }
        }
        return new ArrayList<>();

    }

    private static boolean finishCheckForEvents(int numberOfReveivedEvents, int numberOfExpectedEvents) {
        return (0 == numberOfExpectedEvents && numberOfReveivedEvents > numberOfExpectedEvents) || (0 != numberOfExpectedEvents && numberOfReveivedEvents >= numberOfExpectedEvents);
    }
}
