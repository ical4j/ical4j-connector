package org.ical4j.connector.api.controller;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.Response;
import org.ical4j.connector.ObjectCollection;
import org.ical4j.connector.ObjectStore;
import org.ical4j.connector.ObjectStoreFactory;
import org.ical4j.connector.command.*;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.ical4j.connector.ObjectCollection.DEFAULT_COLLECTION;

@Path("/collections")
public class CollectionController extends AbstractController {

    private final ObjectStore<ObjectCollection<?>> store;

    public CollectionController() {
        store = new ObjectStoreFactory().newInstance();
    }

    @GET
    public Response listCollections(@Context Request req) {
        return response(() -> {
            AtomicReference<List<ObjectCollection<?>>> result = new AtomicReference<>();
            new ListCollections(result::set, store).run();
            return result.get();
        }, req);
    }

    @POST
    public Response createCollection(@Context Request req) {
        return response(() -> {
            AtomicReference<ObjectCollection<?>> result = new AtomicReference<>();
            new CreateCollection(result::set, store).withCollectionName(DEFAULT_COLLECTION).run();
            return result.get();
        }, req);
    }

    @GET
    @Path("{uid}")
    public Response getCollection(@PathParam("uid") String uid, @Context Request req) {
        return response(() -> {
            AtomicReference<ObjectCollection<?>> result = new AtomicReference<>();
            new GetCollectionDetails(result::set, store).run();
            return result.get();
        }, req);
    }

//    @PUT
//    @Path("{uid}")
//    public Response setCollection(@PathParam("{uid}") String uid, @Context Request req) {
//        return response(() -> "OK", req);
//    }

    @PATCH
    @Path("{uid}")
    public Response updateCollection(@PathParam("{uid}") String uid, @Context Request req) {
        return response(() -> {
            AtomicReference<Void> result = new AtomicReference<>();
            new UpdateCollection(result::set, store).run();
            return result.get();
        }, req);
    }

    @DELETE
    @Path("{uid}")
    public Response deleteCollection(@PathParam("{uid}") String uid, @Context Request req) {
        return response(() -> {
            AtomicReference<ObjectCollection<?>> result = new AtomicReference<>();
            new DeleteCollection(result::set, store).run();
            return result.get();
        }, req);
    }
}
