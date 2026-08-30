package com.personal.esttimeconverter.session;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class SessionService {

    private final List<SessionRow> pending = new CopyOnWriteArrayList<>();

    public void add(SessionRow row) {
        pending.add(row);
    }

    public List<SessionRow> getAll() {
        return pending;
    }

    public void clear() {
        pending.clear();
    }
}
