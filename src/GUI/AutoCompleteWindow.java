/**
 * AutoComplete class is the dialog that pops out when user typed in special
 * commands like "edit/-e/delete/-d/complete/-c/incomplete/-inc from the command 
 * line interface.
 * 
 * @author      Steve Ng 
 * @CoAuthor	Jiao Jing Ping
 * @version     0.2                                   
 * @since       2010-10-30    
 * 
 */

package GUI;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.StringTokenizer;

import logic.Controller;

import objects.Result;

import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wb.swt.SWTResourceManager;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;

public class AutoCompleteWindow extends Composite {

	private boolean isOpen = false;
	private static Shell autoCompleteDialog;
	private Composite autoCompleteComposite;
	private static ArrayList<CLabel> taskDescription = new ArrayList<CLabel>();
	private static ArrayList<CLabel> taskEndDate = new ArrayList<CLabel>();
	private KeyAdapter autoCompleteWindowKeyAdapter;
	private ScrolledComposite autoCompleteScrolledComposite;
	private static Label completedTabLabel;
	
	//Final variable for the text colors
	private final int HIGHLIGHTED_TEXT_R = 239;
	private final int HIGHLIGHTED_TEXT_G = 171;
	private final int HIGHLIGHTED_TEXT_B = 8;
	private final int NOT_HIGHLIGHTED_TEXT_R = 255;
	private final int NOT_HIGHLIGHTED_TEXT_G = 247;
	private final int NOT_HIGHLIGHTED_TEXT_B = 153;
	
	//Hashmap variables for mapping each temp task ID like 1. 2. with 
	//the real task ID at backend
	private static HashMap<Integer, Integer> realTaskIDtoTempTaskID;
	private static HashMap<Integer, Integer> tempTaskIDtoRealTaskID;
	
	//year stored in backend to indicate no year set
	private static final int NO_YEAR_SET = 3000;
	
	//variables for the length and height of the dialog
	private static final int X_LENGTH_OF_GUI = 500;
	private static final int Y_LENGTH_OF_GUI = 106;
	
	private static final int MAX_POSSIBLE_SIZE = 100;
	private static final int PRIORITY_EXIST = 1;
	private static final int MAX_TEMP_TASK_ID = 1000;
	
	//variables for autComplete window scrolling
	private final int START_SELECETD_INDEX = -1;
	private final int START_Y_INDEX = -20;
	private final int GAP_BETWEEN_INDEX = 20;
	
	public AutoCompleteWindow(Composite parent, int style, int x_location_of_GUI, int y_location_of_GUI,
			final Result searchList, final StyledText commandLineInterface,
			final String autoCompleteType) {

		super(parent, style);
		
		clearAllTaskDescriptionText();
		isOpen = true;

		realTaskIDtoTempTaskID = UserInterface.getopenRealTaskIDtoTempTaskID();
		
		mapTaskIdAccordingToAutoCompleteType(autoCompleteType);

		autoCompleteDialog = new Shell(getDisplay(), SWT.NO_TRIM);
		autoCompleteDialog.setSize(X_LENGTH_OF_GUI, Y_LENGTH_OF_GUI);
		autoCompleteDialog.setBackgroundImage(SWTResourceManager.getImage(UserInterface.class, "/GUI/GUI_background.png"));
		autoCompleteDialog.setBackgroundMode(SWT.INHERIT_DEFAULT);
		
		createContentForDialog(searchList, commandLineInterface,
				autoCompleteType);

		autoCompleteDialog.pack();
		autoCompleteDialog.open();
		autoCompleteDialog.setLocation(x_location_of_GUI + 20, y_location_of_GUI + 61);

	}

	// #############################################################################################
	// Second level abstraction
	// #############################################################################################

	private void clearAllTaskDescriptionText() {

		for (int i = 0; i < taskDescription.size(); i++) {
			taskDescription.get(i).dispose();
		}
		for (int i = 0; i < taskEndDate.size(); i++) {
			taskEndDate.get(i).dispose();
		}
		taskDescription.clear();
		taskEndDate.clear();
		
		if(completedTabLabel!=null)
		{
			completedTabLabel.dispose();
		}
	}
	
