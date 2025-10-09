# Reply To Mail With CC and BCC

## Overview

Replies to a specific email message with advanced recipient management, allowing you to override or add CC and BCC
recipients. This catalog request provides comprehensive reply functionality with full control over recipient lists and
attachment support.

## Request Details

- **Area**: Messaging
- **Type**: CHANGE_SYSTEM
- **Retry Support**: Yes - Failed requests can be retried automatically

## Input Parameters

| Parameter Name | Type      | Required | Description                                               | Example                                                   |
|----------------|-----------|----------|-----------------------------------------------------------|-----------------------------------------------------------|
| Message ID     | Text      | Yes      | Unique identifier of the email to reply to                | "AQMkADY4ZTFiMGIxLWU1YjUtNDEwMS04Y2Q0..."                 |
| Message        | Rich Text | Yes      | Reply message content                                     | "Thank you for your email. Please see my response below." |
| To             | Text      | Yes      | Primary recipients (overwrites original if set)           | "original-sender@company.com"                             |
| Cc             | Text      | No       | Carbon copy recipients (overwrites original if set)       | "manager@company.com, team@company.com"                   |
| Bcc            | Text      | No       | Blind carbon copy recipients (overwrites original if set) | "compliance@company.com"                                  |
| Reply To       | Text      | No       | Reply-to email address (overwrites original if set)       | "support@company.com"                                     |
| Attachments    | File      | No       | Files to attach to the reply                              | document.pdf                                              |
| BodyType       | PickOne   | No       | Format of the message body (Text or HTML)                 | "HTML"                                                    |

### Parameter Details

#### To (Primary Recipients)

- **Behavior**: Overwrites the original recipient list if provided
- **Default**: If not specified, uses original sender
- **Format**: Comma-separated email addresses
- **Validation**: Each email must be valid format
- **Invalid Handling**: Invalid emails are automatically skipped

#### Cc (Carbon Copy)

- **Behavior**: Overwrites the original CC list if provided
- **Default**: If not specified, uses original CC list
- **Format**: Comma-separated email addresses
- **Visibility**: Recipients can see CC addresses
- **Invalid Handling**: Invalid emails are automatically skipped

#### Bcc (Blind Carbon Copy)

- **Behavior**: Overwrites the original BCC list if provided
- **Default**: If not specified, no BCC recipients
- **Format**: Comma-separated email addresses
- **Visibility**: Recipients cannot see BCC addresses
- **Invalid Handling**: Invalid emails are automatically skipped

#### Reply To

- **Behavior**: Overwrites the original reply-to address if provided
- **Default**: If not specified, uses original reply-to or sender
- **Format**: Single email address
- **Purpose**: Specifies where future replies should be sent

## Output Parameters

| Parameter Name | Type | Description                  |
|----------------|------|------------------------------|
| Message        | Text | Success confirmation message |

**Example Output**: "Mail Sent Successfully"

## Validation Rules

| Validation            | Error Message                                                    | Resolution                     |
|-----------------------|------------------------------------------------------------------|--------------------------------|
| Message ID is empty   | "Message ID cannot be empty"                                     | Provide valid message ID       |
| Message ID not found  | "Unable to find email with messageID"                            | Verify message ID exists       |
| Message content empty | "Message content cannot be empty"                                | Provide reply message content  |
| To field is empty     | "To field cannot be empty"                                       | Provide at least one recipient |
| Invalid email format  | "Mail address is not valid. Please provide correct mail address" | Correct email format           |

## Usage Examples

### Example 1: Reply with Additional CC

**Scenario**: Reply to customer inquiry and add manager to CC

**Input**:

```
Message ID: "AQMkADY4ZTFiMGIxLWU1YjUtNDEwMS04Y2Q0..."
Message: "Thank you for your inquiry. I've reviewed your request and will have the information ready by tomorrow."
To: "customer@external.com"
Cc: "manager@company.com"
BodyType: "Text"
```

**Output**:

```
Message: "Mail Sent Successfully"
```

### Example 2: Reply with BCC for Documentation

**Scenario**: Reply to project discussion with BCC to documentation team

**Input**:

```
Message ID: "AQMkADY4ZTFiMGIxLWU1YjUtNDEwMS04Y2Q0..."
Message: "<p>Thank you for the update.</p><p>I'll incorporate these changes into the project plan.</p>"
To: "project-lead@company.com"
Cc: "team@company.com"
Bcc: "documentation@company.com"
BodyType: "HTML"
```

**Output**:

```
Message: "Mail Sent Successfully"
```

### Example 3: Reply with Custom Reply-To

**Scenario**: Reply to support request with dedicated support reply-to

**Input**:

```
Message ID: "AQMkADY4ZTFiMGIxLWU1YjUtNDEwMS04Y2Q0..."
Message: "We've received your support request and will respond within 24 hours."
To: "user@external.com"
Reply To: "support@company.com"
Attachments: ["troubleshooting_guide.pdf"]
BodyType: "Text"
```

