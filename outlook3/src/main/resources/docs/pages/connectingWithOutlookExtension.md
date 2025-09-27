# 🔗 Connecting with Krista Outlook Extension

## 📋 Overview

Connecting your Microsoft Outlook account to Krista enables powerful email automation capabilities. This guide walks you through the complete connection process, from initial setup to testing your first automation workflow.

## 🚀 Quick Start Guide

### 📋 Prerequisites

Before you begin, ensure you have:

- Active Microsoft Outlook account (Microsoft 365, Outlook.com, or Exchange Online)
- Valid Krista platform account with appropriate permissions
- Modern web browser (Chrome, Firefox, Safari, or Edge)
- Stable internet connection
- Basic understanding of your organization's IT policies

### ⚡ 5-Minute Setup

Follow these steps to connect your Outlook account quickly:

1. **Access Krista Platform**
   - Log into your Krista account
   - Navigate to Extensions or Integrations section
   - Look for "Outlook Extension" or "Email Automation"

2. **Choose Authentication Method**
   - Select "Public Authentication" for quick setup
   - Choose "Private Authentication" for enterprise environments
   - Review the comparison table if unsure

3. **Enter Email Address**
   - Provide your Outlook email address
   - Ensure it's hosted on Microsoft platforms
   - Double-check for typos or formatting errors

4. **Complete Microsoft Authorization**
   - You'll be redirected to Microsoft's login page
   - Enter your credentials and complete any MFA
   - Review and accept the permission requests

5. **Verify Connection**
   - Return to Krista platform
   - Confirm connection status shows as "Active"
   - Test basic functionality

## 🔐 Authentication Options

### 🌐 Public Authentication

**Best for**: Individual users, small teams, quick pilots

**Features**:
- 5-minute setup process
- No IT involvement required
- Shared API limits
- Standard security features
- Self-service configuration

**When to Choose**:
- You're an individual user or small team
- You need immediate access
- Your organization allows third-party apps
- You have basic automation needs

### 🏢 Private Authentication

**Best for**: Large organizations, enterprise environments

**Features**:
- Custom organizational branding
- Dedicated API limits
- Enhanced security controls
- Centralized IT management
- Advanced compliance features

**When to Choose**:
- You're part of a large organization
- You need higher API limits
- Custom branding is important
- Enhanced security is required
- IT wants centralized control

## 🔧 Detailed Setup Process

### 📊 Step 1: Platform Access

1. **Login to Krista**
   - Navigate to your Krista instance URL
   - Enter your username and password
   - Complete any multi-factor authentication

2. **Navigate to Extensions**
   - Look for "Extensions," "Integrations," or "Connectors"
   - Find "Outlook Extension" or "Email Automation"
   - Click to access the configuration area

3. **Check Permissions**
   - Ensure you have rights to add new connections
   - Contact your Krista administrator if access is denied
   - Verify your account has the necessary licenses

### 🔑 Step 2: Authentication Setup

#### For Public Authentication:

1. **Select Public Option**
   - Choose "Public Authentication" from available options
   - Review the features and limitations
   - Confirm this meets your requirements

2. **Enter Email Details**
   - Provide your complete Outlook email address
   - Verify the email is correctly formatted
   - Ensure it's hosted on Microsoft platforms

3. **Initiate Connection**
   - Click "Connect" or "Authorize"
   - You'll be redirected to Microsoft's authorization page
   - Keep the original tab open

#### For Private Authentication:

1. **Prepare Azure Credentials**
   - Obtain Client ID from your Azure App Registration
   - Get Client Secret or certificate details
   - Confirm Tenant ID for your organization
   - Verify redirect URIs are configured

2. **Enter Application Details**
   - Input Client ID in the designated field
   - Provide Client Secret securely
   - Enter Tenant ID for your organization
   - Verify all details are accurate

3. **Test Configuration**
   - Use the "Test Connection" feature if available
   - Verify credentials are accepted
   - Confirm permissions are properly configured

### 🛡️ Step 3: Microsoft Authorization

1. **Microsoft Login Page**
   - Enter your Outlook email address
   - Provide your password securely
   - Complete any two-factor authentication

2. **Permission Review**
   - Carefully read the permission requests
   - Understand what access is being granted
   - Note the application name (Krista or your organization)

3. **Grant Consent**
   - Click "Accept" or "Allow" to grant permissions
   - Wait for the authorization to complete
   - You'll be redirected back to Krista

### ✅ Step 4: Verification and Testing

1. **Connection Status**
   - Verify the connection shows as "Active" or "Connected"
   - Check that your email address is displayed correctly
   - Note any warning messages or issues

