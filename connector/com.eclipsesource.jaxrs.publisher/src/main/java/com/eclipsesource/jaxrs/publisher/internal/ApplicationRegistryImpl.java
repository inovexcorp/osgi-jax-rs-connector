
package com.eclipsesource.jaxrs.publisher.internal;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.Path;

import org.osgi.service.http.HttpService;

import com.eclipsesource.jaxrs.publisher.ServletConfiguration;
import com.eclipsesource.jaxrs.publisher.api.ApplicationRegistry;

/**
 *
 * @author mvrueden
 */
public class ApplicationRegistryImpl implements ApplicationRegistry {

    private ServletConfiguration servletConfiguration;
    private Configuration configuration;
    private final ServiceContainer serviceContainer;
    private final Map<HttpService, Map<String, JerseyContext>> contextMap = new HashMap<>();

    public ApplicationRegistryImpl(Configuration configuration, ServiceContainer serviceContainer) {
        this.configuration = Objects.requireNonNull(configuration);
        this.serviceContainer = Objects.requireNonNull(serviceContainer);
    }

    public void setServletConfiguration(ServletConfiguration servletConfiguration) {
        this.servletConfiguration = servletConfiguration;
    }

    /**
     * Add a specific resource to the httpService.
     * The service may has a {@link ApplicationPath} annotation which indicates
     * where the service should be deployed to (e.g. /services).
     * 
     * @param httpService The HTTP Service to attach the resource to
     * @param service The service to attach
     */
    public void addResource(HttpService httpService, Object service) {
        // Determine location (e.g. /services)
        String location = getApplicationPath(service);

        // Get JerseyContext and create if not already created
        JerseyContext context = getContext(httpService, location);
        if (context == null) {
            contextMap.putIfAbsent(httpService, new HashMap<>());
            contextMap.get(httpService).put(location,
                    createJerseyContext(
                            httpService,
                            new JerseyContextConfiguration()
                                    .withPublishDelay(configuration.getPublishDelay())
                                    .withRootPath(location)
                            , servletConfiguration,
                            serviceContainer));
            context = contextMap.get(httpService).get(location);
        }
        context.addResource(service);
    }

    @Override
    public List<String> getEndpoints() {
        final List<String> endpoints = contextMap.values().stream()
                .flatMap(entry -> entry.entrySet().stream())
                .flatMap(e -> {
                    // get servlet path and ensure it ends with /
                    final String servletPath = !e.getKey().endsWith("/") ? e.getKey() + "/" : e.getKey();
                    // get endpoints
                    return e.getValue().getRootApplication().getResources()
                            .stream()
                            .filter(resource -> hasAnnotation(resource, Path.class))
                            .map(resource -> getAnnotation(resource, Path.class).value())
                            .filter(path -> path != null && !path.trim().isEmpty())
                            .map(path -> path.startsWith("/") ? path.substring(1, path.length()) : path)
                            .map(path -> servletPath + path);

                }).collect(Collectors.toList());
        return endpoints;
    }

    private <T extends Annotation> T getAnnotation(Object resource, Class<T> annotationClass) {
            T annotation = resource.getClass().getAnnotation(annotationClass);
            if( annotation == null ) {
                Class<?>[] interfaces = resource.getClass().getInterfaces();
                for( Class<?> type : interfaces ) {
                    annotation = type.getAnnotation(annotationClass);
                    if (annotation != null) {
                        return annotation;
                    }
                }
            }
            return annotation;
    }

    // TODO MVR this is duplicated code ...
    private boolean hasAnnotation(Object service, Class<? extends Annotation> annotationClass) {
        boolean result = service.getClass().isAnnotationPresent(annotationClass);
        if( !result ) {
            Class<?>[] interfaces = service.getClass().getInterfaces();
            for( Class<?> type : interfaces ) {
                result = result || type.isAnnotationPresent(annotationClass);
            }
        }
        return result;
    }

    private JerseyContext getContext(HttpService httpService, String location) {
        if (contextMap.get(httpService) != null &&
            contextMap.get(httpService).get(location) != null) {
            return contextMap.get(httpService).get(location);
        }
        return null;
    }

    private String getApplicationPath(Object service) {
        final ApplicationPath applicationPath = findApplicationPathAnnotation(service.getClass());
        if (applicationPath != null
                && applicationPath.value() != null
                && !applicationPath.value().isEmpty()
                && !applicationPath.value().trim().isEmpty()) {
            return prependSlashIfMissing(applicationPath.value());
        }
        return prependSlashIfMissing(configuration.getDefaultRootPath());
    }

    public void removeResource(HttpService httpService, Object resource) {
        final String applicationPath = getApplicationPath(resource);
        final JerseyContext jerseyContext = getContext(httpService, applicationPath);
        if( jerseyContext != null ) {
            jerseyContext.removeResource( resource );
        }
    }

    public List<JerseyContext> removeHttpService(HttpService service) {
        Map<String, JerseyContext> remove = contextMap.remove(service);
        if (remove != null) {
            return new ArrayList<>(remove.values());
        }
        return new ArrayList<>();
    }

    public void updateServletConfiguration(HttpService httpService, ServletConfiguration servletConfiguration) {
        if (contextMap.get(httpService) != null) {
            contextMap.get(httpService).values().forEach(context -> {
                context.updateServletConfiguration( servletConfiguration );
            });
        }
    }

    public void updateAppConfiguration(HttpService httpService, ServiceContainer applicationConfigurations) {
        if (contextMap.get(httpService) != null) {
            contextMap.get(httpService).values().forEach(context -> context.updateAppConfiguration( applicationConfigurations));
        }
    }

    public void updateConfiguration(HttpService httpService, Configuration configuration) {
        // update the default root path if it changed
        if (!this.configuration.getDefaultRootPath().equals(configuration.getDefaultRootPath())) {
            JerseyContext context = getContext(httpService, this.configuration.getDefaultRootPath());
            if (context != null) {
                JerseyContextConfiguration newConfiguration = new JerseyContextConfiguration()
                        .withPublishDelay(configuration.getPublishDelay())
                        .withRootPath(configuration.getDefaultRootPath());
                context.updateConfiguration(newConfiguration);
            }
        }

        // update all contexts according to configuration
        contextMap.values().stream().flatMap(map -> map.entrySet().stream()).forEach(entry -> {
            JerseyContextConfiguration newConfiguration = new JerseyContextConfiguration()
                    .withPublishDelay(configuration.getPublishDelay())
                    .withRootPath(entry.getKey());
            entry.getValue().updateConfiguration(newConfiguration);
        });

        this.configuration = configuration;
    }

    static JerseyContext createJerseyContext( HttpService service,
                                              JerseyContextConfiguration configuration,
                                              ServletConfiguration servletConfiguration,
                                              ServiceContainer serviceContainer) {
        return new JerseyContext( service, configuration, servletConfiguration, serviceContainer );
    }

    private static String prependSlashIfMissing(String applicationPath) {
        if (!applicationPath.startsWith("/")) {
            return  "/" + applicationPath;
        }
        return applicationPath;
    }

    private static ApplicationPath findApplicationPathAnnotation(Class type) {
        if (type.getAnnotation(ApplicationPath.class) != null) {
            return (ApplicationPath) type.getAnnotation(ApplicationPath.class);
        }
        for (Class eachType : type.getInterfaces()) {
            ApplicationPath applicationPath = findApplicationPathAnnotation(eachType);
            if (applicationPath != null) {
                return applicationPath;
            }
        }
        return null;
    }
}
