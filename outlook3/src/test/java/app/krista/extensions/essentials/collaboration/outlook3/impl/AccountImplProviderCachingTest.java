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

package app.krista.extensions.essentials.collaboration.outlook3.impl;

import app.krista.extensions.essentials.collaboration.outlook3.impl.connectors.GraphServiceClientProvider;
import app.krista.extensions.essentials.collaboration.outlook3.impl.connectors.GraphServiceClientProviderFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test suite for AccountImpl provider caching.
 *
 * Tests verify that getProvider() uses lazy initialization and caches the
 * GraphServiceClientProvider to avoid redundant KeyValueStore calls
 * through providerFactory.create() on every invocation.
 */
@DisplayName("AccountImpl - Provider Caching Tests")
public class AccountImplProviderCachingTest {

    private GraphServiceClientProviderFactory providerFactory;
    private GraphServiceClientProvider mockProvider;

    @BeforeEach
    public void setup() {
        providerFactory = mock(GraphServiceClientProviderFactory.class);
        mockProvider = mock(GraphServiceClientProvider.class);
        when(providerFactory.create()).thenReturn(mockProvider);
    }

    // ========================================================================
    // Lazy Initialization Tests
    // ========================================================================

    @Test
    @DisplayName("getProvider: Should create provider via factory on first call")
    public void testGetProvider_FirstCallCreatesProvider() {
        AccountImpl account = new AccountImpl(providerFactory);

        GraphServiceClientProvider result = account.getProvider();

        assertNotNull(result);
        assertSame(mockProvider, result);
        verify(providerFactory, times(1)).create();
    }

    @Test
    @DisplayName("getProvider: Should return cached provider on subsequent calls (factory called only once)")
    public void testGetProvider_SubsequentCallsReturnCached() {
        AccountImpl account = new AccountImpl(providerFactory);

        GraphServiceClientProvider result1 = account.getProvider();
        GraphServiceClientProvider result2 = account.getProvider();
        GraphServiceClientProvider result3 = account.getProvider();

        assertSame(result1, result2);
        assertSame(result2, result3);
        verify(providerFactory, times(1)).create();
    }

    @Test
    @DisplayName("getProvider: Factory should not be called when provider is set via direct constructor")
    public void testGetProvider_DirectProviderInjection() {
        AccountImpl account = new AccountImpl(mockProvider);

        GraphServiceClientProvider result = account.getProvider();

        assertSame(mockProvider, result);
    }

    @Test
    @DisplayName("getProvider: Should not re-create provider even if factory returns different instance")
    public void testGetProvider_LazyInitOnlyOnce() {
        GraphServiceClientProvider firstProvider = mock(GraphServiceClientProvider.class);
        GraphServiceClientProvider secondProvider = mock(GraphServiceClientProvider.class);
        when(providerFactory.create()).thenReturn(firstProvider, secondProvider);

        AccountImpl account = new AccountImpl(providerFactory);

        // First call triggers factory — gets firstProvider
        GraphServiceClientProvider first = account.getProvider();
        assertSame(firstProvider, first);

        // Second call should return cached firstProvider, NOT secondProvider
        GraphServiceClientProvider second = account.getProvider();
        assertSame(firstProvider, second);
        assertNotSame(secondProvider, second);

        verify(providerFactory, times(1)).create();
    }

    // ========================================================================
    // Multiple Operations Tests
    // ========================================================================

    @Test
    @DisplayName("getProvider: Should use same cached provider across multiple account operations")
    public void testGetProvider_ConsistentAcrossOperations() {
        AccountImpl account = new AccountImpl(providerFactory);

        // Call getProvider() multiple times (simulating multiple account operations)
        GraphServiceClientProvider p1 = account.getProvider();
        GraphServiceClientProvider p2 = account.getProvider();
        GraphServiceClientProvider p3 = account.getProvider();
        GraphServiceClientProvider p4 = account.getProvider();
        GraphServiceClientProvider p5 = account.getProvider();

        // All should be the exact same instance
        assertSame(p1, p2);
        assertSame(p2, p3);
        assertSame(p3, p4);
        assertSame(p4, p5);

        // Factory should only be called once
        verify(providerFactory, times(1)).create();
    }
}
