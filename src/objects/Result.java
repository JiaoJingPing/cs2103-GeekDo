package objects;

import java.util.ArrayList;

/**
 * @author Jiao Jing Ping
 * @version 0.2
 * @since 2011-09-29
 * 
 *        The Result object is a data encapsulation of an execution result. It
 *        conveys the success or failure of an execution, execution error if there is
 *        any and the data needed and command type of the original command that brought
 *        about the Result. 
 * 
 */

public class Result {
	// A header will be attached only by CommandHandler
	private String commandType;

	private boolean isSuccessful;
	private ArrayList<Event> eventList;
	private String detail;
	private String error;

	public Result(boolean isSuccessful, ArrayList<Event> eventList,
			String detail, String error) {
		this.isSuccessful = isSuccessful;
		this.eventList = eventList;
		this.detail = detail;
		this.error = error;
	}

	// if no detail
	public Result(boolean isSuccessful, ArrayList<Event> eventList) {
		this.isSuccessful = isSuccessful;
		this.eventList = eventList;

	}

	public Result(boolean isSuccessful, ArrayList<Event> eventList, String error) {
		this.isSuccessful = isSuccessful;
		this.error = error;
		this.eventList = eventList;

	}

	public void setSuccessful(boolean isSuccessful) {
		this.isSuccessful = isSuccessful;
	}

	public boolean isSuccessful() {
		return isSuccessful;
	}

	public ArrayList<Event> getData() {
		return eventList;
	}

	public void setData(ArrayList<Event> eventList) {
		this.eventList = eventList;
	}

	public String getDetail() {
		return detail;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public String getCommandType() {
		return commandType;
	}

	public Result setCommandType(String commandType) {
		this.commandType = commandType;
		return this;
	}
}
