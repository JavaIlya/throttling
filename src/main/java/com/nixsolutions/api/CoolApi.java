package com.nixsolutions.api;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;

import java.util.Random;

@Controller("{tenant}/coolApi")
public class CoolApi {

    @Get("/feature")
    public HttpResponse<?> doSomething() throws Exception {
        Thread.sleep(2500);
        return new Random().nextInt() == 0 ? HttpResponse.ok() : HttpResponse.badRequest();
    }
}
