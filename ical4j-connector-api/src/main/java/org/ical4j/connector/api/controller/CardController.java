package org.ical4j.connector.api.controller;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.Response;
import net.fortuna.ical4j.vcard.VCard;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Path("/collections/{collectionId}/cards")
public class CardController extends AbstractController {

    @GET
    public Response listCards(@PathParam("collectionId") String collectionId, @Context Request req) {
        return response(() -> {
            AtomicReference<List<VCard>> result = new AtomicReference<>();
//            new ListMeetings(list -> result.set(list.stream().map(Calendars::wrap).collect(Collectors.toList()))).run();
            return result.get();
        }, req);
    }

    @POST
    public Response createCard(@PathParam("collectionId") String collectionId, @Context Request req) {
        return response(() -> {
            AtomicReference<VCard> result = new AtomicReference<>();
//            new CreateMeeting(meeting -> result.set(Calendars.wrap(meeting))).run();
            return result.get();
        }, req);
    }

    @GET
    @Path("{uid}")
    public Response getCard(@PathParam("collectionId") String collectionId, @PathParam("uid") String uid,
                                @Context Request req) {
        return response(() -> "OK", req);
    }

    @PUT
    @Path("{uid}")
    public Response setCard(@PathParam("collectionId") String collectionId, @PathParam("{uid}") String uid,
                                @Context Request req) {
        return response(() -> "OK", req);
    }

    @PATCH
    @Path("{uid}")
    public Response updateCard(@PathParam("collectionId") String collectionId, @PathParam("{uid}") String uid,
                                   @Context Request req) {
        return response(() -> "OK", req);
    }

    @DELETE
    @Path("{uid}")
    public Response deleteCard(@PathParam("collectionId") String collectionId, @PathParam("{uid}") String uid,
                                   @Context Request req) {
        return response(() -> "OK", req);
    }
}
