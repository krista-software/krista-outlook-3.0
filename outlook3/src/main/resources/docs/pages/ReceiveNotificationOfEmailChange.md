# Receive notification of Email Change

## Overview

Allows Krista to be notified of email changes, specifically for identifying when an email has been moved into a specific folder or when new emails arrive. This is a WAIT_FOR_EVENT type catalog request that is triggered automatically by Microsoft Graph subscriptions when emails are created or updated in monitored folders.

## Prerequisites

Before this event can be triggered, you must complete the following setup steps in order:

### 1. Configure Outlook Extension
Run [Save Outlook Private Configuration](pages/SaveOutlookPrivateConfiguration.md) to set up the Outlook extension with your Microsoft 365 credentials and authentication.

**Required**: Yes
**Purpose**: Establishes connection to Microsoft Graph API

### 2. List All Available Folders
Run [List All Folders](pages/ListAllFolders.md) to discover all available folders in your mailbox.

**Required**: Recommended
**Purpose**: Identify exact folder names (case-sensitive) before configuring monitoring
**Example**: Discover folders like "Inbox", "Customer Support", "Sales Leads", "Inbox/Projects"

### 3. Set Monitored Folders
Run [Set Monitored Folders](pages/SetMonitoredFolders.md) to specify which folders should trigger notifications.

**Required**: Recommended (if not set, all folders will be monitored)
**Purpose**: Filter notifications to specific folders to reduce noise and improve performance
**Example**: Monitor only "Customer Support" and "Sales Leads" folders
**Best Practice**: Use exact folder names from Step 2 to avoid case-sensitivity issues

### 4. Enable Folder Monitoring Subscription
Run [Enable Folder Monitoring](pages/EnableFolderMonitoring.md) to create the Microsoft Graph subscription.

**Required**: Yes
**Purpose**: Creates the subscription that sends notifications when emails are created or updated
**Note**: Subscription expires after 3 days and must be renewed

### Setup Sequence Example

```
Step 1: Save Outlook Private Configuration
  ↓
Step 2: List All Folders (discover available folders)
  ↓
Step 3: Set Monitored Folders (e.g., ["Inbox", "Important", "Customer Support"])
  ↓
Step 4: Enable Folder Monitoring
  ↓
Step 5: Receive notification of Email Change (automatic when emails arrive/move)
```

### Verification Checklist

Before expecting notifications, verify:
- ✅ Outlook extension is configured ([Save Outlook Private Configuration](pages/SaveOutlookPrivateConfiguration.md))
- ✅ Available folders discovered ([List All Folders](pages/ListAllFolders.md))
- ✅ Monitored folders are set ([Set Monitored Folders](pages/SetMonitoredFolders.md) - use [Get Monitored Folders](pages/GetMonitoredFolders.md) to verify)
- ✅ Folder monitoring subscription is active ([Enable Folder Monitoring](pages/EnableFolderMonitoring.md))
- ✅ Workflow is listening for `emailChangeNotification` event
- ✅ Test email sent to monitored folder

## Request Details

- **Area**: Messaging
- **Type**: WAIT_FOR_EVENT
- **Retry Support**: No (Event-driven)
- **Event Name**: `emailChangeNotification`

## Input Parameters

This catalog request is triggered by events and receives the following parameters automatically:

| Parameter Name | Type     | Required | Description                                      | Example                          |
|----------------|----------|----------|--------------------------------------------------|----------------------------------|
| eventName      | Text     | Yes      | Name of the event (must be "emailChangeNotification") | "emailChangeNotification"        |
| eventData      | FreeForm | Yes      | Event data containing email and notification details | See Event Data Structure below   |

### Event Data Structure

The `eventData` parameter contains the following fields:

