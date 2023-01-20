package com.vaadin.labs.konflikter.data.service;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vaadin.labs.konflikter.data.entity.SampleEntity;

public interface SampleEntityRepository extends JpaRepository<SampleEntity, Long> {

}