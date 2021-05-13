/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * ================================================================================
 * Modifications Copyright (C) 2019 Ericsson
 * =============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * ============LICENSE_END=========================================================
 */

package org.onap.ccsdk.sli.adaptors.iaas.impl;

import com.att.cdp.exceptions.ContextConnectionException;
import com.att.cdp.exceptions.ZoneException;
import com.att.cdp.openstack.util.ExceptionMapper;
import com.att.cdp.pal.util.Time;
import com.att.cdp.zones.ContextFactory;
import com.att.cdp.zones.spi.RequestState;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.woorea.openstack.base.client.OpenStackBaseException;
import com.woorea.openstack.base.client.OpenStackClientConnector;
import com.woorea.openstack.base.client.OpenStackSimpleTokenProvider;
import com.woorea.openstack.keystone.Keystone;
import com.woorea.openstack.keystone.api.TokensResource;
import com.woorea.openstack.keystone.model.Access;
import com.woorea.openstack.keystone.model.Access.Service;
import com.woorea.openstack.keystone.model.Access.Service.Endpoint;
import com.woorea.openstack.keystone.model.Authentication;
import com.woorea.openstack.keystone.model.Tenant;
import com.woorea.openstack.keystone.model.authentication.UsernamePassword;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is used to capture and cache the service catalog for a specific OpenStack provider.
 * <p>
 * This is needed because the way the servers are represented in the ECOMP product is as their fully qualified URL's.
 * This is very problematic, because we cant identify their region from the URL, URL's change, and we cant identify the
 * versions of the service implementations. In otherwords, the URL does not provide us enough information.
 * </p>
 * <p>
 * The zone abstraction layer is designed to detect the versions of the services dynamically, and step up or down to
 * match those reported versions. In order to do that, we need to know before hand what region we are accessing (since
 * the supported versions may be different by regions). We will need to authenticate to the identity service in order to
 * do this, plus we have to duplicate the code supporting proxies and trusted hosts that exists in the abstraction
 * layer, but that cant be helped.
 * </p>
 * <p>
 * What we do to circumvent this is connect to the provider using the lowest supported identity api, and read the entire
 * service catalog into this object. Then, we parse the vm URL to extract the host and port and match that to the
 * compute services defined in the catalog. When we find a compute service that has the same host name and port,
 * whatever region that service is supporting is the region for that server.
 * </p>
 * <p>
 * While we really only need to do this for compute nodes, there is no telling what other situations may arise where the
 * full service catalog may be needed. Also, there is very little additional cost (additional RAM) associated with
 * caching the full service catalog since there is no way to list only a portion of it.
 * </p>
 */
public class ServiceCatalogV2 extends ServiceCatalog {

    protected static final EELFLogger loggerV2 = EELFManager.getInstance().getLogger(ServiceCatalogV2.class);

    /**
     * The Openstack Access object that manages the authenticated token and access control
     */
    private Access access;

    /**
     * A map of endpoints for each service organized by service type
     */
    private Map<String /* Service Type */, List<Service.Endpoint>> serviceEndpoints;

    /**
     * A map of service types that are published
     */
    private Map<String /* Service Type */, Service> serviceTypes;

    /**
     * The tenant that we are accessing
     */
    private Tenant tenant;

    /**
     * A "token provider" that manages the authentication token that we obtain when logging in
     */
    private OpenStackSimpleTokenProvider tokenProvider;

    public ServiceCatalogV2(String identityURL, String tenantIdentifier, String principal, String credential,
            Properties properties) {
        super(identityURL, tenantIdentifier, principal, credential, null, properties);
    }