| Field Name       | Type    | Description                                           | Example                                    |
|------------------|---------|-------------------------------------------------------|--------------------------------------------|
| notificationId   | Text    | Unique identifier for the notification                | "AQMkADY4ZTFiMGIxLWU1YjUtNDEwMS04Y2Q0..." |
| subscriptionId   | Text    | ID of the subscription that triggered the notification | "7f6aa149-aefe-4f3a-b4be-943a3e8e8e8e"    |
| changeType       | Text    | Type of change (created or updated)                   | "created" or "updated"                     |
| folderId         | Text    | ID of the folder containing the email                 | "AQMkADAwATM0MDAAMS1iNTcyLTI2ZjYtMDAA..."  |
| messageId        | Text    | Unique identifier for the email message               | "AAMkADY4ZTFiMGIxLWU1YjUtNDEwMS04Y2Q0..." |
| subject          | Text    | Email subject line                                    | "Project Update"                           |
| body             | Text    | Email body content                                    | "Here's the latest update..."              |
| from             | Email   | Sender's email address                                | "sender@company.com"                       |
| to               | Email   | Primary recipients (comma-separated)                  | "user@company.com"                         |
| cc               | Email   | Carbon copy recipients (comma-separated)              | "manager@company.com"                      |
| bcc              | Email   | Blind carbon copy recipients (comma-separated)        | "archive@company.com"                      |
| attachments      | Text    | "true" or "false" indicating if email has attachments | "true"                                     |

## Output Parameters

| Parameter Name   | Type    | Description                                                    |
|------------------|---------|----------------------------------------------------------------|
| Notification Id  | Text    | Unique identifier for the notification                         |
| Subscription Id  | Text    | ID of the subscription that triggered the notification         |
| Change Type      | Text    | Type of change (created or updated)                            |
| Folder ID        | Text    | ID of the folder containing the email                          |
| Subject          | Text    | Email subject line                                             |
| Body             | Text    | Email body content                                             |
| From             | Email   | Sender's email address                                         |
| To               | Email   | Primary recipients                                             |
| CC               | Email   | Carbon copy recipients                                         |
| BCC              | Email   | Blind carbon copy recipients                                   |
| Attachments      | Boolean | True if email has attachments, false otherwise                 |

**Example Output**:
```json
{
  "Notification Id": "AQMkADY4ZTFiMGIxLWU1YjUtNDEwMS04Y2Q0...",
  "Subscription Id": "7f6aa149-aefe-4f3a-b4be-943a3e8e8e8e",
  "Change Type": "created",
  "Folder ID": "AQMkADAwATM0MDAAMS1iNTcyLTI2ZjYtMDAA...",
  "Subject": "New Customer Inquiry",
  "Body": "We have received a new customer inquiry...",
  "From": "customer@example.com",
  "To": "support@company.com",
  "CC": "",
  "BCC": "",
  "Attachments": true
}
```

## Validation Rules

| Validation           | Error Message                                                    | Resolution                                           |
|----------------------|------------------------------------------------------------------|------------------------------------------------------|
| Invalid event name   | Invalid event name. Expected: emailChangeNotification            | Ensure event name is "emailChangeNotification"       |
| Missing event data   | Event data is required                                           | Verify subscription is configured correctly          |
| Authentication error | Authorization error                                              | Re-authenticate and verify permissions               |

## Error Handling

### Input Errors (INPUT_ERROR)

**Cause**: Invalid event name or missing event data
**Common Scenarios**:
- Event name is not "emailChangeNotification"
- Event data is null or empty
- Required fields missing from event data

**Resolution**: Verify subscription configuration and event payload

### Authorization Errors (MustAuthorizeException)

**Cause**: Authentication or permission issues
**Common Scenarios**:
- Authentication token expired
- Insufficient permissions
- User not authenticated

**Resolution**: Re-authenticate and verify Mail.ReadWrite permission

### System Errors (SYSTEM_ERROR)

**Cause**: Processing errors during notification handling
**Common Scenarios**:
- Email retrieval failed
- Folder lookup failed
- Data parsing errors

**Resolution**: Check logs for detailed error information and retry

## Usage Examples

> **Note**: All examples assume the prerequisites have been completed:
> 1. [Save Outlook Private Configuration](pages/SaveOutlookPrivateConfiguration.md) ✓
> 2. [List All Folders](pages/ListAllFolders.md) ✓
> 3. [Set Monitored Folders](pages/SetMonitoredFolders.md) ✓
> 4. [Enable Folder Monitoring](pages/EnableFolderMonitoring.md) ✓

### Example 1: New Email Arrives in Monitored Folder

**Scenario**: Customer sends email to support inbox

**Prerequisites Setup**:
```
1. Configured Outlook extension
2. Listed all folders and identified "Customer Support" folder
3. Set monitored folders to ["Customer Support"]
4. Enabled folder monitoring subscription
```