	private void mapTaskIdAccordingToAutoCompleteType(
			final String autoCompleteType) {
		
		if (autoCompleteType.equalsIgnoreCase("delete") || autoCompleteType.equalsIgnoreCase("-d")) {
			
			HashMap<Integer, Integer> completedTempTaskIDtoRealTaskID = UserInterface.getcompletedTempTaskIDtoRealTaskID();
			HashMap<Integer, Integer> openTempTaskIDtoRealTaskID = UserInterface.getopenTempTaskIDtoRealTaskID();
	
			for (int i = 1; i < completedTempTaskIDtoRealTaskID.size() + 1; i++) {
				openTempTaskIDtoRealTaskID.put(i + MAX_TEMP_TASK_ID,
						completedTempTaskIDtoRealTaskID.get(i));
				realTaskIDtoTempTaskID.put(completedTempTaskIDtoRealTaskID.get(i),
						i + MAX_TEMP_TASK_ID);
			}

			tempTaskIDtoRealTaskID = openTempTaskIDtoRealTaskID;

		} else if (autoCompleteType.equalsIgnoreCase("Incomplete") || autoCompleteType.equalsIgnoreCase("-inc") ) {
			tempTaskIDtoRealTaskID = UserInterface
					.getcompletedTempTaskIDtoRealTaskID();
			realTaskIDtoTempTaskID = UserInterface
					.getcompletedRealTaskIDtoTempTaskID();
		} else {
			tempTaskIDtoRealTaskID = UserInterface
					.getopenTempTaskIDtoRealTaskID();
			realTaskIDtoTempTaskID = UserInterface
					.getopenRealTaskIDtoTempTaskID();
		}
	}
	
	private void createContentForDialog(final Result searchList,
			final StyledText commandLineInterface, final String autoCompleteType) {
		autoCompleteScrolledComposite = new ScrolledComposite(autoCompleteDialog, SWT.V_SCROLL);
		autoCompleteScrolledComposite.addFocusListener(new FocusAdapter() {
		@Override
		public void focusLost(FocusEvent e) {
				isOpen = false;
				disposeWindow();
			}
		});

		addKeyEnterKeyListener(commandLineInterface, autoCompleteType);
		autoCompleteScrolledComposite.addKeyListener(autoCompleteWindowKeyAdapter);
		autoCompleteScrolledComposite.setBounds(0, 0, 383, 85);
		autoCompleteScrolledComposite.setExpandHorizontal(true);
		autoCompleteScrolledComposite.setExpandVertical(true);

		autoCompleteComposite = new Composite(autoCompleteScrolledComposite, SWT.NONE);
		autoCompleteComposite.setBackgroundImage(SWTResourceManager.getImage(UserInterface.class, "/GUI/GUI_background.png"));
		autoCompleteComposite.setBackgroundMode(SWT.INHERIT_DEFAULT);

		populateWindowWithTask(searchList, commandLineInterface,autoCompleteType);
		
		autoCompleteScrolledComposite.setContent(autoCompleteComposite);
		autoCompleteScrolledComposite.setMinSize(autoCompleteComposite.computeSize(SWT.DEFAULT,SWT.DEFAULT));
	}
	
	// #############################################################################################
	// Third level abstraction
	// #############################################################################################
	
