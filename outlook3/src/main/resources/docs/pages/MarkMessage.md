# Mark Message

## Overview

Marks an email message as read or unread using the message ID. This catalog request provides essential email management
functionality for tracking email status and organizing inbox workflows.

## Request Details

- **Area**: Messaging
- **Type**: CHANGE_SYSTEM
- **Retry Support**: Yes - Failed requests can be retried automatically

## Input Parameters

| Parameter Name | Type    | Required | Description                                                  | Example                                   |
|----------------|---------|----------|--------------------------------------------------------------|-------------------------------------------|
| Message ID     | Text    | Yes      | Unique identifier of the email to mark                       | "AQMkADY4ZTFiMGIxLWU1YjUtNDEwMS04Y2Q0..." |
| Is Read        | Boolean | Yes      | Mark as read (true) or unread (false)                        | true                                      |
| Allow Retry    | Boolean | No       | Enable interactive retry on validation errors (default: false) | true                                      |

### Parameter Details

#### Message ID

- **Format**: Base64-encoded string from Microsoft Graph
- **Source**: Obtained from other catalog requests like Fetch Inbox, Fetch Mail Details By Query
- **Validation**: Must be a valid, existing message ID
- **Case Sensitivity**: Exact match required

#### Is Read

- **Values**:
    - `true` - Mark email as read
    - `false` - Mark email as unread
- **Purpose**: Update the read status of the email
- **Effect**: Changes the visual appearance in email clients

#### Allow Retry

- **Values**:
    - `true` - Prompt user to retry on validation errors
    - `false` or `null` - Return immediate error without retry option (default)
- **Purpose**: Controls error handling behavior for validation failures
- **Use Case**: Set to `true` for interactive workflows where users can correct errors
- **Default Behavior**: When not specified or `false`, validation errors return immediately

## Output Parameters

| Parameter Name | Type | Description                  |
|----------------|------|------------------------------|
| Response       | Text | Success confirmation message |

**Example Output**: "Success"

## Validation Rules

| Validation           | Error Message                                           | Resolution                                 |
|----------------------|---------------------------------------------------------|--------------------------------------------|
| Message ID is empty  | "Message ID cannot be empty"                            | Provide a valid message ID                 |
| Message ID not found | "Unable to mark message, no email found with messageID" | Verify message ID exists and is accessible |
| Is Read is null      | "Is Read parameter is required"                         | Provide true or false value                |

## Error Handling

### Input Errors (INPUT_ERROR)

**Cause**: Invalid or missing required parameters
**Common Scenarios**:

- Empty or null message ID
- Missing Is Read parameter
- Invalid message ID format

**Resolution**: Validate all required parameters before submission

### Logic Errors (LOGIC_ERROR)

**Cause**: Business logic validation failures
**Common Scenarios**:

- Message ID not found in mailbox
- User lacks permission to modify the email
- Email has been deleted

**Resolution**: Verify message ID exists and user has modify permissions

### System Errors (SYSTEM_ERROR)

**Cause**: Microsoft Graph API or system-level failures
**Common Scenarios**:

- Network connectivity issues
- Microsoft Graph service unavailable
- Authentication token expired

**Resolution**: Retry the operation or check system connectivity

## Usage Examples

### Example 1: Mark Email as Read

**Scenario**: Mark a processed email as read

**Input**:

```
Message ID: "AQMkADY4ZTFiMGIxLWU1YjUtNDEwMS04Y2Q0..."
Is Read: true
```

**Output**:

```
Response: "Success"
```

### Example 2: Mark Email as Unread

**Scenario**: Mark an important email as unread for follow-up

**Input**:

```
Message ID: "AQMkADY4ZTFiMGIxLWU1YjUtNDEwMS04Y2Q0..."
Is Read: false
```

**Output**:

```
Response: "Success"
```

### Example 3: Interactive Retry on Validation Error

**Scenario**: Allow user to correct invalid message ID

**Input**:

```
Message ID: "invalid-id"
Is Read: true
Allow Retry: true
```

**Behavior**:
- System validates message ID and detects it's invalid
- User is prompted to re-enter the correct message ID
- User provides valid message ID and operation succeeds

### Example 4: Automated Processing Without Retry

**Scenario**: Automated workflow that handles errors programmatically

**Input**:

```
Message ID: "AQMkADY4ZTFiMGIxLWU1YjUtNDEwMS04Y2Q0..."
Is Read: true
Allow Retry: false
```

**Behavior**:
- If validation fails, error is returned immediately
- Calling application handles error programmatically
- No user interaction required

### Example 5: Workflow Integration

**Scenario**: Mark emails as read after automated processing

**Process**:

