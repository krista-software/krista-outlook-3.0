# Enable Folder Monitoring

## Overview

Enable enhanced folder monitoring subscription for email notifications. This catalog request creates a Microsoft Graph subscription that monitors all folders and triggers on both created and updated events. When emails arrive in or are moved into monitored folders, Krista will receive notifications and can trigger workflows.

## Request Details

- **Area**: Setup
- **Type**: CHANGE_SYSTEM
- **Retry Support**: Yes - Automatic retry mechanism with exponential backoff

## Input Parameters

This catalog request requires no input parameters.

## Output Parameters

| Parameter Name | Type    | Description                                                              |
|----------------|---------|--------------------------------------------------------------------------|
| Is Successful  | Boolean | Indicates whether the folder monitoring subscription was enabled successfully |

**Example Output**: 
```json
{
  "Is Successful": true
}
```

## Validation Rules

| Validation                    | Error Message                                    | Resolution                                           |
|-------------------------------|--------------------------------------------------|------------------------------------------------------|
| Authentication failure        | Unauthorized access                              | Verify authentication and permissions                |
| Subscription creation failed  | Failed to create subscription                    | Check permissions and retry                          |
| Invalid notification URL      | Invalid notification endpoint                    | Verify routing URL configuration                     |

## Error Handling

### Authentication Errors

**Cause**: Missing or invalid authentication
**Common Scenarios**:
- Authentication token expired
- Insufficient permissions
- User not authenticated
- Missing Mail.ReadWrite permission

**Resolution**: 
1. Verify authentication is complete
2. Check Mail.ReadWrite permission is granted
3. Re-authenticate if necessary
4. Test with [Test Connection](pages/TestConnection.md)

### Subscription Creation Errors

**Cause**: Microsoft Graph API subscription creation failure
**Common Scenarios**:
- Invalid notification URL
- Subscription quota exceeded
- Network connectivity issues
- API rate limiting

**Resolution**: 
1. Verify notification endpoint is accessible
2. Check existing subscriptions
3. Retry operation (automatic retry included)
4. Review Microsoft Graph service status

### System Errors (SYSTEM_ERROR)

**Cause**: System-level failures
**Common Scenarios**:
- Network connectivity issues
- Microsoft Graph service unavailable
- Configuration errors

**Resolution**: Retry the operation (automatic retry with exponential backoff)

## Usage Examples

### Example 1: Enable Monitoring for First Time

**Scenario**: Set up folder monitoring for email workflows

**Input**: (No parameters required)

**Output**:
```json
{
  "Is Successful": true
}
```

**Result**: Subscription created, monitoring all folders for created and updated events

### Example 2: Renew Existing Subscription

**Scenario**: Subscription exists but needs renewal

**Input**: (No parameters required)

**Output**:
```json
{
  "Is Successful": true
}
```

**Result**: Existing subscription renewed with extended expiration

### Example 3: Subscription Creation Failed

**Scenario**: Insufficient permissions or API error

**Input**: (No parameters required)

**Output**:
```json
{
  "Is Successful": false
}
```

**Result**: Subscription not created, check logs for error details

## Business Rules

1. **Subscription Scope**: Monitors ALL user messages (`/messages` resource)
2. **Change Types**: Subscribes to both `created` and `updated` events
3. **Subscription Duration**: Valid for 3 days (72 hours)
4. **Auto-Renewal**: Subscription should be renewed before expiration
5. **Existing Subscription Handling**: If subscription exists, it will be renewed
6. **Retry Mechanism**: Automatic retry with exponential backoff (up to 3 attempts)
7. **Notification Endpoint**: Uses `/rest/outlook/folderMonitoringNotification`
8. **Lifecycle Endpoint**: Uses `/rest/outlook/folderLifecycleNotification`

## Subscription Details

### Monitored Resource
- **Resource**: `/users/{email}/messages`
- **Scope**: All folders in the mailbox
- **Events**: Email created and email updated

### Change Types
- **created**: Triggered when new email arrives
- **updated**: Triggered when email is modified (moved, marked read, etc.)

### Notification URLs
- **Notification URL**: `{routingUrl}/rest/outlook/folderMonitoringNotification`
- **Lifecycle URL**: `{routingUrl}/rest/outlook/folderLifecycleNotification`

### Subscription Validity
- **Duration**: 3 days (72 hours)
- **Renewal Window**: Should renew within 25 hours before expiration
- **Maximum Validity**: Microsoft Graph limits subscription duration

## Limitations

