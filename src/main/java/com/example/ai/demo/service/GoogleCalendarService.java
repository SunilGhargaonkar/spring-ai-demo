package com.example.ai.demo.service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.TextStyle;
import java.time.format.ResolverStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class GoogleCalendarService {
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final List<String> SCOPES = List.of(CalendarScopes.CALENDAR);
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final ZoneId TIMEZONE = ZoneId.of("Europe/London");
    @Value("${google.credentials.path:credentials.json}")
    private String credentialsFilePath;

    private Calendar getCalendarService() throws Exception {
        var HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        final GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(new FileInputStream(credentialsFilePath)));
        final GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES).setDataStoreFactory(new FileDataStoreFactory(Paths.get(TOKENS_DIRECTORY_PATH).toFile())).setAccessType("offline").build();
        final LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        final Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
        return new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).setApplicationName("Spring AI Demo").build();
    }

    private String bookEvent(String title, LocalDateTime start, LocalDateTime end) {
        try {
            final Calendar service = getCalendarService();
            final DateTime startDateTime = new DateTime(Date.from(start.atZone(TIMEZONE).toInstant()));
            final DateTime endDateTime = new DateTime(Date.from(end.atZone(TIMEZONE).toInstant()));
            // Check conflicts
            final Events existingEvents = service.events()
                    .list("primary")
                    .setTimeMin(startDateTime)
                    .setTimeMax(endDateTime)
                    .setSingleEvents(true)
                    .execute();
            if (!existingEvents.getItems().isEmpty()) {
                return "Cannot book event '" + title + "' because there is already an event: " + existingEvents
                        .getItems()
                        .get(0)
                        .getSummary();
            }
            final Event event = new Event()
                    .setSummary(title)
                    .setStart(new EventDateTime()
                                    .setDateTime(startDateTime)
                                    .setTimeZone(TIMEZONE.toString()))
                    .setEnd(new EventDateTime()
                            .setDateTime(endDateTime)
                            .setTimeZone(TIMEZONE.toString()))
                    .setDescription("Created via Spring AI Demo");
            final Event createdEvent = service.events().insert("primary", event).execute();
            return createdEvent.getHtmlLink();
        } catch (Exception e) {
            e.printStackTrace();
            return "Failed to book event: " + e.getMessage();
        }
    }

    public String bookEventFromQuery(String query) {
        try {
            final String title = query.contains("for") ? query.substring(query.indexOf("for") + 4).trim() : "Event";
            final LocalDateTime start = parseDateTime(query);
            final LocalDateTime end = start.plusHours(1);
            // default 1 hour
            return "Event booked successfully: " + bookEvent(title, start, end);
        } catch (Exception e) {
            e.printStackTrace();
            return "Failed to book event from query: " + e.getMessage();
        }
    }

    public LocalDateTime parseDateTime(String input) {
        input = input.toLowerCase().trim();
        LocalDate today = LocalDate.now(TIMEZONE);
        LocalTime defaultTime = LocalTime.of(10, 0);
        LocalDate date = null;
        LocalTime time = defaultTime;
        //Extract time "at 3 PM", "at 15:30", "at 12pm"
        Pattern timePattern = Pattern.compile("\\bat\\s*(\\d{1,2})(?::(\\d{2}))?\\s*(am|pm)?\\b");
        Matcher timeMatcher = timePattern.matcher(input);
        if (timeMatcher.find()) {
            int hour = Integer.parseInt(timeMatcher.group(1));
            int minute = timeMatcher.group(2) != null ? Integer.parseInt(timeMatcher.group(2)) : 0;
            String ampm = timeMatcher.group(3);
            if (ampm != null) {
                if (ampm.equalsIgnoreCase("pm") && hour < 12) hour += 12;
                if (ampm.equalsIgnoreCase("am") && hour == 12) hour = 0;
            }
            time = LocalTime.of(hour, minute);
        }
        //Extract explicit date like "4th Oct 2025" or "4 Oct"
        Pattern datePattern = Pattern.compile("(\\d{1,2})(st|nd|rd|th)?\\s+([a-zA-Z]+)(\\s+\\d{4})?");
        Matcher dateMatcher = datePattern.matcher(input);
        if (dateMatcher.find()) {
            String day = dateMatcher.group(1);
            String month = dateMatcher.group(3);
            String year = dateMatcher.group(4) != null ? dateMatcher.group(4).trim() : String.valueOf(today.getYear());
            try {
                DateTimeFormatter formatter = new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("d MMM uuuu").toFormatter(Locale.ENGLISH).withResolverStyle(ResolverStyle.SMART);
                date = LocalDate.parse(day + " " + month + " " + year, formatter);
            } catch (Exception ignored) {
            }
        }
        //Relative dates
        if (date == null) {
            if (input.contains("tomorrow")) date = today.plusDays(1);
            else if (input.contains("today")) date = today;
            else if (input.contains("next")) {
                for (DayOfWeek dow : DayOfWeek.values()) {
                    if (input.contains(dow.getDisplayName(TextStyle.FULL, Locale.ENGLISH).toLowerCase())) {
                        date = today.with(TemporalAdjusters.next(dow));
                        break;
                    }
                }
            }
        }
        if (date == null) date = today;
        return LocalDateTime.of(date, time);
    }
}