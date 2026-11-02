-- Migration script for Prescription and PrescriptionRefill tables
-- Spring PetClinic - Prescription Refills Feature
-- JIRA-402: Veterinary Prescription Refills Workflow

-- Create prescriptions table
CREATE TABLE IF NOT EXISTS prescriptions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    visit_id INT NOT NULL,
    pet_id INT NOT NULL,
    vet_id INT NOT NULL,
    medication_name VARCHAR(200) NOT NULL,
    dosage VARCHAR(100) NOT NULL,
    frequency VARCHAR(100) NOT NULL,
    quantity INT NOT NULL,
    instructions VARCHAR(1000),
    prescribed_date TIMESTAMP NOT NULL,
    expiration_date TIMESTAMP NOT NULL,
    max_refills INT NOT NULL,
    remaining_refills INT NOT NULL,
    status VARCHAR(20) NOT NULL,
    version BIGINT DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_prescription_visit FOREIGN KEY (visit_id) REFERENCES visits(id) ON DELETE CASCADE,
    CONSTRAINT fk_prescription_pet FOREIGN KEY (pet_id) REFERENCES pets(id) ON DELETE CASCADE,
    CONSTRAINT fk_prescription_vet FOREIGN KEY (vet_id) REFERENCES vets(id) ON DELETE RESTRICT,
    CONSTRAINT chk_prescription_status CHECK (status IN ('ACTIVE', 'EXPIRED', 'DEPLETED', 'CANCELLED')),
    CONSTRAINT chk_max_refills CHECK (max_refills >= 0 AND max_refills <= 12),
    CONSTRAINT chk_remaining_refills CHECK (remaining_refills >= 0),
    INDEX idx_prescription_visit (visit_id),
    INDEX idx_prescription_pet (pet_id),
    INDEX idx_prescription_vet (vet_id),
    INDEX idx_prescription_status (status),
    INDEX idx_prescription_expiration (expiration_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create prescription_refills table
CREATE TABLE IF NOT EXISTS prescription_refills (
    id INT AUTO_INCREMENT PRIMARY KEY,
    prescription_id INT NOT NULL,
    requested_date TIMESTAMP NOT NULL,
    approved_date TIMESTAMP NULL,
    dispensed_date TIMESTAMP NULL,
    approved_by_vet_id INT NULL,
    status VARCHAR(20) NOT NULL,
    notes VARCHAR(500),
    rejection_reason VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_refill_prescription FOREIGN KEY (prescription_id) REFERENCES prescriptions(id) ON DELETE CASCADE,
    CONSTRAINT fk_refill_vet FOREIGN KEY (approved_by_vet_id) REFERENCES vets(id) ON DELETE SET NULL,
    CONSTRAINT chk_refill_status CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED', 'DISPENSED', 'CANCELLED')),
    INDEX idx_refill_prescription (prescription_id),
    INDEX idx_refill_status (status),
    INDEX idx_refill_requested_date (requested_date),
    INDEX idx_refill_approved_by (approved_by_vet_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert sample data for testing

-- Sample prescriptions
INSERT INTO prescriptions (visit_id, pet_id, vet_id, medication_name, dosage, frequency, quantity, instructions, prescribed_date, expiration_date, max_refills, remaining_refills, status)
SELECT 
    1, 
    1, 
    1, 
    'Amoxicillin', 
    '500mg', 
    'Twice daily', 
    30, 
    'Take with food. Complete full course.',
    NOW(),
    DATE_ADD(NOW(), INTERVAL 6 MONTH),
    3,
    3,
    'ACTIVE'
WHERE EXISTS (SELECT 1 FROM visits WHERE id = 1 LIMIT 1)
AND EXISTS (SELECT 1 FROM pets WHERE id = 1 LIMIT 1)
AND EXISTS (SELECT 1 FROM vets WHERE id = 1 LIMIT 1);

INSERT INTO prescriptions (visit_id, pet_id, vet_id, medication_name, dosage, frequency, quantity, instructions, prescribed_date, expiration_date, max_refills, remaining_refills, status)
SELECT 
    2, 
    2, 
    2, 
    'Carprofen', 
    '75mg', 
    'Once daily', 
    60, 
    'Administer with food to reduce stomach upset.',
    NOW(),
    DATE_ADD(NOW(), INTERVAL 12 MONTH),
    6,
    5,
    'ACTIVE'
WHERE EXISTS (SELECT 1 FROM visits WHERE id = 2 LIMIT 1)
AND EXISTS (SELECT 1 FROM pets WHERE id = 2 LIMIT 1)
AND EXISTS (SELECT 1 FROM vets WHERE id = 2 LIMIT 1);

-- Sample refill requests
INSERT INTO prescription_refills (prescription_id, requested_date, status, notes)
SELECT 
    p.id,
    DATE_SUB(NOW(), INTERVAL 2 DAY),
    'PENDING',
    'Running low on medication'
FROM prescriptions p
WHERE p.medication_name = 'Amoxicillin'
AND p.status = 'ACTIVE'
LIMIT 1;

INSERT INTO prescription_refills (prescription_id, requested_date, approved_date, approved_by_vet_id, status, notes)
SELECT 
    p.id,
    DATE_SUB(NOW(), INTERVAL 5 DAY),
    DATE_SUB(NOW(), INTERVAL 4 DAY),
    p.vet_id,
    'APPROVED',
    'Approved for refill'
FROM prescriptions p
WHERE p.medication_name = 'Carprofen'
AND p.status = 'ACTIVE'
LIMIT 1;

-- Grant permissions (adjust as needed for your security setup)
-- GRANT SELECT, INSERT, UPDATE, DELETE ON prescriptions TO 'petclinic_user'@'%';
-- GRANT SELECT, INSERT, UPDATE, DELETE ON prescription_refills TO 'petclinic_user'@'%';

-- Verification queries
SELECT COUNT(*) as prescription_count FROM prescriptions;
SELECT COUNT(*) as refill_count FROM prescription_refills;

-- End of migration script