**Event Triggered**:
```json
{
  "eventName": "emailChangeNotification",
  "eventData": {
    "notificationId": "notification-123",
    "subscriptionId": "sub-456",
    "changeType": "created",
    "folderId": "folder-789",
    "messageId": "msg-abc",
    "subject": "Need Help with Product",
    "body": "I'm having trouble with...",
    "from": "customer@example.com",
    "to": "support@company.com",
    "cc": "",
    "bcc": "",
    "attachments": "false"
  }
}
```

**Workflow Action**: Create support ticket, assign to agent, send auto-reply

### Example 2: Email Moved to Specific Folder

**Scenario**: Email moved from Inbox to "High Priority" folder

**Event Triggered**:
```json
{
  "eventName": "emailChangeNotification",
  "eventData": {
    "changeType": "updated",
    "folderId": "high-priority-folder-id",
    "subject": "Urgent: System Down",
    "from": "admin@company.com",
    "attachments": "true"
  }
}
```

**Workflow Action**: Escalate to on-call team, create incident ticket

### Example 3: Email with Attachments

**Scenario**: Invoice email arrives with PDF attachment

**Event Triggered**:
```json
{
  "eventName": "emailChangeNotification",
  "eventData": {
    "changeType": "created",
    "subject": "Invoice #12345",
    "from": "vendor@supplier.com",
    "to": "accounting@company.com",
    "attachments": "true"
  }
}
```

**Workflow Action**: Extract attachment, process invoice, update accounting system

## Business Rules

1. **Event-Driven**: This catalog request is triggered automatically by subscription notifications
2. **Folder Filtering**: Only emails in monitored folders trigger this event (if folder monitoring is configured)
3. **Change Types**: Triggers on both "created" (new email) and "updated" (email moved/modified) events
4. **Attachment Flag**: Attachments field is a boolean indicating presence, not the actual files
5. **Real-time Processing**: Events are processed as they occur
6. **Subscription Required**: Requires active subscription created via [Enable Folder Monitoring](pages/EnableFolderMonitoring.md)

## Event Flow

1. **Email Event Occurs**: Email arrives or is moved in Outlook
2. **Microsoft Graph Notification**: Graph API sends notification to `/rest/outlook/folderMonitoringNotification`
3. **Event Processing**: Notification is processed and email details are extracted
4. **Folder Filtering**: If monitored folders are configured, notification is filtered
5. **Event Triggered**: If email is in monitored folder, `emailChangeNotification` event is triggered
6. **Workflow Execution**: Krista workflow listening for this event is executed
7. **Output Returned**: Email details are provided to the workflow

## Limitations

1. **Subscription Required**: Requires active folder monitoring subscription
2. **Attachment Content**: Only indicates if attachments exist, does not provide attachment content
3. **Folder Name**: Provides folder ID, not folder name (use ID to lookup name if needed)
4. **Event Timing**: Small delay between email event and notification delivery
5. **Duplicate Events**: Same email may trigger multiple events if moved multiple times
6. **Rate Limits**: Subject to Microsoft Graph notification rate limits

## Best Practices

### 1. Workflow Design

- Design workflows to handle both "created" and "updated" change types
- Implement idempotency to handle duplicate notifications
- Use notification ID to track processed events
- Handle missing or null fields gracefully

### 2. Folder Monitoring Strategy

- Configure specific folders to reduce notification volume
- Use folder-based routing for different workflow types
- Monitor high-priority folders separately
- Document folder-to-workflow mappings

### 3. Error Handling

- Implement retry logic for transient failures
- Log all notification events for audit trail
- Handle authentication errors gracefully
- Monitor for missing or delayed notifications

### 4. Performance Optimization

- Process notifications asynchronously when possible
- Batch similar operations
- Cache folder lookups
- Optimize workflow execution time

## Common Use Cases

### 1. Customer Support Automation

```
Scenario: Auto-create support tickets from customer emails
Trigger: Email arrives in "Customer Support" folder
Action: Extract email details, create ticket, assign to agent
Result: Automated ticket creation and routing
```

### 2. Sales Lead Processing

```
Scenario: Process new sales leads from email
Trigger: Email arrives in "Sales Leads" folder
Action: Extract contact info, create lead record, notify sales team
Result: Automated lead capture and distribution
```

### 3. Document Processing

```
Scenario: Process invoices received via email
Trigger: Email with attachments arrives in "Invoices" folder
Action: Extract attachments, parse invoice data, update accounting system
Result: Automated invoice processing
```

### 4. Priority Escalation

