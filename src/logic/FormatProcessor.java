package logic;

import logic.DateParser;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import objects.*;
import test.InvalidUserInput;


/**
 * @author      Goh Horng Bor <infinitey@live.com>
 * @version     0.2                                   
 * @since       2010-09-29    
 * 
 * FormatProcessor is a parser logic-type class which
 * mainly parses any String input into data that can be 
 * understood by the Object classes. 
 * 
 * Its main function is to identify command type of an input and extract 
 * task detail, tag, priority and start/end dates from an input
 * string.
 * 
 * To use, call FormatProcessor.processUserInput(String) 
 * method explicitly and it will return a Command object.
 *     
 */

public class FormatProcessor {
	private static final String wordContentRegex = "\\w*";
	private static final String digitContentRegex = "\\d*";
	/** 
	 *  Command Type Regex - To detect the type of regex
	 *  
	 *  Acceptable Formats :
	 *  searchRegex  -> <start of line>search/-s/-search<spaces><word>
	 *  deleteRegex -> <start of line>delete/-d/delete<spaces><word>
	 *  editRegex -> <start of line>edit/-e/-edit<spaces><word>
	 *  markRegex -> <start of line>mark/-m/-mark<spaces><word>
	 *  
	 */
	static String addRegex = "^(add|-a(dd)?)\\s+";
	//static String addExtractRegex = "^(add|-a(dd)?)\\s+(\\w+).";
	static String searchRegex = "^(search|-s(earch)?)\\s+";
	static String deleteRegex = "^(delete|-d(elete)?)\\s+";
	static String editExtractRegex = "^(edit|-e(dit)?)\\s+(\\d+).";
	static String editRegex = "^(edit|-e(dit)?)\\s+";
	static String markRegex = "^(mark|-m(ark)?)\\s+";
	static String completeRegex = "^(complete|-c(omplete)?)\\s+";
	static String uncompleteRegex = "^(incomplete|-inc(omplete)?)\\s+";
	static String quoteRegex = "\"([^\"]*)\"";
	
	static String sortByTag = "(t(ag)?|t[ag]|" +
			"priority|p[riot](y)?|" +
			"description|desc|" +
			"e(nd)?\\s*|date|ed|" +
			"start\\s*date|sd)" +
			"\\s+\\w";
	
	
	/** 
	 *  Syntax Type format - To detect additional information within input
	 *  
	 *  Acceptable Formats :
	 *  priorityRegex -> !!/!<numeric>
	 *  datePeriodRegex -> <anything>from/-sd<spaces><anything>till/until/to/-ed<space><anything>
	 *  dateAfterRegex -> after/-sd<space><anything>
	 *   
	 *  
	 */
	static String priorityRegex = "-p\\s+\\d|priority\\s+\\d";
	static String priorityExtractRegex = "(^|\\s+)(-p\\s+|priority\\s+)(\\d+)?(\\s+|$)";
	static String tagRegex = "(\\-t\\s+|tag\\s+)";
	static String tagExtractRegex = "(^|\\s+)(-t\\s+|tag\\s+)(\\w+((\\s+\\w+)?\\s+\\w+)?)(-|\\s+|$)";
	static String searchDateAfterRegex = "(after|-sd)\\s+";
	static String searchDateBeforeRegex = "(before|till|until|to|-ed)\\s+";
	static Pattern datePeriodRegex = Pattern.compile("(from|-sd)\\s+.+(till|until|-ed)\\s+.+");
	static Pattern dateAfterRegex = Pattern.compile("(startdate|-sd)\\s+.+");
	static Pattern dateBeforeRegex = Pattern.compile("(enddate|before|till|until|-ed)\\s+.+");
	static Pattern dateWithinRegex = Pattern.compile("within\\s+.+"); //experimental
	static Pattern dateFormatRegex =  Pattern.compile("(\\d+(/|-)\\d+|\\d+(/|-)\\d+(/|-)\\d+)");
	
	
	//======================================================================================
	//Level One Refactor
	//======================================================================================
	
