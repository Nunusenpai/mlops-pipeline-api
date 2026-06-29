package com.example.mlops;

import com.example.mlops.exception.GlobalExceptionMapper;
import com.example.mlops.exception.LinkedWorkspaceNotFoundExceptionMapper;
import com.example.mlops.exception.ModelDeprecatedExceptionMapper;
import com.example.mlops.exception.WorkspaceNotEmptyExceptionMapper;
import com.example.mlops.filter.LoggingFilter;
import com.example.mlops.resource.DiscoveryResource;
import com.example.mlops.resource.ModelResource;
import com.example.mlops.resource.WorkspaceResource;

import javax.ws.rs.ApplicationPath;
import org.glassfish.jersey.server.ResourceConfig;

@ApplicationPath("/api/v1")
public class JakartaRestConfiguration extends ResourceConfig {

    public JakartaRestConfiguration() {
        register(DiscoveryResource.class);
        register(WorkspaceResource.class);
        register(ModelResource.class);

        register(WorkspaceNotEmptyExceptionMapper.class);
        register(LinkedWorkspaceNotFoundExceptionMapper.class);
        register(ModelDeprecatedExceptionMapper.class);
        register(GlobalExceptionMapper.class);

        register(LoggingFilter.class);
    }
}