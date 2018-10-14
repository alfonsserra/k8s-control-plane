package com.systelab.controlplane.controller;


import com.systelab.controlplane.model.Tenant;
import com.systelab.controlplane.model.TenantRequestInfo;
import com.systelab.controlplane.repository.TenantNotFoundException;
import com.systelab.controlplane.repository.TenantRepository;
import com.systelab.controlplane.service.KubernetesService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.annotation.security.PermitAll;
import javax.validation.Valid;
import java.net.URI;
import java.util.UUID;

@Api(value = "Tenant management", description = "API for Tenant management", tags = {"Tenant management"})
@RestController()
@CrossOrigin(origins = "*", allowedHeaders = "*", exposedHeaders = "Authorization", allowCredentials = "true")
@RequestMapping(value = "/controlplane/v1", produces = MediaType.APPLICATION_JSON_VALUE)
public class TenantController {


    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private KubernetesService kubernetesService;

    @ApiOperation(value = "Get all Tenants", notes = "")
    @GetMapping("tenants")
    @PermitAll
    public ResponseEntity<Page<Tenant>> getAllTenants(Pageable pageable) {
        return ResponseEntity.ok(tenantRepository.findAll(pageable));
    }

    @ApiOperation(value = "Get Tenant", notes = "", authorizations = {@Authorization(value = "Bearer")})
    @GetMapping("tenants/{uid}")
    public ResponseEntity<Tenant> getTenant(@PathVariable("uid") UUID tenantId) {
        return this.tenantRepository.findById(tenantId).map(ResponseEntity::ok).orElseThrow(() -> new TenantNotFoundException(tenantId));

    }

    @ApiOperation(value = "Create a Tenant", notes = "", authorizations = {@Authorization(value = "Bearer")})
    @PostMapping("tenants/tenant")
    public ResponseEntity<Tenant> createTenant(@RequestBody @ApiParam(value = "Tenant", required = true) @Valid TenantRequestInfo p) {

        Tenant tenant=kubernetesService.newTenant(p);
        Tenant createdTenant = this.tenantRepository.save(tenant);
        URI uri = MvcUriComponentsBuilder.fromController(getClass()).path("/{id}").buildAndExpand(createdTenant.getId()).toUri();
        return ResponseEntity.created(uri).body(createdTenant);
    }


    @ApiOperation(value = "Create or Update (idempotent) an existing Tenant", notes = "", authorizations = {@Authorization(value = "Bearer")})
    @PutMapping("tenants/{uid}")
    public ResponseEntity<Tenant> updateTenant(@PathVariable("uid") UUID tenantId, @RequestBody @ApiParam(value = "Tenant", required = true) @Valid Tenant p) {
        return this.tenantRepository
                .findById(tenantId)
                .map(existing -> {
                    p.setId(tenantId);
                    Tenant tenant = this.tenantRepository.save(p);
                    URI selfLink = URI.create(ServletUriComponentsBuilder.fromCurrentRequest().toUriString());
                    return ResponseEntity.created(selfLink).body(tenant);
                }).orElseThrow(() -> new TenantNotFoundException(tenantId));
    }


    @ApiOperation(value = "Delete a Tenant", notes = "", authorizations = {@Authorization(value = "Bearer")})
    @DeleteMapping("tenants/{uid}")
    public ResponseEntity<?> removeTenant(@PathVariable("uid") UUID tenantId) {
        return this.tenantRepository.findById(tenantId)
                .map(c -> {
                    tenantRepository.delete(c);
                    return ResponseEntity.noContent().build();
                }).orElseThrow(() -> new TenantNotFoundException(tenantId));
    }


}