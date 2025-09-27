# Supported Email Operations - Krista Outlook Extension

This guide provides a comprehensive overview of all email operations you can automate with the Krista Outlook Extension. Each operation is explained in simple terms with practical examples.

## Overview of Available Operations

The Outlook Extension provides 12 powerful operations to automate your email workflows:

| Operation | Purpose | Complexity | Common Use Cases |
|-----------|---------|------------|------------------|
| [Test Connection](#test-connection) | Verify Outlook connectivity | Beginner | Health checks, troubleshooting |
| [Fetch Mails by Label](#fetch-mails-by-label) | Get emails from specific folders | Beginner | Processing inbox, organizing emails |
| [Fetch Latest Mail](#fetch-latest-mail) | Get the most recent email | Beginner | Monitoring new messages |
| [Send Mail](#send-mail) | Send new emails | Intermediate | Notifications, alerts, reports |
| [Reply to Mail](#reply-to-mail) | Respond to existing emails | Intermediate | Customer service, auto-responses |
| [Forward Mail](#forward-mail) | Forward emails to others | Intermediate | Routing, escalation workflows |
| [Get Mail by ID](#get-mail-by-id) | Retrieve specific email | Intermediate | Detailed processing, follow-ups |
| [Get User Profile](#get-user-profile) | Get account information | Beginner | User verification, personalization |
| [Create Subscription](#create-subscription) | Set up email alerts | Advanced | Real-time monitoring |
| [Delete Subscription](#delete-subscription) | Remove email alerts | Advanced | Cleanup, maintenance |
| [Renew Subscription](#renew-subscription) | Extend alert duration | Advanced | Ongoing monitoring |
| [Get Subscription](#get-subscription) | Check alert status | Advanced | Monitoring management |

---

## Test Connection

**Purpose**: Verify that your Outlook account is properly connected and accessible.

**When to use**: 
- After initial setup
- Troubleshooting connection issues
- Regular health checks
- Before important email campaigns

**What it does**:
- Checks authentication status
- Verifies email access permissions
- Tests API connectivity
- Confirms account details

**Example Response**:
```
✅ Connection Status: Active
✅ Authentication: Valid
✅ Email Access: Granted
✅ Send Permissions: Enabled
📧 Connected Account: john.doe@company.com
🕒 Last Tested: 2024-01-15 10:30 AM
```

**Business Value**: Ensures reliable email automation and prevents workflow failures.

---

## Fetch Mails by Label

**Purpose**: Retrieve emails from specific Outlook folders or with certain labels.

**When to use**:
- Processing emails in your Inbox
- Handling emails in custom folders
- Organizing email workflows by category
- Bulk processing of similar emails

**Key Features**:
- **Folder Selection**: Choose any Outlook folder (Inbox, Sent, Custom folders)
- **Pagination**: Handle large volumes efficiently (up to 1000 emails per request)
- **Filtering**: Get emails from specific time periods
- **Sorting**: Order by date, sender, subject, or importance

**Configuration Options**:
- **Folder Name**: Which folder to search (e.g., "Inbox", "Important", "Customer Inquiries")
- **Page Size**: How many emails to fetch at once (1-1000)
- **Page Number**: Which batch of emails to retrieve
- **Date Range**: Limit to specific time periods

**Example Use Cases**:

*Customer Service Automation*:
- Fetch emails from "Customer Support" folder
- Process inquiries automatically
- Route to appropriate team members

*Invoice Processing*:
- Fetch emails from "Invoices" folder
- Extract invoice data automatically
- Update accounting systems

*Marketing Campaign Monitoring*:
- Fetch emails from "Campaign Responses" folder
- Track engagement and replies
- Update customer databases

**Sample Email Data Returned**:
```
📧 Email 1:
   From: customer@example.com
   Subject: "Question about my order"
   Date: 2024-01-15 09:15 AM
   Folder: Customer Support
   Has Attachments: No
   
📧 Email 2:
   From: vendor@supplier.com
   Subject: "Invoice #12345"
   Date: 2024-01-15 08:30 AM
   Folder: Invoices
   Has Attachments: Yes (PDF)
```

---

## Fetch Latest Mail

**Purpose**: Retrieve the most recent email from your mailbox.

**When to use**:
- Monitoring for urgent messages
- Real-time email processing
- Checking for immediate responses
- Triggering workflows based on new emails

**What it provides**:
- Complete email content
- Sender information
- Timestamp details
- Attachment information
- Email metadata

**Example Scenarios**:

*Executive Assistant Automation*:
- Check for urgent emails every 5 minutes
- Alert executive of high-priority messages
- Auto-schedule meetings from email requests

*Order Processing*:
- Monitor for new order confirmations
- Immediately process payment notifications
- Update inventory systems in real-time

*Customer Emergency Response*:
- Watch for emails with "URGENT" in subject
- Automatically escalate to on-call team
- Send immediate acknowledgment to customer

---

## Send Mail

**Purpose**: Send new emails automatically as part of your workflows.

**When to use**:
- Sending notifications and alerts
- Automated customer communications
- Report distribution
- Follow-up messages

**Email Composition Features**:
- **Rich Text Formatting**: Bold, italic, colors, fonts
- **HTML Content**: Full HTML email support
- **Attachments**: Include files, documents, images
- **Multiple Recipients**: Send to multiple people at once
- **CC and BCC**: Include additional recipients
- **Custom Headers**: Add tracking or routing information

**Configuration Options**:
- **To**: Primary recipients (required)
- **CC**: Carbon copy recipients (optional)
- **BCC**: Blind carbon copy recipients (optional)
- **Subject**: Email subject line
- **Body**: Email content (text or HTML)
- **Attachments**: Files to include
- **Priority**: High, normal, or low importance
- **Delivery Receipt**: Request read confirmations

**Example Use Cases**:

*Automated Reporting*:
```
To: management@company.com
Subject: Daily Sales Report - January 15, 2024
Body: Please find attached today's sales summary...
Attachments: sales_report_20240115.pdf
Priority: Normal
```

*Customer Notifications*:
```
To: customer@example.com
Subject: Your Order #12345 Has Shipped
Body: Great news! Your order is on its way...
Priority: High
Delivery Receipt: Requested
```

*Team Alerts*:
```
To: support-team@company.com
CC: manager@company.com
Subject: URGENT: System Alert Detected
Body: Immediate attention required for...
Priority: High
```

---

## Reply to Mail

**Purpose**: Automatically respond to existing emails while maintaining conversation context.

**When to use**:
- Customer service auto-responses
- Acknowledgment messages
- Information requests
- Escalation notifications

**Key Features**:
- **Thread Preservation**: Maintains email conversation history
- **Original Content**: Option to include original message
- **Smart Formatting**: Proper reply formatting with ">" quotes
- **Recipient Handling**: Automatically includes original sender
- **Attachment Support**: Add files to your reply

**Configuration Options**:
- **Include Original Message**: Yes/No
- **Reply Type**: Reply to sender only or Reply All
- **Message Body**: Your response content
- **Additional Recipients**: Add CC/BCC if needed
- **Attachments**: Include supporting documents

**Example Scenarios**:

*Customer Service Automation*:
```
Original Email: "When will my order arrive?"
Auto-Reply: "Thank you for your inquiry. Your order #12345 
is scheduled to arrive on January 18th. You'll receive 
tracking information shortly."
```

*Meeting Requests*:
```
Original Email: "Can we schedule a meeting next week?"
Auto-Reply: "I'd be happy to meet. I have availability 
Tuesday at 2 PM or Thursday at 10 AM. Please let me know 
which works better for you."
```

*Information Requests*:
```
Original Email: "Please send me the latest product catalog"
Auto-Reply: "Thank you for your interest. Please find our 
latest product catalog attached. If you have any questions, 
feel free to reach out."
Attachment: product_catalog_2024.pdf
```

---

## Forward Mail

**Purpose**: Automatically forward emails to other recipients while preserving the original message.

**When to use**:
- Routing emails to appropriate team members
- Escalating issues to management
- Sharing information across departments
- Creating email distribution workflows

**Key Features**:
- **Original Message Preservation**: Complete original email included
- **Custom Introduction**: Add your own message before the forwarded content
- **Multiple Recipients**: Forward to several people at once
- **Attachment Handling**: All original attachments are included
- **Thread Maintenance**: Preserves conversation history

**Configuration Options**:
- **Forward To**: Recipient email addresses (required)
- **CC Recipients**: Additional people to include
- **Introduction Message**: Your message before the forwarded email
- **Include Attachments**: Yes/No option
- **Priority Level**: Set importance of forwarded message

**Example Use Cases**:

*Customer Escalation*:
```
Forward To: manager@company.com
Introduction: "This customer complaint requires immediate 
attention. Please review and respond within 2 hours."
Original Email: [Customer complaint about defective product]
```

*Department Routing*:
```
Forward To: technical-support@company.com
CC: customer-service@company.com
Introduction: "Technical question from customer - please 
provide detailed response."
Original Email: [Complex technical inquiry]
```

*Information Sharing*:
```
Forward To: sales-team@company.com
Introduction: "FYI - New competitor pricing information 
from industry contact."
Original Email: [Market intelligence from partner]
```

---

## Get Mail by ID

**Purpose**: Retrieve a specific email using its unique identifier.

**When to use**:
- Following up on specific emails
- Detailed processing of individual messages
- Retrieving emails referenced in other systems
- Audit trails and compliance

**What you get**:
- Complete email content and metadata
- Full recipient and sender information
- All attachments and their details
- Email properties (read status, importance, etc.)
- Conversation thread information

**Example Scenarios**:

*Compliance Auditing*:
- Retrieve specific emails for legal review
- Generate detailed reports on email content
- Verify email delivery and receipt

*Customer Service Follow-up*:
- Access previous customer communications
- Review conversation history before responding
- Ensure consistent service quality

*Project Management*:
- Track email-based project communications
- Retrieve specific approvals or decisions
- Maintain project documentation

---

## Get User Profile

**Purpose**: Retrieve information about the connected Outlook account.

**When to use**:
- Verifying account details
- Personalizing automated messages
- User identification in workflows
- Account validation

**Information Retrieved**:
- Display name and email address
- Job title and department
- Office location and phone numbers
- Manager and direct reports
- Account status and permissions

**Example Use Cases**:

*Personalized Communications*:
```
"Hello [User's First Name], this is an automated message 
from the [Department] team..."
```

*Signature Generation*:
```
Best regards,
[Full Name]
[Job Title]
[Department]
[Phone Number]
```

*Access Control*:
- Verify user permissions before processing requests
- Route emails based on user's department
- Apply different rules for different user types

---

## Subscription Management (Advanced)

The Outlook Extension provides sophisticated real-time email monitoring through subscriptions. These operations are typically used by advanced users or system administrators.

### Create Subscription

**Purpose**: Set up real-time notifications when new emails arrive.

**How it works**:
1. You specify what emails to monitor (folder, sender, subject keywords)
2. Microsoft sends instant notifications to Krista when matching emails arrive
3. Krista can immediately trigger workflows without polling for new emails

**Benefits**:
- **Instant Response**: Process emails within seconds of arrival
- **Efficient**: No need to constantly check for new emails
- **Scalable**: Handle high volumes without performance impact
- **Reliable**: Microsoft guarantees delivery of notifications

**Configuration**:
- **Folder to Monitor**: Which folder to watch (Inbox, specific folders)
- **Notification URL**: Where Microsoft sends alerts (automatically configured)
- **Expiration**: How long the subscription lasts (maximum 3 days)
- **Filter Criteria**: Optional filters for specific types of emails

### Delete Subscription

**Purpose**: Stop real-time email monitoring.

**When to use**:
- Ending a monitoring campaign
- Changing monitoring criteria
- Troubleshooting subscription issues
- System maintenance

### Renew Subscription

**Purpose**: Extend the duration of existing email monitoring.

**Why needed**: Microsoft subscriptions expire after maximum 3 days for security
**Best practice**: Automatically renew before expiration to maintain continuous monitoring

### Get Subscription

**Purpose**: Check the status and details of existing email monitoring.

**Information provided**:
- Subscription status (active, expired, error)
- Remaining time before expiration
- Monitoring criteria and filters
- Notification statistics

---

## Practical Workflow Examples

### Customer Service Automation
```
1. Create Subscription → Monitor "Customer Support" folder
2. When new email arrives → Fetch Latest Mail
3. Analyze email content → Determine urgency and category
4. If urgent → Forward Mail to on-call manager
5. Send Mail → Acknowledgment to customer
6. Reply to Mail → Provide initial response or next steps
```

### Invoice Processing Workflow
```
1. Fetch Mails by Label → Get emails from "Invoices" folder
2. For each email → Get Mail by ID for detailed processing
3. Extract invoice data from attachments
4. Update accounting system
5. Send Mail → Confirmation to vendor
6. Forward Mail → Copy to accounting team
```

### Executive Assistant Workflow
```
1. Create Subscription → Monitor executive's inbox
2. When VIP email arrives → Fetch Latest Mail
3. Check sender against VIP list
4. If VIP → Send Mail immediate alert to executive
5. Reply to Mail → Professional acknowledgment
6. Forward Mail → Copy to assistant for follow-up
```

## Error Handling and Troubleshooting

### Common Issues and Solutions

**"Email Not Found" Error**
- Check if email still exists in the specified folder
- Verify folder name spelling and case sensitivity
- Ensure you have permission to access the folder

**"Rate Limit Exceeded" Error**
- Reduce frequency of email operations
- Implement delays between bulk operations
- Consider upgrading to Private Authentication for higher limits

**"Subscription Expired" Error**
- Subscriptions automatically expire after 3 days
- Implement automatic renewal in your workflows
- Monitor subscription status regularly

**"Permission Denied" Error**
- Verify authentication is still valid
- Check if account permissions have changed
- Re-authenticate if necessary

### Best Practices

**Performance Optimization**:
- Use subscriptions instead of frequent polling
- Implement pagination for large email volumes
- Cache frequently accessed data

**Security Considerations**:
- Regularly review and rotate authentication credentials
- Monitor for unusual email access patterns
- Implement proper error logging and alerting

**Reliability Measures**:
- Build retry logic for temporary failures
- Implement backup notification methods
- Monitor subscription health and renewal

## Getting Started with Your First Workflow

1. **Start Simple**: Begin with Test Connection and Fetch Latest Mail
2. **Test Thoroughly**: Use small email volumes during development
3. **Monitor Performance**: Watch for rate limits and errors
4. **Scale Gradually**: Increase complexity and volume over time
5. **Document Everything**: Keep records of your workflow configurations

Your Outlook Extension is now ready to power sophisticated email automation workflows that save time, improve accuracy, and enhance customer service!