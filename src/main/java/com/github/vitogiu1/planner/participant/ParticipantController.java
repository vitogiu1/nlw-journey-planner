package com.github.vitogiu1.planner.participant;

import com.github.vitogiu1.planner.trip.Trip;
import com.github.vitogiu1.planner.trip.TripRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/participants")
public class ParticipantController {

    @Autowired
    private ParticipantRepository repository;

    @Autowired
    private TripRepository tripRepository;

    @PostMapping("/{id}/confirm")
    public ResponseEntity<Participant> confirmParticipant(@PathVariable UUID id, @RequestBody ParticipantRequestPayload payload){
        Optional<Participant> participant = this.repository.findById(id);
        Optional<Trip> trip = this.tripRepository.findById(participant.get().getTrip().getId()); //Get Trip ID

        if(participant.isPresent()) {
            Participant rawParticipant = participant.get();
            Trip rawTrip = trip.get();

            //Check if the Trip is confirmed
            if(rawTrip.getIsConfirmed()) {
                rawParticipant.setIsConfirmed(true);
                rawParticipant.setName(payload.name());

                this.repository.save(rawParticipant);

                return ResponseEntity.ok(rawParticipant);
            }

            return ResponseEntity.internalServerError().build();
        }

        return ResponseEntity.notFound().build();
    };
}
