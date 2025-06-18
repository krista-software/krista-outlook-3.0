# Supported Requests

## Catalog Requests

The Outlook Extension supports the following catalog requests.

> **Note :**
> - Files with extensions such as "html", "php5", "pht", "phtml", "shtml", "asa", "cer", "asax", "swf", "xap", "jsp", "
    exe", and "js" are attached as a zip file in the email. Likewise, attachments are received as zip files in the
    email.

> **Error Handling Note :**
> - If a user makes a mistake when entering data, the system will give them a chance to fix it.
    An error message is displayed. The user can re-enter the information for that specific field. If they enter valid
    data the second time, everything will work as expected. However, if they enter incorrect data again, the system will
    encounter a problem and display a more specific error message.

### Fetch All Labels

- **Description**: Returns list of labels.
- **Input Parameters**: NA
- **Output Parameters**:

| **Parameter Name** | **Parameter Type** | **Example** |
|--------------------|--------------------|-------------|
| Labels             | List&lt;Label>     | Inbox, Sent |

### Mark Message

- **Description**: Accepts message ID,label & Category as input and mark mail as read/unread and returns response
  message.
- **Input Parameters**:

| **Parameter Name** | **Parameter Type** | **Mandatory** | **Example**      |
|--------------------|--------------------|---------------|------------------|
| Message ID         | Text               | Yes           | Message_ID_Value |
| Label              | Pick One           | Yes           | Read             |

- **Output Parameters**:

| **Parameter Name** | **Parameter Type** | **Example** |
|--------------------|--------------------|-------------|
| Response           | Text               | Success     |

- **Validation Fields**

| Input Parameter | Valid Data        | Invalid Data                                                              |
|-----------------|-------------------|---------------------------------------------------------------------------|
| Message ID      | Status As Success | The remediation action will be received, and the data will be re-entered. |

### Add Category To Message

- **Description**: Accepts message ID and Category as input and add given category to message and returns response
  message.

> **Note :**
> - In the event that **Create Category** is selected, the category will be accessible globally for all messages.
    Conversely, if this option is not selected, the category will only be available for the specific message provided.

- **Input Parameters**:

| **Parameter Name** | **Parameter Type** | **Mandatory** | **Example**      |
|--------------------|--------------------|---------------|------------------|
| Message ID         | Text               | Yes           | Message_ID_Value |
| Category           | Text               | Yes           | Krista           |
| Create Category    | Yes/No             | No            | Yes              |

- **Output Parameters**:

| **Parameter Name** | **Parameter Type** | **Example** |
|--------------------|--------------------|-------------|
| Category Added     | Yes/No             | Yes         |

- **Validation Fields**

| Input Parameter | Valid Data        | Invalid Data                                                              |
|-----------------|-------------------|---------------------------------------------------------------------------|
| Message ID      | Status As Success | The remediation action will be received, and the data will be re-entered. |

### Remove Category From Message

- **Description**: Accepts message ID and Category as input and remove given category from the message and returns
  response
  message.
- **Input Parameters**:

| **Parameter Name** | **Parameter Type** | **Mandatory** | **Example**      |
|--------------------|--------------------|---------------|------------------|
| Message ID         | Text               | Yes           | Message_ID_Value |
| Category           | Text               | Yes           | Krista           |

- **Output Parameters**:

| **Parameter Name** | **Parameter Type** | **Example** |
|--------------------|--------------------|-------------|
| Category Removed   | Yes/No             | Yes         |

- **Validation Fields**

| Input Parameter | Valid Data        | Invalid Data                                                              |
|-----------------|-------------------|---------------------------------------------------------------------------|
| Message ID      | Status As Success | The remediation action will be received, and the data will be re-entered. |
| Category        | Status As Success | The remediation action will be received, and the data will be re-entered. |

### Fetch Mail By Message Id

- **Description**: Accepts message Id as input and returns mail. In case of invalid input, this will return empty data.
- **Input Parameters**:

| **Parameter Name** | **Parameter Type** | **Mandatory** | **Example**      |
|--------------------|--------------------|---------------|------------------|
| Message ID         | Text               | Yes           | Message_Id_Value |

- **Output Parameters**:

| **Parameter Name** | **Parameter Type** |
|--------------------|--------------------|
| Mail               | Mail               |

- **Validation Fields**

| Input Parameter | Valid Data        | Invalid Data                                                              |
|-----------------|-------------------|---------------------------------------------------------------------------|
| Message ID      | Status As Success | The remediation action will be received, and the data will be re-entered. |

### Move Message

- **Description**: Accepts message ID, and folder name as input and move one message from source folder to another
  folder and returns response message.
- **Input Parameters**:

| **Parameter Name** | **Parameter Type** | **Mandatory** | **Example**      |
|--------------------|--------------------|---------------|------------------|
| Message ID         | Text               | Yes           | Message_ID_Value |
| Folder Name        | Text               | Yes           | Inbox            |

- **Output Parameters**:

| **Parameter Name** | **Parameter Type** | **Example** |
|--------------------|--------------------|-------------|
| Response           | Text               | Success     |

- **Validation Fields**

| Input Parameter | Valid Data        | Invalid Data                                                              |
|-----------------|-------------------|---------------------------------------------------------------------------|
| Message ID      | Status As Success | The remediation action will be received, and the data will be re-entered. |
| Folder Name     | Status As Success | The remediation action will be received, and the data will be re-entered. |

### Fetch Mails By Label

- **Description**: Accepts label, page number, and page size as input and returns list of mail. Page number, and page
  size are optional input.
- **Input Parameters**: Currently supports page size and page number between 0 and 15. Default value of page number is 1
  and of page size is 15. To fetch subfolder provide folder path such as parent folder followed by '/' followed by child
  folder in label.

| **Parameter Name** | **Parameter Type** | **Mandatory** | **Example**       |
|--------------------|--------------------|---------------|-------------------|
| Label              | Text               | Yes           | Inbox, Inbox/Demo |
| Page Number        | Number             | No            | 1                 |
| Page Size          | Number             | No            | 1                 |

- **Output Parameters**:

| **Parameter Name** | **Parameter Type** |
|--------------------|--------------------|
| Mails              | List&lt;Mails>     |

- **Validation Fields**

| Input Parameter | Valid Data        | Invalid Data                                                              |
|-----------------|-------------------|---------------------------------------------------------------------------|
| Label           | Status As Success | The remediation action will be received, and the data will be re-entered. |
| Page Number     | Status As Success | The remediation action will be received, and the data will be re-entered. |
| Page Size       | Status As Success | The remediation action will be received, and the data will be re-entered. |

### Mail Received Alert

- **Description**: This request returns an email when the user receives a new email.
- **Input Parameters**: NA
- **Output Parameters**:

| **Parameter Name** | **Parameter Type** |
|--------------------|--------------------|
| Mail Details       | Mail               |

> **Note :** If you are not receiving mail Alerts Please Upgrade Outlook Extension and Validate Attributes from Setup
> with "Allow Alert Mail" Checked.

### Reply To All

- **Description**: In this request, the user can respond to everyone on the thread. Other recipients will see a message
  user 'Reply All' to, whether they're in the 'To' or 'Cc' fields.
- **Input Parameters**:

| **Parameter Name** | **Parameter Type** | **Mandatory** | **Example**               |
|--------------------|--------------------|---------------|---------------------------|
| Message ID         | Text               | Yes           | Message_ID_Value          |
| Message            | RichText           | Yes           | Hi sir, This is a message |
| Attachments        | File               | No            | file.xlsx                 |
| BodyType           | PickOne            | No            | Text OR HTML              |

- **Output Parameters**:

