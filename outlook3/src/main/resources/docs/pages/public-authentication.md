# Public Authentication Guide

This guide provides detailed information about setting up and using Public Authentication with the Krista Outlook Extension.

## What is Public Authentication?

Public Authentication uses Microsoft's standard OAuth 2.0 process with Krista's pre-registered application. This is the simplest method for most users and leverages Krista's existing Microsoft partnership for seamless integration.

## How Public Authentication Works

### OAuth 2.0 Flow
Public Authentication uses the **Authorization Code Grant** flow through Krista's pre-registered Microsoft application:

```
Grant Type: authorization_code
Client: Krista's Pre-registered Application
Flow: Authorization Code with Microsoft's standard endpoints
Authorization Endpoint: https://login.microsoftonline.com/common/oauth2/v2.0/authorize
Token Endpoint: https://login.microsoftonline.com/common/oauth2/v2.0/token
```

### Step-by-Step Process
1. **User Input**: You enter your actual Outlook email address in Krista
2. **Initiation**: You click "Connect to Outlook" in Krista
3. **Redirect**: Microsoft redirects you to their secure login page
4. **Authentication**: You log in with your actual Outlook credentials
5. **Consent**: Microsoft shows "Krista Email Automation" requesting permissions
6. **Authorization**: You approve permissions for Krista to access your email
7. **Token Exchange**: Krista receives secure tokens to access your emails
8. **Auto-Refresh**: Tokens automatically refresh to maintain connection

## Email Address Requirements

### What Email to Use
**Enter YOUR actual Outlook email address** - the email account you want to automate:

```
✅ Correct Examples:
john.doe@company.com (your work email)
mary.smith@outlook.com (your personal Outlook email)
sarah@university.edu (your school email)
alex.jones@hotmail.com (your personal email)

❌ Incorrect Examples:
support@krista.ai (Krista's email, not yours)
admin@krista.com (any Krista email address)
```

### Supported Email Types
- **Microsoft 365 Business/Enterprise accounts**
- **Outlook.com personal accounts**
- **Hotmail.com accounts**
- **Live.com accounts**
- **Educational accounts (.edu domains with Microsoft 365)**
- **Any email hosted on Microsoft Exchange Online**

## Setup Process

### Step 1: Access Krista Extension
1. Log into your Krista platform
2. Navigate to **Extensions** → **Outlook Extension**
3. Click **"Add New Connection"** or **"Configure"**

### Step 2: Choose Public Authentication
1. Select **"Public Authentication"** radio button
2. Enter your Outlook email address in the Email field
3. Optionally check "Allow Mail Alert" if you want notifications
4. Click **"Test Connection"**

### Step 3: Microsoft Authorization
1. **Redirect to Microsoft**
   - You'll be taken to login.microsoftonline.com
   - URL shows it's for "Krista Email Automation"
   - This is normal and expected

2. **Microsoft Login**
   - Enter the SAME email address you entered in Krista
   - Enter your actual Outlook password
   - Complete two-factor authentication if enabled

3. **Permission Review**
   Microsoft shows a consent screen:
   ```
   Krista Email Automation wants to:
   ✓ Read your mail
   ✓ Send mail on your behalf  
   ✓ Access your profile information
   ✓ Maintain access to data you have given it access to
   ```

4. **Grant Permission**
   - Review each permission carefully
   - Click **"Accept"** to allow Krista to automate your email
   - Click **"Cancel"** to stop the process

5. **Confirmation**
   - You're redirected back to Krista
   - See "Connection Successful" message
   - Click **"Save Changes"** to complete setup

## Understanding the Permissions

### Required Permissions
| Permission | Purpose | Why Needed |
|------------|---------|------------|
| `Mail.Read` | Read emails from your mailbox | Fetch emails for automation workflows |
| `Mail.Send` | Send emails on your behalf | Send automated responses and notifications |
| `User.Read` | Access your profile information | Verify identity and get basic account info |
| `offline_access` | Maintain access when you're offline | Keep automation running without re-login |

### What Krista Can and Cannot Do
**✅ What Krista CAN do:**
- Read emails in your mailbox
- Send emails from your account
- Access email metadata (subject, sender, date)
- Create and manage email folders
- Set email properties (read/unread, importance)

**❌ What Krista CANNOT do:**
- See or store your password
- Access other Microsoft services (OneDrive, Teams, etc.)
- Modify your account settings
- Access emails without your explicit permission
- Share your data with third parties

## Rate Limits and Quotas

### Microsoft Graph API Limits
With Public Authentication, you share API limits with other Krista users:

| Operation | Limit | Reset Period |
|-----------|-------|--------------|
| Email Reading | 1,000 emails/hour | Rolling hour |
| Email Sending | 100 emails/hour | Rolling hour |
| Real-time Subscriptions | 5 active subscriptions | Per account |
| Bulk Operations | 20 requests/minute | Rolling minute |

