package objects;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;

import logic.Controller;

/**
 * @author Jiao Jing Ping <elleryjiao@gmail.com>
 * @Coauthor Lee Zhi Xin
 * @version 0.2
 * @since 2011-11-1
 * 
 *        <code>SortedEventList<code> is a logic representation of data.
 * It takes the responsibility of maintain a data and retrieving and inserting 
 * as a normal data structure, except it returns a <code>Result<code> object upon 
 * completion of a operation.
 * 
 * It also takes charge of reading and writing data to a local data file, both a 
 * normal "txt" file and a "bk" file. The .txt file is the one read when Geekdo starts up,
 *  and the .bk file stores backup data for when the .txt file is corrupted. 
 * 
 * Besides the normal constructor, initialise() must be called before further 
 * operation.
 * 
 */
public class SortedEventList {
	private static final SORT_TYPE DEFAULT_SORT_TYPE = SORT_TYPE.END_DATE;
	private static final boolean FAIL = false;
	private static final boolean SUCCESSFUL = true;
	private static final String PRINT_MSG = "#####################DO NOT Modify this file###################\r\n"
			+ "###EVENT ID__DESCRIPTION__START DATE__ENDDATE__TAG__PRIORITY###\r\n"
			+ "###############################################################\r\n";
	// Error messages;
	private static final String INVALID_TASK_ID_MSG = "No task with given ID";
	private static final String DUPLICATE_ID_WHEN_ADD_MSG = "Duplicate ID detected when adding";

	private ArrayList<Event> eventList;
	private String fileName;
	private String backup_FileName;
	private boolean isReadFail = false;

	/**
	 * Constructor of SortedEventList, requires a fileName. It reads data from
	 * text file and initialises the List.
	 * 
	 * @param fileName
	 * @throws Exception
	 */
	public SortedEventList(String fileName) {
		eventList = new ArrayList<Event>();
		this.fileName = getWorkingDirectory() + fileName + ".txt";
		this.backup_FileName = fileName + ".bk";
	}

	/**
	 * This Constructor for test purposes
	 * 
	 * @param dummyEvents
	 */
	public SortedEventList(ArrayList<Event> dummyEvents) {
		//For testing purposes
		this.eventList = dummyEvents;
	}

	private String getWorkingDirectory() {
		File directory = new File(". ");
		String s = "";
		try {
			s = directory.getCanonicalPath();
		} catch (IOException e) {
			Controller.logSystemExceptionError(e.toString());
		}
		return s;

	}

	public Result initialize() throws Exception {
		// Read and write data from fileName with the directory
		if (fileName == null || backup_FileName == null) {
			throw new Error("Null filename in SortedEventList");
		}
		try {
			eventList = readData(fileName);
			// sort pre-loaded data by default sort type;
			this.sortBySortType(eventList, DEFAULT_SORT_TYPE);
		} catch (Exception e) {
			// if read data fails
			Controller
					.logSystemExceptionError("SortedEventList construction has failed");
			throw e;
		}
		return new Result(SUCCESSFUL, null, "Initialisation successful");
	}

	public boolean isReadFail() {
		return isReadFail;
	}

	public int size() {
		return eventList.size();
	}

	// =====================================================
	// = API TO CommandHanlder =
	// =====================================================
	/**
	 * Retrieves an event of a specified ID
	 * 
	 * @param id
	 * @return
	 */
	public Result getEventWithId(int id) {
		// TODO search through the list, return event with given id
		ArrayList<Event> data = new ArrayList<Event>();
		for (int i = 0; i < eventList.size(); i++) {
			Event e = eventList.get(i);
			if (e.getId() == id) {
				data.add(e);
				return new Result(SUCCESSFUL, data);
			}
		}
		return new Result(FAIL, null, null, INVALID_TASK_ID_MSG);
	}

	/**
	 * Reads data from a backup file when the read data from the .txt file fails
	 * 
	 * @return
	 */
	public Result readBackUp() {
		try {
			eventList = readData(backup_FileName);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			return new Result(false, null, null, e.toString());
		}
		return new Result(true, null, "Backup has been read successfully");
	}

	/**
	 * Add a event to the data list. It will Fail if a event with same Id is
	 * already exists. addEvent
	 * 
	 * @param event
	 * @return Result
	 */
	public Result addEvent(Event eventToAdd) {
		if (eventToAdd == null) {
			return new Result(FAIL, null, DUPLICATE_ID_WHEN_ADD_MSG);
		}
		ArrayList<Event> addedEvent = new ArrayList<Event>();
		addedEvent.add(eventToAdd);
		add(eventToAdd);
		return new Result(SUCCESSFUL, addedEvent);
	}

	private boolean add(Event event) {
		if (event == null) {
			return false;
		}
		eventList.add(event);
		return true;
	}

