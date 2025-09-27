# Connecting with Krista Outlook Extension

![Connection Overview](../_media/homePage.png)

This guide walks you through connecting your Outlook account to Krista for email automation. The process is straightforward and secure, taking just a few minutes to complete.

## Before You Begin

### What You'll Need

- **Active Outlook Account**: Microsoft 365, Outlook.com, or Exchange Online
- **Krista Platform Access**: Valid Krista account with appropriate permissions
- **Internet Connection**: Stable connection for authentication process
- **Modern Web Browser**: Chrome, Firefox, Safari, or Edge (latest versions)

### Choose Your Authentication Method

![Ask A System Checked](../_media/askASystemChecked.png)

**Public Authentication** - Quick 5-minute setup for individuals and small teams

![Ask A System Unchecked](../_media/askASystemUnchecked.png)

**Private Authentication** - Enterprise setup requiring IT involvement

**Not sure which to choose?** See our [Authentication Guide](authentication.md) for detailed comparison.

## Step-by-Step Connection Process

### Step 1: Access Krista Extension Settings

![Krista Extension Access](../_media/homePage.png)

1. **Log into Krista Platform**
   - Open your web browser
   - Navigate to your Krista instance URL
   - Sign in with your Krista credentials

2. **Navigate to Outlook Extension**
   - Go to **Extensions** in the main menu
   - Click **Outlook Extension**
   - Select **"Add New Connection"** or **"Configure"**

### Step 2: Choose Authentication Method

**For Public Authentication:**

![Public Auth Setup](../_media/public_auth.png)

- Select **"Public Authentication"**
- **Enter YOUR Outlook email address** (the email account you want to automate)
  
  **Examples of correct email addresses:**
  ```
  ✅ john.doe@company.com (your work email)
  ✅ mary.smith@outlook.com (your personal Outlook email)
  ✅ sarah@university.edu (your school email)
  ```
  
  **Important:** Do NOT enter Krista email addresses like support@krista.ai - enter the email account YOU want to automate.

- Click **"Connect to Outlook"**

**For Private Authentication:**

![Private Auth Setup](../_media/privateAuth.png)

- Select **"Private Authentication"**
- You'll need credentials from your IT administrator
- See [Obtaining Credentials Guide](obtainingClientIDClientSecret.md) for details

### Step 3: Microsoft Authorization (Public Authentication)

When you click "Connect to Outlook" with Public Authentication, here's exactly what happens:

![Using Email](../_media/usingEmail.png)

1. **Redirect to Microsoft**
   - You'll be taken to login.microsoftonline.com
   - The URL will show it's for "Krista Email Automation"
   - This is normal and expected

2. **Microsoft Login**
   - Enter the SAME email address you entered in Krista
   - Enter your actual Outlook password
   - Complete any two-factor authentication if enabled

3. **Permission Review Screen**

   Microsoft will show a screen like this:
   ```
   Krista Email Automation wants to:
   ✓ Read your mail
   ✓ Send mail on your behalf  
   ✓ Access your profile information
   ✓ Maintain access to data you have given it access to
   ```

4. **Understanding the Permissions**

![Select Permissions](../_media/selectPermissions.png)

   - **Read your mail**: Allows Krista to fetch emails from your account for automation
   - **Send mail on your behalf**: Enables Krista to send automated emails from your account
   - **Access your profile**: Gets basic info like your name and email for verification
   - **Maintain access**: Keeps the connection active without requiring frequent re-login

![Delegated Permissions](../_media/delegatedPermissions.png)

5. **Grant Permission**
   - Review each permission carefully
   - Click **"Accept"** if you agree to let Krista automate your email
   - Click **"Cancel"** if you want to stop the process

### Step 4: Private Authentication Setup (Enterprise)

For organizations using Private Authentication, the setup involves Azure Active Directory:

![Azure Active Directory](../_media/azureActiveDirectory.png)

#### Azure App Registration Process

1. **Create New Registration**

![New Registration](../_media/newRegistration.png)

2. **Configure App Registration**

![App Registration](../_media/appRegistration.png)

3. **Register Application**

![Register](../_media/register.png)

4. **Get Application Details**

![Client ID Tenant ID](../_media/clientIDtenantID.png)

5. **Configure Certificates & Secrets**

![Certificates & Secrets](../_media/certificates&Secrets.png)

![Add Client Secret](../_media/addClientSecret.png)

![Client Secret](../_media/clientSecret.png)

6. **Set Up Microsoft Graph Permissions**

![Microsoft Graph](../_media/microsoftGraph.png)

![Add A Permission](../_media/addAPermission.png)

7. **Configure Redirect URIs**

![Authorized Redirect URI Reference](../_media/authorizedRedirectURIReference.png)

### Step 5: Complete Configuration

![Routing ID](../_media/routingId.png)

1. **Test Connection**: Click "Test Connection" to verify setup
2. **Save Configuration**: Click "Save Changes" when test succeeds
3. **Verify Status**: Ensure connection shows as "Active"

## Verification and Testing

### Connection Health Check

- **Token Status**: Ensure authentication tokens are valid
- **Permission Scope**: Verify all required permissions are granted
- **API Connectivity**: Test connection to Microsoft Graph API
- **Error Logs**: Check for any authentication or permission errors

## Troubleshooting Common Issues

### Connection Fails During Setup

**Possible Causes:**
- Incorrect email address format
- Network connectivity issues
- Browser blocking pop-ups or redirects
- Organization security policies

**Solutions:**
1. Double-check email address spelling
2. Try different browser or incognito mode
3. Disable browser extensions temporarily
4. Contact IT department about security policies

### "Access Denied" Error

**Common Causes:**
- Organization blocks third-party applications
- Account lacks necessary permissions
- Conditional access policies

**Solutions:**
1. Check with IT about third-party app policies
2. Verify account has valid Microsoft 365 license
3. Try connecting from organization network
4. Consider Private Authentication for enterprise environments

### Authentication Timeout

**Causes:**
- Slow network connection
- Microsoft services experiencing delays
- Browser session timeout

**Solutions:**
1. Refresh page and try again
2. Check Microsoft service status
3. Clear browser cache and cookies
4. Try from different network if possible

## Security Best Practices

### During Setup

- ✅ Always verify you're on legitimate Microsoft login pages
- ✅ Check URL shows login.microsoftonline.com
- ✅ Never enter credentials on suspicious pages
- ✅ Review permissions carefully before accepting
- ✅ Use strong, unique passwords for your accounts

### After Connection

- ✅ Regularly review connected applications in Microsoft account settings
- ✅ Monitor email automation activity
- ✅ Report any suspicious activity immediately
- ✅ Keep your authentication credentials secure
- ✅ Update passwords regularly

## Next Steps

### Start Automating

Once connected, you can:
- **Create Email Workflows**: Set up automated responses and processing
- **Configure Triggers**: Define when automation should activate
- **Set Up Monitoring**: Track email automation performance
- **Explore Features**: Discover advanced automation capabilities

### Learn More

- **[Supported Operations](supportedCatalogRequests.md)**: Explore all available email automation features
- **[Authentication Guide](authentication.md)**: Understand security and authentication options
- **[Troubleshooting](authentication.md#troubleshooting)**: Get help with common issues

Your Outlook account is now securely connected to Krista and ready for intelligent email automation!