package objects;

/**
 * @author JJP
 * Command is the object abstraction of an actual instruction. 
 * It handles the actual execution of an instruction and
 * interacts with SortedEventList.
 * 
 * To use it, one needs to construct a Command and fill the relevant fields he wants.
 * Upon construction, c.execute() is called to let Command execute the instruction
 * 
 * Notice that some fields are set to a default value in order to identify changes in fields.
 */

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class Command {
	private final static DateFormat DATE_FORMAT = new SimpleDateFormat(
			"dd-MM-yyyy");
	private final static String EMPTY = "no_change";
	private static final int NO_CHANGE_FOR_PRIORITY = -1000;
	private static final String NO_CHANGE_FOR_DETAIL = "!!NO_CHANGE_FOR_DES!!";
	private static final String NO_CHANGE_FOR_TAG = "!!NO_CHANGE_FOR_TAG!!";
	private static final Calendar NO_CHANGE_FOR_ENDDATE = getNoChangeEndDate();
	private static final Calendar NO_CHANGE_FOR_STARTDATE = getNoChangeStartDate();
	private static final String DEFAULT_SEARCHTYPE = "description";
	private static final String INVALID_COMPLETED_TASK_ID_MSG = "Invalid completed task ID";
	private static final String INVALID_INCOMPLETE_TASK_ID_MSG = "Invalid incomplete task ID";
	private static final String IO_EXCEPTION_MSG = "Error in exporting tasks";

	// Command Type
	private String commandType;

	// task related
	@SuppressWarnings("unused")
	private Event newEvent;
	private int Id;
	private String detail;
	private String tag;
	private int priority;
	private Calendar startDate;
	private Calendar endDate;
	// keyword
	private ArrayList<String> sortKeyWords;
	private ArrayList<String> searchKeyWords;
	private String searchType;

	/**
	 * 
	 * @param objects
	 */

	public Command() {
		// set all field to default values
		this.commandType = new String();

		this.detail = new String();
		this.tag = NO_CHANGE_FOR_TAG;
		this.priority = NO_CHANGE_FOR_PRIORITY;
		this.startDate = NO_CHANGE_FOR_STARTDATE;
		this.endDate = NO_CHANGE_FOR_ENDDATE;
		this.searchKeyWords = new ArrayList<String>();
		this.sortKeyWords = new ArrayList<String>();
		this.searchType = DEFAULT_SEARCHTYPE;

	}

	// ======================Command Execution===========================
	public Result execute(SortedEventList openTasks,
			SortedEventList completeTasks) {
		return executeCommand(this, openTasks, completeTasks);
	}

	private Result executeCommand(Command command, SortedEventList openTasks,
			SortedEventList completeTasks) {

		COMMAND_TYPE commandType = COMMAND_TYPE.determineCommandType(command);
		return execute(commandType, command, openTasks, completeTasks);
	}

	private Result execute(COMMAND_TYPE commandType, Command command,
			SortedEventList openTasks, SortedEventList completeTasks) {
		// TODO Auto-generated method stub
		Result r = new Result(false, null, null, null);
		switch (commandType) {
		case ADD:
			r = openTasks.addEvent(command.getEventFromCommand());
			r.setCommandType("add");
			return r;
		case GET:
			r = openTasks.getEventWithId(command.getId());
			r.setCommandType("get");
			return r;
		case SET:
			r = openTasks.editEventWithId(command.getEventFromCommand(),
					command.getId());
			r.setCommandType("set");
			return r;
		case DELETEOPEN:
			r = openTasks.deleteEventWithId(command.getId());
			r.setCommandType("deleteOpen");
			return r;
		case DELETECOMPLETE:
			r = completeTasks.deleteEventWithId(command.getId());
			r.setCommandType("deleteComplete");
			return r;
		case GETOPEN:
			r = openTasks.getArrayList();
			r.setCommandType("getOpen");
			return r;
		case GETCOMPLETE:
			r = completeTasks.getArrayList();
			r.setCommandType("getComplete");
			return r;
		case SORT:
			r = openTasks.sortBy(command.getSortKeyWords());
			r.setCommandType("sort");
			return r;
		case SEARCHOPEN:
			r = openTasks.searchBy(command.getSearchKeyWords(),
					command.getSearchType());
			r.setCommandType("SEARCHOPEN");
			return r;
		case SEARCHCOMPLETE:
			r = completeTasks.searchBy(command.getSearchKeyWords(),
					command.getSearchType());
			r.setCommandType("searchComplete");
			return r;
		case COMPLETE:
			r.setCommandType("COMPLETE");
			Result r1 = completeTasks.addEvent(openTasks
					.getEventWithId(command.getId()).getData().get(0));
			Result r2 = openTasks.deleteEventWithId(command.getId());
			if (r1.isSuccessful() && r2.isSuccessful()) {
				ArrayList<Event> data = new ArrayList<Event>();
				data.add(r1.getData().get(0));
				r.setData(r1.getData());
				r.setSuccessful(true);
				return r;
			} else {
				ArrayList<Event> data = new ArrayList<Event>();
				data.add(r1.getData().get(0));
				r.setSuccessful(false);
				r.setData(data);
				r.setError(INVALID_COMPLETED_TASK_ID_MSG);
				return r;
			}
		case INCOMPLETE:
			Result r3 = openTasks.addEvent(completeTasks
					.getEventWithId(command.getId()).getData().get(0));
			Result r4 = completeTasks.deleteEventWithId(command.getId());

			r.setCommandType("INCOMPLETE");
			if (r3.isSuccessful() && r4.isSuccessful()) {
				ArrayList<Event> data = new ArrayList<Event>();
				data.add(r3.getData().get(0));
				r.setData(r3.getData());
				r.setSuccessful(true);

				return r;
			} else {
				ArrayList<Event> data = new ArrayList<Event>();
				data.add(r3.getData().get(0));
				r.setSuccessful(false);
				r.setData(data);
				r.setError(INVALID_INCOMPLETE_TASK_ID_MSG);
				return r;
			}
		case SAVE:
			Boolean re1 = openTasks.write();
			Boolean re2 = completeTasks.write();
			r.setCommandType("save");
			if (re1 && re2) {
				return r;
			} else {
				r.setSuccessful(false);
				r.setError(IO_EXCEPTION_MSG);
				return r;
			}
		case BACKUP:
			r.setCommandType("backup");
			if (completeTasks.readBackUp().isSuccessful()
					&& openTasks.readBackUp().isSuccessful()) {
				r.setSuccessful(true);
				return r;
			} else {
				r.setSuccessful(false);
				r.setError(IO_EXCEPTION_MSG);
				return r;
			}
		default:
			throw new Error("Unrecognized command type");
		}
	}

	// ============================================
	public String toString() {
		String startDateString = DATE_FORMAT.format(startDate.getTime());
		String endDateString = DATE_FORMAT.format(endDate.getTime());
		String priorityString = Integer.toString(priority);
		String tagString = tag;

		if (startDate.equals(NO_CHANGE_FOR_STARTDATE)) {
			startDateString = EMPTY;
		}
		if (endDate.equals(NO_CHANGE_FOR_ENDDATE)) {
			endDateString = EMPTY;
		}
		if (priority == NO_CHANGE_FOR_PRIORITY) {
			priorityString = EMPTY;
		}
		if (tag.equals(NO_CHANGE_FOR_TAG)) {
			tagString = EMPTY;
		}
		// if no change of subtasks then don't print
		String output = "CommandType: " + this.commandType + "\r\n" + Id + "=="
				+ startDateString + "==" + endDateString + "==" + tagString
				+ "==" + priorityString;

		return output;
	}

	public Event getEventFromCommand() {
		Event e = new Event();
		if (Id != 0) {
			e.setId(Id);
		}
		if (!detail.equals(NO_CHANGE_FOR_DETAIL)) {
			e.setDetails(detail);
		}
		if (!startDate.equals(NO_CHANGE_FOR_STARTDATE)) {
			e.setStartDate(startDate);
		}
		if (!endDate.equals(NO_CHANGE_FOR_ENDDATE)) {
			e.setEndDate(endDate);
		}
		if (priority != NO_CHANGE_FOR_PRIORITY) {
			e.setPriority(priority);
		}
		if (!tag.equals(NO_CHANGE_FOR_TAG)) {
			e.setTag(tag);
		}
		return e;
	}

	public void setNewEvent(Event newEvent) {
		// TODO Auto-generated method stub
		assert (newEvent != null);
		this.newEvent = newEvent;
	}

	public void setCommandType(String commandType) {
		assert (commandType != null);
		this.commandType = commandType;
	}

	public void setDetail(String detail) {
		assert (detail != null);
		this.detail = detail;
	}

	public void setTag(String tag) {
		assert (tag != null);
		this.tag = tag;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public void setStartDate(Calendar startDate) {
		assert (startDate != null);
		this.startDate = startDate;
	}

	public void setEndDate(Calendar endDate) {
		assert (endDate != null);
		this.endDate = endDate;
	}

	public void setSortKeyWords(ArrayList<String> sortKeyWords) {
		this.sortKeyWords = sortKeyWords;
	}

	public void setSearchKeyWords(ArrayList<String> searchKeyWords) {
		this.searchKeyWords = searchKeyWords;
	}

	// =============================================
	public String getCommandType() {
		return commandType;
	}

	public String getDetail() {
		return detail;
	}

	public String getTag() {
		return tag;
	}

	public int getPriority() {
		return priority;
	}

	public Calendar getStartDate() {
		return startDate;
	}

	public Calendar getEndDate() {
		return endDate;
	}

	public ArrayList<String> getSortKeyWords() {
		return sortKeyWords;
	}

	public ArrayList<String> getSearchKeyWords() {
		return searchKeyWords;
	}

	public int getId() {
		return Id;
	}

	public void setId(int Id) {
		this.Id = Id;
	}

	public String getSearchType() {
		return searchType;
	}

	public void setSearchType(String searchType) {
		this.searchType = searchType;
	}

	public static boolean isSetDetail(String details) {
		return NO_CHANGE_FOR_DETAIL.equals(details);
	}

	public static boolean isSetPriority(int priority) {
		return (priority == NO_CHANGE_FOR_PRIORITY);
	}

	public static boolean isSetTag(String tag) {
		return NO_CHANGE_FOR_TAG.equals(tag);
	}

	public static boolean isSetStartDate(Calendar startDate) {
		return NO_CHANGE_FOR_STARTDATE.equals(startDate);
	}

	public static boolean isSetEndDate(Calendar endDate) {
		return NO_CHANGE_FOR_ENDDATE.equals(endDate);
	}

	private static Calendar getNoChangeEndDate() {
		Calendar cal = Calendar.getInstance();
		cal.clear();
		cal.set(3999, 12, 31);// Far Away Date
		return (Calendar) cal.clone();
	}

	private static Calendar getNoChangeStartDate() {
		Calendar cal = Calendar.getInstance();
		cal.clear();
		cal.set(1800, 1, 1);// DummyDate
		return (Calendar) cal.clone();
	}

}
