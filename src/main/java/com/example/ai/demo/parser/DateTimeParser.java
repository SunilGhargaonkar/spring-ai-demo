package com.example.ai.demo.parser;

import org.springframework.stereotype.Component;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.ResolverStyle;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class DateTimeParser {

    private static final ZoneId TIMEZONE = ZoneId.of("Europe/London");

    public LocalDateTime parse(String input) {
        input = input.toLowerCase().trim();
        final LocalDate today = LocalDate.now(TIMEZONE);
        final LocalTime defaultTime = LocalTime.of(10, 0);

        LocalDate date = null;
        LocalTime time = defaultTime;

        // Time: "at 3 PM", "at 15:30"
        final Matcher timeMatcher = Pattern.compile("\\bat\\s*(\\d{1,2})(?::(\\d{2}))?\\s*(am|pm)?\\b").matcher(input);
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

        // Date: "4th Oct 2025"
        final Matcher dateMatcher = Pattern.compile("(\\d{1,2})(st|nd|rd|th)?\\s+([a-zA-Z]+)(\\s+\\d{4})?").matcher(input);
        if (dateMatcher.find()) {
            String day = dateMatcher.group(1);
            String month = dateMatcher.group(3);
            String year = dateMatcher.group(4) != null ? dateMatcher.group(4).trim() : String.valueOf(today.getYear());

            try {
                DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                        .parseCaseInsensitive()
                        .appendPattern("d MMM uuuu")
                        .toFormatter(Locale.ENGLISH)
                        .withResolverStyle(ResolverStyle.SMART);

                date = LocalDate.parse(day + " " + month + " " + year, formatter);
            } catch (Exception ignored) {}
        }

        // Relative: "today", "tomorrow", "next Monday"
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
