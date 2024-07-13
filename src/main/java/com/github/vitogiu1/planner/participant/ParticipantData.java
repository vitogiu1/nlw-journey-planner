package com.github.vitogiu1.planner.participant;

import java.util.UUID;

public record ParticipantData(UUID id, String name, String email, Boolean is_confirmed) {
}
