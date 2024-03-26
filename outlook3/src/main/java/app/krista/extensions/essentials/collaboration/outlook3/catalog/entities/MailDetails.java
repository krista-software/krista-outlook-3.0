package app.krista.extensions.essentials.collaboration.outlook3.catalog.entities;

import app.krista.extension.impl.anno.*;
import app.krista.model.base.File;

import java.util.List;

@Domain(id = "catEntryDomain_5fa2fc97-4b17-44cf-b98f-aa91a459a091",
        name = "Collaboration",
        ecosystemId = "catEntryEcosystem_84b53163-327b-4b1b-8c96-9334d292f9f5",
        ecosystemName = "Essentials",
        ecosystemVersion = "442034bc-7967-4349-9b66-6b13682ca806")
@Entity(name = "Mail Details", id = "localDomainEntity_0fb99723-377c-419e-b24e-0ab0ce948e8c", primaryKey = "Message ID", options = {})
public class MailDetails {

    @Field.Text(name = "From", required = false, attributes = {}, options = {})
    public String from;

    @Field.Text(name = "To", required = true, attributes = {}, options = {})
    public String to;

    @Field(name = "Message", type = "RichText", required = true, attributes = {}, options = {})
    public String message;

    @Searchable
    @Field.Text(name = "Subject", required = true, attributes = {}, options = {})
    public String subject;

    @Field.File(name = "File Attachment", multipleFileUpload = true, required = false, attributes = {}, options = {})
    public List<File> fileAttachment;

    @Field.Desc(name = "Item attachment", type = "[ Label ]", required = false)
    public List<String> itemAttachment;

    @Field.File(name = "Reference attachment", multipleFileUpload = false, required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {})
    public File referenceAttachment;

    @Field.Text(name = "Message ID", required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {})
    public String messageID;

    @Field.Text(name = "Cc", required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {})
    public String cc;

    @Field.Text(name = "Bcc", required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {})
    public String bcc;

    @Field.Boolean(name = "Is Read", required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {})
    public Boolean isRead;

    @Field.Text(name = "Reply To", required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {})
    public String replyTo;

    @Field.Date(name = "Send Date and Time", required = false, includeTimeOfDay = true, showHowManyDaysInViewer = 60, allowPast = false, allowToday = false, allowFuture = false, defaultTimeSpan = 1, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {})
    public Long sendDateAndTime;

    @Field.Date(name = "Received Date and Time", required = false, includeTimeOfDay = true, showHowManyDaysInViewer = 60, allowPast = false, allowToday = false, allowFuture = false, defaultTimeSpan = 1, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {})
    public Long receivedDateAndTime;

    @Field.Desc(name = "Categories", type = "[ Text ]", required = false)
    public List<String> categories;

    @Override
    public String toString() {
        return new StringBuilder()
                .append("MailDetails{")
                .append("messageId='").append(messageID).append("'")
                .append("from='").append(from).append("'")
                .append(", to='").append(to).append("'")
                .append(", subject='").append(subject).append("'")
                .append(", message='").append(message).append("'")
                .append("}")
                .toString();
    }
}
