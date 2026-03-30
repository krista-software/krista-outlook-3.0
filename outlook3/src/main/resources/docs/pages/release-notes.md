# Release Notes

## Version 3.0.30 - Current Release

**Release Date**: March 2026

### Version Information

| Component                  | Version                      |
|----------------------------|------------------------------|
| Extension Version          | 3.0.30                       |
| Developer                  | Deepak Shingan, Simran Sethi |
| Krista Service APIs (Java) | 1.0.121                      |
| Global Catalog Version     | GC-2026.4.1                  |


### Performance Improvements

#### Access Token Caching in GraphServiceClientProvider

- **Problem**: Every Graph API request was making redundant KeyValueStore REST calls to fetch the refresh token and then calling Azure AD to acquire a new access token. During "Mail Received Alert" flow, this resulted in 6-24 KeyValueStore calls per request, causing performance degradation and 504 gateway timeouts.
- **Solution**: Implemented in-memory `ConcurrentHashMap`-based caching for `GraphServiceClient` instances with access token expiry tracking. Cache check is performed before the KeyValueStore call, avoiding both the refresh token fetch and Azure AD call when cache is valid.
- **Token Refresh Strategy**: Access tokens (valid for ~1 hour) are cached with a 5-minute buffer before expiry, meaning tokens refresh approximately every 55 minutes.
- **Cache Invalidation**: Stale cache entries are automatically removed on authentication errors to ensure re-authentication flows work correctly.
- **Impact**: Reduced KeyValueStore calls from 6-24 per request to 0 (cache hit) or 2 (cache miss, once per ~55 min). Eliminated redundant Azure AD calls.
- **Files Changed**: `GraphServiceClientProvider.java`

#### Provider Caching in AccountImpl

- **Problem**: `AccountImpl.getProvider()` was creating a new `GraphServiceClientProvider` on every call via `providerFactory.create()`, which loaded `OutlookAttributes` from KeyValueStore each time. This caused repeated REST calls for every Graph API operation.
- **Solution**: Added lazy initialization caching, matching the pattern already used in `MessagingAreaImpl`, `EmailImpl`, `EmailBuilderImpl`, and `FolderImpl`.
- **Files Changed**: `AccountImpl.java`

### Backward Compatibility

**100% Backward Compatible** - Access token caching is transparent to all existing workflows. Cache properly invalidates on authentication errors and token expiry. Same pattern already proven in MS Dynamics 365 extension.

### Breaking Changes

**None**

---

### Bug Fixes [KE-2869]

#### Fixed Mail Received Alert Not Firing Consistently

- **Problem**: The "Mail Received Alert" was not triggering consistently for incoming emails. Microsoft Graph webhook notifications were intermittently missed or duplicated.
- **Root Cause**: The duplicate message detection used a non-thread-safe `LinkedHashSet` for tracking recently processed mail IDs. When multiple webhook notifications arrived concurrently on different HTTP threads, race conditions in the `contains()` / `add()` / `remove()` sequence caused:
  - Missed inserts (message processed but not recorded, leading to duplicate processing)
  - `ConcurrentModificationException` during iteration for oldest ID eviction
  - Inconsistent state where messages were neither tracked nor triggered
- **Fix**: Replaced the `LinkedHashSet` with a self-evicting `LinkedHashMap`-backed set using `Collections.newSetFromMap()`. The `LinkedHashMap.removeEldestEntry()` automatically evicts the oldest entries when capacity is exceeded, eliminating the manual iterator-based eviction. Added `synchronized` to the `isDuplicateMessageID()` method to guarantee thread-safe compound check-and-add operations.
- **Impact**: Eliminates race conditions in duplicate detection, ensuring every unique email notification is reliably triggered exactly once.

1. **[WATCH-620] Graceful Handling of Expected Graph API Errors in Mail Alert Flow**
    - **Problem**: Webhook notifications for `mailReceivedAlert` sometimes deliver conversation IDs, deleted email IDs, or truncated/malformed IDs instead of valid message IDs. These caused `GraphServiceException` errors that were logged as `ERROR`, triggering false alerts in monitoring systems.
    - **Fix in `AccountImpl.getEmail()`**: Added a dedicated `GraphServiceException` catch block that classifies errors before falling through to the generic handler:
        - `ErrorItemNotFound` — email was deleted or moved before retrieval → logged as `WARN`
        - `ErrorInvalidOperation` — a conversation ID was used where a message ID is expected → logged as `WARN`
        - `ErrorInvalidIdMalformed` — truncated or malformed message ID from notification → logged as `WARN`
        - All other unexpected `GraphServiceException` errors → still logged as `ERROR`
    - **Fix in `AccountImpl.getEmailWithRetry()`**: Downgraded "could not be retrieved after N attempts" log from `ERROR` to `WARN` since the specific error was already logged by `getEmail()`
    - **Fix in `MessagingArea.mailReceivedAlert()`**:
        - Downgraded "Mail details not available" log from `ERROR` to `WARN` with an improved message describing the expected causes
        - Added a dedicated `catch (IllegalStateException)` block before the generic `catch (Exception)` block to prevent the same error from being double-logged at `ERROR` level
    - **Result**: Expected transient scenarios (deleted emails, conversation IDs, malformed IDs) no longer generate `ERROR` logs or trigger false monitoring alerts. Unexpected Graph API errors continue to log at `ERROR` for proper alerting.
    - **Files Changed**: `AccountImpl.java`, `MessagingArea.java`

