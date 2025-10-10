# Fetch All Labels

## Overview

Retrieves a list of all folder names (labels) available in the user's mailbox. This catalog request provides access to the
complete folder structure, enabling dynamic folder selection and email organization workflows.

## Request Details

- **Area**: Messaging
- **Type**: QUERY_SYSTEM
- **Retry Support**: Yes - Failed requests can be retried automatically

## Input Parameters

| Parameter Name | Type    | Required | Description                                                    | Example |
|----------------|---------|----------|----------------------------------------------------------------|---------|
| Allow Retry    | Boolean | No       | Enable interactive retry on validation errors (default: false) | true    |

### Parameter Details

#### Allow Retry

- **Values**:
    - `true` - Prompt user to retry on validation errors
    - `false` or `null` - Return immediate error without retry option (default)
- **Purpose**: Controls error handling behavior for validation failures
- **Use Case**: Parameter added for API consistency (no validation logic in this method)
- **Default Behavior**: When not specified or `false`, validation errors return immediately
- **Note**: This method has no validation logic, so the parameter has no practical effect

## Output Parameters

| Parameter Name | Type        | Description                                      |
|----------------|-------------|--------------------------------------------------|
| Labels         | List<Text>  | List of all folder names in the user's mailbox   |

**Example Output**:

```
Labels: [
  "Inbox",
  "Sent Items",
  "Drafts",
  "Deleted Items",
  "Archive",
  "Projects",
  "Projects/2023",
  "Projects/2023/Alpha",
  "Customer Service",
  "Important"
]
```

## Validation Rules

This method has no input validation rules as it requires no parameters.

## Error Handling

### Authorization Errors

**Cause**: Authentication or permission issues
**Common Scenarios**:

- Authentication token expired
- User not authorized
- Insufficient permissions

**Resolution**: Verify authentication and permissions

### System Errors (SYSTEM_ERROR)

**Cause**: Microsoft Graph API or system-level failures
**Common Scenarios**:

- Network connectivity issues
- Microsoft Graph service unavailable
- Mailbox access issues

**Resolution**: Retry the operation or check system connectivity

## Usage Examples

### Example 1: Fetch All Folders

**Scenario**: Retrieve complete list of available folders

**Input**:

```
Allow Retry: false
```

**Output**:

```
Labels: [
  "Inbox",
  "Sent Items",
  "Drafts",
  "Deleted Items",
  "Archive",
  "Projects",
  "Projects/2023",
  "Customer Service"
]
```

### Example 2: Dynamic Folder Selection

**Scenario**: Populate dropdown with available folders for user selection

**Input**:

```
Allow Retry: false
```

**Output**:

```
Labels: ["Inbox", "Archive", "Projects", "Important"]
```

**Usage**:
- Display labels in UI dropdown
- User selects folder for email operations
- Selected folder used in subsequent catalog requests

### Example 3: Folder Validation

**Scenario**: Validate folder exists before moving emails

**Input**:

```
Allow Retry: false
```

**Process**:
1. Fetch all labels
2. Check if target folder exists in the list
3. If exists, proceed with move operation
4. If not exists, prompt user to create folder or select different one

### Example 4: Folder Structure Analysis

**Scenario**: Analyze mailbox folder organization

**Input**:

```
Allow Retry: false
```

**Output**:

```
Labels: [
  "Inbox",
  "Projects",
  "Projects/2023",
  "Projects/2023/Alpha",
  "Projects/2023/Beta",
  "Projects/2024"
]
```

**Analysis**:
- Identify folder hierarchy
- Detect nested folder structures
- Plan folder organization strategies

## Business Rules

1. **Complete List**: Returns all folders accessible to the user
2. **Hierarchy Included**: Nested folders shown with slash notation
3. **System Folders**: Includes both system and custom folders
4. **Read Permissions**: User must have read access to mailbox
5. **Real-time Data**: Returns current folder structure
6. **No Filtering**: All folders returned without filtering
7. **Error Handling Control**: Allow Retry parameter added for API consistency (no validation logic)

## Limitations

1. **Read Permissions**: User must have permission to read mailbox structure
2. **No Filtering**: Cannot filter or search folders (returns all)
3. **No Pagination**: All folders returned in single response
4. **Folder Count**: Large number of folders may impact response time
5. **Rate Limits**: Subject to Microsoft Graph API rate limiting

## Best Practices

### 1. Folder Management

- Cache folder list when appropriate to reduce API calls
- Refresh folder list periodically to detect changes
- Handle folder hierarchy appropriately in UI
- Consider folder naming conventions

