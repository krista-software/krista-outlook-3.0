# Save Outlook Private Configuration

## Overview

Configures the Outlook Extension for Private Authentication mode using your own Azure AD application. This catalog
request sets up enterprise-grade authentication with full OAuth 2.0 implementation, providing complete control over
security policies and compliance requirements.

## Request Details

- **Area**: Messaging
- **Type**: CHANGE_SYSTEM
- **Retry Support**: Yes - Failed requests can be retried automatically

## Input Parameters

| Parameter Name   | Type    | Required | Description                              | Example                                |
|------------------|---------|----------|------------------------------------------|----------------------------------------|
| Email            | Text    | Yes      | Administrator email address              | "admin@company.com"                    |
| Client ID        | Text    | Yes      | Azure AD Application (client) ID         | "12345678-1234-1234-1234-123456789012" |
| Client Secret    | Text    | Yes      | Azure AD application client secret value | "abcdef123456..."                      |
| Tenant ID        | Text    | Yes      | Azure AD Directory (tenant) ID           | "87654321-4321-4321-4321-210987654321" |
| Allow Alert Mail | Boolean | No       | Enable real-time email notifications     | true                                   |

### Parameter Details

#### Email

- **Format**: Valid email address
- **Purpose**: Administrator email account for authentication
- **Validation**: Must be valid email format
- **Usage**: Should match the account used to register Azure AD application
- **Requirements**: Account must have necessary mailbox permissions

#### Client ID

- **Format**: GUID (UUID) format
- **Source**: Azure AD App Registration > Overview > Application (client) ID
- **Validation**: Must be valid GUID format
- **Purpose**: Identifies your Azure AD application
- **Security**: Not sensitive but should be documented securely

#### Client Secret

- **Format**: Base64-encoded string
- **Source**: Azure AD App Registration > Certificates & secrets > Client secret value
- **Validation**: Must be valid client secret format
- **Security**: Highly sensitive - store securely and rotate regularly
- **Expiration**: Monitor expiration date and renew before expiry

#### Tenant ID

- **Format**: GUID (UUID) format
- **Source**: Azure AD App Registration > Overview > Directory (tenant) ID
- **Validation**: Must be valid GUID format
- **Purpose**: Identifies your Azure AD tenant
- **Security**: Not sensitive but should be documented

#### Allow Alert Mail

- **Default**: false (if not specified)
- **Purpose**: Enable webhook-based email notifications
- **Values**:
    - `true` - Enable real-time email alerts
    - `false` - Disable real-time email alerts
- **Impact**: Affects availability of notification-based catalog requests

## Output Parameters

| Parameter Name       | Type | Description                  |
|----------------------|------|------------------------------|
| Configuration Status | Text | Success confirmation message |

**Example Output**: "Private configuration saved successfully"

## Validation Rules

| Validation             | Error Message                                 | Resolution                               |
|------------------------|-----------------------------------------------|------------------------------------------|
| Email is empty         | "Email address is required"                   | Provide a valid email address            |
| Client ID is empty     | "Client ID is required"                       | Provide Azure AD Application (client) ID |
| Client Secret is empty | "Client Secret is required"                   | Provide Azure AD client secret value     |
| Tenant ID is empty     | "Tenant ID is required"                       | Provide Azure AD Directory (tenant) ID   |
| Invalid GUID format    | "Invalid GUID format for Client ID/Tenant ID" | Use proper GUID format                   |
| Invalid email format   | "Invalid email address format"                | Use proper email format                  |

## Usage Examples

### Example 1: Complete Private Configuration

**Scenario**: Set up private authentication for production environment

**Input**:

```
Email: "admin@company.com"
Client ID: "12345678-1234-1234-1234-123456789012"
Client Secret: "abcdef123456789..."
Tenant ID: "87654321-4321-4321-4321-210987654321"
Allow Alert Mail: true
```

**Output**:

```
Configuration Status: "Private configuration saved successfully"
```

### Example 2: Private Configuration without Alerts

**Scenario**: Enterprise setup without real-time notifications

**Input**:

```
Email: "service.account@company.com"
Client ID: "12345678-1234-1234-1234-123456789012"
Client Secret: "abcdef123456789..."
Tenant ID: "87654321-4321-4321-4321-210987654321"
Allow Alert Mail: false
```

**Output**:

```
Configuration Status: "Private configuration saved successfully"
```

## Business Rules

1. **Authentication Mode**: Sets extension to use Private Authentication
2. **Azure AD Integration**: Uses your Azure AD tenant for authentication
3. **Enterprise Security**: Provides full OAuth 2.0 security implementation
4. **Custom Policies**: Supports conditional access and custom security policies
5. **Configuration Override**: Replaces any existing public authentication configuration
6. **Immediate Effect**: Configuration is applied immediately

## Limitations

1. **Azure AD Requirement**: Requires Azure AD tenant and application registration
2. **Setup Complexity**: More complex setup compared to public authentication
3. **Credential Management**: Requires secure management of client secrets
4. **Maintenance**: Requires ongoing maintenance of Azure AD application
5. **Permissions**: Requires appropriate Azure AD permissions for setup
6. **Cost**: May incur Azure AD licensing costs

