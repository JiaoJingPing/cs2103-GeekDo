/**
 * AlertWindow is the class that load what tasks are overdue in x days
 * and display to user a new window if the user click on the dialog when
 * Geekdo launch
 * 
 * @author      Steve Ng 
 * @version     0.2                                   
 * @since       2010-10-30    
 * 
 * Note to Developers: Edit this class to add more functionalities such as 
 * completing a task or deleting a task from this dialog.
 * 
 */
package GUI;

import java.util.Calendar;
import logic.Controller;
import objects.Result;

import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.wb.swt.SWTResourceManager;


public class AlertWindow extends Composite {

	private static Shell alertWindowDialog;
	private static int noOfTasksInAlertWindow = 0;
	
	//variable that alertWindow check before it open itself
	private static boolean canDialogOpen = false;
	
	//variables for the length and height of the dialog
	private static final int X_LENGTH_OF_GUI = 310;
	private static final int Y_LENGTH_OF_GUI = 318;

	// variables for draganddrop capabilities of the composite
	private boolean isMouseNotClicked = true;
	private Point locationOfBeforeClicking;
	
	// Final variables for RGB colors yellow for tasks that are not overdue
	private static final int YELLOW_R = 255;
	private static final int YELLOW_G = 247;
	private static final int YELLOW_B = 153;
	
	//year stored in backend to indicate no year set
	private static final int NO_YEAR_SET = 3000;
	
	public AlertWindow(Composite parent, int style) {
		super(parent, style);

		alertWindowDialog = new Shell(getDisplay(), SWT.NONE);
		alertWindowDialog.setSize(X_LENGTH_OF_GUI,Y_LENGTH_OF_GUI);
		alertWindowDialog.setBackgroundMode(SWT.INHERIT_DEFAULT);
		alertWindowDialog.setBackgroundImage(SWTResourceManager.getImage(UserInterface.class, "/GUI/Alertwindow_background.png"));
		
		createTopCompositeAndItsControls();
		createMidCompositeAndItsControls();
		
		if(canDialogOpen)
		{
			alertWindowDialog.pack();
			alertWindowDialog.open();
		}
	}
	
	// #############################################################################################
	// Second level abstraction
	// #############################################################################################
	
