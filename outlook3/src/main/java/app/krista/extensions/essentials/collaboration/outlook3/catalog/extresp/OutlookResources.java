/*
 * Outlook 3.0 Extension for Krista
 * Copyright (C) 2025 Krista Software
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package app.krista.extensions.essentials.collaboration.outlook3.catalog.extresp;

import app.krista.model.field.util.GsonJsonMapper;

public class OutlookResources {
    private OutlookResources(){}

    public static final GsonJsonMapper JSON_MAPPER = GsonJsonMapper.getInstance();

    public static final String MESSAGE_ID = "Message ID";
    public static final String FOLDER_NAME = "Folder Name";
    public static final String CC = "Cc";
    public static final String TO = "To";
    public static final String BCC = "Bcc";
    public static final String REPLY_TO = "Reply To";
    public static final String QUERY = "Query";
    public static final String LABEL = "Label";
    public static final String CATEGORY = "Category";
    public static final String STATE_ID = "stateId";
    public static final String MESSAGE = "Message";
    public static final String ATTACHMENTS = "Attachments";
    public static final String BODY_TYPE = "BodyType";
    public static final String SUBJECT = "Subject";
    public static final String ENTITY_LIST = "Entity List";
    public static final String REMOVE_ENTITY_FIELD_FROM_TABLE = "Remove Entity Field From Table";
    public static final String PAGE_NUMBER = "Page Number";
    public static final String PAGE_SIZE = "Page Size";
    public static final String CREATE_CATEGORY = "Create Category";
    public static final String PREFERENCE = "Preference";
}
