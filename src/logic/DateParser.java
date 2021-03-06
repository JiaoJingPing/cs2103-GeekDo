package logic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import objects.*;
import test.InvalidUserInput;


/**
 * @author      Goh Horng Bor <infinitey@live.com>
 * @version     0.2                                   
 * @since       2010-09-29    
 * 
 * DateParser is a parser logic-type class which
 * mainly help parse date from a string.
 * 
 * The parser is achieved using regex. Hence, the 
 * position of the matcher groups are very important.
 * Do not change until you have understand the code
 * well.
 * 
 * DateParser is capable of reading British date format
 * this means DD/MM/YYYY and 7(th)? May not May 7.
 * 
 * Relaxed Date like DD MMM or DD <FULL MONTH>, Day 
 * of Week is supported as well.
 */

public class DateParser {
	private static final String FULL_FORMAT_DATE_PATTERN = 
			"(by|at|on)?(\\s+)?(0?[1-9]|[12][0-9]|3[01])[-./](0?[1|2-9]|1[012])" +
			"[-./]?((19|20)?\\d\\d)?";
	
	private static final String FULL_FORMAT_DATE_PATTERN_CHECK = 
			"(by|at|on)?(\\s+)?(\\d?\\d)[-./](\\d?\\d)[-./]?((\\d\\d)?\\d\\d)?";
	
	private static final String RELAXED_DATE_PATTERN = 
			"(by|at|on)?(\\s+)?(0?[1-9]|[12][0-9]|3[01])(?i)(st|nd|rd|th)?\\s+(?i)" +
			"(jan(uary)?|feb(ruary)?|mar(ch)?|apr(il)?|may|jun(e)?|" +
			"jul(y)?|aug(ust)?|sep(tember)?|oct(ober)?|nov(ember)?|dec(ember)?)" +
			"(\\s+)?((19|20)?\\d\\d)?";

	private static final String RELAXED_DATE_PATTERN_CHECK = 
			"(by|at|on)?(\\s+)?(\\d?\\d)(?i)(st|nd|rd|th)?\\s+(?i)" +
			"(jan(uary)?|feb(ruary)?|mar(ch)?|apr(il)?|may|jun(e)?|" +
			"jul(y)?|aug(ust)?|sep(tember)?|oct(ober)?|nov(ember)?|dec(ember)?)" +
			"(\\s+)?((\\d\\d)?\\d\\d)?";
	
	private static final String HUMAN_DATE_PATTERN = 
			"(day after|day before)?(\\s+)?(tomorrow|yesterday|yesterday|today)" +
			"(\\s+)?(morning|afternoon|night)?";

	private static final String HUMAN_DATE_ONE_PATTERN = "(after|in)?(\\s+)?" +
			"(\\d+|next)\\s+(day|days|week|weeks|month|months|year|years)" +
			"(\\s+)?(later|ago)?";
	
	private static final String HUMAN_DATE_TWO_PATTERN = "(by|at|on)?(\\s+)?" +
			"(next|last|by)?(\\s+)?((?i)mon(day)?|(?i)tue(sday)?|(?i)wed(nesday)?" +
			"|(?i)thur(sday)?|(?i)fri(day)?|(?i)sat(urday)?|(?i)sun(day)?)";
	
	static String searchDateAfterRegex = "(-sd|from)\\s+";
	static String searchDateBeforeRegex = "(before|till|until|to|on|-ed)\\s+";
	static String searchDateAfterRegexCheck = "(-sd)";
	static String searchDateBeforeRegexCheck = "(-ed)";

//==========================================================================================	
//Level One Refactor	
//==========================================================================================	
	/**
	 * Only method that outside component should only use to parse date.
	 * 
	 * @param input
	 * @param userCommand
	 * @return
	 */
	public static String parseDate(String input, Command userCommand) throws test.InvalidUserInput
	{
		assert input.equals("") : "No input to attempt parse date";
		
		Calendar cal = Calendar.getInstance();
		
		String originalInput = input;
		System.out.println("Original: "+ originalInput);
		input = parseSyntaxIterative(input, userCommand, searchDateBeforeRegex, searchDateAfterRegex);
		
		System.out.println("input: "+ input);
		if(input.equals(originalInput))
		{
			
			checkInvalidDateSyntax(input, originalInput);
			input = parseSyntaxIterative(input, userCommand, "",  "");
		}
		
		if(userCommand.getEndDate()==null)
			userCommand.setEndDate(cal);
		
		
		if(userCommand.getStartDate()==null	|| userCommand.getStartDate().get(Calendar.YEAR) == 1800)
			userCommand.setStartDate(cal);
		
		
		
		input = removeExcessSyntax(input);
		
		return input;
	}

	

