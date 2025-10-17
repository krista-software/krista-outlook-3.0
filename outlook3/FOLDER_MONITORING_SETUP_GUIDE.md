# Email Folder Monitoring - Complete Setup Guide

## 📋 Overview

This guide explains how to set up and use the **new Email Folder Monitoring feature** (Jira ticket KE-2601) without affecting existing "Mail Received Alert" functionality.

### ✅ What's New

The Outlook extension now supports **TWO separate email notification systems**:

| Feature | Original (Unchanged) | New (Enhanced) |
|---------|---------------------|----------------|
| **Endpoint** | `/mailNotification` | `/folderMonitoringNotification` |
| **Event Name** | `mailReceived` | `emailChangeNotification` |
| **Catalog Method** | "Mail Received Alert" | "Email Folder Alert" |
| **Subscription** | Monitors Inbox only | Monitors all folders |
| **Change Types** | `created` only | `created,updated` |
| **Filtering** | None - all emails | Filters by monitored folders |
| **Payload** | Simple - just messageId | Rich - all metadata + folder info |
| **Use Case** | Basic inbox monitoring | Advanced folder-based workflows |

### 🔒 Zero Regression Guarantee

- ✅ **Existing workflows are NOT affected** - The original `/mailNotification` endpoint remains unchanged
- ✅ **Opt-in feature** - You must explicitly enable folder monitoring
- ✅ **Independent subscriptions** - Both can run simultaneously if needed
- ✅ **Backward compatible** - All existing "Mail Received Alert" workflows continue to work exactly as before

---

## 🚀 Quick Start

### Option 1: Use Original Mail Received Alert (No Changes Needed)

If you're already using "Mail Received Alert", **nothing changes**. Your existing setup continues to work:

1. Subscription monitors: **Inbox only**
2. Triggers on: **New emails (created)**
3. Event: **mailReceived**
4. Payload: **{ messageId: "..." }**

### Option 2: Enable New Email Folder Alert

To use the enhanced folder monitoring feature:

1. **Configure monitored folders** (via "Set Monitored Folders" catalog request)
2. **Enable folder monitoring subscription** (via "Enable Folder Monitoring" catalog request)
3. **Use "Email Folder Alert"** catalog request in your workflows
4. **Receive rich email data** including folder info, change type, subject, sender, body, attachments

---

## 📝 Step-by-Step Setup

### Step 1: Azure App Registration (NEW ENDPOINT)

Since we've added a new endpoint `/folderMonitoringNotification`, you need to register it in your Azure AD application.

#### 1.1 Navigate to Azure Portal

