package com.systelab.controlplane.repository;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class TenantNotFoundException extends RuntimeException {

    private final String id;

    public TenantNotFoundException(UUID id) {
        super("tenant-not-found-" + id.toString());
        this.id = id.toString();
    }

    public String getTenantId() {
        return id;
    }
}