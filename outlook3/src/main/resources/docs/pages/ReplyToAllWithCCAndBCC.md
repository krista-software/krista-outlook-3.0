# Reply To All With CC and BCC

## Overview

Replies to all recipients of the original email message with the ability to add or override CC and BCC recipients. This
catalog request provides advanced recipient management for reply-all operations while maintaining conversation context.

## Request Details

- **Area**: Messaging
- **Type**: CHANGE_SYSTEM
- **Retry Support**: Yes - Failed requests can be retried automatically

## Input Parameters

| Parameter Name | Type      | Required | Description                                                    | Example                                             |
|----------------|-----------|----------|----------------------------------------------------------------|-----------------------------------------------------|
| Message ID     | Text      | Yes      | Unique identifier of the email to reply to                     | "AQMkADY4ZTFiMGIxLWU1YjUtNDEwMS04Y2Q0..."           |
| Message        | Rich Text | Yes      | Reply message content                                          | "Thank you all for your feedback on this proposal." |
| To             | Text      | Yes      | Primary recipients (overwrites original if set)                | "original-sender@company.com, team@company.com"     |
| Cc             | Text      | No       | Carbon copy recipients (overwrites original if set)            | "manager@company.com, director@company.com"         |
| Bcc            | Text      | No       | Blind carbon copy recipients (overwrites original if set)      | "compliance@company.com"                            |
| Reply To       | Text      | No       | Reply-to email address (overwrites original if set)            | "project-team@company.com"                          |
| Attachments    | File      | No       | Files to attach to the reply                                   | document.pdf                                        |
| BodyType       | PickOne   | No       | Format of the message body (Text or HTML)                      | "HTML"                                              |
| Allow Retry    | Boolean   | No       | Enable interactive retry on validation errors (default: false) | true                                                |

### Parameter Details

#### To (Primary Recipients)

- **Behavior**: Overwrites the original recipient list if provided
- **Default**: If not specified, uses original sender and all To/CC recipients
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
- **Default**: If not specified, no BCC recipients (original BCC not included)
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

### Example 1: Reply All with Additional CC

**Scenario**: Reply to team email and add executives to CC

**Input**:

```
Message ID: "AQMkADY4ZTFiMGIxLWU1YjUtNDEwMS04Y2Q0..."
Message: "Great work everyone! I've added our executives to keep them informed of our progress."
To: "team-lead@company.com, developer1@company.com, developer2@company.com"
Cc: "ceo@company.com, cto@company.com"
BodyType: "Text"
```

**Output**:

```
Message: "Mail Sent Successfully"
```

### Example 2: Reply All with BCC for Documentation

**Scenario**: Reply to project discussion with BCC to documentation team

**Input**:

```
Message ID: "AQMkADY4ZTFiMGIxLWU1YjUtNDEwMS04Y2Q0..."
Message: "<p>Thank you all for the valuable input.</p><p>I'll incorporate these suggestions into the final proposal.</p>"
To: "stakeholder1@company.com, stakeholder2@company.com"
Bcc: "documentation@company.com, archive@company.com"
BodyType: "HTML"
```

**Output**:

```
Message: "Mail Sent Successfully"
```

### Example 3: Reply All with Custom Reply-To

**Scenario**: Reply to customer inquiry with dedicated support reply-to

**Input**:

```
Message ID: "AQMkADY4ZTFiMGIxLWU1YjUtNDEwMS04Y2Q0..."
Message: "We appreciate your feedback and will address your concerns promptly."
To: "customer@external.com, account-manager@company.com"
Reply To: "customer-support@company.com"
BodyType: "Text"
```

**Output**:

```
Message: "Mail Sent Successfully"
```

## Business Rules

1. **Recipient Override**: Specified recipients completely replace original recipients
2. **Reply-All Scope**: Includes original sender and all To/CC recipients by default
3. **BCC Exclusion**: Original BCC recipients are never included in reply-all
4. **Self Exclusion**: The replying user is automatically excluded from recipients
5. **Email Validation**: All email addresses are validated before sending
6. **Invalid Email Handling**: Invalid email addresses are automatically skipped

## Limitations

1. **Message ID Validity**: Message ID must exist and be accessible
2. **Reply Permissions**: User must have permission to reply to original email
3. **Recipient Limits**: Subject to Microsoft Graph API recipient limits
4. **Attachment Size**: Individual attachments limited to 25MB
5. **Total Email Size**: Complete reply including attachments limited to 25MB
6. **Rate Limits**: Subject to Microsoft Graph API rate limiting

## Best Practices

### 1. Recipient Management

- Carefully consider who should receive reply-all messages
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

### 1. Executive Communication

```
Scenario: Reply to team discussion and include executives
Action: Reply-all with executives added to CC
Result: Team receives response and executives stay informed
```

### 2. Project Coordination

```
Scenario: Reply to project update with additional stakeholders
Action: Reply-all with new stakeholders in CC and documentation in BCC
Result: All relevant parties receive update with proper documentation
```

### 3. Customer Service Escalation

```
Scenario: Reply to customer issue with internal team coordination
Action: Reply-all with customer in To and internal team in CC
Result: Customer receives response and team stays coordinated
```

## Related Catalog Requests

- [Reply To All](pages/ReplyToAll.md) - Basic reply-all functionality
- [Reply To Mail With CC and BCC](pages/ReplyToMailWithCCAndBCC.md) - Reply with recipient management
- [Reply To Mail](pages/ReplyToMail.md) - Basic reply functionality
- [Send Mail](pages/SendMail.md) - Send new emails

## Technical Implementation

### Helper Class

- **Class**: MessagingAreaImpl
- **Package**: app.krista.extensions.essentials.collaboration.outlook3.impl
- **Method**: replyToAllWithCCAndBCC(String messageId, String to, String cc, String bcc, String replyTo, String message,
  List<File> attachments, String bodyType)
- **Validation**: Email address validation and recipient management
- **Service**: Microsoft Graph Mail API reply-all functionality

### Telemetry Metrics

- **REPLY_ALL_WITH_CC_BCC_SUCCESS**: Successful reply-all operations with CC/BCC
- **REPLY_ALL_WITH_CC_BCC_FAILURE**: Failed reply-all operations with CC/BCC
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
- [Reply To All](pages/ReplyToAll.md) - Basic reply-all functionality
- [Reply To Mail With CC and BCC](pages/ReplyToMailWithCCAndBCC.md) - Reply with recipient management
