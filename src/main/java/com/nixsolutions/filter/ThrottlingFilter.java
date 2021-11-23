package com.nixsolutions.filter;

import com.nixsolutions.service.CallsCounterService;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Filter;
import io.micronaut.http.filter.HttpServerFilter;
import io.micronaut.http.filter.ServerFilterChain;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;

@Filter("/**")
@RequiredArgsConstructor
@Slf4j
public class ThrottlingFilter implements HttpServerFilter {

    private final CallsCounterService callsCounterService;

    @Override
    public Publisher<MutableHttpResponse<?>> doFilter(HttpRequest<?> request, ServerFilterChain chain) {
        String tenant = retrieveTenant(request.getPath());
        log.debug("request received for tenants {}", tenant);
        int requestsSent = callsCounterService.getCurrentRequestsSentCount(tenant);
        int allowedRequestsCount = callsCounterService.getAllowedRequestsCount(tenant);

        if (requestsSent >= allowedRequestsCount) {
            throw new RuntimeException("Throttled");
        }

        callsCounterService.incrementRequestsSentCount(tenant);
        return chain.proceed(request);
    }


    private String retrieveTenant(String uri) {
        String[] splitUri = uri.split("/");
        if (splitUri.length > 1) {
            return splitUri[1];
        }

        throw new RuntimeException("Cannot find tenant in the request");
    }
}
