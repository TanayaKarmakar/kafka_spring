package com.app.kafka.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.kafka.clients.consumer.ConsumerRecord;

public interface LibraryEventsService {
    void processLibraryEvent(ConsumerRecord<Integer, String> consumerRecord) throws JsonProcessingException;

    void handleRecovery(ConsumerRecord<Integer, String> consumerRecord);
}