	/**
	 * editEventWithId
	 * 
	 * @param event
	 * @return Result
	 */
	public Result editEventWithId(Event eventToEdit, int id) {
		// TODO Auto-generated method stub
		if (eventToEdit == null) {
			throw new Error("Error: no task to edit");
		}
		Result result = getEventWithId(id);
		if (!result.isSuccessful()) {
			ArrayList<Event> data = new ArrayList<Event>();
			data.add(eventToEdit);
			return new Result(FAIL, data, INVALID_TASK_ID_MSG);
		}
		Event targetEvent = result.getData().get(0);
		ArrayList<Event> editedEvent = new ArrayList<Event>();
		Event beforeEditedEvent = new Event(targetEvent);
		editedEvent.add(beforeEditedEvent);

		// Check which filed to be edited
		if (!Command.isSetDetail(eventToEdit.getDetails())) {
			targetEvent.setDetails(eventToEdit.getDetails());
		}
		if (!Command.isSetPriority(eventToEdit.getPriority())) {
			targetEvent.setPriority(eventToEdit.getPriority());
		}
		if (!Command.isSetTag(eventToEdit.getTag())) {
			targetEvent.setTag(eventToEdit.getTag());
		}
		if (!Command.isSetStartDate(eventToEdit.getStartDate())) {
			targetEvent.setStartDate(eventToEdit.getStartDate());
		}
		if (!Command.isSetEndDate(eventToEdit.getEndDate())) {
			targetEvent.setEndDate(eventToEdit.getEndDate());
		}
		editedEvent.add(targetEvent);
		// data contains tasks before and after editing
		return new Result(SUCCESSFUL, editedEvent);
	}

	/**
	 * deleteEventWithId
	 * 
	 * @param id
	 * @return
	 */
	public Result deleteEventWithId(int id) {
		// TODO Auto-generated method stub
		Result result = getEventWithId(id);
		if (!result.isSuccessful()) {
			ArrayList<Event> container = new ArrayList<Event>();
			Event e = new Event();
			e.setId(id);
			container.add(e);
			return new Result(FAIL, container, INVALID_TASK_ID_MSG);
		}
		Event eventToDelete = result.getData().get(0);
		ArrayList<Event> deletedEvent = new ArrayList<Event>();
		deletedEvent.add(eventToDelete);
		eventList.remove(eventToDelete);
		return new Result(SUCCESSFUL, deletedEvent);
	}

	/**
	 * Return a copy of the entire list of event getArrayList
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Result getArrayList() {
		ArrayList<Event> r = (ArrayList<Event>) eventList.clone();
		sortBySortType(r, DEFAULT_SORT_TYPE);
		return new Result(SUCCESSFUL, r);
	}

	/**
	 * Implementation of sorting of data. Using MergeSort and return a sorted
	 * copy of data
	 * 
	 * @param sortKeyWords
	 * @return
	 */
	public Result sortBy(ArrayList<String> sortKeyWords) {
		ArrayList<SORT_TYPE> sortKeyTypes = getSortTypeList(sortKeyWords);
		if (sortKeyTypes == null) {
			return new Result(FAIL, null, "Unknown Sort Types");
		}
		@SuppressWarnings("unchecked")
		// clone a copy of eventList instead of directly modify the content;
		ArrayList<Event> listAfterSort = sort(
				(ArrayList<Event>) eventList.clone(), sortKeyTypes);
		return new Result(SUCCESSFUL, listAfterSort);
	}

	private ArrayList<SORT_TYPE> getSortTypeList(ArrayList<String> sortkeyWords) {
		// get sortType from list of sortType
		ArrayList<SORT_TYPE> sortKeyTypes = new ArrayList<SORT_TYPE>();
		if (sortkeyWords == null) {
			return null;
		}
		for (int i = 0; i < sortkeyWords.size(); i++) {
			SORT_TYPE st = SORT_TYPE.determineSortType(sortkeyWords.get(i));
			if (st == SORT_TYPE.INVALID) {
				return null;
			}
			sortKeyTypes.add(st);
		}
		return sortKeyTypes;
	}

	private ArrayList<Event> sort(ArrayList<Event> events,
			ArrayList<SORT_TYPE> sortTypeList) {
		if (events == null || sortTypeList == null) {
			return null;
		}
		for (int i = 0; i < sortTypeList.size(); i++) {// sort main events
			sortBySortType(events, sortTypeList.get(i));
		}
		return events;
	}

	private void sortBySortType(ArrayList<Event> events, SORT_TYPE sortKeyWord) {
		mergesort(0, events.size() - 1, events, sortKeyWord);
	}

