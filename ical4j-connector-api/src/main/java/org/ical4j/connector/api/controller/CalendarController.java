package org.ical4j.connector.api.controller;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.Response;
import net.fortuna.ical4j.model.Calendar;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Path("/collections/{collectionId}/calendars")
public class CalendarController extends AbstractController {

    @GET
    public Response listCalendars(@PathParam("collectionId") String collectionId, @Context Request req) {
        return response(() -> {
            AtomicReference<List<Calendar>> result = new AtomicReference<>();
//            new ListMeetings(list -> result.set(list.stream().map(Calendars::wrap).collect(Collectors.toList()))).run();
            return result.get();
        }, req);
    }

    @POST
    public Response createCalendar(@PathParam("collectionId") String collectionId, @Context Request req) {
        return response(() -> {
            AtomicReference<Calendar> result = new AtomicReference<>();
//            new CreateMeeting(meeting -> result.set(Calendars.wrap(meeting))).run();
            return result.get();
        }, req);
    }

    @GET
    @Path("{uid}")
    public Response getCalendar(@PathParam("collectionId") String collectionId, @PathParam("uid") String uid,
                                @Context Request req) {
        return response(() -> "OK", req);
    }

    @PUT
    @Path("{uid}")
    public Response setCalendar(@PathParam("collectionId") String collectionId, @PathParam("{uid}") String uid,
                                @Context Request req) {
        return response(() -> "OK", req);
    }

    @PATCH
    @Path("{uid}")
    public Response updateCalendar(@PathParam("collectionId") String collectionId, @PathParam("{uid}") String uid,
                                   @Context Request req) {
        return response(() -> "OK", req);
    }

    @DELETE
    @Path("{uid}")
    public Response deleteCalendar(@PathParam("collectionId") String collectionId, @PathParam("{uid}") String uid,
                                   @Context Request req) {
        return response(() -> "OK", req);
    }
}
