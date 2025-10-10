# Fetch Mails By Label

## Overview

Retrieves emails from a specific folder (label) in the user's mailbox with optional pagination support. This catalog request
provides access to emails organized in custom folders, allowing efficient retrieval and processing of categorized emails.

## Request Details

- **Area**: Messaging
- **Type**: QUERY_SYSTEM
- **Retry Support**: Yes - Failed requests can be retried automatically

## Input Parameters

| Parameter Name | Type    | Required | Description                                                    | Example    |
|----------------|---------|----------|----------------------------------------------------------------|------------|
| Label          | Text    | Yes      | Name of the folder/label to fetch emails from                  | "Archive"  |
| Page Number    | Number  | No       | Page number for pagination (1-based indexing)                  | 1          |
| Page Size      | Number  | No       | Number of emails per page                                      | 10         |
| Allow Retry    | Boolean | No       | Enable interactive retry on validation errors (default: false) | true       |

### Parameter Details

#### Label

- **Format**: Folder name as it appears in the mailbox
- **Examples**: "Inbox", "Sent Items", "Drafts", "Archive", "Projects"
- **Subfolders**: Use forward slash notation (e.g., "Projects/2023")
- **Case Sensitivity**: Must match exact folder name
- **Validation**: Folder must exist in the user's mailbox
- **Required**: Must be provided

#### Page Number

- **Default**: 1 (first page)
- **Range**: Must be 1 or greater
- **Validation**: Cannot be less than 1
- **Behavior**: Returns the specified page of results from the folder

#### Page Size

- **Default**: 15 (if not specified)
- **Range**: 1 to 15 emails per page
- **Validation**: Must be between 1 and 15
- **Limitation**: Microsoft Graph API pagination limits

#### Allow Retry

- **Values**:
    - `true` - Prompt user to retry on validation errors
    - `false` or `null` - Return immediate error without retry option (default)
- **Purpose**: Controls error handling behavior for validation failures
- **Use Case**: Set to `true` for interactive workflows where users can correct invalid label names or pagination parameters
- **Default Behavior**: When not specified or `false`, validation errors return immediately

## Output Parameters

| Parameter Name | Type                       | Description                                |
|----------------|----------------------------|--------------------------------------------|
| Mails          | List<Entity(Mail Details)> | List of email objects from the specified folder |

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

| Validation      | Error Message                                        | Resolution                     |
|-----------------|------------------------------------------------------|--------------------------------|
| Label is empty  | "Label cannot be empty"                              | Provide a valid folder name    |
| Label not found | "Folder does not exist"                              | Verify folder name exists      |
| Page number < 1 | "Incorrect page number value for fetching mails"     | Use page number 1 or greater   |
| Page size < 1   | "Incorrect page size value for fetching mails"       | Use page size between 1 and 15 |
| Page size > 15  | "Page size up to 15 messages is currently supported" | Reduce page size to 15 or less |

## Error Handling

### Input Errors (INPUT_ERROR)

**Cause**: Invalid or missing required parameters
**Common Scenarios**:

- Empty or null label name
- Invalid pagination parameters
- Page number less than 1
- Page size outside valid range (1-15)

**Resolution**: Provide valid label name and pagination parameters within allowed ranges

### Logic Errors (LOGIC_ERROR)

**Cause**: Business logic validation failures
**Common Scenarios**:

- Label/folder not found in mailbox
- User lacks permission to access the folder
- Folder has been deleted

**Resolution**: Verify folder exists and user has access permissions

### System Errors (SYSTEM_ERROR)

**Cause**: Microsoft Graph API or system-level failures
**Common Scenarios**:

- Network connectivity issues
- Microsoft Graph service unavailable
- Authentication token expired

**Resolution**: Retry the operation or check system connectivity

## Usage Examples

### Example 1: Fetch Emails from Archive Folder

**Scenario**: Retrieve archived emails with pagination

**Input**:

```
Label: "Archive"
Page Number: 1
Page Size: 10
Allow Retry: false
```

**Output**:

```
Mails: [
  {
    From: "sender@company.com",
    Subject: "Q3 Report",
    Message ID: "AQMkADY4ZTFiMGIxLWU1YjUtNDEwMS04Y2Q0...",
    Is Read: true,
    ...
  },
  ...
]
```

### Example 2: Fetch from Project Subfolder

**Scenario**: Retrieve emails from specific project folder

**Input**:

```
Label: "Projects/2023/Alpha Project"
Page Number: 1
Page Size: 15
Allow Retry: false
```

**Output**:

```
Mails: [List of emails from the project folder]
```

### Example 3: Interactive Retry on Invalid Label

**Scenario**: Allow user to correct invalid folder name

**Input**:

```
Label: "NonExistentFolder"
Page Number: 1
Page Size: 10
Allow Retry: true
```

**Behavior**:
- System validates label and detects folder doesn't exist
- User is prompted to re-enter correct folder name
- User provides valid folder name and emails are retrieved

### Example 4: Automated Processing Without Retry

