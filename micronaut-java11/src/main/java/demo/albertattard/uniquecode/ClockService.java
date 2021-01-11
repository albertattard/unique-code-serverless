package demo.albertattard.uniquecode;

import javax.inject.Singleton;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Singleton
public class ClockService {

    public String createdOn() {
        return DateTimeFormatter.ISO_DATE_TIME.format(ZonedDateTime.now());
    }
}
