package com.example.ai.demo.service;

import com.example.ai.demo.client.GoogleCalendarClient;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Service
public class CalendarEventService {
    private static final ZoneId TIMEZONE = ZoneId.of("Europe/London");
    private final GoogleCalendarClient calendarClient;

    public CalendarEventService(GoogleCalendarClient calendarClient) {
        this.calendarClient = calendarClient;
    }

    public String bookEvent(String title, LocalDateTime start, LocalDateTime end) {
        try {
            final Calendar service = calendarClient.getCalendarService();

            final DateTime startDateTime = new DateTime(Date.from(start.atZone(TIMEZONE).toInstant()));
            final DateTime endDateTime = new DateTime(Date.from(end.atZone(TIMEZONE).toInstant()));

            // Check for conflicts
            final Events existingEvents = service.events().list("primary")
                    .setTimeMin(startDateTime)
                    .setTimeMax(endDateTime)
                    .setSingleEvents(true)
                    .execute();

            if (!existingEvents.getItems().isEmpty()) {
                return "Conflict: already booked event '" 
                        + existingEvents.getItems().getFirst().getSummary() + "'";
            }

            final Event event = new Event()
                    .setSummary(title)
                    .setStart(new EventDateTime().setDateTime(startDateTime).setTimeZone(TIMEZONE.toString()))
                    .setEnd(new EventDateTime().setDateTime(endDateTime).setTimeZone(TIMEZONE.toString()))
                    .setDescription("Created via Spring AI Demo");

            final Event createdEvent = service.events().insert("primary", event).execute();
            return createdEvent.getHtmlLink();

        } catch (Exception e) {
            return "Failed to book event: " + e.getMessage();
        }
    }
}
