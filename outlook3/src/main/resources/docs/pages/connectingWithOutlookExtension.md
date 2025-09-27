# Connecting with Krista Outlook Extension

This guide will walk you through the process of connecting your Microsoft Outlook account to Krista, enabling powerful email automation capabilities.

## Before You Begin

### What You'll Need
- A Microsoft Outlook account (Office 365, Outlook.com, or Exchange Online)
- Administrative access to your Krista platform
- Basic understanding of your organization's email policies
- Approximately 15-30 minutes to complete the setup

### Important Considerations
- **Permissions**: You may need IT administrator approval for certain authentication methods
- **Security**: All connections use industry-standard OAuth 2.0 encryption
- **Access**: The extension will only access emails as configured in your automation workflows

## Connection Methods

The Krista Outlook Extension supports two authentication approaches:

### 1. Public Authentication (Recommended for Most Users)
- **Best for**: Individual users and small teams
- **Setup time**: 5-10 minutes
- **IT approval**: Usually not required
- **Limitations**: Standard Microsoft API rate limits apply

### 2. Private Authentication (Enterprise)
- **Best for**: Large organizations with custom requirements
- **Setup time**: 15-30 minutes
- **IT approval**: Required (Azure App Registration needed)
- **Benefits**: Higher API limits, custom branding, enhanced control

## Step-by-Step Connection Process

### Step 1: Access Krista Extension Settings

1. Log into your Krista platform
2. Navigate to **Extensions** → **Outlook Extension**
3. Click **"Add New Connection"** or **"Configure"**

### Step 2: Choose Authentication Method

**For Public Authentication:**
- Select **"Public Authentication"**
- Enter your Outlook email address
- Click **"Connect to Outlook"**

**For Private Authentication:**
- Select **"Private Authentication"**
- You'll need credentials from your IT administrator
- See [Obtaining Credentials Guide](obtainingClientIDClientSecret.md) for details

### Step 3: Microsoft Authorization

1. You'll be redirected to Microsoft's login page
2. Enter your Outlook credentials
3. Review the permissions requested:
   - **Read your mail**: Allows Krista to fetch and process emails
   - **Send mail as you**: Enables automated email sending
   - **Access your profile**: Verifies your identity
4. Click **"Accept"** to grant permissions

### Step 4: Verify Connection

1. You'll be redirected back to Krista
2. Look for the **"Connection Successful"** message
3. Your Outlook account is now linked to Krista

## Testing Your Connection

### Automatic Test
After connection, Krista automatically performs these checks:
- ✅ Authentication validity
- ✅ Email access permissions
- ✅ Send capabilities
- ✅ API connectivity

### Manual Test
You can also test your connection manually:

1. Go to **Extension Settings** → **Test Connection**
2. Click **"Run Test"**
3. Review the test results:
   - **Green checkmarks**: Everything is working correctly
   - **Yellow warnings**: Minor issues that may need attention
   - **Red errors**: Problems that require immediate action

## Common Connection Issues

### "Permission Denied" Error
**Cause**: Your organization may have restricted third-party app access
**Solution**: Contact your IT administrator to whitelist the Krista Outlook Extension

### "Invalid Credentials" Error
**Cause**: Incorrect email address or password
**Solution**: Double-check your login information and try again

### "Connection Timeout" Error
**Cause**: Network connectivity issues
**Solution**: Check your internet connection and try again in a few minutes

### "App Not Approved" Error
**Cause**: Your organization requires admin approval for new applications
**Solution**: Ask your IT administrator to approve the Krista Outlook Extension

## Security and Privacy

### What Data Does Krista Access?
- **Emails**: Only emails processed by your automation workflows
- **Profile Information**: Basic details like name and email address for verification
- **Send Permissions**: Ability to send emails on your behalf as configured

### What Data is NOT Accessed?
- Personal files or documents
- Calendar information (unless specifically configured)
- Contacts (unless specifically configured)
- Other Microsoft services

### Data Protection
- All data transmission uses TLS encryption
- Credentials are stored securely and encrypted
- Access tokens are automatically refreshed and rotated
- No passwords are stored by Krista

## Next Steps

Once your connection is established:

1. **Configure Email Automation**: Set up your first email workflow
2. **Review Supported Operations**: Learn about available email actions
3. **Set Up Monitoring**: Configure alerts and notifications
4. **Test Workflows**: Run test scenarios before going live

## Need Help?

- **Technical Issues**: Contact your Krista administrator
- **Microsoft Account Problems**: Visit Microsoft Support
- **Permission Questions**: Consult with your IT department
- **Workflow Design**: Refer to Krista documentation or training materials

Your Outlook Extension is now ready to power your email automation workflows!