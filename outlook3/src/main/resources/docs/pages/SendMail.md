# Send Mail

## Overview

Sends new email messages with comprehensive recipient management, attachment support, and flexible content formatting.
This catalog request provides full email composition capabilities with advanced recipient handling including To, CC,
BCC, and Reply-To fields.

## Request Details

- **Area**: Messaging
- **Type**: CHANGE_SYSTEM
- **Retry Support**: Yes - Failed requests can be retried automatically

## Input Parameters

| Parameter Name | Type      | Required | Description                                    | Example                                    |
|----------------|-----------|----------|------------------------------------------------|--------------------------------------------|
| Subject        | Text      | Yes      | Email subject line                             | "Project Status Update"                    |
| Message        | Rich Text | Yes      | Email body content                             | "Please find the latest project status..." |
| To             | Text      | Yes      | Primary recipients (comma-separated)           | "user1@company.com, user2@company.com"     |
| Cc             | Text      | No       | Carbon copy recipients (comma-separated)       | "manager@company.com"                      |
| Bcc            | Text      | No       | Blind carbon copy recipients (comma-separated) | "archive@company.com"                      |
| Reply To       | Text      | No       | Reply-to email address                         | "noreply@company.com"                      |
| Attachments    | File      | No       | Files to attach to the email                   | document.pdf                               |
| BodyType       | PickOne   | No       | Format of the message body (Text or HTML)      | "HTML"                                     |

### Parameter Details

#### Subject

- **Format**: Plain text string
- **Length**: Maximum 255 characters recommended
- **Special Characters**: Supported, but avoid control characters
- **Encoding**: UTF-8 encoding for international characters

#### Message

- **Format**: Rich text supporting HTML when BodyType is HTML
- **Content**: Supports text formatting, links, and basic HTML elements
- **Size**: Maximum 64KB for message body
- **Encoding**: UTF-8 encoding for international content

#### To (Primary Recipients)

- **Format**: Comma-separated email addresses
- **Validation**: Each email address is validated for proper format
- **Limit**: Maximum 100 recipients per email
- **Invalid Handling**: Invalid email addresses are automatically skipped

#### Cc (Carbon Copy)

- **Format**: Comma-separated email addresses
- **Visibility**: Recipients can see CC addresses
- **Validation**: Each email address is validated
- **Limit**: Combined with To, maximum 100 total recipients

#### Bcc (Blind Carbon Copy)

- **Format**: Comma-separated email addresses
- **Visibility**: Recipients cannot see BCC addresses
- **Validation**: Each email address is validated
- **Limit**: Combined with To and CC, maximum 100 total recipients

#### Reply To

- **Format**: Single email address
- **Purpose**: Specifies where replies should be sent
- **Default**: If not specified, replies go to sender
- **Validation**: Must be valid email address format

#### Attachments

- **Format**: File objects from Krista platform
- **Size Limit**: Individual files up to 25MB
- **Total Limit**: Combined attachments up to 25MB
- **Types**: All file types supported
- **Count**: Maximum 20 attachments per email

#### BodyType

- **Options**: "Text" or "HTML"
- **Default**: "Text" if not specified
- **Text**: Plain text formatting only
- **HTML**: Rich HTML formatting with tags and styling

## Output Parameters

| Parameter Name | Type | Description                                     |
|----------------|------|-------------------------------------------------|
| Message        | Text | Success confirmation with recipient information |

**Example Output**: "Mail Sent Successfully To: user1@company.com, user2@company.com"

## Validation Rules

| Validation           | Error Message                                                    | Resolution                                           |
|----------------------|------------------------------------------------------------------|------------------------------------------------------|
| Subject is empty     | "Subject cannot be empty"                                        | Provide email subject                                |
| Message is empty     | "Message content cannot be empty"                                | Provide email body content                           |
| To field is empty    | "To field cannot be empty"                                       | Provide at least one recipient                       |
| Invalid email format | "Mail address is not valid. Please provide correct mail address" | Correct email format (user@domain.com)               |
| No valid recipients  | "At least one valid recipient is required"                       | Provide valid email addresses                        |
| Attachment too large | "Attachment exceeds size limit"                                  | Reduce attachment size or split into multiple emails |

## Error Handling

### Input Errors (INPUT_ERROR)

**Cause**: Invalid or missing required parameters
**Common Scenarios**:

- Empty subject or message
- No valid recipients in To field
- Invalid email address format
- Attachment size exceeds limits

**Resolution**: Validate all required parameters and email formats before submission

### Logic Errors (LOGIC_ERROR)

**Cause**: Business logic validation failures
**Common Scenarios**:

- Recipient validation failures
- Attachment processing errors
- Content formatting issues

**Resolution**: Review recipient lists and attachment formats

### System Errors (SYSTEM_ERROR)

**Cause**: Microsoft Graph API or system-level failures
**Common Scenarios**:

- Network connectivity issues
- Microsoft Graph service unavailable
- Authentication token expired

**Resolution**: Retry the operation or check system connectivity

## Usage Examples

### Example 1: Basic Email

**Scenario**: Send simple notification email

**Input**:

```
Subject: "Daily Report Available"
Message: "The daily report has been generated and is ready for review."
To: "team@company.com"
BodyType: "Text"
```

**Output**:

```
Message: "Mail Sent Successfully To: team@company.com"
```

### Example 2: Email with CC and BCC

