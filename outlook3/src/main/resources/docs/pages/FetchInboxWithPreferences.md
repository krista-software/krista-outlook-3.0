# Fetch Inbox With Preferences

## Overview

Retrieves emails from the user's inbox with advanced filtering preferences including read/unread status, date ranges,
and sender filtering. This catalog request provides sophisticated inbox querying capabilities with optional pagination
support.

## Request Details

- **Area**: Messaging
- **Type**: QUERY_SYSTEM
- **Retry Support**: Yes - Failed requests can be retried automatically

## Input Parameters

| Parameter Name | Type    | Required | Description                                   | Example              |
|----------------|---------|----------|-----------------------------------------------|----------------------|
| Page Number    | Number  | No       | Page number for pagination (1-based indexing) | 1                    |
| Page Size      | Number  | No       | Number of emails per page                     | 10                   |
| Is Read        | Boolean | No       | Filter by read status (true/false)            | false                |
| From Date      | Date    | No       | Start date for date range filter              | "2023-01-01"         |
| To Date        | Date    | No       | End date for date range filter                | "2023-12-31"         |
| From Email     | Text    | No       | Filter by sender email address                | "sender@company.com" |

### Parameter Details

#### Page Number

- **Default**: 1 (first page)
- **Range**: Must be 1 or greater
- **Validation**: Cannot be less than 1
- **Behavior**: Returns the specified page of results from the filtered inbox

#### Page Size

- **Default**: 15 (if not specified)
- **Range**: 1 to 15 emails per page
- **Validation**: Must be between 1 and 15
- **Limitation**: Microsoft Graph API pagination limits

#### Is Read

- **Purpose**: Filter emails by read/unread status
- **Values**:
    - `true` - Only read emails
    - `false` - Only unread emails
    - Not specified - All emails (read and unread)
- **Use Case**: Focus on unread emails for processing

#### From Date / To Date

- **Format**: Date string (YYYY-MM-DD) or Date object
- **Purpose**: Filter emails by received date range
- **Behavior**:
    - From Date: Include emails received on or after this date
    - To Date: Include emails received on or before this date
- **Validation**: From Date must be before or equal to To Date

#### From Email

- **Format**: Email address string
- **Purpose**: Filter emails from specific sender
- **Validation**: Must be valid email address format
- **Case Sensitivity**: Case-insensitive matching

## Output Parameters

| Parameter Name | Type                       | Description                                   |
|----------------|----------------------------|-----------------------------------------------|
| Inbox Mails    | List<Entity(Mail Details)> | List of filtered email objects from the inbox |

### Mail Details Entity Structure

Each email in the response contains the following fields:

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

| Validation           | Error Message                                        | Resolution                     |
|----------------------|------------------------------------------------------|--------------------------------|
| Page number < 1      | "Incorrect page number value for fetching mails"     | Use page number 1 or greater   |
| Page size < 1        | "Incorrect page size value for fetching mails"       | Use page size between 1 and 15 |
| Page size > 15       | "Page size up to 15 messages is currently supported" | Reduce page size to 15 or less |
| Invalid date format  | "Invalid date format"                                | Use YYYY-MM-DD format          |
| From Date > To Date  | "From Date must be before or equal to To Date"       | Correct date range             |
| Invalid email format | "Invalid email address format"                       | Use valid email format         |

## Usage Examples

### Example 1: Fetch Unread Emails

**Scenario**: Get all unread emails from inbox

**Input**:

```
Page Number: 1
Page Size: 10
Is Read: false
```

**Output**: Returns first 10 unread emails from inbox

### Example 2: Fetch Emails from Specific Sender

**Scenario**: Get emails from specific sender in date range

**Input**:

```
Page Number: 1
Page Size: 15
From Email: "manager@company.com"
From Date: "2023-01-01"
To Date: "2023-01-31"
```

**Output**: Returns emails from manager@company.com received in January 2023

### Example 3: Fetch Recent Read Emails

**Scenario**: Get read emails from last week

**Input**:

```
Page Number: 1
Page Size: 10
Is Read: true
From Date: "2023-01-15"
To Date: "2023-01-22"
```

**Output**: Returns read emails from the specified week

### Example 4: Complex Filter Combination

**Scenario**: Get unread emails from specific sender in date range

**Input**:

```
Page Number: 1
Page Size: 5
Is Read: false
From Email: "client@external.com"
From Date: "2023-01-01"
To Date: "2023-01-31"
```

**Output**: Returns unread emails from specific client in January 2023

## Business Rules