	private void addKeyEnterKeyListener(final StyledText commandLineInterface,
			final String autoCompleteType) {
		
		autoCompleteWindowKeyAdapter = new KeyAdapter() {
			
			int yIndex = START_Y_INDEX;
			int currentSelection = START_SELECETD_INDEX;
	
			@Override
			public void keyPressed(KeyEvent e) {

				if (e.keyCode == SWT.TAB || e.keyCode == SWT.CR) {
					loadSelectedTaskIntoCommandLine(commandLineInterface,autoCompleteType);
				}
				else if (e.keyCode == SWT.ARROW_DOWN) {
					scrollSelectedTaskDown();
				}
				else if (e.keyCode == SWT.ARROW_UP) {
					scrollSelectedTaskUp();
				}

				Character userKeyedCharacter = e.character;
				if (!Character.isISOControl(userKeyedCharacter))
					commandLineInterface.setText(commandLineInterface.getText()+ userKeyedCharacter);
				else if (e.keyCode == SWT.BS) {
					removeOneCharFromCommandLine(commandLineInterface);
				}
			}

			private void removeOneCharFromCommandLine(final StyledText commandLineInterface) {
				
				String currentText = commandLineInterface.getText();
				if (currentText.length() != 0) {
					commandLineInterface.setText(currentText.substring(0,currentText.length() - 1));
				}

				checkUserInputAndDisposeAutoCompleteIfNotCommand(commandLineInterface);
			}

			private void checkUserInputAndDisposeAutoCompleteIfNotCommand(
					final StyledText commandLineInterface) {
				String userCurrentInput = commandLineInterface.getText().trim();
				String[] splitUserInput = new String[MAX_POSSIBLE_SIZE]; 

				splitUserInput = userCurrentInput.split(" ");

				if (userCurrentInput.trim().length() != 0)
					splitUserInput = userCurrentInput.split(" ");
				else
					splitUserInput[0] = "";
				
				if (!(splitUserInput[0].equalsIgnoreCase("edit")
						|| splitUserInput[0].equalsIgnoreCase("-e")
						|| splitUserInput[0].equalsIgnoreCase("delete")
						|| splitUserInput[0].equalsIgnoreCase("-d")
						|| splitUserInput[0].equalsIgnoreCase("complete") 
						|| splitUserInput[0].equalsIgnoreCase("-c") 
						|| splitUserInput[0].equalsIgnoreCase("incomplete")
						|| splitUserInput[0].equalsIgnoreCase("-inc"))) {
					disposeWindow();
				}
			}

			private void scrollSelectedTaskUp() {
				
				if (currentSelection > 0) { //task selected currently isn't the first one
					currentSelection--;
					if (currentSelection != taskDescription.size()) {
						taskDescription.get(currentSelection + 1).setForeground(SWTResourceManager.getColor(NOT_HIGHLIGHTED_TEXT_R  ,NOT_HIGHLIGHTED_TEXT_R  ,NOT_HIGHLIGHTED_TEXT_B  ));
						taskEndDate.get(currentSelection + 1).setForeground(SWTResourceManager.getColor(NOT_HIGHLIGHTED_TEXT_R ,NOT_HIGHLIGHTED_TEXT_R ,NOT_HIGHLIGHTED_TEXT_B  ));
					}
					taskDescription.get(currentSelection).setForeground(SWTResourceManager.getColor(HIGHLIGHTED_TEXT_R ,HIGHLIGHTED_TEXT_G ,HIGHLIGHTED_TEXT_B ));
					taskEndDate.get(currentSelection).setForeground(SWTResourceManager.getColor(HIGHLIGHTED_TEXT_R,HIGHLIGHTED_TEXT_G,HIGHLIGHTED_TEXT_B ));
					
					if (yIndex > 10)
						yIndex = yIndex - GAP_BETWEEN_INDEX;
					
					autoCompleteScrolledComposite.setOrigin(10, yIndex);
				}
			}

			private void scrollSelectedTaskDown() {
				if (taskDescription.size() - 1 > currentSelection) {
					
					currentSelection++;
					
					taskDescription.get(currentSelection).setForeground(SWTResourceManager.getColor(HIGHLIGHTED_TEXT_R ,HIGHLIGHTED_TEXT_G ,HIGHLIGHTED_TEXT_B ));
					taskEndDate.get(currentSelection).setForeground(SWTResourceManager.getColor(HIGHLIGHTED_TEXT_R,HIGHLIGHTED_TEXT_G,HIGHLIGHTED_TEXT_B ));
					
					if (currentSelection > 0) {
						taskDescription.get(currentSelection-1 ).setForeground(SWTResourceManager.getColor(NOT_HIGHLIGHTED_TEXT_R  ,NOT_HIGHLIGHTED_TEXT_R  ,NOT_HIGHLIGHTED_TEXT_B  ));
						taskEndDate.get(currentSelection-1).setForeground(SWTResourceManager.getColor(NOT_HIGHLIGHTED_TEXT_R ,NOT_HIGHLIGHTED_TEXT_R ,NOT_HIGHLIGHTED_TEXT_B  ));
					}
					autoCompleteScrolledComposite.setOrigin(10, yIndex);
					yIndex = yIndex + GAP_BETWEEN_INDEX;

				}
			}

			private void loadSelectedTaskIntoCommandLine(final StyledText commandLineInterface,final String autoCompleteType) {
				
					if (currentSelection == -1)
						currentSelection = 0;
					
					int realTaskID = getRealTaskID();

					Result taskFound = searchForTask(realTaskID);

					if (autoCompleteType.equalsIgnoreCase("edit") || autoCompleteType.equalsIgnoreCase("-e")) {
						
						populateCliEditKeyCommand(commandLineInterface,autoCompleteType, taskFound);
						
					} else if (autoCompleteType.equalsIgnoreCase("Complete"))
						commandLineInterface.setText("complete "
								+ taskDescription.get(currentSelection)
										.getText());
					else if (autoCompleteType
							.equalsIgnoreCase("-c"))
						commandLineInterface.setText("-c "
								+ taskDescription.get(currentSelection)
										.getText());
					else if (autoCompleteType
							.equalsIgnoreCase("incomplete"))
						commandLineInterface.setText("incomplete "
								+ taskDescription.get(currentSelection)
										.getText());
					else if (autoCompleteType
							.equalsIgnoreCase("-inc"))
						commandLineInterface.setText("-inc "
								+ taskDescription.get(currentSelection)
										.getText());
					else if (autoCompleteType.equalsIgnoreCase("delete") || autoCompleteType.equalsIgnoreCase("-d") ) {
						
						populateCliDeleteCommand(commandLineInterface,autoCompleteType);
					}
					disposeWindow();
					UserInterface.setCommandLineInterfaceTextCursorAtEnd();

				
			}


			private Result searchForTask(int realTaskID) {
				ArrayList<String> searchKeyWord = new ArrayList<String>();
				searchKeyWord.add(Integer.toString(realTaskID));
				Result search = Controller.searchForOpenTask(
						searchKeyWord, "taskID");
				return search;
			}

			private int getRealTaskID() {
				StringTokenizer taskDescriptionTokenizer = new StringTokenizer(taskDescription.get(currentSelection).getText(),".");
				int tempTaskID = Integer.parseInt(taskDescriptionTokenizer.nextToken());

				Result openTaskNumber = Controller.getAllOpenTasks();
				if (currentSelection > openTaskNumber.getData().size())
					tempTaskID = tempTaskID + MAX_TEMP_TASK_ID;

				int realTaskID = tempTaskIDtoRealTaskID.get(tempTaskID);
				return realTaskID;
			}

			private void populateCliDeleteCommand(
					final StyledText commandLineInterface,
					final String autoCompleteType) {
				Result allOpenTasks = Controller.getAllOpenTasks();

				//user selected an open task to delete
				if (currentSelection > allOpenTasks.getData().size() - 1)
				{
					if(autoCompleteType.equalsIgnoreCase("delete"))
						commandLineInterface.setText("delete -c "
							+ taskDescription.get(currentSelection)
									.getText());
					else
						commandLineInterface.setText("-d -c "
								+ taskDescription.get(currentSelection)
										.getText());
						
				}
				else //user selected completed task to delete
				{
					if(autoCompleteType.equalsIgnoreCase("delete"))
						commandLineInterface.setText("delete -i "
							+ taskDescription.get(currentSelection)
									.getText());
					else
						commandLineInterface.setText("-d -i "
								+ taskDescription.get(currentSelection)
										.getText());
				}
			}

			private void populateCliEditKeyCommand(final StyledText commandLineInterface,final String autoCompleteType, Result search) {
				
				Calendar taskEndDate = search.getData().get(0).getEndDate();
				int dayOfEndDate = taskEndDate.get(Calendar.DATE);
				int monthOfEndDate = taskEndDate.get(Calendar.MONTH);
				monthOfEndDate++;
				int yearEndDate = taskEndDate.get(Calendar.YEAR);
				
				Calendar taskStartDate = search.getData().get(0).getStartDate();
				int dayOfStartDate = taskStartDate.get(Calendar.DATE);
				int monthOfStartDate = taskStartDate.get(Calendar.MONTH);
				monthOfStartDate++;
				int yearOfStartDate = taskStartDate.get(Calendar.YEAR);
				
				String tag = search.getData().get(0).getTag();
				String textToSet;
				
				if(autoCompleteType.equalsIgnoreCase("edit"))
					textToSet = "edit "+ taskDescription.get(currentSelection).getText();
				else
					textToSet = "-e "+ taskDescription.get(currentSelection).getText();
				
				if (yearEndDate != NO_YEAR_SET)
					textToSet = textToSet + " -ed " + dayOfEndDate
							+ "/" + monthOfEndDate + "/"
							+ yearEndDate;
				
				if( yearOfStartDate != NO_YEAR_SET )
					textToSet = textToSet + " -sd " + dayOfStartDate
						+ "/" + monthOfStartDate + "/"
						+ yearOfStartDate;

				if (!tag.equals("empty"))
					textToSet = textToSet + " -t " + tag;

				int priority = search.getData().get(0).getPriority();
				
				if (priority == PRIORITY_EXIST) //priority exist 
					textToSet = textToSet + " -p " + priority;

				commandLineInterface.setText(textToSet);
			}
	
		
		};
	}

