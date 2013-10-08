package logic;

import java.util.ArrayList;
import java.util.Calendar;

import objects.Command;
import objects.Event;
import objects.Result;
import objects.SortedEventList;
import test.InvalidUserInput;
import test.MyLogger;

/**
 * 
 * @author Jiao Jing Ping
 * @Coauthor Steve Ng
 * @version 0.2
 * @since 2011-11-1
 * 
 *        Controller is the interface between backend and GUI. Its public APIs
 *        are called by GUI to manipulate data.
 * 
 *        Controller is used mainly for the logic manipulation of Commands
 *        which determines which instructions to pass to SortedEventList class for
 *        executions.
 * 
 *        To use, call any public API accordingly. Upon completion, a Result
 *        object is return.
 * 
 */
public class Controller {

	private static SortedEventList openTasks = new SortedEventList("geekdoOpen");
	private static SortedEventList completeTasks = new SortedEventList(
			"geekdoComplete");

	// This is method must be called before running controller
	public static void initializeEventList() {
		try {
			openTasks.initialize();
			completeTasks.initialize();
		} catch (Exception e) {
			readBackUp();
		}
	}

	public static Result processText(String input) throws InvalidUserInput {
		Command c = FormatProcessor.processUserInput(input);
		Result r = c.execute(openTasks, completeTasks);
		save();
		return (r);
	}

	public static Result processCommand(Command c) {
		Result r = c.execute(openTasks, completeTasks);
		save();
		return r;
	}

	public static Result getAllTasks() {
		Result openTask = getAllOpenTasks();
		Result completeTask = getAllCompletedTask();
		Result combinedList = combineTwoList(openTask, completeTask);

		return combinedList;
	}

	public static Result getAllOpenTasks() {
		Command getAllOpenTasks = new Command();
		getAllOpenTasks.setCommandType("getOpen");
		return Controller.processCommand(getAllOpenTasks);
	}

	public static Result getAllCompletedTask() {
		Command getAllCompletedTask = new Command();
		getAllCompletedTask.setCommandType("getCompleteTasks");
		return Controller.processCommand(getAllCompletedTask);
	}

	public static void markTaskAsComplete(int taskID) {
		Command markAsComplete = new Command();
		markAsComplete.setCommandType("complete");
		markAsComplete.setId(taskID);
		Controller.processCommand(markAsComplete);
	}

	public static void markTaskAsInComplete(int taskID) {
		Command markAsInComplete = new Command();
		markAsInComplete.setCommandType("incomplete");
		markAsInComplete.setId(taskID);
		Controller.processCommand(markAsInComplete);
	}

	public static Result searchForAllTask(ArrayList<String> concatSearchKeyWord) {

		return combineTwoList(searchForOpenTask(concatSearchKeyWord),
				searchForCompletedTask(concatSearchKeyWord));
	}

	public static Result searchForOpenTask(ArrayList<String> searchKeyWord) {
		Command searchTask = new Command();
		searchTask.setCommandType("searchOpen");
		searchTask.setSearchKeyWords(searchKeyWord);
		return Controller.processCommand(searchTask);
	}

	public static Result searchForOpenTask(ArrayList<String> searchKeyWord,
			String searchType) {
		Command searchTask = new Command();
		searchTask.setCommandType("searchOpen");
		searchTask.setSearchType(searchType);
		searchTask.setSearchKeyWords(searchKeyWord);

		return Controller.processCommand(searchTask);
	}

	public static Calendar searchForOpenTaskEndDate(int taskid) {
		Result openTask = Controller.getAllOpenTasks();

		for (int i = 0; i < openTask.getData().size(); i++) {
			if (openTask.getData().get(i).getId() == taskid)
				return openTask.getData().get(i).getEndDate();
		}

		return null;
	}

	public static Calendar searchForOpenTaskStartDate(int taskid) {
		Result openTask = Controller.getAllOpenTasks();
		for (int i = 0; i < openTask.getData().size(); i++) {
			if (openTask.getData().get(i).getId() == taskid)
				return openTask.getData().get(i).getStartDate();
		}
		return null;
	}

	public static Result searchForCompletedTask(ArrayList<String> searchKeyWord) {
		Command searchTask = new Command();
		searchTask.setCommandType("searchClose");
		searchTask.setSearchKeyWords(searchKeyWord);
		return Controller.processCommand(searchTask);
	}

	public static Result searchForCompletedTask(
			ArrayList<String> searchKeyWord, String searchType) {
		Command searchTask = new Command();
		searchTask.setCommandType("searchClose");
		searchTask.setSearchType(searchType);
		searchTask.setSearchKeyWords(searchKeyWord);

		return Controller.processCommand(searchTask);
	}

	public static Result getTaskWithID(int taskID) {
		Command edit = new Command();
		edit.setCommandType("get");
		edit.setId(taskID);
		return Controller.processCommand(edit);
	}

	public static Result editTaskDescription(int taskID, String taskDescription) {
		ArrayList<Event> data = new ArrayList<Event>();
		Result r = getTaskWithID(taskID);
		if (!r.isSuccessful()) {
			return new Result(false, r.getData(), r.getError());
		}
		Event oldEvent = r.getData().get(0);
		oldEvent.setDetails(taskDescription);
		Event newEvent = new Event(oldEvent);
		data.add(oldEvent);
		data.add(newEvent);
		return new Result(true, data);
	}

