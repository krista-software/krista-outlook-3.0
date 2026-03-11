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
        ecosystemVersion = "b28294f6-04ce-453c-84d9-8f56711d4c2f")
@Entity(name = "Label", id = "localDomainEntity_c4bf7faa-5e69-402e-96b8-ce75a7663020", primaryKey = "Name", supportStore = true)
@Searchable
public class Label {

    @Searchable
    @ToString
    @Field(name = "Name", type = "Text")
    public final String name;

    public Label(String name) {
        this.name = name;
    }

    public static Label fromField(String labelName) {
        return new Label(labelName);
    }

    @Override
    public String toString() {
        return "Label{" +
                "Name:'" + name + "'" +
                "}";
    }

}

