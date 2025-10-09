# Move Message

## Overview

Moves an email message from one folder to another using the message ID and destination folder name. This catalog request
allows you to organize emails by moving them between different folders in the mailbox.

## Request Details

- **Area**: Messaging
- **Type**: CHANGE_SYSTEM
- **Retry Support**: Yes - Failed requests can be retried automatically

## Input Parameters

| Parameter Name | Type | Required | Description                            | Example                                   |
|----------------|------|----------|----------------------------------------|-------------------------------------------|
| Message ID     | Text | Yes      | Unique identifier of the email to move | "AQMkADY4ZTFiMGIxLWU1YjUtNDEwMS04Y2Q0..." |
| Folder Name    | Text | Yes      | Name of the destination folder         | "Archive"                                 |

### Parameter Details

#### Message ID

- **Format**: Base64-encoded string from Microsoft Graph
- **Source**: Obtained from other catalog requests like Fetch Inbox, Fetch Mail Details By Query
- **Validation**: Must be a valid, existing message ID
- **Case Sensitivity**: Exact match required

#### Folder Name

- **Format**: Folder name as it appears in the mailbox
- **Examples**: "Inbox", "Sent Items", "Drafts", "Archive", "Projects"
- **Subfolders**: Use forward slash notation (e.g., "Projects/2023")
- **Case Sensitivity**: Must match exact folder name
- **Validation**: Folder must exist in the user's mailbox

## Output Parameters

| Parameter Name | Type | Description                  |
|----------------|------|------------------------------|
| Response       | Text | Success confirmation message |

**Example Output**: "Success"

## Validation Rules

| Validation           | Error Message                                           | Resolution                                 |
|----------------------|---------------------------------------------------------|--------------------------------------------|
| Message ID is empty  | "Message ID cannot be empty"                            | Provide a valid message ID                 |
| Message ID not found | "Unable to move message, no email found with messageID" | Verify message ID exists and is accessible |
| Folder Name is empty | "Folder Name cannot be empty"                           | Provide a valid folder name                |
| Folder not found     | "Destination folder does not exist"                     | Verify folder name exists in mailbox       |
| Same folder move     | "Cannot move message to the same folder"                | Specify a different destination folder     |

## Usage Examples

### Example 1: Move Email to Archive

**Scenario**: Move a processed email to the Archive folder

**Input**:

```
Message ID: "AQMkADY4ZTFiMGIxLWU1YjUtNDEwMS04Y2Q0..."
Folder Name: "Archive"
```

**Output**:

```
Response: "Success"
```

### Example 2: Move Email to Project Subfolder

**Scenario**: Organize project-related email into specific project folder

**Input**:

```
Message ID: "AQMkADY4ZTFiMGIxLWU1YjUtNDEwMS04Y2Q0..."
Folder Name: "Projects/2023/Alpha Project"
```

**Output**:

```
Response: "Success"
```

### Example 3: Move Email to Custom Folder

**Scenario**: Move customer inquiry to dedicated customer service folder

**Input**:

```
Message ID: "AQMkADY4ZTFiMGIxLWU1YjUtNDEwMS04Y2Q0..."
Folder Name: "Customer Service"
```

**Output**:

```
Response: "Success"
```

## Business Rules

1. **Folder Existence**: Destination folder must exist in the user's mailbox
2. **Message Accessibility**: User must have access to the source email
3. **Move Permissions**: User must have permission to move emails
4. **Folder Permissions**: User must have write access to destination folder
5. **Same Folder Prevention**: Cannot move email to its current folder
6. **Immediate Effect**: Email is moved immediately and removed from source folder

## Limitations