1. Fetch unread emails using [Fetch Inbox With Preferences](pages/FetchInboxWithPreferences.md)
2. Process each email according to business logic
3. Mark processed emails as read using this catalog request (Allow Retry: false for automation)
4. Continue with next batch of unread emails

## Business Rules

1. **Status Update**: Email read status is immediately updated in the mailbox
2. **Visual Impact**: Change is reflected in email clients (bold/normal text)
3. **Permission Required**: User must have modify permissions for the email
4. **Immediate Effect**: Status change is applied immediately
5. **Thread Independence**: Marking affects only the specific email, not the entire thread
6. **Reversible Operation**: Read status can be changed back and forth as needed

## Limitations

1. **Message ID Validity**: Message ID must exist and be accessible to the authenticated user
2. **Modify Permissions**: User must have permission to modify the email
3. **Single Email**: Operation affects only one email at a time
4. **Rate Limits**: Subject to Microsoft Graph API rate limiting
5. **Network Dependency**: Requires network connectivity to apply changes

## Best Practices

### 1. Message ID Management

- Store message IDs from previous catalog requests
- Validate message ID exists before attempting to mark
- Handle cases where emails may be deleted during processing
- Use consistent ID format throughout application

### 2. Workflow Integration

- Mark emails as read after successful processing
- Use unread status to identify emails requiring attention
- Implement proper error handling for marking operations
- Consider batch processing for multiple emails

### 3. Status Tracking

- Track read/unread status changes for audit purposes
- Use read status as part of email processing workflows
- Consider user preferences for automatic read marking
- Implement consistent status management across application

### 4. Error Handling

- Implement retry logic for transient failures
- Handle cases where emails are deleted during processing
- Provide meaningful error messages to users
- Log marking operations for troubleshooting

## Common Use Cases

### 1. Email Processing Workflow

```
Scenario: Mark emails as read after automated processing
Action: Process email content, then mark as read
Result: Processed emails are visually distinguished from unprocessed
```

### 2. Priority Email Management

```
Scenario: Mark important emails as unread for follow-up
Action: Mark high-priority emails as unread after initial review
Result: Important emails remain visible for follow-up action
```

### 3. Batch Email Processing

```
Scenario: Process multiple emails and update their status
Action: Fetch unread emails, process each, mark as read
Result: Systematic processing with clear status tracking
```

### 4. User Interface Integration

```
Scenario: Allow users to manually mark emails read/unread
Action: Provide UI controls that call this catalog request
Result: Users can manage email status through custom interface
```

## Related Catalog Requests

- [Mark Message Category And Status](pages/MarkMessageCategoryAndStatus.md) - Advanced message marking with categories
- [Fetch Inbox With Preferences](pages/FetchInboxWithPreferences.md) - Filter emails by read status
- [Fetch Inbox](pages/FetchInbox.md) - Retrieve emails with read status information
- [Add Category To Message](pages/AddCategoryToMessage.md) - Add categories to emails

## Technical Implementation

### Helper Class

- **Class**: MessagingAreaImpl
- **Package**: app.krista.extensions.essentials.collaboration.outlook3.impl
- **Method**: markMessage(String messageId, Boolean isRead)
- **Validation**: Message ID validation and permission checking
- **Service**: Microsoft Graph Mail API message update functionality

### Telemetry Metrics

- **MARK_MESSAGE_SUCCESS**: Successful message marking operations
- **MARK_MESSAGE_FAILURE**: Failed message marking operations
- **MARK_AS_READ**: Count of emails marked as read
- **MARK_AS_UNREAD**: Count of emails marked as unread

## Troubleshooting

### Message ID Not Found

**Cause**: Invalid or non-existent message ID
**Solution**:

1. Verify message ID is complete and correct
2. Check if email has been deleted or moved
3. Ensure user has access to the email
4. Use Fetch Mail By Message Id to validate access

### Permission Denied

**Cause**: User lacks permission to modify the email
**Solution**:

1. Verify Mail.ReadWrite permission is granted
2. Check if user owns or has access to the email
3. Confirm authentication token is valid
4. Test with known modifiable emails

### Operation Failed

**Cause**: System or network issues
**Solution**:

1. Check network connectivity to Microsoft Graph
2. Verify Microsoft Graph service status
3. Retry operation after brief delay
4. Check authentication token validity

### Inconsistent Status

**Cause**: Caching or synchronization issues
**Solution**:

1. Allow time for changes to propagate
2. Refresh email client or application
3. Verify operation completed successfully
4. Check for conflicting operations

## See Also

- [Extension Configuration](pages/ExtensionConfiguration.md) - Setup and configuration
- [Authentication](pages/Authentication.md) - Authentication requirements
- [Mark Message Category And Status](pages/MarkMessageCategoryAndStatus.md) - Advanced message marking
- [Fetch Inbox With Preferences](pages/FetchInboxWithPreferences.md) - Filter by read status
