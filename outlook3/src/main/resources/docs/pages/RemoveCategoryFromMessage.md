# Remove Category From Message

## Overview

Removes a category (label) from an email message using the message ID and category name. This catalog request allows you to
manage email categorization by removing categories that are no longer relevant or were applied incorrectly.

## Request Details

- **Area**: Messaging
- **Type**: CHANGE_SYSTEM
- **Retry Support**: Yes - Failed requests can be retried automatically

## Input Parameters

| Parameter Name | Type    | Required | Description                                                    | Example                                   |
|----------------|---------|----------|----------------------------------------------------------------|-------------------------------------------|
| Message ID     | Text    | Yes      | Unique identifier of the email                                 | "AQMkADY4ZTFiMGIxLWU1YjUtNDEwMS04Y2Q0..." |
| Category       | Text    | Yes      | Category name to remove from the email                         | "Important"                               |
| Allow Retry    | Boolean | No       | Enable interactive retry on validation errors (default: false) | true                                      |

### Parameter Details

#### Message ID

- **Format**: Base64-encoded string from Microsoft Graph
- **Source**: Obtained from other catalog requests like Fetch Inbox, Fetch Mail Details By Query
- **Validation**: Must be a valid, existing message ID
- **Case Sensitivity**: Exact match required

#### Category

- **Format**: Category name as string
- **Validation**: Category must exist in the user's mailbox
- **Case Sensitivity**: Category names are case-sensitive
- **Examples**: "Important", "Work", "Personal", "Follow Up"
- **Requirement**: Category must currently be assigned to the email

#### Allow Retry

- **Values**:
    - `true` - Prompt user to retry on validation errors
    - `false` or `null` - Return immediate error without retry option (default)
- **Purpose**: Controls error handling behavior for validation failures
- **Use Case**: Set to `true` for interactive workflows where users can correct invalid message IDs or category names
- **Default Behavior**: When not specified or `false`, validation errors return immediately

## Output Parameters

| Parameter Name   | Type    | Description                                      |
|------------------|---------|--------------------------------------------------|
| Category Removed | Boolean | Confirmation that category was removed (true)    |

**Example Output**: `Category Removed: true`

## Validation Rules

| Validation           | Error Message                                                     | Resolution                                 |
|----------------------|-------------------------------------------------------------------|--------------------------------------------|
| Message ID is empty  | "Message ID cannot be empty"                                      | Provide a valid message ID                 |
| Message ID not found | "Unable to remove category, no email found with given messageID"  | Verify message ID exists and is accessible |
| Category is empty    | "Category cannot be empty"                                        | Provide a valid category name              |
| Category not found   | "Category does not exist in mailbox"                              | Verify category exists                     |
| Category not assigned| "Category is not assigned to this email"                          | Verify category is currently on the email  |

## Error Handling

### Input Errors (INPUT_ERROR)

**Cause**: Invalid or missing required parameters
**Common Scenarios**:

- Empty or null message ID
- Empty or null category name
- Invalid message ID format
- Invalid category name format

**Resolution**: Validate all required parameters before submission

### Logic Errors (LOGIC_ERROR)

**Cause**: Business logic validation failures
**Common Scenarios**:

- Message ID not found in mailbox
- Category doesn't exist in user's mailbox
- Category not currently assigned to the email
- User lacks permission to modify the email

**Resolution**: Verify message ID exists, category exists, and is assigned to the email

### System Errors (SYSTEM_ERROR)

**Cause**: Microsoft Graph API or system-level failures
**Common Scenarios**:

- Network connectivity issues
- Microsoft Graph service unavailable
- Authentication token expired

**Resolution**: Retry the operation or check system connectivity

## Usage Examples

### Example 1: Remove Category from Email

**Scenario**: Remove "Important" category from processed email

**Input**:

```
Message ID: "AQMkADY4ZTFiMGIxLWU1YjUtNDEwMS04Y2Q0..."
Category: "Important"
Allow Retry: false
```

**Output**:

```
Category Removed: true
```

### Example 2: Remove Work Category

**Scenario**: Remove work categorization from personal email

**Input**:

```
Message ID: "AQMkADY4ZTFiMGIxLWU1YjUtNDEwMS04Y2Q0..."
Category: "Work"
Allow Retry: false
```

**Output**:

```
Category Removed: true
```

### Example 3: Interactive Retry on Invalid Category

**Scenario**: Allow user to correct invalid category name

**Input**:

```
Message ID: "AQMkADY4ZTFiMGIxLWU1YjUtNDEwMS04Y2Q0..."
Category: "NonExistentCategory"
Allow Retry: true
```

**Behavior**:
- System validates category and detects it doesn't exist
- User is prompted to re-enter correct category name
- User provides valid category and it's removed successfully

### Example 4: Automated Processing Without Retry

**Scenario**: Automated workflow that removes categories

**Input**:

```
Message ID: "AQMkADY4ZTFiMGIxLWU1YjUtNDEwMS04Y2Q0..."
Category: "Processed"
Allow Retry: false
```