| **Parameter Name** | **Parameter Type** | **Example** |
|--------------------|--------------------|-------------|
| Is Successful      | Boolean            | true        |

- **Validation Fields**

| Input Parameter | Valid Data        | Invalid Data                                                              |
|-----------------|-------------------|---------------------------------------------------------------------------|
| Message ID      | Status As Success | The remediation action will be received, and the data will be re-entered. |

### Reply To All With CC and BCC

- **Description**: In this request, you can respond to everyone on a thread. Other recipients would see a message. Use '
  Reply All' for all, whether they are in the 'To' or 'Cc' fields. Optional parameters 'To', 'Bcc' and 'Reply To' are
  provided that overwrites the old email addresses, if configured.
- **Input Parameters**:

| **Parameter Name** | **Parameter Type** | **Mandatory**                      | **Example**                            |
|--------------------|--------------------|------------------------------------|----------------------------------------|
| Message ID         | Text               | Yes                                | Message_ID_Value                       |
| Message            | RichText           | Yes                                | Hi sir, This is a message              |
| To                 | Text               | Yes, Overwrites the To list if set | [to@xyz.com, to1@xyz.com, to2@xyz.com] |
| Cc                 | Text               | No, Overwrites the To list if set  | [cc@xyz.com, cc1@xyz.com]              |
| Bcc                | Text               | No, Overwrites the To list if set  | [bcc@xyz.com, bcc1@xyz.com]            |
| Reply To           | Text               | No, Overwrites the To list if set  | replyTo@xyz.com                        |
| Attachments        | File               | No                                 | file.xlsx                              |
| BodyType           | PickOne            | No                                 | Text OR HTML                           |

- **Note**: The parameters To, Cc, Bcc and Reply To are comma separated emails. If any invalid email address is given
  then it will be skipped.

- **Output Parameters**:

| **Parameter Name** | **Parameter Type** | **Example** |
|--------------------|--------------------|-------------|
| Is Successful      | Boolean            | true        |

- **Validation Fields**

| Input Parameter | Valid Data        | Invalid Data                                                              |
|-----------------|-------------------|---------------------------------------------------------------------------|
| Message ID      | Status As Success | The remediation action will be received, and the data will be re-entered. |
| To              | Status As Success | The remediation action will be received, and the data will be re-entered. |
| Cc              | Status As Success | The remediation action will be received, and the data will be re-entered. |
| Bcc             | Status As Success | The remediation action will be received, and the data will be re-entered. |
| Reply To        | Status As Success | The remediation action will be received, and the data will be re-entered. |

### Fetch Sent

- **Description**: Accepts page number, and page size as input and returns list of mails from sent folder.
- **Input Parameters**: Currently supports page size and page number between 0 and 15. Default value of page number is 1
  and of page size is 15.

| **Parameter Name** | **Parameter Type** | **Mandatory** | **Example** |
|--------------------|--------------------|---------------|-------------|
| Page Number        | Number             | No            | 1           |
| Page Size          | Number             | No            | 1           |

- **Output Parameters**:

| **Parameter Name** | **Parameter Type** |
|--------------------|--------------------|
| Sent Mails         | List&lt;Mails>     |

- **Validation Fields**

| Input Parameter | Valid Data        | Invalid Data                                                              |
|-----------------|-------------------|---------------------------------------------------------------------------|
| Page Number     | Status As Success | The remediation action will be received, and the data will be re-entered. |
| Page Size       | Status As Success | The remediation action will be received, and the data will be re-entered. |

### Forward Mail

- **Description**: This request allows a sender to forward the received email to other recipients.
- **Input Parameters**:

| **Parameter Name** | **Parameter Type** | **Mandatory** | **Example**               |
|--------------------|--------------------|---------------|---------------------------|
| Message ID         | Text               | Yes           | Message_ID_Value          |
| To                 | Text               | Yes           | to@xyz.com. to1@xyz.com   |
| Message            | RichText           | Yes           | Hi sir, This is a message |
| BodyType           | PickOne            | No            | Text OR HTML              |

