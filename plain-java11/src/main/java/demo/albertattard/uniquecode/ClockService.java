package demo.albertattard.uniquecode;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class ClockService {

    public String createdOn() {
        return DateTimeFormatter.ISO_DATE_TIME.format(ZonedDateTime.now());
    }
}
