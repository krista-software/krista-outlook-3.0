# Health Check

## Overview

Performs comprehensive health validation of the Outlook3 Extension including configuration status, authentication health, API connectivity, and system readiness. This catalog request provides detailed system diagnostics for monitoring and troubleshooting purposes.

## Request Details

- **Area**: Messaging
- **Type**: QUERY_SYSTEM
- **Retry Support**: Yes - Failed requests can be retried automatically

## Input Parameters

This catalog request requires no input parameters.

## Output Parameters

| Parameter Name | Type | Description |
|----------------|------|-------------|
| Health Status | Entity(Health Details) | Comprehensive health information about the extension |

### Health Details Entity Structure

The health status entity contains the following diagnostic information:

| Field Name | Type | Description |
|------------|------|-------------|
| Overall Status | Text | Overall health status (Healthy, Warning, Critical) |
| Configuration Status | Text | Configuration validation status |
| Authentication Status | Text | Authentication health status |
| API Connectivity | Text | Microsoft Graph API connectivity status |
| Last Check Time | Date | Timestamp of the health check |
| Detailed Results | List<Text> | Detailed diagnostic messages |
| Recommendations | List<Text> | Recommended actions for issues found |

## Health Check Components

### 1. Configuration Validation
- **Authentication Mode**: Verifies public or private authentication configuration
- **Required Parameters**: Checks all necessary configuration parameters are present
- **Parameter Validity**: Validates format and content of configuration values
- **Credential Status**: Verifies client secrets haven't expired (Private Auth)

### 2. Authentication Health
- **Token Status**: Checks access token validity and expiration
- **Token Refresh**: Verifies refresh token functionality
- **Permission Validation**: Confirms required OAuth scopes are granted
- **Account Access**: Validates user account accessibility

### 3. API Connectivity
- **Endpoint Accessibility**: Tests connectivity to Microsoft Graph API endpoints
- **Response Validation**: Verifies API responses are properly formatted
- **Rate Limit Status**: Checks current rate limiting status
- **Service Availability**: Confirms Microsoft Graph service availability

### 4. System Readiness
- **Extension Status**: Verifies extension is properly loaded and configured
- **Resource Availability**: Checks system resources and dependencies
- **Network Connectivity**: Validates network connectivity requirements
- **Performance Metrics**: Basic performance and response time checks

## Health Status Levels

### Healthy
- All components functioning normally
- No issues detected
- System ready for production use
- All tests passed successfully

### Warning
- Minor issues detected that don't prevent operation
- Some non-critical components may have issues
- System functional but may need attention
- Recommended actions available

### Critical
- Major issues preventing normal operation
- Authentication or connectivity failures
- System not ready for production use
- Immediate action required

## Usage Examples

### Example 1: Healthy System
**Scenario**: All systems functioning normally

**Input**: (No parameters required)

**Output**:
```json
{
  "Health Status": {
    "Overall Status": "Healthy",
    "Configuration Status": "Valid",
    "Authentication Status": "Active",
    "API Connectivity": "Connected",
    "Last Check Time": 1640995200000,
    "Detailed Results": [
      "Configuration: All parameters valid",
      "Authentication: Token active, expires in 45 minutes",
      "API: Microsoft Graph accessible, response time 150ms",
      "System: Extension loaded and ready"
    ],
    "Recommendations": []
  }
}
```

### Example 2: Warning Status
**Scenario**: Minor issues detected

**Input**: (No parameters required)

**Output**:
```json
{
  "Health Status": {
    "Overall Status": "Warning",
    "Configuration Status": "Valid",
    "Authentication Status": "Token Expiring Soon",
    "API Connectivity": "Connected",
    "Last Check Time": 1640995200000,
    "Detailed Results": [
      "Configuration: All parameters valid",
      "Authentication: Token expires in 5 minutes",
      "API: Microsoft Graph accessible, response time 300ms",
      "System: Extension loaded and ready"
    ],
    "Recommendations": [
      "Token will expire soon - automatic refresh will occur",
      "API response time elevated - monitor performance"
    ]
  }
}
```

### Example 3: Critical Status
**Scenario**: Major issues preventing operation

**Input**: (No parameters required)

**Output**:
```json
{
  "Health Status": {
    "Overall Status": "Critical",
    "Configuration Status": "Invalid",
    "Authentication Status": "Failed",
    "API Connectivity": "Disconnected",
    "Last Check Time": 1640995200000,
    "Detailed Results": [
      "Configuration: Client secret expired",
      "Authentication: Unable to acquire token",
      "API: Microsoft Graph unreachable",
      "System: Extension not ready"
    ],
    "Recommendations": [
      "Update client secret in Microsoft Entra ID application",
      "Re-authenticate with valid credentials",
      "Check network connectivity to graph.microsoft.com",
      "Review extension configuration"
    ]
  }
}
```

## Diagnostic Categories

