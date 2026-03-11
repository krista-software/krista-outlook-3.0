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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Real-world unit tests for AuthHelper utility class.
 * Tests cover OAuth state parameter parsing and auth context ID extraction.
 */
@DisplayName("AuthHelper Tests")
class AuthHelperTest {

    // ==================== Basic Auth Context ID Extraction ====================

    @Test
    @DisplayName("Should extract auth context ID from simple state parameter")
    void testExtractAuthContextIdSimple() {
        String state = "authContextId=abc123";
        String result = AuthHelper.getAuthContextId(state);

        assertEquals("authContextId=abc123", result);
    }

    @Test
    @DisplayName("Should extract auth context ID before hash symbol")
    void testExtractAuthContextIdBeforeHash() {
        String state = "authContextId=abc123#fragment";
        String result = AuthHelper.getAuthContextId(state);

        assertEquals("authContextId=abc123", result);
    }

    @Test
    @DisplayName("Should extract auth context ID with additional parameters")
    void testExtractAuthContextIdWithParams() {
        String state = "param1=value1&authContextId=xyz789&param2=value2";
        String result = AuthHelper.getAuthContextId(state);

        assertEquals("authContextId=xyz789&param2=value2", result);
    }

    @Test
    @DisplayName("Should extract auth context ID before hash with additional params")
    void testExtractAuthContextIdBeforeHashWithParams() {
        String state = "param1=value1&authContextId=xyz789&param2=value2#fragment";
        String result = AuthHelper.getAuthContextId(state);

        assertEquals("authContextId=xyz789&param2=value2", result);
    }

    // ==================== Edge Cases ====================

    @Test
    @DisplayName("Should return null when auth context ID not found")
    void testAuthContextIdNotFound() {
        String state = "param1=value1&param2=value2";
        String result = AuthHelper.getAuthContextId(state);

        assertNull(result);
    }

    @Test
    @DisplayName("Should handle auth context ID at the beginning")
    void testAuthContextIdAtBeginning() {
        String state = "authContextId=start123&other=params";
        String result = AuthHelper.getAuthContextId(state);

        assertEquals("authContextId=start123&other=params", result);
    }

    @Test
    @DisplayName("Should handle auth context ID at the end")
    void testAuthContextIdAtEnd() {
        String state = "param1=value1&param2=value2&authContextId=end456";
        String result = AuthHelper.getAuthContextId(state);

        assertEquals("authContextId=end456", result);
    }

    @Test
    @DisplayName("Should handle auth context ID with hash at end")
    void testAuthContextIdWithHashAtEnd() {
        String state = "authContextId=test789#";
        String result = AuthHelper.getAuthContextId(state);

        assertEquals("authContextId=test789", result);
    }

    // ==================== Real-World OAuth Scenarios ====================

    @Test
    @DisplayName("Should extract auth context ID from Microsoft OAuth callback")
    void testMicrosoftOAuthCallback() {
        // Real-world Microsoft OAuth state parameter format
        String state = "state=random123&authContextId=user-session-456&redirect=/dashboard#access_token";
        String result = AuthHelper.getAuthContextId(state);

        assertEquals("authContextId=user-session-456&redirect=/dashboard", result);
    }

    @Test
    @DisplayName("Should extract auth context ID from Outlook OAuth flow")
    void testOutlookOAuthFlow() {
        // Outlook OAuth state with auth context
        String state = "csrf=token123&authContextId=outlook-auth-789&scope=mail.read";
        String result = AuthHelper.getAuthContextId(state);

        assertEquals("authContextId=outlook-auth-789&scope=mail.read", result);
    }

    @Test
    @DisplayName("Should handle OAuth state with fragment identifier")
    void testOAuthStateWithFragment() {
        // OAuth callback with fragment (common in implicit flow)
        String state = "authContextId=session-abc#id_token=eyJ0eXAiOiJKV1QiLCJhbGc";
        String result = AuthHelper.getAuthContextId(state);

        assertEquals("authContextId=session-abc", result);
    }

    @Test
    @DisplayName("Should extract auth context ID from complex OAuth state")
    void testComplexOAuthState() {
        // Complex state with multiple parameters
        String state = "nonce=n123&state=s456&authContextId=ctx-789&return_url=/app/inbox#token_type=Bearer";
        String result = AuthHelper.getAuthContextId(state);

        assertEquals("authContextId=ctx-789&return_url=/app/inbox", result);
    }

    // ==================== UUID-based Auth Context IDs ====================

    @Test
    @DisplayName("Should extract UUID-based auth context ID")
    void testUuidBasedAuthContextId() {
        String state = "authContextId=550e8400-e29b-41d4-a716-446655440000";
        String result = AuthHelper.getAuthContextId(state);

        assertEquals("authContextId=550e8400-e29b-41d4-a716-446655440000", result);
    }

    @Test
    @DisplayName("Should extract UUID auth context ID with other params")
    void testUuidAuthContextIdWithParams() {
        String state = "csrf=token&authContextId=550e8400-e29b-41d4-a716-446655440000&redirect=/home#fragment";
        String result = AuthHelper.getAuthContextId(state);

        assertEquals("authContextId=550e8400-e29b-41d4-a716-446655440000&redirect=/home", result);
    }

    // ==================== Special Characters in State ====================

    @Test
    @DisplayName("Should handle URL-encoded state parameters")
    void testUrlEncodedState() {
        // URL-encoded state parameter
        String state = "return_url=%2Fapp%2Finbox&authContextId=ctx123&scope=mail.read%20mail.send";
        String result = AuthHelper.getAuthContextId(state);

        assertEquals("authContextId=ctx123&scope=mail.read%20mail.send", result);
    }

