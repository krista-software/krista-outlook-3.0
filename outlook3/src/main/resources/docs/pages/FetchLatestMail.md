# Fetch Latest Mail

## Overview

Retrieves the most recent email received within the last two minutes. This catalog request is designed for real-time email monitoring and immediate processing of newly arrived messages.

## Request Details

- **Area**: Messaging
- **Type**: QUERY_SYSTEM
- **Retry Support**: ✅ Yes - Failed requests can be retried automatically

## Input Parameters

This catalog request requires no input parameters.

## Output Parameters

| Parameter Name | Type | Description |
|----------------|------|-------------|
| New Email | Entity(Mail Details) | The most recent email received within the last 2 minutes |

### Mail Details Entity Structure

The returned email entity contains the following fields:

| Field Name | Type | Description |
|------------|------|-------------|
| From | Text | Sender's email address |
| To | Text | Primary recipients (comma-separated) |
| Subject | Text | Email subject line |
| Message | Rich Text | Email body content |
| Message ID | Text | Unique identifier for the email |
| Cc | Text | Carbon copy recipients (comma-separated) |
| Bcc | Text | Blind carbon copy recipients (comma-separated) |
| Is Read | Boolean | Whether the email has been read (typically false for new emails) |
| Reply To | Text | Reply-to email address |
| Send Date and Time | Date | When the email was sent |
| Received Date and Time | Date | When the email was received |
| File Attachment | List<File> | Attached files |
| Item Attachment | List<Text> | Embedded item attachments |
| Categories | List<Text> | Email categories/labels |

## Validation Rules

This catalog request has no input validation rules as it requires no parameters.

## Error Handling

### Logic Errors (LOGIC_ERROR)
**Cause**: No recent emails found or mailbox access issues
**Common Scenarios**:
- No emails received in the last 2 minutes
- Mailbox access denied
- User permissions insufficient

**Resolution**: This is normal behavior when no recent emails exist

### System Errors (SYSTEM_ERROR)
**Cause**: Microsoft Graph API or system-level failures
**Common Scenarios**:
- Network connectivity issues
- Microsoft Graph service unavailable
- Authentication token expired

**Resolution**: Retry the operation or check system connectivity

### Authorization Errors
**Cause**: Insufficient permissions or authentication issues
**Common Scenarios**:
- Missing Mail.ReadWrite permission
- Expired authentication token
- User lacks mailbox access

**Resolution**: Verify permissions and re-authenticate if necessary

## Usage Examples

### Example 1: Check for New Email
**Scenario**: Monitor for newly arrived emails

**Input**: (No parameters required)

**Output** (when new email exists):
```json
{
  "New Email": {
    "From": "client@external.com",
    "To": "user@company.com",
    "Subject": "Urgent: Project Deadline Update",
    "Message": "We need to discuss the project deadline changes...",
    "Message ID": "AQMkADY4ZTFiMGIxLWU1YjUtNDEwMS04Y2Q0...",
    "Is Read": false,
    "Send Date and Time": 1640995200000,
    "Received Date and Time": 1640995205000,
    "File Attachment": []
  }
}
```

**Output** (when no new email):
```json
{
  "New Email": null
}
```

### Example 2: Real-time Email Processing
**Scenario**: Automated system checking for immediate email processing

**Input**: (No parameters required)

**Process**: 
1. Call Fetch Latest Mail every few minutes
2. If new email found, process immediately
3. If no new email, continue monitoring

### Example 3: Email Alert System
**Scenario**: Alert system for urgent emails

**Input**: (No parameters required)

**Process**:
1. Check for latest email
2. If found, analyze subject/sender for urgency
3. Send alerts for urgent emails
4. Mark email as processed

## Business Rules

1. **Time Window**: Only returns emails received within the last 2 minutes
2. **Single Result**: Returns only the most recent email, not multiple emails
3. **Real-time Monitoring**: Designed for frequent polling to catch new emails
4. **Access Control**: Only emails accessible to authenticated user are returned
5. **Null Response**: Returns null when no emails received in time window
6. **Chronological Order**: Returns the most recently received email

## Limitations

1. **Time Window**: Limited to 2-minute window for recent emails
2. **Single Email**: Returns only one email, not multiple recent emails
3. **Polling Required**: Requires active polling for real-time monitoring
4. **No Filtering**: Cannot filter by sender, subject, or other criteria
5. **Rate Limits**: Subject to Microsoft Graph API rate limiting
6. **Network Dependency**: Requires consistent network connectivity for real-time monitoring

## Best Practices

### 1. Polling Strategy
- Implement reasonable polling intervals (every 1-2 minutes)
- Use exponential backoff for failed requests
- Avoid excessive polling to prevent rate limiting
- Consider using webhook-based alternatives for true real-time notifications

