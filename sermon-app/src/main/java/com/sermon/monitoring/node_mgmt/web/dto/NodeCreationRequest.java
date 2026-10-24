package com.sermon.monitoring.node_mgmt.web.dto;

import com.sermon.monitoring.node_mgmt.model.enums.NodeOpStatus;
import com.sermon.monitoring.node_mgmt.model.enums.NodeType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.IpAddress;

public record NodeCreationRequest (
    @NotBlank(message = "username is required")
    @Size(min=2, max=100 , message = "name should be between 2 and 100 characters")
    String name,

    @NotBlank(message = "OS Name is required")
    @Size(min=2, max=100 , message = "osName should be between 2 and 100 characters")
    String osName,

    @NotBlank(message = "IP address is required")
    @IpAddress(type = IpAddress.Type.ANY, message = "Enter a valid IP address")
    String ipAddress,

    @NotNull(message = "Node Type is required")
    NodeType type,

    @NotNull(message = "Operational Status is required")
    NodeOpStatus opStatus

){ }