	/**
	 * Main method to execute 
	 * Command Types : SEARCH, DELETE, ADD, ADDSUB, EDIT, SORT
	 *
	 * Use {@link #parseDate(String)} to detect date formats
	 *
	 * @param input	String
	 * @return Command
	 */
	

	public static Command processUserInput(String input) throws InvalidUserInput
	{	
		Command userCommand = new Command();
		
		input = input.replace("=", "");
		
		assert !(input.equals("")) : "Empty input is not allowed!";
		
		String commandType = detectCommandByRegex(input);
		userCommand.setCommandType(commandType);
		
		if(commandType.equalsIgnoreCase("COMPLETE"))
		{
			completeParse(input, userCommand);
			return userCommand;
		}
		else if(commandType.equalsIgnoreCase("INCOMPLETE"))
		{
			incompleteParse(input, userCommand);
			return userCommand;
		}
		else if(commandType.equalsIgnoreCase("SEARCHOPEN"))
		{
			searchParse(input, userCommand);
			return userCommand;
		}
		else if(commandType.equalsIgnoreCase("DELETECOMPLETE"))
		{
			deleteParse(input,userCommand);
			return userCommand;
		}
		else if(commandType.equalsIgnoreCase("DELETEOPEN"))
		{
			deleteParse(input,userCommand);
			return userCommand;
		}
		else if(commandType.equalsIgnoreCase("SET"))
		{
			parseFullEditInput(input, userCommand);
			return userCommand;
		}
		else
		{
			parseFullAddInput(input, userCommand);
			return userCommand;
		}
		
	}

	//======================================================================================
	//Level Two Refactor
	//======================================================================================
	private static String detectCommandByRegex(String input) {
		// TODO Auto-generated method stub
		assert !(input.equals("")) : "Empty input is not allowed!";
		
		Matcher checkMatch;
		
		checkMatch = tryMatchPattern(completeRegex+".+", input);
		if(checkMatch.matches())
		{
			return "COMPLETE";
		}

		checkMatch = tryMatchPattern(uncompleteRegex+".+", input);
		if(checkMatch.matches())
		{
			return "INCOMPLETE";
		}
		
		checkMatch = tryMatchPattern(searchRegex+".+", input);
		if(checkMatch.matches())
		{
			return "SEARCHOPEN";
		}
		
		checkMatch = tryMatchPattern(deleteRegex+".+", input);
		if(checkMatch.matches())
		{
			if(input.contains("-i"))
			{
				return "DELETEOPEN";
			}
			else
			{
				return "DELETECOMPLETE";
			}
		}
		checkMatch = tryMatchPattern(editRegex+digitContentRegex+".\\s"+".+", input);
		if(checkMatch.matches())
		{
			return "SET";
		}
		
		return "ADD";
	}
	
	//======================================================================================
	//Level Three Refactor
	//======================================================================================

	/**
	 * Parses input which indicates complete command
	 * @param input
	 * @param userCommand
	 * @throws test.InvalidUserInput
	 */
	private static void completeParse(String input, Command userCommand) throws test.InvalidUserInput {
		// TODO Auto-generated method stub
		input = input.replaceFirst(completeRegex, "").trim();
		input = input.replaceFirst("\\.", "").trim();
		String[] contentHolder = input.split(" ");
		if(contentHolder[0]!= null)
		{
			try
			{
				int taskId = Integer.parseInt(contentHolder[0].trim());
				userCommand.setId(taskId);
			}
			catch(NumberFormatException e)
			{
				throw new test.InvalidUserInput("Invalid complete command format.");
			}
		}
	}
	
	/**
	 * Parses input which indicates incomplete command
	 * @param input
	 * @param userCommand
	 * @throws test.InvalidUserInput
	 */
	private static void incompleteParse(String input, Command userCommand) throws test.InvalidUserInput {
		// TODO Auto-generated method stub
		input = input.replaceFirst(uncompleteRegex, "").trim();
		input = input.replaceFirst("\\.", "").trim();
		String[] contentHolder = input.split(" ");
		if(contentHolder[0]!= null)
		{
			try
			{
				int taskId = Integer.parseInt(contentHolder[0].trim());
				userCommand.setId(taskId);
			}
			catch(NumberFormatException e)
			{
				throw new test.InvalidUserInput("Invalid incomplete command format.");
			}
		}
	}

