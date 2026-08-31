package com.personal.esttimeconverter.session;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SessionRepository extends JpaRepository<SessionRow, Long> {

    List<SessionRow> findAllByOrderByIdAsc();
}
