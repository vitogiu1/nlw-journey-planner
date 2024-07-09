package com.github.vitogiu1.planner.trip;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

//Trip Repository
public interface TripRepository extends JpaRepository<Trip, UUID> {
}
