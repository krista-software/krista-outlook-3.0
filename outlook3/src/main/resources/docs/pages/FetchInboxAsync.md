# Fetch Inbox Async

## Overview

Initiates asynchronous retrieval of emails from the user's inbox for large datasets. This catalog request is designed for processing large volumes of emails without blocking operations, returning a task ID that can be used to retrieve results later.

## Request Details

- **Area**: Messaging
- **Type**: QUERY_SYSTEM
- **Retry Support**: Yes - Failed requests can be retried automatically

## Input Parameters

| Parameter Name | Type | Required | Description | Example |
|----------------|------|----------|-------------|---------|
| Page Size | Number | No | Number of emails to retrieve (maximum 1000) | 500 |

### Parameter Details

#### Page Size
- **Default**: 100 (if not specified)
- **Range**: 1 to 1000 emails
- **Validation**: Must be between 1 and 1000
- **Purpose**: Controls the number of emails retrieved in the async operation
- **Performance**: Larger page sizes may take longer to process

## Output Parameters

| Parameter Name | Type | Description |
|----------------|------|-------------|
| Task ID | Text | Unique identifier for the asynchronous operation |

**Example Output**: "async-task-12345-67890-abcdef"

## Validation Rules

| Validation | Error Message | Resolution |
|------------|---------------|------------|
| Page size < 1 | "Page size must be at least 1" | Use page size of 1 or greater |
| Page size > 1000 | "Page size cannot exceed 1000" | Reduce page size to 1000 or less |

## Usage Examples

### Example 1: Standard Async Inbox Fetch
**Scenario**: Retrieve 500 emails asynchronously for processing

**Input**:
```
Page Size: 500
```

**Output**:
```
Task ID: "async-inbox-fetch-20231201-143022-abc123"
```

### Example 2: Large Dataset Async Fetch
**Scenario**: Retrieve maximum emails for comprehensive processing

**Input**:
```
Page Size: 1000
```

**Output**:
```
Task ID: "async-inbox-fetch-20231201-143045-def456"
```

### Example 3: Default Async Fetch
**Scenario**: Use default settings for async inbox retrieval

**Input**: (No parameters provided)

**Output**:
```
Task ID: "async-inbox-fetch-20231201-143100-ghi789"
```

## Business Rules

1. **Asynchronous Processing**: Operation runs in background without blocking
2. **Task ID Generation**: Unique task ID is generated for each async operation
3. **Result Retrieval**: Use Get Result catalog request with task ID to retrieve emails
4. **Default Behavior**: Without page size, retrieves 100 emails
5. **Sorting**: Emails are retrieved in reverse chronological order (newest first)
6. **Access Control**: Only emails accessible to authenticated user are retrieved

## Limitations

1. **Page Size Limit**: Maximum 1000 emails per async operation
2. **Task Lifetime**: Async tasks have limited lifetime (typically 24 hours)
3. **Concurrent Tasks**: Limited number of concurrent async operations per user
4. **Result Storage**: Results are temporarily stored and must be retrieved promptly
5. **Rate Limits**: Subject to Microsoft Graph API rate limiting
6. **Memory Usage**: Large page sizes consume more system resources

## Best Practices

### 1. Task Management
- Store task IDs for result retrieval
- Implement proper task tracking and monitoring
- Set appropriate timeouts for task completion
- Handle task expiration gracefully

### 2. Performance Optimization
- Use appropriate page sizes based on processing requirements
- Monitor async operation performance and adjust accordingly
- Implement proper error handling for async failures
- Consider system load when initiating async operations

### 3. Result Processing
- Retrieve results promptly after task completion
- Implement proper error handling for result retrieval
- Process results in manageable chunks
- Clean up completed tasks to free resources

### 4. Error Handling
- Implement retry logic for failed async operations
- Monitor task status and handle timeouts
- Provide meaningful status updates to users
- Log async operations for troubleshooting

## Common Use Cases

### 1. Bulk Email Processing
```
Scenario: Process large volumes of emails for analysis or automation
Action: Initiate async fetch for large dataset
Result: Non-blocking retrieval of emails for batch processing
```

### 2. Data Migration
```
Scenario: Migrate email data to external systems
Action: Use async fetch to retrieve emails without blocking operations
Result: Efficient data migration with minimal system impact
```

### 3. Compliance Scanning
```
Scenario: Scan large email volumes for compliance requirements
Action: Async fetch followed by compliance analysis
Result: Comprehensive compliance scanning without system blocking
```

### 4. Email Analytics
```
Scenario: Analyze email patterns and trends across large datasets
Action: Retrieve emails asynchronously for analytics processing
Result: Detailed email analytics without impacting user operations
```

## Async Operation Workflow

### Step 1: Initiate Async Operation
1. Call Fetch Inbox Async with desired page size
2. Receive task ID for tracking
3. Store task ID for later result retrieval

### Step 2: Monitor Task Status
1. Use Get Result catalog request to check task status
2. Monitor for completion or failure
3. Handle task timeouts appropriately

### Step 3: Retrieve Results
1. Once task completes, use Get Result to retrieve emails
2. Process retrieved emails as needed
3. Clean up task resources

## Related Catalog Requests

- [Get Result](GetResult.md) - Retrieve results from async operations
- [Fetch Inbox](FetchInbox.md) - Synchronous inbox retrieval
- [Fetch Inbox With Preferences](FetchInboxWithPreferences.md) - Filtered inbox retrieval
- [Test Connection](TestConnection.md) - Verify system connectivity before async operations

## Technical Implementation

### Helper Class
- **Class**: AsyncMessagingAreaImpl
- **Package**: app.krista.extensions.essentials.collaboration.outlook3.impl
- **Method**: fetchInboxAsync(Double pageSize)
- **Service**: Microsoft Graph Mail API with asynchronous processing

### Telemetry Metrics
- **FETCH_INBOX_ASYNC_INITIATED**: Async operations started
- **FETCH_INBOX_ASYNC_COMPLETED**: Async operations completed
- **FETCH_INBOX_ASYNC_FAILED**: Async operations failed
- **ASYNC_TASK_DURATION**: Time taken for async operations

## Troubleshooting

### Async Operation Failed to Start
**Cause**: System overload or configuration issues
**Solution**:
1. Check system resources and load
2. Verify authentication and permissions
3. Reduce page size and retry
4. Check for concurrent operation limits

### Task ID Not Generated
**Cause**: System or API issues
**Solution**:
1. Verify network connectivity
2. Check authentication token validity
3. Retry operation after brief delay
4. Monitor system status and resources

### Long Processing Times
**Cause**: Large page size or system load
**Solution**:
1. Reduce page size for faster processing
2. Monitor system load and performance
3. Consider breaking into smaller operations
4. Check for network or API issues

### Task Timeout
**Cause**: Operation exceeded maximum processing time
**Solution**:
1. Reduce page size for faster completion
2. Check system performance and load
3. Retry with smaller dataset
4. Monitor task completion times

## See Also

- [Extension Configuration](ExtensionConfiguration.md) - Setup and configuration
- [Authentication](Authentication.md) - Authentication requirements
- [Get Result](GetResult.md) - Retrieve async operation results
- [Fetch Inbox](FetchInbox.md) - Synchronous inbox retrieval
