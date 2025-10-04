# Fetch Inbox

## Overview

Retrieves emails from the user's inbox with optional pagination support. This catalog request provides comprehensive access to inbox emails with configurable page sizes and page numbers for efficient email processing and management.

## Request Details

- **Area**: Messaging
- **Type**: QUERY_SYSTEM
- **Retry Support**: ✅ Yes - Failed requests can be retried automatically

## Input Parameters

| Parameter Name | Type | Required | Description | Example |
|----------------|------|----------|-------------|---------|
| Page Number | Number | No | Page number for pagination (1-based indexing) | 1 |
| Page Size | Number | No | Number of emails per page | 10 |

### Parameter Details

#### Page Number
- **Default**: 1 (first page)
- **Range**: Must be 1 or greater
- **Validation**: Cannot be less than 1
- **Behavior**: Returns the specified page of results from the inbox

#### Page Size
- **Default**: 15 (if not specified)
- **Range**: 1 to 15 emails per page
- **Validation**: Must be between 1 and 15
- **Limitation**: Microsoft Graph API pagination limits

## Output Parameters

| Parameter Name | Type | Description |
|----------------|------|-------------|
| Inbox Mails | List<Entity(Mail Details)> | List of email objects from the inbox |

### Mail Details Entity Structure

Each email in the response contains the following fields:

| Field Name | Type | Description |
|------------|------|-------------|
| From | Text | Sender's email address |
| To | Text | Primary recipients (comma-separated) |
| Subject | Text | Email subject line |
| Message | Rich Text | Email body content |
| Message ID | Text | Unique identifier for the email |
| Cc | Text | Carbon copy recipients (comma-separated) |
| Bcc | Text | Blind carbon copy recipients (comma-separated) |
| Is Read | Boolean | Whether the email has been read |
| Reply To | Text | Reply-to email address |
| Send Date and Time | Date | When the email was sent |
| Received Date and Time | Date | When the email was received |
| File Attachment | List<File> | Attached files |
| Item Attachment | List<Text> | Embedded item attachments |
| Categories | List<Text> | Email categories/labels |

## Validation Rules

| Validation | Error Message | Resolution |
|------------|---------------|------------|
| Page number < 1 | "Incorrect page number value for fetching mails" | Use page number 1 or greater |
| Page size < 1 | "Incorrect page size value for fetching mails" | Use page size between 1 and 15 |
| Page size > 15 | "Page size up to 15 messages is currently supported" | Reduce page size to 15 or less |

## Error Handling

### Input Errors (INPUT_ERROR)
**Cause**: Invalid pagination parameters
**Common Scenarios**:
- Page number less than 1
- Page size outside valid range (1-15)
- Non-numeric values for pagination parameters

**Resolution**: Provide valid pagination parameters within allowed ranges

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
- User lacks inbox access

**Resolution**: Verify permissions and re-authenticate if necessary

## Usage Examples

### Example 1: Fetch First Page of Inbox
**Scenario**: Get the first 10 emails from inbox

**Input**:
```
Page Number: 1
Page Size: 10
```

**Output**:
```json
{
  "Inbox Mails": [
    {
      "From": "sender@company.com",
      "To": "user@company.com",
      "Subject": "Project Update",
      "Message": "Here's the latest update on the project...",
      "Message ID": "AQMkADY4ZTFiMGIxLWU1YjUtNDEwMS04Y2Q0...",
      "Is Read": false,
      "Send Date and Time": 1640995200000,
      "File Attachment": ["report.pdf"]
    }
  ]
}
```

### Example 2: Fetch Specific Page
**Scenario**: Get emails 16-30 from inbox (page 2 with 15 emails per page)

**Input**:
```
Page Number: 2
Page Size: 15
```

**Output**: Returns emails 16-30 from the inbox

### Example 3: Default Pagination
**Scenario**: Get inbox emails with default settings

**Input**: (No parameters provided)

**Output**: Returns first 15 emails from inbox (default behavior)

## Business Rules

1. **Pagination Logic**: Page numbering starts from 1 (not 0)
2. **Default Behavior**: Without pagination parameters, returns first 15 emails
3. **Sorting**: Emails are returned in reverse chronological order (newest first)
4. **Access Control**: Only emails accessible to authenticated user are returned
5. **Real-time Data**: Results reflect current inbox state at request time
6. **Thread Context**: Individual emails are returned, not grouped by conversation