## Best Practices

### 1. Credential Security

- Store client secrets securely (consider Azure Key Vault)
- Rotate client secrets regularly (every 6-12 months)
- Never expose secrets in logs or code
- Use separate credentials for different environments

### 2. Azure AD Application Management

- Use descriptive application names
- Document application purpose and usage
- Regularly review application permissions
- Monitor application usage through Azure AD logs

### 3. Configuration Management

- Test configuration in non-production environments first
- Document all configuration parameters
- Implement configuration backup and recovery procedures
- Monitor configuration health regularly

### 4. Security Monitoring

- Enable Azure AD audit logging
- Monitor authentication attempts and failures
- Implement alerting for suspicious activities
- Regular security reviews and assessments

## Common Use Cases

### 1. Production Environment Setup

```
Scenario: Configure extension for production use with enterprise security
Action: Set up private authentication with Azure AD application
Result: Enterprise-grade security with full audit trails and compliance
```

### 2. Multi-Environment Deployment

```
Scenario: Deploy extension across development, staging, and production
Action: Configure separate Azure AD applications for each environment
Result: Isolated environments with appropriate security controls
```

### 3. Compliance Requirements

```
Scenario: Meet enterprise compliance and security requirements
Action: Use private authentication with conditional access policies
Result: Full compliance with enterprise security standards
```

### 4. Custom Security Policies

```
Scenario: Implement custom authentication and access policies
Action: Configure private authentication with Azure AD policies
Result: Customized security implementation meeting specific requirements
```

## Configuration Workflow

### Step 1: Prepare Azure AD Application

1. Complete Azure AD application registration (see [Creating Outlook App](pages/CreatingOutlookApp.md))
2. Collect Application (client) ID, Client Secret, and Directory (tenant) ID
3. Configure redirect URIs and permissions
4. Grant admin consent for required permissions

### Step 2: Save Configuration

1. Call Save Outlook Private Configuration with all required parameters
2. Verify configuration is saved successfully
3. Document configuration settings securely

### Step 3: Test Configuration

1. Use Test Connection catalog request to verify setup
2. Complete OAuth authentication flow
3. Test basic email operations
4. Verify alert functionality if enabled

### Step 4: Production Deployment

1. Monitor authentication and operations
2. Implement ongoing maintenance procedures
3. Set up monitoring and alerting
4. Document operational procedures

## Related Catalog Requests

- [Save Outlook Public Configuration](pages/SaveOutlookPublicConfiguration.md) - Configure public authentication
- [Test Connection](pages/TestConnection.md) - Verify configuration
- [Health Check](pages/HealthCheck.md) - Monitor configuration health
- [Creating Outlook App](pages/CreatingOutlookApp.md) - Azure AD application setup

## Technical Implementation

### Helper Class

- **Class**: ConfigurationServiceImpl
- **Package**: app.krista.extensions.essentials.collaboration.outlook3.impl
- **Method**: savePrivateConfiguration(String email, String clientId, String clientSecret, String tenantId, Boolean
  allowAlertMail)
- **Service**: Extension configuration management with Azure AD integration

### Telemetry Metrics

- **PRIVATE_CONFIG_SAVE_SUCCESS**: Successful private configuration saves
- **PRIVATE_CONFIG_SAVE_FAILURE**: Failed private configuration saves
- **PRIVATE_AUTH_MODE_ENABLED**: Count of private authentication mode activations
- **AZURE_AD_INTEGRATION**: Count of Azure AD integrations configured

## Troubleshooting

### Configuration Save Failed

**Cause**: Invalid parameters or system issues
**Solution**:

1. Verify all GUID formats are correct
2. Check client secret is the value, not the ID
3. Ensure all required parameters are provided
4. Retry operation after brief delay

### Azure AD Credential Issues

**Cause**: Invalid or expired Azure AD credentials
**Solution**:

1. Verify Client ID matches Azure AD application
2. Check Client Secret is current and not expired
3. Confirm Tenant ID matches your Azure AD tenant
4. Regenerate client secret if expired

### Authentication Problems After Configuration

**Cause**: Azure AD application or permission issues
**Solution**:

1. Verify Azure AD application is properly configured
2. Check redirect URIs are correctly set
3. Ensure required permissions are granted
4. Confirm admin consent has been provided

### Permission Errors

**Cause**: Insufficient Azure AD permissions
**Solution**:

1. Verify Mail.ReadWrite and Mail.Send permissions are granted
2. Check if admin consent is required and provided
3. Confirm user account has necessary permissions
4. Review Azure AD application permissions configuration

## See Also

- [Extension Configuration](pages/ExtensionConfiguration.md) - Complete setup guide
- [Authentication](pages/Authentication.md) - Authentication flows and troubleshooting
- [Creating Outlook App](pages/CreatingOutlookApp.md) - Azure AD application setup
- [Test Connection](pages/TestConnection.md) - Verify configuration
