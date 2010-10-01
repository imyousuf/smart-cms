package com.smartitengineering.cms.client.impl;

import com.google.inject.AbstractModule;
import com.smartitengineering.cms.binder.guice.Initializer;
import com.smartitengineering.cms.client.api.RootResource;
import com.smartitengineering.cms.client.api.WorkspaceContentResouce;
import com.smartitengineering.cms.client.api.domains.Workspace;
import com.smartitengineering.util.bean.guice.GuiceUtil;
import com.smartitengineering.util.rest.client.ApplicationWideClientFactoryImpl;
import com.smartitengineering.util.rest.client.ClientUtil;
import com.smartitengineering.util.rest.client.ResourceLink;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Properties;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseTestingUtility;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Unit test for simple App.
 */
public class AppTest {

  private static final int PORT = 10080;
  public static final String DEFAULT_NS = "com.smartitengineering";
  public static final String ROOT_URI_STRING = "http://localhost:" + PORT + "/";
  public static final String TEST = "test";
  public static final String TEST_NS = "testNS";
  private static final HBaseTestingUtility TEST_UTIL = new HBaseTestingUtility();
  private static final Logger LOGGER = LoggerFactory.getLogger(AppTest.class);
  private static Server jettyServer;

  @BeforeClass
  public static void globalSetup() throws Exception {
    /*
     * Start HBase and initialize tables
     */
    try {
      TEST_UTIL.startMiniCluster();
    }
    catch (Exception ex) {
      LOGGER.error(ex.getMessage(), ex);
    }
    HBaseAdmin admin = new HBaseAdmin(TEST_UTIL.getConfiguration());
    HTableDescriptor workspaceTable = new HTableDescriptor("workspace");
    workspaceTable.addFamily(new HColumnDescriptor("self"));
    workspaceTable.addFamily(new HColumnDescriptor("repInfo"));
    workspaceTable.addFamily(new HColumnDescriptor("repData"));
    workspaceTable.addFamily(new HColumnDescriptor("varInfo"));
    workspaceTable.addFamily(new HColumnDescriptor("varData"));
    workspaceTable.addFamily(new HColumnDescriptor("friendlies"));
    admin.createTable(workspaceTable);

    /*
     * Ensure DIs done
     */
    Properties properties = new Properties();
    properties.setProperty(GuiceUtil.CONTEXT_NAME_PROP, "com.smartitengineering.dao.impl.hbase");
    properties.setProperty(GuiceUtil.IGNORE_MISSING_DEP_PROP, Boolean.toString(false));
    properties.setProperty(GuiceUtil.MODULES_LIST_PROP, ConfigurationModule.class.getName());
    GuiceUtil.getInstance(properties).register();
    Initializer.init();

    /*
     * Start web application container
     */
    jettyServer = new Server(PORT);
    final String webapp = "./src/test/webapp/";
    if (!new File(webapp).exists()) {
      throw new IllegalStateException("WebApp file/dir does not exist!");
    }
    WebAppContext webAppHandler = new WebAppContext(webapp, "/");
    jettyServer.setHandler(webAppHandler);
    jettyServer.setSendDateHeader(true);
    jettyServer.start();

    /*
     * Setup client properties
     */
    System.setProperty(ApplicationWideClientFactoryImpl.TRACE, "true");
  }

  @AfterClass
  public static void globalTearDown() throws Exception {
    TEST_UTIL.shutdownMiniCluster();
    jettyServer.stop();
  }

  @Test
  public void testStartup() throws URISyntaxException {
    RootResource resource = RootResourceImpl.getRoot(new URI("http://localhost:" + PORT + "/"));
    Assert.assertNotNull(resource);
    Assert.assertEquals(0, resource.getWorkspaces().size());
  }

  @Test
  public void testCreationAndRetrievalWithNameOnly() throws URISyntaxException {
    RootResource resource = RootResourceImpl.getRoot(new URI("http://localhost:" + PORT + "/"));
    final MultivaluedMap<String, String> map = new MultivaluedMapImpl();
    map.add("name", TEST);
    ClientResponse response = resource.post(MediaType.APPLICATION_FORM_URLENCODED, map, ClientResponse.Status.CREATED);
    ResourceLink link = ClientUtil.createResourceLink(WorkspaceContentResouce.WORKSPACE_CONTENT, response.getLocation(),
                                                      MediaType.APPLICATION_JSON);
    WorkspaceContentResouce workspaceContentResource = new WorkspaceContentResourceImpl(resource, link);
    Assert.assertNotNull(workspaceContentResource.getLastReadStateOfEntity());
    Workspace workspace = workspaceContentResource.getLastReadStateOfEntity();
    Assert.assertEquals(TEST, workspace.getId().getName());
    Assert.assertEquals(DEFAULT_NS, workspace.getId().getGlobalNamespace());
    Collection<WorkspaceContentResouce> resources = resource.getWorkspaces();
    Assert.assertEquals(1, resource.getWorkspaces().size());
    workspaceContentResource = resources.iterator().next();
    workspace = workspaceContentResource.getLastReadStateOfEntity();
    Assert.assertEquals(TEST, workspace.getId().getName());
    Assert.assertEquals(DEFAULT_NS, workspace.getId().getGlobalNamespace());
  }

  @Test
  public void testCreationAndRetrievalWithNamespace() throws URISyntaxException {
    RootResource resource = RootResourceImpl.getRoot(new URI(ROOT_URI_STRING));
    final MultivaluedMap<String, String> map = new MultivaluedMapImpl();
    map.add("name", TEST);
    map.add("namespace", TEST_NS);
    ClientResponse response = resource.post(MediaType.APPLICATION_FORM_URLENCODED, map, ClientResponse.Status.CREATED);
    ResourceLink link = ClientUtil.createResourceLink(WorkspaceContentResouce.WORKSPACE_CONTENT, response.getLocation(),
                                                      MediaType.APPLICATION_JSON);
    WorkspaceContentResouce workspaceContentResource = new WorkspaceContentResourceImpl(resource, link);
    Assert.assertNotNull(workspaceContentResource.getLastReadStateOfEntity());
    Workspace workspace = workspaceContentResource.getLastReadStateOfEntity();
    Assert.assertEquals(TEST, workspace.getId().getName());
    Assert.assertEquals(TEST_NS, workspace.getId().getGlobalNamespace());
    Collection<WorkspaceContentResouce> resources = resource.getWorkspaces();
    Assert.assertEquals(2, resource.getWorkspaces().size());
    workspaceContentResource = resources.iterator().next();
    workspace = workspaceContentResource.getLastReadStateOfEntity();
    Assert.assertEquals(TEST, workspace.getId().getName());
    Assert.assertEquals(TEST_NS, workspace.getId().getGlobalNamespace());
  }

  @Test
  public void testConditionalRootResourceGet() throws Exception {
    HttpClient client = new HttpClient();
    GetMethod method = new GetMethod(ROOT_URI_STRING);
    client.executeMethod(method);
    Assert.assertEquals(200, method.getStatusCode());
    Header date = method.getResponseHeader(HttpHeaders.LAST_MODIFIED);
    String dateStr = date.getValue();
    Header ifDate = new Header(HttpHeaders.IF_MODIFIED_SINCE, dateStr);
    method = new GetMethod(ROOT_URI_STRING);
    method.addRequestHeader(ifDate);
    client.executeMethod(method);
    Assert.assertEquals(304, method.getStatusCode());
  }

  public static class ConfigurationModule extends AbstractModule {

    @Override
    protected void configure() {
      bind(Configuration.class).toInstance(TEST_UTIL.getConfiguration());
    }
  }
}