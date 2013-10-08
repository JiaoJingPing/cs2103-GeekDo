package objects;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * @author Lee Zhi Xin
 * @Coauthor Jiao Jing Ping
 * @version 0.2
 * @since 2010-09-29
 * 
 *        The Event object is a data encapsulation of a task. It contains
 *        information of a task such as Event name, deadline, priority and so
 *        on.
 * 
 *        There are several highlights of its methods: (a) Event() constructor:
 *        The default constructor of Event class initialises all the fields.
 *        Some of these fields are set to a default value. (b)generateId(): ID
 *        inside Event is the primary identifier for event. The way Geekdo
 *        assigns ID to an Event is based on the time at which it is created.
 *        (c) toString(): It specifies the output format. (d)parseSingleEvent():
 *        parseSingleEvent() is the reverse function of toString().
 */
public class Event implements Comparable<Event> {
	/*************
	 * STATIC Constants
	 **************/
	private final static DateFormat DATE_FORMAT = new SimpleDateFormat(
			"dd-MM-yyyy");
	private final static int NO_PRIORITY = -1;
	private final static int INFINITE_YEAR = 2999;
	private final static int LONG_BEFORE_YEAR = 1900;
	private final static Calendar NO_ENDDATE = getFarAwayDate();
	private final static Calendar NO_STARTDATE = getBeforeDate();
	private final static String EMPTY = "Empty";

	/***********
	 * ATTRIBUTES
	 ************/
	private int Id;
	private String Detail;
	private String Tag;
	private int Priority;
	private Calendar StartDate;
	private Calendar EndDate;

	/***********
	 * CONSTRUCTORS
	 ************/
	public Event() {

		Id = IdGenerator();
		Detail = new String();
		Tag = new String();
		Priority = NO_PRIORITY;
		StartDate = NO_STARTDATE;
		EndDate = NO_ENDDATE;
	}

	public Event(String detail, String tag, int priority, Calendar startDate,
			Calendar endDate) {
		Id = IdGenerator();
		Detail = detail;
		Tag = tag.toLowerCase();
		Priority = priority;
		StartDate = startDate;
		EndDate = endDate;
	}

	public Event(Event e) {
		this.Detail = e.getDetails();
		this.EndDate = e.getEndDate();
		this.Id = e.getId();
		this.Priority = e.getPriority();
		this.StartDate = e.getStartDate();
		this.Tag = e.getTag();
	}

	/***************
	 * METHODS
	 ***************/
	public boolean equals(Object obj) {
		if (obj instanceof Event) {
			Event x = (Event) obj;
			if (Id == x.Id) {
				return true;
			} else {
				return false;
			}
		}
		return super.equals(obj);
	}

	public int compareTo(Event x) {
		if (this.equals(x)) {
			return 0;
		} else if (StartDate != NO_STARTDATE) {
			return StartDate.compareTo(x.getStartDate());
		} else if (x.StartDate != NO_STARTDATE) {
			return -1;
		} else {
			return 0;
		}
	}

	public boolean containsString(Event x) {
		if (Detail.contains(x.getDetails())) {
			return true;
		} else
			return false;
	}

	/**
	 * this method is used when exporting data to text file on local disk
	 */
	public String toString() {

		String startDateString = DATE_FORMAT.format(StartDate.getTime());
		String endDateString = DATE_FORMAT.format(EndDate.getTime());
		String priorityString = Integer.toString(Priority);
		String tagString = Tag;
		if (StartDate.equals(NO_STARTDATE)) {

			startDateString = EMPTY;
		}
		if (EndDate.equals(NO_ENDDATE)) {

			endDateString = EMPTY;
		}
		if (Priority == NO_PRIORITY) {
			priorityString = EMPTY;
		}
		if (Tag.equals("")) {
			tagString = EMPTY;
		}
		// if no change of subtasks then don't print

		String s = Id + "==" + Detail + "==" + startDateString + "=="
				+ endDateString + "==" + tagString + "==" + priorityString;

		return s;
	}

	/**
	 * This method is the inverse method of toString, that is, it creates an Event from a String
	 * 
	 * @param eventStrings
	 * @throws ParseException
	 * @throws Exception
	 */
	public static Event parseSingleEvent(String eventString)
			throws ParseException {

		String[] content = eventString.split("==");
		int id = Integer.parseInt(content[0]);
		String description = content[1].trim();
		Calendar startDate = getBeforeDate();
		Calendar endDate = getFarAwayDate();
		int priority = NO_PRIORITY;

		if (!content[2].equals(EMPTY)) {
			startDate.setTime(DATE_FORMAT.parse(content[2].trim()));
		}
		if (!content[3].equals(EMPTY)) {
			endDate.setTime(DATE_FORMAT.parse(content[3].trim()));
		}

		String tag = content[4].trim();
		if (!content[5].equals(EMPTY)) {
			priority = Integer.parseInt(content[5]);
		}
		Event e = new Event(description, tag, priority, startDate, endDate);
		e.setId(id);
		return e;

	}

	public String getDetails() {
		return Detail;
	}

	public String getTag() {
		return Tag;
	}

	public int getPriority() {
		return Priority;
	}

	public Calendar getStartDate() {
		return (Calendar) StartDate.clone();
	}

	public Calendar getEndDate() {
		return (Calendar) EndDate.clone();
	}

	public void setStartDate(Calendar d) {
		StartDate = d;
	}

	public void setEndDate(Calendar d) {
		EndDate = d;
	}

	public void setDetails(String s) {
		Detail = s;
	}

	public void setTag(String t) {
		Tag = t;
	}

	public void setId(int id) {
		this.Id = id;
	}

	public void setPriority(int b) {
		Priority = b;
	}

	public int getId() {
		return Id;
	}

	public static Calendar getFarAwayDate() {
		Calendar cal = Calendar.getInstance();
		cal.clear();
		cal.set(INFINITE_YEAR, 12, 31);// Far Away Date
		return (Calendar) cal.clone();
	}

	private static Calendar getBeforeDate() {
		Calendar cal = Calendar.getInstance();
		cal.clear();
		cal.set(LONG_BEFORE_YEAR, 1, 1);// Far Away Date
		return (Calendar) cal.clone();
	}

	/**
	 * Generates a unique ID for each Event based on the time it is created
	 * 
	 * @return
	 */
	private int IdGenerator() {

		DateFormat df1 = new SimpleDateFormat("yy-MM-dd");
		DateFormat df2 = new SimpleDateFormat("HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		String[] s1 = df1.format(cal.getTime()).split("-");
		String[] s2 = df2.format(cal.getTime()).split(":");
		int mounth = Integer.parseInt(s1[1]);
		int day = Integer.parseInt(s1[2]);
		int hour = Integer.parseInt(s2[0]);
		int min = Integer.parseInt(s2[1]);
		int sec = Integer.parseInt(s2[2]);
		int id = mounth * 100000000 + day * 1000000 + hour * 10000 + min * 100
				+ sec;
		return id;
	}
   /**
    * A set of APIs to check whether a filed is default value
    */
	public boolean isEmptyDes() {
		return this.Detail.equals("");
	}

	public boolean isEmptyTag() {
		return this.Tag.equals("");
	}

	public boolean isEmptyPriority() {
		return (this.Priority == NO_PRIORITY);
	}

	public boolean isEmptyStartDate() {
		return this.StartDate.equals(NO_STARTDATE);
	}

	public boolean isEmptyEndDate() {
		return this.EndDate.equals(NO_ENDDATE);
	}

	public static int getDefaultPriority() {
		return NO_PRIORITY;
	}
}
