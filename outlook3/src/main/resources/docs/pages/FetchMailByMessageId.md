# Fetch Mail By Message ID

## Overview

Retrieves detailed information about a specific email message using its unique message ID. This catalog request provides
comprehensive access to email content, metadata, attachments, and all associated properties for a single email.

## Request Details

- **Area**: Messaging
- **Type**: QUERY_SYSTEM
- **Retry Support**: Yes - Failed requests can be retried automatically

## Input Parameters

| Parameter Name | Type    | Required | Description                                                    | Example                                   |
|----------------|---------|----------|----------------------------------------------------------------|-------------------------------------------|
| Message ID     | Text    | Yes      | Unique identifier of the email to retrieve                     | "AQMkADY4ZTFiMGIxLWU1YjUtNDEwMS04Y2Q0..." |
| Allow Retry    | Boolean | No       | Enable interactive retry on validation errors (default: false) | true                                      |

### Parameter Details

#### Message ID

- **Format**: Base64-encoded string from Microsoft Graph
- **Source**: Obtained from other catalog requests like Fetch Inbox, Fetch Mail Details By Query
- **Validation**: Must be a valid, existing message ID
- **Case Sensitivity**: Exact match required
- **Uniqueness**: Each email has a unique message ID

#### Allow Retry

- **Values**:
    - `true` - Prompt user to retry on validation errors
    - `false` or `null` - Return immediate error without retry option (default)
- **Purpose**: Controls error handling behavior for validation failures
- **Use Case**: Set to `true` for interactive workflows where users can correct invalid message IDs
- **Default Behavior**: When not specified or `false`, validation errors return immediately

## Output Parameters

| Parameter Name | Type                | Description                          |
|----------------|---------------------|--------------------------------------|
| Mail           | Entity(Mail Details) | Complete email details and metadata |

### Mail Details Entity Structure

The returned email entity contains the following fields:

| Field Name             | Type       | Description                                    |
|------------------------|------------|------------------------------------------------|
| From                   | Text       | Sender's email address                         |
| To                     | Text       | Primary recipients (comma-separated)           |
| Subject                | Text       | Email subject line                             |
| Message                | Rich Text  | Email body content                             |
| Message ID             | Text       | Unique identifier for the email                |
| Cc                     | Text       | Carbon copy recipients (comma-separated)       |
| Bcc                    | Text       | Blind carbon copy recipients (comma-separated) |
| Is Read                | Boolean    | Whether the email has been read                |
| Reply To               | Text       | Reply-to email address                         |
| Send Date and Time     | Date       | When the email was sent                        |
| Received Date and Time | Date       | When the email was received                    |
| File Attachment        | List<File> | Attached files                                 |
| Item Attachment        | List<Text> | Embedded item attachments                      |
| Categories             | List<Text> | Email categories/labels                        |

## Validation Rules

| Validation           | Error Message                                                | Resolution                                 |
|----------------------|--------------------------------------------------------------|--------------------------------------------|
| Message ID is empty  | "Message ID cannot be empty"                                 | Provide a valid message ID                 |
| Message ID not found | "Unable to fetch email, no email found with given messageID" | Verify message ID exists and is accessible |

## Error Handling

### Input Errors (INPUT_ERROR)

**Cause**: Invalid or missing message ID
**Common Scenarios**:

- Empty or null message ID
- Invalid message ID format
- Message ID contains invalid characters

**Resolution**: Provide a valid message ID from a previous catalog request

### Logic Errors (LOGIC_ERROR)

**Cause**: Business logic validation failures
**Common Scenarios**:

- Message ID not found in mailbox
- User lacks permission to access the email
- Email has been deleted

**Resolution**: Verify message ID exists and user has access permissions

### System Errors (SYSTEM_ERROR)

**Cause**: Microsoft Graph API or system-level failures
**Common Scenarios**:

- Network connectivity issues
- Microsoft Graph service unavailable
- Authentication token expired

**Resolution**: Retry the operation or check system connectivity

## Usage Examples

### Example 1: Fetch Email Details

**Scenario**: Retrieve complete details of a specific email

**Input**:

```
Message ID: "AQMkADY4ZTFiMGIxLWU1YjUtNDEwMS04Y2Q0LTk3ZjYwNzI3YzQ3MQBGAAADqR..."
Allow Retry: false
```

**Output**:

```
Mail: {
  From: "sender@company.com",
  To: "recipient@company.com",
  Subject: "Project Update",
  Message: "Here's the latest project status...",
  Message ID: "AQMkADY4ZTFiMGIxLWU1YjUtNDEwMS04Y2Q0...",
  Is Read: true,
  Send Date and Time: "2023-10-15T14:30:00Z",
  File Attachment: ["report.pdf", "data.xlsx"]
}
```

### Example 2: Interactive Retry on Invalid Message ID

**Scenario**: Allow user to correct invalid message ID

**Input**:

```
Message ID: "invalid-message-id"
Allow Retry: true
```

**Behavior**:
- System validates message ID and detects it's invalid
- User is prompted to re-enter the correct message ID
- User provides valid message ID and email details are retrieved