    @Override
    public void init() throws ZoneException {
        serviceTypes = new HashMap<>();
        serviceEndpoints = new HashMap<>();
        Class<?> connectorClass;
        OpenStackClientConnector connector;
        try {
            connectorClass = Class.forName(CLIENT_CONNECTOR_CLASS);
            connector = (OpenStackClientConnector) connectorClass.newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            loggerV2.error(e.getMessage());
            return;
        }
        Keystone keystone = getKeystone(identityURL, connector);

        String proxyHost = properties.getProperty(ContextFactory.PROPERTY_PROXY_HOST);
        String proxyPort = properties.getProperty(ContextFactory.PROPERTY_PROXY_PORT);
        String trustedHosts = properties.getProperty(ContextFactory.PROPERTY_TRUSTED_HOSTS, ""); //$NON-NLS-1$
        if (proxyHost != null && proxyHost.length() > 0) {
            keystone.getProperties().setProperty(com.woorea.openstack.common.client.Constants.PROXY_HOST, proxyHost);
            keystone.getProperties().setProperty(com.woorea.openstack.common.client.Constants.PROXY_PORT, proxyPort);
        }
        if (trustedHosts != null) {
            keystone.getProperties().setProperty(com.woorea.openstack.common.client.Constants.TRUST_HOST_LIST,
                    trustedHosts);
        }

        Authentication authentication = new UsernamePassword(principal, credential);
        TokensResource tokens = keystone.tokens();
        TokensResource.Authenticate authenticate = tokens.authenticate(authentication);
        if (projectIdentifier.length() == 32 && projectIdentifier.matches("[0-9a-fA-F]+")) { //$NON-NLS-1$
            authenticate = authenticate.withTenantId(projectIdentifier);
        } else {
            authenticate = authenticate.withTenantName(projectIdentifier);
        }

        /*
         * We have to set up the TrackRequest TLS collection for the ExceptionMapper
         */
        trackRequest();
        RequestState.put(RequestState.PROVIDER, "OpenStackProvider");
        RequestState.put(RequestState.TENANT, projectIdentifier);
        RequestState.put(RequestState.PRINCIPAL, principal);

        try {
            access = authenticate.execute();
            //Ensure that access or the access token is not null before
            //checking local expiration or accessing the tenant information
            if (access == null || access.getToken() == null) {
                throw new NullPointerException("The access key used to access the provider or access token is null." +
                        "Failed to init ServiceCatalogV2");
            }
            expiresLocal = getLocalExpiration(access);
            tenant = access.getToken().getTenant();
            tokenProvider = new OpenStackSimpleTokenProvider(access.getToken().getId());
            keystone.setTokenProvider(tokenProvider);
            parseServiceCatalog(access.getServiceCatalog());
        } catch (OpenStackBaseException e) {
            ExceptionMapper.mapException(e);
        } catch (Exception ex) {
            throw new ContextConnectionException(ex.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Service.Endpoint> getEndpoints(String serviceType) {
        Lock readLock = rwLock.readLock();
        readLock.lock();
        try {
            return serviceEndpoints.get(serviceType);
        } finally {
            readLock.unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getProjectId() {
        Lock readLock = rwLock.readLock();
        readLock.lock();
        try {
            return tenant.getId();
        } finally {
            readLock.unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getProjectName() {
        Lock readLock = rwLock.readLock();
        readLock.lock();
        try {
            return tenant.getName();
        } finally {
            readLock.unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> getRegions() {
        Lock readLock = rwLock.readLock();
        readLock.lock();
        try {
            return regions;
        } finally {
            readLock.unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getServiceTypes() {
        Lock readLock = rwLock.readLock();
        readLock.lock();
        try {
            ArrayList<String> result = new ArrayList<>();
            result.addAll(serviceTypes.keySet());
            return result;
        } finally {
            readLock.unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getVMRegion(VMURL url) {
        String region = null;
        if (url == null) {
            return region;
        }

        Pattern urlPattern = Pattern.compile("[^:]+://([^:/]+)(?::([0-9]+)).*");

        for (Endpoint endpoint : getEndpoints(ServiceCatalog.COMPUTE_SERVICE)) {
            String endpointUrl = endpoint.getPublicURL();
            Matcher matcher = urlPattern.matcher(endpointUrl);
            if (!matcher.matches() ||
                !url.getHost().equals(matcher.group(1)) ||
                (url.getPort() != null && !url.getPort().equals(matcher.group(2))) ) {
                continue;
            }

            region = endpoint.getRegion();
            break;
        }

        return region;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isServicePublished(String serviceType) {
        Lock readLock = rwLock.readLock();
        readLock.lock();
        try {
            return serviceTypes.containsKey(serviceType);
        } finally {
            readLock.unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        Lock lock = rwLock.readLock();
        lock.lock();
        try {
            builder.append(String.format("Service Catalog: tenant %s, id[%s], description[%s]%n", tenant.getName(), //$NON-NLS-1$
                    tenant.getId(), tenant.getDescription()));
            if (regions != null && !regions.isEmpty()) {
                builder.append(String.format("%d regions:%n", regions.size())); //$NON-NLS-1$
                for (String region : regions) {
                    builder.append("\t" + region + "%n"); //$NON-NLS-1$ //$NON-NLS-2$
                }
            }
            builder.append(String.format("%d services:%n", serviceEndpoints.size())); //$NON-NLS-1$
            for(Map.Entry<String, List<Access.Service.Endpoint>> entry : serviceEndpoints.entrySet()){
                List<Service.Endpoint> endpoints = entry.getValue();
                Service service = serviceTypes.get(entry.getKey());

                builder.append(String.format("\t%s [%s] - %d endpoints%n", service.getType(), service.getName(), //$NON-NLS-1$
                        endpoints.size()));
                for (Service.Endpoint endpoint : endpoints) {
                    builder.append(String.format("\t\tRegion [%s], public URL [%s]%n", endpoint.getRegion(), //$NON-NLS-1$
                            endpoint.getPublicURL()));
                }
            }
        } finally {
            lock.unlock();
        }

        return builder.toString();
    }

    /**
     * Parses the service catalog and caches the results
     *
     * @param services The list of services published by this provider
     */
    private void parseServiceCatalog(List<Service> services) {
        Lock lock = rwLock.writeLock();
        lock.lock();
        try {
            serviceTypes.clear();
            serviceEndpoints.clear();
            regions.clear();

            for (Service service : services) {
                String type = service.getType();
                serviceTypes.put(type, service);

                List<Service.Endpoint> endpoints = service.getEndpoints();
                for (Service.Endpoint endpoint : endpoints) {
                    List<Service.Endpoint> endpointList = serviceEndpoints.get(type);
                    if (endpointList == null) {
                        endpointList = new ArrayList<>();
                        serviceEndpoints.put(type, endpointList);
                    }
                    endpointList.add(endpoint);

                    String region = endpoint.getRegion();
                    if (!regions.contains(region)) {
                        regions.add(region);
                    }
                }
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Computes the local time when the access token will expire, after which we will need to re-login to access the
     * provider.
     *
     * @param accessKey The access key used to access the provider
     * @return The local time the key expires
     */
    private static long getLocalExpiration(Access accessKey) {
        Date now = Time.getCurrentUTCDate();
        Calendar issued = accessKey.getToken().getIssued_at();
        Calendar expires = accessKey.getToken().getExpires();
        if (issued != null && expires != null) {
            long tokenLife = expires.getTimeInMillis() - issued.getTimeInMillis();
            return now.getTime() + tokenLife;
        }
        return now.getTime();
    }

    protected Keystone getKeystone(String identityUrl, OpenStackClientConnector connector) {
        return new Keystone(identityURL, connector);
    }
}
