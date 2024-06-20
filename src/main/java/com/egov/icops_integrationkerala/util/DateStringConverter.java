package com.egov.icops_integrationkerala.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
@Slf4j
public class DateStringConverter {

    public String convertDate(String originalDate) {
        // Define the original date format
        DateTimeFormatter originalFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        // Define the new date format
        DateTimeFormatter newFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        // Parse the original date string to a LocalDate object
        LocalDate date = LocalDate.parse(originalDate, originalFormat);
        // Format the LocalDate object to the new date string
        return date.format(newFormat);
    }
}