	private boolean populateWindowWithTask(final Result searchList,final StyledText commandLineInterface,final String autoCompleteType) {
		
		boolean isTaskCompletedTask = false;
		
		for (int i = 0; i < searchList.getData().size(); i++) {
		
			int searchListTaskID = searchList.getData().get(i).getId();
			int tempTaskID = realTaskIDtoTempTaskID.get(searchListTaskID);

			//if task is a completed task, add "Completed task" headings
			isTaskCompletedTask = addCompletedTaskHeading(isTaskCompletedTask, i, tempTaskID);

			final CLabel taskDescriptionLabel = createTaskDescription(
					searchList, commandLineInterface, autoCompleteType,
					isTaskCompletedTask, i, tempTaskID);

			final CLabel taskEndDateLabel = createEndDateLabel(searchList,
					commandLineInterface, isTaskCompletedTask, i,
					taskDescriptionLabel);
			
			taskEndDate.add(taskEndDateLabel);

		}
		return isTaskCompletedTask;
	}

	// #############################################################################################
	// Forth level abstraction
	// #############################################################################################
	
	
	private CLabel createTaskDescription(final Result searchList,
			final StyledText commandLineInterface,
			final String autoCompleteType, boolean isTaskCompletedTask, int i,
			int tempTaskID) {
	
		final CLabel taskDescriptionLabel = new CLabel(autoCompleteComposite, SWT.NONE);
		taskDescriptionLabel.setFont(SWTResourceManager.getFont("Segoe UI", 8, SWT.NORMAL));
		taskDescriptionLabel.setForeground(SWTResourceManager.getColor(NOT_HIGHLIGHTED_TEXT_R ,NOT_HIGHLIGHTED_TEXT_G ,NOT_HIGHLIGHTED_TEXT_B ));
		
		if (isTaskCompletedTask)
		{
			taskDescriptionLabel.setBounds(10, 10 + i * 20 + 40, 300, 20);
			taskDescriptionLabel.setText((tempTaskID - MAX_TEMP_TASK_ID) + ". "
					+ searchList.getData().get(i).getDetails());
		}
		else
		{
			taskDescriptionLabel.setBounds(10, 10 + i * 20, 300, 20);
			taskDescriptionLabel.setText(tempTaskID + ". "
					+ searchList.getData().get(i).getDetails());
		}
					
		taskDescriptionLabel.addMouseListener(new MouseListener() {
			@Override
			public void mouseDoubleClick(MouseEvent arg0) {}

			@Override
			public void mouseDown(MouseEvent arg0) {

				StringTokenizer taskDescriptionTokenizer = new StringTokenizer(taskDescriptionLabel.getText(), ".");
				
				assert taskDescriptionTokenizer.hasMoreTokens(): "AutoComplete.java - no way the tokenizer has no token here";
				
				int tempTaskID = Integer.parseInt(taskDescriptionTokenizer.nextToken());
				int realTaskID = tempTaskIDtoRealTaskID.get(tempTaskID);

				ArrayList<String> searchKeyWord = new ArrayList<String>();
				searchKeyWord.add(Integer.toString(realTaskID));
				Result taskFound = Controller.searchForOpenTask(searchKeyWord,"taskID");

				assert checkAutoCompleteType(autoCompleteType): "AutoComplete.java - AutoCompleteType is not a valid type here!";
				
				if (autoCompleteType.equalsIgnoreCase("edit") || autoCompleteType.equalsIgnoreCase("-e") ) {

					populateEditMouseCommand(commandLineInterface,
							autoCompleteType, taskDescriptionLabel, taskFound);

				} else if (autoCompleteType.equalsIgnoreCase("Complete"))
					commandLineInterface.setText("complete "
							+ taskDescriptionLabel.getText());
				else if (autoCompleteType.equalsIgnoreCase("-c"))
					commandLineInterface.setText("-c "
							+ taskDescriptionLabel.getText());
				else if (autoCompleteType.equalsIgnoreCase("incomplete"))
					commandLineInterface.setText("incomplete "
							+ taskDescriptionLabel.getText());
				else if (autoCompleteType.equalsIgnoreCase("-inc"))
					commandLineInterface.setText("-inc "
							+ taskDescriptionLabel.getText());
				else if (autoCompleteType.equalsIgnoreCase("delete"))
					commandLineInterface.setText("delete "
							+ taskDescriptionLabel.getText());
				else if (autoCompleteType.equalsIgnoreCase("-d"))
					commandLineInterface.setText("-d "
							+ taskDescriptionLabel.getText());
				UserInterface.setCommandLineInterfaceTextCursorAtEnd();
			
			}
			
			private void populateEditMouseCommand(
					final StyledText commandLineInterface,
					final String autoCompleteType,
					final CLabel taskDescriptionLabel, Result taskFound) {
				
				//get start date of task
				Calendar taskEndDate = taskFound.getData().get(0).getEndDate();
				int dayOfEndDate = taskEndDate.get(Calendar.DATE);
				int monthOfEndDate = taskEndDate.get(Calendar.MONTH);
				int yearOfEndDate = taskEndDate.get(Calendar.YEAR);
				monthOfEndDate++;

				//get end date of task
				Calendar taskStartDate = taskFound.getData().get(0).getStartDate();
				int dayOfStartDate = taskStartDate.get(Calendar.DATE);
				int monthOfStartDate = taskStartDate.get(Calendar.MONTH);
				monthOfStartDate++;
				int yearOfStartDate = taskStartDate.get(Calendar.YEAR);
				
				//get tag of task
				String tag = taskFound.getData().get(0).getTag();
				
				String textToShowUser;

				if(autoCompleteType.equalsIgnoreCase("edit"))
					textToShowUser = "edit " + taskDescriptionLabel.getText();
				else
					textToShowUser = "-e " + taskDescriptionLabel.getText();
				
				if (yearOfEndDate != NO_YEAR_SET)
					textToShowUser = textToShowUser + " -ed " + dayOfEndDate
							+ "/" + monthOfEndDate + "/"
							+ yearOfEndDate;
				
				if( yearOfStartDate != NO_YEAR_SET )
					textToShowUser = textToShowUser + " -sd " + dayOfStartDate
						+ "/" + monthOfStartDate + "/"
						+ yearOfStartDate;

				if (!tag.equals("empty"))
					textToShowUser = textToShowUser + " -t " + tag;

				int priority = taskFound.getData().get(0).getPriority();
				if (priority == 1)
					textToShowUser = textToShowUser + " -p " + priority;

				commandLineInterface.setText(textToShowUser);
			}

			@Override
			public void mouseUp(MouseEvent arg0) {}
		});
		taskDescription.add(taskDescriptionLabel);
		return taskDescriptionLabel;
	}

