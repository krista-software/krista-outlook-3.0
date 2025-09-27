# Private Authentication Guide

This comprehensive guide helps IT administrators set up Private Authentication for the Krista Outlook Extension using your organization's own Azure App Registration.

## What is Private Authentication?

Private Authentication uses your organization's own Microsoft Azure App Registration with OAuth 2.0 Authorization Code Grant flow. This gives you complete control over the authentication process, higher API limits, and enhanced security monitoring.

## OAuth 2.0 Technical Specifications

### Grant Type and Flow
Private Authentication uses the **Authorization Code Grant** flow, the most secure OAuth 2.0 flow for web applications:

```
Grant Type: authorization_code
Flow: Authorization Code with PKCE (Proof Key for Code Exchange)
Client Type: Confidential Client
Authentication Method: Client Secret
```

### Required Endpoints
```
Authorization Endpoint: https://login.microsoftonline.com/{tenant-id}/oauth2/v2.0/authorize
Token Endpoint: https://login.microsoftonline.com/{tenant-id}/oauth2/v2.0/token
Microsoft Graph API: https://graph.microsoft.com/
```

### Required OAuth 2.0 Configuration
```json
{
  "grant_type": "authorization_code",
  "response_type": "code",
  "scope": "https://graph.microsoft.com/Mail.Read https://graph.microsoft.com/Mail.Send https://graph.microsoft.com/User.Read offline_access",
  "redirect_uri": "https://your-krista-instance.com/outlook/v3/oauth/callback",
  "client_authentication": "client_secret_post",
  "pkce": true
}
```

## Email Address Requirements

### Organizational Email Only
Private Authentication requires email addresses that belong to your organization's Azure Active Directory tenant:

```
✅ Correct for Private Authentication:
john.doe@company.com (if company.com is your organization's domain)
mary.smith@university.edu (if university.edu is your organization's domain)
sarah.jones@hospital.org (if hospital.org is your organization's domain)

❌ Incorrect for Private Authentication:
john.doe@gmail.com (external email, not in your organization)
mary@outlook.com (personal email, not organizational)
contractor@external-company.com (not in your Azure AD tenant)
guest.user@partner.com (guest users may need special configuration)
```

### User Account Requirements
- **Azure AD Account**: Must exist in your organization's Azure Active Directory
- **Licensed Account**: Must have appropriate Microsoft 365/Exchange Online licenses
- **Active Status**: Account must be enabled and not blocked
- **Proper Roles**: Must have necessary role assignments for email access

## Prerequisites

### Required Access and Permissions
- **Azure Active Directory Admin**: Global Administrator or Application Administrator role
- **Microsoft 365 Admin**: Ability to grant organization-wide consent
- **Technical Knowledge**: Understanding of Azure Portal and OAuth 2.0 concepts
- **Time Required**: 30-45 minutes for initial setup

