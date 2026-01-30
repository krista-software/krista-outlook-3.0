# Get Notification Delta

## Overview

Retrieves delta notifications that were missed by the alert event. This catalog request uses Microsoft Graph's delta query functionality to efficiently track incremental changes to emails in the inbox, returning only the message IDs that have changed since the last query.

## Request Details

- **Area**: Messaging
- **Type**: CHANGE_SYSTEM
- **Retry Support**: Yes - Failed requests can be retried automatically

## Input Parameters

This catalog request requires no input parameters. The delta token is automatically managed internally.

## Output Parameters

| Parameter Name | Type       | Description                                                |
|----------------|------------|------------------------------------------------------------|
| Message Ids    | [ Text ]   | List of message IDs that have changed since last query     |

**Example Output**:

```
Message Ids: ["AAMkAGI2T...", "AAMkAGI2U...", "AAMkAGI2V..."]
```

## How It Works

### Delta Query Mechanism

1. **First Call**: Performs a full synchronization of the inbox
   - Retrieves all messages from the last 24 hours
   - Stores a delta token for future incremental queries
   - Returns message IDs of all qualifying messages

2. **Subsequent Calls**: Performs incremental synchronization
   - Uses stored delta token to fetch only changes since last query
   - Returns only new, updated, or deleted message IDs
   - Updates delta token for next query

### Delta Token Management

- **Automatic Storage**: Delta tokens are automatically stored and retrieved
- **Expiration Handling**: Expired tokens are automatically detected and refreshed
- **Self-Healing**: System automatically recovers from token expiration errors
- **No Manual Intervention**: Token lifecycle is fully managed by the system

## Usage Examples

### Example 1: Initial Delta Query

**Scenario**: First time fetching notification delta

**Input**: None (no parameters required)

**Output**:
```
Message Ids: ["AAMkAGI2T...", "AAMkAGI2U...", "AAMkAGI2V..."]
```

**Behavior**: Returns all messages from the last 24 hours and stores delta token

### Example 2: Incremental Delta Query

**Scenario**: Subsequent delta query after initial call

**Input**: None (uses stored delta token automatically)

**Output**:
```
Message Ids: ["AAMkAGI2W..."]
```

**Behavior**: Returns only new messages since last query

### Example 3: After Token Expiration

**Scenario**: Delta query after token has expired (7-30 days of inactivity)

**Input**: None

**Output**:
```
Message Ids: ["AAMkAGI2X...", "AAMkAGI2Y...", "AAMkAGI2Z..."]
```

**Behavior**: Automatically clears expired token, performs full sync, stores new token

## Business Rules

1. **Time Window**: Only returns messages from the last 24 hours
2. **Incremental Tracking**: Uses delta tokens to track changes efficiently
3. **Automatic Recovery**: Handles token expiration automatically
4. **Duplicate Prevention**: Delta tokens prevent duplicate processing
5. **Change Detection**: Detects new, updated, and deleted messages
6. **Sender Filtering**: Excludes messages sent from the configured email account

## Limitations

1. **Time Constraint**: Limited to messages from the last 24 hours
2. **Token Expiration**: Delta tokens expire after 7-30 days of inactivity
3. **Rate Limits**: Subject to Microsoft Graph API rate limiting
4. **Network Dependency**: Requires stable connection to Microsoft Graph API
5. **Deleted Messages**: Deleted messages are tracked but content is not available

## Best Practices

### 1. Regular Polling

- Call this request at regular intervals (e.g., every 2 minutes)
- Avoid excessive polling to prevent rate limiting
- Use appropriate intervals based on business requirements
- Monitor API usage and adjust frequency as needed

### 2. Error Handling

- Implement retry logic for transient failures
- Handle empty result sets gracefully
- Log errors for troubleshooting
- Monitor for authentication issues

### 3. Message Processing

- Process returned message IDs promptly
- Use message IDs to fetch full message details if needed
- Implement idempotency to handle duplicate IDs
- Track processed messages to avoid reprocessing

## Delta Token Expiration and Recovery

### Understanding Token Expiration

**What Happens When Tokens Expire**:
- Microsoft Graph returns **HTTP 410 Gone** error
- Error message contains **"SyncStateNotFound"**
- Example: `The sync state generation is not found; generation=1609;[highest=1612]`

**Why Tokens Expire**:
- **Inactivity**: No queries for 7-30 days (typical for Outlook entities)
- **Server Maintenance**: Microsoft's internal maintenance or migration
- **Cache Eviction**: Newer tokens fill up Microsoft's internal cache
- **Tenant Changes**: Changes that invalidate old sync states

### Automatic Recovery Process

The system handles token expiration automatically:

