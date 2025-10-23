# List All Folders

## Overview

Retrieve all available Outlook folders that can be monitored. This catalog request queries the user's mailbox and returns a complete list of all folders, including nested subfolders, that are available for monitoring. Use this request to discover folder names before configuring folder monitoring.

## Request Details

- **Area**: Setup
- **Type**: QUERY_SYSTEM
- **Retry Support**: No

## Input Parameters

This catalog request requires no input parameters.

## Output Parameters

| Parameter Name     | Type         | Description                                                                    |
|--------------------|--------------|--------------------------------------------------------------------------------|
| Available Folders  | List of Text | Complete list of all folders in the mailbox, including nested folders         |

**Example Output**: 
```json
{
  "Available Folders": [
    "Inbox",
    "Inbox/Projects",
    "Inbox/Urgent",
    "Sent Items",
    "Drafts",
    "Archive",
    "Deleted Items",
    "Junk Email",
    "Sales",
    "Sales/Leads",
    "Sales/Customers"
  ]
}
```

## Validation Rules

| Validation                    | Error Message                                    | Resolution                                           |
|-------------------------------|--------------------------------------------------|------------------------------------------------------|
| Authentication failure        | Unauthorized access                              | Verify authentication and permissions                |
| Microsoft Graph API error     | Error listing folders                            | Check connectivity and retry                         |

## Error Handling

### Authentication Errors

**Cause**: Missing or invalid authentication
**Common Scenarios**:
- Authentication token expired
- Insufficient permissions
- User not authenticated

**Resolution**: 
1. Verify authentication is complete
2. Check Mail.ReadWrite permission is granted
3. Re-authenticate if necessary
4. Test with [Test Connection](pages/TestConnection.md)

### System Errors (SYSTEM_ERROR)

**Cause**: Microsoft Graph API or system-level failures
**Common Scenarios**:
- Network connectivity issues
- Microsoft Graph service unavailable
- API rate limiting

**Resolution**: Retry the operation or check system connectivity

## Usage Examples

### Example 1: Discover All Folders

**Scenario**: Get complete list of folders before configuring monitoring

**Input**: (No parameters required)

**Output**:
```json
{
  "Available Folders": [
    "Inbox",
    "Sent Items",
    "Drafts",
    "Deleted Items",
    "Junk Email",
    "Archive",
    "Notes",
    "Outbox"
  ]
}
```

**Use Case**: Use this list to select folders for monitoring

### Example 2: Identify Nested Folders

**Scenario**: Find subfolders within main folders

**Input**: (No parameters required)

**Output**:
```json
{
  "Available Folders": [
    "Inbox",
    "Inbox/Important",
    "Inbox/Projects",
    "Inbox/Projects/Active",
    "Inbox/Projects/Completed",
    "Sent Items"
  ]
}
```

**Use Case**: Identify exact nested folder paths for monitoring configuration

### Example 3: Custom Folder Structure

**Scenario**: View user-created custom folders

**Input**: (No parameters required)

**Output**:
```json
{
  "Available Folders": [
    "Inbox",
    "Customer Support",
    "Customer Support/Tier 1",
    "Customer Support/Tier 2",
    "Sales Leads",
    "Marketing",
    "Internal"
  ]
}
```

**Use Case**: Discover custom organizational folders

## Business Rules

1. **Hierarchical Format**: Nested folders are represented with forward slash (/) separator
2. **Complete Enumeration**: All folders and subfolders are returned
3. **Real-time Data**: Results reflect current mailbox state at request time
4. **User-Specific**: Returns folders for authenticated user only
5. **System Folders Included**: Standard Outlook folders (Inbox, Sent Items, etc.) are included
6. **Custom Folders Included**: User-created folders are included

## Limitations

1. **Permissions Required**: Requires Mail.ReadWrite permission
2. **Large Mailboxes**: May take longer for mailboxes with many folders
3. **API Rate Limits**: Subject to Microsoft Graph API rate limiting
4. **No Filtering**: Returns all folders without filtering options
5. **Snapshot in Time**: Results are current at request time, not real-time

## Best Practices