	/**
	 * Parses input which indicates search command
	 * @param input
	 * @param userCommand
	 * @throws test.InvalidUserInput
	 */
	private static void searchParse(String input, Command userCommand) throws test.InvalidUserInput {
		// TODO Auto-generated method stub
		input = input.replaceFirst(searchRegex, "").trim();
		if(!input.equals(""))
		{
			input = processAndSetSearchType(input, userCommand);
			
			ArrayList<String> searchKeyWords = new ArrayList<String>();
			
			processAndSetSearchKeyWords(input, userCommand,
					searchKeyWords);
		}
		
	}
	
	/**
	 * Parses input which indicates delete command
	 * @param input
	 * @param userCommand
	 * @throws test.InvalidUserInput
	 */
	private static void deleteParse(String input, Command userCommand) throws test.InvalidUserInput {
		// TODO Auto-generated method stub
		assert !(input.equals("")) : "Empty input is not allowed!";
		input = input.replaceFirst(deleteRegex, "");
		if(input.contains("-i"))
		{
			input = input.replace("-i", "");
		}
		else if (input.contains("-c"))
		{
			input = input.replace("-c", "");
		}
		else
		{
			throw new test.InvalidUserInput("Invalid delete command type.");
		}
		
		String[] sHolder = input.split("\\.");
		
		try {
            userCommand.setId(Integer.parseInt(sHolder[0].trim()));
        } catch (NumberFormatException ex) {
        	userCommand.setId(-1);
        	throw new test.InvalidUserInput("Invalid delete command format.");
        }
	}


	/**
	 * Parses input which indicates edit command
	 * @param input
	 * @param userCommand
	 * @throws test.InvalidUserInput
	 */
	private static void parseFullEditInput(String input, Command userCommand) throws InvalidUserInput {
		Matcher checkMatch = tryMatchPattern(editExtractRegex,input);
		if(checkMatch.find())
		{
			userCommand.setId(Integer.parseInt(checkMatch.group(3)));
			input = input.replace(checkMatch.group(), "");
		}
		else
		{
			throw new InvalidUserInput("Invalid edit command format.");
		}
		
		checkMatch = tryMatchPattern(quoteRegex, input);
		String quotedWords = "";
		boolean isThereQuotes = false;
		if(checkMatch.find())
		{
			quotedWords = checkMatch.group(1);
			input = input.replace(quotedWords, "");
			isThereQuotes = true;
		}

		
		input = parsePriority(input, userCommand);
		input = parseTag(input, userCommand);
		input = DateParser.parseDate(input, userCommand);
		if(isThereQuotes)
		{
			input = input.replaceFirst("\"\"", quotedWords);
		}

		
		userCommand.setDetail(input.trim());
	}

	/**
	 * Parses input which indicates add command
	 * @param input
	 * @param userCommand
	 * @throws test.InvalidUserInput
	 */
	private static void parseFullAddInput(String input, Command userCommand) throws InvalidUserInput {
		Matcher checkMatch = tryMatchPattern(addRegex,input);
		if(checkMatch.find())
		{
			input = input.replace(checkMatch.group(), "");
		}
		
		checkMatch = tryMatchPattern(quoteRegex, input);
		String quotedWords = "";
		boolean isThereQuotes = false;
		if(checkMatch.find())
		{
			quotedWords = checkMatch.group(1);
			input = input.replace(quotedWords, "");
			isThereQuotes = true;
		}
	
		input = parsePriority(input, userCommand);
		input = parseTag(input, userCommand);
		input = DateParser.parseDate(input, userCommand);
		
		if(isThereQuotes)
		{
			input = input.replaceFirst("\"\"", quotedWords);
		}
		
		checkValidStartDate(userCommand);
		userCommand.setDetail(input.trim());
	}

