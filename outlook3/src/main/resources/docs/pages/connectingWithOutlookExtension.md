### Connecting with Outlook Extension

To establish a connection with the Outlook Extension, follow the steps below based on your authentication method:

## Using Work or School Account

* Check the "Login with Microsoft" option and provide your work or school account email ID, then click on "Validate Attributes."
  ![Using Emial](../_media/usingEmail.png)

## Using Client Id and Client Secret

* To set up the connection you must provide the Client ID, Client Secret, and Tenant ID from your registered web application on the Azure portal.
* Refer to the **Credentials** tab of your selected project on the Azure portal.
  * Client ID and Tenant ID
    ![Client ID And Tenant ID](../_media/clientIDtenantID.png)
  * Client Secret
    ![Client Secret](../_media/clientSecret.png)


* Authorize with the same administrator account details that you registered application with, on the Azure portal.


* Navigate to the Details tab in the extension, copy the **Extension Base URL**. Append **/rest/outlook/callback** at the end of URL ID.
  ![Routing Id Reference](../_media/routingId.png)


* Enter this entire string in the **Redirect URI** field on **Register an application** page on the Azure portal.
  ![Authorized redirect uri Reference](../_media/authorizedRedirectURIReference.png)

  
* In the **Email** field, enter the email ID of the administrator. After user authentication a Refresh Token is provided along with this email ID.


* On the extension set up page, click **Validate**. On the resulting Authentication tab, authorize with the same account details that you registered application with, on the Azure portal.


* On the **Setup** page, click **Test Connection** to confirm that the connection is established. After that you are good to go for the extension requests.
