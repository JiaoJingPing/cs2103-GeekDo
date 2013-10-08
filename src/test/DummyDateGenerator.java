package test;
/**
 * This is the class to generate DummyDate in 2011 
 * in the format of MM-DD-YYYY
 * @author JJP
 */
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class DummyDateGenerator {
	private static Random r = new Random();
	private static final SimpleDateFormat bartDateFormat = new SimpleDateFormat(
			"MM-dd-yyyy");
	final private static int CURRENT_YEAR = 2011;
	final private static String TAIL_DATE = "12-31-2011";
	final private static String FRONT_DATE = "1-1-2011";
	private final static int[] LEAP_DAYS = { 31, 29, 31, 30, 31, 30, 31, 31,
			30, 31, 30, 31 };
	private final static int[] NONE_LEAP_DAYS = { 31, 28, 31, 30, 31, 30, 31,
			31, 30, 31, 30, 31 };

	public static Calendar getDummyDateAfterDate(Calendar date) {
		if (date.equals(getTailDate())) {
			throw new Error("No more date after " + date);
		}
		Calendar dummyDate = getDummyDate();
		while (dummyDate.compareTo(date) < 0) {
			dummyDate = getDummyDate();
		}
		return dummyDate;
	}

	public static Calendar getDummyDateBeforeDate(Calendar date) {
		if (date.equals(getFrontDate())) {
			throw new Error("No more date before " + date);
		}
		Calendar dummyDate = getDummyDate();
		while (dummyDate.compareTo(date) > 0) {
			dummyDate = getDummyDate();
		}
		return dummyDate;
	}

	public static Calendar getDummyDate() {
		int dummyYear = CURRENT_YEAR;
		int dummyMonth = getDummyMonth();
		int dummyDay = getDummyDay(dummyYear, dummyMonth);
		Date dummyDate = formatDate(2011, dummyMonth, dummyDay);
		Calendar cal = Calendar.getInstance();
		cal.setTime(dummyDate);
		return cal;
	}

	private static Date formatDate(int year, int month, int day) {
		Date date = null;
		String dateStringToParse = Integer.toString(month) + "-"
				+ Integer.toString(day) + "-" + Integer.toString(year);
		try {
			date = bartDateFormat.parse(dateStringToParse);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return date;

	}

	private static int getDummyMonth() {
		int dummyMonth;
		dummyMonth = r.nextInt(13);
		while (dummyMonth <= 0) {
			dummyMonth = r.nextInt(13);
		}
		return dummyMonth;
	}

	private static int getDummyDay(int year, int month) {
		if (year < 1 || month < 1 || month > 12) {
			throw new Error("Invalid Input");
		}

		int dummyDay;

		if (isLeapYear(year)) {
			int maxDay = LEAP_DAYS[month - 1];
			dummyDay = r.nextInt(maxDay + 1);
			while (dummyDay <= 0) {
				dummyDay = r.nextInt(maxDay + 1);
			}
			return dummyDay;
		} else {
			int maxDay = NONE_LEAP_DAYS[month - 1];
			dummyDay = r.nextInt(maxDay + 1);
			while (dummyDay <= 0) {
				dummyDay = r.nextInt(maxDay + 1);
			}
			return dummyDay;
		}
	}

	private static boolean isLeapYear(int year) {
		return ((year % 4) == 0);
	}

	private static Date getTailDate() {
		Date date = null;
		try {
			date = bartDateFormat.parse(TAIL_DATE);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return date;
	}

	private static Date getFrontDate() {
		Date date = null;
		try {
			date = bartDateFormat.parse(FRONT_DATE);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return date;
	}

	public static void main(String args[]) {
		int testNumber = 50;
		for (int i = 0; i < testNumber; i++) {
			Calendar dummyDate = getDummyDate();
			System.out.println(dummyDate.getTime());
		}
		try {
			Date date1=bartDateFormat.parse(FRONT_DATE);
			Date date2=bartDateFormat.parse(FRONT_DATE);
			Calendar c1=Calendar.getInstance();
			Calendar c2=Calendar.getInstance();
			c1.setTime(date1);
			c2.setTime(date2);
			System.out.println(c1.equals(c2));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