	private CLabel createEndDateLabel(final Result searchList,
			final StyledText commandLineInterface, boolean isTaskCompletedTask,
			int i, final CLabel taskDescriptionLabel) {
		
		final CLabel taskEndDateLabel = new CLabel(autoCompleteComposite, SWT.NONE);
		taskEndDateLabel.setFont(SWTResourceManager.getFont("Segoe UI", 8, SWT.NORMAL));
		taskEndDateLabel.setForeground(SWTResourceManager.getColor(NOT_HIGHLIGHTED_TEXT_R ,NOT_HIGHLIGHTED_TEXT_G ,NOT_HIGHLIGHTED_TEXT_B ));
		if (isTaskCompletedTask)
			taskEndDateLabel.setBounds(320, 10 + i * 20 + 40, 50, 20);
		else
			taskEndDateLabel.setBounds(320, 10 + i * 20, 50, 20);
		
		Calendar taskEndDateCalendar = searchList.getData().get(i).getEndDate();
		int dayEndDate = taskEndDateCalendar.get(Calendar.DATE);
		int monthEndDate = taskEndDateCalendar.get(Calendar.MONTH);
		int yearEndDate = taskEndDateCalendar.get(Calendar.YEAR);
		monthEndDate++;

		if (yearEndDate != NO_YEAR_SET)
			taskEndDateLabel.setText(dayEndDate + "/" + monthEndDate);
		else
			taskEndDateLabel.setText("");

		taskEndDateLabel.addMouseListener(new MouseListener() {
			@Override
			public void mouseDown(MouseEvent arg0) {
				loadDetailsIntoCommandLine(commandLineInterface,
						taskDescriptionLabel);
			}

			@Override
			public void mouseUp(MouseEvent arg0) {}

			@Override
			public void mouseDoubleClick(MouseEvent arg0) {
				loadDetailsIntoCommandLine(commandLineInterface,
						taskDescriptionLabel);
			}

		});
		return taskEndDateLabel;
	}

