package org.springframework.samples.petclinic.repository.springdatajpa;

import java.time.OffsetDateTime;

import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import org.springframework.samples.petclinic.model.VetAvailabilitySlot;
import org.springframework.samples.petclinic.repository.VetAvailabilitySlotRepository;

@Profile("spring-data-jpa")
public interface SpringDataVetAvailabilitySlotRepository extends VetAvailabilitySlotRepository, Repository<VetAvailabilitySlot, Integer> {

    @Override
    @Query("select (count(s) > 0) from VetAvailabilitySlot s where s.vet.id = :vetId and s.startDateTime <= :start and s.endDateTime >= :end")
    boolean existsSlotCovering(@Param("vetId") int vetId,
                               @Param("start") OffsetDateTime start,
                               @Param("end") OffsetDateTime end);
}