	//================================================================================	
	//Level Two Refactor	
	//================================================================================
	/**
	 * Check for any date syntax like -ed and -sd. Throw exception if there is no
	 * recognized date format followed by the syntax.
	 * 
	 * @param input
	 * @param originalInput
	 * @throws InvalidUserInput
	 */
	private static void checkInvalidDateSyntax(String input,
			String originalInput) throws InvalidUserInput {
		if(input.equals(originalInput))
		{
			Pattern pattern = Pattern.compile(appendFlexibleRegex(searchDateAfterRegexCheck));
			Matcher matcher = pattern.matcher(input);
			if(matcher.find())
			{
				throw new test.InvalidUserInput("Invalid Date Input.");
			}
			pattern = Pattern.compile(appendFlexibleRegex(searchDateBeforeRegexCheck));
			matcher = pattern.matcher(input);
			if(matcher.find())
			{
				throw new test.InvalidUserInput("Invalid Date Input.");
			}
		}
	}
	
	/**
	 * Simple method to remove unnecessary enddate and startdate syntax
	 * @param input
	 * @return
	 */
	private static String removeExcessSyntax(String input) {
		input = input.replaceAll("-ed", "");
		input = input.replaceAll("-sd", "");
		return input;
	}

	
	/**
	 * This is the algorithim to parse an input iteratively for dates. 
	 * This method runs based on two logic which are formality of the date and
	 * the position of the date in the input.
	 * 
	 * Formatted Date Format like DD/MM/YYYY are always treated as the most
	 * important date. Therefore, in order to overwrite the same format
	 * date, you will have to type in similar format to overwrite the older one.
	 *  Example: add drink milk -ed 23/05/2011 -ed thursday 
	 *  does not overwrite.
	 *  Example: add drink milk -ed 23/05/2011 -ed 24/05/2011
	 *  does overwrite the previous date.
	 * 
	 * @param input
	 * @param userCommand
	 * @param endDateSyntax
	 * @param startDateSyntax
	 * @return
	 */
	private static String parseSyntaxIterative(String input, Command userCommand,
			String endDateSyntax,  String startDateSyntax) throws test.InvalidUserInput{
		String previousInput = input;
		boolean hasChanged = true;
		while(hasChanged)
		{
						
			input = validateAndExtractHumanDayWeekFormat(input, userCommand, endDateSyntax);
			input = validateAndExtractRelaxedDateFormat(input, userCommand, endDateSyntax);
			input = validateAndExtractHumanFormat(input, userCommand, endDateSyntax);
			input = validateAndExtractHumanDateFormat(input, userCommand, endDateSyntax);
			input = validateAndExtractFormattedDate(input, userCommand, endDateSyntax);
			
			
			input = validateAndExtractHumanDayWeekFormat(input, userCommand, startDateSyntax);
			input = validateAndExtractRelaxedDateFormat(input, userCommand, startDateSyntax);
			input = validateAndExtractHumanFormat(input, userCommand, startDateSyntax);
			input = validateAndExtractHumanDateFormat(input, userCommand, startDateSyntax);
			input = validateAndExtractFormattedDate(input, userCommand, startDateSyntax);
			
			if(!previousInput.equals(input))
			{
				hasChanged = true;
				previousInput = input;
			}
			else
			{
				hasChanged = false;
			}
		}
		return input;
	}
	
	//==========================================================================================	
	//Level Three Refactor	
	//==========================================================================================	

