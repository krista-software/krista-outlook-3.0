# Test Connection

## Overview

Validates the connection to Microsoft Outlook using stored or provided configuration parameters. This catalog request
performs comprehensive connectivity tests including OAuth token acquisition, mailbox connectivity, and scope validation
to ensure the integration is working properly.

## Request Details

- **Area**: Messaging
- **Type**: QUERY_SYSTEM
- **Retry Support**: Yes - Failed requests can be retried automatically

## Input Parameters

This catalog request requires no input parameters. It uses the stored configuration from the extension setup.

## Output Parameters

| Parameter Name           | Type    | Description                                              |
|--------------------------|---------|----------------------------------------------------------|
| Is Connection Successful | Boolean | Returns true if all connection tests passed successfully |

**Example Output**: `true`

## Validation Tests Performed

### 1. Configuration Validation

- **Authentication Mode**: Verifies public or private authentication configuration
- **Required Parameters**: Checks all necessary configuration parameters are present
- **Parameter Format**: Validates format of Client ID, Tenant ID, and other credentials

### 2. OAuth Token Acquisition

- **Token Request**: Attempts to acquire or refresh OAuth access token
- **Token Validation**: Verifies token format and expiration
- **Scope Verification**: Confirms required scopes are granted

### 3. Microsoft Graph API Connectivity

- **API Endpoint**: Tests connectivity to Microsoft Graph API endpoints
- **Authentication**: Verifies API authentication using acquired token
- **Response Validation**: Confirms API responses are properly formatted

### 4. Mailbox Access Validation

- **Mailbox Connectivity**: Tests access to user's mailbox
- **Permission Verification**: Confirms Mail.ReadWrite and Mail.Send permissions
- **Basic Operations**: Validates ability to perform basic email operations

## Test Results

### Success Conditions

All of the following conditions must be met for a successful connection test:

**Configuration Valid**: All required configuration parameters are present and correctly formatted
**Authentication Successful**: OAuth token successfully acquired or refreshed
**API Connectivity**: Microsoft Graph API endpoints are accessible
**Mailbox Access**: User's mailbox is accessible with required permissions
**Scope Validation**: All required OAuth scopes are granted and functional

### Failure Conditions

The test fails if any of the following conditions occur:

**Configuration Missing**: Required configuration parameters are missing or invalid
**Authentication Failed**: Unable to acquire or refresh OAuth token
**API Unreachable**: Microsoft Graph API endpoints are not accessible
**Permission Denied**: Insufficient permissions to access mailbox or perform operations
**Scope Issues**: Required OAuth scopes are not granted or functional

## Error Handling

### Configuration Errors

**Cause**: Missing or invalid configuration parameters
**Common Issues**:

- Missing Client ID, Client Secret, or Tenant ID (Private Auth)
- Invalid email address format
- Incorrect authentication mode selection

**Resolution**: Review and correct configuration parameters in extension setup

### Authentication Errors

**Cause**: OAuth authentication failures
**Common Issues**:

- Expired or invalid client secret
- Incorrect tenant ID
- User account issues

**Resolution**: Re-authenticate or update credentials as needed

### Permission Errors

**Cause**: Insufficient permissions or scope issues
**Common Issues**:

- Missing Mail.ReadWrite or Mail.Send permissions
- Admin consent not granted
- User lacks mailbox access

**Resolution**: Review and grant necessary permissions in Microsoft Entra ID

### Network Errors

**Cause**: Connectivity issues
**Common Issues**:

- Network connectivity problems
- Firewall blocking Microsoft Graph endpoints
- DNS resolution issues

**Resolution**: Check network connectivity and firewall settings

## Usage Examples

### Example 1: Successful Connection Test

**Scenario**: All configuration and permissions are correct

**Input**: (No parameters required)

**Output**:

```json
{
  "Is Connection Successful": true
}
```

**Interpretation**: Connection is fully functional and ready for email operations

### Example 2: Failed Connection Test

**Scenario**: Configuration or permission issues exist

**Input**: (No parameters required)

**Output**:

```json
{
  "Is Connection Successful": false
}
```

**Interpretation**: Issues exist that prevent proper email operations

## Diagnostic Information

When connection tests fail, the extension logs detailed diagnostic information including:

### Authentication Diagnostics

- OAuth token acquisition attempts
- Token validation results
- Scope verification outcomes
- Authentication error details

### API Connectivity Diagnostics

- Microsoft Graph API endpoint accessibility
- HTTP response codes and messages
- Network connectivity test results
- SSL/TLS certificate validation