### 2. Performance Optimization

- Cache results for short periods to reduce API calls
- Use folder list for validation before operations
- Consider folder count impact on performance
- Monitor API rate limits

### 3. Error Handling

- Implement retry logic for transient failures
- Handle cases where mailbox is inaccessible
- Provide meaningful error messages to users
- Log folder retrieval operations

### 4. Workflow Integration

- Use folder list for dynamic UI population
- Validate folder existence before operations
- Implement folder selection workflows
- Coordinate with other email management operations

## Common Use Cases

### 1. Folder Selection UI

```
Scenario: Populate folder dropdown for user selection
Action: Fetch all labels and display in dropdown
Result: User can select from available folders
```

### 2. Folder Validation

```
Scenario: Validate folder exists before moving emails
Action: Fetch all labels, check if target folder exists
Result: Prevent errors from invalid folder names
```

### 3. Folder Organization

```
Scenario: Analyze and organize mailbox folder structure
Action: Fetch all labels, analyze hierarchy
Result: Understand and optimize folder organization
```

### 4. Automated Folder Management

```
Scenario: Automated email routing based on available folders
Action: Fetch all labels, route emails to appropriate folders
Result: Emails automatically organized into existing folders
```

### 5. Folder Monitoring

```
Scenario: Monitor for new folders created by users
Action: Periodically fetch all labels, detect changes
Result: System aware of new folders for processing
```

## Related Catalog Requests

- [Fetch Mails By Label](pages/FetchMailsByLabel.md) - Retrieve emails from specific folder
- [Move Message](pages/MoveMessage.md) - Move emails to folders
- [Fetch Inbox](pages/FetchInbox.md) - Retrieve inbox emails
- [Fetch Mail By Message Id](pages/FetchMailByMessageId.md) - Retrieve specific email

## Technical Implementation

### Helper Class

- **Class**: MessagingAreaImpl
- **Package**: app.krista.extensions.essentials.collaboration.outlook3.impl
- **Method**: fetchAllLabels(Boolean allowRetry)
- **Validation**: No validation required (parameter added for API consistency)
- **Service**: Microsoft Graph Mail API folder enumeration functionality

### Telemetry Metrics

- **FETCH_ALL_LABELS_SUCCESS**: Successful folder list retrieval operations
- **FETCH_ALL_LABELS_FAILURE**: Failed folder list retrieval operations
- **LABEL_COUNT**: Number of folders returned
- **AUTHORIZATION_ERROR**: Authentication/authorization failures

## Troubleshooting

### Permission Denied

**Cause**: User lacks read permissions
**Solution**:

1. Verify Mail.Read permission is granted
2. Check if user has access to the mailbox
3. Confirm authentication token is valid
4. Test with accessible mailboxes

### Empty Folder List

**Cause**: Mailbox has no folders or access issues
**Solution**:

1. Verify mailbox is properly configured
2. Check if user has any folders
3. Confirm mailbox is accessible
4. Test with known populated mailboxes

### Operation Timeout

**Cause**: Large number of folders or slow network
**Solution**:

1. Check network connectivity
2. Consider folder count impact
3. Retry operation after brief delay
4. Monitor API response times

### Authentication Errors

**Cause**: Token expired or invalid
**Solution**:

1. Refresh authentication token
2. Re-authenticate user
3. Verify OAuth configuration
4. Check token expiration

## See Also

- [Extension Configuration](pages/ExtensionConfiguration.md) - Setup and configuration
- [Authentication](pages/Authentication.md) - Authentication requirements
- [Fetch Mails By Label](pages/FetchMailsByLabel.md) - Retrieve emails from folders
- [Move Message](pages/MoveMessage.md) - Move emails to folders

## Notes

### API Consistency

The **Allow Retry** parameter is included in this method for API consistency across all catalog requests, even though this
method has no validation logic. The parameter has no practical effect on the method's behavior but maintains a consistent
interface pattern across the extension.

### Folder Hierarchy

Folders are returned with their full path using forward slash notation:
- Top-level folder: `"Projects"`
- Nested folder: `"Projects/2023"`
- Deeply nested: `"Projects/2023/Alpha Project"`

This notation allows you to understand the folder structure and use the exact folder path in other catalog requests like
Move Message or Fetch Mails By Label.

### System vs Custom Folders

The returned list includes both:
- **System Folders**: Inbox, Sent Items, Drafts, Deleted Items, etc.
- **Custom Folders**: User-created folders for organization

All folders are treated equally and can be used in other catalog requests.