    @Test
    @DisplayName("Should handle state with equals signs in values")
    void testStateWithEqualsInValues() {
        String state = "authContextId=base64Value==&other=param";
        String result = AuthHelper.getAuthContextId(state);

        assertEquals("authContextId=base64Value==&other=param", result);
    }

    @Test
    @DisplayName("Should handle state with multiple hash symbols")
    void testStateWithMultipleHashes() {
        // Only first hash should be considered
        String state = "authContextId=ctx123#fragment1#fragment2";
        String result = AuthHelper.getAuthContextId(state);

        assertEquals("authContextId=ctx123", result);
    }

    // ==================== Empty and Null Cases ====================

    @Test
    @DisplayName("Should handle empty state parameter")
    void testEmptyState() {
        String state = "";
        String result = AuthHelper.getAuthContextId(state);

        assertNull(result);
    }

    @Test
    @DisplayName("Should handle state with only hash")
    void testStateWithOnlyHash() {
        String state = "#";
        String result = AuthHelper.getAuthContextId(state);

        assertNull(result);
    }

    @Test
    @DisplayName("Should handle state with only authContextId key")
    void testStateWithOnlyAuthContextIdKey() {
        String state = "authContextId=";
        String result = AuthHelper.getAuthContextId(state);

        assertEquals("authContextId=", result);
    }

    // ==================== Integration Scenarios ====================

    @Test
    @DisplayName("Should extract auth context from initial OAuth request")
    void testInitialOAuthRequest() {
        // Scenario: User initiates OAuth flow
        String state = "csrf_token=abc123&authContextId=user-session-456&original_url=/inbox";
        String result = AuthHelper.getAuthContextId(state);

        assertTrue(result.contains("authContextId=user-session-456"));
        assertTrue(result.contains("original_url=/inbox"));
    }

    @Test
    @DisplayName("Should extract auth context from OAuth callback")
    void testOAuthCallback() {
        // Scenario: OAuth provider redirects back with state
        String state = "csrf_token=abc123&authContextId=user-session-456#code=auth_code_xyz";
        String result = AuthHelper.getAuthContextId(state);

        assertTrue(result.contains("authContextId=user-session-456"));
        assertFalse(result.contains("#"));
        assertFalse(result.contains("code="));
    }

    @Test
    @DisplayName("Should handle multi-tenant OAuth scenario")
    void testMultiTenantOAuth() {
        // Scenario: Multi-tenant application with tenant-specific auth context
        String state = "tenant=company-a&authContextId=tenant-company-a-session-123&user=john@company-a.com";
        String result = AuthHelper.getAuthContextId(state);

        assertTrue(result.contains("authContextId=tenant-company-a-session-123"));
        assertTrue(result.contains("user=john@company-a.com"));
    }

    @Test
    @DisplayName("Should extract auth context from service account flow")
    void testServiceAccountFlow() {
        // Scenario: Service account authentication
        String state = "service=outlook-sync&authContextId=service-account-789&scope=mail.read";
        String result = AuthHelper.getAuthContextId(state);

        assertTrue(result.contains("authContextId=service-account-789"));
        assertTrue(result.contains("scope=mail.read"));
    }

    @Test
    @DisplayName("Should handle refresh token scenario")
    void testRefreshTokenScenario() {
        // Scenario: Refreshing access token with auth context
        String state = "refresh=true&authContextId=refresh-session-999&grant_type=refresh_token";
        String result = AuthHelper.getAuthContextId(state);

        assertTrue(result.contains("authContextId=refresh-session-999"));
        assertTrue(result.contains("grant_type=refresh_token"));
    }

    // ==================== Security Scenarios ====================

    @Test
    @DisplayName("Should handle state with CSRF token")
    void testStateWithCsrfToken() {
        String state = "csrf=random-token-123&authContextId=session-456&nonce=nonce-789";
        String result = AuthHelper.getAuthContextId(state);

        assertTrue(result.contains("authContextId=session-456"));
        assertTrue(result.contains("nonce=nonce-789"));
    }

    @Test
    @DisplayName("Should extract auth context with security parameters")
    void testAuthContextWithSecurityParams() {
        String state = "nonce=n123&state=s456&authContextId=secure-ctx-789&code_challenge=challenge#token";
        String result = AuthHelper.getAuthContextId(state);

        assertTrue(result.contains("authContextId=secure-ctx-789"));
        assertTrue(result.contains("code_challenge=challenge"));
        assertFalse(result.contains("#token"));
    }

    // ==================== Error Recovery Scenarios ====================

    @Test
    @DisplayName("Should handle malformed state gracefully")
    void testMalformedState() {
        String state = "malformed&&&authContextId=ctx123&&&";
        String result = AuthHelper.getAuthContextId(state);

        assertEquals("authContextId=ctx123&&&", result);
    }

    @Test
    @DisplayName("Should handle state with no value after authContextId")
    void testStateWithNoValueAfterAuthContextId() {
        String state = "param1=value1&authContextId";
        String result = AuthHelper.getAuthContextId(state);

        assertEquals("authContextId", result);
    }

    @Test
    @DisplayName("Should handle case-sensitive authContextId")
    void testCaseSensitiveAuthContextId() {
        // authContextId is case-sensitive, should not match AuthContextId
        String state = "AuthContextId=abc123";
        String result = AuthHelper.getAuthContextId(state);

        assertNull(result); // Should not find it because case doesn't match
    }

    @Test
    @DisplayName("Should find exact authContextId match")
    void testExactAuthContextIdMatch() {
        // Should match exact "authContextId" string
        String state = "myauthContextId=wrong&authContextId=correct&authContextIdExtra=also-wrong";
        String result = AuthHelper.getAuthContextId(state);

        assertTrue(result.contains("authContextId=correct"));
    }
}

