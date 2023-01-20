package com.vaadin.labs.konflikter.data.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.vaadin.labs.konflikter.data.entity.SampleEntity;

@Service
public class SampleEntityService {

    private final SampleEntityRepository repository;

    @Autowired
    public SampleEntityService(SampleEntityRepository repository) {
        this.repository = repository;
    }

    public Optional<SampleEntity> get(Long id) {
        return repository.findById(id);

    }

    public SampleEntity update(SampleEntity entity) {
        return repository.save(entity);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public Page<SampleEntity> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public int count() {
        return (int) repository.count();
    }

}
