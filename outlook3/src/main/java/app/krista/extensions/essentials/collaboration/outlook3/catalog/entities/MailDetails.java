package app.krista.extensions.essentials.collaboration.outlook3.catalog.entities;

import app.krista.extension.impl.anno.*;
import app.krista.model.base.File;

import java.util.List;

@Domain(id = "catEntryDomain_5fa2fc97-4b17-44cf-b98f-aa91a459a091",
        name = "Collaboration",
        ecosystemId = "catEntryEcosystem_84b53163-327b-4b1b-8c96-9334d292f9f5",
        ecosystemName = "Essentials",
        ecosystemVersion = "442034bc-7967-4349-9b66-6b13682ca806")
@Entity(name = "Mail Details", id = "localDomainEntity_0fb99723-377c-419e-b24e-0ab0ce948e8c", primaryKey = "Message ID", supportStore = false)
public class MailDetails {

    @Field.Text(name = "From", required = false)
    public String from;

    @Field.Text(name = "To")
    public String to;

    @Field(name = "Message", type = "RichText")
    public String message;

    @Searchable
    @Field.Text(name = "Subject")
    public String subject;

    @Field.File(name = "File Attachment", multipleFileUpload = true, required = false)
    public List<File> fileAttachment;

    @Field.Desc(name = "Item attachment", type = "[ RichText ]", required = false)
    public List<String> itemAttachment;

    @Field.File(name = "Reference attachment", required = false, attributes = {@Attribute(name = "visualWidth", value = "S")})
    public File referenceAttachment;

    @Field.Text(name = "Message ID", required = false, attributes = {@Attribute(name = "visualWidth", value = "S")})
    public String messageID;

    @Field.Text(name = "Cc", required = false, attributes = {@Attribute(name = "visualWidth", value = "S")})
    public String cc;

    @Field.Text(name = "Bcc", required = false, attributes = {@Attribute(name = "visualWidth", value = "S")})
    public String bcc;

    @Field.Boolean(name = "Is Read", required = false, attributes = {@Attribute(name = "visualWidth", value = "S")})
    public Boolean isRead;

    @Field.Text(name = "Reply To", required = false, attributes = {@Attribute(name = "visualWidth", value = "S")})
    public String replyTo;

    @Field.Date(name = "Send Date and Time", showHowManyDaysInViewer = 60, allowPast = false, allowToday = false, allowFuture = false, attributes = {@Attribute(name = "visualWidth", value = "S")})
    public Long sendDateAndTime;

    @Field.Date(name = "Received Date and Time", showHowManyDaysInViewer = 60, allowPast = false, allowToday = false, allowFuture = false, attributes = {@Attribute(name = "visualWidth", value = "S")})
    public Long receivedDateAndTime;

    @Field.Desc(name = "Categories", type = "[ Text ]", required = false)
    public List<String> categories;

    @Field.Text(name = "Conversation ID", required = false, attributes = {@Attribute(name = "visualWidth", value = "S")})
    public String conversationID;

    @Field(name = "Unique Body", type = "RichText", required = false, attributes = {@Attribute(name = "visualWidth", value = "L")})
    public String uniqueBody;

    @Field.Text(name = "Sensitivity", required = false, attributes = {@Attribute(name = "visualWidth", value = "S")})
    public String sensitivity;

    @Override
    public String toString() {
        return new StringBuilder("MailDetails{")
                .append("messageID='").append(messageID).append("', ")
                .append("from='").append(from).append("', ")
                .append("to='").append(to).append("', ")
                .append("cc='").append(cc).append("', ")
                .append("bcc='").append(bcc).append("', ")
                .append("replyTo='").append(replyTo).append("', ")
                .append("subject='").append(subject).append("', ")
                .append("message='").append(message).append("', ")
                .append("isRead=").append(isRead).append(", ")
                .append("categories=").append(categories)
                .append("Conversation ID=").append(conversationID)
                .append("sensitivity='").append(sensitivity).append("'")
                .append('}')
                .toString();
    }

}