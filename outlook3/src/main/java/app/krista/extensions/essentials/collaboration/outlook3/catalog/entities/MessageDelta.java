package app.krista.extensions.essentials.collaboration.outlook3.catalog.entities;

import app.krista.extension.impl.anno.*;

@Domain(id = "catEntryDomain_5fa2fc97-4b17-44cf-b98f-aa91a459a091",
        name = "Collaboration",
        ecosystemId = "catEntryEcosystem_84b53163-327b-4b1b-8c96-9334d292f9f5",
        ecosystemName = "Essentials",
        ecosystemVersion = "f44f7a84-d1b0-46d7-85da-179f249b5217")
@Entity(name = "Message Delta", id = "localDomainEntity_2a5449f5-e303-411f-b2e9-9a96c81ae065", primaryKey = "Message ID", supportStore = false)
public class MessageDelta {

    @Searchable
    @Field.Text(name = "Message ID", attributes = {@Attribute(name = "visualWidth", value = "S")})
    public String messageID;

}