### Configuration Diagnostics
- **Parameter Validation**: All required parameters present and valid
- **Credential Status**: Client secrets and certificates not expired
- **Authentication Mode**: Correct mode selected and configured
- **Redirect URI**: Proper redirect URI configuration

### Authentication Diagnostics
- **Token Acquisition**: Ability to acquire new access tokens
- **Token Refresh**: Refresh token functionality working
- **Scope Validation**: Required permissions granted and functional
- **Account Status**: User account accessible and not disabled

### Connectivity Diagnostics
- **Network Access**: Internet connectivity available
- **DNS Resolution**: Microsoft Graph endpoints resolvable
- **SSL/TLS**: Secure connections working properly
- **Firewall**: No blocking of required endpoints

### Performance Diagnostics
- **Response Times**: API response times within acceptable ranges
- **Rate Limiting**: Current rate limit status and usage
- **Resource Usage**: System resource utilization
- **Error Rates**: Recent error rates and patterns

## Best Practices

### 1. Regular Monitoring
- Run health checks on a regular schedule
- Set up automated monitoring and alerting
- Track health trends over time
- Monitor before and after configuration changes

### 2. Proactive Maintenance
- Address warnings before they become critical
- Monitor token expiration times
- Keep client secrets updated
- Review and update configurations regularly

### 3. Troubleshooting Integration
- Use health check results to guide troubleshooting
- Combine with [Test Connection](TestConnection.md) for comprehensive diagnostics
- Document common issues and resolutions
- Share diagnostic information with support teams

### 4. Performance Monitoring
- Track API response times and performance metrics
- Monitor rate limiting and usage patterns
- Identify performance degradation early
- Optimize based on health check insights

## Common Use Cases

### 1. System Monitoring
```
Scenario: Regular monitoring of extension health
Action: Schedule periodic health checks
Result: Proactive identification of issues before they impact users
```

### 2. Troubleshooting Support
```
Scenario: Investigate reported issues with email operations
Action: Run health check to identify root causes
Result: Specific diagnostic information to guide resolution
```

### 3. Deployment Validation
```
Scenario: Verify system health after configuration changes
Action: Run health check after updates or maintenance
Result: Confirmation that changes didn't introduce issues
```

### 4. Performance Monitoring
```
Scenario: Monitor system performance and identify trends
Action: Regular health checks with performance tracking
Result: Early identification of performance degradation
```

## Related Catalog Requests

- [Test Connection](TestConnection.md) - Focused connection testing
- [Save Outlook Public Configuration](SaveOutlookPublicConfiguration.md) - Configure public authentication
- [Save Outlook Private Configuration](SaveOutlookPrivateConfiguration.md) - Configure private authentication
- [Extension Configuration](ExtensionConfiguration.md) - Setup and configuration guide

## Technical Implementation

### Helper Class
- **Class**: HealthCheckServiceImpl
- **Package**: app.krista.extensions.essentials.collaboration.outlook3.impl
- **Method**: performHealthCheck()
- **Validation**: Comprehensive system health validation
- **Service**: Multi-component health assessment

### Telemetry Metrics
- **HEALTH_CHECK_SUCCESS**: Successful health check operations
- **HEALTH_CHECK_FAILURE**: Failed health check operations
- **HEALTH_STATUS_HEALTHY**: Count of healthy status results
- **HEALTH_STATUS_WARNING**: Count of warning status results
- **HEALTH_STATUS_CRITICAL**: Count of critical status results

## Troubleshooting

### Critical Health Status
**Symptoms**: Health check returns Critical status
**Steps**:
1. Review detailed results for specific issues
2. Follow recommendations provided in health check
3. Check configuration parameters and credentials
4. Verify network connectivity and authentication
5. Use [Test Connection](TestConnection.md) for additional diagnostics

### Authentication Health Issues
**Symptoms**: Authentication status shows problems
**Steps**:
1. Check if client secret has expired (Private Auth)
2. Verify authentication token validity
3. Confirm required permissions are granted
4. Re-authenticate if necessary
5. Review Microsoft Entra ID application configuration

### API Connectivity Problems
**Symptoms**: API connectivity shows disconnected or slow
**Steps**:
1. Check internet connectivity
2. Verify Microsoft Graph service status
3. Test DNS resolution for graph.microsoft.com
4. Check firewall and proxy settings
5. Monitor for rate limiting issues

### Performance Warnings
**Symptoms**: Health check shows performance warnings
**Steps**:
1. Monitor API response times over time
2. Check for rate limiting or throttling
3. Review system resource usage
4. Optimize query patterns and frequency
5. Consider caching strategies

## See Also

- [Extension Configuration](ExtensionConfiguration.md) - Setup and configuration guide
- [Authentication](Authentication.md) - Authentication troubleshooting
- [Test Connection](TestConnection.md) - Connection testing
- [Microsoft Graph Service Health](https://status.graph.microsoft.com/) - Check service status
