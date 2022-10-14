package com.vaadin.labs.konflikter.data.service;

import com.vaadin.labs.konflikter.data.entity.SampleAddress;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SampleAddressRepository extends JpaRepository<SampleAddress, UUID> {

}