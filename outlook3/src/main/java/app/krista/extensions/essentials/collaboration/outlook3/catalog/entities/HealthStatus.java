package app.krista.extensions.essentials.collaboration.outlook3.catalog.entities;

import app.krista.extension.impl.anno.*;


@Domain(id = "catEntryDomain_5fa2fc97-4b17-44cf-b98f-aa91a459a091",
        name = "Collaboration",
        ecosystemId = "catEntryEcosystem_84b53163-327b-4b1b-8c96-9334d292f9f5",
        ecosystemName = "Essentials",
        ecosystemVersion ="6072dcfd-83e6-436c-b1f7-0239a34bcf74")
@Entity(name = "Health Status", id = "localDomainEntity_0442f6c7-1c6e-457d-be3c-bd395a31cbce", primaryKey = "Extension Name", supportStore = false)
public class HealthStatus {

    @Searchable
    @ToString
    @Field.Text(name = "Extension Name", attributes = {@Attribute(name = "visualWidth", value = "S"), @Attribute(name = "toolTip", value = "'Name of the extension reporting health status'")})
    public String extensionName;

    @Searchable
    @ToString
    @Field(name = "Current Memory Usage MB", type = "Number", attributes = {@Attribute(name = "visualWidth", value = "S"), @Attribute(name = "toolTip", value = "'Current memory usage in megabytes'")})
    public Double currentMemoryUsageMB;

    @Searchable
    @ToString
    @Field(name = "Available Memory MB", type = "Number", attributes = {@Attribute(name = "visualWidth", value = "S"), @Attribute(name = "toolTip", value = "'Available memory in megabytes'")})
    public Double availableMemoryMB;

    @Searchable
    @ToString
    @Field(name = "Total Memory MB", type = "Number", attributes = {@Attribute(name = "visualWidth", value = "S"), @Attribute(name = "toolTip", value = "'Total memory allocated in megabytes'")})
    public Double totalMemoryMB;

    @Searchable
    @ToString
    @Field(name = "CPU Usage Percentage", type = "Number", attributes = {@Attribute(name = "visualWidth", value = "S"), @Attribute(name = "toolTip", value = "'Current CPU usage as a percentage'")})
    public Double cPUUsagePercentage;

    @Searchable
    @ToString
    @Field(name = "Active Threads", type = "Number", attributes = {@Attribute(name = "visualWidth", value = "S"), @Attribute(name = "toolTip", value = "'Number of active threads'")})
    public Double activeThreads;

    @Searchable
    @ToString
    @Field(name = "Uptime Hours", type = "Number", attributes = {@Attribute(name = "visualWidth", value = "S"), @Attribute(name = "toolTip", value = "'System uptime in hours'")})
    public Double uptimeHours;

    @Searchable
    @ToString
    @Field.PickOne(name = "System Status", values = {"HEALTHY","DEGRADED (SLOW RESPONSE)","UNHEALTHY","EXTENSION PROCESS OFFLINE","THIRD PARTY SYSTEM OFFLINE"}, attributes = {@Attribute(name = "visualWidth", value = "S"), @Attribute(name = "toolTip", value = "'Overall system status (HEALTHY, DEGRADED, UNHEALTHY)'")})
    public String systemStatus;

    @Searchable
    @ToString
    @Field(name = "Last Health Check Time", type = "Time", attributes = {@Attribute(name = "visualWidth", value = "S"), @Attribute(name = "toolTip", value = "'Timestamp of the last health check'")})
    public Long lastHealthCheckTime;

    @Searchable
    @ToString
    @Field.Text(name = "Auth Type", attributes = {@Attribute(name = "visualWidth", value = "S"), @Attribute(name = "toolTip", value = "'To check the authentication type'")})
    public String authType;

    @Searchable
    @ToString
    @Field(name = "Email", type = "Email", attributes = {@Attribute(name = "visualWidth", value = "S"), @Attribute(name = "toolTip", value = "'Email Id of the user'")})
    public String email;

    @Searchable
    @ToString
    @Field.Boolean(name = "Has Refresh Token", attributes = {@Attribute(name = "visualWidth", value = "S"), @Attribute(name = "toolTip", value = "'To check whether refresh token is available'")})
    public Boolean hasRefreshToken;

    @Field.Boolean(name = "Token Valid", attributes = {@Attribute(name = "visualWidth", value = "S"), @Attribute(name = "toolTip", value = "'To know whether referesh token is valid'")})
    public Boolean tokenValid;

}
