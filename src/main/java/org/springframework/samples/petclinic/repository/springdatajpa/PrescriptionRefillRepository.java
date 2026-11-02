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
import org.springframework.samples.petclinic.model.PrescriptionRefill;
import org.springframework.samples.petclinic.model.RefillStatus;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for PrescriptionRefill entity using Spring Data JPA.
 * Follows Spring Data naming conventions.
 *
 * @author PetClinic Domain Advisor
 */
@Repository
public interface PrescriptionRefillRepository extends JpaRepository<PrescriptionRefill, Integer> {

    /**
     * Find all refills for a specific prescription
     *
     * @param prescriptionId the prescription's ID
     * @return list of refills
     */
    List<PrescriptionRefill> findByPrescriptionId(Integer prescriptionId);

    /**
     * Find refills for a prescription filtered by status
     *
     * @param prescriptionId the prescription's ID
     * @param status         the refill status
     * @return list of refills
     */
    List<PrescriptionRefill> findByPrescriptionIdAndStatus(Integer prescriptionId, RefillStatus status);

    /**
     * Find all pending refill requests ordered by request date
     *
     * @return list of pending refills
     */
    @Query("SELECT r FROM PrescriptionRefill r WHERE r.status = 'PENDING' ORDER BY r.requestedDate ASC")
    List<PrescriptionRefill> findPendingRefills();

    /**
     * Find all refills by status
     *
     * @param status the refill status
     * @return list of refills
     */
    List<PrescriptionRefill> findByStatus(RefillStatus status);

    /**
     * Find all refills approved by a specific veterinarian
     *
     * @param vetId the veterinarian's ID
     * @return list of refills
     */
    List<PrescriptionRefill> findByApprovedById(Integer vetId);
}
