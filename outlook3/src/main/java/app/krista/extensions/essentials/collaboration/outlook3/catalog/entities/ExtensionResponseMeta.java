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

package app.krista.extensions.essentials.collaboration.outlook3.catalog.entities;


import app.krista.extension.impl.anno.*;

@Domain(id = "catEntryDomain_5fa2fc97-4b17-44cf-b98f-aa91a459a091",
        name = "Collaboration",
        ecosystemId = "catEntryEcosystem_84b53163-327b-4b1b-8c96-9334d292f9f5",
        ecosystemName = "Essentials",
        ecosystemVersion ="6072dcfd-83e6-436c-b1f7-0239a34bcf74")
@Entity(name = "Extension Response Meta", id = "localDomainEntity_415a46c3-047d-4caf-9169-5f2413759b34", primaryKey = "Message", supportStore = false)
public class ExtensionResponseMeta {

    @Searchable
    @ToString
    @Field(name = "Message", type = "RichText", attributes = {@Attribute(name = "visualWidth", value = "M"), @Attribute(name = "toolTip", value = "'Detailed message on extension response'")})
    public String message;

    @Field(name = "Technical Detailed Error Report", type = "RichText", required = false, attributes = {@Attribute(name = "visualWidth", value = "L"), @Attribute(name = "toolTip", value = "'The system showed some error details to help the technical team understand what went wrong.'")})
    public String technicalDetailedErrorReport;

    @Field.PickOne(name = "Response Type", values = {"SUCCESS","INPUT_ERROR","LOGIC_ERROR","AUTHENTICATION_ERROR","SYSTEM_ERROR","UNAVAILABILITY_ERROR"}, attributes = {@Attribute(name = "visualWidth", value = "S"), @Attribute(name = "toolTip", value = "'Represents exception types to help categorize and handle errors'")})
    public String responseType;

    @Field(name = "Time Taken In Seconds", type = "Number", attributes = {@Attribute(name = "visualWidth", value = "S")})
    public Double timeTakenInSeconds;

}
