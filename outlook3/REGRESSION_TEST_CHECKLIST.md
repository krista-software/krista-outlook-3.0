# Regression Test Checklist - Email Folder Monitoring

## 📋 Purpose

This checklist ensures that the new Email Folder Monitoring feature (KE-2601) does **NOT** break existing functionality.

**Test Environment:** Before deploying to production, run these tests in a non-production environment.

---

## ✅ Pre-Deployment Checklist

### 1. Build Verification

- [ ] **Build Status:** `./gradlew clean build` completes successfully
- [ ] **Test Results:** All 284 tests pass
- [ ] **No Compilation Errors:** Zero errors in build output
- [ ] **JAR File Created:** `outlook3/build/libs/Outlook-3.0.16.jar` exists

**Command:**
```bash
cd outlook3 && ./gradlew clean build
```

**Expected Output:**
```
BUILD SUCCESSFUL in XXs
14 actionable tasks: XX executed, XX up-to-date
```

---

### 2. Code Review Verification

- [ ] **Original Endpoint Unchanged:** `/mailNotification` logic is simple (no folder filtering)
- [ ] **Original Event Unchanged:** `MAIL_RECEIVED` event still triggered
- [ ] **Original Catalog Method Unchanged:** "Mail Received Alert" code is identical
- [ ] **Original Subscription Unchanged:** `MailSubscription.java` not modified
- [ ] **New Files Only:** `FolderMonitoringSubscription.java` is a new file (not modifying existing)

**Files to Review:**
```
✅ outlook3/src/main/java/app/krista/extensions/essentials/collaboration/outlook3/api/OutlookApiResource.java
   - Lines 247-280: /mailNotification endpoint (simple, unchanged logic)
   
✅ outlook3/src/main/java/app/krista/extensions/essentials/collaboration/outlook3/catalog/MessagingArea.java
   - Lines 1402-1455: "Mail Received Alert" method (unchanged)
   
✅ outlook3/src/main/java/app/krista/extensions/essentials/collaboration/outlook3/impl/MailSubscription.java
   - Entire file: No modifications
```

---

## 🧪 Regression Tests

### Test Group 1: Original "Mail Received Alert" Functionality

#### Test 1.1: Basic Inbox Monitoring

**Preconditions:**
- Outlook extension deployed
- User authenticated
- Original subscription active (auto-created on first use)

**Test Steps:**
1. Send an email to the user's Inbox
2. Wait 5-10 seconds for notification
3. Check Krista logs for `MAIL_RECEIVED` event

**Expected Results:**
- ✅ `MAIL_RECEIVED` event is triggered
- ✅ Event payload contains `messageId`
- ✅ "Mail Received Alert" catalog method receives event
- ✅ MailDetails entity is returned with email data

**Pass Criteria:** Same behavior as before the changes

---

#### Test 1.2: Mail Received Alert Workflow

**Preconditions:**
- Existing workflow using "Mail Received Alert" catalog request

**Test Steps:**
1. Trigger the workflow (send email to Inbox)
2. Verify workflow executes
3. Check workflow output

**Expected Results:**
- ✅ Workflow triggers automatically
- ✅ Email details are retrieved correctly
- ✅ Workflow completes without errors
- ✅ Same behavior as before deployment

**Pass Criteria:** Workflow works exactly as before

---

#### Test 1.3: Subscription Auto-Renewal

**Preconditions:**
- Original subscription exists and is active

**Test Steps:**
1. Check subscription expiration time
2. Wait for auto-renewal (or trigger manually)
3. Verify subscription is renewed

**Expected Results:**
- ✅ Subscription renews automatically
- ✅ No errors in logs
- ✅ Notifications continue to work

**Pass Criteria:** Auto-renewal works as before

---

### Test Group 2: Coexistence Testing

#### Test 2.1: Both Subscriptions Active

**Preconditions:**
- Original subscription active
- Folder monitoring subscription enabled

**Test Steps:**
1. Send email to Inbox
2. Check which events are triggered

**Expected Results:**
- ✅ `MAIL_RECEIVED` event is triggered (original)
- ✅ If Inbox is in monitored folders: `EMAIL_CHANGE_NOTIFICATION` also triggered
- ✅ Both events can be processed independently
- ✅ No conflicts or errors

**Pass Criteria:** Both systems work independently

---

#### Test 2.2: Disable Folder Monitoring

**Preconditions:**
- Both subscriptions active

**Test Steps:**
1. Delete folder monitoring subscription
2. Send email to Inbox
3. Verify only original event triggers

**Expected Results:**
- ✅ `MAIL_RECEIVED` event still triggers
- ✅ `EMAIL_CHANGE_NOTIFICATION` does NOT trigger
- ✅ Original functionality unaffected

**Pass Criteria:** Disabling new feature doesn't break original

---

### Test Group 3: New Feature Isolation

#### Test 3.1: New Feature Not Enabled

**Preconditions:**
- Folder monitoring subscription NOT created
- Only original subscription active

**Test Steps:**
1. Send email to any folder
2. Check events triggered

**Expected Results:**
- ✅ Only `MAIL_RECEIVED` event triggers (for Inbox emails)
- ✅ No `EMAIL_CHANGE_NOTIFICATION` events
- ✅ No errors in logs

