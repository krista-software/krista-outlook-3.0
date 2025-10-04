# Reply To Mail

## Overview

Replies to a specific email message using the message ID, maintaining the conversation thread and context. This catalog request provides comprehensive reply functionality with attachment support and flexible content formatting.

## Request Details

- **Area**: Messaging
- **Type**: CHANGE_SYSTEM
- **Retry Support**: Yes - Failed requests can be retried automatically

## Input Parameters

| Parameter Name | Type | Required | Description | Example |
|----------------|------|----------|-------------|---------|
| Message ID | Text | Yes | Unique identifier of the email to reply to | "AQMkADY4ZTFiMGIxLWU1YjUtNDEwMS04Y2Q0..." |
| Message | Rich Text | Yes | Reply message content | "Thank you for your email. I'll review this and get back to you." |
| Attachments | File | No | Files to attach to the reply | document.pdf |
| BodyType | PickOne | No | Format of the message body (Text or HTML) | "HTML" |

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

## Output Parameters

| Parameter Name | Type | Description |
|----------------|------|-------------|
| Message | Text | Success confirmation message |

**Example Output**: "Mail Sent Successfully"

## Validation Rules

| Validation | Error Message | Resolution |
|------------|---------------|------------|
| Message ID is empty | "Message ID cannot be empty" | Provide valid message ID |
| Message ID not found | "Unable to find email with messageID" | Verify message ID exists and is accessible |
| Message content empty | "Message content cannot be empty" | Provide reply message content |
| Attachment too large | "Attachment exceeds size limit" | Reduce attachment size or split into multiple replies |

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

### Example 1: Simple Reply
**Scenario**: Reply to a customer inquiry with additional information

**Input**:
```
Message ID: "AQMkADY4ZTFiMGIxLWU1YjUtNDEwMS04Y2Q0..."
Message: "Thank you for your inquiry. I've reviewed your request and will have the information ready by tomorrow."
BodyType: "Text"
```

**Output**:
```
Message: "Mail Sent Successfully"
```

### Example 2: Reply with Attachment
**Scenario**: Reply to a request with supporting documents

**Input**:
```
Message ID: "AQMkADY4ZTFiMGIxLWU1YjUtNDEwMS04Y2Q0..."
Message: "<p>Thank you for your request.</p><p>Please find the requested documents attached.</p>"
Attachments: ["contract.pdf", "specifications.docx"]
BodyType: "HTML"
```

**Output**:
```
Message: "Mail Sent Successfully"
```

### Example 3: Formatted HTML Reply
**Scenario**: Reply with rich formatting and structured content

**Input**:
```
Message ID: "AQMkADY4ZTFiMGIxLWU1YjUtNDEwMS04Y2Q0..."
Message: "<h3>Project Update</h3><p>Here's the current status:</p><ul><li>Phase 1: Complete</li><li>Phase 2: In Progress</li><li>Phase 3: Scheduled</li></ul><p>Let me know if you need more details.</p>"
BodyType: "HTML"
```

**Output**:
```
Message: "Mail Sent Successfully"
```

## Business Rules

1. **Thread Maintenance**: Reply maintains the original conversation thread
2. **Recipient Handling**: Reply is sent to the original sender by default
3. **Subject Preservation**: Original subject is preserved with "Re:" prefix if not already present
4. **Context Preservation**: Original email context is maintained in the thread
5. **Permission Validation**: User must have permission to reply to the original email
6. **Attachment Processing**: Attachments are processed and validated before sending

## Limitations

1. **Message ID Validity**: Message ID must exist and be accessible to the authenticated user
2. **Reply Permissions**: User must have permission to reply to the original email
3. **Attachment Size**: Individual attachments limited to 25MB
4. **Total Email Size**: Complete reply including attachments limited to 25MB
5. **Attachment Count**: Maximum 20 attachments per reply
6. **Rate Limits**: Subject to Microsoft Graph API rate limiting

## Best Practices

### 1. Message ID Management
- Store message IDs from previous catalog requests
- Validate message ID exists before attempting reply
- Handle cases where original emails may be deleted
- Use consistent ID format throughout application

### 2. Reply Content
- Provide clear and relevant reply content
- Use appropriate formatting for the audience
- Include necessary context for the recipient
- Keep replies concise and professional

### 3. Attachment Handling
- Validate file sizes before attaching
- Use descriptive file names
- Consider security implications of attachments
- Compress large files when appropriate

### 4. Error Handling
- Implement retry logic for transient failures
- Handle cases where original email is deleted
- Provide meaningful error messages to users
- Log reply operations for troubleshooting

## Common Use Cases

### 1. Customer Service Responses
```
Scenario: Reply to customer inquiries with solutions
Action: Use message ID from customer email to send reply
Result: Customer receives response in the same thread
```

### 2. Document Sharing
```
Scenario: Reply to requests with requested documents
Action: Attach documents to reply message
Result: Recipient receives documents in context of original request
```

### 3. Status Updates
```
Scenario: Provide updates on ongoing requests or projects
Action: Reply to original email with current status
Result: Stakeholders receive updates in proper thread context
```

## Related Catalog Requests

- [Reply To Mail With CC and BCC](ReplyToMailWithCCAndBCC.md) - Reply with advanced recipient management
- [Reply To All](ReplyToAll.md) - Reply to all recipients of original email
- [Fetch Mail By Message Id](FetchMailByMessageId.md) - Get email details before replying
- [Send Mail](SendMail.md) - Send new emails

## Technical Implementation

### Helper Class
- **Class**: MessagingAreaImpl
- **Package**: app.krista.extensions.essentials.collaboration.outlook3.impl
- **Method**: replyToMail(List<File> attachments, String messageID, String message, String bodyType)
- **Validation**: Message ID validation and reply permission checking
- **Service**: Microsoft Graph Mail API reply functionality

### Telemetry Metrics
- **REPLY_TO_MAIL_SUCCESS**: Successful reply operations
- **REPLY_TO_MAIL_FAILURE**: Failed reply operations
- **REPLY_WITH_ATTACHMENTS**: Replies that include attachments
- **REPLY_MESSAGE_LENGTH**: Length of reply messages

## Troubleshooting

### Message ID Not Found
**Cause**: Invalid or non-existent message ID
**Solution**:
1. Verify message ID is complete and correct
2. Check if original email has been deleted
3. Ensure user has access to the original email
4. Use Fetch Mail By Message Id to validate access

### Reply Not Sent
**Cause**: Permission or authentication issues
**Solution**:
1. Verify Mail.Send permission is granted
2. Check authentication token validity
3. Confirm user has reply permissions
4. Verify original email still exists

### Attachment Issues
**Cause**: File size or format problems
**Solution**:
1. Check individual file sizes (max 25MB)
2. Verify total email size doesn't exceed limits
3. Ensure file formats are supported
4. Try replying without attachments first

### Thread Context Lost
**Cause**: Message ID or threading issues
**Solution**:
1. Verify correct message ID is used
2. Check if original email supports threading
3. Ensure reply is sent to correct recipient
4. Test with known good message IDs

## See Also

- [Extension Configuration](ExtensionConfiguration.md) - Setup and configuration
- [Authentication](Authentication.md) - Authentication requirements
- [Reply To Mail With CC and BCC](ReplyToMailWithCCAndBCC.md) - Advanced reply functionality
- [Fetch Mail By Message Id](FetchMailByMessageId.md) - Retrieve email details
