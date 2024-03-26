# Overview #

* The Krista extension connects to the Microsoft Outlook application and performs various operations.
* The Outlook Krista extension is a collaboration offering.

## Prerequisite

* Before proceeding with the integration or setup process, ensure you have the necessary access and privileges.
  Depending on your scenario, follow one of the two options below:
    1. Public authentication, ensure you have access to your work or school email account provided by your organization.
    2. Private Authentication
        - Obtain the Client ID and Client Secret.
        - Grant the Admin level consent.

* To use shared mail in the Krista Outlook Extension, ensure you have a service account with the following privileges.
  * Delegate access to the shared mail account.
  * Send As Permissions.
  * Full Read and Manage Permissions.

## Limitations

* As of now, the Microsoft Graph API (v1.0) for Outlook does not support reference attachments.
* If the invoker remains unused for a period exceeding **90 days**, re-authentication will be necessary.
* You'll need to set up the mail alert again if you don't get any new emails for **3 days**.
