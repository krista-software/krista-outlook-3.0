# Outlook3 Extension

## Overview

The Krista Outlook3 Extension is an advanced email collaboration platform that provides comprehensive integration with Microsoft Outlook through both simplified public authentication and full-featured private authentication modes. This extension enables sophisticated email management, automation, and real-time processing capabilities for enterprise-grade email workflows.

## Key Features

✅ **Dual Authentication Modes**
- Public Authentication for simplified setup and testing
- Private Authentication with full OAuth 2.0 and Azure AD integration
- Flexible authentication switching based on security requirements

✅ **Advanced Email Management**
- Send emails with dynamic HTML tables from entity data
- Reply, reply-all, and forward with advanced recipient management
- Comprehensive attachment handling and file processing
- Rich text and HTML email composition

✅ **Sophisticated Query Operations**
- Inbox retrieval with advanced preference filtering
- Sent items management and tracking
- Advanced email search with complex query syntax
- Real-time latest email monitoring

✅ **Message Organization & Management**
- Category assignment and management
- Message status tracking (read/unread)
- Email labeling and organization
- Message movement between folders

✅ **Asynchronous & Event Processing**
- Background email processing for large datasets
- Real-time email alerts and notifications
- Event-driven email workflows
- Triggered email ID tracking and validation

✅ **Enterprise Configuration**
- Comprehensive health checking and diagnostics
- Test connection validation
- Configuration management for both authentication modes
- Advanced telemetry and monitoring

## Quick Start Guide

### 1. Choose Authentication Mode

#### Public Authentication (Recommended for Testing)
- Simplified setup process
- No Azure AD application required
- Perfect for development and testing environments

#### Private Authentication (Recommended for Production)
- Full OAuth 2.0 security
- Azure AD application integration
- Enterprise-grade security and compliance

### 2. Setup Steps

#### For Public Authentication:
1. **Configure Extension** - [Set up public authentication](pages/ExtensionConfiguration.md#public-authentication)
2. **Test Connection** - Verify your setup is working correctly
3. **Start Using** - Begin with any of the 18+ catalog requests

#### For Private Authentication:
1. **Create Azure AD Application** - [Follow our guide](pages/CreatingOutlookApp.md)
2. **Configure Extension** - [Set up private authentication](pages/ExtensionConfiguration.md#private-authentication)
3. **Authenticate** - [Complete OAuth 2.0 authentication](pages/Authentication.md)
4. **Test Connection** - Verify your setup is working correctly
5. **Start Using** - Access all advanced features and catalog requests

### 3. Immediate Benefits
Once configured, you can immediately leverage 18+ specialized catalog requests for comprehensive email automation and management.

## Documentation Structure

### 📚 Getting Started
- [Extension Configuration](pages/ExtensionConfiguration.md) - Complete setup guide for both authentication modes
- [Authentication](pages/Authentication.md) - Public and private authentication flows
- [Creating Outlook App](pages/CreatingOutlookApp.md) - Azure AD application setup for private auth

### 📧 Email Management
- [Send Mail](pages/SendMail.md) - Send emails with attachments and recipients
- [Send Mail With Table](pages/SendMailWithTable.md) - Send emails with dynamic HTML tables
- [Reply To Mail](pages/ReplyToMail.md) - Reply to specific emails
- [Reply To Mail With CC and BCC](pages/ReplyToMailWithCCAndBCC.md) - Advanced reply with recipient control
- [Reply To All](pages/ReplyToAll.md) - Reply to all recipients
- [Reply To All With CC and BCC](pages/ReplyToAllWithCCAndBCC.md) - Reply-all with recipient management

### 🔍 Query Operations
- [Fetch Inbox](pages/FetchInbox.md) - Retrieve inbox emails with pagination
- [Fetch Inbox With Preferences](pages/FetchInboxWithPreferences.md) - Advanced inbox filtering
- [Fetch Sent](pages/FetchSent.md) - Get sent emails with pagination
- [Fetch Mail Details By Query](pages/FetchMailDetailsByQuery.md) - Search emails with advanced queries
- [Fetch Latest Mail](pages/FetchLatestMail.md) - Get most recent email within time window

### 📋 Message Management
- [Mark Message](pages/MarkMessage.md) - Mark emails as read/unread with labels
- [Mark Message Category And Status](pages/MarkMessageCategoryAndStatus.md) - Advanced message categorization
- [Add Category To Message](pages/AddCategoryToMessage.md) - Assign categories to emails
- [Move Message](pages/MoveMessage.md) - Move emails between folders

### ⚡ Async & Event Operations
- [Fetch Inbox Async](pages/FetchInboxAsync.md) - Asynchronous inbox retrieval for large datasets
- [Get Result](pages/GetResult.md) - Retrieve results from async operations
- [Send Alert Using Notification Delta](pages/SendAlertUsingNotificationDelta.md) - Real-time email notifications
- [Check If Triggered Mail Ids Exist](pages/CheckIfTriggeredMailIdsExist.md) - Validate triggered email IDs

### ⚙️ Setup & Configuration
- [Save Outlook Public Configuration](pages/SaveOutlookPublicConfiguration.md) - Configure public authentication
- [Save Outlook Private Configuration](pages/SaveOutlookPrivateConfiguration.md) - Configure private authentication
- [Health Check](pages/HealthCheck.md) - System health validation
- [Test Connection](pages/TestConnection.md) - Connection testing and diagnostics

## Support & Resources

### Version Information
- **Extension Version**: 3.5.1
- **Krista Service APIs Java**: 1.0.101
- **Global Catalog Version**: 1.0.48
- **Appliance Release Version**: 3.5.1

### API Information
- **Microsoft Graph API**: v1.0
- **OAuth 2.0**: Microsoft Azure AD implementation
- **Supported Scopes**: Mail.ReadWrite, Mail.Send, offline_access

### Authentication Modes
- **Public Authentication**: Simplified setup for testing and development
- **Private Authentication**: Full OAuth 2.0 with Azure AD for production

### Additional Resources
- [Release Notes](pages/release-notes.md) - Latest updates and bug fixes
- [Troubleshooting Guide](pages/ExtensionConfiguration.md#troubleshooting) - Common issues and solutions
- [Security Best Practices](pages/Authentication.md#security-best-practices) - Secure implementation guidelines

---

> **💡 Tip**: Start with the [Extension Configuration](pages/ExtensionConfiguration.md) guide to set up your connection, then explore the catalog requests based on your specific email automation needs. Choose Public Authentication for quick testing or Private Authentication for production deployments.