	private void createTopCompositeAndItsControls() {
		
		//creation of topcomposite
		Composite topComposite = new Composite(alertWindowDialog, SWT.NONE);
		topComposite.setBounds(0, 0, 308, 50);
		topComposite.setBackgroundMode(SWT.INHERIT_DEFAULT);
		createDragCapabilities(topComposite);
		
		//creation of close button
		ToolBar toolBarForCloseButton = new ToolBar(topComposite, SWT.FLAT);
		toolBarForCloseButton.setBackground(SWTResourceManager.getColor(SWT.TRANSPARENT));
		toolBarForCloseButton.setBounds(X_LENGTH_OF_GUI-50,7, 40, 40);
		ToolItem buttonClose = new ToolItem(toolBarForCloseButton, SWT.NONE);
		buttonClose.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				alertWindowDialog.close();
			}
		});
		buttonClose.setImage(SWTResourceManager.getImage(UserInterface.class, "/GUI/button_close.png"));
		
		//creation of alert window dialog 
		Label lblTitle = new Label(topComposite, SWT.NONE);
		lblTitle.setFont(SWTResourceManager.getFont("Segoe UI Semibold", 13, SWT.NORMAL));
		lblTitle.setForeground(SWTResourceManager.getColor(255,255,0));
		lblTitle.setBounds(10, 14, 215, 25);
		lblTitle.setText("Tasks that are due in "+UserInterface.getNoOfDaysForAlertReminder() + " days");
		createDragCapabilities(lblTitle);
	}

	private void createMidCompositeAndItsControls() {
		
		noOfTasksInAlertWindow =0;
		
		//creation of the scrolledComposite
		ScrolledComposite midScrolledComposite = new ScrolledComposite(alertWindowDialog, SWT.V_SCROLL);
		midScrolledComposite.setBounds(0, 46, X_LENGTH_OF_GUI, Y_LENGTH_OF_GUI-46);
		midScrolledComposite.setExpandHorizontal(true);
		midScrolledComposite.setExpandVertical(true);
		
		//creation of the main composite inside scrolled composite
		Composite midComposite = new Composite(midScrolledComposite, SWT.NONE);
		midComposite.setBackgroundImage(SWTResourceManager.getImage(UserInterface.class, "/GUI/GUI_background.png"));
		midComposite.setBackgroundMode(SWT.INHERIT_DEFAULT);
		
		//creation of the "Task Description" title
		Label lblTaskDescription = new Label(midComposite, SWT.NONE);
		lblTaskDescription.setFont(SWTResourceManager.getFont("Segoe UI Semibold", 11, SWT.NORMAL));
		lblTaskDescription.setForeground(SWTResourceManager.getColor(YELLOW_R,YELLOW_G,YELLOW_B));
		lblTaskDescription.setBounds(10, 13, 115, 20);
		lblTaskDescription.setText("Task Description");
		
		//creation of the "End Date" title
		Label lblEndDate = new Label(midComposite, SWT.NONE);
		lblEndDate.setFont(SWTResourceManager.getFont("Segoe UI Semibold", 11, SWT.NORMAL));
		lblEndDate.setForeground(SWTResourceManager.getColor(YELLOW_R ,YELLOW_G ,YELLOW_B ));
		lblEndDate.setBounds(203, 13, 104, 20);
		lblEndDate.setText("End Date");
			
		loadAllAlertTasks(midComposite);
		
		midScrolledComposite.setContent(midComposite);
		midScrolledComposite.setMinSize(midComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
	}

	// #############################################################################################
	// Third level abstraction
	// #############################################################################################
	
	/**
	 * Method allow the composite to be with drag capabilities
	 * @param composite
	 */
	private void createDragCapabilities(final Control composite) {
		composite.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				isMouseNotClicked = false;
				locationOfBeforeClicking = getDisplay().getCursorLocation();
			}

			@Override
			public void mouseUp(MouseEvent e) {
				isMouseNotClicked = true;
			}
		});

		composite.addMouseMoveListener(new MouseMoveListener() {
			@Override
			public void mouseMove(MouseEvent arg0) {
				if (isMouseNotClicked == false) {
					
					Point locationOfNewWindow = getDisplay().getCursorLocation();
					Point shellLocation = getDisplay().getActiveShell().getLocation();
					
					locationOfNewWindow = new Point(
							shellLocation.x
									+ (locationOfNewWindow.x - locationOfBeforeClicking.x),
							shellLocation.y
									+ (locationOfNewWindow.y - locationOfBeforeClicking.y));
					getDisplay().getActiveShell().setLocation(locationOfNewWindow);
					locationOfBeforeClicking = getDisplay().getCursorLocation();
				}
			}
		});
	}

	private void loadAllAlertTasks(Composite midComposite) {
		
		Result allOpenTasks = Controller.getAllOpenTasks();
		
		int userDefinedDaysForAlert = UserInterface.getNoOfDaysForAlertReminder();
		
		Calendar todayDate = Calendar.getInstance();
		int todayDateMonth = todayDate.get(Calendar.MONTH);
		int todayDateDay = todayDate.get(Calendar.DATE);
		int todayDateYear = todayDate.get(Calendar.YEAR);
		todayDate.clear();
		todayDate.set(todayDateYear,todayDateMonth,todayDateDay);
		
		populateTaskList(midComposite, allOpenTasks,
				userDefinedDaysForAlert, todayDate, todayDateYear);
	}

	// #############################################################################################
	// Fourth level abstraction
	// #############################################################################################
			
	/**
	 * Method loop through the task list and check for tasks with due date
	 * within the range to be displayed into the alert window
	 */
	private void populateTaskList(Composite midComposite,
			Result allOpenTasks, int userDefinedDaysForAlert,
			Calendar todayDate, int todayDateYear) {
		
		for(int i =0;i<allOpenTasks.getData().size();i++)
		{
			Calendar taskEndDate = allOpenTasks.getData().get(i).getEndDate();
			int taskEndDateMonth = taskEndDate.get(Calendar.MONTH);
			int taskEndDateDay = taskEndDate.get(Calendar.DATE);
			int taskEndDateYear = taskEndDate.get(Calendar.YEAR);
			
			if(taskEndDateYear ==NO_YEAR_SET)
				taskEndDateYear=todayDateYear;
			
			taskEndDate.clear();
			taskEndDate.set(taskEndDateYear,taskEndDateMonth,taskEndDateDay);
			
			int dayBetween = daysBetween(todayDate,taskEndDate);
			
			if(dayBetween <= userDefinedDaysForAlert && dayBetween >-1)
			{
					//creation of the task description label
					Label lblTaskDescription = new Label(midComposite, SWT.NONE);
					lblTaskDescription.setBounds(10, 40 + noOfTasksInAlertWindow*30, 180, 20);
					lblTaskDescription.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.NORMAL));
					lblTaskDescription.setForeground(SWTResourceManager.getColor(YELLOW_R ,YELLOW_G ,YELLOW_B ));
					lblTaskDescription.setText(allOpenTasks.getData().get(i).getDetails());

					//creation of the end date label
					Label lblEndDate = new Label(midComposite, SWT.NONE);
					lblEndDate.setBounds(203,40 + noOfTasksInAlertWindow*30, 65, 20);
					lblEndDate.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.NORMAL));
					lblEndDate.setForeground(SWTResourceManager.getColor(YELLOW_R ,YELLOW_G ,YELLOW_B ));
					
					setEndDateLabelBasedOnDayBetween(lblEndDate,
							taskEndDateMonth, taskEndDateDay, dayBetween);
					
					noOfTasksInAlertWindow++;

			}
					
		}
	}

	/**
	 * Method check the day between for todayDate and the task end date
	 * and set the end date label accordingly to the day between
	 * 
	 */
	private void setEndDateLabelBasedOnDayBetween(Label lblEndDate,
			int taskEndDateMonth, int taskEndDateDay, int dayBetween) {
		if(dayBetween ==0)
			lblEndDate.setText("Today");
		else if(dayBetween ==1)
			lblEndDate.setText("Tomorrow");
		else if(dayBetween < 8)
			lblEndDate.setText("In "+dayBetween+" days");
		else
			lblEndDate.setText(taskEndDateDay + "/" + (taskEndDateMonth+1));
	}
	
	/**
	 * Method calculate the daysBetween 2 given date
	 *  
	 * @return -1 if todayDate is after taskEndDate
	 * @return else return the number of days differences between 2 given date
	 * 
	 */
	private int daysBetween(Calendar todayDate, Calendar taskEndDate){
	
		    if (todayDate.after(taskEndDate)) {  // swap dates so that d1 is start and d2 is end
		        return -1;
		    }
	    return (int) ((taskEndDate.getTime().getTime() - todayDate.getTime().getTime())/(1000*60*60*24));
	}
	
	// #############################################################################################
	// Package GUI API
	// #############################################################################################
	
	public static void setSwitchToOpen(boolean setSwitchToOpen)
	{
		canDialogOpen = setSwitchToOpen;
	}
	
	
	public static int getNoOfTasksInAlertWindow()
	{
		return noOfTasksInAlertWindow;
	}
}