	/**
	 * This method parses for dates which have Days of Week. This also includes 
	 * (next|last) <DAY_OF_WEEK>; next monday, last mon. 
	 * 
	 * @param input
	 * @param userCommand
	 * @param syntaxRegex
	 * @return
	 */
	private static String validateAndExtractHumanDayWeekFormat(String input,
			Command userCommand, String syntaxRegex) {
		// TODO Auto-generated method stub
		Pattern pattern;
		Matcher matcher;
		boolean isNotNull = false;
		
		int firstWordPosition = 4;
		int secondWordPosition = 6;
		
		if(syntaxRegex.trim().equals(""))
		{
			pattern = Pattern.compile(appendFlexibleRegex(HUMAN_DATE_TWO_PATTERN));
			matcher = pattern.matcher(input);
		}
		else
		{
			pattern = Pattern.compile(appendFlexibleRegex(syntaxRegex+HUMAN_DATE_TWO_PATTERN));
			matcher = pattern.matcher(input);
			firstWordPosition++;
			secondWordPosition++;
		}

		Calendar cal = Calendar.getInstance();

		if(matcher.find())	
		{
			String day = matcher.group(secondWordPosition);
			int dayInteger = convertWeekDayToIntegerDay(day);
			
			Calendar currentDate = Calendar.getInstance();
			
			int currentIntegerDay = currentDate.DAY_OF_WEEK;
			adjustWeightOfDays(cal, dayInteger, currentIntegerDay);
			
			
			if(matcher.group(firstWordPosition)!=null)
			{
				isNotNull = true;
			}
			
			if(isNotNull && matcher.group(firstWordPosition).equals("next"))
			{
				calculateDayWithNext(cal, dayInteger, currentIntegerDay);
			}
			else
			{
				calculateDayWithLast(matcher, firstWordPosition, cal,
						dayInteger, currentIntegerDay, isNotNull);
			}
			
			input = input.replace(matcher.group(), " ");
			
			setDatesBasedOnSyntax(userCommand, syntaxRegex, cal);
		}
		return input;
	}

	/**
	 * This method parses potential date indicators like today,
	 * tomorrow, yesterday etc.
	 * 
	 * @param input
	 * @param userCommand
	 * @param syntaxRegex
	 * @return
	 */
	public static String validateAndExtractHumanFormat(String input,
			Command userCommand, String syntaxRegex)
	{
		Pattern pattern;
		Matcher matcher;
		
		int firstWordPosition = 2;
		int secondWordPosition = 4;
		
		if(syntaxRegex.trim().equals(""))
		{
			pattern = Pattern.compile(appendFlexibleRegex(HUMAN_DATE_PATTERN));
			matcher = pattern.matcher(input);
		}
		else
		{
			pattern = Pattern.compile(appendFlexibleRegex(syntaxRegex+HUMAN_DATE_PATTERN));
			matcher = pattern.matcher(input);
			firstWordPosition++;
			secondWordPosition++;
		}
		
		String combinedWord = "";

		
		if(matcher.find()) 
		{
			
			if(matcher.group(firstWordPosition)!=null)
			{
				combinedWord = matcher.group(firstWordPosition) +" "+ 
							matcher.group(secondWordPosition);
			}
			else
			{
				combinedWord = 	matcher.group(secondWordPosition);
			}
			Calendar cal = Calendar.getInstance();
			
			
			if(combinedWord.equals("tomorrow"))
			{
				cal.add(Calendar.DATE, 1);
			}
			else if(combinedWord.equals("day after tomorrow"))
			{
				cal.add(Calendar.DATE, 2);
			}
			else if(combinedWord.equals("yesterday"))
			{
				cal.add(Calendar.DATE, -1);
			}
			else if(combinedWord.equals("day before yesterday"))
			{
				cal.add(Calendar.DATE, -2);
			}
			
			setDatesBasedOnSyntax(userCommand, syntaxRegex, cal);
			input = input.replace(matcher.group(), " ");
		}
		
		return input;
	}
	
