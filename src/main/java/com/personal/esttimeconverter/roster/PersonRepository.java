package com.personal.esttimeconverter.roster;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PersonRepository extends JpaRepository<Person, Long> {

    List<Person> findByRoleOrderByNameAsc(PersonRole role);

    Optional<Person> findByEmailIgnoreCaseAndRole(String email, PersonRole role);
}