	public static Result editTaskPriority(int taskID, int priority) {

		if (priority > 1)
			priority = 1;
		else if (priority < 0)
			priority = 0;

		ArrayList<Event> data = new ArrayList<Event>();
		Result r = getTaskWithID(taskID);
		if (!r.isSuccessful()) {
			return new Result(false, r.getData(), r.getError());
		}
		Event oldEvent = r.getData().get(0);
		oldEvent.setPriority(priority);
		Event newEvent = new Event(oldEvent);
		data.add(oldEvent);
		data.add(newEvent);
		return new Result(true, data);
	}

	/**
	 * Edits the end date of an Event. To set a task with no end date simply pass
	 * in 0 for day, month and year
	 * 
	 * @return a result containing the event of the edited task
	 */
	public static Result editTaskEndDate(int taskID, int day, int month,
			int year) {

		if (day < 0)
			day = 0;
		if (month < 0)
			month = 0;
		if (year < 0)
			year = 0;

		ArrayList<Event> data = new ArrayList<Event>();
		Result r = getTaskWithID(taskID);
		if (!r.isSuccessful()) {
			return new Result(false, r.getData(), r.getError());
		}

		Event oldEvent = r.getData().get(0);
		if (day == 0 && month == 0 && year == 0) {
			oldEvent.setEndDate(Event.getFarAwayDate());
		} else {
			Calendar newEndDate = Calendar.getInstance();
			newEndDate.set(year, month, day);
			oldEvent.setEndDate(newEndDate);
		}
		Event newEvent = new Event(oldEvent);
		data.add(oldEvent);
		data.add(newEvent);
		return new Result(true, data);
	}

	/**
	 * Edit the start date of an Event. To set a task with no start date simply
	 * pass in 0 for day,month and year
	 * 
	 * @return a result containing the event of the edited task
	 */
	public static Result editTaskStartDate(int taskID, int day, int month,
			int year) {

		if (day < 0)
			day = 0;
		if (month < 0)
			month = 0;
		if (year < 0)
			year = 0;

		ArrayList<Event> data = new ArrayList<Event>();
		Result r = getTaskWithID(taskID);
		if (!r.isSuccessful()) {
			return new Result(false, r.getData(), r.getError());
		}

		Event oldEvent = r.getData().get(0);
		if (day == 0 && month == 0 && year == 0) {
			oldEvent.setStartDate(Event.getFarAwayDate());
		} else {
			Calendar newEndDate = Calendar.getInstance();
			newEndDate.set(year, month, day);
			oldEvent.setStartDate(newEndDate);
		}
		Event newEvent = new Event(oldEvent);
		data.add(oldEvent);
		data.add(newEvent);
		return new Result(true, data);
	}

	public static void deleteOpenTask(int taskID) {
		Command deleteTask = new Command();
		deleteTask.setCommandType("deleteOpen");
		deleteTask.setId(taskID);
		Controller.processCommand(deleteTask);
	}

	public static void deleteCompleteTask(int taskID) {
		Command deleteTask = new Command();
		deleteTask.setCommandType("deleteComplete");
		deleteTask.setId(taskID);
		Controller.processCommand(deleteTask);
	}

	public static void deleteAllOpenTask() {
		Result openTask = getAllOpenTasks();
		for (int i = 0; i < openTask.getData().size(); i++) {
			int taskID = openTask.getData().get(i).getId();
			deleteOpenTask(taskID);
		}

	}

	public static void deleteAllCompleteTask() {
		Result completedTask = getAllCompletedTask();
		for (int i = 0; i < completedTask.getData().size(); i++) {
			int taskID = completedTask.getData().get(i).getId();
			deleteCompleteTask(taskID);
		}

	}

	public static void deleteAllOpenTaskOfTag(String selectedTab) {

		ArrayList<String> searchTab = new ArrayList<String>();
		searchTab.add(selectedTab);
		Result r = Controller.searchForOpenTask(searchTab, "tag");

		for (int i = 0; i < r.getData().size(); i++) {
			Controller.deleteOpenTask(r.getData().get(i).getId());
		}

	}

	public static void deleteAllCompletedTaskOfTag(String selectedTab) {

		ArrayList<String> searchTab = new ArrayList<String>();
		searchTab.add(selectedTab);
		Result r = Controller.searchForCompletedTask(searchTab, "tag");
		for (int i = 0; i < r.getData().size(); i++) {
			Controller.deleteCompleteTask(r.getData().get(i).getId());
		}

	}

	public static void readBackUp() {
		Command c = new Command();
		c.setCommandType("backup");
		Controller.processCommand(c);
	}

	public static void logSystemInfo(String logInfo) {
		MyLogger infoLogger = MyLogger.getInstance();
		infoLogger.logInfo(logInfo);
	}

	public static void logSystemExceptionError(String logMessage) {
		MyLogger exceptionLogger = MyLogger.getInstance();
		exceptionLogger.logException(logMessage);
	}

	public static Result addTask(int ID, String detail, String tag,
			Calendar endDate, Calendar startDate, int priority) {
		Command add = new Command();
		if (ID != 0) {
			add.setId(ID);
		}
		if (detail != null) {
			add.setDetail(detail);
		}
		if (tag != null) {
			add.setTag(tag);
		}
		if (endDate != null) {
			add.setEndDate(endDate);
		}
		if (startDate != null) {
			add.setStartDate(startDate);
		}
		add.setPriority(priority);
		add.setCommandType("add");
		return Controller.processCommand(add);
	}

	// #################################################################
	// Private API for Controller class
	// #################################################################

	private static Result combineTwoList(Result list1, Result list2) {
		for (int i = 0; i < list2.getData().size(); i++)
			list1.getData().add(list2.getData().get(i));

		return list1;
	}

	private static void save() {
		Command save = new Command();
		save.setCommandType("save");
		save.execute(openTasks, completeTasks);
	}

}