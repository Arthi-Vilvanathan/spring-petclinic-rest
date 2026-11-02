/*
 * Copyright 2002-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.repository.springdatajpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.samples.petclinic.model.Prescription;
import org.springframework.samples.petclinic.model.PrescriptionStatus;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

/**
 * Repository interface for Prescription entity using Spring Data JPA.
 * Follows Spring Data naming conventions.
 *
 * @author PetClinic Domain Advisor
 */
@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Integer> {

    /**
     * Find all prescriptions for a specific pet
     *
     * @param petId the pet's ID
     * @return list of prescriptions
     */
    List<Prescription> findByPetId(Integer petId);

    /**
     * Find prescriptions for a pet filtered by status
     *
     * @param petId  the pet's ID
     * @param status the prescription status
     * @return list of prescriptions
     */
    List<Prescription> findByPetIdAndStatus(Integer petId, PrescriptionStatus status);

    /**
     * Find all active prescriptions for a pet that are not expired
     *
     * @param petId the pet's ID
     * @param now   current timestamp
     * @return list of active prescriptions
     */
    @Query("SELECT p FROM Prescription p WHERE p.pet.id = :petId AND p.status = 'ACTIVE' AND p.expirationDate > :now")
    List<Prescription> findActivePrescriptionsByPetId(@Param("petId") Integer petId, @Param("now") Instant now);

    /**
     * Find all prescriptions by veterinarian
     *
     * @param vetId the veterinarian's ID
     * @return list of prescriptions
     */
    List<Prescription> findByVeterinarianId(Integer vetId);

    /**
     * Find all prescriptions for a specific visit
     *
     * @param visitId the visit ID
     * @return list of prescriptions
     */
    List<Prescription> findByVisitId(Integer visitId);

    /**
     * Find all expired prescriptions with ACTIVE status
     *
     * @param now current timestamp
     * @return list of expired prescriptions
     */
    @Query("SELECT p FROM Prescription p WHERE p.status = 'ACTIVE' AND p.expirationDate <= :now")
    List<Prescription> findExpiredPrescriptions(@Param("now") Instant now);

    /**
     * Count active prescriptions for a pet
     *
     * @param petId the pet's ID
     * @return count of active prescriptions
     */
    @Query("SELECT COUNT(p) FROM Prescription p WHERE p.pet.id = :petId AND p.status = 'ACTIVE'")
    long countActivePrescriptionsByPetId(@Param("petId") Integer petId);
}
