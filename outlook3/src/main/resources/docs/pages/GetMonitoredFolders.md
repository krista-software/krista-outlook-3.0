# Get Monitored Folders

## Overview

Retrieve the list of currently monitored Outlook folders. This catalog request returns the folder configuration that was set using the Set Monitored Folders request, allowing you to verify which folders are being monitored for email notifications.

## Request Details

- **Area**: Setup
- **Type**: QUERY_SYSTEM
- **Retry Support**: No

## Input Parameters

This catalog request requires no input parameters.

## Output Parameters

| Parameter Name     | Type         | Description                                                           |
|--------------------|--------------|-----------------------------------------------------------------------|
| Monitored Folders  | List of Text | List of folder names currently being monitored. Empty if monitoring all folders. |

**Example Output**: 
```json
{
  "Monitored Folders": ["Inbox", "Important", "Sales"]
}
```

## Validation Rules

| Validation              | Error Message                | Resolution                                      |
|-------------------------|------------------------------|-------------------------------------------------|
| No configuration exists | Configuration not found      | Run Save Outlook Private Configuration first    |

## Error Handling

### Configuration Errors

**Cause**: Missing Outlook configuration
**Common Scenarios**:
- Outlook extension not configured
- Configuration was deleted or corrupted
- User not authenticated

**Resolution**: 
1. Run [Save Outlook Private Configuration](pages/SaveOutlookPrivateConfiguration.md) first
2. Verify authentication is complete
3. Check configuration exists

### System Errors (SYSTEM_ERROR)

**Cause**: System-level failures during configuration retrieval
**Common Scenarios**:
- Database connection issues
- Configuration store unavailable
- Deserialization errors

**Resolution**: Retry the operation or check system connectivity

## Usage Examples

### Example 1: Check Current Configuration

**Scenario**: Verify which folders are currently being monitored

**Input**: (No parameters required)

**Output**:
```json
{
  "Monitored Folders": ["Inbox", "Important"]
}
```

**Interpretation**: Only "Inbox" and "Important" folders are being monitored

### Example 2: Monitoring All Folders

**Scenario**: Check if all folders are being monitored

**Input**: (No parameters required)

**Output**:
```json
{
  "Monitored Folders": []
}
```

**Interpretation**: Empty list means all folders are being monitored

### Example 3: Nested Folder Configuration

**Scenario**: View configuration with nested folders

**Input**: (No parameters required)

**Output**:
```json
{
  "Monitored Folders": ["Inbox/Projects", "Inbox/Urgent", "Sales"]
}
```

**Interpretation**: Monitoring specific subfolders and a top-level folder

## Business Rules

1. **Empty List Meaning**: An empty list indicates all folders are being monitored
2. **Configuration Persistence**: Returns the most recently saved configuration
3. **No Default Value**: If never configured, returns empty list (monitor all)
4. **Read-Only Operation**: This request does not modify any configuration
5. **User-Specific**: Returns configuration for the authenticated user only

## Limitations

1. **No Folder Validation**: Does not verify if listed folders actually exist
2. **No Status Information**: Does not indicate if subscription is active
3. **User Context**: Only returns configuration for current user
4. **No Historical Data**: Only shows current configuration, not history

## Best Practices

### 1. Configuration Verification

- Check configuration after making changes
- Verify folder names are correct
- Confirm expected folders are listed
- Use before troubleshooting notification issues

### 2. Monitoring Strategy

- Review configuration periodically
- Document folder monitoring strategy
- Verify configuration matches requirements
- Update when folder structure changes

### 3. Troubleshooting

- Use this request to diagnose notification issues
- Verify configuration before enabling subscription
- Check after folder structure changes
- Confirm settings after migration or updates

## Common Use Cases

### 1. Configuration Audit

```
Scenario: Verify folder monitoring configuration during audit
Action: Call Get Monitored Folders
Result: Documentation of current monitoring scope
```

### 2. Troubleshooting Notifications

```
Scenario: Emails not triggering notifications as expected
Action: Check monitored folders configuration
Result: Identify if folder is included in monitoring
```

### 3. Pre-Deployment Verification

```
Scenario: Verify configuration before deploying workflow
Action: Confirm monitored folders match workflow requirements
Result: Ensure proper folder coverage
```

### 4. Configuration Documentation

```
Scenario: Document system configuration for compliance
Action: Retrieve and record monitored folders
Result: Audit trail of monitoring configuration
```

## Related Catalog Requests

- [Set Monitored Folders](pages/SetMonitoredFolders.md) - Configure which folders to monitor
- [List All Folders](pages/ListAllFolders.md) - Get list of all available folders
- [Enable Folder Monitoring](pages/EnableFolderMonitoring.md) - Enable the folder monitoring subscription
- [Receive notification of Email Change](pages/ReceiveNotificationOfEmailChange.md) - Event triggered when monitored emails change

## Technical Implementation

### Helper Class

- **Class**: SetupArea
- **Package**: app.krista.extensions.essentials.collaboration.outlook3.catalog
- **Method**: getMonitoredFolders()
- **Storage**: OutlookAttributeStore
- **Retrieval**: Loads configuration from OutlookAttributes object

### Implementation Details

```java
OutlookAttributes attributes = outlookAttributeStore.load(invoker.getInvokerId());
List<String> monitoredFolders = attributes.getMonitoredFolders();
```

The method retrieves the monitored folders list from the stored OutlookAttributes configuration.

## Troubleshooting

### Empty Result When Folders Were Configured

**Cause**: Configuration was reset or cleared
**Solution**:
1. Verify configuration wasn't overwritten
2. Check if [Set Monitored Folders](pages/SetMonitoredFolders.md) was called with empty list
3. Review recent configuration changes
4. Reconfigure if necessary

### Configuration Not Found Error

**Cause**: Outlook extension not configured
**Solution**:
1. Run [Save Outlook Private Configuration](pages/SaveOutlookPrivateConfiguration.md)
2. Complete authentication process
3. Verify user has proper permissions
4. Check configuration store connectivity

### Unexpected Folder List

**Cause**: Configuration was modified by another process
**Solution**:
1. Review recent configuration changes
2. Verify no automated processes are modifying configuration
3. Check for concurrent configuration updates
4. Reconfigure using [Set Monitored Folders](pages/SetMonitoredFolders.md)

## See Also

- [Extension Configuration](pages/ExtensionConfiguration.md) - Initial setup and configuration
- [Set Monitored Folders](pages/SetMonitoredFolders.md) - Configure monitored folders
- [List All Folders](pages/ListAllFolders.md) - Browse available folders
- [Enable Folder Monitoring](pages/EnableFolderMonitoring.md) - Enable monitoring subscription

