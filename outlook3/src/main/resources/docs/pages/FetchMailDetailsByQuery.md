# Fetch Mail Details By Query

## Overview

Searches for emails using advanced query syntax and returns detailed email information. This catalog request allows you to find specific emails based on content, sender, subject, or other criteria with comprehensive search capabilities.

## Request Details

- **Area**: Messaging
- **Type**: QUERY_SYSTEM
- **Retry Support**: ✅ Yes - Failed requests can be retried automatically

## Input Parameters

| Parameter Name | Type | Required | Description | Example |
|----------------|------|----------|-------------|---------|
| Query | Text | Yes | Search query using Outlook search syntax | "from:john@company.com subject:project" |

### Parameter Details

#### Query
- **Format**: Outlook search query syntax
- **Examples**: 
  - `"from:sender@company.com"` - Emails from specific sender
  - `"subject:meeting"` - Emails with "meeting" in subject
  - `"hasattachments:yes"` - Emails with attachments
  - `"received:today"` - Emails received today
- **Exact Match**: Enclose search terms in double quotes for exact matches
- **Case Sensitivity**: Generally case-insensitive
- **Operators**: Supports AND, OR, NOT operators

## Output Parameters

| Parameter Name | Type | Description |
|----------------|------|-------------|
| Mails | List<Entity(Mail Details)> | List of email objects matching the search query |

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
| Query is empty | "Query cannot be empty" | Provide a valid search query |
| Query is null | "Search query is required" | Ensure query parameter is not null |
| Invalid query syntax | "Invalid search query format" | Use proper Outlook search syntax |

## Query Syntax Examples

### Basic Search Queries

#### Search by Sender
```
Query: "from:john.doe@company.com"
Result: All emails from john.doe@company.com
```

#### Search by Subject
```
Query: "subject:project update"
Result: Emails with "project update" in subject line
```

#### Search by Content
```
Query: "body:quarterly report"
Result: Emails containing "quarterly report" in message body
```

### Advanced Search Queries

#### Multiple Criteria
```
Query: "from:manager@company.com AND subject:meeting"
Result: Emails from manager about meetings
```

#### Date Range Search
```
Query: "received:last week"
Result: Emails received in the last week
```

#### Attachment Search
```
Query: "hasattachments:yes AND from:client@external.com"
Result: Emails with attachments from external client
```

### Exact Match Queries

#### Exact Subject Match
```
Query: "subject:\"Daily Briefing\""
Result: Emails with exact subject "Daily Briefing"
```

#### Exact Phrase Search
```
Query: "\"project alpha completion\""
Result: Emails containing the exact phrase "project alpha completion"
```

## Supported Query Operators

| Operator | Description | Example |
|----------|-------------|---------|
| from: | Search by sender | `from:user@company.com` |
| to: | Search by recipient | `to:team@company.com` |
| subject: | Search in subject line | `subject:meeting` |
| body: | Search in message body | `body:report` |
| hasattachments: | Filter by attachment presence | `hasattachments:yes` |
| received: | Filter by received date | `received:today` |
| sent: | Filter by sent date | `sent:yesterday` |
| category: | Filter by category | `category:important` |
| AND | Combine conditions (all must match) | `from:user AND subject:project` |
| OR | Alternative conditions (any can match) | `subject:meeting OR subject:call` |
| NOT | Exclude conditions | `NOT from:spam@domain.com` |

## Usage Examples

### Example 1: Find Project Emails
**Scenario**: Search for all project-related emails from team lead

**Input**:
```
Query: "from:teamlead@company.com AND subject:project"
```

**Output**:
```json
{
  "Mails": [
    {
      "From": "teamlead@company.com",
      "To": "team@company.com",
      "Subject": "Project Alpha Status Update",
      "Message": "Here's the latest update on Project Alpha...",
      "Message ID": "AQMkADY4ZTFiMGIxLWU1YjUtNDEwMS04Y2Q0...",
      "Is Read": false,
      "Send Date and Time": 1640995200000
    }
  ]
}
```

### Example 2: Find Recent Emails with Attachments
**Scenario**: Search for emails with attachments received today

**Input**:
```
Query: "hasattachments:yes AND received:today"
```

**Output**: Returns emails received today that have attachments

### Example 3: Find Customer Communications
**Scenario**: Search for emails from external customers

