package com.vaadin.labs.konflikter.data.service;

import com.vaadin.labs.konflikter.data.entity.SamplePerson;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SamplePersonRepository extends JpaRepository<SamplePerson, Long> {

}