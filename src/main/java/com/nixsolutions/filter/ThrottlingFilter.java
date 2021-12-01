package com.nixsolutions.filter;

import com.nixsolutions.service.CallsCounterService;
import io.micronaut.core.util.StringUtils;
import io.micronaut.http.HttpAttributes;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Filter;
import io.micronaut.http.filter.HttpServerFilter;
import io.micronaut.http.filter.ServerFilterChain;
import io.micronaut.web.router.UriRouteMatch;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;

import java.util.Map;
import java.util.Optional;

@Filter("/**")
@RequiredArgsConstructor
@Slf4j
public class ThrottlingFilter implements HttpServerFilter {

    private final CallsCounterService callsCounterService;

    @Override
    public Publisher<MutableHttpResponse<?>> doFilter(HttpRequest<?> request, ServerFilterChain chain) {
        var uriRouteMatch = request.getAttributes()
                .get(HttpAttributes.ROUTE_MATCH.toString(), UriRouteMatch.class);

        if (uriRouteMatch.isEmpty() || !containsTenant(uriRouteMatch.get().getVariableValues())) {
            return chain.proceed(request);
        }

        Map<String, Object> uriVariables = uriRouteMatch.get().getVariableValues();

        String tenant = (String) uriVariables.get("tenant");

        log.debug("request received for tenants {}", tenant);
        final int requestsSent = callsCounterService.getCurrentRequestsSentCount(tenant);
        final int allowedRequestsCount = callsCounterService.getAllowedRequestsCount(tenant);

        if (requestsSent >= allowedRequestsCount) {
            throw new RuntimeException("Throttled");
        }

        callsCounterService.incrementRequestsSentCount(tenant);
        return chain.proceed(request);
    }


    private boolean containsTenant(Map<String, Object> uriVariables) {
        return uriVariables.containsKey("tenant");
    }
}