### Permission Diagnostics

- Granted OAuth scopes
- Mailbox access test results
- Permission validation outcomes
- Admin consent status

## Best Practices

### 1. Regular Testing

- Run connection tests after configuration changes
- Perform periodic tests to ensure ongoing connectivity
- Test after credential updates or renewals
- Validate connection before deploying workflows

### 2. Troubleshooting Workflow

1. **Run Test Connection**: Start with this catalog request
2. **Review Results**: Check if connection is successful
3. **Analyze Logs**: Review diagnostic information for failures
4. **Fix Issues**: Address identified configuration or permission problems
5. **Retest**: Run Test Connection again to verify fixes

### 3. Monitoring Integration

- Include connection tests in monitoring workflows
- Set up alerts for connection failures
- Track connection success rates over time
- Monitor for authentication token expiration

### 4. Documentation

- Document successful test results for reference
- Keep records of configuration changes
- Maintain troubleshooting logs
- Share diagnostic information with support teams

## Common Use Cases

### 1. Initial Setup Validation

```
Scenario: Verify extension configuration after initial setup
Action: Run Test Connection to validate all settings
Result: Confirmation that extension is ready for use
```

### 2. Troubleshooting Connection Issues

```
Scenario: Email operations are failing unexpectedly
Action: Run Test Connection to identify root cause
Result: Specific diagnostic information to guide troubleshooting
```

### 3. Periodic Health Checks

```
Scenario: Regular monitoring of extension health
Action: Schedule periodic Test Connection requests
Result: Proactive identification of connectivity issues
```

### 4. Post-Maintenance Validation

```
Scenario: Verify connectivity after system maintenance
Action: Run Test Connection after configuration changes
Result: Confirmation that changes didn't break connectivity
```

## Related Catalog Requests

- [Extension Configuration](pages/ExtensionConfiguration.md) - Configure extension settings
- [Authentication](pages/Authentication.md) - Authentication setup and troubleshooting
- [Health Check](pages/HealthCheck.md) - System health validation
- [Fetch Inbox](pages/FetchInbox.md) - Test basic email operations

## Technical Implementation

### Helper Class

- **Class**: TestConnectionServiceImpl
- **Package**: app.krista.extensions.essentials.collaboration.outlook3.impl
- **Method**: testConnection()
- **Validation**: Comprehensive connectivity and permission testing
- **Service**: Microsoft Graph API connectivity validation

### Telemetry Metrics

- **TEST_CONNECTION_SUCCESS**: Successful connection tests
- **TEST_CONNECTION_FAILURE**: Failed connection tests
- **CONNECTION_TEST_DURATION**: Time taken for connection tests
- **AUTH_TOKEN_VALIDATION**: Token validation results

## Troubleshooting Guide

### Connection Test Failed - Authentication Issues

**Symptoms**: Test Connection returns false with authentication errors
**Steps**:

1. Verify authentication mode (Public vs Private)
2. Check configuration parameters are correct
3. Ensure client secret hasn't expired (Private Auth)
4. Re-authenticate if necessary
5. Verify tenant ID is correct (Private Auth)

### Connection Test Failed - Permission Issues

**Symptoms**: Authentication succeeds but mailbox access fails
**Steps**:

1. Verify Mail.ReadWrite permission is granted
2. Confirm Mail.Send permission is granted
3. Check if admin consent is required and granted
4. Ensure user account has mailbox access
5. Review Microsoft Entra ID application permissions

### Connection Test Failed - Network Issues

**Symptoms**: Cannot reach Microsoft Graph API endpoints
**Steps**:

1. Check internet connectivity
2. Verify firewall allows Microsoft Graph endpoints
3. Test DNS resolution for graph.microsoft.com
4. Check proxy settings if applicable
5. Verify SSL/TLS certificate validation

### Intermittent Connection Failures

**Symptoms**: Connection tests sometimes pass, sometimes fail
**Steps**:

1. Check for token expiration issues
2. Monitor network connectivity stability
3. Review Microsoft Graph service status
4. Check for rate limiting issues
5. Implement retry logic with exponential backoff

## See Also

- [Extension Configuration](pages/ExtensionConfiguration.md) - Setup and configuration guide
- [Authentication](pages/Authentication.md) - Authentication troubleshooting
- [Health Check](pages/HealthCheck.md) - System health monitoring
- [Microsoft Graph Service Status](https://status.graph.microsoft.com/) - Check service availability
