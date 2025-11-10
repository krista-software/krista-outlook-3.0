# Reply To All

## Overview

Replies to all recipients of the original email message, including the sender and all To/CC recipients. This catalog
request maintains the conversation thread while ensuring all original participants receive the reply.

## Request Details

- **Area**: Messaging
- **Type**: CHANGE_SYSTEM
- **Retry Support**: Yes - Failed requests can be retried automatically

## Input Parameters

| Parameter Name | Type      | Required | Description                                                    | Example                                        |
|----------------|-----------|----------|----------------------------------------------------------------|------------------------------------------------|
| Message ID     | Text      | Yes      | Unique identifier of the email to reply to                     | "AQMkADY4ZTFiMGIxLWU1YjUtNDEwMS04Y2Q0..."      |
| Message        | Rich Text | Yes      | Reply message content                                          | "Thank you all for your input on this matter." |
| Attachments    | File      | No       | Files to attach to the reply                                   | document.pdf                                   |
| BodyType       | PickOne   | No       | Format of the message body (Text or HTML)                      | "HTML"                                         |
| Allow Retry    | Boolean   | No       | Enable interactive retry on validation errors (default: false) | true                                           |

### Parameter Details

#### Message ID

- **Format**: Base64-encoded string from Microsoft Graph
- **Source**: Obtained from other catalog requests like Fetch Inbox, Fetch Mail Details By Query
- **Validation**: Must be a valid, existing message ID
- **Case Sensitivity**: Exact match required

#### Message

- **Format**: Rich text supporting HTML when BodyType is HTML
- **Content**: Supports text formatting, links, and basic HTML elements
- **Size**: Maximum 64KB for message body
- **Encoding**: UTF-8 encoding for international content

#### Attachments

- **Format**: File objects from Krista platform
- **Size Limit**: Individual files up to 25MB
- **Total Limit**: Combined attachments up to 25MB
- **Types**: All file types supported
- **Count**: Maximum 20 attachments per reply

#### BodyType

- **Options**: "Text" or "HTML"
- **Default**: "Text" if not specified
- **Text**: Plain text formatting only
- **HTML**: Rich HTML formatting with tags and styling

#### Allow Retry

- **Values**:
    - `true` - Prompt user to retry on validation errors
    - `false` or `null` - Return immediate error without retry option (default)
- **Purpose**: Controls error handling behavior for validation failures
- **Use Case**: Set to `true` for interactive workflows where users can correct invalid message IDs
- **Default Behavior**: When not specified or `false`, validation errors return immediately

## Output Parameters

| Parameter Name | Type | Description                  |
|----------------|------|------------------------------|
| Message        | Text | Success confirmation message |

**Example Output**: "Mail Sent Successfully"

## Validation Rules

| Validation            | Error Message                         | Resolution                                            |
|-----------------------|---------------------------------------|-------------------------------------------------------|
| Message ID is empty   | "Message ID cannot be empty"          | Provide valid message ID                              |
| Message ID not found  | "Unable to find email with messageID" | Verify message ID exists and is accessible            |
| Message content empty | "Message content cannot be empty"     | Provide reply message content                         |
| Attachment too large  | "Attachment exceeds size limit"       | Reduce attachment size or split into multiple replies |

## Error Handling

### Input Errors (INPUT_ERROR)

**Cause**: Invalid or missing required parameters
**Common Scenarios**:

- Empty or null message ID
- Missing reply message content
- Invalid message ID format
- Attachment size exceeds limits

**Resolution**: Validate all required parameters before submission

### Logic Errors (LOGIC_ERROR)

**Cause**: Business logic validation failures
**Common Scenarios**:

- Message ID not found in mailbox
- Original email cannot be accessed
- Reply permissions denied
- Attachment processing errors

**Resolution**: Verify message ID exists and user has reply permissions

### System Errors (SYSTEM_ERROR)

**Cause**: Microsoft Graph API or system-level failures
**Common Scenarios**:

- Network connectivity issues
- Microsoft Graph service unavailable
- Authentication token expired

**Resolution**: Retry the operation or check system connectivity

## Usage Examples

### Example 1: Team Discussion Reply

**Scenario**: Reply to team discussion with status update

**Input**:

```
Message ID: "AQMkADY4ZTFiMGIxLWU1YjUtNDEwMS04Y2Q0..."
Message: "Thank you all for your feedback. Based on your input, I'll proceed with option B as discussed."
BodyType: "Text"
```

**Output**:

```
Message: "Mail Sent Successfully"
```

### Example 2: Project Update with Attachment

**Scenario**: Reply to project team with updated documents

**Input**:

```
Message ID: "AQMkADY4ZTFiMGIxLWU1YjUtNDEwMS04Y2Q0..."
Message: "<h3>Project Update</h3><p>Thank you all for the great discussion.</p><p>Please find the updated project plan attached.</p>"
Attachments: ["updated_project_plan.pdf", "timeline.xlsx"]
BodyType: "HTML"
```

**Output**:

```
Message: "Mail Sent Successfully"
```

### Example 3: Meeting Follow-up

**Scenario**: Follow up on meeting discussion with all attendees

