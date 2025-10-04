# 🔑 Obtaining Credentials For Private Authentication

## 📋 Overview

Private Authentication requires creating an Azure App Registration in your organization's Azure Active Directory. This guide provides step-by-step instructions for Azure administrators to obtain the necessary credentials for enterprise-grade Outlook integration.

## 📋 Prerequisites

### 👤 Required Permissions

You need one of the following Azure AD roles:
- Global Administrator
- Application Administrator
- Cloud Application Administrator

### 🔧 Required Access

- Access to Azure Portal (portal.azure.com)
- Permissions to create app registrations
- Ability to grant admin consent for the organization
- Access to manage certificates and secrets

### 📊 Information Needed

Before starting, gather:
- Your organization's Azure AD tenant information
- Krista instance URL and callback endpoints
- Preferred authentication method (secret vs. certificate)
- List of users who will need access

## 🚀 Step-by-Step Setup Process

### 📊 Step 1: Access Azure Portal

1. **Navigate to Azure Portal**
   - Open your web browser
   - Go to https://portal.azure.com
   - Sign in with your administrator account

2. **Access Azure Active Directory**
   - Click on "Azure Active Directory" in the left navigation
   - Or search for "Azure Active Directory" in the top search bar
   - Ensure you're in the correct tenant

3. **Navigate to App Registrations**
   - In the Azure AD menu, click "App registrations"
   - You'll see a list of existing applications
   - Click "New registration" to create a new app

### 🔧 Step 2: Create App Registration