	private void mergesort(int low, int high, ArrayList<Event> events,
			SORT_TYPE sortKeyWord) {
		// Check if low is smaller then high, if not then the array is sorted
		if (low < high) {
			int middle = (low + high) / 2;
			mergesort(low, middle, events, sortKeyWord);
			mergesort(middle + 1, high, events, sortKeyWord);
			merge(low, middle, high, events, sortKeyWord);
		}
	}

	private static void merge(int low, int middle, int high,
			ArrayList<Event> events, SORT_TYPE sortKeyWord) {
		ArrayList<Event> helper = new ArrayList<Event>();
		for (int i = 0; i < events.size(); i++) {
			helper.add(null);
		}
		for (int i = low; i <= high; i++) {
			helper.set(i, events.get(i));
		}
		int i = low;
		int j = middle + 1;
		int k = low;
		while (i <= middle && j <= high) {
			Event e1 = helper.get(i);
			Event e2 = helper.get(j);
			int result = Comparer.compareTo(e1, e2, sortKeyWord);
			if (result < 0) {
				events.set(k, helper.get(i));
				i++;
			} else {
				events.set(k, helper.get(j));
				j++;
			}
			k++;
		}
		while (i <= middle) {
			events.set(k, helper.get(i));
			k++;
			i++;
		}
		helper = null;
	}

	enum SORT_TYPE {
		START_DATE, END_DATE, PRIORITY, TAG, INVALID;

		static SORT_TYPE determineSortType(String sortKeyWord) {
			// TODO to identify the sort type
			if (sortKeyWord == null)
				throw new Error("Command type string cannot be null!");
			if (sortKeyWord.equalsIgnoreCase("startdate")) {
				return SORT_TYPE.START_DATE;
			} else if (sortKeyWord.equalsIgnoreCase("enddate")
					|| sortKeyWord.equalsIgnoreCase("duedate")
					|| sortKeyWord.equalsIgnoreCase("end_date")) {
				return SORT_TYPE.END_DATE;
			} else if (sortKeyWord.equalsIgnoreCase("priority")) {
				return SORT_TYPE.PRIORITY;
			} else if (sortKeyWord.equalsIgnoreCase("tag")) {
				return SORT_TYPE.TAG;
			} else
				return SORT_TYPE.INVALID;
		}
	}

	static class Comparer {
		public static int compareTo(Event e1, Event e2, SORT_TYPE st) {
			switch (st) {
			case START_DATE:
				return e1.getStartDate().compareTo(e2.getStartDate());
			case END_DATE:
				return e1.getEndDate().compareTo(e2.getEndDate());
			case PRIORITY:
				if (e1.getPriority() < e2.getPriority())
					return -1;
				if (e1.getPriority() > e2.getPriority())
					return 1;
				if (e1.getPriority() == e2.getPriority())
					return 0;
			case TAG:
				return e1.getTag().compareTo(e2.getTag());
			case INVALID:
				throw new Error("Unknown sort type");
			default:
				return 0;
			}
		}
	}

	/**
	 * Method: searchBy
	 * 
	 * @param searchKeyWords
	 *            : ArrayList of key words user wants to search by
	 * @param searchType
	 *            : String containing the attribute of the task that the user
	 *            wants to search in
	 * @return Result
	 */

	/*
	 * ############# First Level #############
	 */

	public Result searchBy(ArrayList<String> searchKeyWords, String searchType) {
		ArrayList<Event> results = new ArrayList<Event>();
		boolean isSuccessful = true;

		try {
			assert searchType != null : "Search type cannot be null! ";
			for (int i = 0; i < eventList.size(); i++) {
				Event currentTask = eventList.get(i);
				String taskSearchContent = returnRelevantAttribute(currentTask,
						searchType);

				// for anything else other than description, we want both search
				// term and result returned to be equal
				if (!searchType.equalsIgnoreCase("description")) {
					assert (searchKeyWords.size() == 1) : "Search keyword size is invalid; this is the size: "
							+ searchKeyWords.size();
					if (taskSearchContent.equalsIgnoreCase(searchKeyWords
							.get(0))) {
						isSuccessful = results.add(currentTask) && isSuccessful;
					}

				} else if (containsTerms(searchKeyWords, taskSearchContent)) {
					isSuccessful = results.add(currentTask) && isSuccessful;
				}
			}
			return new Result(isSuccessful, results);

		} catch (Exception e) {
			Controller.logSystemExceptionError(e.toString());
			return new Result(!isSuccessful, results, null, e.toString());
		}
	}
	/**
	 * Based on searchType given, returns the attribute specified by searchType of the Event
	 * @param currentTask
	 * @param searchType
	 * @return String
	 */

	/*
	 * ############# Second Level #############
	 */

