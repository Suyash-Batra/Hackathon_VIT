package com.wildguard.conservation.dto;

import com.wildguard.conservation.entity.ResourceStatus;

import jakarta.validation.constraints.NotNull;

// Used to send a resource to maintenance or bring it back.
public class ResourceStatusRequest {

    @NotNull
    private ResourceStatus status;

    // getters and setters

    public ResourceStatus getStatus() {
        return status;
    }

    public void setStatus(ResourceStatus status) {
        this.status = status;
    }
}
