# Get Result

## Overview

Retrieves the results of an asynchronous operation using the task ID. This catalog request allows you to check the
status and retrieve the results of async operations initiated by other catalog requests such as Fetch Inbox Async.

## Request Details

- **Area**: Messaging
- **Type**: QUERY_SYSTEM
- **Retry Support**: Yes - Failed requests can be retried automatically

## Input Parameters

| Parameter Name | Type | Required | Description                                     | Example                                    |
|----------------|------|----------|-------------------------------------------------|--------------------------------------------|
| Task ID        | Text | Yes      | Unique identifier of the asynchronous operation | "async-inbox-fetch-20231201-143022-abc123" |

### Parameter Details

#### Task ID

- **Format**: String identifier returned by async operations
- **Source**: Obtained from async catalog requests like Fetch Inbox Async
- **Validation**: Must be a valid, existing task ID
- **Case Sensitivity**: Exact match required
- **Lifetime**: Task IDs have limited lifetime (typically 24 hours)

## Output Parameters

| Parameter Name | Type                 | Description                                       |
|----------------|----------------------|---------------------------------------------------|
| Status         | Text                 | Current status of the async operation             |
| Result         | Entity(Async Result) | Results of the completed operation (if available) |

### Status Values

| Status    | Description                                          |
|-----------|------------------------------------------------------|
| PENDING   | Operation is still in progress                       |
| COMPLETED | Operation completed successfully                     |
| FAILED    | Operation failed with errors                         |
| EXPIRED   | Task has expired and results are no longer available |

### Async Result Entity Structure

When status is COMPLETED, the Result entity contains:

| Field Name      | Type         | Description                                     |
|-----------------|--------------|-------------------------------------------------|
| Operation Type  | Text         | Type of async operation that was performed      |
| Completion Time | Date         | When the operation completed                    |
| Item Count      | Number       | Number of items returned                        |
| Data            | List<Entity> | The actual results (emails, etc.)               |
| Error Details   | Text         | Error information if operation partially failed |

## Validation Rules

| Validation             | Error Message               | Resolution                                         |
|------------------------|-----------------------------|----------------------------------------------------|
| Task ID is empty       | "Task ID cannot be empty"   | Provide a valid task ID                            |
| Task ID not found      | "Task not found or expired" | Verify task ID is correct and not expired          |
| Invalid task ID format | "Invalid task ID format"    | Use task ID exactly as returned by async operation |

## Usage Examples

### Example 1: Check Pending Operation

**Scenario**: Check status of ongoing async inbox fetch

**Input**:

```
Task ID: "async-inbox-fetch-20231201-143022-abc123"
```

**Output**:

```json
{
  "Status": "PENDING",
  "Result": null
}
```

### Example 2: Retrieve Completed Results

**Scenario**: Get results from completed async operation

**Input**:

```
Task ID: "async-inbox-fetch-20231201-143022-abc123"
```

**Output**:

```json
{
  "Status": "COMPLETED",
  "Result": {
    "Operation Type": "Fetch Inbox Async",
    "Completion Time": 1640995200000,
    "Item Count": 500,
    "Data": [
      {
        "From": "sender@company.com",
        "To": "user@company.com",
        "Subject": "Project Update",
        "Message ID": "AQMkADY4ZTFiMGIxLWU1YjUtNDEwMS04Y2Q0...",
        "Is Read": false
      }
    ]
  }
}
```

### Example 3: Handle Failed Operation

**Scenario**: Check results of failed async operation

**Input**:

```
Task ID: "async-inbox-fetch-20231201-143022-abc123"
```

**Output**:

```json
{
  "Status": "FAILED",
  "Result": {
    "Operation Type": "Fetch Inbox Async",
    "Completion Time": 1640995200000,
    "Item Count": 0,
    "Error Details": "Authentication token expired during operation"
  }
}
```

## Business Rules

1. **Task Lifecycle**: Tasks have limited lifetime and expire after period of inactivity
2. **Result Availability**: Results are available only after operation completes
3. **Single Retrieval**: Results should be retrieved promptly after completion
4. **Status Polling**: Can be called multiple times to check operation progress
5. **Error Preservation**: Failed operations preserve error details for troubleshooting
6. **Resource Cleanup**: Completed tasks are eventually cleaned up by system

## Limitations

1. **Task Expiration**: Tasks expire after limited time (typically 24 hours)
2. **Result Storage**: Results are temporarily stored and should be retrieved promptly
3. **Polling Frequency**: Avoid excessive polling to prevent rate limiting
4. **Concurrent Access**: Multiple calls with same task ID return same results
5. **Rate Limits**: Subject to Microsoft Graph API rate limiting
6. **Memory Usage**: Large result sets consume system resources

