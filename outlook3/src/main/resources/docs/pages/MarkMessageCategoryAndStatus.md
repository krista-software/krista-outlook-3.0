# Mark Message Category And Status

## Overview

Marks an email message with both read/unread status and category assignment in a single operation. This catalog request
provides comprehensive email management functionality for organizing and tracking email status with category-based
organization.

## Request Details

- **Area**: Messaging
- **Type**: CHANGE_SYSTEM
- **Retry Support**: Yes - Failed requests can be retried automatically

## Input Parameters

| Parameter Name | Type    | Required | Description                            | Example                                   |
|----------------|---------|----------|----------------------------------------|-------------------------------------------|
| Message ID     | Text    | Yes      | Unique identifier of the email to mark | "AQMkADY4ZTFiMGIxLWU1YjUtNDEwMS04Y2Q0..." |
| Is Read        | Boolean | Yes      | Mark as read (true) or unread (false)  | true                                      |
| Category       | Text    | No       | Category name to assign to the email   | "Important"                               |

### Parameter Details

#### Message ID

- **Format**: Base64-encoded string from Microsoft Graph
- **Source**: Obtained from other catalog requests like Fetch Inbox, Fetch Mail Details By Query
- **Validation**: Must be a valid, existing message ID
- **Case Sensitivity**: Exact match required

#### Is Read

- **Values**:
    - `true` - Mark email as read
    - `false` - Mark email as unread
- **Purpose**: Update the read status of the email
- **Effect**: Changes the visual appearance in email clients

#### Category

- **Format**: Category name as string
- **Validation**: Category must exist in the user's mailbox
- **Purpose**: Assign email to specific category for organization
- **Optional**: If not provided, only read status is updated

## Output Parameters

| Parameter Name | Type | Description                  |
|----------------|------|------------------------------|
| Response       | Text | Success confirmation message |

**Example Output**: "Success"

## Validation Rules

| Validation           | Error Message                                           | Resolution                                     |
|----------------------|---------------------------------------------------------|------------------------------------------------|
| Message ID is empty  | "Message ID cannot be empty"                            | Provide a valid message ID                     |
| Message ID not found | "Unable to mark message, no email found with messageID" | Verify message ID exists and is accessible     |
| Is Read is null      | "Is Read parameter is required"                         | Provide true or false value                    |
| Category not found   | "Category does not exist"                               | Use existing category or create category first |

## Usage Examples

### Example 1: Mark as Read with Category

**Scenario**: Mark processed email as read and assign to "Completed" category

**Input**:

```
Message ID: "AQMkADY4ZTFiMGIxLWU1YjUtNDEwMS04Y2Q0..."
Is Read: true
Category: "Completed"
```

**Output**:

```
Response: "Success"
```

### Example 2: Mark as Unread with Priority Category

**Scenario**: Mark important email as unread and assign to "High Priority" category

**Input**:

```
Message ID: "AQMkADY4ZTFiMGIxLWU1YjUtNDEwMS04Y2Q0..."
Is Read: false
Category: "High Priority"
```

**Output**:

```
Response: "Success"
```

### Example 3: Update Status Only

**Scenario**: Mark email as read without changing category

**Input**:

```
Message ID: "AQMkADY4ZTFiMGIxLWU1YjUtNDEwMS04Y2Q0..."
Is Read: true
```

**Output**:

```
Response: "Success"
```

## Business Rules

1. **Combined Operation**: Both read status and category are updated in single operation
2. **Category Validation**: Category must exist in user's mailbox before assignment
3. **Status Update**: Email read status is immediately updated in the mailbox
4. **Visual Impact**: Changes are reflected in email clients
5. **Permission Required**: User must have modify permissions for the email
6. **Atomic Operation**: Both status and category are updated together or operation fails

## Limitations

1. **Message ID Validity**: Message ID must exist and be accessible to the authenticated user
2. **Modify Permissions**: User must have permission to modify the email
3. **Category Existence**: Category must already exist in the mailbox
4. **Single Email**: Operation affects only one email at a time
5. **Rate Limits**: Subject to Microsoft Graph API rate limiting

## Best Practices

### 1. Category Management

- Verify categories exist before using them
- Use consistent category naming conventions
- Create categories before assigning them to emails
- Consider category hierarchy and organization

### 2. Workflow Integration

- Use combined operation for efficiency
- Implement proper error handling for both status and category operations
- Track category assignments for reporting
- Consider batch processing for multiple emails

### 3. Status and Category Coordination

- Use meaningful category names that reflect email status
- Coordinate read status with category assignments
- Implement consistent categorization rules
- Document category usage for team consistency

## Common Use Cases

### 1. Email Processing Workflow

```
Scenario: Mark emails as processed and categorize by completion status
Action: Mark as read and assign "Processed" category
Result: Clear visual indication of processed emails with proper categorization
```

### 2. Priority Email Management

```
Scenario: Mark urgent emails as unread and assign priority category
Action: Mark as unread with "Urgent" category
Result: High-priority emails remain visible with clear priority indication
```

### 3. Project Email Organization

```
Scenario: Organize project emails by status and category
Action: Mark with appropriate read status and project category
Result: Project emails organized by both status and project association
```

## Related Catalog Requests

- [Mark Message](pages/MarkMessage.md) - Basic message status marking
- [Add Category To Message](pages/AddCategoryToMessage.md) - Category assignment only
- [Fetch Inbox With Preferences](pages/FetchInboxWithPreferences.md) - Filter emails by read status
- [List Categories](pages/ListCategories.md) - Get available categories

## Technical Implementation

### Helper Class

- **Class**: MessagingAreaImpl
- **Package**: app.krista.extensions.essentials.collaboration.outlook3.impl
- **Method**: markMessageCategoryAndStatus(String messageId, Boolean isRead, String category)
- **Validation**: Message ID validation, category existence checking
- **Service**: Microsoft Graph Mail API message update functionality

### Telemetry Metrics

- **MARK_MESSAGE_CATEGORY_STATUS_SUCCESS**: Successful combined operations
- **MARK_MESSAGE_CATEGORY_STATUS_FAILURE**: Failed combined operations
- **CATEGORY_ASSIGNMENT**: Count of category assignments
- **STATUS_CATEGORY_COMBINED**: Count of combined status and category updates

## Troubleshooting

### Category Not Found

**Cause**: Specified category doesn't exist in mailbox
**Solution**:

1. Verify category name spelling and case
2. Check if category exists in user's mailbox
3. Create category if it doesn't exist
4. Use existing category names

### Message ID Not Found

**Cause**: Invalid or non-existent message ID
**Solution**:

1. Verify message ID is complete and correct
2. Check if email has been deleted or moved
3. Ensure user has access to the email
4. Use Fetch Mail By Message Id to validate access

### Permission Denied

**Cause**: User lacks permission to modify the email or assign categories
**Solution**:

1. Verify Mail.ReadWrite permission is granted
2. Check if user owns or has access to the email
3. Confirm authentication token is valid
4. Test with known modifiable emails

## See Also

- [Extension Configuration](pages/ExtensionConfiguration.md) - Setup and configuration
- [Authentication](pages/Authentication.md) - Authentication requirements
- [Mark Message](pages/MarkMessage.md) - Basic message status marking
- [Add Category To Message](pages/AddCategoryToMessage.md) - Category assignment functionality