1. **Detection**: Catches HTTP 410 error or SyncStateNotFound message
2. **Logging**: Logs a WARNING (not ERROR) about token expiration
3. **Cleanup**: Clears the expired delta token from storage
4. **Full Sync**: Performs a complete synchronization to get current state
5. **Token Update**: Stores new delta token for future queries
6. **Continuation**: Resumes normal incremental operation

**No Manual Intervention Required**: The entire recovery process is automatic and transparent.

### Expected Behavior

**Normal Operation**:
```
INFO  - Search for the existing delta link: https://graph.microsoft.com/v1.0/users/.../delta?$deltatoken=...
INFO  - Fetching delta link using last checkpoint
INFO  - Notification from current page being started
INFO  - All the Notification being fetched Storing new delta link
```

**During Token Expiration** (First occurrence):
```
INFO  - Search for the existing delta link: https://graph.microsoft.com/v1.0/users/.../delta?$deltatoken=...
INFO  - Fetching delta link using last checkpoint
WARN  - Delta token expired or sync state not found (HTTP 410). Clearing expired token and restarting delta query from scratch.
INFO  - Restarting delta query without checkpoint
INFO  - Notification from current page being started
INFO  - All the Notification being fetched Storing new delta link
```

**After Recovery** (Subsequent calls):
```
INFO  - Search for the existing delta link: https://graph.microsoft.com/v1.0/users/.../delta?$deltatoken=... (new token)
INFO  - Fetching delta link using last checkpoint
INFO  - Notification from current page being started
INFO  - All the Notification being fetched Storing new delta link
```

## Common Use Cases

### 1. Webhook Backup

```
Scenario: Ensure no notifications are missed if webhook fails
Action: Periodically call Get Notification Delta
Result: Catch any missed notifications from webhook downtime
```

### 2. Scheduled Email Processing

```
Scenario: Process new emails on a schedule
Action: Call Get Notification Delta every 2 minutes
Result: Retrieve and process new email IDs incrementally
```

### 3. Notification Recovery

```
Scenario: Recover from system downtime or maintenance
Action: Call Get Notification Delta after system restart
Result: Retrieve all changes that occurred during downtime
```

### 4. Change Monitoring

```
Scenario: Monitor inbox for specific types of changes
Action: Use delta query to track all inbox modifications
Result: Efficient change detection without full inbox scans
```

## Technical Implementation

### Helper Class

- **Class**: MessagingAreaImpl
- **Package**: app.krista.extensions.essentials.collaboration.outlook3.impl
- **Method**: fetchNotificationDelta()
- **Service**: Microsoft Graph delta query with automatic token management

### Implementation Details

**Delta Query Flow**:
1. Retrieve stored delta token from RefreshTokenStore
2. If token exists, use it for incremental query
3. If token is null or expired, perform full sync
4. Filter messages based on time window (last 24 hours)
5. Exclude messages from configured sender
6. Store new delta token for next query
7. Return list of message IDs

**Error Handling**:
- Catches `GraphServiceException` with response code 410
- Detects "SyncStateNotFound" in error message
- Automatically clears expired token and restarts query
- Logs warnings for token expiration (not errors)
- Re-throws other exceptions for proper error handling

### Telemetry Metrics

- **outlook3.getNotificationDelta.success**: Successful delta queries
- **outlook3.getNotificationDelta.failure**: Failed delta queries
- **outlook3.getNotificationDelta.validationError**: Validation or authorization errors

## Troubleshooting

### No Message IDs Returned

**Cause**: No new messages since last query
**Solution**: This is normal behavior - empty list means no changes

### Repeated Token Expiration Errors

**Cause**: System not deployed with latest error handling code
**Solution**: Deploy latest version with automatic token recovery

### Authentication Errors

**Cause**: Token expired or permissions revoked
**Solution**:
1. Re-authenticate user
2. Verify Mail.ReadWrite permission
3. Check token expiration
4. Test with [Test Connection](pages/TestConnection.md)

### Missing Recent Messages

**Cause**: Messages outside 24-hour window
**Solution**: Adjust time window expectations or use [Fetch Inbox](pages/FetchInbox.md) for older messages

## Related Catalog Requests

- [Send Alert Using Notification Delta](pages/SendAlertUsingNotificationDelta.md) - Send alerts based on delta changes
- [Receive Notification Of Email Change](pages/ReceiveNotificationOfEmailChange.md) - Webhook-based notifications
- [Fetch Latest Mail](pages/FetchLatestMail.md) - Get most recent email
- [Fetch Inbox](pages/FetchInbox.md) - Retrieve inbox emails with pagination
- [Test Connection](pages/TestConnection.md) - Verify connectivity

## See Also

- [Microsoft Graph Delta Query Documentation](https://learn.microsoft.com/en-us/graph/delta-query-overview)
- [Authentication](pages/Authentication.md) - OAuth 2.0 authentication setup
- [Extension Configuration](pages/ExtensionConfiguration.md) - Extension setup and configuration