## Best Practices

### 1. Polling Strategy

- Implement reasonable polling intervals (every 30-60 seconds)
- Use exponential backoff for failed requests
- Stop polling once operation completes or fails
- Handle task expiration gracefully

### 2. Result Processing

- Retrieve results immediately after completion
- Process large result sets in manageable chunks
- Implement proper error handling for result processing
- Store results locally if needed for further processing

### 3. Error Handling

- Handle all possible status values appropriately
- Implement retry logic for transient failures
- Provide meaningful status updates to users
- Log async operation results for troubleshooting

### 4. Resource Management

- Clean up local references to completed tasks
- Avoid storing large result sets in memory unnecessarily
- Implement proper timeout handling for long-running operations
- Monitor system resources during result processing

## Common Use Cases

### 1. Async Operation Monitoring

```
Scenario: Monitor progress of long-running email fetch operation
Action: Periodically call Get Result to check status
Result: Real-time status updates on async operation progress
```

### 2. Batch Processing Workflow

```
Scenario: Process large email datasets retrieved asynchronously
Action: Wait for completion then retrieve and process results
Result: Efficient batch processing of large email volumes
```

### 3. User Interface Updates

```
Scenario: Provide status updates to users for long operations
Action: Poll async status and update UI accordingly
Result: Users receive real-time feedback on operation progress
```

### 4. Error Recovery

```
Scenario: Handle and recover from failed async operations
Action: Check failed operation details and implement recovery
Result: Robust error handling and recovery for async workflows
```

## Async Operation Workflow

### Step 1: Initiate Async Operation

1. Call async catalog request (e.g., Fetch Inbox Async)
2. Receive and store task ID
3. Begin monitoring operation

### Step 2: Monitor Operation Progress

1. Periodically call Get Result with task ID
2. Check status for PENDING, COMPLETED, FAILED, or EXPIRED
3. Handle each status appropriately

### Step 3: Process Results

1. When status is COMPLETED, retrieve result data
2. Process results according to business requirements
3. Clean up task references

## Related Catalog Requests

- [Fetch Inbox Async](pages/FetchInboxAsync.md) - Initiate async inbox retrieval
- [Send Alert Using Notification Delta](pages/SendAlertUsingNotificationDelta.md) - Async notification operations
- [Check If Triggered Mail Ids Exist](pages/CheckIfTriggeredMailIdsExist.md) - Async validation operations
- [Test Connection](pages/TestConnection.md) - Verify connectivity before async operations

## Technical Implementation

### Helper Class

- **Class**: AsyncResultServiceImpl
- **Package**: app.krista.extensions.essentials.collaboration.outlook3.impl
- **Method**: getResult(String taskId)
- **Service**: Async task management and result retrieval

### Telemetry Metrics

- **GET_RESULT_SUCCESS**: Successful result retrievals
- **GET_RESULT_FAILURE**: Failed result retrievals
- **TASK_STATUS_PENDING**: Count of pending status checks
- **TASK_STATUS_COMPLETED**: Count of completed status checks
- **TASK_STATUS_FAILED**: Count of failed status checks

## Troubleshooting

### Task Not Found

**Cause**: Invalid task ID or expired task
**Solution**:

1. Verify task ID is correct and complete
2. Check if task has expired (typically after 24 hours)
3. Ensure task was successfully created initially
4. Re-initiate async operation if task expired

### Status Always Pending

**Cause**: Long-running operation or system issues
**Solution**:

1. Check if operation is still within expected timeframe
2. Verify system resources and performance
3. Monitor for system or network issues
4. Consider operation timeout and retry if necessary

### Failed to Retrieve Results

**Cause**: System or network issues
**Solution**:

1. Check network connectivity
2. Verify authentication token validity
3. Retry operation after brief delay
4. Check system status and resources

### Large Result Processing Issues

**Cause**: Memory or performance constraints
**Solution**:

1. Process results in smaller chunks
2. Implement streaming or pagination for large datasets
3. Monitor system resources during processing
4. Consider breaking large operations into smaller ones

## See Also

- [Extension Configuration](pages/ExtensionConfiguration.md) - Setup and configuration
- [Authentication](pages/Authentication.md) - Authentication requirements
- [Fetch Inbox Async](pages/FetchInboxAsync.md) - Initiate async operations
- [Test Connection](pages/TestConnection.md) - Verify system connectivity
