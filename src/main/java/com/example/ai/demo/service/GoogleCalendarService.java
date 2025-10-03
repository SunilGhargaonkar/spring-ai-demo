package com.example.ai.demo.service;

import com.example.ai.demo.parser.DateTimeParser;
import com.example.ai.demo.parser.QueryParser;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class GoogleCalendarService {

    private final CalendarEventService calendarEventService;
    private final QueryParser queryParser;
    private final DateTimeParser dateTimeParser;

    public GoogleCalendarService(CalendarEventService calendarEventService,
                                 QueryParser queryParser,
                                 DateTimeParser dateTimeParser) {
        this.calendarEventService = calendarEventService;
        this.queryParser = queryParser;
        this.dateTimeParser = dateTimeParser;
    }

    public String bookEventFromQuery(String query) {
        try {
            final String title = queryParser.extractTitle(query);
            final LocalDateTime start = dateTimeParser.parse(query);
            final LocalDateTime end = start.plusHours(1);

            return "Event booked successfully: " + calendarEventService.bookEvent(title, start, end);
        } catch (Exception e) {
            return "Failed to book event from query: " + e.getMessage();
        }
    }
}