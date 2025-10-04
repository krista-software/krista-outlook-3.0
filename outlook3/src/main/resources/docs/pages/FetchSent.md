# Fetch Sent

## Overview

Retrieves emails from the user's sent items folder with optional pagination support. This catalog request provides access to all emails sent by the authenticated user, enabling tracking, analysis, and follow-up on sent communications.

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
- **Behavior**: Returns the specified page of results from the sent items folder

#### Page Size
- **Default**: 15 (if not specified)
- **Range**: 1 to 15 emails per page
- **Validation**: Must be between 1 and 15
- **Limitation**: Microsoft Graph API pagination limits

## Output Parameters

| Parameter Name | Type | Description |
|----------------|------|-------------|
| Sent Mails | List<Entity(Mail Details)> | List of email objects from the sent items folder |

### Mail Details Entity Structure

Each email in the response contains the following fields:

| Field Name | Type | Description |
|------------|------|-------------|
| From | Text | Sender's email address (authenticated user) |
| To | Text | Primary recipients (comma-separated) |
| Subject | Text | Email subject line |
| Message | Rich Text | Email body content |
| Message ID | Text | Unique identifier for the email |
| Cc | Text | Carbon copy recipients (comma-separated) |
| Bcc | Text | Blind carbon copy recipients (comma-separated) |
| Is Read | Boolean | Whether the email has been read (typically true for sent emails) |
| Reply To | Text | Reply-to email address |
| Send Date and Time | Date | When the email was sent |
| Received Date and Time | Date | When the email was received (same as sent for sent items) |
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
- User lacks sent items access

**Resolution**: Verify permissions and re-authenticate if necessary

## Usage Examples

### Example 1: Fetch First Page of Sent Emails
**Scenario**: Get the first 10 sent emails

**Input**:
```
Page Number: 1
Page Size: 10
```

**Output**:
```json
{
  "Sent Mails": [
    {
      "From": "user@company.com",
      "To": "client@external.com, partner@company.com",
      "Subject": "Project Proposal",
      "Message": "Please find attached our project proposal...",
      "Message ID": "AQMkADY4ZTFiMGIxLWU1YjUtNDEwMS04Y2Q0...",
      "Is Read": true,
      "Send Date and Time": 1640995200000,
      "File Attachment": ["proposal.pdf"]
    }
  ]
}
```

### Example 2: Fetch Specific Page
**Scenario**: Get sent emails 16-30 (page 2 with 15 emails per page)

**Input**:
```
Page Number: 2
Page Size: 15
```

**Output**: Returns sent emails 16-30 from the sent items folder

### Example 3: Default Pagination
**Scenario**: Get sent emails with default settings

**Input**: (No parameters provided)

**Output**: Returns first 15 sent emails (default behavior)

## Business Rules

1. **Pagination Logic**: Page numbering starts from 1 (not 0)
2. **Default Behavior**: Without pagination parameters, returns first 15 sent emails
3. **Sorting**: Emails are returned in reverse chronological order (newest first)
4. **Access Control**: Only emails sent by authenticated user are returned
5. **Real-time Data**: Results reflect current sent items state at request time
6. **Folder Scope**: Limited to sent items folder only

## Limitations

1. **Page Size Limit**: Maximum 15 emails per request
2. **Access Permissions**: User must have read access to sent items folder
3. **Real-time Updates**: Results are snapshot at request time, not real-time
4. **Attachment Size**: Large attachments may impact response time
5. **Rate Limits**: Subject to Microsoft Graph API rate limiting
6. **User Scope**: Only emails sent by the authenticated user

## Best Practices

### 1. Pagination Strategy
- Start with smaller page sizes for better performance
- Use consistent page sizes throughout your application
- Implement proper error handling for pagination edge cases
- Consider total sent items count when planning pagination

### 2. Performance Optimization
- Cache sent items when appropriate to reduce API calls
- Implement incremental loading for large sent items folders
- Monitor response times and adjust page sizes accordingly
- Use appropriate page sizes based on your use case

### 3. Data Processing
- Process sent emails in batches for better performance
- Store message IDs for further operations
- Handle empty result sets gracefully
- Implement proper error handling for failed requests

### 4. Error Handling
- Implement retry logic for transient failures
- Handle cases where sent items folder may be empty
- Provide meaningful error messages to users
- Log failed requests for troubleshooting

## Common Use Cases

### 1. Sent Email Tracking
```
Scenario: Track emails sent to specific clients or projects
Action: Fetch sent emails and analyze recipients and content
Result: Comprehensive tracking of outbound communications
```

### 2. Communication Audit
```
Scenario: Audit sent communications for compliance
Action: Retrieve all sent emails for review and analysis
Result: Complete audit trail of sent communications
```

### 3. Follow-up Management
```
Scenario: Identify sent emails that may need follow-up
Action: Fetch recent sent emails and check for responses
Result: Proactive follow-up on important communications
```

### 4. Email Analytics
```
Scenario: Analyze sent email patterns and volumes
Action: Retrieve sent emails over time periods for analysis
Result: Insights into communication patterns and effectiveness
```

## Related Catalog Requests

- [Fetch Inbox](FetchInbox.md) - Retrieve received emails
- [Fetch Inbox With Preferences](FetchInboxWithPreferences.md) - Advanced inbox filtering
- [Fetch Mail By Message Id](FetchMailByMessageId.md) - Get specific email details
- [Send Mail](SendMail.md) - Send new emails

## Technical Implementation

### Helper Class
- **Class**: MessagingAreaImpl
- **Package**: app.krista.extensions.essentials.collaboration.outlook3.impl
- **Method**: fetchSent(Double pageNumber, Double pageSize)
- **Service**: Microsoft Graph Mail API with sent items folder access

### Telemetry Metrics
- **FETCH_SENT_SUCCESS**: Successful sent items retrievals
- **FETCH_SENT_FAILURE**: Failed sent items retrievals
- **SENT_EMAIL_COUNT**: Number of sent emails returned
- **SENT_PAGE_SIZE**: Page size used for requests

## Troubleshooting

### No Sent Emails Returned
**Cause**: Empty sent items folder or pagination beyond available emails
**Solution**:
1. Check if sent items folder actually contains emails
2. Verify page number is not beyond available pages
3. Try with page number 1 to confirm sent items access
4. Check user permissions for sent items folder access

### Pagination Issues
**Cause**: Invalid pagination parameters
**Solution**:
1. Ensure page number is 1 or greater
2. Verify page size is between 1 and 15
3. Use default parameters first to test basic functionality
4. Check for non-numeric parameter values

### Performance Issues
**Cause**: Large sent items folder or slow network
**Solution**:
1. Reduce page size for better performance
2. Implement caching for frequently accessed sent emails
3. Use asynchronous operations for large sent items folders
4. Monitor and optimize based on usage patterns

### Permission Errors
**Cause**: Insufficient sent items access permissions
**Solution**:
1. Verify Mail.ReadWrite permission is granted
2. Check authentication token validity
3. Confirm user has sent items access
4. Test with [Test Connection](TestConnection.md)

## See Also

- [Extension Configuration](ExtensionConfiguration.md) - Setup and configuration
- [Authentication](Authentication.md) - Authentication requirements
- [Fetch Inbox](FetchInbox.md) - Retrieve received emails
- [Send Mail](SendMail.md) - Send new emails
