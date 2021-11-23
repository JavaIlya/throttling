package com.nixsolutions.api;

import com.nixsolutions.model.Tenant;
import com.nixsolutions.repository.TenantRepository;
import com.nixsolutions.service.CallsCounterService;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.*;
import lombok.RequiredArgsConstructor;

import javax.persistence.PersistenceException;

import java.util.Optional;

import static java.lang.String.format;

@Controller("/{tenant}")
@RequiredArgsConstructor
public class TenantApi {

    private final CallsCounterService callsCounterService;
    private final TenantRepository tenantRepository;

    @Post(value = "/create", consumes = "application/json")
    public HttpResponse<?> createTenant(
            @PathVariable("tenant") String tenantName,
            @Body("allowedCallsPerSecond") int allowedCallsPerSecond) {
        Tenant tenant = new Tenant(tenantName, allowedCallsPerSecond);
        try {
            tenantRepository.save(tenant);
            callsCounterService.addTenant(tenantName);
        } catch (PersistenceException e) {
            return HttpResponse.badRequest(format("Cannot create tenant with name '%s', does it already exists?", tenantName));
        }
        return HttpResponse.created(tenant);
    }

    @Get("/callsPerSecondAvailable")
    public HttpResponse<?> getCallsPerSecondAvailable(@PathVariable("tenant") String tenantName) {
        Optional<Tenant> tenant = tenantRepository.findById(tenantName);

        if (tenant.isPresent()) {
            return HttpResponse.ok(tenant.get().getAllowedCallsPerSecond());
        }

        return HttpResponse.badRequest(format("There's not tenant with '%s' name", tenantName));
    }

}
