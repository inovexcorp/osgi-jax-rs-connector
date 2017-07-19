package com.eclipsesource.jaxrs.sample.service;


import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.eclipsesource.jaxrs.sample.model.SimpleMessage;

@Path("/greeting")
public interface GreetingResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    SimpleMessage greeting();
}
