package com.personal.esttimeconverter.session;

import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Holds the batch of session rows built up before download — now backed by
 * the database (H2 locally, PostgreSQL in production) instead of an
 * in-memory list, so rows aren't lost if the app restarts before you've
 * downloaded them.
 */
@Service
public class SessionService {

    private final SessionRepository repository;

    public SessionService(SessionRepository repository) {
        this.repository = repository;
    }

    public void add(SessionRow row) {
        repository.save(row);
    }

    public List<SessionRow> getAll() {
        return repository.findAllByOrderByIdAsc();
    }

    public void clear() {
        repository.deleteAll();
    }
}
