# Release Notes

## Version 3.0.27 - Current Release


*Release Date**: December 2026

### Version Information

| Component                  | Version     |
|----------------------------|-------------|
| Extension Version          | 3.0.27      |
| Developer                  | Krista Team |
| Krista Service APIs (Java) | 1.0.120     |
| Global Catalog Version     | GC-2026.1.4 |

### What's New

#### Subscription Cleanup Service

* **Automatic Subscription Cleanup on Credential Changes**
    - Implemented `SubscriptionCleanupService` to automatically delete orphaned Microsoft Graph subscriptions when users change their email address or update credentials
    - Previously, changing email addresses left active subscriptions in Microsoft Graph that couldn't be managed or deleted, potentially causing notification delivery issues and hitting subscription limits
    - New service detects email changes and automatically:
        - Deletes old subscriptions associated with the previous email
        - Creates new subscriptions for the updated email (if mail alerts are enabled)
        - Handles cleanup errors gracefully without blocking credential updates
    - Refactored `SaveConfigurationImpl` to use dedicated service layer for better separation of concerns
    - Added comprehensive logging at appropriate levels (INFO, DEBUG, WARN, ERROR) for production monitoring

#### Technical Improvements

* **Service Architecture**
    - New `SubscriptionCleanupService` with `@Service` annotation for dependency injection
    - Centralized subscription management logic
    - Proper error handling with try-catch blocks
    - Cleanup operations continue even if deletion fails

* **Logging Strategy**
    - INFO: Important business operations (email changes, subscription creation/deletion)
    - DEBUG: Detailed technical information (access tokens, auth context cleanup)
    - WARN: Failed operations that don't stop execution
    - ERROR: Exceptions with full stack traces

### Impact

**Bug Fix**: Prevents orphaned subscriptions in Microsoft Graph when users change their email address or update credentials. This resolves issues with:
- Unmanageable subscriptions that couldn't be deleted
- Potential notification delivery to wrong email addresses
- Hitting Microsoft Graph subscription limits (max 1000 per app)
- Confusion about active subscriptions in the system

### Backward Compatibility

**100% Backward Compatible** - Subscription cleanup is automatic and transparent. No changes required to existing workflows or configurations.

### Breaking Changes

**None**

---

## Version 3.0.26 - Previous Release

**Release Date**: December 2025

### Version Information

| Component                  | Version       |
|----------------------------|---------------|
| Extension Version          | 3.0.26        |
| Developer                  | Krista Team   |
| Krista Service APIs (Java) | 1.0.118       |
| Global Catalog Version     | GC-2025.12.01 |

---

### What's New

#### Documentation Updates

* **OAuth Scope Documentation Enhancement**
    - Added complete documentation for all 7 required OAuth scopes
    - Added `openid` scope documentation for user authentication
    - Added `Mail.Send.Shared` scope for shared mailbox send operations
    - Added `Mail.ReadWrite.Shared` scope for shared mailbox access
    - Added `MailboxSettings.ReadWrite` scope for mailbox configuration
    - Updated [Creating Outlook App](CreatingOutlookApp.md) guide with all required permissions
    - Updated [Authentication](Authentication.md) guide with detailed scope descriptions
    - Enhanced verification checklist to include all 7 permissions

#### Required OAuth Scopes

The extension now documents all required Microsoft Graph API scopes:

1. **openid** - Sign in users and read basic profile
2. **offline_access** - Maintain access to data when user is offline
3. **Mail.ReadWrite** - Read and write access to user mail
4. **Mail.Send** - Send mail as a user
5. **Mail.ReadWrite.Shared** - Read and write user and shared mail
6. **Mail.Send.Shared** - Send mail on behalf of others (shared mailboxes)
7. **MailboxSettings.ReadWrite** - Read and write user mailbox settings

### Backward Compatibility

**100% Backward Compatible** - No code changes, documentation updates only.

### Breaking Changes

**None**

---

## Version 3.0.25 - Previous Release

**Release Date**: December 2025

### Version Information

| Component                  | Version       |
|----------------------------|---------------|
| Extension Version          | 3.0.25        |
| Developer                  | Simran Sethi  |
| Krista Service APIs (Java) | 1.0.118       |
| Global Catalog Version     | GC-2025.12.01 |

### What's New

