# Testing Guide: Email Folder Monitoring Feature

## Overview
This guide provides comprehensive instructions for testing the new email folder monitoring feature that detects when emails arrive in or are moved into specific Outlook folders.

## Prerequisites

### 1. Outlook Configuration
- Outlook extension must be configured (Public or Private authentication)
- Valid Microsoft 365 account with access to Outlook
- Mail alerts must be enabled (`Allow Mail Alert = true`)

### 2. Test Environment Setup
- Krista platform running and accessible
- Outlook extension deployed
- Network connectivity to Microsoft Graph API
- Valid routing URL for webhook notifications

## Feature Components

### What Was Implemented

1. **Subscription Changes**
   - Now subscribes to both `created` and `updated` change types
   - Monitors all user messages (`/messages`) instead of just Inbox
   - Detects emails moved by rules or manually

2. **Folder Configuration**
   - New catalog requests to manage monitored folders
   - Stores folder preferences in OutlookAttributes
   - Supports monitoring specific folders or all folders

3. **Enhanced Notifications**
   - Extracts full message details (subject, sender, body, attachments)
   - Includes folder information (folder ID and name)
   - Filters notifications by monitored folders
   - Triggers new event: `emailChangeNotification`

4. **New Catalog Requests**
   - `Set Monitored Folders` - Configure which folders to monitor
   - `Get Monitored Folders` - View current configuration
   - `List All Folders` - See all available folders

## Testing Scenarios

### Scenario 1: Initial Setup and Configuration

#### Step 1: Configure Outlook Extension
```
Catalog Request: Save Outlook Public Configuration (or Private)
Input:
  - Email: your-email@company.com
  - Allow Mail Alert: true
```

**Expected Result**: Configuration saved successfully

#### Step 2: List Available Folders
```
Catalog Request: List All Folders
```

**Expected Result**: Returns list of all Outlook folders (Inbox, Sent Items, Drafts, custom folders, etc.)

#### Step 3: Configure Monitored Folders
```
Catalog Request: Set Monitored Folders
Input:
  - Folder Names: "Krista Inbox, Action Items, Need Human Review"
```

**Expected Result**: 
- Configuration successful
- Message: "Monitoring 3 folder(s): Krista Inbox, Action Items, Need Human Review"

#### Step 4: Verify Configuration
```
Catalog Request: Get Monitored Folders
```

**Expected Result**: Returns "Krista Inbox, Action Items, Need Human Review"

---

### Scenario 2: Test Email Arrival in Monitored Folder

#### Setup
1. Create a custom folder named "Krista Inbox" in Outlook
2. Configure monitored folders to include "Krista Inbox"
3. Create a wait-for-event in Krista workflow:
   - Event Name: `emailChangeNotification`
   - Expected Fields: messageId, changeType, folderId, folderName, subject, from, to, body, attachments

#### Test Steps
1. Send an email directly to your Outlook account
2. Create an Outlook rule: "Move emails from [specific sender] to Krista Inbox"
3. Send a test email from that sender

**Expected Results**:
- Email arrives in "Krista Inbox" folder
- Microsoft Graph sends "created" notification
- Krista receives notification with:
  - `changeType`: "created"
  - `folderName`: "Krista Inbox"
  - `subject`: Email subject
  - `from`: Sender email
  - Full message details
- Wait-for-event is triggered
- Workflow continues

---

### Scenario 3: Test Email Moved to Monitored Folder

#### Setup
1. Ensure "Action Items" folder exists and is monitored
2. Create wait-for-event for `emailChangeNotification`

#### Test Steps
1. Receive an email in Inbox (not monitored)
2. Manually drag/move the email to "Action Items" folder

**Expected Results**:
- Microsoft Graph sends "updated" notification
- Krista receives notification with:
  - `changeType`: "updated"
  - `folderName`: "Action Items"
  - `parentFolderId`: ID of Action Items folder
  - Full message details
- Wait-for-event is triggered

---

### Scenario 4: Test Email Moved by Outlook Rule

#### Setup
1. Create Outlook rule: "Move emails with subject containing 'URGENT' to Need Human Review"
2. Configure "Need Human Review" as monitored folder

#### Test Steps
1. Send email with subject "URGENT: Please Review"
2. Outlook rule automatically moves it to "Need Human Review"