1. Go to [Azure Portal](https://portal.azure.com)
2. Navigate to **Azure Active Directory** → **App registrations**
3. Select your Outlook integration app (or the app you created for Krista)

#### 1.2 Add Redirect URI for New Endpoint

1. Click on **Authentication** in the left menu
2. Under **Redirect URIs**, add the new endpoint:
   ```
   https://your-krista-domain.com/rest/outlook/folderMonitoringNotification
   ```
   Replace `your-krista-domain.com` with your actual Krista appliance domain.

3. Click **Save**

#### 1.3 Verify API Permissions

Ensure your app has the following Microsoft Graph permissions:

**Delegated Permissions** (for Public auth):
- `Mail.ReadWrite`
- `Mail.Send`
- `MailboxSettings.ReadWrite`
- `offline_access`

**Application Permissions** (for Private auth):
- `Mail.ReadWrite`
- `Mail.Send`

#### 1.4 Grant Admin Consent

If using Private (service account) authentication:
1. Click **API permissions** in the left menu
2. Click **Grant admin consent for [Your Organization]**
3. Confirm the consent

---

### Step 2: Configure Monitored Folders in Krista

Use the **"Set Monitored Folders"** catalog request to specify which folders to monitor.

#### Example: Monitor "Krista Inbox" and "Action Items"

```json
{
  "Monitored Folders": "Krista Inbox, Action Items"
}
```

**Important Notes:**
- Folder names are **case-insensitive**
- Use **exact folder names** as they appear in Outlook
- Separate multiple folders with **commas**
- Leave **empty** to monitor ALL folders (not recommended for performance)

#### How to Get Available Folder Names

Use the **"List All Folders"** catalog request to see all available folders:

**Response Example:**
```
Available Folders: "Inbox, Sent Items, Drafts, Deleted Items, Krista Inbox, Action Items, Archive"
```

---

### Step 3: Enable Folder Monitoring Subscription

Use the **"Enable Folder Monitoring"** catalog request to create the subscription.

**What this does:**
- Creates a Microsoft Graph subscription for `/messages` (all folders)
- Monitors both `created` and `updated` events
- Sends notifications to `/folderMonitoringNotification` endpoint
- Subscription auto-renews every 3 days

**Response:**
```json
{
  "Is Successful": true,
  "Extension Response Meta": {
    "message": "Folder monitoring subscription enabled successfully",
    "responseType": "SUCCESS"
  }
}
```

---

### Step 4: Use "Email Folder Alert" in Workflows

Create a workflow that uses the **"Email Folder Alert"** catalog request.

#### Example Workflow

```
WHEN Email Folder Alert is triggered
  GET Email Details from event
  
  IF Email Details.folderName = "Krista Inbox"
    THEN Process as high priority
  ELSE IF Email Details.folderName = "Action Items"
    THEN Create task from email
  END IF
END WHEN
```

#### Available Fields in Email Details

The `Email Details` FreeForm contains:

| Field | Type | Description |
|-------|------|-------------|
| `messageId` | Text | Unique message ID |
| `subject` | Text | Email subject |
| `from` | Text | Sender email address |
| `to` | Text | Recipient email addresses |
| `cc` | Text | CC recipients |
| `bcc` | Text | BCC recipients |
| `body` | Text | Email body content |
| `attachments` | List | List of attachment names |
| `folderName` | Text | Name of the folder containing the email |
| `folderId` | Text | Unique folder ID |
| `changeType` | Text | "created" or "updated" |
| `notificationId` | Number | Notification sequence number |
| `subscriptionId` | Text | Subscription ID |

---

## 🧪 Testing Guide

### Test Scenario 1: Email Delivered to Monitored Folder

**Setup:**
- Monitored folders: "Krista Inbox"
- Folder monitoring: Enabled

**Test Steps:**
1. Send an email directly to "Krista Inbox" folder (using Outlook rules)
2. Verify "Email Folder Alert" is triggered
3. Check that `changeType = "created"`
4. Check that `folderName = "Krista Inbox"`

**Expected Result:** ✅ Alert triggered with full email details

---

### Test Scenario 2: Email Moved into Monitored Folder

**Setup:**
- Monitored folders: "Action Items"
- Folder monitoring: Enabled

**Test Steps:**
1. Receive an email in Inbox
2. Manually move the email to "Action Items" folder
3. Verify "Email Folder Alert" is triggered
4. Check that `changeType = "updated"`
5. Check that `folderName = "Action Items"`

**Expected Result:** ✅ Alert triggered when email is moved

---

### Test Scenario 3: Email in Non-Monitored Folder

**Setup:**
- Monitored folders: "Krista Inbox"
- Folder monitoring: Enabled

**Test Steps:**
1. Send an email to "Inbox" (not "Krista Inbox")
2. Verify "Email Folder Alert" is **NOT** triggered

**Expected Result:** ✅ No alert (email not in monitored folder)

---

### Test Scenario 4: Backward Compatibility Check

**Setup:**
- Original "Mail Received Alert" workflow exists
- Folder monitoring: Enabled

**Test Steps:**
1. Send an email to Inbox
2. Verify "Mail Received Alert" is **still triggered**
3. Verify it receives `messageId` as before

**Expected Result:** ✅ Original workflow continues to work

---

## 🔧 Troubleshooting

### Issue: Folder monitoring alerts not triggering

**Possible Causes:**
1. Subscription not created - Run "Enable Folder Monitoring"
2. Folder name mismatch - Use "List All Folders" to verify exact names
3. Azure redirect URI not configured - Add `/folderMonitoringNotification` endpoint

**Solution:**
```
1. Check logs for subscription creation errors
2. Verify monitored folder names match exactly
3. Confirm Azure app registration includes new endpoint
```

---

### Issue: Original "Mail Received Alert" stopped working

**This should NOT happen** - the original endpoint is unchanged.

**If it does:**
1. Check that original subscription still exists (it should auto-renew)
2. Verify `/mailNotification` endpoint is accessible
3. Check Azure app registration for `/mailNotification` redirect URI

---

### Issue: Getting duplicate alerts

**Cause:** Both subscriptions are active and monitoring the same folder.

**Solution:**
- Use **either** "Mail Received Alert" **or** "Email Folder Alert", not both for the same use case
- If you need both, use different folders or different logic to handle duplicates

---

## 📊 Comparison Matrix

### When to Use Which Feature

| Use Case | Recommended Feature |
|----------|-------------------|
| Monitor all emails in Inbox | **Mail Received Alert** (original) |
| Monitor specific custom folders | **Email Folder Alert** (new) |
| Detect emails moved by rules | **Email Folder Alert** (new) |
| Need full email metadata immediately | **Email Folder Alert** (new) |
| Simple notification with messageId only | **Mail Received Alert** (original) |
| Existing workflows (don't change) | **Mail Received Alert** (original) |
| Folder-based routing/workflows | **Email Folder Alert** (new) |

---

## 🎯 Best Practices

### 1. Start with Specific Folders

❌ **Don't:** Monitor all folders (leave monitored folders empty)
```
Monitored Folders: ""  // Will monitor EVERY folder - performance impact!
```

✅ **Do:** Monitor only the folders you need
```
Monitored Folders: "Krista Inbox, Action Items, High Priority"
```

### 2. Use Folder Names for Routing

```
IF folderName = "High Priority"
  THEN Escalate to manager
ELSE IF folderName = "Action Items"
  THEN Create task
ELSE IF folderName = "Archive"
  THEN Log for compliance
END IF
```

### 3. Handle Both Created and Updated Events

```
IF changeType = "created"
  THEN Log "New email arrived in " + folderName
ELSE IF changeType = "updated"
  THEN Log "Email moved to " + folderName
END IF
```

### 4. Keep Original Workflows Unchanged

- Don't modify existing "Mail Received Alert" workflows
- Create **new** workflows for "Email Folder Alert"
- Test new workflows in non-production first

---

## 📞 Support

If you encounter issues:

1. **Check logs** - Look for errors in Krista logs related to subscriptions
2. **Verify Azure setup** - Ensure new endpoint is registered
3. **Test incrementally** - Start with one folder, then add more
4. **Review this guide** - Ensure all steps were followed

---

## 🎉 Summary

You now have **two independent email notification systems**:

1. **Original "Mail Received Alert"** - Simple, reliable, unchanged
2. **New "Email Folder Alert"** - Advanced, folder-aware, feature-rich

Choose the right tool for your use case, and enjoy zero-regression deployment! 🚀

