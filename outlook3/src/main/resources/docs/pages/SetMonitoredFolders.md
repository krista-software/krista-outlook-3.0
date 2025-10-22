# Set Monitored Folders

## Overview

Configure which Outlook folders to monitor for email notifications. When emails arrive in or are moved into these folders, Krista will be notified and can trigger workflows. This catalog request allows you to specify a list of folders to monitor, or leave empty to monitor all folders.

## Request Details

- **Area**: Setup
- **Type**: CHANGE_SYSTEM
- **Retry Support**: No

## Input Parameters

| Parameter Name | Type         | Required | Description                                                                      | Example                          |
|----------------|--------------|----------|----------------------------------------------------------------------------------|----------------------------------|
| Folder Names   | List of Text | No       | List of folder names to monitor. Leave empty to monitor all folders.            | ["Inbox", "Important", "Sales"]  |

### Parameter Details

#### Folder Names

- **Format**: List of folder name strings
- **Case Sensitivity**: Folder names are case-sensitive
- **Nested Folders**: Use forward slash (/) to specify nested folders (e.g., "Inbox/Projects")
- **Empty List**: If empty or not provided, all folders will be monitored
- **Validation**: Folder names are not validated at configuration time - validation occurs when notifications are received
- **Special Characters**: Folder names with special characters are supported

## Output Parameters

| Parameter Name              | Type    | Description                                                |
|-----------------------------|---------|------------------------------------------------------------|
| Is Configuration Successful | Boolean | Indicates whether the folder monitoring was configured successfully |

**Example Output**: 
```json
{
  "Is Configuration Successful": true
}
```

## Validation Rules

| Validation                    | Error Message                                    | Resolution                                           |
|-------------------------------|--------------------------------------------------|------------------------------------------------------|
| No configuration exists       | Configuration not found                          | Run Save Outlook Private Configuration first         |
| Invalid folder name format    | Folder name contains invalid characters          | Use valid folder names without control characters    |

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

**Cause**: System-level failures during configuration save
**Common Scenarios**:
- Database connection issues
- Configuration store unavailable
- Serialization errors

**Resolution**: Retry the operation or check system connectivity

## Usage Examples

### Example 1: Monitor Specific Folders

**Scenario**: Monitor only "Inbox" and "Important" folders for email notifications

**Input**:
```
Folder Names: ["Inbox", "Important"]
```

**Output**:
```json
{
  "Is Configuration Successful": true
}
```

**Behavior**: Only emails arriving in or moved to "Inbox" or "Important" folders will trigger notifications

### Example 2: Monitor Nested Folders

**Scenario**: Monitor a specific subfolder within Inbox

**Input**:
```
Folder Names: ["Inbox/Projects", "Inbox/Urgent"]
```

**Output**:
```json
{
  "Is Configuration Successful": true
}
```

**Behavior**: Only emails in the "Projects" and "Urgent" subfolders of Inbox will trigger notifications

### Example 3: Monitor All Folders

**Scenario**: Monitor all folders in the mailbox

**Input**:
```
Folder Names: []
```

**Output**:
```json
{
  "Is Configuration Successful": true
}
```

**Behavior**: Emails arriving in or moved to any folder will trigger notifications

### Example 4: Monitor Multiple Top-Level Folders

**Scenario**: Monitor multiple standard Outlook folders

**Input**:
```
Folder Names: ["Inbox", "Sent Items", "Drafts", "Archive"]
```

**Output**:
```json
{
  "Is Configuration Successful": true
}
```

**Behavior**: Emails in any of these four folders will trigger notifications

## Business Rules

1. **Configuration Persistence**: Monitored folder settings are stored in OutlookAttributes and persist across sessions
2. **Empty List Behavior**: An empty folder list means ALL folders are monitored
3. **Case Sensitivity**: Folder names must match exactly (case-sensitive)
4. **Nested Folder Format**: Use forward slash (/) to separate parent and child folder names
5. **No Validation at Config Time**: Folder names are not validated when configured - invalid folders are filtered out during notification processing
6. **Subscription Required**: Folder monitoring requires an active subscription created via [Enable Folder Monitoring](pages/EnableFolderMonitoring.md)

## Limitations

1. **Folder Name Format**: Must use exact folder names as they appear in Outlook
2. **Case Sensitivity**: Folder names are case-sensitive
3. **No Wildcards**: Wildcard patterns are not supported
4. **Requires Subscription**: Folder monitoring only works if subscription is enabled
5. **No Real-time Validation**: Folder existence is not validated at configuration time

