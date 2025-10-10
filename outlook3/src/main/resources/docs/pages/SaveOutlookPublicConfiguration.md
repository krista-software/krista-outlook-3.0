# Save Outlook Public Configuration

## Overview

Configures the Outlook Extension for Public Authentication mode. This catalog request sets up simplified authentication
using Microsoft's public OAuth endpoints, perfect for development, testing, and environments that don't require custom
Microsoft Entra ID applications.

## Request Details

- **Area**: Messaging
- **Type**: CHANGE_SYSTEM
- **Retry Support**: Yes - Failed requests can be retried automatically

## Input Parameters

| Parameter Name   | Type    | Required | Description                                    | Example             |
|------------------|---------|----------|------------------------------------------------|---------------------|
| Email            | Text    | Yes      | Administrator email address for authentication | "admin@company.com" |
| Allow Alert Mail | Boolean | No       | Enable real-time email notifications           | true                |

### Parameter Details

#### Email

- **Format**: Valid email address
- **Purpose**: Administrator email account for authentication
- **Validation**: Must be valid email format
- **Usage**: This account will be used for all email operations
- **Requirements**: Account must have necessary mailbox permissions

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

**Example Output**: "Public configuration saved successfully"

## Validation Rules

| Validation                 | Error Message                                          | Resolution                                                     |
|----------------------------|--------------------------------------------------------|----------------------------------------------------------------|
| Email is empty             | "Email address is required"                            | Provide a valid email address                                  |
| Invalid email format       | "Invalid email address format"                         | Use proper email format (user@domain.com)                      |
| Email domain not supported | "Email domain not supported for public authentication" | Use supported email domain or switch to private authentication |

## Usage Examples

### Example 1: Basic Public Configuration

**Scenario**: Set up public authentication for testing environment

**Input**:

```
Email: "test.admin@company.com"
Allow Alert Mail: false
```

**Output**:

```
Configuration Status: "Public configuration saved successfully"
```

### Example 2: Public Configuration with Alerts

**Scenario**: Enable public authentication with real-time notifications

**Input**:

```
Email: "admin@company.com"
Allow Alert Mail: true
```

**Output**:

```
Configuration Status: "Public configuration saved successfully"
```

### Example 3: Development Environment Setup

**Scenario**: Quick setup for development environment

**Input**:

```
Email: "developer@company.com"
```

**Output**:

```
Configuration Status: "Public configuration saved successfully"
```

## Business Rules

1. **Authentication Mode**: Sets extension to use Public Authentication
2. **Microsoft Endpoints**: Uses Microsoft's public OAuth endpoints
3. **No Microsoft Entra ID App**: Does not require custom Microsoft Entra ID application
4. **Account Validation**: Email account must be accessible for authentication
5. **Configuration Override**: Replaces any existing private authentication configuration
6. **Immediate Effect**: Configuration is applied immediately

## Limitations

1. **Authentication Control**: Limited control over authentication policies
2. **Audit Capabilities**: Basic audit trail compared to private authentication
3. **Customization**: Limited customization options for authentication flow
4. **Enterprise Features**: Some enterprise features may not be available
5. **Shared Infrastructure**: Uses Microsoft's shared authentication infrastructure
6. **Rate Limits**: Subject to Microsoft's public endpoint rate limits

## Best Practices

### 1. Email Account Selection

- Use dedicated service account for extension operations
- Ensure account has necessary mailbox permissions
- Use account with stable credentials and access
- Document account usage for team reference

### 2. Configuration Management

- Test configuration after setup
- Document configuration settings for team reference
- Use consistent email accounts across environments
- Monitor configuration status regularly

### 3. Security Considerations

- Use accounts with appropriate permission levels
- Monitor account usage and access patterns
- Implement proper account management practices
- Consider switching to private authentication for production

### 4. Environment Planning

- Use public authentication for development and testing
- Plan migration to private authentication for production
- Document authentication mode decisions
- Test configuration changes in non-production environments

## Common Use Cases

### 1. Development Environment Setup

```
Scenario: Quick setup for development and testing
Action: Configure public authentication with developer account
Result: Rapid development environment setup without Microsoft Entra ID complexity
```

### 2. Proof of Concept

```
Scenario: Demonstrate extension capabilities quickly
Action: Use public authentication for fast setup
Result: Quick demonstration without enterprise authentication setup
```

### 3. Testing Environment

```
Scenario: Set up testing environment for extension validation
Action: Configure public authentication with test account
Result: Isolated testing environment with simplified authentication
```

### 4. Training and Learning

```
Scenario: Training environment for learning extension capabilities
Action: Use public authentication for simplified learning experience
Result: Focus on extension features without authentication complexity
```

## Configuration Workflow

### Step 1: Prepare Email Account

1. Identify appropriate email account for extension use
2. Verify account has necessary mailbox permissions
3. Ensure account credentials are accessible
4. Document account usage and purpose

### Step 2: Save Configuration

1. Call Save Outlook Public Configuration with email and alert settings
2. Verify configuration is saved successfully
3. Document configuration settings

### Step 3: Test Configuration

1. Use Test Connection catalog request to verify setup
2. Test basic email operations
3. Verify alert functionality if enabled
4. Document test results

### Step 4: Begin Using Extension

1. Start using extension catalog requests
2. Monitor performance and functionality
3. Plan for production authentication if needed

## Related Catalog Requests

- [Save Outlook Private Configuration](pages/SaveOutlookPrivateConfiguration.md) - Configure private authentication
- [Test Connection](pages/TestConnection.md) - Verify configuration
- [Health Check](pages/HealthCheck.md) - Monitor configuration health
- [Extension Configuration](pages/ExtensionConfiguration.md) - Complete setup guide

## Technical Implementation

### Helper Class

- **Class**: ConfigurationServiceImpl
- **Package**: app.krista.extensions.essentials.collaboration.outlook3.impl
- **Method**: savePublicConfiguration(String email, Boolean allowAlertMail)
- **Service**: Extension configuration management

### Telemetry Metrics

- **PUBLIC_CONFIG_SAVE_SUCCESS**: Successful public configuration saves
- **PUBLIC_CONFIG_SAVE_FAILURE**: Failed public configuration saves
- **PUBLIC_AUTH_MODE_ENABLED**: Count of public authentication mode activations
- **ALERT_MAIL_ENABLED**: Count of alert mail enablements

## Troubleshooting

### Configuration Save Failed

**Cause**: Invalid parameters or system issues
**Solution**:

1. Verify email address format is correct
2. Check system connectivity and resources
3. Retry operation after brief delay
4. Verify extension permissions and access

### Email Account Issues

**Cause**: Account access or permission problems
**Solution**:

1. Verify email account is accessible
2. Check account has necessary mailbox permissions
3. Test account authentication manually
4. Ensure account is not disabled or locked

### Alert Mail Configuration Issues

**Cause**: Webhook or notification setup problems
**Solution**:

1. Verify system supports webhook notifications
2. Check network connectivity for webhook endpoints
3. Test with alert mail disabled first
4. Review system logs for webhook errors

### Authentication Problems After Configuration

**Cause**: Configuration or account issues
**Solution**:

1. Use Test Connection to verify setup
2. Check if account requires additional authentication steps
3. Verify Microsoft public endpoints are accessible
4. Review authentication flow for errors

## See Also

- [Extension Configuration](pages/ExtensionConfiguration.md) - Complete setup guide
- [Authentication](pages/Authentication.md) - Authentication flows and troubleshooting
- [Save Outlook Private Configuration](pages/SaveOutlookPrivateConfiguration.md) - Private authentication setup
- [Test Connection](pages/TestConnection.md) - Verify configuration
