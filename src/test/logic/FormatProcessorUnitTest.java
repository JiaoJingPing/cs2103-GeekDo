package test.logic;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.junit.Test;

import objects.Command;
import test.InvalidUserInput;
import junit.framework.TestCase;
import logic.FormatProcessor;
import test.InvalidUserInput;
/**
 * @author Goh Horng Bor
 *
 */
public class FormatProcessorUnitTest extends TestCase {
	private static String input;
	private static Command result = new Command();
	private static Format formatter = new SimpleDateFormat("ddMMyyyy");
	
	@Test
	public void testmain() 
	{
		try {
			addParseTestSuite();
			editParseTest();
			deleteParseTest();
			searchParseTest();
			completeParseTest();
			incompleteParseTest();
			
			//TestRunner.runAndWait(new TestSuite(ThingTester.class));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	public static void addParseTestSuite() throws Exception 
	{
		boolean isError = false;
		
		priorityTest();
		dateTest();
		standardScenarioTest();

				
	}


	private static void standardScenarioTest() throws InvalidUserInput {

		input = "";
		result = FormatProcessor.processUserInput(input);
		assertEquals("ADD", result.getCommandType());
		assertEquals(input, result.getDetail());
		assertEquals(-1, result.getPriority());
		assertEquals("empty", result.getTag());
		assertEquals("31014000", formatter.format(result.getEndDate().getTime()));
		assertEquals(getCurrentTime(), formatter.format(result.getStartDate().getTime()));
		
		
		input = "this is a normal task";
		result =  FormatProcessor.processUserInput(input);
		assertEquals("ADD", result.getCommandType());
		assertEquals(input, result.getDetail());
		assertEquals(-1, result.getPriority());
		assertEquals("empty", result.getTag());
		assertEquals("31014000", formatter.format(result.getEndDate().getTime()));
		assertEquals(getCurrentTime(), formatter.format(result.getStartDate().getTime()));
		
		input = "1";
		result = FormatProcessor.processUserInput(input);
		assertEquals("ADD", result.getCommandType());
		assertEquals(input, result.getDetail());
		assertEquals(-1, result.getPriority());
		assertEquals("empty", result.getTag());
		assertEquals("31014000", formatter.format(result.getEndDate().getTime()));
		assertEquals(getCurrentTime(), formatter.format(result.getStartDate().getTime()));
		
		input = "add i want to buy milk -ed 06/05/2012 -t Grocery -p 1";
		result = FormatProcessor.processUserInput(input);
		assertEquals("ADD", result.getCommandType());
		assertEquals("i want to buy milk", result.getDetail());
		assertEquals(1, result.getPriority());
		assertEquals("Grocery", result.getTag());
		assertEquals("06052012", formatter.format(result.getEndDate().getTime()));
		assertEquals(getCurrentTime(), formatter.format(result.getStartDate().getTime()));
		
		input = "add i want to buy milk \"ed 06/05/2012\" -t Grocery -p 1";
		result = FormatProcessor.processUserInput(input);
		assertEquals("ADD", result.getCommandType());
		assertEquals("i want to buy milk ed 06/05/2012", result.getDetail());
		assertEquals(1, result.getPriority());
		assertEquals("Grocery", result.getTag());
		assertEquals("06052012", formatter.format(result.getEndDate().getTime()));
		assertEquals(getCurrentTime(), formatter.format(result.getStartDate().getTime()));
		
		input = "add i want to buy milk from 25/05/2011 to 23/12/2011 -t Grocery -p 1";
		result = FormatProcessor.processUserInput(input);
		assertEquals("ADD", result.getCommandType());
		assertEquals("i want to buy milk", result.getDetail());
		assertEquals(1, result.getPriority());
		assertEquals("Grocery", result.getTag());
		assertEquals("23122011", formatter.format(result.getEndDate().getTime()));
		assertEquals("25052011", formatter.format(result.getStartDate().getTime()));
	
		input = "add i want to watch \"Day After Tomorrow\" from 25/05/2011 to 23/12/2011 -t Grocery -p 1";
		result = FormatProcessor.processUserInput(input);
		assertEquals("ADD", result.getCommandType());
		assertEquals("i want to watch Day After Tomorrow", result.getDetail());
		assertEquals(1, result.getPriority());
		assertEquals("Grocery", result.getTag());
		assertEquals("23122011", formatter.format(result.getEndDate().getTime()));
		assertEquals("25052011", formatter.format(result.getStartDate().getTime()));
	}


	private static void dateTest() throws InvalidUserInput {
		boolean isError;
		input = "-ed";
		errorCheckTest();
		input = "-ed attempt to spoil things";
		errorCheckTest();
		
		input = "32 January";
		errorCheckTest();
		input = "32 February";
		errorCheckTest();
		input = "32 March";
		errorCheckTest();
		input = "31 April";
		errorCheckTest();
		input = "32 May";
		errorCheckTest();
		input = "31 June";
		errorCheckTest();
		input = "32 July";
		errorCheckTest();
		input = "32 August";
		errorCheckTest();
		input = "31 September";
		errorCheckTest();
		input = "32 October";
		errorCheckTest();
		input = "31 November";
		errorCheckTest();
		input = "32 December";
		errorCheckTest();
		
		input = "32/01";
		errorCheckTest();
		input = "32/02";
		errorCheckTest();
		input = "32/03";
		errorCheckTest();
		input = "31/04";
		errorCheckTest();
		input = "32/05";
		errorCheckTest();
		input = "31/06";
		errorCheckTest();
		input = "32/07";
		errorCheckTest();
		input = "32/08";
		errorCheckTest();
		input = "31/09";
		errorCheckTest();
		input = "32/10";
		errorCheckTest();
		input = "31/11";
		errorCheckTest();
		input = "32/12";
		errorCheckTest();
		
		input = "0 January";
		errorCheckTest();
		input = "0 February";
		errorCheckTest();
		input = "0 March";
		errorCheckTest();
		input = "0 April";
		errorCheckTest();
		input = "0 May";
		errorCheckTest();
		input = "0 June";
		errorCheckTest();
		input = "0 July";
		errorCheckTest();
		input = "0 August";
		errorCheckTest();
		input = "0 September";
		errorCheckTest();
		input = "0 October";
		errorCheckTest();
		input = "0 November";
		errorCheckTest();
		input = "0 December";
		errorCheckTest();
		
		input = "0/01";
		errorCheckTest();
		input = "0/02";
		errorCheckTest();
		input = "0/03";
		errorCheckTest();
		input = "0/04";
		errorCheckTest();
		input = "0/05";
		errorCheckTest();
		input = "0/06";
		errorCheckTest();
		input = "0/07";
		errorCheckTest();
		input = "0/08";
		errorCheckTest();
		input = "0/09";
		errorCheckTest();
		input = "0/10";
		errorCheckTest();
		input = "0/11";
		errorCheckTest();
		input = "0/12";
		errorCheckTest();
		
		
		
		input = "31 January";
		result = FormatProcessor.processUserInput(input);
		assertEquals("ADD", result.getCommandType());
		assertEquals("", result.getDetail());
		assertEquals(-1, result.getPriority());
		assertEquals("empty", result.getTag());
		assertEquals("31012011", formatter.format(result.getEndDate().getTime()));
		assertEquals(getCurrentTime(), formatter.format(result.getStartDate().getTime()));
		
		input = "31 March";
		result = FormatProcessor.processUserInput(input);
		assertEquals("ADD", result.getCommandType());
		assertEquals("", result.getDetail());
		assertEquals(-1, result.getPriority());
		assertEquals("empty", result.getTag());
		assertEquals("31032011", formatter.format(result.getEndDate().getTime()));
		assertEquals(getCurrentTime(), formatter.format(result.getStartDate().getTime()));
		
		input = "31 May";
		result = FormatProcessor.processUserInput(input);
		assertEquals("ADD", result.getCommandType());
		assertEquals("", result.getDetail());
		assertEquals(-1, result.getPriority());
		assertEquals("empty", result.getTag());
		assertEquals("31052011", formatter.format(result.getEndDate().getTime()));
		assertEquals(getCurrentTime(), formatter.format(result.getStartDate().getTime()));
		
		
		input = "31 July";
		result = FormatProcessor.processUserInput(input);
		assertEquals("ADD", result.getCommandType());
		assertEquals("", result.getDetail());
		assertEquals(-1, result.getPriority());
		assertEquals("empty", result.getTag());
		assertEquals("31072011", formatter.format(result.getEndDate().getTime()));
		assertEquals(getCurrentTime(), formatter.format(result.getStartDate().getTime()));
		
		
		input = "31 August";
		result = FormatProcessor.processUserInput(input);
		assertEquals("ADD", result.getCommandType());
		assertEquals("", result.getDetail());
		assertEquals(-1, result.getPriority());
		assertEquals("empty", result.getTag());
		assertEquals("31082011", formatter.format(result.getEndDate().getTime()));
		assertEquals(getCurrentTime(), formatter.format(result.getStartDate().getTime()));
		
		input = "31 October";
		result = FormatProcessor.processUserInput(input);
		assertEquals("ADD", result.getCommandType());
		assertEquals("", result.getDetail());
		assertEquals(-1, result.getPriority());
		assertEquals("empty", result.getTag());
		assertEquals("31102011", formatter.format(result.getEndDate().getTime()));
		assertEquals(getCurrentTime(), formatter.format(result.getStartDate().getTime()));
		
		input = "31 December";
		result = FormatProcessor.processUserInput(input);
		assertEquals("ADD", result.getCommandType());
		assertEquals("", result.getDetail());
		assertEquals(-1, result.getPriority());
		assertEquals("empty", result.getTag());
		assertEquals("31122011", formatter.format(result.getEndDate().getTime()));
		assertEquals(getCurrentTime(), formatter.format(result.getStartDate().getTime()));
		
		
		input = "10/08/2011";
		result = FormatProcessor.processUserInput(input);
		assertEquals("ADD", result.getCommandType());
		assertEquals("", result.getDetail());
		assertEquals(-1, result.getPriority());
		assertEquals("empty", result.getTag());
		assertEquals("10082011", formatter.format(result.getEndDate().getTime()));
		assertEquals(getCurrentTime(), formatter.format(result.getStartDate().getTime()));
		
		
		input = "16 May";
		result = FormatProcessor.processUserInput(input);
		assertEquals("ADD", result.getCommandType());
		assertEquals("", result.getDetail());
		assertEquals(-1, result.getPriority());
		assertEquals("empty", result.getTag());
		assertEquals("16052011", formatter.format(result.getEndDate().getTime()));
		assertEquals(getCurrentTime(), formatter.format(result.getStartDate().getTime()));
	}


	private static void errorCheckTest() {
		boolean isError;
		isError = false;
		try {
			result =  FormatProcessor.processUserInput(input);
		}catch (InvalidUserInput e)
		{
			isError = true;
		}
		assertEquals(true, isError);
	}


	private static void priorityTest() throws test.InvalidUserInput{
		boolean isError;
		input = "-p";
		errorCheckTest();
		
		input = "-p";
		errorCheckTest();
		
		input = "-p 1";
		isError = false;
		try {
			result =  FormatProcessor.processUserInput(input);
			assertEquals(1, result.getPriority());
		}catch (InvalidUserInput e)
		{
			isError = true;
		}
		assertEquals(false, isError);
		
		input = "-p 0";
		isError = false;
		try {
			result =  FormatProcessor.processUserInput(input);
			assertEquals(0, result.getPriority());
		}catch (InvalidUserInput e)
		{
			isError = true;
		}
		assertEquals(false, isError);
		
		input = "-p 2";
		errorCheckTest();
		
		input = "-p -1";
		errorCheckTest();
		
		
		
		input = "priority 0";
		isError = false;
		try {
			result =  FormatProcessor.processUserInput(input);
			assertEquals(0, result.getPriority());
		}catch (InvalidUserInput e)
		{
			isError = true;
		}
		assertEquals(false, isError);
		
		input = "priority 1";
		isError = false;
		try {
			result =  FormatProcessor.processUserInput(input);
			assertEquals(1, result.getPriority());
		}catch (InvalidUserInput e)
		{
			isError = true;
		}
		assertEquals(false, isError);
		
	}
	
	
	public static void editParseTest() throws Exception 
	{
		
		input = "edit 1. whatever -ed 20/07/2011 -t Market";
		result = FormatProcessor.processUserInput(input);
		assertEquals("SET", result.getCommandType());
		assertEquals("whatever", result.getDetail());
		assertEquals(-1, result.getPriority());
		assertEquals("Market", result.getTag());
		assertEquals(1, result.getId());
		assertEquals("20072011", formatter.format(result.getEndDate().getTime()));
		assertEquals(getCurrentTime(), formatter.format(result.getStartDate().getTime()));
		
		
		input = "-e 1. love cookie";
		result = FormatProcessor.processUserInput(input);
		assertEquals("SET", result.getCommandType());
		assertEquals("love cookie", result.getDetail());
		assertEquals(-1, result.getPriority());
		assertEquals("empty", result.getTag());
		assertEquals(1, result.getId());
		assertEquals("31014000", formatter.format(result.getEndDate().getTime()));
		assertEquals(getCurrentTime(), formatter.format(result.getStartDate().getTime()));
		
		
		input = "-e 1. recommend cookie -sd 24/06/2012 -ed 24/07/2012 -p 2";
		result = FormatProcessor.processUserInput(input);
		assertEquals("SET", result.getCommandType());
		assertEquals("recommend cookie", result.getDetail());
		assertEquals(2, result.getPriority());
		assertEquals("empty", result.getTag());
		assertEquals(1, result.getId());
		assertEquals("24072012", formatter.format(result.getEndDate().getTime()));
		assertEquals("24062012", formatter.format(result.getStartDate().getTime()));
		
		
		input = "edit 999999999. test the very very long int";
		result = FormatProcessor.processUserInput(input);
		assertEquals("SET", result.getCommandType());
		assertEquals("test the very very long int", result.getDetail());
		assertEquals(-1, result.getPriority());
		assertEquals("empty", result.getTag());
		assertEquals(999999999, result.getId());
		assertEquals("31014000", formatter.format(result.getEndDate().getTime()));
		assertEquals(getCurrentTime(), formatter.format(result.getStartDate().getTime()));
		
		
		input = "edit 4. -p 2 -t very cool tag";
		result = FormatProcessor.processUserInput(input);
		assertEquals("SET", result.getCommandType());
		assertEquals("", result.getDetail());
		assertEquals(2, result.getPriority());
		assertEquals("very cool tag", result.getTag());
		assertEquals(4, result.getId());
		assertEquals("31014000", formatter.format(result.getEndDate().getTime()));
		assertEquals(getCurrentTime(), formatter.format(result.getStartDate().getTime()));
		
	}
	
	public static void deleteParseTest() throws Exception 
	{
		
		input = "delete -i 1. i want to eat money";
		result = FormatProcessor.processUserInput(input);
		assertEquals("DELETEOPEN", result.getCommandType());
		assertEquals("", result.getDetail());
		assertEquals(-1000, result.getPriority());
		assertEquals("!!NO_CHANGE_FOR_TAG!!", result.getTag());
		assertEquals(1, result.getId());
		assertEquals("31014000", formatter.format(result.getEndDate().getTime()));
		assertEquals("01021800", formatter.format(result.getStartDate().getTime()));
		
		input = "delete -c 1. i want to eat money";
		result = FormatProcessor.processUserInput(input);
		assertEquals("DELETECOMPLETE", result.getCommandType());
		assertEquals("", result.getDetail());
		assertEquals(-1000, result.getPriority());
		assertEquals("!!NO_CHANGE_FOR_TAG!!", result.getTag());
		assertEquals(1, result.getId());
		assertEquals("31014000", formatter.format(result.getEndDate().getTime()));
		assertEquals("01021800", formatter.format(result.getStartDate().getTime()));
	
		input = "delete -i 1.";
		result = FormatProcessor.processUserInput(input);
		assertEquals("DELETEOPEN", result.getCommandType());
		assertEquals("", result.getDetail());
		assertEquals(-1000, result.getPriority());
		assertEquals("!!NO_CHANGE_FOR_TAG!!", result.getTag());
		assertEquals(1, result.getId());
		assertEquals("31014000", formatter.format(result.getEndDate().getTime()));
		assertEquals("01021800", formatter.format(result.getStartDate().getTime()));
		
		input = "delete -c 1.";
		result = FormatProcessor.processUserInput(input);
		assertEquals("DELETECOMPLETE", result.getCommandType());
		assertEquals("", result.getDetail());
		assertEquals(-1000, result.getPriority());
		assertEquals("!!NO_CHANGE_FOR_TAG!!", result.getTag());
		assertEquals(1, result.getId());
		assertEquals("31014000", formatter.format(result.getEndDate().getTime()));
		assertEquals("01021800", formatter.format(result.getStartDate().getTime()));
	}
	
	
	public static void searchParseTest() throws Exception 
	{
		
		input = "search this is a very cool line";
		result = FormatProcessor.processUserInput(input);
		assertEquals("SEARCHOPEN", result.getCommandType());
		assertEquals(6, result.getSearchKeyWords().size());
		assertEquals(-1000, result.getPriority());
		assertEquals("!!NO_CHANGE_FOR_TAG!!", result.getTag());
		assertEquals(0, result.getId());
		assertEquals("DESCRIPTION", result.getSearchType());
		assertEquals("31014000", formatter.format(result.getEndDate().getTime()));
		assertEquals("01021800", formatter.format(result.getStartDate().getTime()));
		
		
		input = "search -t school works";
		result = FormatProcessor.processUserInput(input);
		assertEquals("SEARCHOPEN", result.getCommandType());
		assertEquals(1, result.getSearchKeyWords().size());
		assertEquals(-1000, result.getPriority());
		assertEquals("!!NO_CHANGE_FOR_TAG!!", result.getTag());
		assertEquals(0, result.getId());
		assertEquals("TAG", result.getSearchType());
		assertEquals("31014000", formatter.format(result.getEndDate().getTime()));
		assertEquals("01021800", formatter.format(result.getStartDate().getTime()));
		
		
		input = "search -p 2";
		result = FormatProcessor.processUserInput(input);
		assertEquals("SEARCHOPEN", result.getCommandType());
		assertEquals(1, result.getSearchKeyWords().size());
		assertEquals(-1000, result.getPriority());
		assertEquals("!!NO_CHANGE_FOR_TAG!!", result.getTag());
		assertEquals(0, result.getId());
		assertEquals("PRIORITY", result.getSearchType());
		assertEquals("31014000", formatter.format(result.getEndDate().getTime()));
		assertEquals("01021800", formatter.format(result.getStartDate().getTime()));
		
		
		input = "search -ed 17 May";
		result = FormatProcessor.processUserInput(input);
		assertEquals("SEARCHOPEN", result.getCommandType());
		assertEquals("17052011", result.getSearchKeyWords().get(0));
		assertEquals(-1000, result.getPriority());
		assertEquals("!!NO_CHANGE_FOR_TAG!!", result.getTag());
		assertEquals(0, result.getId());
		assertEquals("ENDDATE", result.getSearchType());
		assertEquals("17052011", formatter.format(result.getEndDate().getTime()));
		assertEquals(getCurrentTime(), formatter.format(result.getStartDate().getTime()));
		

	}

	public static void completeParseTest() throws Exception 
	{
		
		input = "complete 22. doing homework";
		result = FormatProcessor.processUserInput(input);
		assertEquals("COMPLETE", result.getCommandType());
		assertEquals(-1000, result.getPriority());
		assertEquals("!!NO_CHANGE_FOR_TAG!!", result.getTag());
		assertEquals(22, result.getId());
		assertEquals("31014000", formatter.format(result.getEndDate().getTime()));
		assertEquals("01021800", formatter.format(result.getStartDate().getTime()));
		
		input = "complete 0. doing homework";
		result = FormatProcessor.processUserInput(input);
		assertEquals("COMPLETE", result.getCommandType());
		assertEquals(-1000, result.getPriority());
		assertEquals("!!NO_CHANGE_FOR_TAG!!", result.getTag());
		assertEquals(0, result.getId());
		assertEquals("31014000", formatter.format(result.getEndDate().getTime()));
		assertEquals("01021800", formatter.format(result.getStartDate().getTime()));
	}
	
	public static void incompleteParseTest() throws Exception 
	{
		
		input = "incomplete 22. doing homework";
		result = FormatProcessor.processUserInput(input);
		assertEquals("INCOMPLETE", result.getCommandType());
		//assertEquals("i want to eat money", result.getDetail());
		assertEquals(-1000, result.getPriority());
		assertEquals("!!NO_CHANGE_FOR_TAG!!", result.getTag());
		assertEquals(22, result.getId());
		assertEquals("31014000", formatter.format(result.getEndDate().getTime()));
		assertEquals("01021800", formatter.format(result.getStartDate().getTime()));
		
		input = "incomplete 0. doing homework";
		result = FormatProcessor.processUserInput(input);
		assertEquals("INCOMPLETE", result.getCommandType());
		//assertEquals("i want to eat money", result.getDetail());
		assertEquals(-1000, result.getPriority());
		assertEquals("!!NO_CHANGE_FOR_TAG!!", result.getTag());
		assertEquals(0, result.getId());
		assertEquals("31014000", formatter.format(result.getEndDate().getTime()));
		assertEquals("01021800", formatter.format(result.getStartDate().getTime()));
		
	}
	
	private static String getCurrentTime() {
		return formatter.format(Calendar.getInstance().getTime());
	}
	
	public String commandToString(String input)
	{
		Command cmd = null;
		try {
			cmd = FormatProcessor.processUserInput(input);
		} catch (InvalidUserInput e) {
			e.printStackTrace();
		}

		cmd.getCommandType();
		cmd.getDetail();
		cmd.getEndDate();
		cmd.getId();
		cmd.getPriority();
		cmd.getSearchKeyWords();
		cmd.getSearchType();
		cmd.getStartDate();
		cmd.getTag();
		return "";
	}
}