**Input**:
```
Query: "from:@external.com OR from:@customer.com"
```

**Output**: Returns emails from external domains

## Business Rules

1. **Search Scope**: Searches across all accessible folders in the mailbox
2. **Real-time Results**: Results reflect current mailbox state at query time
3. **Access Control**: Only emails accessible to authenticated user are returned
4. **Query Processing**: Complex queries may take longer to process
5. **Relevance Ranking**: Results are typically ranked by relevance
6. **Result Limits**: Subject to Microsoft Graph API result limitations

## Limitations

1. **Query Complexity**: Very complex queries may fail or timeout
2. **Search Scope**: Limited to user's accessible mailbox content
3. **Real-time Updates**: Results are snapshot at query time
4. **Rate Limits**: Subject to Microsoft Graph API rate limiting
5. **Syntax Support**: Limited to supported Outlook search operators
6. **Result Size**: Large result sets may be truncated

## Best Practices

### 1. Query Construction
- Use specific criteria to narrow search results
- Combine multiple operators for precise searches
- Use exact match quotes for specific phrases
- Test queries with simple criteria first

### 2. Performance Optimization
- Keep queries as specific as possible
- Avoid overly broad search terms
- Use date ranges to limit search scope
- Monitor query response times

### 3. Result Processing
- Handle empty result sets gracefully
- Process results in order of relevance
- Store message IDs for further operations
- Implement proper error handling

### 4. Search Strategy
- Start with broad queries and refine as needed
- Use multiple targeted queries instead of one complex query
- Implement search result caching when appropriate
- Provide search suggestions to users

## Common Use Cases

### 1. Email Discovery
```
Scenario: Find all emails related to specific project
Action: Search using project name and key stakeholders
Result: Comprehensive list of project-related communications
```

### 2. Compliance Search
```
Scenario: Find emails containing sensitive information
Action: Search using specific keywords and date ranges
Result: Emails requiring compliance review
```

### 3. Customer Communication Review
```
Scenario: Find all communications with specific customer
Action: Search using customer domain and contact names
Result: Complete customer communication history
```

## Related Catalog Requests

- [Fetch Inbox](FetchInbox.md) - Retrieve emails from inbox
- [Fetch Inbox With Preferences](FetchInboxWithPreferences.md) - Advanced inbox filtering
- [Fetch Sent](FetchSent.md) - Get sent emails
- [Fetch Latest Mail](FetchLatestMail.md) - Get most recent emails

## Technical Implementation

### Helper Class
- **Class**: MessagingAreaImpl
- **Package**: app.krista.extensions.essentials.collaboration.outlook3.impl
- **Method**: fetchMailDetailsByQuery(String query)
- **Service**: Microsoft Graph Mail API search functionality

### Telemetry Metrics
- **SEARCH_QUERY_SUCCESS**: Successful search operations
- **SEARCH_QUERY_FAILURE**: Failed search operations
- **SEARCH_RESULTS_COUNT**: Number of results returned
- **SEARCH_QUERY_TIME**: Time taken to process search

## Troubleshooting

### No Results Found
**Cause**: Query doesn't match any emails or too restrictive
**Solution**:
1. Simplify search query
2. Check query syntax is correct
3. Verify emails matching criteria exist
4. Try broader search terms

### Invalid Query Syntax
**Cause**: Unsupported or malformed query syntax
**Solution**:
1. Review supported query operators
2. Check for proper quote usage
3. Simplify complex queries
4. Test with basic query syntax first

### Search Performance Issues
**Cause**: Complex queries or large mailbox
**Solution**:
1. Use more specific search criteria
2. Add date range limitations
3. Break complex queries into simpler ones
4. Monitor and optimize query performance

### Permission Errors
**Cause**: Insufficient search permissions
**Solution**:
1. Verify Mail.ReadWrite permission is granted
2. Check authentication token validity
3. Confirm user has mailbox search access
4. Test with simpler search queries

## See Also

- [Extension Configuration](ExtensionConfiguration.md) - Setup and configuration
- [Authentication](Authentication.md) - Authentication requirements
- [Fetch Inbox With Preferences](FetchInboxWithPreferences.md) - Advanced filtering
- [Fetch Inbox](FetchInbox.md) - Browse inbox emails
