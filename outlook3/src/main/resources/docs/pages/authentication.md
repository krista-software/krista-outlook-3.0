# 🔐 Authentication

## 🎯 Overview

The Krista Outlook Extension offers two secure authentication methods to connect your Microsoft Outlook account. Both methods use industry-standard OAuth 2.0 protocol to ensure your credentials remain secure while enabling powerful email automation.

## 🔒 Authentication Methods Comparison

### 🌐 Public Authentication
**Perfect for individuals and small teams**

| Feature | Details |
|---------|---------|
| Setup Time | 5 minutes |
| Best For | Individual users, small teams (1-50 users) |
| IT Involvement | None required |
| Branding | Shows "Krista Email Automation" |
| API Limits | Shared limits with other Krista users |
| Security | OAuth 2.0 with Microsoft's standard security |

### 🏢 Private Authentication
**Enterprise-grade solution for organizations**

| Feature | Details |
|---------|---------|
| Setup Time | 30-60 minutes (requires IT setup) |
| Best For | Organizations, enterprise teams (50+ users) |
| IT Involvement | Azure AD administrator required |
| Branding | Shows your organization's name |
| API Limits | Dedicated limits for your organization |
| Security | OAuth 2.0 + organizational security policies |

## 🌐 Public Authentication

![Ask A System Checked](../_media/askASystemChecked.png)

### How It Works

Public Authentication uses Krista's pre-registered Microsoft application to provide quick, secure access to your Outlook account.

```
OAuth 2.0 Flow:
Your Email → Krista App → Microsoft Login → Permission Grant → Automation Ready
```

### Perfect For

- Individual Users: Personal email automation
- Small Teams: Up to 50 users
- Quick Start: Need to begin immediately
- Simple Requirements: Basic email automation needs
- No IT Restrictions: Organization allows third-party apps

### Benefits

- Instant Setup: Connect in under 5 minutes
- No IT Required: Self-service configuration
- Cost Effective: No additional infrastructure needed
- Secure: Full OAuth 2.0 protection
- User Friendly: Simple, intuitive process

### Rate Limits

| Operation | Limit | Reset Period |
|-----------|-------|--------------|
| Email Reading | 1,000 emails/hour | Rolling hour |
| Email Sending | 100 emails/hour | Rolling hour |
| Subscriptions | 5 active/account | Per account |
| Bulk Operations | 20 requests/minute | Rolling minute |

### Get Started
[Public Authentication Setup Guide](public-authentication.md)

## 🏢 Private Authentication

![Ask A System Unchecked](../_media/askASystemUnchecked.png)

### How It Works

Private Authentication uses your organization's own Azure App Registration, providing complete control over the authentication process.

```
Enterprise OAuth Flow:
Your Email → Your Azure App → Org Login → Admin Consent → Enterprise Ready
```

### Perfect For

- Large Organizations: 50+ users
- High Volume: Processing 1000+ emails daily
- Custom Branding: Organization name in consent screens
- Strict Security: Enterprise compliance requirements
- IT Policies: Organization requires custom app registrations

### Benefits

- Custom Branding: Your organization's name appears in all consent screens
- Centralized Control: IT manages all aspects of the integration
- User Management: Control which users can access the application
- Audit Trails: Comprehensive logging of all authentication events
- Higher Limits: Dedicated API quota for your organization
- Enhanced Security: Integration with organizational security policies

### Enhanced Rate Limits

| Operation | Limit | Reset Period |
|-----------|-------|--------------|
| Email Reading | 10,000 emails/hour | Rolling hour |
| Email Sending | 1,000 emails/hour | Rolling hour |
| Subscriptions | 50 active/account | Per account |
| Bulk Operations | 100 requests/minute | Rolling minute |

### Get Started
[Private Authentication Setup Guide](private-authentication.md)
[Obtaining Azure Credentials Guide](obtainingClientIDClientSecret.md)

