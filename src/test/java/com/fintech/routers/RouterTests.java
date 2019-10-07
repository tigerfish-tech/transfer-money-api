package com.fintech.routers;

import com.fintech.models.ErrorResponse;
import com.fintech.testutils.DefaultUndertowServer;
import com.fintech.testutils.HttpUtils;
import io.undertow.util.StatusCodes;
import java.io.IOException;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class RouterTests {

  private static DefaultUndertowServer server;

  @BeforeClass
  public static void initClass() {
    server = DefaultUndertowServer.createServer(Router.getInstance().routingHandler());
  }

  @AfterClass
  public static void afterClass() {
    server.stop();
  }

  @Test
  public void wrongMethodInvokeTest() throws IOException {
    try (CloseableHttpClient httpClient = HttpClients.createMinimal()) {
      HttpGet getExisting = new HttpGet(server.getUrl() + "/index");
      HttpResponse result = httpClient.execute(getExisting);

      ErrorResponse errorResponse = HttpUtils.readBody(result.getEntity(), ErrorResponse.class);
      Assert.assertEquals(StatusCodes.BAD_REQUEST, result.getStatusLine().getStatusCode());
      Assert.assertEquals(400, errorResponse.getCode());
      Assert.assertEquals("Method not found", errorResponse.getMessage());
    }
  }

}