```
Scenario: Escalate high-priority emails
Trigger: Email moved to "Urgent" folder
Action: Send alerts, create incident, notify on-call team
Result: Rapid response to urgent issues
```

## Related Catalog Requests

- [Enable Folder Monitoring](pages/EnableFolderMonitoring.md) - Enable the subscription that triggers this event
- [Set Monitored Folders](pages/SetMonitoredFolders.md) - Configure which folders trigger notifications
- [Fetch Mail By Message ID](pages/FetchMailByMessageId.md) - Get full email details using message ID
- [Send Alert Using Notification Delta](pages/SendAlertUsingNotificationDelta.md) - Original mail received alert

## Technical Implementation

### Helper Class

- **Class**: MessagingArea
- **Package**: app.krista.extensions.essentials.collaboration.outlook3.catalog
- **Method**: receiveNotificationOfEmailChange(String eventName, FreeForm eventData)
- **Event Name**: `emailChangeNotification` (defined in Constants.EMAIL_CHANGE_NOTIFICATION)
- **Endpoint**: `/rest/outlook/folderMonitoringNotification`

### Event Constants

```java
public static final String EMAIL_CHANGE_NOTIFICATION = "emailChangeNotification";
public static final String MESSAGE_ID = "messageId";
public static final String FOLDER_NAME = "folderName";
public static final String CHANGE_TYPE = "changeType";
public static final String NOTIFICATION_ID = "notificationId";
public static final String SUBSCRIPTION_ID = "subscriptionId";
public static final String ATTACHMENTS = "attachments";
```

## Troubleshooting

### Events Not Triggered

**Cause**: Subscription not active or folder not monitored
**Solution**:
1. Verify subscription is active using [Enable Folder Monitoring](pages/EnableFolderMonitoring.md)
2. Check monitored folders configuration with [Get Monitored Folders](pages/GetMonitoredFolders.md)
3. Verify exact folder name using [List All Folders](pages/ListAllFolders.md) (folder names are case-sensitive)
4. Verify email is in monitored folder
5. Check subscription expiration

### Prerequisites Not Completed

**Cause**: Missing required setup steps
**Solution**:
1. **Check Outlook Configuration**: Verify [Save Outlook Private Configuration](pages/SaveOutlookPrivateConfiguration.md) was completed
2. **Discover Folders**: Run [List All Folders](pages/ListAllFolders.md) to identify exact folder names
3. **Configure Monitored Folders**: Use [Set Monitored Folders](pages/SetMonitoredFolders.md) with exact folder names from step 2
4. **Verify Folder Configuration**: Use [Get Monitored Folders](pages/GetMonitoredFolders.md) to confirm folder settings
5. **Enable Subscription**: Confirm [Enable Folder Monitoring](pages/EnableFolderMonitoring.md) was run successfully
6. **Test Connection**: Run [Test Connection](pages/TestConnection.md) to verify authentication
7. **Review Setup Sequence**: Follow the prerequisites section step-by-step

**Common Missing Steps**:
- Forgot to run [Enable Folder Monitoring](pages/EnableFolderMonitoring.md) after configuration
- Subscription expired (renew every 3 days)
- Monitored folders list doesn't include the target folder (verify with [List All Folders](pages/ListAllFolders.md))
- Folder name case mismatch (folder names are case-sensitive)
- Outlook extension not properly configured

### Missing Event Data

**Cause**: Notification payload incomplete
**Solution**:
1. Check Microsoft Graph notification logs
2. Verify subscription configuration
3. Review notification endpoint processing
4. Check for API errors

### Duplicate Events

**Cause**: Email moved multiple times or notification retry
**Solution**:
1. Implement idempotency using notification ID
2. Track processed notifications
3. Use message ID to deduplicate
4. Design workflows to handle duplicates

### Authentication Errors

**Cause**: Token expired or permissions revoked
**Solution**:
1. Re-authenticate user
2. Verify Mail.ReadWrite permission
3. Check token expiration
4. Test with [Test Connection](pages/TestConnection.md)

## See Also

- [Extension Configuration](pages/ExtensionConfiguration.md) - Initial setup and configuration
- [Enable Folder Monitoring](pages/EnableFolderMonitoring.md) - Enable subscription
- [Set Monitored Folders](pages/SetMonitoredFolders.md) - Configure monitored folders
- [Send Alert Using Notification Delta](pages/SendAlertUsingNotificationDelta.md) - Original mail alert feature