## 🔒 Security Features

### OAuth 2.0 Protection

Both authentication methods provide enterprise-grade security:

- No Password Sharing: Krista never sees your actual password
- Limited Scope: Only grants access to specific email functions
- Revocable Access: Remove permissions anytime through Microsoft settings
- Encrypted Communication: All data transfer uses HTTPS/TLS encryption
- Token Expiration: Access tokens expire and refresh automatically

### Data Protection Standards

- Minimal Data Storage: Only necessary tokens and metadata stored
- No Email Storage: Email content processed in real-time, not stored
- Secure Token Storage: All tokens encrypted at rest
- Audit Logging: All access attempts logged for security monitoring
- Compliance Ready: SOC 2, GDPR, HIPAA compatible

## 🤔 Which Method Should You Choose?

### Choose Public Authentication If:

- You're an individual user or small team (under 50 users)
- You need to start immediately without IT involvement
- Your organization allows third-party applications
- You have basic email automation requirements
- You process fewer than 1,000 emails per day

### Choose Private Authentication If:

- You're a large organization (50+ users)
- You process high volumes of email (1,000+ daily)
- You need custom branding in consent screens
- Your organization has strict security policies
- You want dedicated API limits and enhanced performance
- You need centralized IT control and management

## 🔄 Migration Between Methods

### Public to Private Migration

If you start with Public Authentication and later need Private Authentication:

1. Plan Migration: Document current users and workflows
2. Set Up Azure: Create Azure App Registration following our guide
3. Pilot Test: Test with small group of users first
4. Communicate: Inform users about the change
5. Migrate Users: Have users re-authenticate with Private method
6. Verify: Ensure all users successfully migrated

## 🛠️ Troubleshooting Common Issues

### Access Denied Errors

**Symptoms**: Cannot connect to Outlook, access denied message

**Common Causes**:
- Organization blocks third-party applications
- Account lacks necessary permissions
- Conditional access policies blocking connection

**Solutions**:
1. Check with IT department about third-party app policies
2. Try connecting from organization network
3. Verify account has valid Microsoft 365 license
4. Consider switching to Private Authentication for enterprise environments

### Connection Timeout Issues

**Symptoms**: Setup process hangs or times out

**Common Causes**:
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

### Invalid Email Errors

**Symptoms**: Email address not accepted during setup

**Common Causes**:
- Email not hosted on Microsoft platforms
- Typo in email address
- Account doesn't exist or is disabled

**Solutions**:
1. Verify email address spelling
2. Ensure email is hosted on Microsoft 365, Outlook.com, or Exchange Online
3. Test logging into outlook.office.com with the same email
4. Contact email administrator if account issues persist

### Token Expired Errors

**Symptoms**: Previously working connection stops working

**Common Causes**:
- Refresh token expired (rare, usually 90 days)
- Password changed on Microsoft account
- Account disabled or permissions revoked

**Solutions**:
1. Re-authenticate through Krista settings
2. Verify Microsoft account is still active
3. Check if password was recently changed
4. Review connected apps in Microsoft account settings

## 📞 Getting Help

### Self-Service Resources

1. Test Your Connection: Use Krista's built-in connection test
2. Microsoft Account Settings: Review connected apps at account.microsoft.com
3. Browser Developer Tools: Check for JavaScript errors during setup
4. Network Diagnostics: Verify connectivity to Microsoft endpoints

### Support Channels

- Krista Support: For integration and configuration questions
- Microsoft Support: For account and authentication issues
- IT Department: For organizational policy questions
- Community Forums: For user experiences and tips

## 🚀 Next Steps

Once you've chosen your authentication method:

1. Follow Setup Guide: Complete the detailed setup process
2. Configure Automation: Set up your first email workflows
3. Monitor Performance: Track automation effectiveness
4. Optimize Settings: Fine-tune based on usage patterns

Ready to secure your email automation? Choose your authentication method and get started today! 🚀