### Information You'll Need
- Your organization's primary domain (e.g., company.com)
- Krista platform URL (e.g., https://your-krista-instance.com)
- List of users who will use the Outlook Extension
- Your organization's security and compliance requirements
- Azure Active Directory tenant ID

## Step-by-Step Setup Process

### Step 1: Create Azure App Registration

1. **Access Azure Portal**
   - Go to [portal.azure.com](https://portal.azure.com)
   - Sign in with your administrator account

2. **Navigate to App Registrations**
   - Click "Azure Active Directory" in the left menu
   - Click "App registrations"
   - Click "New registration"

3. **Configure Basic Settings**
   ```
   Name: Krista Outlook Extension - [Your Organization]
   Supported account types: Accounts in this organizational directory only
   Redirect URI: Web - https://your-krista-instance.com/outlook/v3/oauth/callback
   ```

### Step 2: Configure API Permissions

1. **Navigate to API Permissions**
2. **Add Microsoft Graph Permissions**:
   ```
   Delegated Permissions:
   - Mail.Read (Read user mail)
   - Mail.Send (Send mail as a user)
   - Mail.ReadWrite (Read and write access to user mail)
   - User.Read (Sign in and read user profile)
   - offline_access (Maintain access to data you have given it access to)
   ```

3. **Grant Admin Consent**
   - Click "Grant admin consent for [Your Organization]"
   - Confirm the consent grant

### Step 3: Generate Client Secret

1. **Navigate to Certificates & Secrets**
2. **Create New Client Secret**:
   ```
   Description: Krista Outlook Extension Secret
   Expires: 24 months (recommended)
   ```
3. **Copy Secret Value**: Save this immediately - you cannot retrieve it later

### Step 4: Configure Authentication Settings

1. **Navigate to Authentication**
2. **Configure Advanced Settings**:
   ```
   Access tokens: Checked
   ID tokens: Checked
   Allow public client flows: Unchecked (for security)
   ```

3. **Add Additional Redirect URIs** (if needed):
   ```
   https://your-krista-instance.com/outlook/v3/oauth/callback
   https://your-krista-instance.com/rest/outlook/v3/oauth/callback
   ```

### Step 5: Gather Required Information

Collect these values for Krista configuration:
```
Client ID: [Application (client) ID from Overview page]
Client Secret: [Secret value from step 3]
Tenant ID: [Directory (tenant) ID from Overview page]
```

## Configuring Krista

### Step 1: Access Krista Extension Settings
1. Log into your Krista platform
2. Navigate to **Extensions** → **Outlook Extension**
3. Click **"Add New Connection"** or **"Configure"**

### Step 2: Enter Private Authentication Details
1. Select **"Private Authentication"** radio button
2. Fill in the required fields:
   ```
   Email: user@yourorganization.com
   Client ID: [From Azure App Registration]
   Client Secret: [From Azure App Registration]
   Tenant ID: [From Azure App Registration]
   Allow Mail Alert: [Optional checkbox]
   ```

### Step 3: Test and Save Configuration
1. Click **"Test Connection"**
2. Complete the authentication flow
3. Click **"Save Changes"** when test succeeds

## User Authentication Process

### For End Users
1. **Access Krista**: User navigates to Outlook Extension settings
2. **Enter Email**: User enters their organizational email address
3. **Initiate Connection**: User clicks "Connect to Outlook"
4. **Organization Login**: User is redirected to YOUR organization's login page
5. **Authentication**: User logs in with organizational credentials
6. **Consent Review**: User sees YOUR organization's app requesting permissions
7. **Grant Access**: User approves permissions
8. **Completion**: User is redirected back to Krista with successful connection

### What Users See
```
Login Page: login.microsoftonline.com/yourorganization.com
App Name: Krista Outlook Extension - [Your Organization]
Permissions: [Your organization's app] wants to:
- Read your mail
- Send mail on your behalf
- Access your profile information
```

## Rate Limits and Benefits

### Enhanced API Limits
Private Authentication provides significantly higher limits:

| Operation | Public Auth Limit | Private Auth Limit |
|-----------|------------------|-------------------|
| Graph API Requests | 1,000/hour shared | 10,000/10 minutes per app |
| Email Reading | 1,000/hour shared | 10,000/hour per app |
| Email Sending | 100/hour shared | 1,000/hour per app |
| Concurrent Subscriptions | 5 shared | 1,000 per app |
| Bulk Operations | 20/minute shared | 100/minute per app |

### Additional Benefits
- **Dedicated Resources**: Your own API quota, not shared
- **Custom Branding**: Your organization's name in consent screens
- **Enhanced Monitoring**: Detailed usage analytics and audit logs
- **Conditional Access**: Integration with your organization's security policies
- **Compliance**: Meets enterprise security and governance requirements

## Common Issues and Solutions

### 1. Email Domain Mismatch
**Issue**: "AADSTS50020: User account from identity provider does not exist in tenant"
**Cause**: User email domain doesn't match organization's Azure AD tenant
**Solution**:
- Verify user email domain matches your organization
- Check if user exists in Azure Active Directory
- Ensure user account is properly licensed
- For guest users, configure external collaboration settings

### 2. Insufficient Permissions
**Issue**: "AADSTS65001: The user or administrator has not consented to use the application"
**Cause**: App registration lacks required permissions or admin consent not granted
**Solution**:
```
Required Actions:
1. Verify all required permissions are added to app registration
2. Grant admin consent for the organization
3. Ensure permissions include:
   - Mail.Read (Delegated)
   - Mail.Send (Delegated)
   - Mail.ReadWrite (Delegated)
   - User.Read (Delegated)
   - offline_access (Delegated)
```

### 3. Invalid Client Configuration
**Issue**: "AADSTS700016: Application with identifier 'xxx' was not found"
**Cause**: Incorrect Client ID or app registration issues
**Solution**:
- Verify Client ID exactly matches Azure Portal (no extra spaces/characters)
- Ensure app registration exists in correct tenant
- Check app registration status is "Enabled"
- Verify app registration hasn't been deleted

### 4. Client Secret Issues
**Issue**: "AADSTS7000215: Invalid client secret is provided"
**Cause**: Client secret expired, incorrect, or not properly configured
**Solution**:
- Generate new client secret in Azure Portal
- Ensure secret hasn't expired (check expiration date)
- Copy secret value immediately after creation
- Update Krista configuration with new secret

### 5. Redirect URI Mismatch
**Issue**: "AADSTS50011: The reply URL specified in the request does not match the reply URLs configured for the application"
**Cause**: Redirect URI in Azure doesn't exactly match Krista's callback URL
**Solution**:
```
Correct Redirect URIs to add:
- https://your-krista-instance.com/outlook/v3/oauth/callback
- https://your-krista-instance.com/rest/outlook/v3/oauth/callback

Common Mistakes:
❌ http:// instead of https://
❌ Extra trailing slash
❌ Wrong domain or path
❌ Missing port number if non-standard
```

### 6. Tenant Configuration Issues
**Issue**: "AADSTS90002: Tenant 'xxx' not found"
**Cause**: Incorrect Tenant ID or tenant configuration problems
**Solution**:
- Verify Tenant ID exactly matches Azure Portal
- Ensure tenant is active and properly configured
- Check if tenant has been migrated or renamed
- Verify you're using Directory (tenant) ID, not domain name

### 7. Conditional Access Blocking
**Issue**: "AADSTS53003: Access has been blocked by Conditional Access policies"
**Cause**: Organization's conditional access policies blocking the application
**Solution**:
- Review conditional access policies in Azure AD
- Add Krista app to trusted applications if appropriate
- Configure device compliance requirements
- Work with security team to adjust policies

### 8. User Assignment Issues
**Issue**: "AADSTS50105: The signed in user is not assigned to a role for the application"
**Cause**: App requires user assignment but user isn't assigned
**Solution**:
- In Azure Portal, go to Enterprise Applications
- Find your Krista app registration
- Go to Users and Groups
- Assign appropriate users or groups
- Or disable "User assignment required" if appropriate

### 9. Multi-Factor Authentication Problems
**Issue**: Authentication fails during MFA challenge
**Cause**: MFA configuration or policy issues
**Solution**:
- Verify user's MFA methods are properly set up
- Check conditional access policies for MFA requirements
- Ensure app is configured to handle MFA flows
- Test MFA with other applications

### 10. Guest User Limitations
**Issue**: External users cannot authenticate
**Cause**: Guest user restrictions or configuration
**Solution**:
- Configure external collaboration settings in Azure AD
- Enable guest user access for the application
- Verify guest users have appropriate licenses
- Check if guest user redemption is required

## Security Best Practices

### App Registration Security
- ✅ Use certificate-based authentication instead of client secrets when possible
- ✅ Set appropriate client secret expiration (12-24 months maximum)
- ✅ Implement proper secret rotation procedures
- ✅ Limit permissions to minimum required scope
- ✅ Enable audit logging for all app activities

### User Management
- ✅ Implement conditional access policies
- ✅ Require multi-factor authentication
- ✅ Monitor sign-in logs regularly
- ✅ Use privileged identity management for admin roles
- ✅ Implement device compliance requirements

### Ongoing Maintenance
- ✅ Monitor app usage and performance
- ✅ Review and rotate secrets before expiration
- ✅ Audit permissions and user assignments quarterly
- ✅ Keep documentation updated
- ✅ Test authentication flows after Azure AD changes

## Monitoring and Troubleshooting

### Azure AD Monitoring
1. **Sign-in Logs**: Monitor authentication attempts and failures
2. **Audit Logs**: Track permission changes and app modifications
3. **Application Logs**: Review app-specific events and errors
4. **Usage Analytics**: Monitor API usage and performance

### Key Metrics to Monitor
- Authentication success/failure rates
- API request volumes and patterns
- Token refresh frequency
- User adoption and usage patterns
- Error rates and types

### Troubleshooting Tools
- **Azure AD Sign-in Logs**: Detailed authentication flow analysis
- **Microsoft Graph Explorer**: Test API calls and permissions
- **Azure AD App Registration Test**: Validate OAuth flows
- **Fiddler/Browser Dev Tools**: Network traffic analysis

## Migration from Public to Private

### Planning the Migration
1. **Assess Current Usage**: Document existing Public Authentication users
2. **Create Azure App Registration**: Follow setup process above
3. **Test with Pilot Users**: Validate configuration with small group
4. **Plan User Communication**: Inform users about the change
5. **Schedule Migration**: Choose appropriate timing

### Migration Steps
1. **Prepare Private Authentication**: Complete Azure setup
2. **Test Configuration**: Verify everything works correctly
3. **Communicate to Users**: Provide migration instructions
4. **Migrate Users**: Have users re-authenticate with Private method
5. **Verify Migration**: Ensure all users successfully migrated
6. **Cleanup**: Remove old Public Authentication configurations

Your Private Authentication setup provides enterprise-grade security, control, and performance for your organization's email automation needs!