- **Note**: The parameter To is comma seperated emails and if any invalid email address given will be skipped.

- **Output Parameters**:

| **Parameter Name** | **Parameter Type** | **Example** |
|--------------------|--------------------|-------------|
| Is Forwarded       | Boolean            | true        |

- **Validation Fields**

| Input Parameter | Valid Data        | Invalid Data                                                              |
|-----------------|-------------------|---------------------------------------------------------------------------|
| Message ID      | Status As Success | The remediation action will be received, and the data will be re-entered. |
| To              | Status As Success | The remediation action will be received, and the data will be re-entered. |

### Send Mail

- **Description**: Accepts subject, message, attachments, to, bcc, cc, reply to as input and returns response message.
  Attachments, bcc, cc, and reply to are optional inputs.
- **Input Parameters**:

| **Parameter Name** | **Parameter Type** | **Mandatory** | **Example**               |
|--------------------|--------------------|---------------|---------------------------|
| Subject            | Text               | Yes           | This is subject           |
| Message            | Rich Text          | Yes           | Hi sir, This is a message |
| Attachments        | File               | No            | file.xlsx                 |
| To                 | Text               | Yes           | to@xyz.com, to1@xyz.com   |
| Bcc                | Text               | No            | bcc@xyz.com, bcc1@xyz.com |
| Cc                 | Text               | No            | cc@xyz.com, cc1@xyz.com   |
| ReplyTo            | Email              | No            | replyto@xyz.com           |
| BodyType           | PickOne            | No            | Text OR HTML              |

- **Note**: The parameters To, Cc, Bcc and Reply To are comma separated emails. If any invalid email address is given
  then it will be skipped.

- **Output Parameters**:

| **Parameter Name** | **Parameter Type** | **Example**                            |
|--------------------|--------------------|----------------------------------------|
| Message            | Text               | Mail Sent Successfully To: abc@xyz.com |

- **Validation Fields**

| Input Parameter | Valid Data        | Invalid Data                                                              |
|-----------------|-------------------|---------------------------------------------------------------------------|
| To              | Status As Success | The remediation action will be received, and the data will be re-entered. |
| Cc              | Status As Success | The remediation action will be received, and the data will be re-entered. |
| Bcc             | Status As Success | The remediation action will be received, and the data will be re-entered. |
| Reply To        | Status As Success | The remediation action will be received, and the data will be re-entered. |

### Send Mail With Table

- **Description**: Accepts subject, message, attachments, to, bcc, cc, List of Entities, reply to as input and returns
  response message.
  Attachments, bcc, cc, reply to and Remove Table Column are optional inputs.
- **Input Parameters**:

| **Parameter Name**             | **Parameter Type** | **Mandatory** | **Example**                                   |
|--------------------------------|--------------------|---------------|-----------------------------------------------|
| Subject                        | Text               | Yes           | This is subject                               |
| Message                        | Rich Text          | Yes           | Hi sir, This is a message                     |
| Attachments                    | File               | No            | file.xlsx                                     |
| To                             | Text               | Yes           | to@xyz.com, to1@xyz.com                       |
| Bcc                            | Text               | No            | bcc@xyz.com                                   |
| Cc                             | Text               | No            | cc@xyz.com                                    |
| ReplyTo                        | Email              | No            | replyto@xyz.com                               |
| Entity List                    | List&lt;Entity>    | Yes           | {Name: name1, Age: 12},{Name: name2, Age: 23} |
| Remove Entity Field From Table | List&lt;String>    | No            | ["primaryKey","Phone"]                        |

> **Note :** The parameters To, Cc, Bcc and Reply To are comma separated emails. If any invalid email address is given
> then it will be skipped.
>
>Input key for Date Field Should contain keyword like "date". For Example -> approvedOnDate or approved_on_date or
> $APPROVED_ON_DATE
> Similarly, Input key for Time Field Should contain keyword like "time". For Example -> startTime or start_time or
> $START_TIME

