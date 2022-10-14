package com.vaadin.labs.konflikter.data.service;

import com.vaadin.labs.konflikter.data.entity.SamplePerson;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SamplePersonRepository extends JpaRepository<SamplePerson, UUID> {

}