1. **Basic Information**
   - **Name**: Enter a descriptive name (e.g., "Krista Outlook Integration")
   - **Supported account types**: Choose "Accounts in this organizational directory only"
   - **Redirect URI**: Leave blank for now (we'll add this later)

2. **Register the Application**
   - Click "Register" to create the app registration
   - Note the Application (client) ID that appears
   - Note the Directory (tenant) ID from the overview page

3. **Configure Application Settings**
   - Review the app registration overview
   - Verify the application ID and tenant ID
   - Note these values for later configuration

### 🔐 Step 3: Configure Authentication

1. **Add Redirect URIs**
   - In your app registration, click "Authentication" in the left menu
   - Click "Add a platform"
   - Select "Web"
   - Add the Krista callback URLs:
     - `https://your-krista-instance.com/oauth/callback`
     - `https://your-krista-instance.com/auth/microsoft/callback`
   - Replace "your-krista-instance.com" with your actual Krista URL

2. **Configure Token Settings**
   - Under "Implicit grant and hybrid flows":
     - Leave "Access tokens" unchecked
     - Leave "ID tokens" unchecked
   - Under "Advanced settings":
     - Set "Allow public client flows" to "No"
     - Enable "Live SDK support" if needed

3. **Save Configuration**
   - Click "Save" to apply the authentication settings
   - Verify the redirect URIs are correctly configured

### 🔑 Step 4: Create Client Secret

#### Option A: Client Secret (Recommended for most scenarios)

1. **Navigate to Certificates & Secrets**
   - In your app registration, click "Certificates & secrets"
   - Click on the "Client secrets" tab
   - Click "New client secret"

2. **Configure Secret**
   - **Description**: Enter a meaningful description (e.g., "Krista Integration Secret")
   - **Expires**: Choose expiration period (12 months recommended)
   - Click "Add"

3. **Copy Secret Value**
   - **IMPORTANT**: Copy the secret value immediately
   - This value will not be shown again
   - Store it securely (password manager recommended)
   - Note the secret ID for reference

#### Option B: Certificate (For enhanced security)

1. **Prepare Certificate**
   - Generate a self-signed certificate or use existing PKI
   - Export the public key in .cer format
   - Keep the private key secure for Krista configuration

2. **Upload Certificate**
   - In "Certificates & secrets", click "Certificates" tab
   - Click "Upload certificate"
   - Select your .cer file
   - Add a description
   - Click "Add"

3. **Note Certificate Details**
   - Record the certificate thumbprint
   - Note the expiration date
   - Ensure private key is available for Krista

### 🛡️ Step 5: Configure API Permissions

1. **Add Required Permissions**
   - Click "API permissions" in the left menu
   - Click "Add a permission"
   - Select "Microsoft Graph"
   - Choose "Delegated permissions"

2. **Select Specific Permissions**
   Add these permissions:
   - **Mail.Read**: Read user mail
   - **Mail.Send**: Send mail as a user  
   - **Mail.ReadWrite**: Read and write access to user mail
   - **User.Read**: Sign in and read user profile
   - **offline_access**: Maintain access to data

3. **Grant Admin Consent**
   - After adding all permissions, click "Grant admin consent for [Your Organization]"
   - Confirm the consent in the popup dialog
   - Verify all permissions show "Granted" status with green checkmarks

### 📊 Step 6: Record Configuration Details

Create a secure record of the following information:

#### Application Information
- **Application (Client) ID**: [Copy from Overview page]
- **Directory (Tenant) ID**: [Copy from Overview page]
- **Client Secret**: [Copy from Certificates & secrets]
- **Secret Expiration**: [Note expiration date]

#### Redirect URIs
- List all configured redirect URIs
- Verify they match your Krista instance URLs

#### Permissions
- Confirm all required permissions are granted
- Note any additional permissions if added

## 🔧 Advanced Configuration Options

### 👥 User Assignment

1. **Require User Assignment**
   - Go to "Enterprise applications" in Azure AD
   - Find your app registration
   - Click "Properties"
   - Set "User assignment required?" to "Yes"
   - This restricts access to assigned users only

2. **Assign Users or Groups**
   - Click "Users and groups"
   - Click "Add user/group"
   - Select specific users or groups
   - Assign appropriate roles

### 🔒 Conditional Access

1. **Create Conditional Access Policy**
   - Navigate to "Security" > "Conditional Access"
   - Create new policy targeting your application
   - Configure conditions (location, device, risk level)
   - Set access controls (MFA, compliant device, etc.)

2. **Test Policy**
   - Use "What If" tool to test policy impact
   - Pilot with small group before full deployment
   - Monitor sign-in logs for policy effectiveness

### 📊 Monitoring and Auditing

1. **Enable Audit Logging**
   - Ensure audit logs are enabled in Azure AD
   - Configure log retention as per compliance requirements
   - Set up alerts for suspicious activities

2. **Monitor Application Usage**
   - Review sign-in logs regularly
   - Monitor API usage patterns
   - Set up alerts for unusual activity

## 🛠️ Troubleshooting Common Issues

### 🚫 Permission Issues

**Problem**: "Insufficient privileges" errors
**Solutions**:
1. Verify all required permissions are added
2. Ensure admin consent has been granted
3. Check permission types (Delegated vs Application)
4. Confirm user has necessary licenses

### 🔑 Authentication Failures

**Problem**: "Invalid client" or authentication errors
**Solutions**:
1. Verify Client ID and Tenant ID are correct
2. Check Client Secret hasn't expired
3. Ensure redirect URIs match exactly
4. Confirm app registration is in correct tenant

### 🌐 Redirect URI Mismatches

**Problem**: "Reply URL mismatch" errors
**Solutions**:
1. Verify exact Krista instance URL
2. Ensure HTTPS protocol is used
3. Check for trailing slashes
4. Add all required callback URLs

## 🔄 Maintenance and Security

### 🔑 Secret Rotation

1. **Plan Rotation Schedule**
   - Set calendar reminders before expiration
   - Plan rotation during maintenance windows
   - Coordinate with Krista administrators

2. **Rotation Process**
   - Create new secret before old one expires
   - Update Krista configuration with new secret
   - Test functionality thoroughly
   - Delete old secret after verification

### 📊 Regular Reviews

1. **Quarterly Reviews**
   - Review user assignments
   - Audit permission grants
   - Check for unused applications
   - Update documentation

2. **Security Assessments**
   - Review conditional access policies
   - Analyze sign-in patterns
   - Check for security alerts
   - Update security configurations

## 📞 Getting Help

### 🆘 Microsoft Support

For Azure AD issues:
- Azure Portal help and support
- Microsoft 365 admin center
- Azure AD documentation
- Microsoft community forums

### 🔧 Krista Support

For integration issues:
- Krista support portal
- Technical documentation
- Implementation guides
- Professional services

### 📋 Information to Provide

When seeking support, include:
- Application (Client) ID
- Tenant ID (if safe to share)
- Error messages and screenshots
- Steps taken before the issue
- Browser and environment details

## ✅ Verification Checklist

Before providing credentials to Krista team:

- [ ] App registration created successfully
- [ ] Client ID and Tenant ID recorded
- [ ] Client Secret created and securely stored
- [ ] All required permissions added
- [ ] Admin consent granted for organization
- [ ] Redirect URIs configured correctly
- [ ] Authentication settings verified
- [ ] User assignments configured (if required)
- [ ] Documentation updated with details

## 🚀 Next Steps

After obtaining credentials:

1. **Provide to Krista Team**
   - Share credentials securely
   - Provide any additional configuration details
   - Schedule implementation call if needed

2. **Test Integration**
   - Verify connection works correctly
   - Test with pilot users first
   - Monitor for any issues

3. **Full Deployment**
   - Roll out to all users
   - Provide user training
   - Monitor usage and performance

Ready to set up Private Authentication? Follow this guide to create your Azure App Registration and obtain the necessary credentials for enterprise-grade Outlook integration!
