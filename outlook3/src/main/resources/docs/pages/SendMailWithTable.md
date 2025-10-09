# Send Mail With Table

## Overview

Sends email messages with dynamically generated HTML tables from entity data. This catalog request automatically
converts entity lists into formatted HTML tables within email content, perfect for sending structured data reports,
summaries, and data-driven communications.

## Request Details

- **Area**: Messaging
- **Type**: CHANGE_SYSTEM
- **Retry Support**: Yes - Failed requests can be retried automatically

## Input Parameters

| Parameter Name                 | Type         | Required | Description                                    | Example                                                            |
|--------------------------------|--------------|----------|------------------------------------------------|--------------------------------------------------------------------|
| Subject                        | Text         | Yes      | Email subject line                             | "Weekly Sales Report"                                              |
| Message                        | Rich Text    | Yes      | Email body content (table will be appended)    | "Please review the weekly sales data below:"                       |
| To                             | Text         | Yes      | Primary recipients (comma-separated)           | "sales-team@company.com"                                           |
| Cc                             | Text         | No       | Carbon copy recipients (comma-separated)       | "manager@company.com"                                              |
| Bcc                            | Text         | No       | Blind carbon copy recipients (comma-separated) | "executives@company.com"                                           |
| Reply To                       | Text         | No       | Reply-to email address                         | "sales-reports@company.com"                                        |
| Attachments                    | File         | No       | Files to attach to the email                   | report.pdf                                                         |
| Entity List                    | List<Entity> | Yes      | Data entities to convert to HTML table         | [{"Name": "John", "Sales": 1000}, {"Name": "Jane", "Sales": 1500}] |
| Remove Entity Field From Table | List<String> | No       | Entity fields to exclude from table            | ["primaryKey", "internalId"]                                       |

### Parameter Details

#### Entity List

- **Format**: List of entity objects with consistent field structure
- **Table Generation**: Automatically converts entities to HTML table
- **Field Names**: Entity field names become table column headers
- **Data Types**: Supports text, numbers, dates, and boolean values
- **Formatting**: Automatic formatting based on data type

#### Remove Entity Field From Table

- **Purpose**: Exclude specific fields from table display
- **Format**: List of field names to exclude
- **Common Exclusions**: Primary keys, internal IDs, sensitive data
- **Case Sensitivity**: Field names must match exactly

#### Special Field Handling

- **Date Fields**: Field names containing "date" are formatted as dates
- **Time Fields**: Field names containing "time" are formatted as time
- **Examples**: "approvedOnDate", "approved_on_date", "startTime", "start_time"

## Output Parameters

| Parameter Name | Type | Description                                     |
|----------------|------|-------------------------------------------------|
| Message        | Text | Success confirmation with recipient information |

**Example Output**: "Mail Sent Successfully To: sales-team@company.com, manager@company.com"

## Validation Rules

| Validation           | Error Message                                                    | Resolution                      |
|----------------------|------------------------------------------------------------------|---------------------------------|
| Subject is empty     | "Subject cannot be empty"                                        | Provide email subject           |
| Message is empty     | "Message content cannot be empty"                                | Provide email body content      |
| To field is empty    | "To field cannot be empty"                                       | Provide at least one recipient  |
| Entity List is empty | "Entity List cannot be empty"                                    | Provide data entities for table |
| Invalid email format | "Mail address is not valid. Please provide correct mail address" | Correct email format            |

## Usage Examples

### Example 1: Sales Report Table

**Scenario**: Send weekly sales report with team performance data

**Input**:

```
Subject: "Weekly Sales Report - Team Performance"
Message: "Here's our team performance for this week:"
To: "sales-team@company.com"
Cc: "sales-manager@company.com"
Entity List: [
  {"Name": "John Smith", "Territory": "North", "Sales": 15000, "Target": 12000, "Achievement": "125%"},
  {"Name": "Jane Doe", "Territory": "South", "Sales": 18000, "Target": 15000, "Achievement": "120%"},
  {"Name": "Bob Johnson", "Territory": "East", "Sales": 22000, "Target": 20000, "Achievement": "110%"}
]
Remove Entity Field From Table: ["primaryKey"]
```

**Generated Table**:
| Name | Territory | Sales | Target | Achievement |
|------|-----------|-------|--------|-------------|
| John Smith | North | 15000 | 12000 | 125% |
| Jane Doe | South | 18000 | 15000 | 120% |
| Bob Johnson | East | 22000 | 20000 | 110% |

### Example 2: Project Status Report

**Scenario**: Send project status with milestone tracking

**Input**:

```
Subject: "Project Alpha - Milestone Status"
Message: "Current status of Project Alpha milestones:"
To: "project-team@company.com"
Entity List: [
  {"Milestone": "Requirements", "Status": "Complete", "DueDate": "2023-01-15", "CompletedDate": "2023-01-12"},
  {"Milestone": "Design", "Status": "In Progress", "DueDate": "2023-02-01", "CompletedDate": null},
  {"Milestone": "Development", "Status": "Not Started", "DueDate": "2023-03-15", "CompletedDate": null}
]
```

### Example 3: Financial Summary

**Scenario**: Send monthly financial summary with department breakdown