2. **Basic Functionality Test**
   - Try reading a recent email
   - Test sending a simple email
   - Verify folder access works
   - Check attachment handling

3. **Automation Setup**
   - Create a simple test automation
   - Configure basic email rules
   - Test the automation with sample data
   - Monitor for any errors or issues

## 🔍 Troubleshooting Connection Issues

### 🚫 Common Problems

#### Authentication Failures

**Symptoms**:
- Cannot complete login process
- "Invalid credentials" errors
- Redirect loops or timeouts

**Solutions**:
1. Verify email address is correct and properly formatted
2. Ensure password is current and account is not locked
3. Try different browser or incognito mode
4. Check for browser extensions blocking the process
5. Verify network connectivity to Microsoft endpoints

#### Permission Denied

**Symptoms**:
- "Access denied" during authorization
- "Insufficient privileges" errors
- Cannot grant consent

**Solutions**:
1. Check if organization blocks third-party applications
2. Verify account has necessary Microsoft 365 licenses
3. Try connecting from organization network
4. Contact IT about conditional access policies
5. Consider Private Authentication for enterprise environments

#### Connection Timeouts

**Symptoms**:
- Setup process hangs or freezes
- Long delays during authorization
- Browser timeout errors

**Solutions**:
1. Check internet connection stability
2. Try from different network if possible
3. Clear browser cache and cookies
4. Disable VPN temporarily
5. Contact support if issues persist

### 🔧 Advanced Troubleshooting

#### Network Connectivity

Ensure access to these Microsoft endpoints:
- login.microsoftonline.com
- graph.microsoft.com
- outlook.office.com
- login.windows.net

#### Browser Requirements

- JavaScript must be enabled
- Cookies must be allowed
- Pop-ups should be permitted for Microsoft domains
- Modern browser version (updated within last 6 months)

#### Firewall and Proxy

- Whitelist Microsoft authentication domains
- Allow HTTPS traffic on port 443
- Configure proxy settings if required
- Check for SSL inspection interference

## 📊 Post-Connection Configuration

### ⚙️ Basic Settings

1. **Email Monitoring**
   - Choose which folders to monitor
   - Set up real-time vs. polling frequency
   - Configure email filtering rules
   - Define processing priorities

2. **Automation Rules**
   - Create basic email routing rules
   - Set up auto-response templates
   - Configure attachment handling
   - Define escalation procedures

3. **Notification Preferences**
   - Choose notification channels
   - Set alert thresholds
   - Configure error reporting
   - Define maintenance windows

### 🔒 Security Configuration

1. **Access Controls**
   - Review granted permissions
   - Set up user access levels
   - Configure approval workflows
   - Define audit requirements

2. **Data Handling**
   - Configure data retention policies
   - Set up encryption requirements
   - Define backup procedures
   - Establish compliance controls

## 📈 Monitoring and Maintenance

### 📊 Health Monitoring

1. **Connection Status**
   - Regularly check connection health
   - Monitor authentication token expiration
   - Track API usage and limits
   - Review error logs and alerts

2. **Performance Metrics**
   - Monitor email processing speed
   - Track automation success rates
   - Measure response times
   - Analyze usage patterns

### 🔄 Maintenance Tasks

1. **Regular Updates**
   - Keep authentication tokens current
   - Update automation rules as needed
   - Review and optimize performance
   - Test disaster recovery procedures

2. **Security Reviews**
   - Audit user access regularly
   - Review permission grants
   - Update security configurations
   - Conduct compliance assessments

## 🆘 Getting Support

### 📞 Support Channels

1. **Self-Service Resources**
   - Built-in connection testing tools
   - Comprehensive documentation
   - Video tutorials and guides
   - Community forums and discussions

2. **Direct Support**
   - Email support for technical issues
   - Phone support for urgent problems
   - Chat support during business hours
   - Escalation procedures for critical issues

### 📋 When Contacting Support

Provide the following information:
- Your Krista instance URL
- Email address being connected
- Authentication method used
- Error messages or screenshots
- Steps taken before the issue occurred
- Browser and operating system details

## 🚀 Next Steps

After successfully connecting your Outlook account:

1. **Explore Automation Features**
   - Review available automation templates
   - Create your first email workflow
   - Test with sample data
   - Gradually expand automation scope

2. **Optimize Performance**
   - Monitor usage patterns
   - Adjust settings based on needs
   - Implement best practices
   - Plan for scaling requirements

3. **Advanced Configuration**
   - Explore advanced features
   - Integrate with other systems
   - Set up custom workflows
   - Implement governance controls

Ready to transform your email workflows? Follow this guide to connect your Outlook account and start automating today!