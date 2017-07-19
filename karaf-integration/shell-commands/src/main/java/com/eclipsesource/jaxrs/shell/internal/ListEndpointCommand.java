/*******************************************************************************
 * Copyright (c) 2014 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Holger Staudacher - initial API and implementation
 *    Markus von Rüden - various updates and enhancements
 ******************************************************************************/

package com.eclipsesource.jaxrs.shell.internal;

import java.util.List;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import com.eclipsesource.jaxrs.publisher.api.ApplicationRegistry;

@Command(scope = "rest", name = "list-endpoints", description="List all endpoints")
@Service
public class ListEndpointCommand implements Action {

    @Reference
    private ApplicationRegistry applicationRegistry;

    @Override
    public Object execute() throws Exception {
        System.out.println("Listing all registered endpoints:");
        List<String> endpoints = applicationRegistry.getEndpoints();
        if (endpoints.isEmpty()) {
            System.out.println("No endpoints registered");
        } else {
            endpoints.forEach(e -> System.out.println(e));
        }
        return null;
    }
}
