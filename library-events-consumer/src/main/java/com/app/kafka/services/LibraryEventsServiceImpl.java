package com.app.kafka.services;

import com.app.kafka.entity.LibraryEvent;
import com.app.kafka.repositories.LibraryEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.hibernate.cfg.RecoverableException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.Optional;

@Service
@Slf4j
public class LibraryEventsServiceImpl implements LibraryEventsService {
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LibraryEventRepository libraryEventRepository;

    @Autowired
    private KafkaTemplate<Integer, String> kafkaTemplate;

    @Override
    public void processLibraryEvent(ConsumerRecord<Integer, String> consumerRecord) throws JsonProcessingException {
        LibraryEvent libraryEvent = objectMapper.readValue(consumerRecord.value(), LibraryEvent.class);
        log.info("libraryEvent: {} ", libraryEvent);

        if(libraryEvent.getLibraryEventId() != null && libraryEvent.getLibraryEventId() == 000)
            throw new RecoverableDataAccessException("Temporary network issue");

        switch(libraryEvent.getLibraryEventType()) {
            case NEW:
                //save operation
                save(libraryEvent);
                break;
            case UPDATE:
                //validate the libraryevent
                validate(libraryEvent);
                //save
                save(libraryEvent);
                break;
            default:
                log.info("Invalid Library Event Type");
        }
    }

    @Override
    public void handleRecovery(ConsumerRecord<Integer, String> consumerRecord) {
        Integer key = consumerRecord.key();
        String message = consumerRecord.value();


        ListenableFuture<SendResult<Integer, String>> listenableFuture = kafkaTemplate.sendDefault(key, message);
        listenableFuture.addCallback(new ListenableFutureCallback<SendResult<Integer, String>>() {

            @Override
            public void onSuccess(SendResult<Integer, String> result) {
                handleSuccess(key, message, result);
            }

            @Override
            public void onFailure(Throwable ex) {
                handleFailure(key, message, ex);
            }
        });

    }

    private void validate(LibraryEvent libraryEvent) {
        if(libraryEvent.getLibraryEventId() == null) {
            throw new IllegalArgumentException("LibraryEvent ID is missing");
        }

        Optional<LibraryEvent> libraryEventOptional = libraryEventRepository.findById(libraryEvent.getLibraryEventId());
        if(!libraryEventOptional.isPresent()) {
            throw new IllegalArgumentException("Not a valid library event");
        }

        log.info("Validation is successful for the libraryEvent: {} ", libraryEventOptional.get());
    }

    private void save(LibraryEvent libraryEvent) {
        libraryEvent.getBook().setLibraryEvent(libraryEvent);
        libraryEventRepository.save(libraryEvent);
        log.info("Successfully persisted the library event {} ", libraryEvent);
    }

    private void handleSuccess(Integer key, String value, SendResult<Integer, String> result) {
        log.info("Message sent successfully for the key: {} and the value is: {}, " + "partition is: {}", key, value,
                result.getRecordMetadata().partition());
    }

    private void handleFailure(Integer key, String value, Throwable ex) {
        log.error("Error sending the Message and the exception is: {}", ex.getMessage());

        try {
            throw ex;
        } catch (Throwable throwable) {
            log.error("Error in OnFailure: {}", throwable.getMessage());
        }
    }
}
