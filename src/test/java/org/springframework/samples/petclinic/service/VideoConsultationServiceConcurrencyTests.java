package org.springframework.samples.petclinic.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.samples.petclinic.rest.dto.EmergencyVideoConsultationRequestDto;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.server.ResponseStatusException;

/**
 * GENERATED TESTS (not executed here): Concurrency test for JIRA-402.
 * <p>
 * Ensures the overlap-prevention is effective under concurrent booking attempts.
 */
@SpringBootTest
@ActiveProfiles({"spring-data-jpa", "hsqldb"})
class VideoConsultationServiceConcurrencyTests {

    @Autowired
    private VideoConsultationService videoConsultationService;

    @Test
    void shouldPreventConcurrentOverlappingBookings() throws Exception {
        EmergencyVideoConsultationRequestDto request = new EmergencyVideoConsultationRequestDto()
            .vetId(1)
            .startDateTime(OffsetDateTime.parse("2026-11-03T13:00:00Z"))
            .endDateTime(OffsetDateTime.parse("2026-11-03T13:15:00Z"))
            .triageLevel(EmergencyVideoConsultationRequestDto.TriageLevelEnum.MODERATE)
            .reason("Concurrent booking test");

        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Callable<Boolean> task = () -> {
                try {
                    videoConsultationService.bookEmergency("owner1", request);
                    return true;
                } catch (ResponseStatusException ex) {
                    if (ex.getStatusCode() == HttpStatus.CONFLICT || ex.getStatusCode() == HttpStatus.FORBIDDEN) {
                        return false;
                    }
                    throw ex;
                }
            };

            List<Future<Boolean>> futures = new ArrayList<>();
            futures.add(executor.submit(task));
            futures.add(executor.submit(task));

            int successCount = 0;
            for (Future<Boolean> future : futures) {
                successCount += future.get() ? 1 : 0;
            }

            assertThat(successCount).isEqualTo(1);
        } catch (ExecutionException e) {
            if (e.getCause() instanceof ResponseStatusException rse) {
                // unexpected error
                throw rse;
            }
            throw e;
        } finally {
            executor.shutdownNow();
        }
    }
}
