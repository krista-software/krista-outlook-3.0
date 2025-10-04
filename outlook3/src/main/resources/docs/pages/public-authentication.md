# 🌐 Public Authentication

## 📋 Overview

Public Authentication provides a quick and easy way to connect your Outlook account to Krista using our pre-registered Microsoft application. This method is perfect for individual users and small teams who need to get started immediately without IT involvement.

## ✨ Key Benefits

### ⚡ Quick Setup
- Connect your account in under 5 minutes
- No IT department involvement required
- Self-service configuration process
- Immediate access to email automation features

### 🔒 Secure Connection
- Industry-standard OAuth 2.0 authentication
- No password sharing with Krista
- Encrypted data transmission
- Revocable access permissions

### 💰 Cost Effective
- No additional infrastructure required
- No Azure AD setup costs
- Shared API limits across users
- Perfect for small teams and individuals

## 🚀 Getting Started

### 📋 Prerequisites
- Active Microsoft Outlook account (Microsoft 365, Outlook.com, or Exchange Online)
- Modern web browser (Chrome, Firefox, Safari, or Edge)
- Stable internet connection
- Valid Krista platform account

### 🔧 Setup Process

#### Step 1: Access Krista Extension
1. Log into your Krista platform
2. Navigate to Extensions → Outlook Extension
3. Click "Add New Connection" or "Configure"

#### Step 2: Choose Public Authentication
1. Select "Public Authentication" option
2. Enter your Outlook email address
3. Click "Connect to Outlook"

#### Step 3: Microsoft Authorization
1. You'll be redirected to Microsoft's login page
2. Enter your Outlook email and password
3. Complete any two-factor authentication if enabled
4. Review the permission request screen

#### Step 4: Grant Permissions
Microsoft will request the following permissions:
- Read your mail
- Send mail on your behalf
- Access your profile information
- Maintain access to data you have given it access to

#### Step 5: Complete Setup
1. Click "Accept" to grant permissions
2. You'll be redirected back to Krista
3. Verify the connection is active
4. Test the connection functionality

## 📊 Rate Limits and Performance

### 🔢 API Limits
- Email Reading: 1,000 emails per hour
- Email Sending: 100 emails per hour
- Active Subscriptions: 5 per account
- Attachment Downloads: 50 per hour
- Bulk Operations: 20 requests per minute

### ⚡ Performance Characteristics
- Shared infrastructure with other Krista users
- Standard processing priority
- Typical response times under 2 seconds
- 99.5% uptime availability

## 🛡️ Security Features

### 🔐 Authentication Security
- OAuth 2.0 with PKCE (Proof Key for Code Exchange)
- No password storage by Krista
- Automatic token refresh
- Secure token encryption at rest

### 📋 Data Protection
- Minimal data storage (only necessary tokens and metadata)
- No email content stored permanently
- Encrypted data transmission (HTTPS/TLS)
- Regular security audits and updates

### 🔄 Access Control
- Granular permission scoping
- User-controlled access revocation
- Session timeout management
- Audit logging of all activities

## 🎯 Best Use Cases

### 👤 Individual Users
- Personal email automation
- Small business owners
- Freelancers and consultants
- Students and researchers

### 👥 Small Teams
- Startups with limited IT resources
- Small departments within larger organizations
- Project teams needing quick setup
- Temporary or pilot implementations

### 🚀 Quick Pilots
- Testing Krista capabilities before enterprise rollout
- Proof of concept implementations
- Temporary automation needs
- Development and testing environments

## 🔧 Troubleshooting

### 🚫 Common Issues

#### Authentication Fails
**Symptoms**: Cannot complete the login process
**Possible Causes**:
- Incorrect email address
- Network connectivity issues
- Browser blocking pop-ups
- Organization security policies

**Solutions**:
1. Verify email address spelling and format
2. Try a different browser or incognito mode
3. Check internet connection stability
4. Disable browser extensions temporarily
5. Contact IT about third-party app policies

#### Permission Denied
**Symptoms**: "Access denied" error during setup
**Possible Causes**:
- Organization blocks third-party applications
- Account lacks necessary permissions
- Conditional access policies

**Solutions**:
1. Check with IT about third-party app policies
2. Verify account has valid Microsoft 365 license
3. Try connecting from organization network
4. Consider Private Authentication for enterprise environments

#### Connection Timeout
**Symptoms**: Setup process hangs or times out
**Possible Causes**:
- Slow network connection
- Microsoft services experiencing delays
- Browser session timeout

**Solutions**:
1. Refresh page and try again
2. Check Microsoft service status
3. Clear browser cache and cookies
4. Try from different network if possible

### 🔍 Diagnostic Steps

#### Verify Account Access
1. Test logging into outlook.office.com with your email
2. Ensure account is active and not locked
3. Verify two-factor authentication is working
4. Check for any account restrictions

#### Check Network Connectivity
1. Ensure access to login.microsoftonline.com
2. Verify graph.microsoft.com is accessible
3. Test from different network if possible
4. Check firewall and proxy settings

#### Browser Troubleshooting
1. Try different browsers (Chrome, Firefox, Safari, Edge)
2. Use incognito/private browsing mode
3. Disable browser extensions
4. Clear cache and cookies
5. Check for JavaScript errors in developer console

## 🔄 Managing Your Connection

### 📊 Monitoring Connection Health
- Check connection status in Krista dashboard
- Monitor API usage and rate limits
- Review authentication token expiration
- Track automation performance metrics

### 🔧 Updating Settings
- Modify email automation rules
- Update notification preferences
- Change folder monitoring settings
- Adjust rate limiting preferences

### 🚫 Revoking Access
If you need to disconnect your account:
1. Go to Krista Extension settings
2. Click "Disconnect" or "Remove Connection"
3. Optionally revoke access in Microsoft account settings
4. Verify all automation has stopped

### 🔄 Reconnecting
If your connection stops working:
1. Check token expiration status
2. Try refreshing the connection
3. Re-authenticate if necessary
4. Contact support if issues persist

## 🆙 Upgrading to Private Authentication

### 🤔 When to Consider Upgrading
- Your team grows beyond 50 users
- You need higher API rate limits
- Organization requires custom branding
- Enhanced security features are needed
- Centralized IT management is required

### 🔄 Migration Process
1. Set up Azure App Registration (see our guide)
2. Test Private Authentication with pilot users
3. Plan migration timeline and communication
4. Migrate users during low-usage period
5. Verify all users successfully migrated
6. Decommission Public Authentication connections

## 📞 Getting Help

### 🆘 Self-Service Resources
- Test connection functionality in Krista dashboard
- Review Microsoft account connected apps
- Check browser developer tools for errors
- Verify network connectivity to Microsoft endpoints

### 📧 Support Channels
- Krista Support: For integration and configuration questions
- Microsoft Support: For account and authentication issues
- Community Forums: For user experiences and tips
- Documentation: Comprehensive guides and troubleshooting

## 🚀 Next Steps

Once your Public Authentication is set up:

1. **⚙️ Configure Automation**: Set up your first email workflows
2. **📊 Monitor Performance**: Track automation effectiveness
3. **🔧 Optimize Settings**: Fine-tune based on usage patterns
4. **📚 Explore Features**: Discover advanced automation capabilities

Ready to connect your Outlook account? Follow our step-by-step setup guide and start automating your email workflows today!