### Backward Compatibility

**100% Backward Compatible** - All exception types are still thrown and propagated identically. Return values are unchanged. Only logging levels for known transient scenarios have been adjusted.

### Breaking Changes

**None**

---

## Version 3.0.29

**Release Date**: January 2026

### Version Information

| Component                  | Version     |
|----------------------------|-------------|
| Extension Version          | 3.0.29      |
| Developer                  | Krista Team |
| Krista Service APIs (Java) | 1.0.120     |
| Global Catalog Version     | GC-2026.2.1 |

### What's New

#### Bug Fixes

1. **Fixed NullPointerException in Delta Token Storage**
   - Fixed critical bug where clearing expired delta tokens caused `NullPointerException: Cannot invoke "Object.getClass()" because "value" is null`
   - Modified `storeDeltaLink()` method to use `remove()` instead of `put(null)` when clearing tokens
   - Added comprehensive logging for delta token storage and removal operations
   - This fix enables proper HTTP 410 error recovery when delta tokens expire after 7-30 days of inactivity
   - Created comprehensive test suite with 100% code coverage (13 test cases, all passing)

2. **Enhanced Delta Sync Error Handling and Logging**
   - Added comprehensive logging throughout the delta synchronization flow
   - Added specific error detection and handling for `MailboxNotEnabledForRESTAPI` errors
   - Added detailed logging for HTTP 410 (Gone) error recovery process
   - Added invoker email logging to help diagnose multi-invoker scenarios
   - Added duration tracking and message count metrics for delta queries
   - Improved error messages with actionable guidance for administrators


## Version 3.0.28

**Release Date**: January 2026

### Version Information

| Component                  | Version     |
|----------------------------|-------------|
| Extension Version          | 3.0.28      |
| Developer                  | Krista Team |
| Krista Service APIs (Java) | 1.0.120     |
| Global Catalog Version     | GC-2026.1.4 |

### What's New

#### Parallel Email Processing with Semaphore-Based Concurrency Control

* **Performance Improvement**
    - Implemented parallel processing using Java 21 Virtual Threads for `fetchMailsByLabel` catalog request
    - 10x faster than sequential processing (250s → 75-80s for 15 emails)
    - Semaphore with 10 concurrent permits limits simultaneous API calls
    - 33% reduction in peak memory usage
    - Prevents Microsoft Graph API rate limiting

* **Technical Implementation**
    - Java 21 Virtual Threads with `Executors.newVirtualThreadPerTaskExecutor()`
    - ThreadLocal context propagation for authentication
    - Graceful fallback to sequential processing on errors
    - 13 new unit tests, 100% code coverage

#### Delta Query Token Expiration - Automatic Recovery

**Problem Solved**: Microsoft Graph delta tokens expire after 7-30 days of inactivity, causing HTTP 410 Gone errors that previously required manual intervention.

**Solution**: Implemented automatic recovery mechanism that:
- Detects HTTP 410 Gone errors and SyncStateNotFound messages
- Automatically clears expired delta tokens
- Performs full synchronization to catch up on missed changes
- Stores new delta token for future incremental queries
- Logs as WARNING (not ERROR) for appropriate alerting

**Technical Details**:
- **File**: `AccountImpl.java` (lines 314-331)
- **Method**: `fetchNotificationDeltaQuery()`
- **Error Handling**: Catches `GraphServiceException` with response code 410
- **Recovery**: Automatic token cleanup and full sync restart
- **Logging**: Uses `LOGGER.warn()` for token expiration events

**Benefits**:
- ✅ Self-healing - no manual intervention required
- ✅ No data loss - full sync ensures all changes are captured
- ✅ Reduced noise - warning instead of error, only happens once
- ✅ Future-proof - handles token expiration whenever it occurs
- ✅ Efficient - returns to incremental sync after recovery

**Documentation**:
- New comprehensive guide: `GetNotificationDelta.md` (292 lines)
- Enhanced troubleshooting: `SendAlertUsingNotificationDelta.md`
- Architecture documentation: `ARCHITECTURE.md` with error flow diagram
- Implementation guide: `DELTA_TOKEN_ERROR_HANDLING.md`

**Microsoft Reference**: Follows official Microsoft Graph delta query guidance for handling 410 Gone responses.

**Impact**: Eliminates recurring error logs and manual token cleanup for delta query operations.

### Backward Compatibility

**100% Backward Compatible** - Both features provide automatic optimization and self-healing. No changes required to existing workflows.

### Breaking Changes

**None**

---

## Version 3.0.27 - Subscription Cleanup Service

**Release Date**: December 2026

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
| **3.0.30** | February 2026    | WATCH-620: Graceful handling of expected Graph API errors (ErrorItemNotFound, ErrorInvalidOperation, ErrorInvalidIdMalformed) to eliminate false monitoring alerts in mail alert flow |
| **3.0.29** | January 2026     | Fixed NullPointerException in Delta Token Storage + Enhanced Delta Sync Error Handling and Logging (100% test coverage) + Parallel Email Processing (10x performance with Java 21 Virtual Threads) + Delta Query Token Auto-Recovery |
| **3.0.28** | January 2026     | Parallel Email Processing (10x performance with Java 21 Virtual Threads, 33% memory reduction) + Delta Query Token Auto-Recovery (HTTP 410 error handling, self-healing) |
| **3.0.27** | December 2026    | Subscription Cleanup Service - Automatic deletion of orphaned Microsoft Graph subscriptions on credential changes, production-ready logging |
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
