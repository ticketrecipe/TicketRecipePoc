package com.ticketrecipe.api.event;

import com.ticketrecipe.common.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, String> {

    List<Event> findByIdIn(List<String> eventIds);
}

