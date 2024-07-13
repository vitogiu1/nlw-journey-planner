package com.github.vitogiu1.planner.trip;

import java.time.LocalDateTime;
import java.util.UUID;

public record TripData(UUID id, String destination, LocalDateTime starts_at, LocalDateTime ends_at, Boolean is_confirmed, String owner_name, String owner_email) {
}
