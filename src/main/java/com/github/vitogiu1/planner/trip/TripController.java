package com.github.vitogiu1.planner.trip;

import com.github.vitogiu1.planner.activity.ActivityData;
import com.github.vitogiu1.planner.activity.ActivityRequestPayload;
import com.github.vitogiu1.planner.activity.ActivityResponse;
import com.github.vitogiu1.planner.activity.ActivityService;
import com.github.vitogiu1.planner.link.LinkData;
import com.github.vitogiu1.planner.link.LinkRequestPayload;
import com.github.vitogiu1.planner.link.LinkResponse;
import com.github.vitogiu1.planner.link.LinkService;
import com.github.vitogiu1.planner.participant.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/trips")
public class TripController {

    @Autowired
    private ParticipantService participantService;

    @Autowired
    private TripRepository repository;

    @Autowired
    private ActivityService activityService;

    @Autowired
    private LinkService linkService;


    //Trips

    // POST Method: Create Trip
    @PostMapping
    public ResponseEntity<TripResponse> createTrip(@RequestBody TripRequestPayload payload) {
        Trip newTrip = new Trip(payload);

        this.repository.save(newTrip); //Save on H2
        this.participantService.registerParticipantsToEvent(payload.emails_to_invite(), newTrip);

        return ResponseEntity.ok(new TripResponse(newTrip.getId()));
    };

    // GET Method: Get Trip Info
    @GetMapping("/{id}")
    public ResponseEntity<Trip> getTripDetails(@PathVariable UUID id){
        Optional<Trip> trip = this.repository.findById(id);

        return trip.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    };

    // PUT Method: Actualize Trip Info
    @PutMapping("/{id}")
    public ResponseEntity<Trip> updateTrip(@PathVariable UUID id, @RequestBody TripRequestPayload payload){
        Optional<Trip> trip = this.repository.findById(id);

        if(trip.isPresent()) {
            Trip rawTrip = trip.get();
            rawTrip.setStartsAt(LocalDateTime.parse(payload.starts_at(), DateTimeFormatter.ISO_DATE_TIME));
            rawTrip.setEndsAt(LocalDateTime.parse(payload.ends_at(), DateTimeFormatter.ISO_DATE_TIME));
            rawTrip.setDestination(payload.destination());

            this.repository.save(rawTrip); //Save on H2

            return ResponseEntity.ok(rawTrip);
        }

        return ResponseEntity.notFound().build();
    };

    // GET Method: Confirm Trip
    @GetMapping("/{id}/confirm")
    public ResponseEntity<Trip> confirmTrip(@PathVariable UUID id){
        Optional<Trip> trip = this.repository.findById(id);

        if(trip.isPresent()) {
            Trip rawTrip = trip.get();
            rawTrip.setIsConfirmed(true);

            this.repository.save(rawTrip); //save on H2
            this.participantService.triggerConfirmationEmailToParticipants(id); //trigger confirmation for other participants

            return ResponseEntity.ok(rawTrip);
        }

        return ResponseEntity.notFound().build();
    };


    //Participants


    // POST Method: Invite Participants
    @PostMapping("/{id}/invite")
    public ResponseEntity<ParticipantCreateResponse> inviteParticipant(@PathVariable UUID id, @RequestBody ParticipantRequestPayload payload) {
        Optional<Trip> trip = this.repository.findById(id);

        if(trip.isPresent()) {
            Trip rawTrip = trip.get();

//            List<String> participantToInvite = new ArrayList<>();
//            participantToInvite.add(payload.email());

            ParticipantCreateResponse participantResponse = this.participantService.registerParticipantToEvent(payload.email(), rawTrip);

            if(rawTrip.getIsConfirmed()) this.participantService.triggerConfirmationEmailToParticipant(payload.email());

            return ResponseEntity.ok(participantResponse);
        }

        return ResponseEntity.notFound().build();
    };

    //GET Method: Participants Information
    @GetMapping("/{id}/participants")
    public ResponseEntity<List<ParticipantData>> listParticipants(@PathVariable UUID id){
        List<ParticipantData> participantList = this.participantService.getAllParticipantsFromEvent(id);

        return ResponseEntity.ok(participantList);
    };


    //Activities


    @PostMapping("/{id}/activities/create")
    public ResponseEntity<ActivityResponse> createActivity(@PathVariable UUID id, @RequestBody ActivityRequestPayload payload) {
        Optional<Trip> trip = this.repository.findById(id);

        if(trip.isPresent()) {
            Trip rawTrip = trip.get();

            ActivityResponse activityResponse = this.activityService.saveActivity(payload, rawTrip);

            return ResponseEntity.ok(activityResponse);
        }

        return ResponseEntity.notFound().build();
    };

    @GetMapping("/{id}/activities")
    public ResponseEntity<List<ActivityData>> listActivities(@PathVariable UUID id){
        List<ActivityData> activitiesList = this.activityService.getAllActivitiesFromEvent(id);

        return ResponseEntity.ok(activitiesList);
    };


    //Links


    @PostMapping("/{id}/links/create")
    public ResponseEntity<LinkResponse> createLink(@PathVariable UUID id, @RequestBody LinkRequestPayload payload) {
        Optional<Trip> trip = this.repository.findById(id);

        if(trip.isPresent()) {
            Trip rawTrip = trip.get();

            LinkResponse linkResponse = this.linkService.saveLink(payload, rawTrip);

            return ResponseEntity.ok(linkResponse);
        }

        return ResponseEntity.notFound().build();
    };

    @GetMapping("/{id}/links")
    public ResponseEntity<List<LinkData>> listLinks(@PathVariable UUID id){
        List<LinkData> linksList = this.linkService.getAllLinksFromEvent(id);

        return ResponseEntity.ok(linksList);
    };
}
