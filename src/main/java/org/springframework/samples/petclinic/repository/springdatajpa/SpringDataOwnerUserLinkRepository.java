package org.springframework.samples.petclinic.repository.springdatajpa;

import java.util.Optional;

import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import org.springframework.samples.petclinic.model.OwnerUserLink;
import org.springframework.samples.petclinic.repository.OwnerUserLinkRepository;

@Profile("spring-data-jpa")
public interface SpringDataOwnerUserLinkRepository extends OwnerUserLinkRepository, Repository<OwnerUserLink, Integer> {

    @Override
    @Query("select l from OwnerUserLink l join fetch l.owner o where l.user.username = :username and l.verified = true")
    Optional<OwnerUserLink> findVerifiedByUsername(@Param("username") String username);
}
