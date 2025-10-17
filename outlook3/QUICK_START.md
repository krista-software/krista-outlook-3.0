# Quick Start Guide: Email Folder Monitoring

## What's New?

Krista can now detect when emails:
- ✅ Arrive in specific folders
- ✅ Are moved into specific folders (manually or by rules)
- ✅ Trigger workflows based on folder location

## 5-Minute Setup

### Step 1: Configure Outlook (if not already done)
```
Catalog Request: Save Outlook Public Configuration
  Email: your-email@company.com
  Allow Mail Alert: true
```

### Step 2: See Available Folders
```
Catalog Request: List All Folders
```
**Result**: Shows all your Outlook folders

### Step 3: Set Monitored Folders
```
Catalog Request: Set Monitored Folders
  Folder Names: "Krista Inbox, Action Items"
```
**Result**: Krista will now monitor these 2 folders

### Step 4: Create Workflow with Wait-for-Event
```
Event Name: emailChangeNotification
Event Data:
  - messageId
  - folderName
  - subject
  - from
  - body
  - changeType (created or updated)
```

### Step 5: Test It!
1. Send email to your Outlook
2. Move it to "Krista Inbox" folder
3. Watch your workflow trigger! 🎉

---

## Common Use Cases

### Use Case 1: Inbox Triage
**Scenario**: Automatically process emails in "Krista Inbox"

**Setup**:
```
Monitored Folders: "Krista Inbox"
Workflow: Extract task from email → Create ticket
```

**How It Works**:
1. User moves email to "Krista Inbox"
2. Krista detects the move
3. Extracts requirements from email body
4. Creates task automatically

---

### Use Case 2: Escalation Workflow
**Scenario**: Human review needed for certain emails

**Setup**:
```
Monitored Folders: "Need Human Review"
Workflow: Send notification → Create review ticket
```

**How It Works**:
1. Outlook rule moves urgent emails to "Need Human Review"
2. Krista detects the move
3. Sends notification to team
4. Creates review ticket with email details

---

### Use Case 3: Multi-Folder Routing
**Scenario**: Different workflows for different folders

**Setup**:
```
Monitored Folders: "Krista Inbox, Action Items, Completed"
Workflow: 
  IF folderName = "Krista Inbox" THEN process_new_task()
  ELSE IF folderName = "Action Items" THEN escalate()
  ELSE IF folderName = "Completed" THEN archive()
```

---

## Event Data Reference

When email arrives/moves to monitored folder, you get:

| Field | Type | Example | Description |
|-------|------|---------|-------------|
| messageId | Text | "AAMkAG..." | Unique email ID |
| changeType | Text | "created" or "updated" | How email got there |
| folderName | Text | "Krista Inbox" | Folder display name |
| folderId | Text | "AAMkAD..." | Folder unique ID |
| subject | Text | "Please review" | Email subject |
| from | Text | "user@example.com" | Sender email |
| to | Text | "you@company.com" | Recipients |
| cc | Text | "team@company.com" | CC recipients |
| bcc | Text | "" | BCC recipients |
| body | Text | "Email content..." | Email body (HTML) |
| attachments | Text | "true" or "false" | Has attachments? |

---

## FAQ

### Q: What if I don't set monitored folders?
**A**: Krista monitors ALL folders (backward compatible)

### Q: How do I monitor nested folders?
**A**: Use full path: "Inbox/Subfolder"

### Q: Are folder names case-sensitive?
**A**: No, "Krista Inbox" = "krista inbox"

### Q: Can I use Outlook rules?
**A**: Yes! Rules work perfectly with this feature

### Q: What's the difference between "created" and "updated"?
**A**: 
- `created` = Email newly arrived in folder
- `updated` = Email moved into folder

### Q: How fast are notifications?
**A**: Usually 1-3 seconds after email arrives/moves

### Q: Can I monitor the same folder in multiple workflows?
**A**: Yes, all workflows listening to `emailChangeNotification` will trigger

---

## Troubleshooting

### Problem: Not receiving notifications

**Check**:
1. ✅ Mail alerts enabled? (`Allow Mail Alert = true`)
2. ✅ Folder name spelled correctly?
3. ✅ Subscription active? (auto-renews every 25 hours)

**Solution**: Run "Health Check" catalog request

---

### Problem: Notifications for wrong folder

**Check**:
1. ✅ Folder name matches exactly?
2. ✅ Using full path for nested folders?

**Solution**: Run "List All Folders" to see exact names

---

### Problem: Duplicate notifications

**Don't worry!** Krista automatically filters duplicates.

---

## Advanced Configuration

### Monitor All Folders
```
Catalog Request: Set Monitored Folders
  Folder Names: (leave empty)
```

### Monitor Multiple Folders
```
Catalog Request: Set Monitored Folders
  Folder Names: "Folder1, Folder2, Folder3"
```

### Check Current Configuration
```
Catalog Request: Get Monitored Folders
```

---

## Example Workflow

```yaml
Name: Process Krista Inbox Emails
Trigger: Wait-for-Event
  Event: emailChangeNotification
  
Steps:
  1. Check if folderName = "Krista Inbox"
  2. Extract subject and body
  3. Parse requirements using AI
  4. Create task in project management system
  5. Move email to "Processed" folder
  6. Send confirmation to sender
```

---

## Best Practices

1. **Use Descriptive Folder Names**: "Krista Inbox" better than "Inbox2"
2. **Test with One Folder First**: Start simple, add more later
3. **Use Outlook Rules**: Automate email routing
4. **Monitor Workflow Logs**: Check for errors
5. **Keep Folder List Updated**: Remove unused folders

---

## Next Steps

1. ✅ Complete 5-minute setup above
2. ✅ Test with one email
3. ✅ Create your first workflow
4. ✅ Add more folders as needed
5. ✅ Share with your team!

---

## Resources

- **Full Testing Guide**: See `TESTING_GUIDE.md`
- **Implementation Details**: See `IMPLEMENTATION_SUMMARY.md`
- **Jira Ticket**: KE-2601

---

## Support

Need help? Check:
1. Logs in Krista platform
2. Microsoft Graph subscription status
3. Outlook folder permissions

---

**Happy Automating! 🚀**

