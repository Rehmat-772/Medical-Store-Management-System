package smstrProject;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class DateUtil {
    public static Date convertStringToSqlDate(String dateStr) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        java.util.Date parsed = format.parse(dateStr);
        return new java.sql.Date(parsed.getTime());
    }
}
