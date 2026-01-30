# Send Alert Using Notification Delta

## Overview

Sends real-time email alerts based on notification delta changes in the mailbox. This catalog request provides
webhook-based email notifications for immediate processing of new emails, changes, or specific events in the user's
mailbox.

## Request Details

- **Area**: Messaging
- **Type**: CHANGE_SYSTEM
- **Retry Support**: Yes - Failed requests can be retried automatically

## Input Parameters

| Parameter Name    | Type | Required | Description                           | Example                                    |
|-------------------|------|----------|---------------------------------------|--------------------------------------------|
| Notification Type | Text | Yes      | Type of notification to send          | "NEW_EMAIL"                                |
| Delta Token       | Text | No       | Delta token for incremental changes   | "delta-token-12345"                        |
| Alert Recipients  | Text | Yes      | Recipients for the alert notification | "admin@company.com, alerts@company.com"    |
| Alert Subject     | Text | No       | Subject line for alert email          | "New Email Alert: Urgent Message Received" |

### Parameter Details

#### Notification Type

- **Values**:
    - `NEW_EMAIL` - Alert for new emails received
    - `EMAIL_UPDATED` - Alert for email status changes
    - `EMAIL_DELETED` - Alert for deleted emails
    - `FOLDER_CHANGED` - Alert for folder structure changes
- **Purpose**: Specifies the type of mailbox change to alert on
- **Validation**: Must be one of the supported notification types

#### Delta Token

- **Format**: String token from previous delta operations
- **Purpose**: Track incremental changes since last notification
- **Optional**: If not provided, alerts on all current changes
- **Usage**: Enables efficient change tracking and prevents duplicate alerts

#### Alert Recipients

- **Format**: Comma-separated email addresses
- **Purpose**: Recipients who will receive the alert notifications
- **Validation**: All email addresses must be valid format
- **Limit**: Maximum 50 recipients per alert

#### Alert Subject

- **Default**: Auto-generated based on notification type
- **Purpose**: Custom subject line for alert emails
- **Format**: Plain text string
- **Length**: Maximum 255 characters

## Output Parameters

| Parameter Name  | Type | Description                            |
|-----------------|------|----------------------------------------|
| Alert Status    | Text | Status of the alert operation          |
| New Delta Token | Text | Updated delta token for next operation |

**Example Output**:

```
Alert Status: "Alert sent successfully"
New Delta Token: "delta-token-67890"
```

## Validation Rules

| Validation                 | Error Message                         | Resolution                      |
|----------------------------|---------------------------------------|---------------------------------|
| Notification Type is empty | "Notification Type is required"       | Provide valid notification type |
| Invalid notification type  | "Unsupported notification type"       | Use supported notification type |
| Alert Recipients is empty  | "Alert Recipients are required"       | Provide at least one recipient  |
| Invalid email format       | "Invalid email address in recipients" | Correct email format            |
| Too many recipients        | "Maximum 50 recipients allowed"       | Reduce number of recipients     |

## Usage Examples

### Example 1: New Email Alert

**Scenario**: Send alert when new emails are received

**Input**:

```
Notification Type: "NEW_EMAIL"
Alert Recipients: "admin@company.com, support@company.com"
Alert Subject: "New Email Alert: Message Received"
```

**Output**:

```
Alert Status: "Alert sent successfully"
New Delta Token: "delta-token-abc123"
```

### Example 2: Email Status Change Alert

**Scenario**: Alert when email status changes (read/unread)

**Input**:

```
Notification Type: "EMAIL_UPDATED"
Delta Token: "delta-token-abc123"
Alert Recipients: "monitoring@company.com"
Alert Subject: "Email Status Change Alert"
```

**Output**:

```
Alert Status: "Alert sent successfully"
New Delta Token: "delta-token-def456"
```

### Example 3: Folder Change Alert

**Scenario**: Alert when folder structure changes

**Input**:

```
Notification Type: "FOLDER_CHANGED"
Alert Recipients: "admin@company.com"
```

**Output**:

```
Alert Status: "Alert sent successfully"
New Delta Token: "delta-token-ghi789"
```

## Business Rules

1. **Real-time Processing**: Alerts are sent immediately when changes are detected
2. **Delta Tracking**: Uses delta tokens to track incremental changes efficiently
3. **Duplicate Prevention**: Delta tokens prevent duplicate alerts for same changes
4. **Alert Delivery**: Alerts are sent via email to specified recipients
5. **Token Management**: New delta token is returned for subsequent operations
6. **Change Detection**: Monitors specified types of mailbox changes

## Limitations

1. **Webhook Dependency**: Requires webhook infrastructure for real-time notifications
2. **Network Connectivity**: Requires stable network connection for webhook delivery
3. **Rate Limits**: Subject to Microsoft Graph API rate limiting
4. **Alert Volume**: High-volume changes may result in many alerts
5. **Delivery Guarantee**: Email delivery depends on recipient email systems
6. **Token Expiration**: Delta tokens have limited lifetime

## Best Practices

### 1. Alert Management

- Use appropriate notification types for specific use cases
- Implement alert filtering to prevent notification overload
- Monitor alert volume and adjust notification criteria
- Provide meaningful alert subjects and content

### 2. Delta Token Handling

- Store and manage delta tokens properly
- Use delta tokens to prevent duplicate processing
- Handle token expiration gracefully
- Implement token refresh mechanisms

### 3. Recipient Management