**Input**:

```
Message ID: "AQMkADY4ZTFiMGIxLWU1YjUtNDEwMS04Y2Q0..."
Message: "Thank you everyone for the productive meeting. Here are the action items we discussed:\n\n1. John - Complete requirements by Friday\n2. Sarah - Review design mockups\n3. Mike - Set up development environment\n\nLet me know if I missed anything."
BodyType: "Text"
```

**Output**:

```
Message: "Mail Sent Successfully"
```

## Business Rules

1. **Reply-All Scope**: Includes original sender and all To/CC recipients
2. **BCC Exclusion**: Original BCC recipients are never included in reply-all
3. **Self Exclusion**: The replying user is automatically excluded from recipients
4. **Thread Maintenance**: Reply maintains the original conversation thread
5. **Permission Validation**: User must have permission to reply to the original email
6. **Email Validation**: All recipient addresses are validated before sending

## Limitations

1. **Message ID Validity**: Message ID must exist and be accessible to the authenticated user
2. **Reply Permissions**: User must have permission to reply to the original email
3. **Recipient Limits**: Subject to Microsoft Graph API recipient limits
4. **Attachment Size**: Individual attachments limited to 25MB
5. **Total Email Size**: Complete reply including attachments limited to 25MB
6. **Rate Limits**: Subject to Microsoft Graph API rate limiting

## Best Practices

### 1. Message Relevance

- Ensure reply content is relevant to all recipients
- Consider if reply-all is appropriate for the message
- Use clear and concise language
- Provide context for all participants

### 2. Attachment Management

- Only attach files relevant to all recipients
- Use descriptive file names
- Consider file sizes and recipient limitations
- Compress large files when appropriate

### 3. Content Formatting

- Use appropriate formatting for the audience
- Keep messages professional and clear
- Include necessary context for all parties
- Consider using HTML for better formatting when needed

### 4. Error Handling

- Implement retry logic for transient failures
- Handle cases where original email is deleted
- Provide meaningful error messages to users
- Log reply operations for troubleshooting

## Common Use Cases

### 1. Team Collaboration

```
Scenario: Respond to team discussion with updates for all members
Action: Reply-all with status update or decision
Result: All team members receive the same information
```

### 2. Meeting Follow-up

```
Scenario: Send meeting notes or action items to all attendees
Action: Reply-all to meeting invitation with follow-up information
Result: All attendees receive consistent follow-up information
```

### 3. Project Communication

```
Scenario: Update project stakeholders on progress or changes
Action: Reply-all to project communication with updates
Result: All stakeholders stay informed of project status
```

## Related Catalog Requests

- [Reply To All With CC and BCC](pages/ReplyToAllWithCCAndBCC.md) - Reply-all with advanced recipient management
- [Reply To Mail](pages/ReplyToMail.md) - Reply to sender only
- [Reply To Mail With CC and BCC](pages/ReplyToMailWithCCAndBCC.md) - Reply with recipient control
- [Send Mail](pages/SendMail.md) - Send new emails

## Technical Implementation

### Helper Class

- **Class**: MessagingAreaImpl
- **Package**: app.krista.extensions.essentials.collaboration.outlook3.impl
- **Method**: replyToAll(String messageId, String message, List<File> attachments, String bodyType)
- **Validation**: Message ID validation and reply permission checking
- **Service**: Microsoft Graph Mail API reply-all functionality

### Telemetry Metrics

- **REPLY_TO_ALL_SUCCESS**: Successful reply-all operations
- **REPLY_TO_ALL_FAILURE**: Failed reply-all operations
- **REPLY_ALL_RECIPIENT_COUNT**: Number of recipients in reply-all
- **REPLY_ALL_WITH_ATTACHMENTS**: Reply-all messages with attachments

## Troubleshooting

### Message ID Not Found

**Cause**: Invalid or non-existent message ID
**Solution**:

1. Verify message ID is complete and correct
2. Check if original email has been deleted
3. Ensure user has access to the original email
4. Use Fetch Mail By Message Id to validate access

### Reply Not Sent to All Recipients

**Cause**: Recipient validation or delivery issues
**Solution**:

1. Verify all original recipients are still valid
2. Check for email server restrictions
3. Confirm recipients' email servers are accessible
4. Test with known good recipient addresses

### Attachment Issues

**Cause**: File size or format problems
**Solution**:

1. Check individual file sizes (max 25MB)
2. Verify total email size doesn't exceed limits
3. Ensure file formats are supported
4. Try replying without attachments first

### Permission Errors

**Cause**: Insufficient reply permissions
**Solution**:

1. Verify Mail.Send permission is granted
2. Check authentication token validity
3. Confirm user has reply permissions
4. Verify original email still exists

## See Also

- [Extension Configuration](pages/ExtensionConfiguration.md) - Setup and configuration
- [Authentication](pages/Authentication.md) - Authentication requirements
- [Reply To All With CC and BCC](pages/ReplyToAllWithCCAndBCC.md) - Advanced reply-all functionality
- [Reply To Mail](pages/ReplyToMail.md) - Basic reply functionality
