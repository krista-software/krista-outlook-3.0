# Obtaining Credentials For Private Authentication

![Azure Active Directory](../_media/azureActiveDirectory.png)

This comprehensive guide walks IT administrators through creating an Azure App Registration to obtain the Client ID, Client Secret, and Tenant ID required for Private Authentication with the Krista Outlook Extension.

## Prerequisites

Before starting, ensure you have:
- **Azure Administrator Access**: Global Administrator or Application Administrator role
- **Azure Active Directory**: Your organization must use Azure AD
- **Krista Instance Details**: Your Krista platform URL for redirect configuration
- **Modern Web Browser**: For accessing Azure Portal

## Step-by-Step Azure App Registration

### Step 1: Access Azure Portal

![Home Page](../_media/homePage.png)

1. **Navigate to Azure Portal**
   - Go to [portal.azure.com](https://portal.azure.com)
   - Sign in with your Azure administrator credentials
   - Ensure you're in the correct tenant/directory

2. **Access Azure Active Directory**
   - Click **"Azure Active Directory"** from the left menu
   - Or search for "Azure Active Directory" in the top search bar

### Step 2: Create New App Registration

![New Registration](../_media/newRegistration.png)

1. **Start Registration Process**
   - In Azure AD, click **"App registrations"** in the left menu
   - Click **"+ New registration"** at the top

2. **Configure Basic Application Details**

![App Registration](../_media/appRegistration.png)

   Fill in the registration form:
   ```
   Name: Krista Outlook Extension - [Your Organization Name]
   
   Supported account types: 
   ✓ Accounts in this organizational directory only ([Your Org] only - Single tenant)
   
   Redirect URI (optional):
   Platform: Web
   URI: https://your-krista-instance.com/outlook/v3/oauth/callback
   ```

3. **Complete Registration**

![Register](../_media/register.png)

   - Review all settings carefully
   - Click **"Register"** to create the application
   - Wait for the registration to complete

### Step 3: Collect Application Identifiers

![Client ID Tenant ID](../_media/clientIDtenantID.png)

After registration, you'll see the **Overview** page with essential information:

1. **Copy Application (Client) ID**
   ```
   Application (client) ID: xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
   ```
   - This is your **Client ID** for Krista configuration
   - Copy and store this value securely

2. **Copy Directory (Tenant) ID**
   ```
   Directory (tenant) ID: xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
   ```
   - This is your **Tenant ID** for Krista configuration
   - Copy and store this value securely

### Step 4: Create Client Secret

![Certificates & Secrets](../_media/certificates&Secrets.png)

1. **Navigate to Certificates & Secrets**
   - In your app registration, click **"Certificates & secrets"** in the left menu
   - Click the **"Client secrets"** tab

2. **Add New Client Secret**

![Add Client Secret](../_media/addClientSecret.png)

   - Click **"+ New client secret"**
   - Configure the secret:
     ```
     Description: Krista Outlook Extension Secret
     Expires: 12 months (recommended for security)
     ```
   - Click **"Add"**

3. **Copy Secret Value**

![Client Secret](../_media/clientSecret.png)

   **⚠️ CRITICAL**: Copy the secret value immediately!
   ```
   Value: [Long string of characters]
   ```
   - This is your **Client Secret** for Krista configuration
   - You cannot retrieve this value again after leaving this page
   - Store it securely in your password manager or secure documentation

### Step 5: Configure API Permissions

![Microsoft Graph](../_media/microsoftGraph.png)

1. **Navigate to API Permissions**
   - Click **"API permissions"** in the left menu
   - You'll see **"User.Read"** permission is already granted by default

2. **Add Microsoft Graph Permissions**

![Add A Permission](../_media/addAPermission.png)

   - Click **"+ Add a permission"**
   - Select **"Microsoft Graph"**
   - Choose **"Delegated permissions"**

3. **Select Required Permissions**

![Select Permissions](../_media/selectPermissions.png)

   Search for and select these permissions:
   ```
   ✓ Mail.Read - Read user mail
   ✓ Mail.Send - Send mail as a user  
   ✓ Mail.ReadWrite - Read and write access to user mail
   ✓ User.Read - Sign in and read user profile (already present)
   ✓ offline_access - Maintain access to data you have given it access to
   ```

![Delegated Permissions](../_media/delegatedPermissions.png)

4. **Grant Admin Consent**
   - Click **"Grant admin consent for [Your Organization]"**
   - Confirm by clicking **"Yes"** in the popup
   - Verify all permissions show **"Granted for [Your Organization]"** status

### Step 6: Configure Authentication Settings

1. **Navigate to Authentication**
   - Click **"Authentication"** in the left menu

2. **Add Redirect URIs**

![Authorized Redirect URI Reference](../_media/authorizedRedirectURIReference.png)

   Add these redirect URIs for your Krista instance:
   ```
   Platform: Web
   
   Redirect URIs:
   https://your-krista-instance.com/outlook/v3/oauth/callback
   https://your-krista-instance.com/rest/outlook/v3/oauth/callback
   ```

3. **Configure Advanced Settings**
   ```
   Access tokens (used for implicit flows): ✓ Checked
   ID tokens (used for implicit and hybrid flows): ✓ Checked
   Allow public client flows: ✗ Unchecked (for security)
   ```

4. **Save Configuration**
   - Click **"Save"** at the top of the page

## Configuration Summary

After completing the Azure setup, you should have these three values for Krista configuration:

![Routing ID](../_media/routingId.png)

```
Client ID: xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
Client Secret: [Secret value you copied]
Tenant ID: xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
```

## Configuring Krista with Your Credentials

### Step 1: Access Krista Extension Settings

1. **Log into Krista Platform**
   - Navigate to your Krista instance
   - Sign in with your Krista administrator credentials

2. **Navigate to Outlook Extension**
   - Go to **Extensions** → **Outlook Extension**
   - Click **"Add New Connection"** or **"Configure"**

### Step 2: Enter Private Authentication Details

1. **Select Private Authentication**
   - Choose **"Private Authentication"** radio button

2. **Fill in Credentials**
   ```
   Email: user@yourorganization.com
   Client ID: [From Azure App Registration Overview]
   Client Secret: [From Azure Certificates & Secrets]
   Tenant ID: [From Azure App Registration Overview]
   Allow Mail Alert: [Check if desired]
   ```

3. **Test and Save**
   - Click **"Test Connection"** to verify setup
   - Complete the authentication flow when prompted
   - Click **"Save Changes"** when test succeeds

## Security Best Practices

### Secret Management
- ✅ Store client secret in secure password manager
- ✅ Set calendar reminders for secret expiration
- ✅ Rotate secrets before expiration (recommended every 12 months)
- ✅ Never share secrets in email or unsecured documents
- ✅ Consider using certificate-based authentication for enhanced security

### Access Control
- ✅ Limit Azure AD admin access to necessary personnel
- ✅ Use Azure AD Privileged Identity Management (PIM) when available
- ✅ Enable audit logging for all app registration changes
- ✅ Regularly review app permissions and user assignments
- ✅ Implement conditional access policies for enhanced security

### Monitoring
- ✅ Monitor Azure AD sign-in logs for the application
- ✅ Set up alerts for unusual authentication patterns
- ✅ Review application usage reports regularly
- ✅ Track API usage and performance metrics
- ✅ Document all configuration changes

## Troubleshooting Common Issues

### Invalid Client Secret
**Symptoms**: Authentication fails with "invalid_client" error
**Causes**: 
- Client secret expired
- Incorrect secret copied
- Secret contains extra spaces or characters

**Solutions**:
1. Verify secret hasn't expired in Azure portal
2. Generate new client secret if needed
3. Ensure no extra characters when copying/pasting
4. Test secret with Microsoft Graph Explorer

### Incorrect Tenant ID
**Symptoms**: "AADSTS90002: Tenant not found" error
**Causes**:
- Wrong tenant ID copied
- Typo in tenant ID
- App registered in different tenant

**Solutions**:
1. Verify tenant ID from Azure App Registration Overview
2. Ensure you're in the correct Azure AD tenant
3. Check for typos or extra characters
4. Confirm GUID format: xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx

### Permission Issues
**Symptoms**: "AADSTS65001: The user or administrator has not consented" error
**Causes**:
- Missing required permissions
- Admin consent not granted
- Permissions configured incorrectly

**Solutions**:
1. Verify all required permissions are added
2. Grant admin consent for the organization
3. Check permission types (Delegated vs Application)
4. Ensure permissions show "Granted" status

### Redirect URI Mismatch
**Symptoms**: "AADSTS50011: The reply URL specified in the request does not match" error
**Causes**:
- Incorrect redirect URI configured
- Missing redirect URI
- Protocol mismatch (http vs https)

**Solutions**:
1. Verify exact Krista instance URL
2. Ensure https:// protocol is used
3. Check for trailing slashes or extra characters
4. Add both callback URLs if needed

## Advanced Configuration

### Multi-Tenant Setup
For organizations with multiple Azure AD tenants:
1. Configure app registration as "Multi-tenant"
2. Add redirect URIs for each tenant
3. Test authentication from each tenant
4. Document tenant-specific configurations

### Certificate-Based Authentication
For enhanced security instead of client secrets:
1. Generate X.509 certificate
2. Upload certificate to app registration
3. Configure Krista to use certificate authentication
4. Set up certificate renewal procedures

### Conditional Access Integration
To integrate with organizational security policies:
1. Create conditional access policies for the app
2. Configure device compliance requirements
3. Set up location-based access rules
4. Test policies with different user scenarios

Your Azure App Registration is now configured and ready for Private Authentication with the Krista Outlook Extension!