package org.springframework.samples.petclinic.repository.springdatajpa;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import org.springframework.samples.petclinic.model.VideoConsultation;
import org.springframework.samples.petclinic.repository.VideoConsultationRepository;

@Profile("spring-data-jpa")
public interface SpringDataVideoConsultationRepository extends VideoConsultationRepository, Repository<VideoConsultation, Integer> {

    @Override
    @Query("select c from VideoConsultation c where c.vet.id = :vetId and c.startDateTime < :end and c.endDateTime > :start")
    List<VideoConsultation> findOverlappingForVet(@Param("vetId") int vetId,
                                                  @Param("start") OffsetDateTime start,
                                                  @Param("end") OffsetDateTime end);
}