**Scenario**: Send project update with management visibility

**Input**:

```
Subject: "Project Alpha Milestone Completed"
Message: "<h2>Milestone Achievement</h2><p>We have successfully completed the first milestone of Project Alpha.</p>"
To: "team@company.com"
Cc: "manager@company.com"
Bcc: "executives@company.com"
BodyType: "HTML"
```

**Output**:

```
Message: "Mail Sent Successfully To: team@company.com, manager@company.com"
```

### Example 3: Email with Attachments

**Scenario**: Send report with supporting documents

**Input**:

```
Subject: "Monthly Financial Report"
Message: "Please find attached the monthly financial report and supporting documentation."
To: "finance-team@company.com"
Cc: "cfo@company.com"
Attachments: ["monthly_report.pdf", "supporting_data.xlsx"]
Reply To: "finance-reports@company.com"
BodyType: "Text"
```

**Output**:

```
Message: "Mail Sent Successfully To: finance-team@company.com, cfo@company.com"
```

## Business Rules

1. **Recipient Validation**: All email addresses are validated before sending
2. **Invalid Email Handling**: Invalid email addresses are automatically skipped
3. **Attachment Processing**: Attachments are processed and validated before sending
4. **Content Encoding**: All content is properly encoded for international characters
5. **Size Limits**: Email size including attachments cannot exceed 25MB
6. **Delivery Confirmation**: Success message includes actual recipients who received the email

## Limitations

1. **Recipient Limit**: Maximum 100 recipients per email (To + CC + BCC combined)
2. **Attachment Size**: Individual attachments limited to 25MB
3. **Total Email Size**: Complete email including attachments limited to 25MB
4. **Attachment Count**: Maximum 20 attachments per email
5. **Subject Length**: Recommended maximum 255 characters
6. **Rate Limits**: Subject to Microsoft Graph API rate limiting

## Best Practices

### 1. Recipient Management

- Validate email addresses before sending
- Use BCC for large recipient lists to protect privacy
- Consider using distribution lists for regular communications
- Separate internal and external recipients appropriately

### 2. Content Formatting

- Use HTML formatting for rich content presentation
- Keep plain text alternative for accessibility
- Optimize images and content for email clients
- Test formatting across different email clients

### 3. Attachment Handling

- Compress large files before attaching
- Use cloud storage links for very large files
- Validate file types and content before sending
- Consider security implications of attachments

### 4. Error Handling

- Implement retry logic for transient failures
- Validate all inputs before submission
- Handle partial delivery scenarios gracefully
- Provide meaningful error messages to users

## Common Use Cases

### 1. Automated Notifications

```
Scenario: System sends automated status notifications
Action: Send email with system status and relevant details
Result: Stakeholders receive timely system updates
```

### 2. Report Distribution

```
Scenario: Distribute regular reports to team members
Action: Send email with report attachments to distribution list
Result: Team receives reports automatically on schedule
```

### 3. Customer Communications

```
Scenario: Send customer service responses
Action: Send personalized email responses with relevant information
Result: Customers receive timely and professional communications
```

## Related Catalog Requests

- [Send Mail With Table](pages/SendMailWithTable.md) - Send emails with dynamic HTML tables
- [Reply To Mail](pages/ReplyToMail.md) - Reply to existing emails
- [Reply To All](pages/ReplyToAll.md) - Reply to all recipients of an email
- [Test Connection](pages/TestConnection.md) - Verify email sending capabilities

## Technical Implementation

### Helper Class

- **Class**: MessagingAreaImpl
- **Package**: app.krista.extensions.essentials.collaboration.outlook3.impl
- **Method**: sendMail(String subject, String message, List<File> attachments, String to, String cc, String bcc, String
  replyTo, String bodyType)
- **Validation**: Email address validation and recipient management
- **Service**: Microsoft Graph Mail API send functionality

### Telemetry Metrics

- **SEND_MAIL_SUCCESS**: Successful email sending operations
- **SEND_MAIL_FAILURE**: Failed email sending operations
- **RECIPIENT_COUNT**: Number of recipients per email
- **ATTACHMENT_COUNT**: Number of attachments per email

## Troubleshooting

### Email Not Delivered

**Cause**: Recipient validation or delivery issues
**Solution**:

1. Verify all email addresses are correct
2. Check for typos in recipient addresses
3. Confirm recipients' email servers are accessible
4. Test with known good email addresses

### Attachment Issues

**Cause**: File size or format problems
**Solution**:

1. Check individual file sizes (max 25MB)
2. Verify total email size doesn't exceed limits
3. Ensure file formats are supported
4. Try sending without attachments first

### Formatting Problems

**Cause**: HTML content or encoding issues
**Solution**:

1. Validate HTML content is well-formed
2. Test with plain text first
3. Check for special characters or encoding issues
4. Use simple HTML formatting initially

### Permission Errors

**Cause**: Insufficient sending permissions
**Solution**:

1. Verify Mail.Send permission is granted
2. Check authentication token validity
3. Confirm user has send permissions
4. Test with [Test Connection](pages/TestConnection.md)

## See Also

- [Extension Configuration](pages/ExtensionConfiguration.md) - Setup and configuration
- [Authentication](pages/Authentication.md) - Authentication requirements
- [Send Mail With Table](pages/SendMailWithTable.md) - Advanced email with tables
- [Test Connection](pages/TestConnection.md) - Connection testing and validation