	private static String returnRelevantAttribute(Event currentTask,
			String searchType) {
		String searchContent = null;
		if (searchType.equalsIgnoreCase("tag")) {
			searchContent = currentTask.getTag();
		} else if (searchType.equalsIgnoreCase("priority")) {
			Integer x = currentTask.getPriority();
			searchContent = x.toString();
		} else if (searchType.equalsIgnoreCase("enddate")) {
			Calendar date = currentTask.getEndDate();
			Integer dd = date.get(Calendar.DAY_OF_MONTH);
			Integer mm = date.get(Calendar.MONTH) + 1;
			Integer yyyy = date.get(Calendar.YEAR);
			searchContent = makeDoubleDigit(dd).concat(makeDoubleDigit(mm))
					.concat(yyyy.toString());
		} else if (searchType.equalsIgnoreCase("startdate")) {
			Calendar date = currentTask.getStartDate();
			Integer dd = date.get(Calendar.DAY_OF_MONTH);
			Integer mm = date.get(Calendar.MONTH) + 1;
			Integer yyyy = date.get(Calendar.YEAR);
			searchContent = makeDoubleDigit(dd).concat(makeDoubleDigit(mm))
					.concat(yyyy.toString());
		} else if (searchType.equalsIgnoreCase("taskid"))
			searchContent = Integer.toString(currentTask.getId());
		else { // default: description
			searchContent = currentTask.getDetails();
		}
		return searchContent;
	}

	private static boolean containsTerms(ArrayList<String> query, String item) {
		String currentItem = item.toLowerCase();
		for (int i = 0; i < query.size(); i++) {
			if (!currentItem.contains(query.get(i).toLowerCase())) {
				break;
			}
			if (i == query.size() - 1)
				return true;
		}
		return false;
	}

	/**
	 * Makes a single digit double by concatenating zero in front, and leaves
	 * double-digit numbers intact
	 * 
	 * @param x
	 * @return String
	 */

	/*
	 * ############# Third Level #############
	 */

	private static String makeDoubleDigit(int x) {
		assert (x / 10 < 4 && x / 10 > 0) : "Invalid date input: " + x;
		if (x / 10 < 1) {
			return "0".concat(Integer.toString(x));
		} else
			return Integer.toString(x);
	}

	/**
	 * Write to local text file and at the same time write to backup file
	 * 
	 * @return
	 */
	public boolean write() {
		// TODO Auto-generated method stub

		boolean r1 = writeToFile(fileName);
		boolean r2 = writeToBackupFile(backup_FileName);
		return r1 && r2;
	}

	private boolean writeToFile(String fileName) {
		// TODO Auto-generated method stub

		BufferedWriter outer;
		try {

			FileWriter fr = new FileWriter(fileName);
			outer = new BufferedWriter(fr);
			outer.write(PRINT_MSG);
			for (int i = 0; i < eventList.size(); i++) {
				Event e = eventList.get(i);
				outer.write(e.toString());
				outer.newLine();
			}
			outer.close();
		} catch (IOException e) {
			System.out.println(e.toString());
			Controller.logSystemExceptionError(e.toString());
			return false;
		}
		return true;
	}

	private boolean writeToBackupFile(String fileName) {
		// TODO Auto-generated method stub
		BufferedWriter outer;
		try {
			FileWriter fr = new FileWriter(fileName);
			outer = new BufferedWriter(fr);
			outer.write(PRINT_MSG);
			for (int i = 0; i < eventList.size(); i++) {
				Event e = eventList.get(i);
				outer.write(e.toString());
				outer.newLine();
			}
			outer.close();
		} catch (IOException e) {
			System.out.println(e.toString());
			Controller.logSystemExceptionError(e.toString());
			return false;
		}
		return true;
	}

	/**
	 * Read from local text file
	 * 
	 * @throws IOException
	 *             when file not found
	 * @throws ParseException
	 *             when local data file is randomly modified or does not exist
	 */
	private ArrayList<Event> readData(String fileName) throws ParseException,
			IOException {
		// TODO read data
		if (fileName == null) {
			throw new Error("Null fileName has been passed to readData");
		}
		FileReader fr = null;
		fr = new FileReader(fileName);
		BufferedReader br = new BufferedReader(fr);
		ArrayList<Event> eventList = new ArrayList<Event>();
		String st;
		while ((st = (br.readLine())) != null) {
			if (!(st.startsWith("#"))) {
				eventList.add(Event.parseSingleEvent(st));
			}
		}
		return eventList;
	}

	/**
	 * To String method.
	 */
	public String toString() {
		String s = "";
		for (int i = 0; i < eventList.size(); i++) {
			s = s + "\r\nTask " + (i + 1) + ":\r\n";
			s = s + eventList.get(i).toString();
		}
		return s;
	}

}
