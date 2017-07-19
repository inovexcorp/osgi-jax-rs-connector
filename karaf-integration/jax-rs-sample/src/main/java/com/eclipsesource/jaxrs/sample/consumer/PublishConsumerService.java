package com.eclipsesource.jaxrs.sample.consumer;

import java.util.Map;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eclipsesource.jaxrs.consumer.ConsumerPublisher;
import com.eclipsesource.jaxrs.sample.service.GreetingResource;

@Component(immediate = true)
public class PublishConsumerService {

    private final Logger log  = LoggerFactory.getLogger(getClass());

    private ConsumerPublisher publisher;

    @Activate
    public void activate(Map<String, Object> config) {
        log.info("Publishing jax-rs consumer");
        publisher.publishConsumers("http://localhost:8181/services",
                new Class<?>[]{GreetingResource.class},
                new Object[]{});
    }

    @Reference(service = ConsumerPublisher.class, unbind = "unSetPublisher")
    public void setPublisher(ConsumerPublisher publisher) {
        this.publisher = publisher;
    }

    protected void unSetPublisher(ConsumerPublisher consumerPublisher) {
        this.publisher = null;
    }

    public ConsumerPublisher getPublisher() {
        return publisher;
    }
}
