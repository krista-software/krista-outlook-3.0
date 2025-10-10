# Release Notes

## Version 3.0.16 - Current Release

- **Developer**: Vaibhav Choudhary
- **Krista Service APIs (Java)**: 1.0.115
- **Global Catalog Version**: GC-2025.10.2
- **Release Date**: October 2025

### New Features

#### Allow Retry Parameter

Added optional **Allow Retry** boolean parameter to 19 catalog request methods, providing fine-grained control over validation error handling:

**Methods Updated:**
- **Messaging Operations**: Send Mail, Send Mail With Table, Reply To Mail, Reply To All, Reply To Mail With CC And BCC, Reply To All With CC And BCC, Forward Mail, Move Message, Mark Message
- **Fetch Operations**: Fetch Inbox, Fetch Inbox With Preferences, Fetch Sent, Fetch Mail By Message ID, Fetch Mail Details By Query, Fetch Mails By Label
- **Category Operations**: Add Category To Message, Remove Category From Message, Mark Message Category And Status
- **Label Operations**: Fetch All Labels (parameter added for API consistency)

**Behavior:**
- **When `Allow Retry = true`**: Validation errors trigger an interactive SubCatalog flow, prompting users to correct invalid inputs
- **When `Allow Retry = false` or `null` (default)**: Validation errors return immediately without user interaction
- **Backward Compatible**: Default behavior unchanged - existing implementations continue to work without modification

**Use Cases:**
- **Interactive Workflows**: Set to `true` for user-facing applications where users can correct errors in real-time
- **Automated Processes**: Set to `false` for batch processing and automated workflows that handle errors programmatically
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

**None** - This release is fully backward compatible. The Allow Retry parameter is optional and defaults to `false`, maintaining existing behavior.

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

| Version    | Release Date     | Key Features                                                                                                                             |
|------------|------------------|------------------------------------------------------------------------------------------------------------------------------------------|
| **3.0.16** | Current Release  | Added Retry Mechanism Flag for all catalog requests                                                                                      |
| **3.0.15** | Previous Release | Fetch Inbox Async was not returning any emails so we have made some logger changes due to excessive logging , Enhanced API documentation |
| **3.0.0**  | Major Release    | Complete platform redesign, OAuth 2.0 authentication, enhanced security, modern UI                                                       |

Stay updated with the latest features and improvements by subscribing to our release notifications! 