**Input**:

```
Subject: "Monthly Financial Summary - Department Breakdown"
Message: "Monthly financial performance by department:"
To: "finance-team@company.com"
Bcc: "cfo@company.com"
Entity List: [
  {"Department": "Sales", "Budget": 100000, "Actual": 95000, "Variance": -5000, "VariancePercent": "-5%"},
  {"Department": "Marketing", "Budget": 50000, "Actual": 52000, "Variance": 2000, "VariancePercent": "+4%"},
  {"Department": "Operations", "Budget": 75000, "Actual": 73000, "Variance": -2000, "VariancePercent": "-3%"}
]
Remove Entity Field From Table: ["internalId", "createdBy"]
```

## Business Rules

1. **Table Generation**: Entity list is automatically converted to HTML table
2. **Field Exclusion**: Specified fields are excluded from table display
3. **Data Formatting**: Automatic formatting based on field names and data types
4. **Email Integration**: Table is appended to the message content
5. **Recipient Validation**: All email addresses are validated before sending
6. **Content Encoding**: All content including table data is properly encoded

## Limitations

1. **Entity Consistency**: All entities in the list should have consistent field structure
2. **Table Size**: Large tables may impact email readability and size limits
3. **Field Names**: Field names become column headers - use descriptive names
4. **Data Types**: Complex nested objects are not supported in table cells
5. **Formatting**: Limited to basic HTML table formatting
6. **Email Size**: Total email including table content limited to 25MB

## Best Practices

### 1. Entity Data Preparation

- Ensure consistent field structure across all entities
- Use descriptive field names that work well as column headers
- Format data appropriately before including in entity list
- Remove unnecessary fields using the exclusion parameter

### 2. Table Design

- Keep tables reasonably sized for email readability
- Use meaningful column headers
- Consider data formatting for better presentation
- Test table appearance in different email clients

### 3. Content Organization

- Place explanatory text before the table
- Use clear subject lines that indicate table content
- Consider breaking large datasets into multiple emails
- Provide context for the data being presented

### 4. Performance Optimization

- Limit entity list size for better performance
- Remove unnecessary fields to reduce email size
- Use appropriate data types for better formatting
- Test with representative data volumes

## Common Use Cases

### 1. Regular Reporting

```
Scenario: Send weekly/monthly reports with structured data
Action: Generate entity list from database and send as table
Result: Recipients receive formatted reports automatically
```

### 2. Data Summaries

```
Scenario: Provide data summaries for decision making
Action: Aggregate data into entities and send formatted table
Result: Stakeholders receive clear, structured information
```

### 3. Status Updates

```
Scenario: Share project or operational status updates
Action: Convert status data to entities and send as table
Result: Team receives organized status information
```

## Related Catalog Requests

- [Send Mail](pages/SendMail.md) - Basic email sending without tables
- [Reply To Mail](pages/ReplyToMail.md) - Reply to existing emails
- [Fetch Inbox](pages/FetchInbox.md) - Retrieve emails for processing
- [Test Connection](pages/TestConnection.md) - Verify email sending capabilities

## Technical Implementation

### Helper Class

- **Class**: MessagingAreaImpl
- **Package**: app.krista.extensions.essentials.collaboration.outlook3.impl
- **Method**: sendMailWithTable(String subject, String message, String to, String cc, String bcc, String replyTo,
  List<File> attachments, List<Entity> entityList, List<String> removeEntityFieldFromTable)
- **Table Generation**: Automatic HTML table creation from entity data
- **Service**: Microsoft Graph Mail API with HTML content support

### Telemetry Metrics

- **SEND_MAIL_WITH_TABLE_SUCCESS**: Successful table email operations
- **SEND_MAIL_WITH_TABLE_FAILURE**: Failed table email operations
- **ENTITY_COUNT**: Number of entities in table
- **TABLE_FIELD_COUNT**: Number of fields/columns in table

## Troubleshooting

### Table Not Displaying Correctly

**Cause**: Entity structure or HTML formatting issues
**Solution**:

1. Verify all entities have consistent field structure
2. Check field names are valid and descriptive
3. Test with simple entity structure first
4. Verify email client supports HTML tables

### Missing Table Data

**Cause**: Field exclusion or entity processing issues
**Solution**:

1. Check Remove Entity Field From Table parameter
2. Verify entity list is not empty
3. Confirm field names match exactly
4. Test without field exclusions first

### Email Size Issues

**Cause**: Large table data exceeding size limits
**Solution**:

1. Reduce number of entities in table
2. Remove unnecessary fields from entities
3. Consider pagination for large datasets
4. Use attachments for very large data sets

### Formatting Problems

**Cause**: Data type or encoding issues
**Solution**:

1. Ensure proper data types in entities
2. Check for special characters in data
3. Test with simple text data first
4. Verify date/time field naming conventions

## See Also

- [Extension Configuration](pages/ExtensionConfiguration.md) - Setup and configuration
- [Authentication](pages/Authentication.md) - Authentication requirements
- [Send Mail](pages/SendMail.md) - Basic email sending functionality
- [Test Connection](pages/TestConnection.md) - Connection testing and validation
