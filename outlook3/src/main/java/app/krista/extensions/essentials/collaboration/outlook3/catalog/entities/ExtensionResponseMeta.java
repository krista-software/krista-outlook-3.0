package app.krista.extensions.essentials.collaboration.outlook3.catalog.entities;


import app.krista.extension.impl.anno.*;

@Domain(id = "catEntryDomain_5fa2fc97-4b17-44cf-b98f-aa91a459a091",
        name = "Collaboration",
        ecosystemId = "catEntryEcosystem_84b53163-327b-4b1b-8c96-9334d292f9f5",
        ecosystemName = "Essentials",
        ecosystemVersion ="6072dcfd-83e6-436c-b1f7-0239a34bcf74")
@Entity(name = "Extension Response Meta", id = "localDomainEntity_415a46c3-047d-4caf-9169-5f2413759b34", primaryKey = "Message", supportStore = false, options = {})
public class ExtensionResponseMeta {

    @Searchable
    @ToString
    @Field(name = "Message", required = true, type = "RichText", attributes = {@Attribute(name = "visualWidth", value = "M"), @Attribute(name = "toolTip", value = "'Detailed message on extension response'")}, options = {})
    public String message;

    @Field(name = "Technical Detailed Error Report", type = "RichText", required = false, attributes = {@Attribute(name = "visualWidth", value = "L"), @Attribute(name = "toolTip", value = "'The system showed some error details to help the technical team understand what went wrong.'")}, options = {})
    public String technicalDetailedErrorReport;

    @Field.PickOne(name = "Response Type", values = {"SUCCESS","INPUT_ERROR","LOGIC_ERROR","AUTHENTICATION_ERROR","SYSTEM_ERROR","UNAVAILABILITY_ERROR"}, required = true, attributes = {@Attribute(name = "visualWidth", value = "S"), @Attribute(name = "toolTip", value = "'Represents exception types to help categorize and handle errors'")}, options = {})
    public String responseType;

    @Field(name = "Time Taken In Seconds", type = "Number", required = true, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {})
    public Double timeTakenInSeconds;

}
