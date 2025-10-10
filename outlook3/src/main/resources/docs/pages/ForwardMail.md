# Forward Mail

## Overview

Forwards an existing email message to one or more recipients with optional additional message content. This catalog request
maintains the original email content while allowing you to add forwarding comments and specify new recipients.

## Request Details

- **Area**: Messaging
- **Type**: CHANGE_SYSTEM
- **Retry Support**: Yes - Failed requests can be retried automatically

## Input Parameters

| Parameter Name | Type      | Required | Description                                                    | Example                                                  |
|----------------|-----------|----------|----------------------------------------------------------------|----------------------------------------------------------|
| Message Id     | Text      | Yes      | Unique identifier of the email to forward                      | "AQMkADY4ZTFiMGIxLWU1YjUtNDEwMS04Y2Q0..."                |
| To             | Text      | Yes      | Recipients to forward to (comma-separated)                     | "user1@company.com, user2@company.com"                   |
| Message        | Rich Text | No       | Additional message to include with the forward                 | "FYI - Please review the information below."             |
| BodyType       | PickOne   | No       | Format of the additional message (Text or HTML)                | "HTML"                                                   |
| Allow Retry    | Boolean   | No       | Enable interactive retry on validation errors (default: false) | true                                                     |

### Parameter Details

#### Message Id

- **Format**: Base64-encoded string from Microsoft Graph
- **Source**: Obtained from other catalog requests like Fetch Inbox, Fetch Mail Details By Query
- **Validation**: Must be a valid, existing message ID
- **Case Sensitivity**: Exact match required

#### To (Recipients)

- **Format**: Comma-separated email addresses
- **Validation**: Each email address is validated for proper format
- **Limit**: Maximum 100 recipients per forward
- **Invalid Handling**: Invalid email addresses are automatically skipped
- **Required**: At least one valid recipient must be provided

#### Message

- **Format**: Rich text supporting HTML when BodyType is HTML
- **Content**: Additional comments or context for the forward
- **Optional**: Can be empty if no additional message is needed
- **Size**: Maximum 64KB for message content
- **Encoding**: UTF-8 encoding for international content

#### BodyType

- **Options**: "Text" or "HTML"
- **Default**: "Text" if not specified
- **Text**: Plain text formatting only
- **HTML**: Rich HTML formatting with tags and styling
- **Applies To**: Only affects the additional message, not the original email

#### Allow Retry

- **Values**:
    - `true` - Prompt user to retry on validation errors
    - `false` or `null` - Return immediate error without retry option (default)
- **Purpose**: Controls error handling behavior for validation failures
- **Use Case**: Set to `true` for interactive workflows where users can correct invalid message IDs or email addresses
- **Default Behavior**: When not specified or `false`, validation errors return immediately

## Output Parameters

| Parameter Name | Type   | Description                                |
|----------------|--------|--------------------------------------------|
| Is Forwarded   | Switch | Confirmation that email was forwarded (true) |

**Example Output**: `Is Forwarded: true`

## Validation Rules

| Validation           | Error Message                                                | Resolution                                 |
|----------------------|--------------------------------------------------------------|--------------------------------------------|
| Message Id is empty  | "Message ID cannot be empty"                                 | Provide a valid message ID                 |
| Message Id not found | "Unable to forward email, no email found with given messageID" | Verify message ID exists and is accessible |
| To field is empty    | "To field cannot be empty"                                   | Provide at least one recipient             |
| Invalid email format | "Mail address is not valid. Please provide correct mail address" | Correct email format (user@domain.com)     |
| No valid recipients  | "At least one valid recipient is required"                   | Provide valid email addresses              |

## Error Handling

### Input Errors (INPUT_ERROR)

**Cause**: Invalid or missing required parameters
**Common Scenarios**:

- Empty or invalid message ID
- No valid recipients in To field
- Invalid email address format

**Resolution**: Validate all required parameters and email formats before submission

### Logic Errors (LOGIC_ERROR)

**Cause**: Business logic validation failures
**Common Scenarios**:

- Message ID not found in mailbox
- User lacks permission to forward the email
- Email has been deleted
- Recipient validation failures

**Resolution**: Verify message ID exists and user has forward permissions

### System Errors (SYSTEM_ERROR)

**Cause**: Microsoft Graph API or system-level failures
**Common Scenarios**:

- Network connectivity issues
- Microsoft Graph service unavailable
- Authentication token expired

**Resolution**: Retry the operation or check system connectivity

## Usage Examples

### Example 1: Simple Forward

**Scenario**: Forward an email to a colleague

**Input**:

```
Message Id: "AQMkADY4ZTFiMGIxLWU1YjUtNDEwMS04Y2Q0..."
To: "colleague@company.com"
Message: "FYI - Please review this request."
BodyType: "Text"
Allow Retry: false
```

**Output**:

```
Is Forwarded: true
```

### Example 2: Forward to Multiple Recipients

**Scenario**: Forward important email to team members

**Input**:

```
Message Id: "AQMkADY4ZTFiMGIxLWU1YjUtNDEwMS04Y2Q0..."
To: "team-member1@company.com, team-member2@company.com, manager@company.com"
Message: "<p><strong>Team,</strong></p><p>Please review the attached proposal and provide feedback by EOD.</p>"
BodyType: "HTML"
Allow Retry: false
```

**Output**:

```
Is Forwarded: true
```

### Example 3: Forward Without Additional Message

**Scenario**: Forward email without adding comments

**Input**:

```
Message Id: "AQMkADY4ZTFiMGIxLWU1YjUtNDEwMS04Y2Q0..."
To: "recipient@company.com"
Allow Retry: false
```

