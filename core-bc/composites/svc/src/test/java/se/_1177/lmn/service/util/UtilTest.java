package se._1177.lmn.service.util;

import org.junit.Test;

import javax.xml.datatype.XMLGregorianCalendar;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Patrik Björk
 */
public class UtilTest {

    @Test
    public void isValidEmailAddress() throws Exception {

        List<String> validEmails = new ArrayList<>();
        validEmails.add("user@domain.com");
        validEmails.add("user@domain.co.in");
        validEmails.add("user.name@domain.com");
        validEmails.add("user_name@domain.com");
        validEmails.add("username@yahoo.corporate.in");

        List<String> invalidEmails = new ArrayList<>();

        invalidEmails.add(".username@yahoo.com");
        invalidEmails.add("username@yahoo.com.");
        invalidEmails.add("username@yahoo..com");
        invalidEmails.add("username@yahoo.c");
        invalidEmails.add("username@yahoo.corporate");

        for (String validEmail : validEmails) {
            assertTrue(Util.isValidEmailAddress(validEmail));
        }

        for (String invalidEmail : invalidEmails) {
            assertFalse(Util.isValidEmailAddress(invalidEmail));
        }
    }

    @Test
    public void isBeforeToday() {
        Calendar calendar = Calendar.getInstance();

        calendar.add(Calendar.DATE, -1);
        XMLGregorianCalendar yesterday = Util.toXmlGregorianCalendar((GregorianCalendar) calendar);
        assertTrue(Util.isBeforeToday(yesterday));

        calendar.add(Calendar.DATE, 1);
        XMLGregorianCalendar today = Util.toXmlGregorianCalendar((GregorianCalendar) calendar);
        assertFalse(Util.isBeforeToday(today));
    }

    @Test
    public void isOlderThanAYear() throws Exception {

        // Today
        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        assertFalse(Util.isOlderThanAYear(Util.toXmlGregorianCalendar(gregorianCalendar)));

        // Exactly a year ago
        gregorianCalendar = new GregorianCalendar();
        gregorianCalendar.add(Calendar.YEAR, -1);
        assertFalse(Util.isOlderThanAYear(Util.toXmlGregorianCalendar(gregorianCalendar)));

        // A year ago minus one millisecond. The millisecond shouldn't matter since it's still the same day (except if
        // this test is run the first millisecond of the day)
        gregorianCalendar = new GregorianCalendar();
        gregorianCalendar.add(Calendar.YEAR, -1);
        gregorianCalendar.add(Calendar.MILLISECOND, -1);
        assertFalse(Util.isOlderThanAYear(Util.toXmlGregorianCalendar(gregorianCalendar)));

        // A year ago
        gregorianCalendar = new GregorianCalendar();
        gregorianCalendar.add(Calendar.YEAR, -1);
        gregorianCalendar.add(Calendar.DATE, -1);
        assertTrue(Util.isOlderThanAYear(Util.toXmlGregorianCalendar(gregorianCalendar)));
    }

    @Test
    public void isAfterToday() throws Exception {

        // Today
        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        assertFalse(Util.isAfterToday(Util.toXmlGregorianCalendar(gregorianCalendar)));

        // Tomorrow
        gregorianCalendar = new GregorianCalendar();
        gregorianCalendar.add(Calendar.DATE, 1);
        assertTrue(Util.isAfterToday(Util.toXmlGregorianCalendar(gregorianCalendar)));

        // The last millisecond of today, but still today.
        gregorianCalendar = new GregorianCalendar();
        gregorianCalendar.set(Calendar.HOUR_OF_DAY, 23);
        gregorianCalendar.set(Calendar.MINUTE, 59);
        gregorianCalendar.set(Calendar.SECOND, 59);
        gregorianCalendar.set(Calendar.MILLISECOND, 999);
        assertFalse(Util.isAfterToday(Util.toXmlGregorianCalendar(gregorianCalendar)));

        // Add just one millisecond to previous section, making it tomorrow.
        gregorianCalendar = new GregorianCalendar();
        gregorianCalendar.set(Calendar.HOUR_OF_DAY, 23);
        gregorianCalendar.set(Calendar.MINUTE, 59);
        gregorianCalendar.set(Calendar.SECOND, 59);
        gregorianCalendar.set(Calendar.MILLISECOND, 999);
        gregorianCalendar.add(Calendar.MILLISECOND, 1); // The important detail.
        assertTrue(Util.isAfterToday(Util.toXmlGregorianCalendar(gregorianCalendar)));
    }
}