1. **Filter Combination**: All specified filters are applied together (AND logic)
2. **Date Range**: Both From Date and To Date are inclusive
3. **Pagination Logic**: Page numbering starts from 1 (not 0)
4. **Default Behavior**: Without filters, returns all inbox emails
5. **Sorting**: Emails are returned in reverse chronological order (newest first)
6. **Access Control**: Only emails accessible to authenticated user are returned

## Limitations

1. **Page Size Limit**: Maximum 15 emails per request
2. **Date Range**: Limited by mailbox retention policies
3. **Filter Complexity**: Limited to basic AND logic between filters
4. **Real-time Updates**: Results are snapshot at request time
5. **Rate Limits**: Subject to Microsoft Graph API rate limiting
6. **Sender Matching**: Exact email address match only

## Best Practices

### 1. Filter Strategy

- Use specific filters to reduce result sets
- Combine filters for precise targeting
- Start with broader filters and refine as needed
- Consider mailbox size when setting date ranges

### 2. Performance Optimization

- Use smaller page sizes for better performance
- Apply filters to reduce data transfer
- Cache results when appropriate
- Monitor response times and adjust accordingly

### 3. Date Range Management

- Use reasonable date ranges to avoid large result sets
- Consider timezone implications for date filtering
- Account for email delivery delays in date ranges
- Use consistent date formats throughout application

### 4. Error Handling

- Implement retry logic for transient failures
- Handle empty result sets gracefully
- Validate filter parameters before submission
- Provide meaningful error messages to users

## Common Use Cases

### 1. Unread Email Processing

```
Scenario: Process only unread emails for automation
Action: Filter by Is Read = false
Result: Focus processing on emails requiring attention
```

### 2. Sender-Specific Processing

```
Scenario: Process emails from specific clients or systems
Action: Filter by From Email address
Result: Targeted processing of emails from known sources
```

### 3. Time-Based Email Analysis

```
Scenario: Analyze emails from specific time periods
Action: Use From Date and To Date filters
Result: Time-bounded email analysis and reporting
```

### 4. Priority Email Identification

```
Scenario: Identify urgent unread emails from VIP senders
Action: Combine Is Read = false with specific From Email
Result: High-priority email identification for immediate processing
```

## Related Catalog Requests

- [Fetch Inbox](pages/FetchInbox.md) - Basic inbox retrieval without filters
- [Fetch Mail Details By Query](pages/FetchMailDetailsByQuery.md) - Advanced search functionality
- [Fetch Latest Mail](pages/FetchLatestMail.md) - Get most recent email
- [Mark Message](pages/MarkMessage.md) - Mark emails as read/unread

## Technical Implementation

### Helper Class

- **Class**: MessagingAreaImpl
- **Package**: app.krista.extensions.essentials.collaboration.outlook3.impl
- **Method**: fetchInboxWithPreferences(Double pageNumber, Double pageSize, Boolean isRead, Date fromDate, Date toDate,
  String fromEmail)
- **Filtering**: Advanced filtering with Microsoft Graph API query parameters
- **Service**: Microsoft Graph Mail API with advanced filtering

### Telemetry Metrics

- **FETCH_INBOX_WITH_PREFERENCES_SUCCESS**: Successful filtered inbox retrievals
- **FETCH_INBOX_WITH_PREFERENCES_FAILURE**: Failed filtered inbox retrievals
- **FILTER_USAGE**: Usage statistics for different filter types
- **FILTERED_EMAIL_COUNT**: Number of emails returned after filtering

## Troubleshooting

### No Emails Returned

**Cause**: Filters too restrictive or no matching emails
**Solution**:

1. Verify filter parameters are correct
2. Try broader date ranges
3. Check if sender email address is exact match
4. Test without filters to confirm inbox access

### Date Filter Issues

**Cause**: Invalid date format or range
**Solution**:

1. Use YYYY-MM-DD date format
2. Ensure From Date is before or equal to To Date
3. Check timezone considerations
4. Verify dates are within mailbox retention period

### Sender Filter Not Working

**Cause**: Email address mismatch or format issues
**Solution**:

1. Verify exact email address spelling
2. Check for case sensitivity issues
3. Ensure sender email exists in inbox
4. Test with known sender addresses

### Performance Issues

**Cause**: Large result sets or complex filters
**Solution**:

1. Use more specific filters to reduce result size
2. Implement smaller page sizes
3. Add date range filters to limit scope
4. Monitor and optimize filter combinations

## See Also

- [Extension Configuration](pages/ExtensionConfiguration.md) - Setup and configuration
- [Authentication](pages/Authentication.md) - Authentication requirements
- [Fetch Inbox](pages/FetchInbox.md) - Basic inbox retrieval
- [Fetch Mail Details By Query](pages/FetchMailDetailsByQuery.md) - Advanced search functionality