**Output**:

```
Is Forwarded: true
```

### Example 4: Interactive Retry on Invalid Email

**Scenario**: Allow user to correct invalid recipient email

**Input**:

```
Message Id: "AQMkADY4ZTFiMGIxLWU1YjUtNDEwMS04Y2Q0..."
To: "invalid-email"
Message: "Please review"
Allow Retry: true
```

**Behavior**:
- System validates email address and detects invalid format
- User is prompted to re-enter correct email address
- User provides valid address and email is forwarded successfully

### Example 5: Automated Forwarding Without Retry

**Scenario**: Automated workflow that forwards emails

**Input**:

```
Message Id: "AQMkADY4ZTFiMGIxLWU1YjUtNDEwMS04Y2Q0..."
To: "automated-inbox@company.com"
Message: "Auto-forwarded from processing system"
Allow Retry: false
```

**Behavior**:
- If validation fails, error is returned immediately
- Calling application handles error programmatically
- No user interaction required

## Business Rules

1. **Original Content Preserved**: Original email content and attachments are included in the forward
2. **Recipient Validation**: All email addresses are validated before forwarding
3. **Invalid Email Handling**: Invalid email addresses are automatically skipped
4. **Thread Continuity**: Forward maintains email thread and conversation history
5. **Attachment Handling**: All original attachments are included in the forwarded email
6. **Sender Attribution**: Forward clearly indicates it was forwarded by the current user
7. **Error Handling Control**: Allow Retry parameter controls whether validation errors trigger interactive retry prompts or immediate error returns

## Limitations

1. **Recipient Limit**: Maximum 100 recipients per forward
2. **Message ID Validity**: Message ID must exist and be accessible to the authenticated user
3. **Forward Permissions**: User must have permission to forward the email
4. **Attachment Size**: Original attachments are included, subject to size limits
5. **Rate Limits**: Subject to Microsoft Graph API rate limiting

## Best Practices

### 1. Recipient Management

- Validate email addresses before forwarding
- Consider using distribution lists for regular forwards
- Separate internal and external recipients appropriately
- Handle recipient validation errors gracefully

### 2. Message Content

- Provide context in additional message when appropriate
- Use HTML formatting for rich content presentation
- Keep additional messages concise and relevant
- Consider email client compatibility

### 3. Error Handling

- Implement retry logic for transient failures
- Validate all inputs before submission
- Handle partial delivery scenarios gracefully
- Provide meaningful error messages to users

### 4. Workflow Integration

- Forward emails as part of larger processing workflows
- Track forwarded emails for audit purposes
- Implement proper logging for forward operations
- Consider automation opportunities

## Common Use Cases

### 1. Email Routing

```
Scenario: Route customer emails to appropriate department
Action: Forward email to department-specific address
Result: Email reaches correct team for handling
```

### 2. Information Sharing

```
Scenario: Share important information with team
Action: Forward email to team members with context
Result: Team receives relevant information
```

### 3. Escalation Workflow

```
Scenario: Escalate issue to management
Action: Forward email to manager with explanation
Result: Issue is escalated with full context
```

### 4. Automated Distribution

```
Scenario: Automatically forward specific emails to archive
Action: Forward matching emails to archive address
Result: Emails are automatically archived
```

## Related Catalog Requests

- [Reply To Mail](pages/ReplyToMail.md) - Reply to the original sender
- [Reply To All](pages/ReplyToAll.md) - Reply to all recipients
- [Send Mail](pages/SendMail.md) - Send new email
- [Fetch Mail By Message Id](pages/FetchMailByMessageId.md) - Retrieve email details

## Technical Implementation

### Helper Class

- **Class**: MessagingAreaImpl
- **Package**: app.krista.extensions.essentials.collaboration.outlook3.impl
- **Method**: forwardMail(String messageId, String to, String message, String bodyType, Boolean allowRetry)
- **Validation**: Message ID and recipient email validation
- **Service**: Microsoft Graph Mail API forward functionality

### Telemetry Metrics

- **FORWARD_MAIL_SUCCESS**: Successful email forward operations
- **FORWARD_MAIL_FAILURE**: Failed email forward operations
- **RECIPIENT_COUNT**: Number of recipients per forward
- **INVALID_RECIPIENT**: Invalid recipient email addresses

## Troubleshooting

### Message Not Found

**Cause**: Invalid or non-existent message ID
**Solution**:

1. Verify message ID is complete and correct
2. Check if email has been deleted
3. Ensure user has access to the email
4. Test with known valid message IDs

### Invalid Recipients

**Cause**: Malformed email addresses
**Solution**:

1. Verify all email addresses are correct
2. Check for typos in recipient addresses
3. Ensure proper email format (user@domain.com)
4. Test with known good email addresses

### Permission Denied

**Cause**: User lacks forward permissions
**Solution**:

1. Verify Mail.Send permission is granted
2. Check if user has access to the original email
3. Confirm authentication token is valid
4. Test with accessible emails

### Forward Failed

**Cause**: System or network issues
**Solution**:

1. Check network connectivity to Microsoft Graph
2. Verify Microsoft Graph service status
3. Retry operation after brief delay
4. Check authentication token validity

## See Also

- [Extension Configuration](pages/ExtensionConfiguration.md) - Setup and configuration
- [Authentication](pages/Authentication.md) - Authentication requirements
- [Reply To Mail](pages/ReplyToMail.md) - Reply to emails
- [Send Mail](pages/SendMail.md) - Send new emails

