# Implementation Summary: Email Folder Monitoring Feature

## Jira Ticket
**KE-2601**: Expand Outlook Integration to Notify Krista When Emails Arrive in or Are Moved Into Specific Folders

## Overview
This implementation adds support for detecting when emails arrive in or are moved into specific Outlook folders, enabling folder-based workflow automation.

---

## Changes Made

### 1. Constants.java
**File**: `outlook3/src/main/java/app/krista/extensions/essentials/collaboration/outlook3/impl/util/Constants.java`

**Added Constants**:
```java
public static final String UPDATED = "updated";
public static final String CREATED_AND_UPDATED = "created,updated";
public static final String ME_MESSAGES = "/messages";
public static final String EMAIL_CHANGE_NOTIFICATION = "emailChangeNotification";
public static final String CHANGE_TYPE = "changeType";
public static final String NOTIFICATION_ID = "notificationId";
public static final String FOLDER_ID = "folderId";
public static final String FOLDER_NAME = "folderName";
public static final String SUBJECT = "subject";
public static final String FROM = "from";
public static final String TO = "to";
public static final String CC = "cc";
public static final String BCC = "bcc";
public static final String BODY = "body";
public static final String ATTACHMENTS = "attachments";
public static final String PARENT_FOLDER_ID = "parentFolderId";
public static final String MONITORED_FOLDERS = "monitoredFolders";
```

**Purpose**: Support new change types, event names, and notification fields

---

### 2. MailSubscription.java
**File**: `outlook3/src/main/java/app/krista/extensions/essentials/collaboration/outlook3/impl/MailSubscription.java`

**Changes**:
```java
// OLD:
subscription.changeType = Constants.CREATED;
subscription.resource = "/users/" + alertUserMailId + Constants.ME_MAIL_FOLDERS_INBOX_MESSAGES;

// NEW:
subscription.changeType = Constants.CREATED_AND_UPDATED;
subscription.resource = "/users/" + alertUserMailId + Constants.ME_MESSAGES;
```

**Purpose**: 
- Subscribe to both "created" and "updated" events
- Monitor all messages instead of just Inbox
- Detect emails moved by rules or manually

---

### 3. OutlookAttributes.java
**File**: `outlook3/src/main/java/app/krista/extensions/essentials/collaboration/outlook3/OutlookAttributes.java`

**Added Field**:
```java
@SerializedName(MONITORED_FOLDERS)
private final List<String> monitoredFolders;
```

**New Constructor**:
```java
public OutlookAttributes(String clientId, String clientSecret, String tenantId, 
                         String email, boolean allowMailAlert, String authType, 
                         String routingUrl, List<String> monitoredFolders)
```

**New Method**:
```java
public List<String> getMonitoredFolders()
```

**Purpose**: Store and retrieve list of folders to monitor

---

### 4. OutlookAttributeStore.java
**File**: `outlook3/src/main/java/app/krista/extensions/essentials/collaboration/outlook3/impl/stores/OutlookAttributeStore.java`

**Changes**:
- Updated `load()` method to extract monitored folders from stored attributes
- Handles backward compatibility (empty list if not present)

**Purpose**: Persist monitored folders configuration

---

### 5. OutlookApiResource.java
**File**: `outlook3/src/main/java/app/krista/extensions/essentials/collaboration/outlook3/api/OutlookApiResource.java`

**Major Changes**:

#### a. Enhanced Notification Handler
```java
@POST
@Path("/mailNotification")
public Response subscriptionNotification(JsonObject notification)
```

**New Behavior**:
- Extracts changeType and subscriptionId from notification
- Calls new `processEmailNotification()` method
- Maintains backward compatibility

#### b. New Method: processEmailNotification()
```java
private void processEmailNotification(String messageId, String changeType, 
                                       String subscriptionId, int notificationId)
```

**Functionality**:
1. Fetches full message details from Microsoft Graph
2. Extracts `parentFolderId` to determine current folder
3. Gets folder name using `account.getFolder(folderId)`
4. Checks if folder is in monitored list
5. Filters notifications based on monitored folders
6. Triggers event only for monitored folders

#### c. New Method: buildEmailNotificationPayload()
```java
private FreeForm buildEmailNotificationPayload(Message message, String changeType, 
                                                String subscriptionId, int notificationId, 
                                                String folderName, String folderId)
```

**Functionality**:
- Builds comprehensive event payload with all required fields
- Extracts: subject, from, to, cc, bcc, body, attachments
- Includes folder information
- Returns FreeForm object for event handler

**New Event Triggered**:
- Event Name: `emailChangeNotification`
- Legacy Event: `mailReceived` (for backward compatibility)

---

### 6. SetupArea.java
**File**: `outlook3/src/main/java/app/krista/extensions/essentials/collaboration/outlook3/catalog/SetupArea.java`

**New Catalog Requests**:

#### a. Set Monitored Folders
```java
@CatalogRequest(name = "Set Monitored Folders")
public ExtensionResponse setMonitoredFolders(String folderNames)
```

**Input**: Comma-separated folder names (e.g., "Krista Inbox, Action Items")
**Output**: Success/failure status
**Purpose**: Configure which folders to monitor

#### b. Get Monitored Folders
```java
@CatalogRequest(name = "Get Monitored Folders")
public ExtensionResponse getMonitoredFolders()
```