	/**
	 * This method parses and calculates the span of days/weeks/months/years.
	 * Example: 3 weeks later
	 * 
	 * @param input
	 * @param userCommand
	 * @param syntaxRegex
	 * @return
	 */
	public static String validateAndExtractHumanDateFormat(String input, 
			Command userCommand, String syntaxRegex)
	{
		Pattern pattern;
		Matcher matcher;
		
		int numberPosition = 4;
		int timeSpanPosition = 5;
		int laterAgoPosition =7;
		
		
		if(syntaxRegex.trim().equals(""))
		{
			pattern = Pattern.compile(appendFlexibleRegex(HUMAN_DATE_ONE_PATTERN));
			matcher = pattern.matcher(input);
		}
		else
		{
			pattern = Pattern.compile(appendFlexibleRegex(syntaxRegex+HUMAN_DATE_ONE_PATTERN));
			matcher = pattern.matcher(input);
			numberPosition++;
			timeSpanPosition++;
			laterAgoPosition++;

		}

		if(matcher.find()) 
		{	
			Calendar cal = Calendar.getInstance();
			
			
			String timeSpan = matcher.group(timeSpanPosition);
			if(timeSpan.equals("day") || timeSpan.equals("days"))
			{
				setDayPastOrFuture(matcher, numberPosition, laterAgoPosition,
						cal);
			}
			else if (timeSpan.equals("week") || timeSpan.equals("weeks"))
			{
				setWeekPastOrFuture(matcher, numberPosition,
						laterAgoPosition, cal);
			}
			else if (timeSpan.equals("month") || timeSpan.equals("months"))
			{
				setMonthPastOrFuture(matcher, numberPosition,
						laterAgoPosition, cal);
			}
			else
			{
				setYearPastOrFuture(matcher, numberPosition, laterAgoPosition,
						cal);
			}
			
			setDatesBasedOnSyntax(userCommand, syntaxRegex, cal);
			input = input.replace(matcher.group(), " ");
		}
		
		
		return input;
	}

	/**
	 * This parses relaxed date format. DD MMM or DD FULLMONTH
	 * @param input
	 * @param userCommand
	 * @param syntaxRegex
	 * @return
	 */
	public static String validateAndExtractRelaxedDateFormat(String input, 
			Command userCommand, String syntaxRegex) throws test.InvalidUserInput
	{
		Pattern pattern;
		Matcher matcher;
		
		int dayGroupPosition = 4;
		int monthGroupPosition = 6;
		int yearGroupPosition = 19;
		boolean isAppearValid = false;
		boolean isValidDate = false;
		
		if(syntaxRegex.equals(""))
		{
			pattern = Pattern.compile(appendFlexibleRegex(RELAXED_DATE_PATTERN_CHECK));
			matcher = pattern.matcher(input);
			if(matcher.find())
				isAppearValid = true;
			
			pattern = Pattern.compile(appendFlexibleRegex(RELAXED_DATE_PATTERN));
			matcher = pattern.matcher(input);
		}
		else
		{
			pattern = Pattern.compile(appendFlexibleRegex(syntaxRegex+RELAXED_DATE_PATTERN_CHECK));
			matcher = pattern.matcher(input);
			if(matcher.find())
				isAppearValid = true;
			
			pattern = Pattern.compile(appendFlexibleRegex(syntaxRegex+RELAXED_DATE_PATTERN));
			matcher = pattern.matcher(input);
			dayGroupPosition++;
			monthGroupPosition++;
			yearGroupPosition++;
		}
		
		
		
		if(matcher.find()) 
		{
			String day = matcher.group(dayGroupPosition);
			String month = convertToDigitsString(matcher.group(monthGroupPosition));
			Calendar cal = Calendar.getInstance();
			
			
			int year = cal.get(Calendar.YEAR);
			if(matcher.group(yearGroupPosition)!=null) {
				year = Integer.parseInt(matcher.group(yearGroupPosition));
			}
			
			year = appendFourDigitsYear(year);
			isValidDate = isValidDateAndMonth(day, month, year);
			
			if(isValidDate)
			{
				day = appendZeroIfLessThanTen(day);
				month = appendZeroIfLessThanTen(month);
				year = appendFourDigitsYear(year);
				
				input = input.replace(matcher.group(), " ");
				
				cal.set(year, Integer.parseInt(month), Integer.parseInt(day));
				setDatesBasedOnSyntax(userCommand, syntaxRegex, cal);
			}
			else
			{
				input = input.replace(matcher.group(), " ");
			}
			
		
		}
		
		if(isAppearValid && !isValidDate)
		{
			throw new test.InvalidUserInput("Invalid Date Format");
		}
		
			
		return input;
		
	}

