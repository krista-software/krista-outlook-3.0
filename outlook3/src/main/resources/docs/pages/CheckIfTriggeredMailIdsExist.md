# Check If Triggered Mail Ids Exist

## Overview

Validates whether specific email message IDs exist and are accessible in the user's mailbox. This catalog request is used to verify that emails referenced in triggers, workflows, or notifications are still available for processing.

## Request Details

- **Area**: Messaging
- **Type**: QUERY_SYSTEM
- **Retry Support**: Yes - Failed requests can be retried automatically

## Input Parameters

| Parameter Name | Type | Required | Description | Example |
|----------------|------|----------|-------------|---------|
| Message IDs | List<Text> | Yes | List of email message IDs to validate | ["AQMkADY4ZTFi...", "AQMkADY4ZTFj..."] |

### Parameter Details

#### Message IDs
- **Format**: List of Base64-encoded strings from Microsoft Graph
- **Source**: Obtained from other catalog requests, triggers, or notifications
- **Validation**: Each ID must be valid message ID format
- **Limit**: Maximum 100 message IDs per request
- **Purpose**: Verify existence and accessibility of specific emails

## Output Parameters

| Parameter Name | Type | Description |
|----------------|------|-------------|
| Validation Results | List<Entity(Message Validation)> | Results for each message ID |

### Message Validation Entity Structure

Each validation result contains:

| Field Name | Type | Description |
|------------|------|-------------|
| Message ID | Text | The message ID that was validated |
| Exists | Boolean | Whether the message exists and is accessible |
| Status | Text | Detailed status of the validation |
| Error Details | Text | Error information if validation failed |

### Status Values

| Status | Description |
|--------|-------------|
| EXISTS | Message exists and is accessible |
| NOT_FOUND | Message does not exist or has been deleted |
| ACCESS_DENIED | Message exists but user lacks access |
| INVALID_ID | Message ID format is invalid |
| SYSTEM_ERROR | System error occurred during validation |

## Validation Rules

| Validation | Error Message | Resolution |
|------------|---------------|------------|
| Message IDs is empty | "Message IDs list cannot be empty" | Provide at least one message ID |
| Too many message IDs | "Maximum 100 message IDs allowed per request" | Reduce number of message IDs |
| Invalid message ID format | "Invalid message ID format" | Use proper message ID format |

## Usage Examples

### Example 1: Validate Single Message ID
**Scenario**: Check if specific email still exists before processing

**Input**:
```
Message IDs: ["AQMkADY4ZTFiMGIxLWU1YjUtNDEwMS04Y2Q0..."]
```

**Output**:
```json
{
  "Validation Results": [
    {
      "Message ID": "AQMkADY4ZTFiMGIxLWU1YjUtNDEwMS04Y2Q0...",
      "Exists": true,
      "Status": "EXISTS",
      "Error Details": null
    }
  ]
}
```

### Example 2: Validate Multiple Message IDs
**Scenario**: Validate list of emails from notification or trigger

**Input**:
```
Message IDs: [
  "AQMkADY4ZTFiMGIxLWU1YjUtNDEwMS04Y2Q0...",
  "AQMkADY4ZTFiMGIxLWU1YjUtNDEwMS04Y2Q1...",
  "AQMkADY4ZTFiMGIxLWU1YjUtNDEwMS04Y2Q2..."
]
```

**Output**:
```json
{
  "Validation Results": [
    {
      "Message ID": "AQMkADY4ZTFiMGIxLWU1YjUtNDEwMS04Y2Q0...",
      "Exists": true,
      "Status": "EXISTS",
      "Error Details": null
    },
    {
      "Message ID": "AQMkADY4ZTFiMGIxLWU1YjUtNDEwMS04Y2Q1...",
      "Exists": false,
      "Status": "NOT_FOUND",
      "Error Details": "Message has been deleted"
    },
    {
      "Message ID": "AQMkADY4ZTFiMGIxLWU1YjUtNDEwMS04Y2Q2...",
      "Exists": true,
      "Status": "EXISTS",
      "Error Details": null
    }
  ]
}
```

### Example 3: Handle Invalid Message IDs
**Scenario**: Validate list containing invalid message IDs

**Input**:
```
Message IDs: [
  "AQMkADY4ZTFiMGIxLWU1YjUtNDEwMS04Y2Q0...",
  "invalid-message-id"
]
```

**Output**:
```json
{
  "Validation Results": [
    {
      "Message ID": "AQMkADY4ZTFiMGIxLWU1YjUtNDEwMS04Y2Q0...",
      "Exists": true,
      "Status": "EXISTS",
      "Error Details": null
    },
    {
      "Message ID": "invalid-message-id",
      "Exists": false,
      "Status": "INVALID_ID",
      "Error Details": "Message ID format is invalid"
    }
  ]
}
```

## Business Rules

1. **Batch Validation**: Multiple message IDs are validated in single operation
2. **Access Control**: Only validates messages accessible to authenticated user
3. **Real-time Check**: Validation reflects current mailbox state
4. **Individual Results**: Each message ID gets individual validation result
5. **Error Isolation**: Invalid IDs don't affect validation of other IDs
6. **Status Reporting**: Detailed status provided for each validation