**Behavior**:
- If validation fails, error is returned immediately
- Calling application handles error programmatically
- No user interaction required

## Business Rules

1. **Category Existence**: Category must exist in the user's mailbox
2. **Category Assignment**: Category must currently be assigned to the email
3. **Message Accessibility**: User must have access to the email
4. **Modify Permissions**: User must have permission to modify email categories
5. **Immediate Effect**: Category removal is applied immediately
6. **Multiple Categories**: Email can have multiple categories; this removes only the specified one
7. **Error Handling Control**: Allow Retry parameter controls whether validation errors trigger interactive retry prompts or immediate error returns

## Limitations

1. **Message ID Validity**: Message ID must exist and be accessible to the authenticated user
2. **Category Existence**: Category must exist in the user's mailbox
3. **Category Assignment**: Category must currently be assigned to the email
4. **Modify Permissions**: User must have permission to modify the email
5. **Single Category**: Operation removes only one category at a time
6. **Rate Limits**: Subject to Microsoft Graph API rate limiting

## Best Practices

### 1. Category Management

- Verify category exists before attempting removal
- Check category is assigned to email before removal
- Use consistent category naming conventions
- Handle cases where categories may be deleted

### 2. Message ID Handling

- Validate message ID exists before attempting removal
- Store message IDs from previous catalog requests
- Handle cases where emails may be deleted during processing
- Use consistent ID format throughout application

### 3. Error Handling

- Implement retry logic for transient failures
- Validate category existence and assignment before removal
- Handle cases where categories are removed by other processes
- Provide meaningful error messages to users

### 4. Workflow Integration

- Remove categories as part of larger processing workflows
- Consider category management strategies
- Implement proper logging for category operations
- Coordinate with other email management operations

## Common Use Cases

### 1. Email Processing Workflow

```
Scenario: Remove "Pending" category after processing email
Action: Process email content, then remove "Pending" category
Result: Email no longer marked as pending
```

### 2. Category Cleanup

```
Scenario: Remove incorrect or outdated categories
Action: Remove categories that are no longer relevant
Result: Email categorization is accurate and current
```

### 3. Workflow State Management

```
Scenario: Manage email workflow states with categories
Action: Remove old state category when moving to new state
Result: Email reflects current workflow state
```

### 4. Batch Category Removal

```
Scenario: Remove specific category from multiple emails
Action: Iterate through emails and remove category from each
Result: Category is removed from all specified emails
```

## Related Catalog Requests

- [Add Category To Message](pages/AddCategoryToMessage.md) - Add categories to emails
- [Mark Message Category And Status](pages/MarkMessageCategoryAndStatus.md) - Mark message with category and status
- [Fetch Mail By Message Id](pages/FetchMailByMessageId.md) - Retrieve email details
- [List Categories](pages/ListCategories.md) - Get available categories

## Technical Implementation

### Helper Class

- **Class**: MessagingAreaImpl
- **Package**: app.krista.extensions.essentials.collaboration.outlook3.impl
- **Method**: removeCategoryFromMessage(String messageID, String category, Boolean allowRetry)
- **Validation**: Message ID and category validation
- **Service**: Microsoft Graph Mail API category management functionality

### Telemetry Metrics

- **REMOVE_CATEGORY_SUCCESS**: Successful category removal operations
- **REMOVE_CATEGORY_FAILURE**: Failed category removal operations
- **CATEGORY_NOT_FOUND**: Category not found errors
- **CATEGORY_NOT_ASSIGNED**: Category not assigned to email errors

## Troubleshooting

### Message ID Not Found

**Cause**: Invalid or non-existent message ID
**Solution**:

1. Verify message ID is complete and correct
2. Check if email has been deleted
3. Ensure user has access to the email
4. Use Fetch Mail By Message Id to validate access

### Category Not Found

**Cause**: Invalid or non-existent category
**Solution**:

1. Verify category name spelling and case sensitivity
2. Check category exists in user's mailbox
3. Use List Categories to get available categories
4. Test with known existing categories

### Category Not Assigned

**Cause**: Category not currently on the email
**Solution**:

1. Verify category is assigned to the email
2. Check if category was already removed
3. Fetch email details to see current categories
4. Test with emails known to have the category

### Permission Denied

**Cause**: User lacks modify permissions
**Solution**:

1. Verify Mail.ReadWrite permission is granted
2. Check if user has modify access to the email
3. Confirm authentication token is valid
4. Test with modifiable emails

### Operation Failed

**Cause**: System or network issues
**Solution**:

1. Check network connectivity to Microsoft Graph
2. Verify Microsoft Graph service status
3. Retry operation after brief delay
4. Check authentication token validity

## See Also

- [Extension Configuration](pages/ExtensionConfiguration.md) - Setup and configuration
- [Authentication](pages/Authentication.md) - Authentication requirements
- [Add Category To Message](pages/AddCategoryToMessage.md) - Add categories to emails
- [Mark Message Category And Status](pages/MarkMessageCategoryAndStatus.md) - Advanced category management

