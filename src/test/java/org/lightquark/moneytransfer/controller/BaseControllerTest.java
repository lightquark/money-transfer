package org.lightquark.moneytransfer.controller;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.lightquark.moneytransfer.service.AccountService;
import org.lightquark.moneytransfer.web.JettyWebServer;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

public abstract class BaseControllerTest {

    private static final String LOCALHOST_URI = "http://localhost:8080/";
    private static JettyWebServer webServer;
    private static Client client;
    private static WebTarget webTarget;

    AccountService accountService = AccountService.getInstance();

    @BeforeClass
    public static void beforeClass() {
        webServer = new JettyWebServer();
        webServer.startForTests();
        client = ClientBuilder.newClient();
        webTarget = client.target(LOCALHOST_URI);
    }

    @AfterClass
    public static void afterClass() {
        client.close();
        webServer.stop();
    }

    @Before
    public void before() {
        accountService.clear();
    }

    static Response assertOkResponse(Response response) {
        Assert.assertNotNull(response);
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        Assert.assertNotNull(response.getEntity());
        return response;
    }

    static void assertBadRequestResponse(Response response) {
        Assert.assertNotNull(response);
        Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        Assert.assertNotNull(response.getEntity());
    }

    static Response get(String path, String... params) {
        return createTargetWithQueryParams(path, params).request().get();
    }

    static Response put(String path, String... params) {
        return createTargetWithQueryParams(path, params).request().put(Entity.text(""));
    }

    static Response delete(String path, String... params) {
        return createTargetWithQueryParams(path, params).request().delete();
    }

    private static WebTarget createTargetWithQueryParams(String path, String... params) {
        Assert.assertEquals(0, params.length % 2);

        WebTarget target = webTarget.path(path);
        int i = 0;
        while (i < params.length - 1) {
            target = target.queryParam(params[i], params[i + 1]);
            i += 2;
        }

        return target;
    }

}