## Limitations

1. **Page Size Limit**: Maximum 15 emails per request
2. **Access Permissions**: User must have read access to inbox
3. **Real-time Updates**: Results are snapshot at request time, not real-time
4. **Attachment Size**: Large attachments may impact response time
5. **Rate Limits**: Subject to Microsoft Graph API rate limiting
6. **Folder Scope**: Limited to inbox folder only

## Best Practices

### 1. Pagination Strategy
- Start with smaller page sizes for better performance
- Use consistent page sizes throughout your application
- Implement proper error handling for pagination edge cases
- Consider total inbox size when planning pagination

### 2. Performance Optimization
- Cache inbox contents when appropriate to reduce API calls
- Implement incremental loading for large inboxes
- Monitor response times and adjust page sizes accordingly
- Use appropriate page sizes based on your use case

### 3. Data Processing
- Process emails in batches for better performance
- Store message IDs for further operations
- Handle empty result sets gracefully
- Implement proper error handling for failed requests

### 4. Error Handling
- Implement retry logic for transient failures
- Handle cases where inbox may be empty
- Provide meaningful error messages to users
- Log failed requests for troubleshooting

## Common Use Cases

### 1. Email Processing Workflows
```
Scenario: Process all emails in inbox for automation
Action: Fetch emails in batches and process each one
Result: Systematic processing of inbox emails
```

### 2. Email Monitoring
```
Scenario: Monitor inbox for new emails periodically
Action: Fetch first page of inbox regularly
Result: Detection of new emails for immediate processing
```

### 3. Email Analysis
```
Scenario: Analyze inbox emails for patterns or content
Action: Fetch all emails using pagination
Result: Comprehensive analysis of email data
```

## Related Catalog Requests

- [Fetch Inbox With Preferences](FetchInboxWithPreferences.md) - Advanced inbox filtering
- [Fetch Mail By Message Id](FetchMailByMessageId.md) - Get specific email details
- [Fetch Latest Mail](FetchLatestMail.md) - Get most recent email
- [Mark Message](MarkMessage.md) - Mark emails as read/unread

## Technical Implementation

### Helper Class
- **Class**: MessagingAreaImpl
- **Package**: app.krista.extensions.essentials.collaboration.outlook3.impl
- **Method**: fetchInbox(Double pageNumber, Double pageSize)
- **Service**: Microsoft Graph Mail API with inbox folder access

### Telemetry Metrics
- **FETCH_INBOX_SUCCESS**: Successful inbox retrievals
- **FETCH_INBOX_FAILURE**: Failed inbox retrievals
- **INBOX_EMAIL_COUNT**: Number of emails returned
- **INBOX_PAGE_SIZE**: Page size used for requests

## Troubleshooting

### No Emails Returned
**Cause**: Empty inbox or pagination beyond available emails
**Solution**:
1. Check if inbox actually contains emails
2. Verify page number is not beyond available pages
3. Try with page number 1 to confirm inbox access
4. Check user permissions for inbox access

### Pagination Issues
**Cause**: Invalid pagination parameters
**Solution**:
1. Ensure page number is 1 or greater
2. Verify page size is between 1 and 15
3. Use default parameters first to test basic functionality
4. Check for non-numeric parameter values

### Performance Issues
**Cause**: Large inbox or slow network
**Solution**:
1. Reduce page size for better performance
2. Implement caching for frequently accessed emails
3. Use asynchronous operations for large inboxes
4. Monitor and optimize based on usage patterns

### Permission Errors
**Cause**: Insufficient inbox access permissions
**Solution**:
1. Verify Mail.ReadWrite permission is granted
2. Check authentication token validity
3. Confirm user has inbox access
4. Test with [Test Connection](TestConnection.md)

## See Also

- [Extension Configuration](ExtensionConfiguration.md) - Setup and configuration
- [Authentication](Authentication.md) - Authentication requirements
- [Fetch Inbox With Preferences](FetchInboxWithPreferences.md) - Advanced inbox filtering
- [Test Connection](TestConnection.md) - Connection testing and validation