**Scenario**: Automated workflow that processes folder emails

**Input**:

```
Label: "Processed"
Page Number: 1
Page Size: 15
Allow Retry: false
```

**Behavior**:
- If validation fails, error is returned immediately
- Calling application handles error programmatically
- No user interaction required

## Business Rules

1. **Folder Existence**: Folder must exist in the user's mailbox
2. **Access Permissions**: User must have read access to the folder
3. **Pagination**: Results are paginated with configurable page size
4. **Folder Hierarchy**: Supports nested folders with slash notation
5. **Case Sensitivity**: Folder names are case-sensitive
6. **Empty Folders**: Returns empty list if folder contains no emails
7. **Error Handling Control**: Allow Retry parameter controls whether validation errors trigger interactive retry prompts or immediate error returns

## Limitations

1. **Folder Existence**: Folder must already exist (operation doesn't create folders)
2. **Read Permissions**: User must have permission to read from the folder
3. **Page Size Limit**: Maximum 15 emails per page
4. **Folder Access**: User must have access to the specified folder
5. **Rate Limits**: Subject to Microsoft Graph API rate limiting

## Best Practices

### 1. Folder Management

- Verify folder names are spelled correctly and match exactly
- Use consistent folder naming conventions
- Consider folder hierarchy when organizing emails
- Handle cases where folders may be deleted

### 2. Pagination Strategy

- Use appropriate page sizes based on processing needs
- Implement proper pagination logic for large folders
- Handle empty result sets gracefully
- Consider performance implications of large page sizes

### 3. Error Handling

- Implement retry logic for transient failures
- Validate folder existence before fetching emails
- Handle cases where folders are deleted during processing
- Provide meaningful error messages to users

### 4. Workflow Integration

- Fetch emails as part of larger processing workflows
- Consider filtering and sorting requirements
- Implement proper logging for fetch operations
- Coordinate with other email management operations

## Common Use Cases

### 1. Archive Processing

```
Scenario: Process archived emails for compliance
Action: Fetch emails from Archive folder, process each
Result: Archived emails are systematically processed
```

### 2. Project Email Management

```
Scenario: Retrieve project-related emails
Action: Fetch emails from project-specific folders
Result: Project emails are organized and accessible
```

### 3. Folder Monitoring

```
Scenario: Monitor specific folder for new emails
Action: Periodically fetch emails from monitored folder
Result: New emails in folder are detected and processed
```

### 4. Batch Processing

```
Scenario: Process emails in specific folder in batches
Action: Fetch emails with pagination, process each batch
Result: Efficient batch processing of folder emails
```

## Related Catalog Requests

- [Fetch All Labels](pages/FetchAllLabels.md) - Get list of available folders
- [Fetch Inbox](pages/FetchInbox.md) - Retrieve emails from inbox
- [Move Message](pages/MoveMessage.md) - Move emails to different folders
- [Fetch Mail By Message Id](pages/FetchMailByMessageId.md) - Retrieve specific email

## Technical Implementation

### Helper Class

- **Class**: MessagingAreaImpl
- **Package**: app.krista.extensions.essentials.collaboration.outlook3.impl
- **Method**: fetchMailByLabel(String label, Double pageNumber, Double pageSize, Boolean allowRetry)
- **Validation**: Label validation and pagination parameter checking
- **Service**: Microsoft Graph Mail API folder query functionality

### Telemetry Metrics

- **FETCH_MAILS_BY_LABEL_SUCCESS**: Successful folder email retrieval operations
- **FETCH_MAILS_BY_LABEL_FAILURE**: Failed folder email retrieval operations
- **LABEL_NOT_FOUND**: Folder not found errors
- **INVALID_PAGINATION**: Invalid pagination parameter errors

## Troubleshooting

### Folder Not Found

**Cause**: Invalid or non-existent folder name
**Solution**:

1. Verify folder name spelling and case sensitivity
2. Check folder exists in user's mailbox
3. Use Fetch All Labels to get available folders
4. Test with known existing folders

### Permission Denied

**Cause**: User lacks read permissions
**Solution**:

1. Verify Mail.Read permission is granted
2. Check if user has access to the folder
3. Confirm authentication token is valid
4. Test with accessible folders

### Invalid Pagination

**Cause**: Pagination parameters outside valid range
**Solution**:

1. Ensure page number is 1 or greater
2. Verify page size is between 1 and 15
3. Use default values when appropriate
4. Test with valid pagination parameters

### Empty Results

**Cause**: Folder contains no emails or pagination beyond available pages
**Solution**:

1. Verify folder contains emails
2. Check pagination parameters are within range
3. Confirm folder hasn't been emptied
4. Test with folders known to contain emails

## See Also

- [Extension Configuration](pages/ExtensionConfiguration.md) - Setup and configuration
- [Authentication](pages/Authentication.md) - Authentication requirements
- [Fetch All Labels](pages/FetchAllLabels.md) - Get available folders
- [Fetch Inbox](pages/FetchInbox.md) - Retrieve inbox emails