	/**
	 * This method processes the formal/formatted date type.
	 * Available formats are : dd/MM/YYYY, dd/MM
	 * The accepted separators : -/.
	 * 
	 * @param input
	 * @param userCommand
	 * @param syntaxRegex
	 * @return
	 * @throws test.InvalidUserInput
	 */
	public static String validateAndExtractFormattedDate(String input,
			Command userCommand, String syntaxRegex) throws test.InvalidUserInput
	{
		Pattern pattern;
		Matcher matcher;

		int dayGroupPosition = 4;
		int monthGroupPosition = 5;
		int yearGroupPosition = 6;
		boolean isAppearValidDate = false;
		boolean isValidDate = false;
		
		
		
		if(syntaxRegex.equals(""))
		{
			pattern = Pattern.compile(appendFlexibleRegex(FULL_FORMAT_DATE_PATTERN_CHECK));
			matcher = pattern.matcher(input);
			if(matcher.find())
				isAppearValidDate = true;
			
			
			pattern = Pattern.compile(appendFlexibleRegex(FULL_FORMAT_DATE_PATTERN));
			matcher = pattern.matcher(input);
		}
		else
		{
			pattern = Pattern.compile(appendFlexibleRegex(syntaxRegex+FULL_FORMAT_DATE_PATTERN_CHECK));
			matcher = pattern.matcher(input);
			if(matcher.find())
				isAppearValidDate = true;
			
			pattern = Pattern.compile(appendFlexibleRegex(syntaxRegex+FULL_FORMAT_DATE_PATTERN));
			matcher = pattern.matcher(input);
			dayGroupPosition++;
			monthGroupPosition++;
			yearGroupPosition++;
		}
		
		if (matcher.find()) 
		{
			String day = matcher.group(dayGroupPosition);
			String month = matcher.group(monthGroupPosition);
			month = javaMonthAdjustment(month);
			Calendar cal = Calendar.getInstance();
			
			int year = cal.get(Calendar.YEAR);
			if(matcher.group(yearGroupPosition)!=null) {
				year = Integer.parseInt(matcher.group(yearGroupPosition));
			}
			
			year = appendFourDigitsYear(year);
			isValidDate = isValidDateAndMonth(day, month, year);
			if(isValidDate)
			{
				day = appendZeroIfLessThanTen(day);
				month = appendZeroIfLessThanTen(month);
				year = appendFourDigitsYear(year);
				
				input = input.replace(matcher.group(), " ");
				
				cal.set(year, Integer.parseInt(month), Integer.parseInt(day));
				
				setDatesBasedOnSyntax(userCommand, syntaxRegex, cal);
			}
			else
			{
				input = input.replace(matcher.group(), " ");
			}
			
		
		}
		
		if(isAppearValidDate && !isValidDate)
		{
			throw new test.InvalidUserInput("Invalid date format.");
		}
		
		return input;
			
		
	}

	/**
	 * Adjust to java month which is lesser in value of 1.
	 * @param month
	 * @return
	 */
	private static String javaMonthAdjustment(String month) {
		return (Integer.parseInt(month.trim())-1)+"";
	}
	
	//==========================================================================================	
	//Level Four Refactor	
	//==========================================================================================	

	/**
	 * Set calendars to Command startdate and enddate.
	 * @param userCommand
	 * @param syntaxRegex
	 * @param cal
	 */
	private static void setDatesBasedOnSyntax(Command userCommand,
			String syntaxRegex, Calendar cal) {
		if(syntaxRegex.equals(searchDateAfterRegex))
		{
			userCommand.setStartDate(cal);
		}
		else
		{
			userCommand.setEndDate(cal);
		}
	}


	/**
	 * Compare between current day and user specified day.
	 * If user specified day is less than current, increase week by 1
	 * to get the next following day of week.
	 * 
	 * @param cal
	 * @param dayInteger
	 * @param currentIntegerDay
	 */
	private static void adjustWeightOfDays(Calendar cal, int dayInteger,
			int currentIntegerDay) {
		if(dayInteger > currentIntegerDay)
		{
			cal.set(Calendar.DAY_OF_WEEK, dayInteger);
		}
		else
		{
			cal.add(Calendar.WEEK_OF_MONTH, 1);
			cal.set(Calendar.DAY_OF_WEEK, dayInteger);	
		}
	}



	private static void calculateDayWithNext(Calendar cal, int dayInteger,
			int currentIntegerDay) {
		if(dayInteger > currentIntegerDay)
		{
			cal.add(Calendar.WEEK_OF_YEAR, 1);
		}
	}



