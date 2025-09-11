# Release Notes – Krista Outlook Extension

## Version 3.0.11

- **Developers**: VaraPrasad Kolli, Vaibhav Choudhary
- **Krista Service APIs (Java)**: 1.0.115
- **Global Catalog Version**: GC-2025.09.2
- **Appliance Release Version**: 3.5.1

---

## Resolved Bugs

- [**KE-2465**](https://antbrains.atlassian.net/browse/KE-2465):  
  Real Auction - Rich Text auto-response emails sent via Outlook extension improperly formatted

- [**KE-2450**](https://antbrains.atlassian.net/browse/KE-2450):  
  NullPointerException in Outlook email reply operations when timezone is unavailable

- [**KE-2274**](https://antbrains.atlassian.net/browse/KE-2274):  
  Outlook3: Credentials are returned in plain text instead of encrypted in extension test connection response

- [**KE-2358**](https://antbrains.atlassian.net/browse/KE-2358):  
  Outlook extension Paging occasionally fails on when iterating over pages - Fetch Mails By Label request

- [**KE-2349**](https://antbrains.atlassian.net/browse/KE-2349):  
  Outlook Extension: Fetch Latest Mail request should fetch the latest mail object

- [**KE-2273**](https://antbrains.atlassian.net/browse/KE-2273):  
  Outlook Extension, Add "include email thread" option in the Reply to Mail and Forward Mail requests

- [**KE-2293**](https://antbrains.atlassian.net/browse/KE-2293):  
  Our outlook extension has limits on the max Page Number and max Page Size parameters for the "Fetch Mails By Label" 
  request that limit the max # of emails I can handle.

- [**KE-2156**](https://antbrains.atlassian.net/browse/KE-2156):  
  Add authentication error handling for existing catalog requests, and create health check + test connection for Cujo.

- [**KR-18796**](https://antbrains.atlassian.net/browse/KR-18796):  
  Outlook Extension: Unexpected authentication prompt triggered during request execution.

- [**KE-2232**](https://antbrains.atlassian.net/browse/KE-2232):  
  Add two additional fields in Message entity for Outlook.

- [**KE-2129**](https://antbrains.atlassian.net/browse/KE-2129):  
  Outlook extension: Automatic token refresh regardless of private or public authentication credentials.

- [**KE-2019**](https://antbrains.atlassian.net/browse/KE-2019):  
  Automatic renewal of Subscription for alert emails in Outlook Extension.

---

## Known Issues

- None reported.
