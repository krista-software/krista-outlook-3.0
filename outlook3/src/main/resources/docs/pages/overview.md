# Overview

- The **Krista Outlook Extension** connects to the Microsoft Outlook application and performs various operations.
- The Outlook Krista Extension is a collaboration offering.

---

## Prerequisites

Before proceeding with the integration or setup process, ensure you have the necessary access and privileges.  
Depending on your authentication scenario, follow one of the two options below:

1. **Public Authentication**
    - Ensure you have access to your work or school email account provided by your organization.

2. **Private Authentication**
    - Obtain the **Client ID** and **Client Secret**.
    - Grant **Admin-level consent**.

To use **shared mail** in the Krista Outlook Extension, ensure that your service account has the following permissions:

- **Delegate access** to the shared mail account
- **Send As** permissions
- **Full Read and Manage** permissions

---

## Limitations

- As of now, the **Microsoft Graph API (v1.0)** for Outlook does **not support reference attachments**.
- If the invoker remains unused for more than **90 days**, **re-authentication will be required**.
- You will need to **set up the mail alert again** if no new emails are received for more than **3 days**.