	private static void calculateDayWithLast(Matcher matcher,
			int firstWordPosition, Calendar cal, int dayInteger,
			int currentIntegerDay, boolean isNotNull) {
		if(isNotNull && matcher.group(firstWordPosition).equals("last") )
		{
			if(!(dayInteger > currentIntegerDay))
			{
				cal.add(Calendar.WEEK_OF_YEAR, -2);
			}
			else
			{
				cal.add(Calendar.WEEK_OF_YEAR, -3);
			}
			
		}
		else
		{
			cal.add(Calendar.WEEK_OF_YEAR, -1);
		}
	}



	private static int convertWeekDayToIntegerDay(String day) {
		int dayInteger;
		if(day.equalsIgnoreCase("sun")||day.equalsIgnoreCase("sunday"))
		{
			dayInteger = 1;
		}
		else if(day.equalsIgnoreCase("mon")||day.equalsIgnoreCase("monday"))
		{
			dayInteger = 2;
		}
		else if(day.equalsIgnoreCase("tue")||day.equalsIgnoreCase("tuesday"))
		{
			dayInteger = 3;
		}
		else if(day.equalsIgnoreCase("wed")||day.equalsIgnoreCase("wednesday"))
		{
			dayInteger = 4;
		}
		else if(day.equalsIgnoreCase("thur")||day.equalsIgnoreCase("thursday"))
		{
			dayInteger = 5;
		}
		else if(day.equalsIgnoreCase("fri")||day.equalsIgnoreCase("friday"))
		{
			dayInteger = 6;
		}
		else
		{
			dayInteger = 7;
		}
		return dayInteger;
	}

	private static String appendFlexibleRegex(String input)
	{
		return "(^|\\s+)"+ input + "($|\\s+)";
	}
	
	
	private static void setYearPastOrFuture(Matcher matcher,
			int numberPosition, int laterAgoPosition, Calendar cal) {
		int number;
		
		if(matcher.group(numberPosition).equals("next"))
		{
			number = 1;
		}
		else
		{
			number = Integer.parseInt(matcher.group(numberPosition));
		}
		
		if(matcher.group(laterAgoPosition)!=null)
		{
			if(matcher.group(laterAgoPosition).equals("ago"))
			{
				
				cal.add(Calendar.YEAR, number*-1);
			}
			else
			{
				cal.add(Calendar.YEAR, number);
			}
		}
		else
		{
			cal.add(Calendar.YEAR, number);
		}
	}

	private static void setMonthPastOrFuture(Matcher matcher,
			int numberPosition, int laterAgoPosition, Calendar cal) {
		int number;
		
		if(matcher.group(numberPosition).equals("next"))
		{
			number = 1;
		}
		else
		{
			number = Integer.parseInt(matcher.group(numberPosition));
		}
		
		if(matcher.group(laterAgoPosition)!=null)
		{
			if(matcher.group(laterAgoPosition).equals("ago"))
			{
				
				cal.add(Calendar.MONTH, number*-1);
			}
			else
			{
				cal.add(Calendar.MONTH, number);
			}
		}
		else
		{
			cal.add(Calendar.MONTH, number);
		}
	}

	private static void setWeekPastOrFuture(Matcher matcher,
			int numberPosition, int laterAgoPosition, Calendar cal) {
		int number;
		
		if(matcher.group(numberPosition).equals("next"))
		{
			number = 1;
		}
		else
		{
			number = Integer.parseInt(matcher.group(numberPosition));
		}
		
		if(matcher.group(laterAgoPosition)!=null)
		{
			if(matcher.group(laterAgoPosition).equals("ago"))
			{
				
				cal.add(Calendar.DATE, number*-1*7);
			}
			else
			{
				cal.add(Calendar.DATE, number*7);
			}
		}
		else
		{
			cal.add(Calendar.DATE, number*7);
		}
	}

