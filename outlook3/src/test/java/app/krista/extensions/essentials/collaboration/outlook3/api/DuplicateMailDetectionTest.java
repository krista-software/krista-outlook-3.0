/*
 * Outlook 3.0 Extension for Krista
 * Copyright (C) 2025 Krista Software
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package app.krista.extensions.essentials.collaboration.outlook3.api;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for thread-safe duplicate mail detection in OutlookApiResource.
 * Verifies that the self-evicting LinkedHashMap-backed set correctly handles
 * concurrent access without race conditions (KE-2869 fix).
 */
class DuplicateMailDetectionTest {

    @Test
    void testIsMessageIdTriggered_NewMessageId_ShouldNotBeTriggered() {

        // A random UUID should not exist in the triggered set
        String uniqueId = UUID.randomUUID().toString();
        assertFalse(OutlookApiResource.isMessageIdTriggered(uniqueId));
    }

    @Test
    void testConcurrentAccess_ShouldNotThrowException() throws InterruptedException {

        // Arrange - simulate concurrent webhook notifications
        int threadCount = 50;
        int messagesPerThread = 20;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        List<Exception> exceptions = Collections.synchronizedList(new ArrayList<>());

        // Act - all threads start simultaneously and check/add message IDs
        for (int t = 0; t < threadCount; t++) {

            final int threadIdx = t;
            executor.submit(() -> {

                try {
                    startLatch.await();
                    for (int m = 0; m < messagesPerThread; m++) {

                        String messageId = "thread-" + threadIdx + "-msg-" + m;
                        // Call the static method to verify no ConcurrentModificationException
                        OutlookApiResource.isMessageIdTriggered(messageId);
                    }
                } catch (Exception e) {

                    exceptions.add(e);
                } finally {

                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        assertTrue(doneLatch.await(10, TimeUnit.SECONDS), "All threads should complete within 10 seconds");
        executor.shutdown();

        // Assert - no exceptions should have been thrown
        assertTrue(exceptions.isEmpty(),
                "No exceptions should occur during concurrent access, but got: " + exceptions);
    }
}