**Expected Results**:
- Email moved automatically by rule
- Microsoft Graph sends "updated" notification
- Krista detects email in monitored folder
- Notification includes all message details
- Workflow triggered

---

### Scenario 5: Test Non-Monitored Folder (Negative Test)

#### Setup
1. Configure monitored folders: "Krista Inbox, Action Items"
2. Create wait-for-event for `emailChangeNotification`

#### Test Steps
1. Send email to Inbox (not in monitored list)
2. Move email to Drafts (not in monitored list)

**Expected Results**:
- Notifications received by Krista
- Notifications are filtered out (folder not monitored)
- Wait-for-event is NOT triggered
- No workflow execution

---

### Scenario 6: Test Monitor All Folders

#### Setup
1. Clear monitored folders configuration:
   ```
   Catalog Request: Set Monitored Folders
   Input: Folder Names: (leave empty)
   ```

#### Test Steps
1. Send email to any folder
2. Move email between any folders

**Expected Results**:
- All email notifications are processed
- Notifications triggered for any folder
- Backward compatibility maintained

---

### Scenario 7: Test Duplicate Notification Handling

#### Setup
1. Configure monitored folder
2. Create wait-for-event

#### Test Steps
1. Send email to monitored folder
2. Microsoft may send duplicate notification (if no acknowledgment)

**Expected Results**:
- First notification processed
- Duplicate notification detected and rejected
- Log message: "Duplicate alert detected, rejecting: {messageId}"
- Wait-for-event triggered only once

---

## Verification Checklist

### Logs to Check

1. **Subscription Creation**
   ```
   Look for: "Subscription created/updated successfully"
   Verify: changeType = "created,updated"
   Verify: resource = "/users/{email}/messages"
   ```

2. **Notification Receipt**
   ```
   Look for: "Krista received a new alert to process"
   Look for: "Processing notification - MessageId: {id}, ChangeType: {type}"
   ```

3. **Folder Filtering**
   ```
   Look for: "Message {id} is in folder: {folderId}"
   Look for: "Folder '{name}' monitored: true/false"
   ```

4. **Event Triggering**
   ```
   Look for: "Triggering email change notification for message {id} in folder '{name}'"
   ```

### Database/Store Verification

1. Check OutlookAttributes in KeyValueStore:
   ```json
   {
     "email": "user@company.com",
     "allowMailAlert": true,
     "monitoredFolders": ["Krista Inbox", "Action Items"]
   }
   ```

### Microsoft Graph Subscription Verification

1. Use Microsoft Graph Explorer or API to check subscription:
   ```
   GET https://graph.microsoft.com/v1.0/subscriptions
   ```

2. Verify subscription properties:
   - `changeType`: "created,updated"
   - `resource`: "/users/{email}/messages"
   - `notificationUrl`: Your webhook URL
   - `expirationDateTime`: Within 72 hours

---

## Troubleshooting

### Issue: Notifications Not Received

**Possible Causes**:
1. Subscription not created/expired
2. Webhook URL not accessible
3. Mail alerts disabled

**Solutions**:
1. Check subscription status in Microsoft Graph
2. Verify routing URL is publicly accessible
3. Ensure `Allow Mail Alert = true`
4. Check firewall/network settings

### Issue: Moved Emails Not Detected

**Possible Causes**:
1. Only subscribed to "created" events (old code)
2. Folder not in monitored list

**Solutions**:
1. Verify subscription includes "updated" changeType
2. Check monitored folders configuration
3. Review logs for folder filtering

### Issue: Wrong Folder Detected

**Possible Causes**:
1. Folder name mismatch (case-sensitive)
2. Nested folder path issue

**Solutions**:
1. Use exact folder names from "List All Folders"
2. For nested folders, use full path: "Parent/Child"

---

## Performance Considerations

1. **Subscription Renewal**: Automatically renewed every 25 hours
2. **Notification Processing**: Asynchronous, non-blocking
3. **Duplicate Detection**: In-memory cache (1000 message IDs)
4. **Folder Lookup**: Cached in notification processing

---

## Next Steps After Testing

1. **Production Deployment**
   - Verify all tests pass
   - Configure production monitored folders
   - Set up monitoring/alerting

2. **Workflow Integration**
   - Create workflows using `emailChangeNotification` event
   - Access notification fields in workflow logic
   - Implement business rules based on folder

3. **Documentation**
   - Update user documentation
   - Create workflow examples
   - Document folder naming conventions