	private static void setDayPastOrFuture(Matcher matcher, int numberPosition,
			int laterAgoPosition, Calendar cal) {
		int number;
		
		if(matcher.group(numberPosition).equals("next"))
		{
			number = 1;
		}
		else
		{
			number = Integer.parseInt(matcher.group(numberPosition));
		}
		
		if(matcher.group(laterAgoPosition)!=null)
		{
			if(matcher.group(laterAgoPosition).equals("ago"))
			{
				
				cal.add(Calendar.DATE, number*-1);
			}
			else
			{
				cal.add(Calendar.DATE, number);
			}
		}
		else
		{
			cal.add(Calendar.DATE, number);
		}
	}
	


	private static String convertToDigitsString(String group) {
		// TODO Auto-generated method stub
		if(group.equalsIgnoreCase("january")||group.equalsIgnoreCase("jan"))
		{
			return "00";
		}
		else if (group.equalsIgnoreCase("february")|group.equalsIgnoreCase("feb"))
		{
			return "01";
		}
		else if (group.equalsIgnoreCase("march")|group.equalsIgnoreCase("mar"))
		{
			return "02";
		}
		else if (group.equalsIgnoreCase("april")|group.equalsIgnoreCase("apr"))
		{
			return "03";
		}
		else if (group.equalsIgnoreCase("may")|group.equalsIgnoreCase("may"))
		{
			return "04";
		}
		else if (group.equalsIgnoreCase("june")|group.equalsIgnoreCase("jun"))
		{
			return "05";
		}
		else if (group.equalsIgnoreCase("july")|group.equalsIgnoreCase("jul"))
		{
			return "06";
		}
		else if (group.equalsIgnoreCase("august")|group.equalsIgnoreCase("aug"))
		{
			return "07";
		}
		else if (group.equalsIgnoreCase("september")|group.equalsIgnoreCase("sep"))
		{
			return "08";
		}
		else if (group.equalsIgnoreCase("october")|group.equalsIgnoreCase("oct"))
		{
			return "09";
		}
		else if (group.equalsIgnoreCase("november")|group.equalsIgnoreCase("nov"))
		{
			return "10";
		}
		else
		{
			return "11";
		}
		
	}
	
	private static int appendFourDigitsYear(int year) {
		if(year<100)
		{
			year = 2000+year;
		}
		return year;
	}

	private static String appendZeroIfLessThanTen(String digit) {
		if(digit.length()<2)
		{
			digit = "0"+digit;
		}
		return digit;
	}

	private static boolean isValidDateAndMonth(String day, String month, int year) 
	{
		if (isValidDateOfMonth(day, month)) 
		{
			return false; // only 1,3,5,7,8,10,12 has 31 days
		} 
		else if (isFebruary(month)) 
		{
			// leap year
			return checkValidDateOfFebruary(day, year);
		} 
		else 
		{
			return true;
		}
	}

	private static boolean checkValidDateOfFebruary(String day, int year) 
	{
		if (isLeapYear(year)) 
		{
			if (day.equals("30") || day.equals("31")) 
			{
				return false;
			} else {
				return true;
			}
		} 
		else 
		{
			if (day.equals("29") || day.equals("30")
					|| day.equals("31")) 
			{
				return false;
			} 
			else 
			{
				return true;
			}
		}
	}

	private static boolean isFebruary(String month) 
	{
		return month.equals("2") || month.equals("02");
	}

	private static boolean isLeapYear(int year) 
	{
		return year % 4==0;
	}

	private static boolean isValidDateOfMonth(String day, String mth) 
	{
		mth = (Integer.parseInt(mth.trim()) + 1) + "";
		return day.equals("31") && 
		  (mth.equals("4") || mth .equals("6") || mth.equals("9") ||
		          mth.equals("11") || mth.equals("04") || mth .equals("06") ||
		          mth.equals("09"));
	}
	
	public static void main(String[] args) 
	{
		// TODO Auto-generated method stub
		String CurLine = ""; // Line read from standard in

		System.out.println("Enter a line of text (type 'quit' to exit): ");
		InputStreamReader converter = new InputStreamReader(System.in);
		BufferedReader in = new BufferedReader(converter);
		Command cmd = new Command();
		while (!(CurLine.equals("quit"))) {
			try {
				CurLine = in.readLine();
				
				String input = parseDate(CurLine, cmd);
				System.out.println("Input "+ input);
				System.out.println("End Date" + cmd.getEndDate());
				System.out.println("Start Date" + cmd.getStartDate());
				//System.out.println(dateResult[1]);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvalidUserInput e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
