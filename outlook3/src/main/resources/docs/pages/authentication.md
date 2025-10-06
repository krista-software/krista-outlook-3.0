# Authentication

## Overview

The Outlook3 Extension implements OAuth 2.0 authentication with Microsoft Entra ID Directory, supporting both Public and Private authentication modes. This comprehensive guide covers authentication flows, security considerations, token management, and troubleshooting for both deployment scenarios.

## Authentication Architecture

### OAuth 2.0 Implementation
The extension uses the **Authorization Code Grant** flow with PKCE (Proof Key for Code Exchange) for enhanced security:

1. **Authorization Request**: User is redirected to Microsoft's authorization server
2. **User Consent**: User authenticates and grants permissions
3. **Authorization Code**: Microsoft returns authorization code to callback URL
4. **Token Exchange**: Extension exchanges code for access and refresh tokens
5. **API Access**: Extension uses access token for Microsoft Graph API calls

### Supported Grant Types
- **Authorization Code Grant with PKCE**: Primary flow for both authentication modes
- **Refresh Token Grant**: Automatic token renewal for continuous access

## Public Authentication

Public Authentication provides simplified setup using Microsoft's public OAuth endpoints, perfect for development and testing environments.

### Authentication Flow

#### Step 1: Authorization Request
```
https://login.microsoftonline.com/common/oauth2/v2.0/authorize?
  client_id={microsoft_public_client_id}
  &response_type=code
  &redirect_uri={extension_callback_url}
  &scope=https://graph.microsoft.com/Mail.ReadWrite https://graph.microsoft.com/Mail.Send offline_access
  &state={security_state}
  &code_challenge={pkce_challenge}
  &code_challenge_method=S256
```

#### Step 2: User Authentication
- User is redirected to Microsoft login page
- User enters credentials and authenticates
- Microsoft validates user identity and permissions

#### Step 3: Consent and Authorization
- User reviews and grants requested permissions
- Microsoft generates authorization code
- User is redirected back to extension with authorization code

#### Step 4: Token Exchange
```
POST https://login.microsoftonline.com/common/oauth2/v2.0/token
Content-Type: application/x-www-form-urlencoded

client_id={microsoft_public_client_id}
&grant_type=authorization_code
&code={authorization_code}
&redirect_uri={extension_callback_url}
&code_verifier={pkce_verifier}
```

### Public Authentication Benefits
**Quick Setup**: No Microsoft Entra ID application required
**Simplified Management**: Microsoft handles client credentials
**Standard Security**: OAuth 2.0 with PKCE protection
**Perfect for Testing**: Ideal for development environments

### Public Authentication Limitations
**Limited Control**: Cannot customize permissions or policies
**Shared Infrastructure**: Uses Microsoft's public endpoints
**Basic Auditing**: Limited audit trail capabilities

## Private Authentication

Private Authentication uses your own Microsoft Entra ID application for enterprise-grade security and full administrative control.

### Prerequisites
- Microsoft Entra ID tenant with application registration permissions
- Registered Microsoft Entra ID application (see [Creating Outlook App](CreatingOutlookApp.md))
- Application credentials (Client ID, Client Secret, Tenant ID)

### Authentication Flow

#### Step 1: Authorization Request
```
https://login.microsoftonline.com/{tenant_id}/oauth2/v2.0/authorize?
  client_id={your_client_id}
  &response_type=code
  &redirect_uri={extension_callback_url}
  &scope=https://graph.microsoft.com/Mail.ReadWrite https://graph.microsoft.com/Mail.Send offline_access
  &state={security_state}
  &code_challenge={pkce_challenge}
  &code_challenge_method=S256
```

#### Step 2: User Authentication
- User authenticates against your Microsoft Entra ID tenant
- Conditional access policies are applied
- Multi-factor authentication enforced if configured

#### Step 3: Consent and Authorization
- User grants permissions to your Microsoft Entra ID application
- Admin consent may be required for certain permissions
- Authorization code is generated and returned

#### Step 4: Token Exchange
```
POST https://login.microsoftonline.com/{tenant_id}/oauth2/v2.0/token
Content-Type: application/x-www-form-urlencoded

client_id={your_client_id}
&client_secret={your_client_secret}
&grant_type=authorization_code
&code={authorization_code}
&redirect_uri={extension_callback_url}
&code_verifier={pkce_verifier}
```

### Private Authentication Benefits
**Enterprise Security**: Full control through your Microsoft Entra ID tenant
**Advanced Policies**: Conditional access and compliance policies
**Complete Auditing**: Full audit trails through Microsoft Entra ID logs
**Custom Permissions**: Fine-grained permission control
**Multi-Factor Authentication**: Enhanced security with MFA
**Compliance Ready**: Meets enterprise security requirements

## OAuth 2.0 Scopes

### Required Scopes
The extension requires the following Microsoft Graph API scopes:

| Scope | Permission Type | Description | Usage |
|-------|----------------|-------------|--------|
| `Mail.ReadWrite` | Delegated | Read and write access to user mailbox | All email operations |
| `Mail.Send` | Delegated | Send emails on behalf of user | Email sending operations |
| `offline_access` | Delegated | Maintain access when user is offline | Token refresh |

### Scope Details

#### Mail.ReadWrite
- **Purpose**: Comprehensive mailbox access
- **Capabilities**:
  - Read emails from all folders
  - Create, update, and delete emails
  - Manage email properties and metadata
  - Access attachments and email content
- **Security**: Provides full mailbox access - use with caution

#### Mail.Send
- **Purpose**: Email sending capabilities
- **Capabilities**:
  - Send new emails
  - Reply to existing emails
  - Forward emails
  - Send emails with attachments
- **Security**: Allows sending emails on behalf of user