### Example 3: Automated Processing Without Retry

**Scenario**: Automated workflow that handles errors programmatically

**Input**:

```
Message ID: "AQMkADY4ZTFiMGIxLWU1YjUtNDEwMS04Y2Q0..."
Allow Retry: false
```

**Behavior**:
- If validation fails, error is returned immediately
- Calling application handles error programmatically
- No user interaction required

## Business Rules

1. **Message ID Required**: Message ID must be provided and valid
2. **Access Permissions**: User must have read access to the email
3. **Single Email**: Returns details for exactly one email
4. **Complete Data**: All available email metadata and content is returned
5. **Attachment Handling**: File attachments are included in the response
6. **Read Status**: Email read status is not modified by this operation
7. **Error Handling Control**: Allow Retry parameter controls whether validation errors trigger interactive retry prompts or immediate error returns

## Limitations

1. **Message ID Validity**: Message ID must exist and be accessible to the authenticated user
2. **Read Permissions**: User must have permission to read the email
3. **Single Retrieval**: Operation retrieves only one email at a time
4. **Attachment Size**: Large attachments may impact response time
5. **Rate Limits**: Subject to Microsoft Graph API rate limiting

## Best Practices

### 1. Message ID Management

- Store message IDs from previous catalog requests
- Validate message ID format before making requests
- Handle cases where emails may be deleted
- Use consistent ID format throughout application

### 2. Error Handling

- Implement retry logic for transient failures
- Validate message ID exists before attempting retrieval
- Handle cases where emails are deleted during processing
- Provide meaningful error messages to users

### 3. Performance Optimization

- Cache email details when appropriate
- Batch multiple email retrievals when possible
- Consider pagination for large result sets
- Monitor API rate limits

### 4. Data Usage

- Extract only needed fields from response
- Handle attachments appropriately based on size
- Consider security implications of email content
- Implement proper data retention policies

## Common Use Cases

### 1. Email Detail View

```
Scenario: Display complete email details in user interface
Action: Fetch email by message ID and display all fields
Result: User sees comprehensive email information
```

### 2. Email Processing Workflow

```
Scenario: Process specific emails based on message ID
Action: Retrieve email details, process content, take action
Result: Automated email processing with full context
```

### 3. Email Verification

```
Scenario: Verify email exists and is accessible
Action: Attempt to fetch email by message ID
Result: Confirmation of email existence and accessibility
```

### 4. Attachment Retrieval

```
Scenario: Download attachments from specific email
Action: Fetch email by message ID, extract attachments
Result: Access to email attachments for processing
```

## Related Catalog Requests

- [Fetch Inbox](pages/FetchInbox.md) - Retrieve multiple emails from inbox
- [Fetch Mail Details By Query](pages/FetchMailDetailsByQuery.md) - Search for emails by query
- [Fetch Inbox With Preferences](pages/FetchInboxWithPreferences.md) - Fetch emails with filters
- [Move Message](pages/MoveMessage.md) - Move email to different folder
- [Mark Message](pages/MarkMessage.md) - Mark email as read/unread

## Technical Implementation

### Helper Class

- **Class**: MessagingAreaImpl
- **Package**: app.krista.extensions.essentials.collaboration.outlook3.impl
- **Method**: fetchMailByMessageId(String messageId, Boolean allowRetry)
- **Validation**: Message ID validation and existence checking
- **Service**: Microsoft Graph Mail API message retrieval functionality

### Telemetry Metrics

- **FETCH_MAIL_BY_MESSAGE_ID_SUCCESS**: Successful email retrieval operations
- **FETCH_MAIL_BY_MESSAGE_ID_FAILURE**: Failed email retrieval operations
- **MESSAGE_NOT_FOUND**: Message ID not found errors
- **INVALID_MESSAGE_ID**: Invalid message ID format errors

## Troubleshooting

### Message ID Not Found

**Cause**: Invalid or non-existent message ID
**Solution**:

1. Verify message ID is complete and correct
2. Check if email has been deleted
3. Ensure user has access to the email
4. Test with known valid message IDs

### Permission Denied

**Cause**: User lacks read permissions
**Solution**:

1. Verify Mail.Read permission is granted
2. Check if user has access to the mailbox
3. Confirm authentication token is valid
4. Test with accessible emails

### Invalid Message ID Format

**Cause**: Malformed message ID string
**Solution**:

1. Verify message ID format is correct
2. Check for truncation or corruption
3. Ensure proper encoding
4. Use message IDs from reliable sources

### Operation Timeout

**Cause**: Large attachments or slow network
**Solution**:

1. Check network connectivity
2. Consider attachment sizes
3. Retry operation after brief delay
4. Monitor API response times

## See Also

- [Extension Configuration](pages/ExtensionConfiguration.md) - Setup and configuration
- [Authentication](pages/Authentication.md) - Authentication requirements
- [Fetch Inbox](pages/FetchInbox.md) - Retrieve multiple emails
- [Fetch Mail Details By Query](pages/FetchMailDetailsByQuery.md) - Search emails

