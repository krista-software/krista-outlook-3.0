package app.krista.extensions.essentials.collaboration.outlook3.service;

public class EmailAddress {

    private final String name;
    private final String mailAddress;

    public EmailAddress(String name, String emailAddress) {
        this.name = name;
        this.mailAddress = emailAddress;
    }

    public String getName() {
        return name;
    }

    public String getMailAddress() {
        return mailAddress;
    }
}