## Best Practices

### 1. Folder Selection Strategy

- Start with specific folders to reduce notification volume
- Use nested folders for granular control
- Monitor all folders only when necessary
- Consider email volume when selecting folders

### 2. Folder Naming

- Use exact folder names as they appear in Outlook
- Be mindful of case sensitivity
- Use forward slash for nested folders
- Verify folder names using [List All Folders](pages/ListAllFolders.md)

### 3. Configuration Management

- Document which folders are being monitored
- Review monitored folders periodically
- Update configuration when folder structure changes
- Test with [Get Monitored Folders](pages/GetMonitoredFolders.md)

### 4. Performance Optimization

- Monitor only necessary folders to reduce processing overhead
- Use specific folders instead of monitoring all folders
- Consider email volume in monitored folders
- Review and optimize based on usage patterns

## Common Use Cases

### 1. Customer Support Workflow

```
Scenario: Monitor customer support inbox for new tickets
Action: Set monitored folders to ["Customer Support", "Urgent Support"]
Result: Krista triggers workflows when emails arrive in support folders
```

### 2. Sales Lead Processing

```
Scenario: Process leads from specific email folders
Action: Set monitored folders to ["Sales Leads", "Inbox/New Leads"]
Result: Automated lead processing for designated folders
```

### 3. Document Processing

```
Scenario: Process emails with attachments in specific folders
Action: Set monitored folders to ["Documents/Incoming", "Invoices"]
Result: Automated document extraction and processing
```

### 4. Compliance Monitoring

```
Scenario: Monitor all folders for compliance purposes
Action: Set monitored folders to [] (empty list)
Result: All email activity is monitored and logged
```

## Related Catalog Requests

- [Get Monitored Folders](pages/GetMonitoredFolders.md) - Retrieve current monitored folder configuration
- [List All Folders](pages/ListAllFolders.md) - Get list of all available folders
- [Enable Folder Monitoring](pages/EnableFolderMonitoring.md) - Enable the folder monitoring subscription
- [Receive notification of Email Change](pages/ReceiveNotificationOfEmailChange.md) - Event triggered when monitored emails change

## Technical Implementation

### Helper Class

- **Class**: SetupArea
- **Package**: app.krista.extensions.essentials.collaboration.outlook3.catalog
- **Method**: setMonitoredFolders(List<String> folderNames)
- **Storage**: OutlookAttributeStore
- **Persistence**: Configuration stored in OutlookAttributes object

### Configuration Storage

The monitored folders are stored as part of the OutlookAttributes object:
```java
OutlookAttributes updatedAttributes = new OutlookAttributes(
    currentAttributes.getClientId(),
    currentAttributes.getClientSecret(),
    currentAttributes.getTenantId(),
    currentAttributes.getEmail(),
    currentAttributes.isAllowMailAlert(),
    currentAttributes.getAuthType(),
    baseRoutingUrl,
    folderNames  // Monitored folders list
);
```

## Troubleshooting

### Configuration Not Saved

**Cause**: Missing or invalid Outlook configuration
**Solution**:
1. Verify Outlook extension is configured
2. Run [Save Outlook Private Configuration](pages/SaveOutlookPrivateConfiguration.md)
3. Check authentication status
4. Verify user permissions

### Folders Not Being Monitored

**Cause**: Subscription not enabled or folder names incorrect
**Solution**:
1. Enable subscription using [Enable Folder Monitoring](pages/EnableFolderMonitoring.md)
2. Verify folder names using [List All Folders](pages/ListAllFolders.md)
3. Check folder name case sensitivity
4. Confirm folders exist in mailbox

### Notifications Not Received

**Cause**: Subscription expired or folder filtering issue
**Solution**:
1. Check subscription status
2. Verify monitored folders configuration with [Get Monitored Folders](pages/GetMonitoredFolders.md)
3. Test with empty folder list (monitor all)
4. Review subscription expiration

## See Also

- [Extension Configuration](pages/ExtensionConfiguration.md) - Initial setup and configuration
- [Get Monitored Folders](pages/GetMonitoredFolders.md) - View current configuration
- [List All Folders](pages/ListAllFolders.md) - Browse available folders
- [Enable Folder Monitoring](pages/EnableFolderMonitoring.md) - Enable monitoring subscription