#### offline_access
- **Purpose**: Long-term access without user interaction
- **Capabilities**:
  - Refresh access tokens automatically
  - Maintain access when user is not present
  - Enable background processing
- **Security**: Essential for automated workflows

### Admin Consent
Some organizations require administrator consent for these scopes:
- Contact your Microsoft Entra ID administrator if consent is required
- Admin can pre-consent for all users in the organization
- Individual user consent may be disabled by policy

## Token Management

### Access Tokens
- **Lifetime**: 1 hour (default)
- **Usage**: Authenticate API requests to Microsoft Graph
- **Storage**: Securely stored by extension
- **Refresh**: Automatically refreshed using refresh token

### Refresh Tokens
- **Lifetime**: 90 days (default, configurable)
- **Usage**: Obtain new access tokens
- **Storage**: Securely encrypted and stored
- **Rotation**: New refresh token issued with each refresh

### Token Refresh Process
```
POST https://login.microsoftonline.com/{tenant_id}/oauth2/v2.0/token
Content-Type: application/x-www-form-urlencoded

client_id={client_id}
&client_secret={client_secret}  // Only for Private Auth
&grant_type=refresh_token
&refresh_token={refresh_token}
&scope=https://graph.microsoft.com/Mail.ReadWrite https://graph.microsoft.com/Mail.Send offline_access
```

### Automatic Token Management
The extension automatically handles:
- Token expiration detection
- Automatic token refresh before expiration
- Retry logic for failed refresh attempts
- Secure token storage and encryption
- Token cleanup on disconnection

## Security Best Practices

### General Security
1. **Use HTTPS**: Always use HTTPS for all communications
2. **Secure Storage**: Tokens are encrypted at rest
3. **Token Rotation**: Refresh tokens are rotated regularly
4. **Minimal Scopes**: Only request necessary permissions
5. **Regular Auditing**: Monitor authentication logs regularly

### Public Authentication Security
1. **Environment Isolation**: Use only for development/testing
2. **Account Separation**: Use dedicated test accounts
3. **Regular Cleanup**: Remove test configurations regularly
4. **Monitor Usage**: Track authentication attempts

### Private Authentication Security
1. **Client Secret Management**:
   - Store client secrets securely
   - Rotate secrets regularly (every 6-12 months)
   - Never expose secrets in logs or code
   - Use Azure Key Vault for secret storage

2. **Microsoft Entra ID Configuration**:
   - Enable conditional access policies
   - Require multi-factor authentication
   - Configure session management policies
   - Enable audit logging

3. **Application Security**:
   - Regularly review application permissions
   - Monitor application usage through Microsoft Entra ID logs
   - Implement proper error handling
   - Use principle of least privilege

### Compliance Considerations
- **Data Residency**: Understand where tokens are stored
- **Audit Requirements**: Ensure proper logging is enabled
- **Retention Policies**: Configure appropriate token lifetimes
- **Access Reviews**: Regularly review application access

## Troubleshooting Authentication Issues

### Common Authentication Errors

#### Invalid Client Error
**Error**: "AADSTS70002: Error validating credentials"
**Causes**:
- Incorrect Client ID or Client Secret
- Client secret expired
- Wrong tenant ID

**Solutions**:
1. Verify all credentials are correct
2. Check if client secret has expired
3. Ensure tenant ID matches your Microsoft Entra ID tenant
4. Regenerate client secret if necessary

#### Insufficient Permissions
**Error**: "AADSTS65001: The user or administrator has not consented"
**Causes**:
- Required permissions not granted
- Admin consent required but not provided
- User lacks permission to consent

**Solutions**:
1. Ensure all required scopes are configured
2. Request admin consent if required
3. Check Microsoft Entra ID application permissions
4. Verify user has consent permissions

#### Redirect URI Mismatch
**Error**: "AADSTS50011: The reply URL specified in the request does not match"
**Causes**:
- Redirect URI not configured in Microsoft Entra ID
- Mismatch between configured and actual redirect URI
- HTTP vs HTTPS mismatch

**Solutions**:
1. Add correct redirect URI to Microsoft Entra ID application
2. Ensure exact match including protocol and path
3. Use HTTPS for production environments
4. Verify extension base URL is correct

#### Token Refresh Failed
**Error**: "AADSTS70008: The provided authorization grant is expired"
**Causes**:
- Refresh token expired
- User password changed
- Account disabled or deleted

**Solutions**:
1. Re-authenticate user to obtain new tokens
2. Check user account status
3. Verify account hasn't been disabled
4. Update password if changed

### Diagnostic Steps

#### Step 1: Verify Configuration
1. Check all authentication parameters are correct
2. Verify Microsoft Entra ID application configuration
3. Confirm redirect URI matches exactly
4. Test with [Test Connection](TestConnection.md) catalog request

#### Step 2: Check Permissions
1. Verify required scopes are granted
2. Check if admin consent is required
3. Confirm user has necessary permissions
4. Review Microsoft Entra ID application permissions

#### Step 3: Monitor Logs
1. Check extension logs for authentication errors
2. Review Microsoft Entra ID sign-in logs
3. Monitor Microsoft Graph API responses
4. Look for token refresh failures

#### Step 4: Test Authentication Flow
1. Clear browser cache and cookies
2. Try authentication in incognito/private mode
3. Test from different network/device
4. Verify with different user account

## See Also

- [Extension Configuration](ExtensionConfiguration.md) - Complete setup guide
- [Creating Outlook App](CreatingOutlookApp.md) - Microsoft Entra ID application setup
- [Test Connection](TestConnection.md) - Connection testing and validation
- [Security Best Practices](https://docs.microsoft.com/en-us/azure/active-directory/develop/security-best-practices-for-app-registration) - Microsoft's security guidelines