* [KE-2766] - MS Outlook - Need support for mail sensitivity
    - Added `Sensitivity` field to Mail Details entity to support filtering emails based on their sensitivity level (
      Normal, Personal, Private, Confidential), allowing partners to exclude personal or private emails from automated
      processing

---

## Version 3.0.23 - (Send Mail Retry Enhancement)

- **Developer**: Vaibhav Choudhary
- **Krista Service APIs (Java)**: 1.0.118
- **Global Catalog Version**: GC-2025.11.5
- **Release Date**: November 2025

### Bug Fixes

- **Send Mail 503 Queue Full Error Handling**
    - **Root Cause**: Microsoft Graph API returns HTTP 503 "Service Unavailable" with message "Application Request Queue
      Full" when their email infrastructure is temporarily overloaded
    - **Issue**: Send Mail operations were failing when Microsoft's server-side request queue was at capacity, even
      though the request was valid
    - **Solution**: Implemented automatic retry mechanism with exponential backoff for HTTP 503 errors
    - **Retry Strategy**:
        - Maximum 3 retry attempts
        - Exponential backoff delays: 2 seconds, 4 seconds, 8 seconds
        - Only retries on HTTP 503 "Queue Full" errors
        - Preserves original error for other failure types
    - **Impact**: Send Mail operations now automatically succeed when Microsoft's queue capacity becomes available,
      eliminating transient failures due to Microsoft infrastructure overload
    - **Affected Methods**:
        - Send Mail
        - Send Mail With Table
        - Reply To Mail
        - Reply To All
        - Reply To Mail With CC And BCC
        - Reply To All With CC And BCC
        - Forward Mail

### Technical Improvements

- Added resilient retry logic specifically for Microsoft Graph API 503 errors
- Implemented exponential backoff to avoid overwhelming Microsoft's infrastructure
- Enhanced error logging to distinguish between transient queue issues and permanent failures
- Improved telemetry tracking for retry attempts and success rates
- Maintained backward compatibility - retry is transparent to existing workflows

### Backward Compatibility

**100% Backward Compatible** – All existing catalog requests work exactly as before; retry mechanism is automatic and
transparent to users.

### Breaking Changes

**None**

---

## Version 3.0.22 - Previous Release (Email Validation Fix)

- **Developer**: Vaibhav Choudhary
- **Krista Service APIs (Java)**: 1.0.118
- **Global Catalog Version**: GC-2025.11.5
- **Release Date**: November 2025

### Bug Fixes

- **Send Mail 'To' field validation**
    - Fixed critical validation bug where invalid 'To' field inputs (such as " , " or ",,") were not caught during early
      validation.
    - Previously, these invalid inputs would pass validation and fail later at the Microsoft Graph API with "
      ErrorInvalidRecipients".
    - Now properly validates that at least one valid email address exists in the 'To' field before proceeding with the
      send operation.
    - Provides clear error messages when 'To' field is empty, contains only whitespace/commas, or has no valid email
      addresses.
    - Prevents unnecessary API calls and provides immediate feedback to users.

### Backward Compatibility

**100% Backward Compatible** – All existing catalog requests and behaviors remain unchanged; this release only fixes
validation logic for the 'To' field in Send Mail operations.

### Breaking Changes

**None**

---

## Version 3.0.21 - (Resilience & Mail Alert Improvements)

- **Developer**: Vaibhav Choudhary
- **Krista Service APIs (Java)**: 1.0.118
- **Global Catalog Version**: GC-2025.11.4
- **Release Date**: November 2025

### New Features & Improvements