- **Output Parameters**:

| **Parameter Name** | **Parameter Type** | **Example**                            |
|--------------------|--------------------|----------------------------------------|
| Message            | Text               | Mail Sent Successfully To: abc@xyz.com |

- **Validation Fields**

| Input Parameter | Valid Data        | Invalid Data                                                              |
|-----------------|-------------------|---------------------------------------------------------------------------|
| To              | Status As Success | The remediation action will be received, and the data will be re-entered. |
| Cc              | Status As Success | The remediation action will be received, and the data will be re-entered. |
| Bcc             | Status As Success | The remediation action will be received, and the data will be re-entered. |
| Reply To        | Status As Success | The remediation action will be received, and the data will be re-entered. |

### Fetch Inbox Asynch

- **Description**: Fetches inbox mails asynchronously and returns task ID. The task ID will get used in getResult
  request to get mails. Maximum limit is 500 mails.
- **Input Parameters**: NA

- **Output Parameters**:

| **Parameter Name** | **Parameter Type** | **Example**                          |
|--------------------|--------------------|--------------------------------------|
| Task ID            | Text               | ffd01b50-cfd7-424b-91d5-e31afe121909 |

### Get Result

- **Description**: Accept task ID as input and return mails. Get this task ID from fetchInboxAsynch request.
- **Input Parameters**:

| **Parameter Name** | **Parameter Type** | **Mandatory** | **Example**                          |
|--------------------|--------------------|---------------|--------------------------------------|
| Task ID            | Text               | Yes           | ffd01b50-cfd7-424b-91d5-e31afe121909 |

- **Output Parameters**:

| **Parameter Name** | **Parameter Type** |
|--------------------|--------------------|
| Mails              | List&lt;Mails>     |

### Fetch Mail Details By Query

- **Description**: Accepts search query as input and returns list of mails. Returns at most 15 mails.
- **Input Parameters**:

| **Parameter Name** | **Parameter Type** | **Mandatory** | **Example**               |
|--------------------|--------------------|---------------|---------------------------|
| Query              | Text               | Yes           | Inbox:Your daily briefing |

- **Note**: You can enclose the search query within double quotes ("") for an exact match.

- **Output Parameters**:

| **Parameter Name** | **Parameter Type** |
|--------------------|--------------------|
| Mails              | List&lt;Mails>     |

### Fetch Inbox

- **Description**: Accepts page number, and page size as input and returns list of mail. Page number, and page size are
  optional parameters.
- **Input Parameters**: Currently supports page size and page number between 0 and 15. Default value of page number is 1
  and of page size is 15.

| **Parameter Name** | **Parameter Type** | **Mandatory** | **Example** |
|--------------------|--------------------|---------------|-------------|
| Page Number        | Number             | No            | 1           |
| Page Size          | Number             | No            | 1           |

- **Output Parameters**:

| **Parameter Name** | **Parameter Type** |
|--------------------|--------------------|
| Inbox Mails        | List&lt;Mails>     |

- **Validation Fields**

| Input Parameter | Valid Data        | Invalid Data                                                              |
|-----------------|-------------------|---------------------------------------------------------------------------|
| Page Number     | Status As Success | The remediation action will be received, and the data will be re-entered. |
| Page Size       | Status As Success | The remediation action will be received, and the data will be re-entered. |

### Fetch Inbox With Preferences

- **Description**: This request is used to fetch Inbox emails with the selected preferences.
- **Input Parameters**: Currently supports page size and page number between 0 and 15. Default value of page number is
  1, page size is 15 and that of Mail Body is Html.
- **Note**: "Mail Body" is a standard key and should not be altered.

