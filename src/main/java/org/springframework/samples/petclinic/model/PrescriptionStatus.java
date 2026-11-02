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
package org.springframework.samples.petclinic.model;

/**
 * Enumeration representing the status of a prescription.
 *
 * @author PetClinic Domain Advisor
 */
public enum PrescriptionStatus {
    /**
     * Prescription is currently active and can be refilled
     */
    ACTIVE,

    /**
     * Prescription has expired and cannot be refilled
     */
    EXPIRED,

    /**
     * Prescription has used all available refills
     */
    DEPLETED,

    /**
     * Prescription has been cancelled by veterinarian
     */
    CANCELLED
}