	private boolean addCompletedTaskHeading(boolean completedTag, int i,
			int tempTaskID) {
		
		if (tempTaskID > MAX_TEMP_TASK_ID && completedTag == false) {
			completedTabLabel = new Label(autoCompleteComposite, SWT.NONE);
			completedTabLabel.setBounds(10, 10 + i * 20 + 20, 150, 20);
			completedTabLabel.setText("Completed Tasks");
			completedTag = true;
		}
		return completedTag;
	}

	private void loadDetailsIntoCommandLine(
			final StyledText commandLineInterface,
			final CLabel taskDescriptionLabel) {
		String currentText[] = commandLineInterface.getText().split(" ");

		String textToSet = currentText[0] + " "
				+ taskDescriptionLabel.getText() + "";

		commandLineInterface.setText(textToSet);
		disposeWindow();
	}

	private static boolean checkAutoCompleteType(String autoCompleteType)
	{
		if (autoCompleteType.equalsIgnoreCase("edit") || autoCompleteType.equalsIgnoreCase("-e") ) 
			return true;
		else if (autoCompleteType.equalsIgnoreCase("Complete"))
			return true;
		else if (autoCompleteType.equalsIgnoreCase("-c"))
			return true;
		else if (autoCompleteType.equalsIgnoreCase("incomplete"))
			return true;
		else if (autoCompleteType.equalsIgnoreCase("-inc"))
			return true;
		else if (autoCompleteType.equalsIgnoreCase("delete"))
			return true;
		else if (autoCompleteType.equalsIgnoreCase("-d"))
			return true;
		else
			return false;
	}
	