### Practical Impact
- **Light Usage**: Perfect for individual users and small teams
- **Moderate Usage**: Suitable for up to 1,000 emails per day
- **Heavy Usage**: May hit limits with high-volume processing

## Best Practices

### For Setup
- ✅ Use your primary work or personal email address
- ✅ Enable two-factor authentication on your Microsoft account
- ✅ Review permissions carefully before accepting
- ✅ Test with a few emails before full automation
- ✅ Keep your Krista account secure

### For Ongoing Use
- ✅ Monitor your email automation workflows
- ✅ Regularly review connected applications in Microsoft account settings
- ✅ Report any suspicious activity immediately
- ✅ Keep your email account information up to date
- ✅ Understand rate limits for your use case

## Security Features

### OAuth 2.0 Security
- **No Password Sharing**: Krista never sees your actual password
- **Limited Scope**: Only grants access to specific email functions
- **Revocable Access**: Remove permissions anytime through Microsoft settings
- **Encrypted Communication**: All data transfer uses HTTPS/TLS encryption
- **Token Expiration**: Access tokens expire and refresh automatically

### Data Protection
- **Minimal Data Storage**: Krista only stores necessary tokens and metadata
- **No Email Content Storage**: Email content is processed in real-time, not stored
- **Secure Token Storage**: All tokens are encrypted at rest
- **Audit Logging**: All access attempts are logged for security monitoring

## Troubleshooting Common Issues

### "Access Denied" Error
**Symptoms**: Cannot connect to Outlook, access denied message
**Causes**:
- Organization blocks third-party applications
- Account doesn't have necessary permissions
- Conditional access policies blocking connection

**Solutions**:
1. Check with IT department about third-party app policies
2. Try connecting from a different network
3. Ensure your account has valid Microsoft 365 license
4. Consider switching to Private Authentication if organizational policies are strict

### "Invalid Email" Error
**Symptoms**: Email address not accepted during setup
**Causes**:
- Email address not hosted on Microsoft platforms
- Typo in email address
- Account doesn't exist or is disabled

**Solutions**:
1. Verify email address spelling
2. Ensure email is hosted on Microsoft 365, Outlook.com, or Exchange Online
3. Test logging into outlook.office.com with the same email
4. Contact your email administrator if account issues persist

### "Connection Timeout" Error
**Symptoms**: Setup process hangs or times out
**Causes**:
- Network connectivity issues
- Firewall blocking Microsoft authentication endpoints
- Browser issues or extensions interfering

**Solutions**:
1. Check internet connection stability
2. Try different browser or incognito/private mode
3. Disable browser extensions temporarily
4. Ensure these domains are accessible:
   - login.microsoftonline.com
   - graph.microsoft.com
   - outlook.office.com

### "Token Expired" Error
**Symptoms**: Previously working connection stops working
**Causes**:
- Refresh token expired (rare, usually 90 days)
- Password changed on Microsoft account
- Account disabled or permissions revoked

**Solutions**:
1. Re-authenticate through Krista settings
2. Verify Microsoft account is still active
3. Check if password was recently changed
4. Review connected apps in Microsoft account settings

### Rate Limit Exceeded
**Symptoms**: Automation stops working, "too many requests" errors
**Causes**:
- Exceeded hourly email processing limits
- Too many concurrent operations
- Bulk operations hitting minute-based limits

**Solutions**:
1. Reduce frequency of email checks
2. Implement delays between bulk operations
3. Consider upgrading to Private Authentication for higher limits
4. Optimize workflows to process emails more efficiently

## When to Choose Public Authentication

### Ideal For:
- **Individual Users**: Personal email automation
- **Small Teams**: Under 50 users
- **Quick Setup**: Need to start immediately
- **Simple Requirements**: Basic email automation needs
- **No IT Restrictions**: Organization allows third-party apps

### Consider Private Authentication If:
- **Large Organization**: 50+ users
- **High Volume**: Processing 1000+ emails daily
- **Custom Branding**: Want organization name in consent screens
- **Strict Security**: Enterprise compliance requirements
- **IT Policies**: Organization requires custom app registrations

## Getting Help

### Self-Service Resources
1. **Test Your Connection**: Use Krista's built-in connection test
2. **Microsoft Account Settings**: Review connected apps at account.microsoft.com
3. **Browser Developer Tools**: Check for JavaScript errors during setup
4. **Network Diagnostics**: Verify connectivity to Microsoft endpoints

### Support Channels
- **Krista Support**: For integration and configuration questions
- **Microsoft Support**: For account and authentication issues
- **IT Department**: For organizational policy questions
- **Community Forums**: For user experiences and tips

Your Public Authentication setup provides a secure, easy-to-use foundation for email automation with Krista!