# Authentication Guide for Krista Outlook Extension

![Authentication Overview](../_media/homePage.png)

This guide provides an overview of authentication methods available with the Krista Outlook Extension and helps you choose the right approach for your organization.

## Understanding Authentication

Authentication is the process of verifying your identity and granting Krista permission to access your Outlook account. Think of it as giving Krista a secure "key" to interact with your emails on your behalf.

## Authentication Methods Overview

The Krista Outlook Extension supports two authentication approaches, each designed for different organizational needs:

### Public Authentication (Standard Method)

![Public Auth Overview](../_media/public_auth.png)

- **Quick Setup**: 5-minute configuration
- **Best For**: Individual users and small teams (under 50 users)
- **Management**: Handled entirely by Krista
- **Branding**: Shows "Krista Email Automation" in consent screens

[📖 **Detailed Public Authentication Guide**](public-authentication.md)

### Private Authentication (Enterprise Method)

![Private Auth Overview](../_media/privateAuth.png)

- **Custom Setup**: 15-30 minute configuration with IT involvement
- **Best For**: Large organizations (50+ users) with enterprise requirements
- **Management**: Full organizational control
- **Branding**: Shows your organization's name in consent screens

[📖 **Detailed Private Authentication Guide**](private-authentication.md)

## Quick Comparison

| Feature | Public Authentication | Private Authentication |
|---------|----------------------|----------------------|
| **Setup Time** | 5 minutes | 15-30 minutes |
| **IT Involvement** | None required | Required |
| **API Rate Limits** | Standard (shared) | Enhanced (dedicated) |
| **Branding** | Krista's branding | Your organization's branding |
| **User Management** | Individual setup | Centralized control |
| **Compliance** | Standard OAuth 2.0 | Enterprise-grade with audit trails |

## Choosing the Right Method

![Ask A System Checked](../_media/askASystemChecked.png)

### Use Public Authentication if:
- You're setting up for yourself or a small team
- You need to get started quickly
- Your organization doesn't have strict app approval processes
- You process moderate volumes of email (under 1000 emails/day)
- You don't require custom branding or enhanced monitoring

![Ask A System Unchecked](../_media/askASystemUnchecked.png)

### Use Private Authentication if:
- You're in a large enterprise environment (50+ users)
- Your IT department requires custom app registrations
- You need higher API limits for heavy email processing
- You want your organization's branding in consent screens
- You have specific compliance or audit requirements
- You need centralized user management and monitoring

## Security Features (Both Methods)

### OAuth 2.0 Protocol

Both authentication methods use OAuth 2.0, the industry standard for secure authorization:
- **No password sharing**: Krista never sees your actual password
- **Limited permissions**: Only grants access to specific email functions
- **Revocable access**: You can remove permissions at any time
- **Encrypted communication**: All data transfer is encrypted

### Permission Scopes

![Select Permissions](../_media/selectPermissions.png)

Both methods request only necessary permissions:

![Delegated Permissions](../_media/delegatedPermissions.png)

| Permission | Purpose | Required |
|------------|---------|----------|
| `Mail.Read` | Read emails from your mailbox | Yes |
| `Mail.Send` | Send emails on your behalf | Yes |
| `User.Read` | Verify your identity | Yes |
| `Mail.ReadWrite` | Modify email properties (labels, etc.) | Optional |

![Add A Permission](../_media/addAPermission.png)

## Azure App Registration Process

For organizations choosing Private Authentication, the setup involves Azure Active Directory:

![Azure Active Directory](../_media/azureActiveDirectory.png)

### Step 1: Create New Registration

![New Registration](../_media/newRegistration.png)

### Step 2: Configure App Registration

![App Registration](../_media/appRegistration.png)

### Step 3: Register Application

![Register](../_media/register.png)

### Step 4: Get Client ID and Tenant ID

![Client ID Tenant ID](../_media/clientIDtenantID.png)

### Step 5: Configure Certificates & Secrets

![Certificates & Secrets](../_media/certificates&Secrets.png)

### Step 6: Add Client Secret

![Add Client Secret](../_media/addClientSecret.png)

![Client Secret](../_media/clientSecret.png)

### Step 7: Configure Microsoft Graph Permissions

![Microsoft Graph](../_media/microsoftGraph.png)

### Step 8: Set Authorized Redirect URI

![Authorized Redirect URI Reference](../_media/authorizedRedirectURIReference.png)

## Getting Started

### For Individual Users or Small Teams

1. **Start Here**: [Public Authentication Guide](public-authentication.md)
2. **Quick Setup**: Follow the step-by-step process
3. **Begin Automating**: Start with simple email workflows

### For Enterprise Organizations

1. **Plan Your Approach**: Review [Private Authentication Guide](private-authentication.md)
2. **Involve IT Team**: Share the guide with your IT administrators
3. **Follow Enterprise Setup**: Complete Azure App Registration process
4. **Deploy to Users**: Roll out to your organization systematically

## Configuration Reference

### Routing ID Setup

![Routing ID](../_media/routingId.png)

## Migration Between Methods

### From Public to Private

Organizations often start with Public Authentication and later migrate to Private Authentication as they grow. The [Private Authentication Guide](private-authentication.md) includes detailed migration instructions.

## Support and Resources

### Documentation

- **[Public Authentication Guide](public-authentication.md)**: Complete setup and troubleshooting
- **[Private Authentication Guide](private-authentication.md)**: Enterprise configuration and management
- **[Connection Guide](connectingWithOutlookExtension.md)**: General connection process
- **[Credentials Guide](obtainingClientIDClientSecret.md)**: Azure App Registration details

Your authentication choice sets the foundation for secure, efficient email automation with Krista. Choose the method that best fits your organization's size, security requirements, and technical capabilities.
