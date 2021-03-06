/**
 * @author Jiao Jing Ping
 * @version 0.2
 * This is a unit test class for SortedEventList object.
 * Test cases are generated by DummyEventGenerator
 * 
 * Tests include API:
 * addEvent();
 * getEventWithId();
 * deleteEventWithId();
 * editTaskWithId();
 * searchBy();
 * sortBy();
 */
package test.objects;

import java.util.ArrayList;
import objects.Event;
import objects.Result;
import objects.SortedEventList;
import test.DummyEventGenerator;
import junit.framework.TestCase;

/**
 */
public class SortedEventListDriver extends TestCase {
	private SortedEventList testList;
	private Event testEvent;
	private int testId;
	private String testDes;
	private String testTag;
	private Event newEvent;

	/**
	 * @param name
	 */
	public SortedEventListDriver(String name) {
		super(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() {
		try {
			super.setUp();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		testList = new SortedEventList(DummyEventGenerator.getDummyEvents(10));
		testEvent = DummyEventGenerator.getDummyEvent();
		newEvent = DummyEventGenerator.getDummyEvent();
		this.testDes = testEvent.getDetails();
		this.testId = testEvent.getId();
		this.testTag = testEvent.getTag();
		testEvent.getPriority();
		testEvent.getEndDate();
		testList.addEvent(testEvent);
	}

	public void testGetEventWithId() {
		// TODO Auto-generated method stub
		System.out.println("This is test for getEventWithId(int id)");
		Result r = testList.getEventWithId(testId);
		assertEquals(testEvent, r.getData().get(0));

	}

	public void testAddEvent() {
		// TODO Auto-generated method stub
		System.out.println("This is test for addEvent(Event e)");
		int oriSize = testList.size();
		testList.addEvent(newEvent);
		int newSize = testList.size();
		assertEquals(oriSize + 1, newSize);
	}

	public void printTestData() {
		if (testList == null) {
			throw new Error("testList is null");
		}
		System.out.println("This is testList:");
		for (int i = 0; i < testList.size(); i++) {
			System.out.println(testList.getArrayList().getData().get(i));
		}
	}

	public void testSearchBy() {
		// TODO Auto-generated method stub
		System.out
				.println("This is test for searchBy(ArrayList<String> list,String type)");
		ArrayList<String> s = new ArrayList<String>();
		s.add(testDes);
		Event e1 = testList.searchBy(s, "description").getData().get(0);
		assertEquals(e1.getDetails(), testDes);
		s.clear();
		s.add(testTag);
		Event e2 = testList.searchBy(s, "tag").getData().get(0);
		assertEquals(e2.getTag(), testTag);
	}

	public void testEditTaskWithId() {
		// TODO Auto-generated method stub
		System.out.println("This is test for editEventWithId(int Id)");
		System.out.println("This is newEvent\n" + newEvent);
		testList.editEventWithId(newEvent, testId);
		Event c = testList.getEventWithId(testId).getData().get(0);
		assertEquals(newEvent.getDetails(), c.getDetails());
		assertEquals(newEvent.getEndDate(), c.getEndDate());
		assertEquals(newEvent.getTag(), c.getTag());
		assertEquals(newEvent.getPriority(), c.getPriority());
		System.out.println("This is afterEditing Event\n" + c);
		System.out.println("This is test for editEventWithId(nagetive value)");
		Result r = testList.editEventWithId(newEvent, -1);
		assertEquals(false, r.isSuccessful());
	}

	public void testDeleteEventWithId() {
		// TODO Auto-generated method stub
		System.out.println("This is test for deleteEventWithId(int Id)");
		int oriSize = testList.size();
		testList.deleteEventWithId(testId);
		int newSize = testList.size();
		assertEquals(oriSize - 1, newSize);
		System.out
				.println("This is test for deleteEventWithId(nagetive value)");
		Result r = testList.deleteEventWithId(-1);
		assertEquals(false, r.isSuccessful());
	}

	public static void main(String[] args) {
		SortedEventListDriver test = new SortedEventListDriver("test");
		test.setUp();
		test.testAddEvent();
		test.testGetEventWithId();
		test.testDeleteEventWithId();
		test.setUp();
		test.testEditTaskWithId();
		test.setUp();
		test.testSearchBy();
		test.setUp();
		test.testSortBy();
	}

	public void testSortBy() {
		// TODO Auto-generated method stub
		System.out
				.println("This is a test for sortBy(ArrayList<String> sortType)");
		ArrayList<String> s = new ArrayList<String>();
		s.add("enddate");
		ArrayList<Event> list = testList.sortBy(s).getData();

		for (int i = 0; i < list.size() - 1; i++) {
			assertEquals(
					true,
					(list.get(i).getEndDate()
							.compareTo(list.get(i + 1).getEndDate()) == 0)
							|| (list.get(i).getEndDate()
									.compareTo(list.get(i + 1).getEndDate()) == -1));
		}
	}
}
