package com.app.kafka.repositories;

import com.app.kafka.entity.LibraryEvent;
import org.springframework.data.repository.CrudRepository;

public interface LibraryEventRepository extends CrudRepository<LibraryEvent, Integer> {
}