	//======================================================================================
	//Level Four Refactor
	//======================================================================================

	/**
	 * Check if the startDate is valid. If not, set to current date.
	 * @param userCommand
	 */
	private static void checkValidStartDate(Command userCommand) {
		Format formatter = new SimpleDateFormat("ddMMyyyy");
		String checkStartDate = formatter.format(userCommand.getStartDate().getTime());
		if(checkStartDate.endsWith("01021900"))
		{
			Calendar cal = Calendar.getInstance();
			userCommand.setStartDate(cal);
		}
	}

	/**
	 * Identity command indicated by user in the input. Then set
	 * commandType field in Command object accordingly.
	 * 
	 * @param input
	 * @param userCommand
	 * @return
	 */
	private static String processAndSetSearchType(String input, Command userCommand) {
		// TODO Auto-generated method stub
		
		boolean beenMatched = false;
		Matcher checkMatch;
		
		checkMatch = tryMatchPattern(tagRegex+".+", input);
		if(checkMatch.matches())
		{
			input = input.replaceFirst(tagRegex, "").trim();
			userCommand.setSearchType("TAG");
			beenMatched = true;
		}
		checkMatch = tryMatchPattern(dateAfterRegex.toString(), input);
		if(checkMatch.matches())
		{
			userCommand.setSearchType("STARTDATE");
			beenMatched = true;
		}
		checkMatch = tryMatchPattern(dateBeforeRegex.toString(), input);
		if(checkMatch.matches())
		{
			userCommand.setSearchType("ENDDATE");

			beenMatched = true;
		}
		
		checkMatch = tryMatchPattern(priorityRegex.toString(),input);
		if(checkMatch.matches())
		{
			userCommand.setSearchType("PRIORITY");
			input = input.replaceFirst("-p|priority", "").trim();
			beenMatched = true;
		}
		
		checkMatch = tryMatchPattern(addRegex.toString(),input);
		if(!beenMatched || checkMatch.matches() )
		{
			userCommand.setSearchType("DESCRIPTION");
			input = input.replaceFirst(addRegex, "").trim();
			
			beenMatched = true;
		}

		return input;
	}
	
	/**
	 * Method to process key words indicated by user in the
	 * input for search. 
	 * 
	 * @param input
	 * @param userCommand
	 * @param searchKeyWords
	 * @throws test.InvalidUserInput
	 */
	private static void processAndSetSearchKeyWords(String input,
			Command userCommand, ArrayList<String> searchKeyWords) throws test.InvalidUserInput{
		Format formatter;
		formatter = new SimpleDateFormat("ddMMyyyy");
		
		if(userCommand.getSearchType().equals("TAG"))
		{
			searchKeyWords.add(input);
			userCommand.setSearchKeyWords(searchKeyWords);
		}
		else if(userCommand.getSearchType().equals("STARTDATE"))
		{
			DateParser.parseDate(input, userCommand);
			input = formatter.format(userCommand.getStartDate().getTime());
			searchKeyWords.add(input);
			userCommand.setSearchKeyWords(searchKeyWords);
		}
		else if(userCommand.getSearchType().equals("ENDDATE"))
		{
			DateParser.parseDate(input, userCommand);
			input = formatter.format(userCommand.getEndDate().getTime());
			searchKeyWords.add(input);
			userCommand.setSearchKeyWords(searchKeyWords);
		}
		else if(userCommand.getSearchType().equals("PRIORITY"))
		{
			searchKeyWords.add(input);
			userCommand.setSearchKeyWords(searchKeyWords);
		}
		else if(userCommand.getSearchType().equals("DESCRIPTION"))
		{
			String[] words = input.split("\\s+");
			for(int i =0; i<words.length; i++)
			{
				searchKeyWords.add(words[i]);
				
			}
			userCommand.setSearchKeyWords(searchKeyWords);
		}
	}
	
