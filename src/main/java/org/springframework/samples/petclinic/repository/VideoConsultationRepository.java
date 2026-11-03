package org.springframework.samples.petclinic.repository;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.samples.petclinic.model.VideoConsultation;

/**
 * Repository for {@link VideoConsultation}.
 */
public interface VideoConsultationRepository {

    VideoConsultation save(VideoConsultation consultation) throws DataAccessException;

    List<VideoConsultation> findOverlappingForVet(int vetId, OffsetDateTime start, OffsetDateTime end) throws DataAccessException;
}
