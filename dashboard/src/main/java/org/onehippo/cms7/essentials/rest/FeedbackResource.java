/*
 * Copyright 2014 Hippo B.V. (http://www.onehippo.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onehippo.cms7.essentials.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.DatatypeConverter;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.rs.security.cors.CrossOriginResourceSharing;
import org.apache.cxf.transport.http.HTTPConduit;
import org.onehippo.cms7.essentials.dashboard.rest.BaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@CrossOriginResourceSharing(allowAllOrigins = true)
@Api(value = "/feedback", description = "Rest resource which provides access to the JIRA-based feedback mechanism")
@Produces({MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_FORM_URLENCODED})
@Path("/feedback")
public class FeedbackResource extends BaseResource {

    private final String baseResourceUri = "https://issues.onehippo.com/rest/api/2/";
    private final String base64Credentials = DatatypeConverter.printBase64Binary(
            "EssentialsFeedbackProvider:F04Yn38nHqm27ZHZ9YHf".getBytes());
    private long receiveTimeout = 2000;
    private long connectionTimeout = 2000;

    private static Logger log = LoggerFactory.getLogger(FeedbackResource.class);

    @ApiOperation(
            value = "Fetch a JIRA issue.",
            response = String.class)
    @GET
    @Path("/{path}")
    public String fetchJiraIssue(@PathParam("path") String path) {
        final WebClient client = WebClient.create(baseResourceUri + "issue/" + path);
        client.header("Authorization", "Basic " + base64Credentials);
        setTimeouts(client, connectionTimeout, receiveTimeout);
        String response = "";
        try {
            response = client.accept(MediaType.APPLICATION_JSON_TYPE).get(String.class);
            log.warn("response:\n\n" + response);
        } catch (Exception e) {
            log.error("Error relaying feedback GET request for path '" + path +"'.", e);
        }
        return response;
    }

    @ApiOperation(
            value = "Create a JIRA issue."
    )
    @POST
    @Path("/")
    public void createJiraIssue(String payload) {
        final WebClient client = WebClient.create(baseResourceUri + "issue");
        client.header("Authorization", "Basic " + base64Credentials);
        setTimeouts(client, connectionTimeout, receiveTimeout);
        String request = "{\n" +
                "    \"fields\": {\n" +
                "       \"project\":\n" +
                "       {\n" +
                "          \"key\": \"ESSENTIALS\"\n" +
                "       },\n" +
                "       \"summary\": \"Tobi testing\",\n" +
                "       \"description\": \"Tobi testing...\",\n" +
                "       \"issuetype\": {\n" +
                "          \"name\": \"Question\"\n" +
                "       }\n" +
                "   }\n" +
                "}";
        try {
            final Response response = client
                    .type(MediaType.APPLICATION_JSON_TYPE)
                    .accept(MediaType.APPLICATION_JSON_TYPE)
                    .post(request);
            log.warn("response:\n\n" + response);
        } catch (Exception e) {
            log.error("Error relaying feedback POST request.", e);
        }
    }

    private void setTimeouts(final WebClient client, final long connectionTimeout, final long receiveTimeout) {
        HTTPConduit conduit = WebClient.getConfig(client).getHttpConduit();
        if (receiveTimeout != 0) {
            conduit.getClient().setReceiveTimeout(receiveTimeout);
        }
        if (connectionTimeout != 0) {
            conduit.getClient().setConnectionTimeout(connectionTimeout);
        }
    }
}
