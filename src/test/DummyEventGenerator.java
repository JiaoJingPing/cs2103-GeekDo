package test;

import objects.Event;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

/**
 * Generates dummy Events
 * 
 * @author JJP
 * 
 */
public class DummyEventGenerator {

	private final static int PRIORITY_LEVEL = 2;
	private static Random r = new Random();

	public static Event getDummyEvent() {
		// TODO generate a dummy event
		String dummyDetail = "DummyEvent." + Double.toString(r.nextInt(100));
		String dummyTag = "DummyTag" + Double.toString(r.nextInt(100));
		int dummyPriority = getDummyPriority(PRIORITY_LEVEL);
		Calendar dummyStartDate = DummyDateGenerator.getDummyDate();
		Calendar dummyEndDate = DummyDateGenerator
				.getDummyDateAfterDate(dummyStartDate);
		return new Event(dummyDetail, dummyTag, dummyPriority, dummyStartDate,
				dummyEndDate);
	}

	private static int getDummyPriority(int priorityLevel) {
		int priority;
		priority = r.nextInt(priorityLevel);
		return priority;
	}

	public static ArrayList<Event> getDummyEvents(int size) {
		// TODO generate a list of events of size
		ArrayList<Event> events = new ArrayList<Event>();
		for (int i = 0; i < size; i++) {
			events.add(getDummyEvent());
		}
		return events;
	}

	/*
	 * public static void printEvent(Event dummyEvent) { int dummyId =
	 * dummyEvent.getId(); String dummyDetail = dummyEvent.getDetails(); String
	 * dummyTag = dummyEvent.getTag(); int dummyPriority =
	 * dummyEvent.getPriority(); Calendar dummyStartDate =
	 * dummyEvent.getStartDate(); Calendar dummyEndDate =
	 * dummyEvent.getEndDate(); ArrayList<Event> subtasks =
	 * dummyEvent.getSubTasks(); System.out.println();
	 * System.out.println("=============="); System.out.println("DummyEvent " +
	 * dummyId); System.out.println("Detail: " + dummyDetail + " Tag: " +
	 * dummyTag + " Priority: " + dummyPriority);
	 * System.out.print("Start Date: "); if (dummyStartDate != null) {
	 * System.out.println(dummyStartDate.getTime()); }
	 * System.out.print("End Date: "); if (dummyEndDate != null) {
	 * System.out.println(dummyEndDate.getTime()); }
	 * 
	 * if (subtasks.size() != 0) {// print subtasks
	 * System.out.println("------------------------");
	 * System.out.println("Subtasks:"); for (int i = 0; i < subtasks.size();
	 * i++) { printEvent(subtasks.get(i)); } } }
	 */
	public static void main(String args[]) {
		int testNumber = 20;

		for (int i = 0; i < testNumber; i++) {
			Event dummyEvent = getDummyEvent();
			System.out.println(dummyEvent);

		}
	}
}
