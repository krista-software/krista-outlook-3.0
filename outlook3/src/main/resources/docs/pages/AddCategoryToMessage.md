# Add Category To Message

## Overview

Assigns a category to an email message using the message ID and category name. This catalog request provides email organization functionality by adding categories to emails for better classification and management.

## Request Details

- **Area**: Messaging
- **Type**: CHANGE_SYSTEM
- **Retry Support**: Yes - Failed requests can be retried automatically

## Input Parameters

| Parameter Name | Type | Required | Description | Example |
|----------------|------|----------|-------------|---------|
| Message ID | Text | Yes | Unique identifier of the email to categorize | "AQMkADY4ZTFiMGIxLWU1YjUtNDEwMS04Y2Q0..." |
| Category | Text | Yes | Category name to assign to the email | "Important" |

### Parameter Details

#### Message ID
- **Format**: Base64-encoded string from Microsoft Graph
- **Source**: Obtained from other catalog requests like Fetch Inbox, Fetch Mail Details By Query
- **Validation**: Must be a valid, existing message ID
- **Case Sensitivity**: Exact match required

#### Category
- **Format**: Category name as string
- **Validation**: Category must exist in the user's mailbox
- **Purpose**: Assign email to specific category for organization
- **Examples**: "Important", "Project Alpha", "Customer Service", "Follow-up"

## Output Parameters

| Parameter Name | Type | Description |
|----------------|------|-------------|
| Response | Text | Success confirmation message |

**Example Output**: "Success"

## Validation Rules

| Validation | Error Message | Resolution |
|------------|---------------|------------|
| Message ID is empty | "Message ID cannot be empty" | Provide a valid message ID |
| Message ID not found | "Unable to add category, no email found with messageID" | Verify message ID exists and is accessible |
| Category is empty | "Category cannot be empty" | Provide a valid category name |
| Category not found | "Category does not exist" | Use existing category or create category first |

## Usage Examples

### Example 1: Add Project Category
**Scenario**: Categorize project-related email

**Input**:
```
Message ID: "AQMkADY4ZTFiMGIxLWU1YjUtNDEwMS04Y2Q0..."
Category: "Project Alpha"
```

**Output**:
```
Response: "Success"
```

### Example 2: Add Priority Category
**Scenario**: Mark email as high priority

**Input**:
```
Message ID: "AQMkADY4ZTFiMGIxLWU1YjUtNDEwMS04Y2Q0..."
Category: "High Priority"
```

**Output**:
```
Response: "Success"
```

### Example 3: Add Customer Service Category
**Scenario**: Categorize customer inquiry

**Input**:
```
Message ID: "AQMkADY4ZTFiMGIxLWU1YjUtNDEwMS04Y2Q0..."
Category: "Customer Service"
```

**Output**:
```
Response: "Success"
```

## Business Rules

1. **Category Assignment**: Email is assigned to the specified category
2. **Category Validation**: Category must exist in user's mailbox before assignment
3. **Multiple Categories**: Email can have multiple categories (additive operation)
4. **Visual Impact**: Category assignment is reflected in email clients
5. **Permission Required**: User must have modify permissions for the email
6. **Immediate Effect**: Category assignment is applied immediately

## Limitations

1. **Message ID Validity**: Message ID must exist and be accessible to the authenticated user
2. **Modify Permissions**: User must have permission to modify the email
3. **Category Existence**: Category must already exist in the mailbox
4. **Single Category**: Operation assigns one category at a time
5. **Rate Limits**: Subject to Microsoft Graph API rate limiting

## Best Practices

### 1. Category Management
- Verify categories exist before using them
- Use consistent category naming conventions
- Create categories before assigning them to emails
- Consider category hierarchy and organization

### 2. Email Organization
- Use meaningful category names that reflect email content or purpose
- Implement consistent categorization rules across the organization
- Document category usage for team consistency
- Consider using multiple categories for complex classification

### 3. Workflow Integration
- Integrate category assignment into email processing workflows
- Use categories to trigger automated actions or routing
- Track category assignments for reporting and analytics
- Implement proper error handling for category operations

### 4. Performance Optimization
- Batch category assignments when processing multiple emails
- Cache category lists to avoid repeated API calls
- Validate categories before processing large batches
- Monitor category assignment performance

## Common Use Cases

### 1. Project Email Organization
```
Scenario: Organize emails by project for better tracking
Action: Assign project-specific categories to related emails
Result: Project emails are easily identifiable and filterable
```

### 2. Priority Classification
```
Scenario: Mark emails by priority level for processing order
Action: Assign priority categories (High, Medium, Low)
Result: Emails can be processed based on priority classification
```

### 3. Department Routing
```
Scenario: Route emails to appropriate departments
Action: Assign department categories for routing
Result: Emails are categorized for department-specific processing
```

### 4. Customer Service Classification
```
Scenario: Classify customer emails by service type
Action: Assign service-type categories to customer emails
Result: Customer emails are organized by service requirements
```

## Related Catalog Requests

- [Mark Message Category And Status](MarkMessageCategoryAndStatus.md) - Combined status and category marking
- [Mark Message](MarkMessage.md) - Basic message status marking
- [List Categories](ListCategories.md) - Get available categories
- [Fetch Mail Details By Query](FetchMailDetailsByQuery.md) - Search emails by category

## Technical Implementation

### Helper Class
- **Class**: MessagingAreaImpl
- **Package**: app.krista.extensions.essentials.collaboration.outlook3.impl
- **Method**: addCategoryToMessage(String messageId, String category)
- **Validation**: Message ID validation, category existence checking
- **Service**: Microsoft Graph Mail API message category functionality

### Telemetry Metrics
- **ADD_CATEGORY_SUCCESS**: Successful category assignments
- **ADD_CATEGORY_FAILURE**: Failed category assignments
- **CATEGORY_USAGE**: Usage statistics for different categories
- **CATEGORY_ASSIGNMENT_COUNT**: Number of category assignments

## Troubleshooting

### Category Not Found
**Cause**: Specified category doesn't exist in mailbox
**Solution**:
1. Verify category name spelling and case
2. Check if category exists in user's mailbox
3. Use List Categories to get available categories
4. Create category if it doesn't exist

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

### Category Assignment Failed
**Cause**: System or API issues
**Solution**:
1. Check network connectivity to Microsoft Graph
2. Verify Microsoft Graph service status
3. Retry operation after brief delay
4. Check authentication token validity

## See Also

- [Extension Configuration](ExtensionConfiguration.md) - Setup and configuration
- [Authentication](Authentication.md) - Authentication requirements
- [Mark Message Category And Status](MarkMessageCategoryAndStatus.md) - Combined operations
- [List Categories](ListCategories.md) - Get available categories
