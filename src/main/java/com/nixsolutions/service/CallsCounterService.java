package com.nixsolutions.service;

import com.nixsolutions.model.Tenant;
import com.nixsolutions.repository.TenantRepository;
import io.micronaut.scheduling.annotation.Scheduled;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.String.format;

@Singleton
@RequiredArgsConstructor
@Slf4j
public class CallsCounterService {
    private final Map<String, AtomicInteger> sentRequestsCounter = new ConcurrentHashMap<>();
    private final Map<String, Integer> allowedRequestsCounter = new HashMap<>();

    private final TenantRepository tenantRepository;

    public void addTenant(String tenantName) {
        sentRequestsCounter.put(tenantName, new AtomicInteger(0));
    }

    public void incrementRequestsSentCount(String tenantName) {
        verifyTenantPresent(tenantName);
        sentRequestsCounter.get(tenantName).incrementAndGet();
    }

    public int getCurrentRequestsSentCount(String tenantName) {
        verifyTenantPresent(tenantName);
        return sentRequestsCounter.get(tenantName).get();
    }

    public int getAllowedRequestsCount(String tenantName) {
        verifyTenantPresent(tenantName);
        return allowedRequestsCounter.get(tenantName);
    }

    private void verifyTenantPresent(String tenantName) {
        if (!sentRequestsCounter.containsKey(tenantName)) {
            throw new RuntimeException(format("Tenant with name '%s' is not created", tenantName));
        }
    }

    @Scheduled(fixedRate = "1s")
    public void clearSentRequestCounter() {
        log.debug("Cleaning map");
        this.sentRequestsCounter.replaceAll((key, value) -> new AtomicInteger(0));
    }

    @PostConstruct
    private void init() {
        Iterable<Tenant> all = tenantRepository.findAll();
        all.forEach(tenant -> {
            String tenantName = tenant.getName();
            addTenant(tenantName);
            allowedRequestsCounter.put(tenantName, tenant.getAllowedCallsPerSecond());
        });
    }
}
