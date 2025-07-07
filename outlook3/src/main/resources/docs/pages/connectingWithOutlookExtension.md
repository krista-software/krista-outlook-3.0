# Connecting with Krista Outlook Extension

To establish a connection with the Outlook Extension, follow the steps below based on your authentication method.

> **Note**
> - If a custom Access Point is being used for the Outlook Extension, it is mandatory to set the Access Point **before** proceeding with authentication.
> - If the Access Point is changed, **re-authentication is required** for the respective Access Point.

---

## 🔓 Using Public Authentication

1. Navigate to the **Authentication** tab in the Outlook Extension and select **Public**.  
   ![Public Authentication](../_media/public_auth.png)

2. Enter your **school or work email ID**, then click on **Test Connection**.

   > **Note**:  
   > If a shared account ID was provided during setup, use the **service account credentials** in the Microsoft login window.

3. Once authentication is successful, click on **Save Changes**.

4. You are now ready to use the extension for catalog requests.

5. > **Note**:  
   > If you enabled **Mail Alerts** during authentication, a mail subscription is automatically created to notify Krista when new emails arrive. This subscription:
   > - Is valid for 3 days (72 hours) and automatically renews every 24 hours *(as long as at least one notification is received and the process remains active)*
   > - Uses Microsoft Graph API’s notification system to deliver real-time alerts
   > - Enables the **"Mail Received Alert"** catalog request to trigger workflows when new emails arrive
   > - May take up to **60 seconds** to initialize during the initial connection test


---

## 🔐 Using Private Authentication

1. To set up the connection using **Private Authentication**, gather the following details from your registered Azure application:
  - Client ID
  - Client Secret
  - Tenant ID

2. In the Outlook Extension, go to the **Details** tab and copy the **Extension Base URL**.

3. Append `/rest/outlook/callback` to the base URL. This full URL will be needed when registering your Azure app.  
   ![Routing ID Reference](../_media/routingId.png)

4. To get the required credentials, navigate to the **Obtaining Credentials For Private Authentication** page or [click here](pages/obtainingClientIDClientSecret.md).

5. Go to the **Authentication** tab and select **Private**.  
   ![Private Authentication](../_media/privateAuth.png)

6. Provide the following:
  - Email
  - Client ID
  - Client Secret
  - Tenant ID

   Then click **Test Connection**.

   > **Note**:
   > - If a shared account ID was provided during setup, use the **service account credentials** in the Microsoft login window.
   > - The test connection process includes setting up **mail subscriptions** if mail alerts are enabled. This may take up to **60 seconds**, as the system retries multiple times to ensure successful subscription creation.
   > - If the subscription creation fails after multiple attempts, you’ll see:  
       > `"Connection successful but failed to create mail subscription"`  
       > In that case, check your **network connectivity**, **Microsoft Graph API permissions**, or retry the test connection.

7. Once the authentication succeeds, click on **Save Changes**.

8. Return to the extension setup page and click **Validate**.

9. In the resulting Authentication tab, authorize using the **same account credentials** you used when registering your Azure application.

10. You are now all set to use the extension for requests.

---
