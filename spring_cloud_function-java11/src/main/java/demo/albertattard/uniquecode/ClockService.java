package demo.albertattard.uniquecode;

import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class ClockService {

    public String createdOn() {
        return DateTimeFormatter.ISO_DATE_TIME.format(ZonedDateTime.now());
    }
}