- **Mail Received Alert reliability**
    - Extended attachment upload fallback to ZIP conversion on any media validation failure (for example, "Invalid
      image") so Mail Received Alert flows continue even when attachments fail standard processing.
    - Preserved existing behavior while adding concise logging around attachment handling.

- **Subscription renewal retry for folder monitoring**
    - Added 3-attempt retry with exponential backoff when renewing Microsoft Graph subscriptions.
    - Ensures temporary Graph or network issues do not immediately break folder monitoring subscription renewals.

- **Paginated email fetch retry**
    - Added 3-attempt retry with exponential backoff around Microsoft Graph paginated email retrieval in
      `Folder.getEmails(...)`.
    - Keeps existing paging and response behavior unchanged while improving resilience to transient Graph errors.

### Backward Compatibility

**100% Backward Compatible** – All existing catalog requests and behaviors remain unchanged; this release only adds
resilience and bug fixes on top of 3.0.20.

### Breaking Changes

**None**

---

## Version 3.0.20 - Current Release (Feature: Enhanced Folder Monitoring)

- **Developer**: Krista Development Team
- **Krista Service APIs (Java)**: 1.0.115+
- **Global Catalog Version**: GC-2025.11.3
- **Release Date**: TBD
- **Branch**: feature/outlook-3.0-Mail-Alert

### New Features

#### Enhanced Folder Monitoring and Email Change Notifications

Added 5 new catalog requests for advanced folder-based email workflow automation:

**New Catalog Requests:**

1. **Set Monitored Folders** - Configure which folders to monitor for notifications
2. **Get Monitored Folders** - Retrieve current monitoring configuration
3. **List All Folders** - Discover all available folders in mailbox
4. **Enable Folder Monitoring** - Create Microsoft Graph subscription (3-day validity)
5. **Receive notification of Email Change** - Event triggered when emails arrive or move to monitored folders

**Setup Sequence:**

```
1. Save Outlook Private Configuration
2. List All Folders (discover available folders)
3. Set Monitored Folders (configure specific folders)
4. Enable Folder Monitoring (create subscription)
5. Receive notification of Email Change (automatic event)
```

**Key Features:**

- Monitor specific folders or all folders
- Detect both new emails and moved emails
- Support for nested folders (e.g., "Inbox/Projects")
- Rich event metadata (subject, body, sender, recipients, attachments)
- Folder-based workflow routing

**Use Cases:**

- Customer support automation
- Sales lead processing
- Document workflow automation
- Priority escalation
- Compliance monitoring

### Technical Improvements

- New event type: `emailChangeNotification`
- Subscription with automatic retry (up to 3 attempts)
- New REST endpoints: `/rest/outlook/folderMonitoringNotification` and `/rest/outlook/folderLifecycleNotification`
- Enhanced OutlookAttributes with monitored folders list
- Support for both "created" and "updated" change types

### Documentation

- 5 new comprehensive documentation pages
- Prerequisites section with setup sequence
- Usage examples and troubleshooting guides
- Updated sidebar navigation

### Backward Compatibility

**100% Backward Compatible** - Original "Mail Received Alert" functionality unchanged. New features are opt-in only.

### Breaking Changes

**None**

---

## Version 3.0.17 - Previous Release

- **Developer**: Vaibhav Choudhary
- **Krista Service APIs (Java)**: 1.0.115
- **Global Catalog Version**: GC-2025.10.3
- **Release Date**: October 2025

### New Features

#### Allow Retry Parameter

Added optional **Allow Retry** boolean parameter to 19 catalog request methods, providing fine-grained control over
validation error handling:

**Methods Updated:**

- **Messaging Operations**: Send Mail, Send Mail With Table, Reply To Mail, Reply To All, Reply To Mail With CC And BCC,
  Reply To All With CC And BCC, Forward Mail, Move Message, Mark Message
- **Fetch Operations**: Fetch Inbox, Fetch Inbox With Preferences, Fetch Sent, Fetch Mail By Message ID, Fetch Mail
  Details By Query, Fetch Mails By Label
- **Category Operations**: Add Category To Message, Remove Category From Message, Mark Message Category And Status
- **Label Operations**: Fetch All Labels (parameter added for API consistency)

**Behavior:**

- **When `Allow Retry = true`**: Validation errors trigger an interactive SubCatalog flow, prompting users to correct
  invalid inputs
- **When `Allow Retry = false` or `null` (default)**: Validation errors return immediately without user interaction
- **Backward Compatible**: Default behavior unchanged - existing implementations continue to work without modification

**Use Cases:**

- **Interactive Workflows**: Set to `true` for user-facing applications where users can correct errors in real-time
- **Automated Processes**: Set to `false` for batch processing and automated workflows that handle errors
  programmatically
- **Flexible Error Handling**: Choose the appropriate error handling strategy based on workflow requirements

**Benefits:**

- **User Experience**: Improved UX for interactive workflows with guided error correction
- **Automation Friendly**: Faster error handling for automated processes without unnecessary prompts
- **Developer Control**: Fine-grained control over error handling behavior per request
- **Telemetry Tracking**: All retry operations tracked with comprehensive telemetry metrics

### Technical Improvements

- Enhanced validation error handling with conditional retry logic
- Improved telemetry tracking with `allow_retry` parameter in all metrics
- Comprehensive logging of retry behavior for debugging and monitoring
- Updated API documentation with detailed parameter descriptions and usage examples

### Testing

- 12 new comprehensive unit tests for Allow Retry functionality
- 100% test pass rate across all 58 tests
- Full backward compatibility validation
- Integration testing for SubCatalog retry flows

### Documentation

- Updated 14 method documentation files with Allow Retry parameter details
- Added usage examples for interactive and automated scenarios
- Enhanced parameter descriptions and business rules
- Complete API reference documentation

### Breaking Changes

**None** - This release is fully backward compatible. The Allow Retry parameter is optional and defaults to `false`,
maintaining existing behavior.

### Migration Guide

No migration required. To use the new Allow Retry feature:

1. Add the optional `Allow Retry` parameter to your catalog request calls
2. Set to `true` for interactive workflows where users can correct errors
3. Set to `false` or omit for automated workflows (default behavior)

**Example:**

```
// Interactive workflow - allow user to retry on errors
Move Message(messageId, folderName, allowRetry: true)

// Automated workflow - return errors immediately
Move Message(messageId, folderName, allowRetry: false)
```

---

## Version 3.0.15 - Previous Release

### Improvements

- Enhanced API documentation

### Bug Fixes

- Fetch Inbox Async was not returning any emails so we have made some logger changes due to excessive logging

---

## Version 3.0.0 - Major Release

### New Features

- Complete platform redesign
- OAuth 2.0 authentication implementation
- Enhanced security architecture
- Modern user interface

### Improvements

- Significant performance improvements
- Better scalability and reliability
- Enhanced API documentation
- Improved error handling

### Bug Fixes

- Resolved legacy authentication issues
- Fixed email synchronization problems
- Corrected API endpoint inconsistencies
- Fixed mobile browser compatibility

---

## Support and Feedback

### Getting Help

- Documentation and guides available online
- Community forum for user discussions
- Email support for technical issues
- Phone support for enterprise customers

### Feature Requests

- Submit requests through our product portal
- Email suggestions to our product team
- Discuss ideas in the community forum
- Work with customer success for enterprise features

### Bug Reports

- Submit detailed reports through our support portal
- Email critical issues directly to support
- Check our status page for known issues
- Use emergency contact for production issues

---

## Version History Summary

| Version    | Release Date     | Key Features                                                                                                                   |
|------------|------------------|--------------------------------------------------------------------------------------------------------------------------------|
| **3.0.27** | Current Release  | Subscription Cleanup Service - Automatic deletion of orphaned Microsoft Graph subscriptions on credential changes, production-ready logging |
| **3.0.26** | December 2025    | OAuth Scope Documentation - Complete documentation for all 7 required OAuth scopes including shared mailbox permissions       |
| **3.0.25** | December 2025    | Mail Sensitivity Support - Added Sensitivity field to filter emails by sensitivity level                                       |
| **3.0.23** | November 2025    | Send Mail Retry Enhancement - Automatic retry with exponential backoff for HTTP 503 Queue Full errors from Microsoft Graph API |
| **3.0.22** | November 2025    | Email Validation Fix - Enhanced 'To' field validation in Send Mail operations                                                  |
| **3.0.21** | November 2025    | Resilience & Mail Alert Improvements - Retry mechanisms for subscriptions and email fetch                                      |
| **3.0.20** | November 2025    | Enhanced Folder Monitoring - 5 new catalog requests for folder-based email workflow automation                                 |
| **3.0.17** | Previous Release | Added Allow Retry parameter for validation error handling                                                                      |
| **3.0.16** | October 2025     | Added Retry Mechanism Flag for all catalog requests                                                                            |
| **3.0.15** | Previous Release | Fetch Inbox Async bug fix, Enhanced API documentation                                                                          |
| **3.0.0**  | Major Release    | Complete platform redesign, OAuth 2.0 authentication, enhanced security, modern UI                                             |

Stay updated with the latest features and improvements by subscribing to our release notifications! 
