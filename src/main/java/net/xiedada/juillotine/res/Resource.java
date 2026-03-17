/*
   Copyright (c) 2026 Xie Youtian. All rights reserved.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

package net.xiedada.juillotine.res;

import net.xiedada.juillotine.ResponseTriplet;
import net.xiedada.juillotine.Service;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;


public class Resource {
    @Inject
    private Service service = new Service();

    @GET
    @Path("/")
    public Response getRoot() {
        try{
            return Response.status(Response.Status.FOUND).location(service.ensureUrl(service.options().defaultUrl()).toURI()).build();
        } catch (URISyntaxException e) {
            // What the fuck?
            throw new RuntimeException(e);
        }
    }

    @GET
    @Path("/{code}")
    public Response getCode(@PathParam("code") String code) {
        return responseFromTriplet(service.get(code));
    }

    @POST
    @Path("/")
    public Response postRoot(@FormParam("url") String url, @FormParam("code") String code) {
        return responseFromTriplet(service.create(url, code));
    }

    private Response responseFromTriplet(ResponseTriplet responseTriplet) {
        Response.ResponseBuilder rb = Response.status(responseTriplet.status());

        if  (responseTriplet.headers() != null) {
            responseTriplet.headers().forEach(rb::header);
        }
        if  (responseTriplet.body() != null) {
            rb.entity(responseTriplet.body());
        }

        return rb.build();
    }
}