1. **Permissions Required**: Requires Mail.ReadWrite permission
2. **Subscription Quota**: Microsoft Graph has subscription limits per user
3. **Expiration**: Subscriptions expire after 3 days and must be renewed
4. **Network Accessibility**: Notification endpoint must be publicly accessible
5. **API Rate Limits**: Subject to Microsoft Graph API rate limiting
6. **Single Subscription**: Only one folder monitoring subscription per user

## Best Practices

### 1. Subscription Management

- Enable subscription after configuring monitored folders
- Monitor subscription expiration
- Implement automatic renewal process
- Log subscription creation and renewal events

### 2. Configuration Sequence

1. Configure Outlook extension ([Save Outlook Private Configuration](pages/SaveOutlookPrivateConfiguration.md))
2. Set monitored folders ([Set Monitored Folders](pages/SetMonitoredFolders.md))
3. Enable folder monitoring (this request)
4. Test with email notifications

### 3. Monitoring and Maintenance

- Check subscription status regularly
- Renew before expiration
- Monitor notification delivery
- Review logs for errors

### 4. Error Handling

- Implement retry logic for transient failures
- Log all subscription operations
- Alert on subscription creation failures
- Monitor subscription health

## Common Use Cases

### 1. Initial Setup

```
Scenario: Setting up folder monitoring for the first time
Action: Enable folder monitoring subscription
Result: Subscription created, ready to receive notifications
```

### 2. Subscription Renewal

```
Scenario: Existing subscription approaching expiration
Action: Call Enable Folder Monitoring to renew
Result: Subscription extended for another 3 days
```

### 3. Re-enable After Failure

```
Scenario: Subscription was deleted or expired
Action: Re-enable folder monitoring
Result: New subscription created
```

### 4. Automated Maintenance

```
Scenario: Scheduled job to maintain subscription
Action: Periodically call Enable Folder Monitoring
Result: Subscription always active and current
```

## Related Catalog Requests

- [Set Monitored Folders](pages/SetMonitoredFolders.md) - Configure which folders to monitor
- [Get Monitored Folders](pages/GetMonitoredFolders.md) - View current monitoring configuration
- [List All Folders](pages/ListAllFolders.md) - Browse available folders
- [Receive notification of Email Change](pages/ReceiveNotificationOfEmailChange.md) - Event triggered by subscription

## Technical Implementation

### Helper Class

- **Class**: SetupArea
- **Package**: app.krista.extensions.essentials.collaboration.outlook3.catalog
- **Method**: enableFolderMonitoring()
- **Service**: FolderMonitoringSubscription.createOrUpdateSubscription()
- **API**: Microsoft Graph Subscriptions API

### Implementation Details

```java
boolean success = FolderMonitoringSubscription
    .createOrUpdateSubscription(baseRoutingUrl, providerFactory.create());
```

### Subscription Creation Process

1. **Prepare Parameters**: Set subscription resource, change types, and notification URLs
2. **Check Existing**: Query for existing subscriptions
3. **Create or Renew**: 
   - If no subscription exists: Create new subscription
   - If subscription exists: Renew with updated expiration
4. **Retry Logic**: Up to 3 attempts with exponential backoff
5. **Return Status**: Boolean indicating success or failure

### Retry Mechanism

- **Max Retries**: 3 attempts
- **Backoff Strategy**: Exponential backoff between retries
- **Retry Delay**: Increases with each attempt
- **Failure Handling**: Returns false after all retries exhausted

## Troubleshooting

### Subscription Creation Failed

**Cause**: Permission or API issues
**Solution**:
1. Verify Mail.ReadWrite permission granted
2. Check authentication token validity
3. Verify notification endpoint is accessible
4. Review Microsoft Graph service status
5. Check subscription quota not exceeded

### Notification Endpoint Not Accessible

**Cause**: Routing URL or network configuration
**Solution**:
1. Verify routing URL is correct
2. Ensure endpoint is publicly accessible
3. Check firewall and network settings
4. Test endpoint accessibility from external network

### Subscription Expires Quickly

**Cause**: Microsoft Graph subscription duration limits
**Solution**:
1. Implement automatic renewal process
2. Schedule renewal before expiration (within 25 hours)
3. Monitor subscription expiration dates
4. Set up alerts for expiration

### Multiple Subscriptions Created

**Cause**: Concurrent subscription creation calls
**Solution**:
1. Implement locking mechanism
2. Check for existing subscription before creating
3. Delete duplicate subscriptions
4. Use single subscription management process

## See Also

- [Extension Configuration](pages/ExtensionConfiguration.md) - Initial setup and configuration
- [Set Monitored Folders](pages/SetMonitoredFolders.md) - Configure monitored folders
- [Receive notification of Email Change](pages/ReceiveNotificationOfEmailChange.md) - Handle notifications
- [Test Connection](pages/TestConnection.md) - Verify connectivity and permissions