### 1. Folder Discovery

- Run this request before configuring folder monitoring
- Use to verify folder names and structure
- Check for nested folder paths
- Identify exact folder names (case-sensitive)

### 2. Configuration Planning

- Review folder structure before setting up monitoring
- Identify high-priority folders for monitoring
- Plan folder hierarchy for organization
- Document folder naming conventions

### 3. Troubleshooting

- Use to verify folder existence
- Confirm exact folder names and paths
- Check for folder name changes
- Validate nested folder structure

### 4. Performance Considerations

- Cache results if folder structure is stable
- Avoid frequent calls for large mailboxes
- Use results to plan efficient monitoring strategy
- Consider folder count when planning monitoring

## Common Use Cases

### 1. Initial Setup

```
Scenario: Setting up folder monitoring for the first time
Action: List all folders to see available options
Result: Complete view of mailbox structure for configuration
```

### 2. Folder Name Verification

```
Scenario: Verify exact folder name before configuration
Action: List all folders and find the target folder
Result: Correct folder name with proper case and path
```

### 3. Mailbox Structure Analysis

```
Scenario: Understand mailbox organization
Action: Retrieve complete folder hierarchy
Result: Documentation of folder structure
```

### 4. Migration Planning

```
Scenario: Plan folder monitoring after mailbox migration
Action: List folders in new mailbox
Result: Updated folder list for reconfiguration
```

## Related Catalog Requests

- [Set Monitored Folders](pages/SetMonitoredFolders.md) - Configure which folders to monitor
- [Get Monitored Folders](pages/GetMonitoredFolders.md) - View current monitoring configuration
- [Enable Folder Monitoring](pages/EnableFolderMonitoring.md) - Enable the folder monitoring subscription
- [Test Connection](pages/TestConnection.md) - Verify connectivity and permissions

## Technical Implementation

### Helper Class

- **Class**: SetupArea
- **Package**: app.krista.extensions.essentials.collaboration.outlook3.catalog
- **Method**: listAllFolders()
- **Service**: AccountImpl.getFolderNames()
- **API**: Microsoft Graph Mail Folders API

### Implementation Details

The method uses the AccountImpl class to retrieve folder names:

```java
AccountImpl account = new AccountImpl(providerFactory);
List<String> folderNames = account.getFolderNames();
```

The `getFolderNames()` method:
1. Retrieves all top-level folders
2. Recursively retrieves child folders
3. Formats nested folders with forward slash separator
4. Returns complete hierarchical list

### Folder Retrieval Process

1. **Top-Level Folders**: Queries `/me/mailFolders` endpoint
2. **Child Folders**: For each folder with children, queries `/me/mailFolders/{id}/childFolders`
3. **Recursive Processing**: Continues until all nested levels are retrieved
4. **Path Construction**: Builds full path using parent/child format

## Troubleshooting

### No Folders Returned

**Cause**: Authentication or permission issues
**Solution**:
1. Verify user is authenticated
2. Check Mail.ReadWrite permission is granted
3. Test connection with [Test Connection](pages/TestConnection.md)
4. Review authentication token validity

### Missing Folders

**Cause**: Permissions or folder visibility issues
**Solution**:
1. Verify user has access to all folders
2. Check for hidden or system folders
3. Confirm folder sharing permissions
4. Review mailbox delegation settings

### Performance Issues

**Cause**: Large number of folders or slow network
**Solution**:
1. Allow sufficient time for large mailboxes
2. Check network connectivity
3. Consider caching results
4. Retry during off-peak hours

### API Errors

**Cause**: Microsoft Graph API issues
**Solution**:
1. Check Microsoft Graph service status
2. Verify API rate limits not exceeded
3. Review authentication token expiration
4. Retry with exponential backoff

## See Also

- [Extension Configuration](pages/ExtensionConfiguration.md) - Initial setup and configuration
- [Set Monitored Folders](pages/SetMonitoredFolders.md) - Configure monitored folders
- [Get Monitored Folders](pages/GetMonitoredFolders.md) - View current configuration
- [Test Connection](pages/TestConnection.md) - Verify connectivity and permissions