## Limitations

1. **Batch Size**: Maximum 100 message IDs per request
2. **Access Scope**: Only validates messages accessible to authenticated user
3. **Real-time Only**: No historical validation for deleted messages
4. **Rate Limits**: Subject to Microsoft Graph API rate limiting
5. **Network Dependency**: Requires network connectivity for validation
6. **Permission Based**: Results depend on user's mailbox permissions

## Best Practices

### 1. Batch Processing
- Group message ID validations into efficient batches
- Use maximum batch size (100) when possible
- Handle partial failures gracefully
- Implement proper error handling for each validation result

### 2. Error Handling
- Check individual validation results for each message ID
- Handle different status types appropriately
- Implement retry logic for system errors
- Log validation failures for troubleshooting

### 3. Performance Optimization
- Cache validation results when appropriate
- Avoid unnecessary re-validation of same message IDs
- Monitor validation performance and adjust batch sizes
- Use validation results to optimize downstream processing

### 4. Workflow Integration
- Validate message IDs before processing operations
- Use validation results to filter valid messages
- Implement proper cleanup for invalid or deleted messages
- Coordinate validation with other email operations

## Common Use Cases

### 1. Trigger Validation
```
Scenario: Validate emails referenced in workflow triggers
Action: Check if triggered email IDs still exist before processing
Result: Only process workflows for existing, accessible emails
```

### 2. Notification Processing
```
Scenario: Validate emails from webhook notifications
Action: Verify notification email IDs are still valid
Result: Process only valid notifications and handle deleted emails
```

### 3. Batch Operation Preparation
```
Scenario: Prepare for batch email operations
Action: Validate all target email IDs before batch processing
Result: Efficient batch processing with only valid emails
```

### 4. Data Integrity Checks
```
Scenario: Verify data integrity in email processing systems
Action: Periodically validate stored email IDs
Result: Maintain data integrity and clean up invalid references
```

## Validation Workflow

### Step 1: Collect Message IDs
1. Gather message IDs from triggers, notifications, or stored data
2. Group IDs into batches of up to 100
3. Prepare for validation processing

### Step 2: Validate Message IDs
1. Call Check If Triggered Mail Ids Exist with message ID batch
2. Process validation results for each message ID
3. Handle different status types appropriately

### Step 3: Process Results
1. Filter valid message IDs for further processing
2. Handle invalid or inaccessible message IDs
3. Update stored data based on validation results
4. Log validation outcomes for audit purposes

## Related Catalog Requests

- [Send Alert Using Notification Delta](pages/SendAlertUsingNotificationDelta.md) - Process validated notifications
- [Fetch Mail By Message Id](pages/FetchMailByMessageId.md) - Retrieve details for validated emails
- [Mark Message](pages/MarkMessage.md) - Process validated emails
- [Move Message](pages/MoveMessage.md) - Move validated emails

## Technical Implementation

### Helper Class
- **Class**: ValidationServiceImpl
- **Package**: app.krista.extensions.essentials.collaboration.outlook3.impl
- **Method**: checkIfTriggeredMailIdsExist(List<String> messageIds)
- **Service**: Microsoft Graph Mail API message validation

### Telemetry Metrics
- **MESSAGE_ID_VALIDATION_SUCCESS**: Successful validation operations
- **MESSAGE_ID_VALIDATION_FAILURE**: Failed validation operations
- **VALID_MESSAGE_IDS**: Count of valid message IDs
- **INVALID_MESSAGE_IDS**: Count of invalid message IDs
- **DELETED_MESSAGE_IDS**: Count of deleted message IDs

## Troubleshooting

### Validation Operation Failed
**Cause**: System or network issues
**Solution**:
1. Check network connectivity to Microsoft Graph
2. Verify authentication token validity
3. Retry operation after brief delay
4. Check system resources and performance

### All Message IDs Invalid
**Cause**: Incorrect message ID format or source
**Solution**:
1. Verify message ID format is correct
2. Check source of message IDs
3. Ensure message IDs are from same mailbox
4. Test with known valid message IDs

### Access Denied for Valid IDs
**Cause**: Permission or authentication issues
**Solution**:
1. Verify Mail.ReadWrite permission is granted
2. Check authentication token validity
3. Confirm user has access to the mailbox
4. Test with known accessible message IDs

### Performance Issues with Large Batches
**Cause**: Network latency or system load
**Solution**:
1. Reduce batch size for better performance
2. Implement parallel processing for multiple batches
3. Monitor validation response times
4. Optimize based on system performance

## See Also

- [Extension Configuration](pages/ExtensionConfiguration.md) - Setup and configuration
- [Authentication](pages/Authentication.md) - Authentication requirements
- [Send Alert Using Notification Delta](pages/SendAlertUsingNotificationDelta.md) - Process notifications
- [Fetch Mail By Message Id](pages/FetchMailByMessageId.md) - Retrieve validated emails