**Output**: List of currently monitored folders
**Purpose**: View current configuration

#### c. List All Folders
```java
@CatalogRequest(name = "List All Folders")
public ExtensionResponse listAllFolders()
```

**Output**: All available Outlook folders
**Purpose**: Help users identify folder names for configuration

---

## Event Payload Structure

### New Event: emailChangeNotification

**Fields**:
```javascript
{
  "notificationId": "0",           // Notification index
  "subscriptionId": "abc123...",   // Microsoft Graph subscription ID
  "changeType": "created|updated", // Type of change
  "messageId": "AAMkAG...",        // Email message ID
  "folderId": "AAMkAD...",         // Parent folder ID
  "folderName": "Krista Inbox",    // Folder display name
  "subject": "Email subject",      // Email subject
  "from": "sender@example.com",    // Sender email
  "to": "recipient@example.com",   // To recipients (comma-separated)
  "cc": "cc@example.com",          // CC recipients (comma-separated)
  "bcc": "bcc@example.com",        // BCC recipients (comma-separated)
  "body": "Email body content",    // Email body (HTML or text)
  "attachments": "true|false"      // Has attachments flag
}
```

---

## Workflow Integration

### Wait-for-Event Configuration

```yaml
Event Name: emailChangeNotification
Event Data Fields:
  - messageId (Text)
  - changeType (Text)
  - folderId (Text)
  - folderName (Text)
  - subject (Text)
  - from (Text)
  - to (Text)
  - cc (Text)
  - bcc (Text)
  - body (Text)
  - attachments (Text)
```

### Example Workflow Logic

```
IF folderName = "Krista Inbox" THEN
  // Process as Krista task
  Extract requirements from body
  Create task in system
ELSE IF folderName = "Need Human Review" THEN
  // Escalate to human
  Send notification to team
  Create review ticket
END IF
```

---

## Backward Compatibility

1. **Empty Monitored Folders**: If no folders configured, monitors ALL folders (original behavior)
2. **Legacy Event**: Still triggers `mailReceived` event with messageId
3. **Existing Workflows**: Continue to work without changes
4. **Configuration**: Existing configurations work without modification

---

## Microsoft Graph API Details

### Subscription Resource
- **Old**: `/users/{email}/mailFolders('Inbox')/messages`
- **New**: `/users/{email}/messages`

### Change Types
- **Old**: `created`
- **New**: `created,updated`

### Notification Payload
Microsoft Graph sends:
```json
{
  "value": [{
    "subscriptionId": "...",
    "changeType": "created|updated",
    "resource": "Users/{email}/Messages/{messageId}",
    "resourceData": {
      "id": "{messageId}"
    }
  }]
}
```

### Message Properties Fetched
```
id, subject, body, bodyPreview, receivedDateTime, sentDateTime,
from, toRecipients, ccRecipients, bccRecipients, replyTo,
isRead, hasAttachments, importance, conversationId, uniqueBody,
parentFolderId
```

---

## Configuration Steps

### 1. Initial Setup
```
1. Configure Outlook (Public or Private)
2. Enable Mail Alerts
3. Verify subscription created
```

### 2. Configure Monitored Folders
```
Catalog Request: Set Monitored Folders
Input: "Krista Inbox, Action Items, Need Human Review"
```

### 3. Create Workflow
```
1. Add Wait-for-Event: emailChangeNotification
2. Access event data fields
3. Implement business logic based on folder
```

---

## Testing Checklist

- [ ] Email arrives in monitored folder (created event)
- [ ] Email moved to monitored folder manually (updated event)
- [ ] Email moved by Outlook rule (updated event)
- [ ] Email in non-monitored folder (filtered out)
- [ ] Empty monitored folders (all folders monitored)
- [ ] Duplicate notification handling
- [ ] Subscription renewal (every 25 hours)
- [ ] All event fields populated correctly

---

## Files Modified

1. `Constants.java` - Added new constants
2. `MailSubscription.java` - Updated subscription parameters
3. `OutlookAttributes.java` - Added monitored folders field
4. `OutlookAttributeStore.java` - Updated load/save logic
5. `OutlookApiResource.java` - Enhanced notification processing
6. `SetupArea.java` - Added folder management catalog requests

---

## Files Created

1. `TESTING_GUIDE.md` - Comprehensive testing instructions
2. `IMPLEMENTATION_SUMMARY.md` - This file

---

## Known Limitations

1. **Folder Name Matching**: Case-insensitive but must match exactly
2. **Nested Folders**: Use full path (e.g., "Inbox/Subfolder")
3. **Subscription Limit**: Microsoft Graph allows max 1000 subscriptions per mailbox
4. **Notification Delay**: Microsoft Graph may have 1-3 second delay
5. **Duplicate Detection**: In-memory cache (lost on restart)

---

## Future Enhancements

1. **Folder ID Support**: Allow monitoring by folder ID instead of name
2. **Regex Matching**: Support pattern matching for folder names
3. **Conditional Filtering**: Filter by subject, sender, etc.
4. **Batch Processing**: Handle multiple notifications more efficiently
5. **Persistent Duplicate Detection**: Use database instead of in-memory cache

---

## Support

For issues or questions:
1. Check logs for error messages
2. Verify Microsoft Graph subscription status
3. Review TESTING_GUIDE.md for troubleshooting
4. Contact Krista support team

