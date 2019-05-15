package com.eclipsesource.jaxrs.sample.service.impl;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eclipsesource.jaxrs.sample.service.GreetingResource;

public class Activator implements BundleActivator {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private ServiceRegistration<GreetingResource> registration;

    @Override
    public void start(BundleContext context) throws Exception {
        log.info("Registering service {}", GreetingResource.class);
        registration = context.registerService(GreetingResource.class, new SimpleResource(), null);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        log.info("Un-registering service {}", GreetingResource.class);
        registration.unregister();
    }
}
