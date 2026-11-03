package org.springframework.samples.petclinic.repository;

import java.util.Optional;

import org.springframework.dao.DataAccessException;
import org.springframework.samples.petclinic.model.OwnerUserLink;

/**
 * Repository for verified user-to-owner links.
 */
public interface OwnerUserLinkRepository {

    Optional<OwnerUserLink> findVerifiedByUsername(String username) throws DataAccessException;

    void save(OwnerUserLink link) throws DataAccessException;
}