	/**
	 * Look for Priority syntax and set priority to the Command object
	 * @param input
	 * @param userCommand
	 * @return
	 */
	private static String parsePriority(String input, Command userCommand) throws test.InvalidUserInput
	{
		// TODO Auto-generated method stub
		Matcher checkMatch = tryMatchPattern(priorityExtractRegex,input);
		boolean checkModified = false;
		int priority;
		while(checkMatch.find())
		{
			priority = Integer.parseInt(checkMatch.group(3));
			
			catchInvalidPriorityParam(priority);
			
			userCommand.setPriority(priority);
			input = input.replace(checkMatch.group(2)+checkMatch.group(3), "");
			checkModified = true;
			checkMatch = tryMatchPattern(priorityExtractRegex,input);
		}
		
		if(!checkModified)
		{
			userCommand.setPriority(-1);
		}
		
		catchPriorityWithNoParam(input);
		
		return input;
	}

	
	/**
	 * Look for Tag syntax and set tag to the Command object. Note that
	 * the tag has an intake size of maximum 3 words.
	 * 
	 * @param input
	 * @param userCommand
	 * @return
	 */
	private static String parseTag(String input, Command userCommand) {
		// TODO Auto-generated method stub
		Matcher checkMatch = tryMatchPattern(tagExtractRegex,input);
		boolean checkModified = false;
		while(checkMatch.find())
		{
			userCommand.setTag(checkMatch.group(3));
			input = input.replace(checkMatch.group(2)+checkMatch.group(3), "");
			checkModified = true;
			checkMatch = tryMatchPattern(tagExtractRegex,input);
		}
		
		if(!checkModified)
		{
			userCommand.setTag("empty");
		}
		return input;
	}

	//======================================================================================
	//Level Five Refactor
	//======================================================================================
	private static void catchInvalidPriorityParam(int priority)
			throws InvalidUserInput {
		if(!(priority == 0 || priority == 1))
			throw new test.InvalidUserInput("Invalid Priority specified. Please enter -p 1 or -p 0.");
	}

	private static void catchPriorityWithNoParam(String input)
			throws InvalidUserInput {
		if(input.contains(" -p ")||input.contains("-p"))
		{
			throw new test.InvalidUserInput("Invalid Priority specified. Please enter -p 1 or -p 0.");
		}
	}
	
	
	private static Matcher tryMatchPattern(String regex, String input) {
		return Pattern.compile(regex).matcher(input);
	}
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		String CurLine = ""; // Line read from standard in

		InputStreamReader converter = new InputStreamReader(System.in);
		BufferedReader in = new BufferedReader(converter);
		Command cmd = null;
		Format formatter = new SimpleDateFormat("ddMMyyyy");
		while (!(CurLine.equals("quit"))) {
			try {
				CurLine = in.readLine();

				try{
					cmd = processUserInput(CurLine);
				}
				catch(InvalidUserInput e)
				{
					Controller.logSystemExceptionError(e.getError());
				}
				System.out.println("Command: "+ cmd.getCommandType());
				System.out.println("Detail:" + cmd.getDetail());
				System.out.println("Search:" + cmd.getSearchType());
				System.out.println("Tag:" + cmd.getTag());
				System.out.println("Priority:" + cmd.getPriority());
				System.out.println("Task Id:" + cmd.getId());
				if(!cmd.getSearchType().equalsIgnoreCase("DESCRIPTION"))
				{
					System.out.println(cmd.getSearchKeyWords().get(0));
				}
				else
				{
					ArrayList<String> temp = cmd.getSearchKeyWords();
					int i = 0;
					while(i<temp.size())
					{
						System.out.println(temp.get(i));
						i++;
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}
	

}


@SuppressWarnings("serial")
class InvalidCommandType extends Exception {
	String mistake;

	// Default constructor - initializes instance variable to unknown
	public InvalidCommandType() {
		super();
		mistake = "unknown";
	}

	// Store error inside this string
	public InvalidCommandType(String err) {
		super(err); // call super class constructor
		mistake = err; // save message
	}

	// public method, callable by exception catcher. It returns the error
	// message
	public String getError() {
		return mistake;
	}
}
