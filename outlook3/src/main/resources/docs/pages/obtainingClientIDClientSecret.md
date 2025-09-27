# Obtaining Credentials for Private Authentication

This comprehensive guide will help your IT administrator set up Private Authentication for the Krista Outlook Extension. Private Authentication provides enhanced security, higher API limits, and complete organizational control.

## Overview

Private Authentication requires creating a Microsoft Azure App Registration that belongs to your organization. This gives you:

- **Complete Control**: Manage permissions and access policies
- **Higher Limits**: Increased API rate limits for heavy usage
- **Custom Branding**: Your organization's name in consent screens
- **Enhanced Security**: Full audit trails and monitoring capabilities
- **Compliance**: Meet enterprise security requirements

## Prerequisites

### Required Access and Permissions
- **Azure Active Directory Admin**: Global Administrator or Application Administrator role
- **Microsoft 365 Admin**: Ability to grant organization-wide consent
- **Technical Knowledge**: Basic understanding of Azure Portal and OAuth concepts
- **Time Required**: 30-45 minutes for initial setup

### Information You'll Need
Before starting, gather this information:
- Your organization's primary domain (e.g., company.com)
- Krista platform URL (e.g., https://your-krista-instance.com)
- List of users who will use the Outlook Extension
- Your organization's security and compliance requirements

## Step-by-Step Setup Process

### Step 1: Access Azure Portal

1. **Open Azure Portal**
   - Go to [portal.azure.com](https://portal.azure.com)
   - Sign in with your administrator account

2. **Navigate to Azure Active Directory**
   - Click "Azure Active Directory" in the left menu
   - If not visible, click "All services" and search for "Azure Active Directory"

3. **Access App Registrations**
   - In the Azure AD menu, click "App registrations"
   - Click "New registration" button

### Step 2: Create App Registration

1. **Basic Information**
   ```
   Name: Krista Outlook Extension - [Your Organization]
   Supported account types: Accounts in this organizational directory only
   Redirect URI: Web
   ```

2. **Redirect URI Configuration**
   ```
   Platform: Web
   Redirect URI: https://your-krista-instance.com/outlook/v3/oauth/callback
   ```
   
   ⚠️ **Important**: Replace `your-krista-instance.com` with your actual Krista platform URL

3. **Click "Register"**
   - Azure will create your app registration
   - You'll be taken to the app overview page

### Step 3: Configure API Permissions

1. **Navigate to API Permissions**
   - In your app registration, click "API permissions" in the left menu
   - Click "Add a permission"

2. **Add Microsoft Graph Permissions**
   - Click "Microsoft Graph"
   - Select "Delegated permissions"
   - Add these permissions:

   | Permission | Purpose | Required |
   |------------|---------|----------|
   | `Mail.Read` | Read user's emails | ✅ Yes |
   | `Mail.Send` | Send emails on behalf of user | ✅ Yes |
   | `Mail.ReadWrite` | Modify email properties | ✅ Yes |
   | `User.Read` | Read user profile information | ✅ Yes |
   | `offline_access` | Maintain access when user is offline | ✅ Yes |

3. **Grant Admin Consent**
   - Click "Grant admin consent for [Your Organization]"
   - Confirm by clicking "Yes"
   - All permissions should show green checkmarks

### Step 4: Create Client Secret

1. **Navigate to Certificates & Secrets**
   - Click "Certificates & secrets" in the left menu
   - Click "New client secret"

2. **Configure Secret**
   ```
   Description: Krista Outlook Extension Secret
   Expires: 24 months (recommended)
   ```

3. **Save the Secret**
   - Click "Add"
   - **IMMEDIATELY COPY THE SECRET VALUE** 
   - ⚠️ **Critical**: You cannot view this secret again after leaving the page
   - Store it securely (password manager, secure note, etc.)

### Step 5: Gather Required Information

From your app registration overview page, collect these values:

```
Application (client) ID: [Copy this value]
Directory (tenant) ID: [Copy this value]
Client Secret: [The value you copied in Step 4]
```

**Example of what you'll see:**
```
Application (client) ID: 12345678-1234-1234-1234-123456789012
Directory (tenant) ID: 87654321-4321-4321-4321-210987654321
Client Secret: abc123def456ghi789jkl012mno345pqr678stu901
```

### Step 6: Configure Advanced Settings (Optional)

#### Token Configuration
1. **Navigate to Token configuration**
2. **Add optional claims** (if needed for compliance):
   - `email`: User's email address
   - `name`: User's display name
   - `preferred_username`: User's preferred username

#### Authentication Settings
1. **Navigate to Authentication**
2. **Configure additional settings**:
   - **Access tokens**: Check if you need access tokens
   - **ID tokens**: Check if you need ID tokens
   - **Allow public client flows**: Leave unchecked for security

#### Branding (Optional)
1. **Navigate to Branding**
2. **Customize consent screen**:
   - **Publisher domain**: Your organization's verified domain
   - **Home page URL**: Your organization's website
   - **Terms of service URL**: Link to your terms
   - **Privacy statement URL**: Link to your privacy policy

## Providing Credentials to Krista

### Information to Share with Krista Administrator

Provide these details to your Krista administrator:

```
=== Azure App Registration Details ===
Application (client) ID: [Your client ID]
Directory (tenant) ID: [Your tenant ID]
Client Secret: [Your client secret]
Redirect URI: https://your-krista-instance.com/outlook/v3/oauth/callback

=== Organization Details ===
Organization Name: [Your company name]
Primary Domain: [Your domain, e.g., company.com]
Expected Users: [Number of users who will use the extension]

=== Security Notes ===
- Admin consent has been granted
- All required permissions are configured
- Secret expires on: [Expiration date]
```

### Secure Transmission Methods

**Recommended approaches for sharing credentials:**
1. **In-person handoff**: Most secure for sensitive environments
2. **Encrypted email**: Use your organization's encrypted email system
3. **Secure file sharing**: Use enterprise file sharing with encryption
4. **Password manager**: Share through enterprise password management system

**Avoid these methods:**
- ❌ Regular email
- ❌ Instant messaging
- ❌ Text messages
- ❌ Unsecured file sharing

## Testing Your Configuration

### Initial Validation

Before providing credentials to Krista, test your configuration:

1. **Check App Registration**
   - Verify all permissions are granted
   - Confirm redirect URI is correct
   - Ensure client secret is active

2. **Test Authentication Flow**
   - Use Microsoft's OAuth testing tools
   - Verify consent screen appears correctly
   - Confirm tokens are generated successfully

### Post-Configuration Testing

After Krista configuration:

1. **Connection Test**
   - Krista administrator will run connection tests
   - Verify successful authentication
   - Confirm email access is working

2. **User Testing**
   - Have a test user authenticate
   - Verify they see your organization's branding
   - Confirm permissions are working correctly

## Ongoing Management

### Security Monitoring

**Regular Tasks:**
- Monitor app usage in Azure AD logs
- Review consent grants and permissions
- Check for unusual authentication patterns
- Audit user access regularly

**Monthly Reviews:**
- Verify app registration settings
- Check client secret expiration dates
- Review user access and permissions
- Update security policies as needed

### Maintenance Schedule

| Task | Frequency | Responsibility |
|------|-----------|----------------|
| Monitor usage logs | Weekly | IT Security Team |
| Review permissions | Monthly | Azure Administrator |
| Check secret expiration | Monthly | Azure Administrator |
| Audit user access | Quarterly | IT Security Team |
| Update documentation | As needed | IT Administrator |

### Client Secret Renewal

**Before Secret Expires:**
1. Create new client secret in Azure Portal
2. Provide new secret to Krista administrator
3. Krista administrator updates configuration
4. Test connection with new secret
5. Delete old secret after confirmation

**Timeline:**
- 60 days before expiration: Create renewal plan
- 30 days before expiration: Generate new secret
- 7 days before expiration: Update Krista configuration
- After confirmation: Remove old secret

## Troubleshooting Common Issues

### "Invalid Client" Error
**Cause**: Incorrect Client ID or Secret
**Solution**: 
- Verify Client ID matches Azure Portal
- Regenerate client secret if needed
- Check for typos in configuration

### "Redirect URI Mismatch" Error
**Cause**: Redirect URI doesn't match Azure configuration
**Solution**:
- Verify redirect URI in Azure Portal
- Ensure exact match including https:// and trailing paths
- Check for extra spaces or characters

### "Insufficient Privileges" Error
**Cause**: Missing API permissions or admin consent
**Solution**:
- Review required permissions in Azure Portal
- Grant admin consent for all permissions
- Wait 5-10 minutes for changes to propagate

### "Tenant Not Found" Error
**Cause**: Incorrect Tenant ID
**Solution**:
- Verify Tenant ID in Azure Portal overview
- Check for typos in configuration
- Ensure using correct Azure AD tenant

## Security Best Practices

### Access Control
- **Principle of Least Privilege**: Only grant necessary permissions
- **Regular Reviews**: Audit access quarterly
- **User Training**: Educate users on security practices
- **Incident Response**: Have plan for security incidents

### Credential Management
- **Secure Storage**: Use enterprise password managers
- **Regular Rotation**: Rotate secrets before expiration
- **Access Logging**: Monitor who accesses credentials
- **Backup Plans**: Have recovery procedures for lost credentials

### Monitoring and Alerting
- **Usage Monitoring**: Track authentication patterns
- **Anomaly Detection**: Alert on unusual access
- **Compliance Reporting**: Generate regular security reports
- **Incident Tracking**: Log and investigate security events

## Compliance Considerations

### Data Protection
- **GDPR Compliance**: Ensure proper data handling
- **Data Residency**: Understand where data is processed
- **Retention Policies**: Configure appropriate data retention
- **User Rights**: Provide mechanisms for data access/deletion

### Audit Requirements
- **Access Logs**: Maintain detailed authentication logs
- **Permission Changes**: Track all permission modifications
- **User Activity**: Monitor email access patterns
- **Compliance Reports**: Generate regular audit reports

## Support and Resources

### Microsoft Documentation
- [Azure App Registrations Guide](https://docs.microsoft.com/azure/active-directory/develop/quickstart-register-app)
- [Microsoft Graph Permissions](https://docs.microsoft.com/graph/permissions-reference)
- [OAuth 2.0 Best Practices](https://docs.microsoft.com/azure/active-directory/develop/v2-oauth2-auth-code-flow)

### Getting Help
- **Azure Support**: For Azure Portal and app registration issues
- **Krista Support**: For integration and configuration questions
- **Microsoft 365 Support**: For tenant and permission issues
- **Internal IT**: For organization-specific security questions

Your Private Authentication setup is now complete! This provides a secure, scalable foundation for your organization's email automation needs.