| **Parameter Name** | **Parameter Type** | **Mandatory** | **Example**             |
|--------------------|--------------------|---------------|-------------------------|
| Page Number        | Number             | No            | 1                       |
| Page Size          | Number             | No            | 1                       |
| Preference         | Multi Field        | No            | Mail Body: Text or Html |

- **Output Parameters**:

| **Parameter Name** | **Parameter Type** |
|--------------------|--------------------|
| Mails              | List&lt;Mails>     |

- **Validation Fields**

| Input Parameter | Valid Data        | Invalid Data                                                              |
|-----------------|-------------------|---------------------------------------------------------------------------|
| Page Number     | Status As Success | The remediation action will be received, and the data will be re-entered. |
| Page Size       | Status As Success | The remediation action will be received, and the data will be re-entered. |

### Fetch Latest Mail

- **Description**: Returns the latest email received, in the last two minutes
- **Input Parameters**: NA

- **Output Parameters**:

| **Parameter Name** | **Parameter Type** |
|--------------------|--------------------|
| New Email          | Mails              |

### Reply To Mail

- **Description**: Accepts message ID, message, and attachments as input and returns response message. Attachment is
  optional input.
- **Input Parameters**:

| **Parameter Name** | **Parameter Type** | **Mandatory** | **Example**               |
|--------------------|--------------------|---------------|---------------------------|
| Message ID         | Text               | Yes           | Message_ID_Value          |
| Message            | Rich Text          | Yes           | Hi sir, This is a message |
| Attachments        | File               | No            | file.xlsx                 |
| BodyType           | PickOne            | No            | Text OR HTML              |

- **Output Parameters**:

| **Parameter Name** | **Parameter Type** | **Example**            |
|--------------------|--------------------|------------------------|
| Message            | Text               | Mail Sent Successfully |

- If given message does not contain replyTo mail then message will get sent to sender.

- **Validation Fields**

| Input Parameter | Valid Data        | Invalid Data                                                              |
|-----------------|-------------------|---------------------------------------------------------------------------|
| Message ID      | Status As Success | The remediation action will be received, and the data will be re-entered. |

### Reply To Mail With CC and BCC

- **Description**: In this request, you can respond to sender on a thread. Accepts Message ID, and Message as mandatory
  parameters and returns response message. Optional parameters 'To', 'Bcc', and 'Reply To' are provided that overwrites
  the old email addresses, if configured.
- **Input Parameters**:

| **Parameter Name** | **Parameter Type** | **Mandatory**                      | **Example**               |
|--------------------|--------------------|------------------------------------|---------------------------|
| Message ID         | Text               | Yes                                | Message_ID_Value          |
| Message            | Rich Text          | Yes                                | Hi sir, This is a message |
| To                 | Text               | Yes, Overwrites the To list if set | to@xyz.com, to1@xyz.com   |
| Cc                 | Text               | No, Overwrites the To list if set  | cc@xyz.com, cc1@xyz.com   |
| Bcc                | Text               | No, Overwrites the To list if set  | bcc@xyz.com, bcc1@xyz.com |
| Reply To           | Text               | No, Overwrites the To list if set  | replyTo@xyz.com           |
| Attachments        | File               | No                                 | file.xlsx                 |
| BodyType           | PickOne            | No                                 | Text OR HTML              |

- **Note**: The parameters To, Cc, Bcc and Reply To are comma separated emails. If any invalid email address is given
  then it will be skipped.

- **Output Parameters**:

| **Parameter Name** | **Parameter Type** | **Example**            |
|--------------------|--------------------|------------------------|
| Message            | Text               | Mail Sent Successfully |

- If given message does not contain replyTo mail then message will get sent to sender.

- **Validation Fields**

| Input Parameter | Valid Data        | Invalid Data                                                              |
|-----------------|-------------------|---------------------------------------------------------------------------|
| To              | Status As Success | The remediation action will be received, and the data will be re-entered. |
| Cc              | Status As Success | The remediation action will be received, and the data will be re-entered. |
| Bcc             | Status As Success | The remediation action will be received, and the data will be re-entered. |
| Reply To        | Status As Success | The remediation action will be received, and the data will be re-entered. |
| Message ID      | Status As Success | The remediation action will be received, and the data will be re-entered. |