- Use distribution lists for multiple recipients
- Validate recipient email addresses before sending
- Consider alert priority and recipient relevance
- Implement alert escalation procedures

### 4. Performance Optimization

- Monitor webhook performance and reliability
- Implement proper error handling for failed alerts
- Use appropriate notification types to reduce noise
- Consider batching alerts for high-volume scenarios

## Common Use Cases

### 1. Real-time Email Monitoring

```
Scenario: Monitor for urgent emails requiring immediate attention
Action: Set up NEW_EMAIL alerts for specific criteria
Result: Immediate notification when urgent emails arrive
```

### 2. System Monitoring

```
Scenario: Monitor mailbox changes for security or compliance
Action: Configure alerts for various mailbox change types
Result: Real-time visibility into mailbox activities
```

### 3. Automated Workflow Triggers

```
Scenario: Trigger automated workflows based on email events
Action: Use alerts to initiate processing workflows
Result: Automated response to email events
```

### 4. Administrative Notifications

```
Scenario: Keep administrators informed of system activities
Action: Send alerts to admin team for important changes
Result: Proactive administrative oversight
```

## Alert Workflow

### Step 1: Configure Notifications

1. Set up webhook infrastructure for receiving notifications
2. Configure notification types and criteria
3. Define alert recipients and subjects

### Step 2: Monitor Changes

1. Microsoft Graph sends webhook notifications for changes
2. Extension processes notifications based on configuration
3. Delta tokens track processed changes

### Step 3: Send Alerts

1. Generate alert emails based on notification criteria
2. Send alerts to specified recipients
3. Update delta tokens for next operation

### Step 4: Manage Tokens

1. Store new delta tokens for subsequent operations
2. Handle token expiration and refresh
3. Maintain change tracking continuity

## Related Catalog Requests

- [Check If Triggered Mail Ids Exist](pages/CheckIfTriggeredMailIdsExist.md) - Validate triggered emails
- [Fetch Latest Mail](pages/FetchLatestMail.md) - Get recent emails for processing
- [Health Check](pages/HealthCheck.md) - Monitor webhook and alert system health
- [Test Connection](pages/TestConnection.md) - Verify webhook connectivity

## Technical Implementation

### Helper Class

- **Class**: NotificationServiceImpl
- **Package**: app.krista.extensions.essentials.collaboration.outlook3.impl
- **Method**: sendAlertUsingNotificationDelta(String notificationType, String deltaToken, String alertRecipients, String
  alertSubject)
- **Service**: Microsoft Graph webhook notifications with alert processing

### Telemetry Metrics

- **NOTIFICATION_ALERT_SUCCESS**: Successful alert operations
- **NOTIFICATION_ALERT_FAILURE**: Failed alert operations
- **WEBHOOK_NOTIFICATIONS_PROCESSED**: Count of processed webhook notifications
- **DELTA_TOKEN_UPDATES**: Count of delta token updates

## Troubleshooting

### Webhook Not Receiving Notifications

**Cause**: Webhook configuration or connectivity issues
**Solution**:

1. Verify webhook endpoint is accessible
2. Check webhook subscription is active
3. Confirm network connectivity and firewall settings
4. Test webhook endpoint manually

### Alerts Not Being Sent

**Cause**: Email delivery or configuration issues
**Solution**:

1. Verify recipient email addresses are correct
2. Check email delivery systems are functioning
3. Confirm alert generation logic is working
4. Test with known good recipient addresses

### Delta Token Issues

**Cause**: Token expiration or management problems (HTTP 410 Gone error)

**Common Error Messages**:
- `SyncStateNotFound: The sync state generation is not found`
- `HTTP 410 Gone`
- `Error code: SyncStateNotFound`

**Solution**:

1. **Check if delta token has expired** (HTTP 410 or SyncStateNotFound error)
2. Implement proper token storage and retrieval
3. **Handle token refresh scenarios** - automatically clear expired token and restart delta query
4. Monitor token lifecycle and expiration (typically 7-30 days for Outlook entities)

**Technical Details**:
- Microsoft Graph returns **HTTP 410 Gone** when delta token expires
- Error message contains **"SyncStateNotFound"** with generation mismatch details
- Application **automatically clears expired token** and performs full synchronization
- New delta token is obtained and stored for future incremental syncs
- This is normal behavior and handled gracefully by the system

**Why Tokens Expire**:
- Long periods of inactivity (7-30 days typically)
- Server-side maintenance or migration
- Cache eviction when newer tokens fill up Microsoft's internal cache
- Tenant changes that invalidate old sync states

**Automatic Recovery**:
The system automatically handles token expiration:
1. Detects HTTP 410 error or SyncStateNotFound message
2. Logs a warning (not an error) about token expiration
3. Clears the expired delta token from storage
4. Performs a full synchronization to get current state
5. Stores new delta token for future incremental queries
6. Continues normal operation without manual intervention

### High Alert Volume

**Cause**: Too many notifications or broad criteria
**Solution**:

1. Refine notification criteria to reduce volume
2. Implement alert filtering and batching
3. Consider alert priority and importance
4. Monitor and adjust notification thresholds

## See Also

- [Extension Configuration](pages/ExtensionConfiguration.md) - Setup webhook configuration
- [Authentication](pages/Authentication.md) - Authentication for webhook operations
- [Check If Triggered Mail Ids Exist](pages/CheckIfTriggeredMailIdsExist.md) - Validate triggered emails
- [Health Check](pages/HealthCheck.md) - Monitor alert system health