### 2. Email Processing
- Process new emails immediately when found
- Store processed email IDs to avoid duplicate processing
- Implement proper error handling for email processing
- Consider email priority and urgency in processing logic

### 3. Performance Optimization
- Cache recent email IDs to avoid reprocessing
- Implement efficient email processing workflows
- Monitor polling frequency and adjust as needed
- Use asynchronous processing for complex email operations

### 4. Error Handling
- Handle null responses gracefully (no new emails)
- Implement retry logic for transient failures
- Log polling activities for troubleshooting
- Provide meaningful status updates for monitoring systems

## Common Use Cases

### 1. Real-time Email Monitoring
```
Scenario: Monitor for urgent customer emails
Action: Poll every 2 minutes for latest email
Result: Immediate detection and processing of urgent communications
```

### 2. Automated Email Processing
```
Scenario: Process incoming orders or requests immediately
Action: Check for latest email and trigger processing workflows
Result: Rapid response to time-sensitive business communications
```

### 3. Alert System Integration
```
Scenario: Integrate with alert systems for critical emails
Action: Monitor for emails from specific senders or with urgent keywords
Result: Immediate notifications for critical business communications
```

## Comparison with Other Approaches

### Fetch Latest Mail vs Webhook Notifications

| Aspect | Fetch Latest Mail | Webhook Notifications |
|--------|------------------|----------------------|
| **Method** | Polling-based | Event-driven |
| **Real-time** | Near real-time (2-minute window) | True real-time |
| **Setup** | No additional setup required | Requires webhook configuration |
| **Resource Usage** | Higher (frequent API calls) | Lower (event-driven) |
| **Reliability** | Depends on polling frequency | Depends on webhook availability |
| **Use Case** | Simple monitoring | Production real-time systems |

### Fetch Latest Mail vs Fetch Inbox

| Aspect | Fetch Latest Mail | Fetch Inbox |
|--------|------------------|-------------|
| **Time Scope** | Last 2 minutes only | All inbox emails |
| **Result Count** | Single email | Multiple emails (up to 15) |
| **Pagination** | Not supported | Supported |
| **Use Case** | Real-time monitoring | General inbox browsing |

## Related Catalog Requests

- [Fetch Inbox](FetchInbox.md) - Retrieve inbox emails with pagination
- [Fetch Inbox With Preferences](FetchInboxWithPreferences.md) - Advanced inbox filtering
- [Fetch Mail Details By Query](FetchMailDetailsByQuery.md) - Search for specific emails
- [Send Alert Using Notification Delta](SendAlertUsingNotificationDelta.md) - Real-time email notifications

## Technical Implementation

### Helper Class
- **Class**: MessagingAreaImpl
- **Package**: app.krista.extensions.essentials.collaboration.outlook3.impl
- **Method**: fetchLatestMail()
- **Service**: Microsoft Graph Mail API with time-based filtering

### Telemetry Metrics
- **FETCH_LATEST_MAIL_SUCCESS**: Successful latest email retrievals
- **FETCH_LATEST_MAIL_FAILURE**: Failed latest email retrievals
- **NEW_EMAIL_FOUND**: Count of new emails detected
- **NO_NEW_EMAIL**: Count of polls with no new emails

## Troubleshooting

### No New Email Returned
**Cause**: No emails received in the last 2 minutes
**Solution**:
1. This is normal behavior when no recent emails exist
2. Continue polling at regular intervals
3. Verify email delivery is working by sending test email
4. Check if emails are being delivered to correct folder

### Polling Performance Issues
**Cause**: Too frequent polling or network issues
**Solution**:
1. Adjust polling frequency to reasonable intervals
2. Implement exponential backoff for failed requests
3. Monitor API rate limits and usage
4. Consider using webhook-based alternatives for better performance

### Missing Recent Emails
**Cause**: Timing issues or email delivery delays
**Solution**:
1. Account for email delivery delays
2. Consider slightly longer time windows in processing logic
3. Verify system clock synchronization
4. Test with known email delivery times

### Authentication Issues During Polling
**Cause**: Token expiration during long-running polling
**Solution**:
1. Implement automatic token refresh
2. Handle authentication errors gracefully
3. Re-authenticate when tokens expire
4. Monitor token expiration times

## See Also

- [Extension Configuration](ExtensionConfiguration.md) - Setup and configuration
- [Authentication](Authentication.md) - Authentication requirements
- [Send Alert Using Notification Delta](SendAlertUsingNotificationDelta.md) - Real-time webhook notifications
- [Fetch Inbox](FetchInbox.md) - General inbox email retrieval
