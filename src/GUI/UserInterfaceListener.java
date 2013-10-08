/**
 * @author      Steve Ng 
 * @version     0.2                                   
 * @since       2010-10-30    
 * 
 * UserInterface Listener as the name suggest is the 
 * listener class for UserInterface's controls.
 * 
 * The rationale for separating some listener out to here is
 * to ease the length of UserInterface and for easy maintenance 
 * should the developers want to edit how the listeners work. 
 *     
 */
package GUI;

import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import logic.Controller;

import objects.Result;
import objects.Event;

import test.InvalidUserInput;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.forms.widgets.FormToolkit;


public class UserInterfaceListener extends Composite {

	private final FormToolkit toolkit = new FormToolkit(Display.getCurrent());

	
	public UserInterfaceListener(Composite parent, int style) {
		
		super(parent, style);
		addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				toolkit.dispose();
			}
		});
		toolkit.adapt(this);
		toolkit.paintBordersFor(this);
		
	}
	
	// #############################################################################################
	// Public Listeners Methods
	// #############################################################################################

	/**
	 * Methods is for the task's end date label listener label. Listener will
	 * 1) Check the input date is valid
	 *  1.1) If valid, update the backend with the valid date
	 * 2) Show user an editable form of date when user click on it
	 */
	public static void addDateModifyListener(final StyledText[] openTaskEndDate, final int i, final int taskID,final String endDateStandForm, final String endDateNiceForm) {

		openTaskEndDate[i].addFocusListener(new FocusListener() {
			
			@Override
			//user clicked on the date label
			public void focusGained(FocusEvent arg0) {
				Calendar taskDate;

				if (UserInterface.isShowEndDAte() == false) // show start date
					taskDate = Controller.searchForOpenTaskStartDate(taskID);
				else //search for end date
					taskDate = Controller.searchForOpenTaskEndDate(taskID);
					
				String dateFormToShow = UserInterfaceListener.datePopulator(taskDate);
				
				openTaskEndDate[i].setText(dateFormToShow);
			}

			@Override
			//user click on some other things after clicking the label
			public void focusLost(FocusEvent arg0) {

				editDateInTask(openTaskEndDate, i, taskID);			
				
				Calendar taskDate;

				if (UserInterface.isShowEndDAte() == false)
					taskDate = Controller.searchForOpenTaskStartDate(taskID);
				else
					taskDate = Controller.searchForOpenTaskEndDate(taskID);

				String endDateFormToShow = UserInterfaceListener.endDatePopulatorInNiceForm(taskDate);
				openTaskEndDate[i].setText(endDateFormToShow);
			}

		});

		openTaskEndDate[i].addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {			
				//user press enter on the end date
				if (e.keyCode == SWT.CR) {

					editDateInTask(openTaskEndDate, i, taskID);

					Calendar taskDate;

					if (UserInterface.isShowEndDAte() == false)
						taskDate = Controller.searchForOpenTaskStartDate(taskID);
					else
						taskDate = Controller.searchForOpenTaskEndDate(taskID);
		
					String endDateFormToShow = UserInterfaceListener.endDatePopulatorInNiceForm(taskDate);
					
					UserInterface.getFocusToMainWindow();
					
					openTaskEndDate[i].setText(endDateFormToShow);
				}
			}
		});
	}
		
	public static void addTaskDescModifyListener(
			final StyledText[] openTaskDescription, final int i,
			final int taskID) {
		openTaskDescription[i].addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				Controller.editTaskDescription(taskID, openTaskDescription[i].getText().trim());
			}
		});

		openTaskDescription[i].addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.CR) {
					
					StringTokenizer removeEnterKey = new StringTokenizer(openTaskDescription[i].getText(), "\r\n");
					String newTaskDescription = "";

					while (removeEnterKey.hasMoreTokens())
					{
						if(newTaskDescription.length()==0)
							newTaskDescription = removeEnterKey.nextToken();
						else
							newTaskDescription = newTaskDescription + " "+removeEnterKey.nextToken();
					}
					
					Controller.editTaskDescription(taskID,newTaskDescription.trim());
					openTaskDescription[i].setText(newTaskDescription);
				}

			}
		});
	}

	public static void addPriorityModifyListener(final ToolBar[] openPriority,
			final ToolItem[] openPriorityIcon, final int i, final int taskID,
			final int priority) {

		final int NO_PRIORITY = 0;
		final int YES_PRIORITY = 1;

		openPriorityIcon[i].addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (priority == YES_PRIORITY) {
					Controller.editTaskPriority(taskID, NO_PRIORITY);
					openPriority[i].setVisible(false);
				} else {
					Controller.editTaskPriority(taskID, YES_PRIORITY);
					openPriority[i].setVisible(true);
				}
				UserInterface.refreshTabList(null, null);
			}
		});

	}

	/**
	 * Listener for composite of each task detail
	 * Listener allows priority,complete,delete button to show when moused-over
	 */
	public static void addCompositeMouseOverListener(
			Composite[] openTaskEntireDetail,
			final ToolBar[] completeIncomplete, final ToolBar[] completeDelete,
			final int i, final Control[] openPriority, final String taskType) {
		openTaskEntireDetail[i].addMouseTrackListener(new MouseTrackListener() {
			
			boolean priorityIsOne = false;

			@Override
			public void mouseEnter(MouseEvent arg0) {
				completeIncomplete[i].setVisible(true);
				completeDelete[i].setVisible(true);
				if (taskType.equals("open")) {
					if (openPriority[i].isVisible() == true)
						priorityIsOne = true;
					else
						openPriority[i].setVisible(true);
				}
			}

			@Override
			public void mouseExit(MouseEvent arg0) {
				completeIncomplete[i].setVisible(false);
				completeDelete[i].setVisible(false);

				if (taskType.equals("open")) {
					if (priorityIsOne == false)
						openPriority[i].setVisible(false);
				}
			}

			@Override
			public void mouseHover(MouseEvent arg0) {
			}

		});

	}

	public static void addToolBarMouseOverListener(
			ToolBar[] completeIncomplete, final ToolBar[] openComplete2,
			final ToolBar[] openDelete, final int i,
			final Control[] openPriority, final String taskType) {

		completeIncomplete[i].addMouseTrackListener(new MouseTrackListener() {

			boolean priorityIsOne = false;

			@Override
			public void mouseEnter(MouseEvent arg0) {
				openComplete2[i].setVisible(true);
				openDelete[i].setVisible(true);
				if (taskType.equals("open")) {
					if (openPriority[i].isVisible() == true)
						priorityIsOne = true;
					else
						openPriority[i].setVisible(true);
				}
			}
			@Override
			public void mouseExit(MouseEvent arg0) {
				openComplete2[i].setVisible(false);
				openDelete[i].setVisible(false);

				if (taskType.equals("open")) {
					if (priorityIsOne == false)
						openPriority[i].setVisible(false);
				}
			}
			@Override
			public void mouseHover(MouseEvent arg0) {}
		});

	}

	public static void addStyledTextMouseOverListener(
			StyledText[] openTaskDescription, final ToolBar[] openComplete,
			final ToolBar[] openDelete, final int i,
			final ToolBar[] openPriority, final String taskType) {
		openTaskDescription[i].addMouseTrackListener(new MouseTrackListener() {

			boolean priorityIsOne = false;

			@Override
			public void mouseEnter(MouseEvent arg0) {
				openComplete[i].setVisible(true);
				openDelete[i].setVisible(true);
				if (taskType.equals("open")) {
					if (openPriority[i].isVisible() == true)
						priorityIsOne = true;
					else
						openPriority[i].setVisible(true);
				}
			}

			@Override
			public void mouseExit(MouseEvent arg0) {
				openComplete[i].setVisible(false);
				openDelete[i].setVisible(false);

				if (taskType.equals("open")) {
					if (priorityIsOne == false)
						openPriority[i].setVisible(false);
				}
			}

			@Override
			public void mouseHover(MouseEvent arg0) {
			}

		});

	}

	public static void addToolBarMouseOverListener(
			final ToolBar[] openPriority, final ToolBar[] openComplete,
			final ToolBar[] openDelete, final int i, final Event event,
			final String taskType) {

		openPriority[i].addMouseTrackListener(new MouseTrackListener() {
			@Override
			public void mouseEnter(MouseEvent arg0) {
				openComplete[i].setVisible(true);
				openDelete[i].setVisible(true);
				openPriority[i].setVisible(true);
			}

			@Override
			public void mouseExit(MouseEvent arg0) {
				openComplete[i].setVisible(false);
				openDelete[i].setVisible(false);

				if (event.getPriority() != 1)
					openPriority[i].setVisible(false);

			}

			@Override
			public void mouseHover(MouseEvent arg0) {
			}

		});

	}

	public static void addLabelMouseOverListener(final CLabel[] openTaskID,
			final ToolBar[] openComplete, final ToolBar[] openDelete,
			final int i, final Control[] openPriority, final String taskType) {

		openTaskID[i].addMouseTrackListener(new MouseTrackListener() {

			boolean priorityIsOne = false;

			@Override
			public void mouseEnter(MouseEvent arg0) {
				openComplete[i].setVisible(true);
				openDelete[i].setVisible(true);
				if (taskType.equals("open")) {
					if (openPriority[i].isVisible() == true)
						priorityIsOne = true;
					else
						openPriority[i].setVisible(true);
				}
			}

			@Override
			public void mouseExit(MouseEvent arg0) {
				openComplete[i].setVisible(false);
				openDelete[i].setVisible(false);

				if (taskType.equals("open")) {
					if (priorityIsOne == false)
						openPriority[i].setVisible(false);
				}

			}

			@Override
			public void mouseHover(MouseEvent arg0) {
			}

		});
	}

	public static void addCompleteMouseListener(ToolItem[] openCompleteIcon,
			int i, final int id) {
		openCompleteIcon[i].addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Controller.markTaskAsComplete(id);
				UserInterface.refreshTabList(null, null);
			}

		});

	}

	public static void addOpenDeleteMouseListener(ToolItem[] openDeleteIcon,
			int i, final int id) {
		openDeleteIcon[i].addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Controller.deleteOpenTask(id);
				UserInterface.refreshTabList(null, null);
			}
		});

	}

	public static void addcompleteDeleteMouseListener(
			ToolItem[] completeDeleteIcon, int i, final int id) {
		completeDeleteIcon[i].addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Controller.deleteCompleteTask(id);
				UserInterface.refreshTabList(null, null);
			}
		});

	}

	public static void addIncompleteMouseListener(
			ToolItem[] completeIncompleteIcon, int i, final int id) {
		completeIncompleteIcon[i].addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Controller.markTaskAsInComplete(id);
				UserInterface.refreshTabList(null, null);
			}

		});

	}

	public static void addDeleteALlOpenTaskListener(ToolItem deleteAllOpenTask) {

		deleteAllOpenTask.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				confirmationWindow("Open");
			}
		});
	}

	public static void addDeleteAllCompleteTaskListener(
			ToolItem deleteAllCompleteTask) {

		deleteAllCompleteTask.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				confirmationWindow("Complete");

			}
		});
	}

	// #############################################################################################
	// Private Methods
	// #############################################################################################
	
	private static void confirmationWindow(final String deleteAllType) {

		Shell s = new Shell(Display.getCurrent());
		MessageBox messageBox = new MessageBox(s, SWT.ICON_QUESTION | SWT.YES
				| SWT.NO);

		messageBox.setText("Delete all tasks? ");

		if (deleteAllType.equals("Open"))
			messageBox.setMessage("Delete all open tasks?");
		else if (deleteAllType.equals("Complete"))
			messageBox.setMessage("Delete all completed tasks?");

		int response = messageBox.open();
		if (response == SWT.YES) {
			if (deleteAllType.equals("Open")) {
				
				deleteOpenTasks();
			} else if (deleteAllType.equals("Complete")) {
				deleteCompletedTasks();
			}
			UserInterface.refreshTabList(null, null);
		}

	}

	private static void deleteCompletedTasks() {
		if (UserInterface.getSelectedTab().equals("All"))
			Controller.deleteAllCompleteTask();
		else
			Controller.deleteAllCompletedTaskOfTag(UserInterface
					.getSelectedTab());
	}

	private static void editDateInTask(final StyledText[] openTaskEndDate,
			final int i, final int taskID) {
		
		Calendar today = Calendar.getInstance();
		int yearOfEvent = today.get(Calendar.YEAR);
	
		//user decided to set end date as nothing
		if (openTaskEndDate[i].getText().trim().length() == 0) { 
			if (UserInterface.isShowEndDAte() == false)
				Controller.editTaskStartDate(taskID,0,0,0);
			else
				Controller.editTaskEndDate(taskID,0,0,0);
		} 
		else {
			
			checkDateValidAndEdit(openTaskEndDate, i, taskID, yearOfEvent);
			
		}

	}

	private static void checkDateValidAndEdit(
			final StyledText[] openTaskEndDate, final int i, final int taskID,
			int yearOfEvent)  {
		
		
		String userKeyedDate = removalOfEnter(openTaskEndDate, i);
		Date dateReal = null;
		
		StringTokenizer splitDate = new StringTokenizer(userKeyedDate, "/");
		int dayOfEvent=0;
		int monthOfEvent=0;
		try {
	
			dayOfEvent = Integer.parseInt(splitDate.nextToken());
			monthOfEvent = Integer.parseInt(splitDate.nextToken());
			
			yearOfEvent = Integer.parseInt(splitDate.nextToken());

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			sdf.setLenient(false);
			String concatDate = yearOfEvent + "-" + monthOfEvent + "-"
					+ dayOfEvent;
			dateReal = sdf.parse(concatDate, new ParsePosition(0));

			if (yearOfEvent > 1 && yearOfEvent <= 99)
				yearOfEvent = yearOfEvent + 2000;
			else if (yearOfEvent < 1000 || yearOfEvent > 9999)
				dateReal = null;
		} catch (NoSuchElementException e) {
			//no year set.
			dateReal = setEndDateWithoutYear(yearOfEvent, dayOfEvent,
					monthOfEvent);
		} catch (NumberFormatException e) {
			dateReal = setEndDateWithoutYear(yearOfEvent, dayOfEvent,
					monthOfEvent);
		} catch (Exception e){
			//no end date set do, hence do nothing.
		}
		monthOfEvent--;

		//is a valid task
		if (dateReal != null) {
			editTaskDate(i, taskID, yearOfEvent, dayOfEvent, monthOfEvent);
		}
	}

	private static Date setEndDateWithoutYear(int yearOfEvent, int dayOfEvent,
			int monthOfEvent) {
		
		try{
		
		Date dateReal;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		sdf.setLenient(false);
		String concatDate = yearOfEvent + "-" + monthOfEvent + "-"
				+ dayOfEvent;
		dateReal = sdf.parse(concatDate);
		return dateReal;
		}catch(ParseException e){ //end date format is wrong
			return null;
		}
	}

	private static String removalOfEnter(final StyledText[] openTaskEndDate,
			final int i) {
		StringTokenizer removalOfEnter = new StringTokenizer(openTaskEndDate[i].getText().trim(),"\r\n");
		String userKeyedDate = "";
		while(removalOfEnter.hasMoreTokens())
			userKeyedDate = userKeyedDate + removalOfEnter.nextToken();
		return userKeyedDate;
	}

	private static void editTaskDate(final int i, final int taskID,
			int yearOfEvent, int dayOfEvent, int monthOfEvent) {
		if (UserInterface.isShowEndDAte() == false) // showing start// date
			Controller.editTaskStartDate(taskID, dayOfEvent,monthOfEvent, yearOfEvent);
		else
		{
			Controller.editTaskEndDate(taskID, dayOfEvent,
					monthOfEvent, yearOfEvent);
		
			Calendar taskEndDate = Calendar.getInstance();
			taskEndDate.clear();
			taskEndDate.set(yearOfEvent, monthOfEvent, dayOfEvent);
			
			Calendar todayDate = Calendar.getInstance();
			int todayYear = todayDate.get(Calendar.YEAR);
			int todayMonth = todayDate.get(Calendar.MONTH);
			int todayDay = todayDate.get(Calendar.DATE);
			todayDate.clear();
			todayDate.set(todayYear, todayMonth,todayDay);
			
			int dayBetween = UserInterfaceListener.daysBetween(todayDate, taskEndDate);
			
			if(dayBetween ==-1) //task over due
				UserInterface.setTaskOverDueColor(i);
			else
				UserInterface.setTaskNotOverDueColor(i);
			
			
		}
	}

	private static String datePopulator(Calendar endDate) {

		String details = "";
		Calendar taskEndDate = endDate;

		int monthOfEndDateTask = taskEndDate.get(Calendar.MONTH);
		monthOfEndDateTask++;
		int dayOfEndDateTask = taskEndDate.get(Calendar.DATE);
		int yearOfEndDateTask = taskEndDate.get(Calendar.YEAR);

		if (yearOfEndDateTask != 3000 && monthOfEndDateTask != 0) {
			if (yearOfEndDateTask != 3000)
				details = dayOfEndDateTask + "/" + monthOfEndDateTask + "/"
						+ yearOfEndDateTask;
			else
				details = dayOfEndDateTask + "/" + monthOfEndDateTask;

		}

		return details;
	}

	private static String endDatePopulatorInNiceForm(Calendar endDate) {

		String details = "";
		String[] month = { "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul",
				"Aug", "Sept", "Oct", "Nov", "Dec" };

		// seperate taskEndDate out into different segment
		Calendar taskEndDate = endDate;
		int monthOfEndDateTask = taskEndDate.get(Calendar.MONTH);
		int dayOfEndDateTask = taskEndDate.get(Calendar.DATE);
		int yearOfEndDateTask = taskEndDate.get(Calendar.YEAR);
		taskEndDate.clear();
		taskEndDate.set(yearOfEndDateTask, monthOfEndDateTask, dayOfEndDateTask);

		// seperate today date out into different segment
		Calendar todayDate = Calendar.getInstance();
		int monthOfToday = todayDate.get(Calendar.MONTH);
		int dayOfToday = todayDate.get(Calendar.DATE);
		int yearOfToday = todayDate.get(Calendar.YEAR);
		todayDate.clear();
		todayDate.set(yearOfToday, monthOfToday, dayOfToday);

		if (yearOfEndDateTask != 3000)// && monthOfEndDateTask!=0) //no end date
										// is set
		{
			int daysBetween = daysBetween(todayDate, taskEndDate);
			if (daysBetween < 8 && daysBetween >= 0) {
				if (daysBetween == 0) {
					details = "Today";
				} else if (daysBetween == 1)
					details = "Tomorrow";
				else
					details = "In " + daysBetween + " days";
			} else if (yearOfEndDateTask == yearOfToday)
				details = dayOfEndDateTask + " " +month[monthOfEndDateTask];
			else
				details = dayOfEndDateTask + " " +month[monthOfEndDateTask] + " "+ yearOfEndDateTask;

		}
		return details;
	}

	private static int daysBetween(Calendar todayDate, Calendar taskEndDate) {

		if (todayDate.equals(taskEndDate))
			return 0;

		if (todayDate.after(taskEndDate)) {
			return -1;
		}
		return (int) ((taskEndDate.getTime().getTime() - todayDate.getTime()
				.getTime()) / (1000 * 60 * 60 * 24));
	}

	private static void deleteOpenTasks() {

		if(UserInterface.isTaskListSearchResult())
		{
			String command = UserInterface.getSearchCommand();
			Result searchResult = null;
			try{
				searchResult = Controller.processText(command);
			}catch(InvalidUserInput e){
				UserInterface.setCommandLabel(e.getError());
			}
			
			if(searchResult!=null)
			{
				for(int i =0;i<searchResult.getData().size();i++)
					Controller.deleteOpenTask(searchResult.getData().get(i).getId());
			}
		}	
		else if (UserInterface.getSelectedTab().equals("All"))
			Controller.deleteAllOpenTask();
		else
			Controller.deleteAllOpenTaskOfTag(UserInterface
					.getSelectedTab());
	}

}
