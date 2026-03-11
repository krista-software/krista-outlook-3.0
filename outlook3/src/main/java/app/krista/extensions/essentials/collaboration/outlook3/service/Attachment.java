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

package app.krista.extensions.essentials.collaboration.outlook3.service;

import app.krista.model.base.File;

public interface Attachment {

    /**
     * Returns name of the given attachment
     *
     * @return name of the given attachment
     */
    String getName();

    /**
     * Returns size of the given attachment
     *
     * @return size of the given attachment
     */
    int getSize();

    /**
     * Returns mime type of the given attachment
     *
     * @return mime type of the given attachment
     */
    String getMimeType();

    /**
     * Returns {@link File} object of outlook.
     *
     * @return {@link File} object of outlook.
     */
    java.io.File download();

}
