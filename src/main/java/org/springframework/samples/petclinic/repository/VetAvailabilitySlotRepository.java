package org.springframework.samples.petclinic.repository;

import java.time.OffsetDateTime;

import org.springframework.dao.DataAccessException;

/**
 * Repository for veterinarian video availability slots.
 */
public interface VetAvailabilitySlotRepository {

    boolean existsSlotCovering(int vetId, OffsetDateTime start, OffsetDateTime end) throws DataAccessException;
}