**Pass Criteria:** New feature is truly opt-in

---

#### Test 3.2: New Feature Enabled

**Preconditions:**
- Monitored folders configured: "Krista Inbox"
- Folder monitoring subscription enabled

**Test Steps:**
1. Send email to "Krista Inbox"
2. Send email to "Inbox" (not monitored)
3. Check events triggered

**Expected Results:**
- ✅ Email to "Krista Inbox": `EMAIL_CHANGE_NOTIFICATION` triggered
- ✅ Email to "Inbox": Only `MAIL_RECEIVED` triggered (original behavior)
- ✅ Filtering works correctly

**Pass Criteria:** New feature works as designed without affecting original

---

## 🔍 Azure Configuration Verification

### Azure App Registration

- [ ] **Original Endpoint Registered:** `/rest/outlook/mailNotification` exists in redirect URIs
- [ ] **New Endpoint Registered:** `/rest/outlook/folderMonitoringNotification` added to redirect URIs
- [ ] **Permissions Unchanged:** No new permissions required
- [ ] **Admin Consent:** Still valid (if using Private auth)

**How to Verify:**
1. Go to Azure Portal → App Registrations
2. Select your Outlook app
3. Click "Authentication"
4. Check "Redirect URIs" section

**Expected:**
```
✅ https://your-domain.com/rest/outlook/mailNotification
✅ https://your-domain.com/rest/outlook/folderMonitoringNotification
```

---

## 📊 Performance Testing

### Test 4.1: Notification Processing Time

**Test Steps:**
1. Send 10 emails to Inbox
2. Measure time from email arrival to event trigger

**Expected Results:**
- ✅ Processing time similar to before (< 5 seconds)
- ✅ No performance degradation

---

### Test 4.2: Subscription Management Overhead

**Test Steps:**
1. Check subscription creation time
2. Check subscription renewal time

**Expected Results:**
- ✅ Original subscription: Same performance as before
- ✅ Folder monitoring subscription: Similar performance
- ✅ No blocking or delays

---

## 🚨 Error Scenarios

### Test 5.1: Invalid Folder Name

**Test Steps:**
1. Configure monitored folders: "NonExistentFolder"
2. Send email to Inbox
3. Check logs

**Expected Results:**
- ✅ No errors or crashes
- ✅ Original functionality still works
- ✅ Graceful handling of invalid folder

---

### Test 5.2: Subscription Failure

**Test Steps:**
1. Simulate subscription creation failure (e.g., network issue)
2. Send email to Inbox
3. Verify original subscription still works

**Expected Results:**
- ✅ Original subscription unaffected
- ✅ Emails still trigger `MAIL_RECEIVED` event
- ✅ Error logged but no crash

---

## 📝 Logging Verification

### Log Entries to Check

**Original Functionality:**
```
✅ "Processing mail notification for subscription: ..."
✅ "Triggering mailReceived event for message: ..."
✅ "Mail subscription renewed successfully"
```

**New Functionality (if enabled):**
```
✅ "Processing folder monitoring notification for subscription: ..."
✅ "Triggering email change notification for message ... in folder '...'"
✅ "Folder monitoring subscription enabled successfully"
```

**No Errors:**
```
❌ No "NullPointerException" errors
❌ No "ClassCastException" errors
❌ No "Subscription failed" errors (unless expected)
```

---

## ✅ Final Checklist

Before marking deployment as successful:

- [ ] All regression tests pass
- [ ] Original "Mail Received Alert" workflows work unchanged
- [ ] New feature can be enabled/disabled independently
- [ ] No errors in production logs
- [ ] Performance is acceptable
- [ ] Azure configuration is correct
- [ ] Documentation is available to users

---

## 🎯 Success Criteria

**Deployment is successful if:**

1. ✅ **Zero Regression:** All existing workflows work exactly as before
2. ✅ **New Feature Works:** Folder monitoring can be enabled and works correctly
3. ✅ **Isolation:** Disabling new feature doesn't affect original functionality
4. ✅ **Performance:** No degradation in notification processing time
5. ✅ **Stability:** No new errors or crashes

---

## 🔄 Rollback Plan

If regression is detected:

### Option 1: Disable New Feature Only
```
1. Delete folder monitoring subscription (via catalog request)
2. Original functionality continues to work
3. No code rollback needed
```

### Option 2: Full Rollback
```
1. Redeploy previous version of Outlook extension
2. Remove new endpoint from Azure redirect URIs
3. Notify users of rollback
```

---

## 📞 Support

If issues are found during testing:

1. **Check logs** for specific error messages
2. **Review this checklist** to identify which test failed
3. **Consult FOLDER_MONITORING_SETUP_GUIDE.md** for troubleshooting
4. **Consult TECHNICAL_IMPLEMENTATION_SUMMARY.md** for architecture details

---

## 🎉 Sign-Off

**Tested By:** ___________________________

**Date:** ___________________________

**Environment:** ___________________________

**Result:** ⬜ PASS  ⬜ FAIL

**Notes:**
```
[Add any observations or issues found during testing]
```

---

**Remember:** The goal is **zero regression**. If any original functionality is broken, do NOT deploy to production.