	// #############################################################################################
	// Package GUI API
	// #############################################################################################
	
	/**
	 * Method update the autocomplete window by repopulating new tasks based on the search keyword
	 * user keyed in at CLI
	 */
	public void update(final StyledText commandLineInterface,
			Result searchList, final String autoCompleteType) {

		clearAllTaskDescriptionText();
		isOpen = true;

		realTaskIDtoTempTaskID = UserInterface.getopenRealTaskIDtoTempTaskID();
		
		mapTaskIdAccordingToAutoCompleteType(autoCompleteType);

		boolean isTaskCompletedTask = false;
		
		for (int i = 0; i < searchList.getData().size(); i++) {
			
			int searchListTaskID = searchList.getData().get(i).getId();
			int tempTaskID = realTaskIDtoTempTaskID.get(searchListTaskID);

			//if task is a completed task, add "Completed task" headings
			isTaskCompletedTask = addCompletedTaskHeading(isTaskCompletedTask, i, tempTaskID);

			final CLabel taskDescriptionLabel = createTaskDescription(
					searchList, commandLineInterface, autoCompleteType,
					isTaskCompletedTask, i, tempTaskID);

			final CLabel taskEndDateLabel = createEndDateLabel(searchList,
					commandLineInterface, isTaskCompletedTask, i,
					taskDescriptionLabel);
			
			taskEndDate.add(taskEndDateLabel);
		}
		
		autoCompleteScrolledComposite.setContent(autoCompleteComposite);
		autoCompleteScrolledComposite.setMinSize(autoCompleteComposite.computeSize(SWT.DEFAULT,SWT.DEFAULT));
	}

	
	public boolean getIsOpen() {
		return isOpen;
	}

	public void disposeWindow() {
		this.isOpen = false;
		autoCompleteDialog.dispose();
		UserInterface.setCommandLineInterfaceTextCursorAtEnd();
	}
}