### List Categories

- **Description**: In this request, you can Get a list of the supported Outlook categories.

- **Output Parameters**:

| **Parameter Name** | **Parameter Type** | **Example**                          |
|--------------------|--------------------|--------------------------------------|
| Category Names     | List&lt;Text>      | [Red category, Orange category, ...] |

### Get Notification Delta

- **Description**: This request is used to retrieve delta notifications that were missed by the alert event.

- **Output Parameters**:

| **Parameter Name** | **Parameter Type** | **Example**                 |
|--------------------|--------------------|-----------------------------|
| Messages Ids       | List&lt;Text>      | [Random id, Random id, ...] |

- **Note**: This request retrieves all notifications, and it is the user's responsibility to track the processed ones to identify any that were missed. 
- After each successful execution, Microsoft returns a checkpoint link that can be used to fetch only the new notifications from that point onward.
- When integrating both "Mail Received Alert" and this request in the same workflow, it is recommended to add a delay of at least 15 seconds after the request get called for optimal performance.

### Send Alert Using Notification Delta

- **Description**: This request is used to send an alert to the Mail Received Alert request and accepts the Message ID as input.

- **Input Parameters**:

| **Parameter Name** | **Parameter Type** | **Example** |
|--------------------|--------------------|-------------|
| Message Id         | Text               | Random id   |

- **Note**: This request takes input of message id to send the alert to "Mail Received Alert" request which will help to execute system trigger conversation using alert request.

### Update Message Category And Status

- **Description**: Accepts message ID, label, and category as input. Updates the read/unread status and adds/removes category for the specified message.
- **Input Parameters**:

| **Parameter Name** | **Parameter Type** | **Mandatory** | **Example**      |
|--------------------|--------------------|---------------|------------------|
| Message ID         | Text               | Yes           | Message_ID_Value |
| Label              | PickOne            | No            | Read/Unread      |
| Category           | Text               | No            | Krista           |

- **Output Parameters**:

| **Parameter Name** | **Parameter Type** | **Example** |
|--------------------|--------------------|-------------|
| Response           | Text               | Success     |

- **Validation Fields**

| Input Parameter | Valid Data        | Invalid Data                                                              |
|-----------------|-------------------|---------------------------------------------------------------------------|
| Message ID      | Status As Success | The remediation action will be received, and the data will be re-entered. |

### Check If Triggered Mail Ids Exist

- **Description**: Checks whether a specific message ID exists in the set of mail IDs that have already triggered alerts. This is useful for preventing duplicate processing of the same email in workflows.
- **Input Parameters**:

| **Parameter Name** | **Parameter Type** | **Mandatory** | **Example**      |
|--------------------|--------------------|---------------|------------------|
| MessageId          | Text               | Yes           | Message_ID_Value |

- **Output Parameters**:

| **Parameter Name** | **Parameter Type** | **Example** |
|--------------------|--------------------|-------------|
| IsExist            | Boolean            | true/false  |

- **Note**: Returns `true` if the message ID has already triggered an alert and exists in the triggered mail IDs set, 
otherwise returns `false`. This can be used to prevent duplicate processing of emails in workflows that use the "Mail Received Alert" request.

## Entity Requests

The Outlook Extension supports the following entity requests.

### Search Labels

- **Description**: Returns list of labels.
- **Input Parameters**: NA
- **Output Parameters**:

| **Parameter Name** | **Parameter Type** | **Example** |
|--------------------|--------------------|-------------|
| Labels             | List&lt;Label>     | Inbox, Sent |

### Get Label

- **Description**: Selects label from searched result.
- **Input Parameters**: NA
- **Output Parameters**: NA
