package com.app.kafka.services;

import com.app.kafka.entity.LibraryEvent;
import com.app.kafka.repositories.LibraryEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class LibraryEventsServiceImpl implements LibraryEventsService {
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LibraryEventRepository libraryEventRepository;

    @Override
    public void processLibraryEvent(ConsumerRecord<Integer, String> consumerRecord) throws JsonProcessingException {
        LibraryEvent libraryEvent = objectMapper.readValue(consumerRecord.value(), LibraryEvent.class);
        log.info("libraryEvent: {} ", libraryEvent);

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
}