**Output**:

```
Message: "Mail Sent Successfully"
```

## Business Rules

1. **Recipient Override**: Specified recipients completely replace original recipients
2. **Thread Maintenance**: Reply maintains the original conversation thread
3. **Email Validation**: All email addresses are validated before sending
4. **Invalid Email Handling**: Invalid email addresses are automatically skipped
5. **Permission Validation**: User must have permission to reply to the original email
6. **Attachment Processing**: Attachments are processed and validated before sending

## Limitations

1. **Message ID Validity**: Message ID must exist and be accessible
2. **Reply Permissions**: User must have permission to reply to original email
3. **Recipient Limits**: Subject to Microsoft Graph API recipient limits
4. **Attachment Size**: Individual attachments limited to 25MB
5. **Total Email Size**: Complete reply including attachments limited to 25MB
6. **Rate Limits**: Subject to Microsoft Graph API rate limiting

## Best Practices

### 1. Recipient Management

- Carefully consider who should receive the reply
- Use BCC for large recipient lists to protect privacy
- Validate all email addresses before sending
- Document recipient override behavior for users

### 2. Reply Content

- Ensure message is relevant to all recipients
- Use appropriate formatting for the audience
- Include necessary context for all parties
- Keep replies concise and professional

### 3. Security Considerations

- Be cautious when adding external recipients
- Use BCC for sensitive recipient lists
- Verify reply-to addresses are legitimate
- Consider data privacy implications

### 4. Error Handling

- Implement retry logic for transient failures
- Handle cases where original email is deleted
- Provide meaningful error messages to users
- Log reply operations for troubleshooting

## Common Use Cases

### 1. Customer Service Escalation

```
Scenario: Reply to customer issue with internal team coordination
Action: Reply with customer in To and internal team in CC
Result: Customer receives response and team stays coordinated
```

### 2. Project Communication

```
Scenario: Reply to project update with additional stakeholders
Action: Reply with new stakeholders in CC and documentation in BCC
Result: All relevant parties receive update with proper documentation
```

### 3. Support Request Management

```
Scenario: Reply to support request with specialized team involvement
Action: Reply with user in To and support team in CC
Result: User receives response and support team stays informed
```

## Related Catalog Requests

- [Reply To Mail](pages/ReplyToMail.md) - Basic reply functionality
- [Reply To All With CC and BCC](pages/ReplyToAllWithCCAndBCC.md) - Reply-all with recipient management
- [Reply To All](pages/ReplyToAll.md) - Basic reply-all functionality
- [Send Mail](pages/SendMail.md) - Send new emails

## Technical Implementation

### Helper Class

- **Class**: MessagingAreaImpl
- **Package**: app.krista.extensions.essentials.collaboration.outlook3.impl
- **Method**: replyToMailWithCCAndBCC(String messageId, String to, String cc, String bcc, String replyTo, String
  message, List<File> attachments, String bodyType)
- **Validation**: Email address validation and recipient management
- **Service**: Microsoft Graph Mail API reply functionality

### Telemetry Metrics

- **REPLY_WITH_CC_BCC_SUCCESS**: Successful reply operations with CC/BCC
- **REPLY_WITH_CC_BCC_FAILURE**: Failed reply operations with CC/BCC
- **RECIPIENT_OVERRIDE**: Recipient list overrides
- **INVALID_RECIPIENT**: Invalid recipient addresses

## Troubleshooting

### Recipients Not Receiving Reply

**Cause**: Invalid email addresses or delivery issues
**Solution**:

1. Verify all email addresses are correct
2. Check for typos in recipient addresses
3. Confirm recipients' email servers are accessible
4. Test with known good email addresses

### Reply Not Sent

**Cause**: Permission or authentication issues
**Solution**:

1. Verify Mail.Send permission is granted
2. Check authentication token validity
3. Confirm user has reply permissions
4. Verify original email still exists

### CC/BCC Not Working

**Cause**: Email client or server limitations
**Solution**:

1. Test with different email clients
2. Verify recipient email servers support CC/BCC
3. Check for email server restrictions
4. Try with smaller recipient lists

### Thread Context Lost

**Cause**: Message ID or threading issues
**Solution**:

1. Verify correct message ID is used
2. Check if original email supports threading
3. Ensure reply is sent to correct recipients
4. Test with known good message IDs

## See Also

- [Extension Configuration](pages/ExtensionConfiguration.md) - Setup and configuration
- [Authentication](pages/Authentication.md) - Authentication requirements
- [Reply To Mail](pages/ReplyToMail.md) - Basic reply functionality
- [Reply To All With CC and BCC](pages/ReplyToAllWithCCAndBCC.md) - Reply-all with recipient management