1. **Message ID Validity**: Message ID must exist and be accessible to the authenticated user
2. **Folder Existence**: Destination folder must already exist (operation doesn't create folders)
3. **Move Permissions**: User must have permission to move the specific email
4. **Folder Access**: User must have write access to destination folder
5. **Rate Limits**: Subject to Microsoft Graph API rate limiting
6. **Cross-Mailbox**: Cannot move emails between different user mailboxes

## Best Practices

### 1. Folder Management

- Verify folder names are spelled correctly and match exactly
- Use consistent folder naming conventions
- Consider folder hierarchy when organizing emails
- Create folders before moving emails to them

### 2. Message ID Handling

- Validate message ID exists before attempting move
- Store message IDs from previous catalog requests
- Handle cases where emails may be deleted during processing
- Use consistent ID format throughout application

### 3. Error Handling

- Implement retry logic for transient failures
- Validate folder existence before moving emails
- Handle cases where destination folder is deleted
- Provide meaningful error messages to users

### 4. Workflow Integration

- Move emails as part of larger processing workflows
- Consider marking emails before moving for audit trails
- Implement proper logging for move operations
- Coordinate with other email management operations

## Common Use Cases

### 1. Email Processing Workflow

```
Scenario: Move processed emails to completed folder
Action: After processing email content, move to "Processed" folder
Result: Inbox stays clean and processed emails are organized
```

### 2. Project Email Organization

```
Scenario: Organize project-related emails into project folders
Action: Move emails to appropriate project subfolders
Result: Project emails are organized for easy access and reference
```

### 3. Customer Service Routing

```
Scenario: Route customer emails to appropriate service folders
Action: Move customer inquiries to specialized service folders
Result: Customer emails are organized by service type or priority
```

### 4. Archive Management

```
Scenario: Archive old emails to maintain inbox organization
Action: Move old or completed emails to archive folders
Result: Active inbox is maintained while preserving email history
```

## Related Catalog Requests

- [Fetch All Labels](pages/FetchAllLabels.md) - Get list of available folders for moving
- [Fetch Mail By Message Id](pages/FetchMailByMessageId.md) - Get email details before moving
- [Mark Message](pages/MarkMessage.md) - Mark emails before or after moving
- [Fetch Mails By Label](pages/FetchMailsByLabel.md) - Verify emails in destination folder

## Technical Implementation

### Helper Class

- **Class**: MessagingAreaImpl
- **Package**: app.krista.extensions.essentials.collaboration.outlook3.impl
- **Method**: moveMessage(String messageId, String folderName)
- **Service**: Microsoft Graph Mail API message move functionality

### Telemetry Metrics

- **MOVE_MESSAGE_SUCCESS**: Successful message move operations
- **MOVE_MESSAGE_FAILURE**: Failed message move operations
- **INVALID_FOLDER**: Invalid destination folder attempts
- **FOLDER_NOT_FOUND**: Destination folder not found errors

## Troubleshooting

### Message Not Found

**Cause**: Invalid or non-existent message ID
**Solution**:

1. Verify message ID is complete and correct
2. Check if email has been deleted or moved already
3. Ensure user has access to the source email
4. Use Fetch Mail By Message Id to validate access

### Folder Not Found

**Cause**: Invalid or non-existent destination folder
**Solution**:

1. Verify folder name spelling and case sensitivity
2. Check folder exists in user's mailbox
3. Create folder if it doesn't exist (separate operation)
4. Test with known existing folders

### Permission Denied

**Cause**: User lacks move permissions
**Solution**:

1. Verify Mail.ReadWrite permission is granted
2. Check if user has write access to destination folder
3. Confirm user owns or has access to source email
4. Test with known moveable emails

### Move Operation Failed

**Cause**: System or network issues
**Solution**:

1. Check network connectivity to Microsoft Graph
2. Verify Microsoft Graph service status
3. Retry operation after brief delay
4. Check authentication token validity

## See Also

- [Extension Configuration](pages/ExtensionConfiguration.md) - Setup and configuration
- [Authentication](pages/Authentication.md) - Authentication requirements
- [Fetch All Labels](pages/FetchAllLabels.md) - Get available folders
- [Fetch Mail By Message Id](pages/FetchMailByMessageId.md) - Retrieve email details
