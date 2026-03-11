# Outlook 3.0 Extension for Krista

## Overview

The **Outlook 3.0** extension for the Krista platform provides comprehensive integration with Microsoft Outlook through both simplified public authentication and full-featured private authentication (OAuth 2.0 / Azure AD). It enables sophisticated email management, automation, and real-time processing capabilities for enterprise-grade email workflows.

### Key Features

- **Dual Authentication Modes** — Public (simplified) and Private (full OAuth 2.0 with Azure AD)
- **Advanced Email Management** — Send, reply, reply-all, forward with rich HTML, dynamic tables, and attachments
- **Sophisticated Query Operations** — Inbox retrieval, sent items, advanced search, real-time monitoring
- **Message Organization** — Categories, labels, read/unread status, folder management
- **Async & Event Processing** — Background processing, real-time alerts, event-driven workflows
- **Enterprise Configuration** — Health checks, diagnostics, test connection validation, telemetry

## Installation

This extension can be installed through the Krista platform.

## Usage

Configure the extension through the Krista administration interface. Choose your authentication mode based on your environment:

- **Public Authentication** — Recommended for testing and development. No Azure AD application required.
- **Private Authentication** — Recommended for production. Full OAuth 2.0 security with Azure AD integration.

For detailed setup instructions, catalog requests, and API documentation, see the [extension documentation](outlook3/src/main/resources/docs/README.md).

## License

This project is licensed under the **GNU General Public License v3.0**.

```
Outlook 3.0 Extension for Krista
Copyright (C) 2025 Krista Software

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.
```

For the full license text, see the [LICENSE](LICENSE) file or visit https://www.gnu.org/licenses/gpl-3.0.html.
