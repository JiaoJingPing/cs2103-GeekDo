/**
 * @author      Steve Ng 
 * @CoAuthor	Lee Zhi Xin
 * @version     0.2                                   
 * @since       2010-10-30    
 * 
 * UserInterface is the main class of the system.
 * Its job is to get user's action and react to it accordingly.
 * 
 * Developer may note that others class such as
 * 1) AlertWindow
 * 2) AutoCompleteWindow
 * 3) TabListWindow
 * 4) PersonaliseWindow
 * 5) UserInterfaceListeners
 * are all classes called by UserInterface.
 * 
 * For backend handling, UserInterface will call Logic.Controller for
 * any processing required.
 */

package GUI;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.StringTokenizer;

import logic.Controller;
import objects.Result;

import test.InvalidUserInput;

import com.melloware.jintellitype.HotkeyListener;
import com.melloware.jintellitype.IntellitypeListener;
import com.melloware.jintellitype.JIntellitype;
import com.melloware.jintellitype.JIntellitypeConstants;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.wb.swt.SWTResourceManager;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.FillLayout;


public class UserInterface extends Composite implements HotkeyListener,
		IntellitypeListener {

	// GUI components for top composite
	private static StyledText commandLineInterface = null;
	private static Label commandLabel;
	private static ToolItem buttonDownArrow;
	private static AutoCompleteWindow autoCompleteWindow;

	// GUI components for the midComposite for refreshing all tabs
	private static ScrolledComposite midScrolledComposite;
	private static Composite midTaskList;
	private static ToolBar completeDeleteAllToolBar;
	private static ToolBar opendeleteAllToolBar;

	// For scrolling of GUI midcomposite
	private static int yMidIndex = 0;

	// GUI midComposite components
	private static final int MAX_TASK_LIST = 1000;

	// Mid composite's open tasks controls
	private static Composite[] openTaskEntireDetail = new Composite[MAX_TASK_LIST];
	private static CLabel[] openTaskID = new CLabel[MAX_TASK_LIST];
	private static StyledText[] openTaskDescription = new StyledText[MAX_TASK_LIST];
	private static StyledText[] openTaskEndDate = new StyledText[MAX_TASK_LIST];
	private static ToolBar[] openDelete = new ToolBar[MAX_TASK_LIST];
	private static ToolItem[] openDeleteIcon = new ToolItem[MAX_TASK_LIST];
	private static ToolBar[] openPriority = new ToolBar[MAX_TASK_LIST];
	private static ToolItem[] openPriorityIcon = new ToolItem[MAX_TASK_LIST];
	private static ToolBar[] openComplete = new ToolBar[MAX_TASK_LIST];
	private static ToolItem[] openCompleteIcon = new ToolItem[MAX_TASK_LIST];
	// Mid composite's completed tasks control
	private static Composite[] completeTaskEntireDetail = new Composite[MAX_TASK_LIST];
	private static CLabel[] completeTaskID = new CLabel[MAX_TASK_LIST];
	private final static StyledText[] completeTaskDescription = new StyledText[MAX_TASK_LIST];
	private static StyledText[] completeTaskEndDate = new StyledText[MAX_TASK_LIST];
	private static CLabel[] completePriority = new CLabel[MAX_TASK_LIST];
	private static ToolBar[] completeDelete = new ToolBar[MAX_TASK_LIST];
	private static ToolItem[] completeDeleteIcon = new ToolItem[MAX_TASK_LIST];
	private static ToolBar[] completeIncomplete = new ToolBar[1000];
	private static ToolItem[] completeIncompleteIcon = new ToolItem[1000];
	private static CLabel lblCompleteTasks;

	// Final variables for RGB colors of orange and yellow
	// Orange for tasks that are overdue
	private static final int ORANGE_R = 239;
	private static final int ORANGE_G = 171;
	private static final int ORANGE_B = 8;
	// Yellow for tasks that are not overdue
	private static final int YELLOW_R = 255;
	private static final int YELLOW_G = 247;
	private static final int YELLOW_B = 153;

	// Final variable for font of task details
	private static final Font TASK_TEXT_FONT = SWTResourceManager.getFont("Segoe UI", 10, SWT.NORMAL);

	// For end date / start date toggling
	private static boolean showEndDate = true;

	// year stored in backend to indicate no year set
	private static final int NO_YEAR_SET = 3000;

	// variables for drag and drop capabilities of the composite
	private boolean returnMouseUp = true;
	private Point locationOfBeforeClicking;

	// Configuration Hotkeys
	private static char quickAddHotKey = 0;
	private static char quickShowAndHideHotKey = 0;

	// variable for the alert function
	private static int noOfDaysForAlert;

	// for deleting of all tasks in each window
	private static String SelectedTab = "All";
	private static String searchCommand = "";
	private static boolean isTaskListSearchResult = false;

	// for GUI appearance of the down arrow key to expand and contract
	private final static int X_LENGTH_OF_GUI = 554;
	private final static int Y_LENGTH_OF_GUI_ONLY_COMMAND_LINE = 115;
	private final static int Y_LENGTH_OF_GUI = 485;

	// for storing of user typed command history
	private static ArrayList<String> commandHistory = new ArrayList<String>();
	private static int historyMark = 0;
	private static String tempText = "";

	// global variable for the bottomLayout if its open or not
	private static boolean bottomDialogOpen = false;
	
	// global variable for displaying error message to user
	private static boolean isErrorMessage = false;

	// for mapping of label's task ID to the real ID at the backend
	private static HashMap<Integer, Integer> completedRealTaskIDtoTempTaskID = new HashMap<Integer, Integer>();
	private static HashMap<Integer, Integer> completedTempTaskIDtoRealTaskID = new HashMap<Integer, Integer>();
	private static HashMap<Integer, Integer> openRealTaskIDtoTempTaskID = new HashMap<Integer, Integer>();
	private static HashMap<Integer, Integer> openTempTaskIDtoRealTaskID = new HashMap<Integer, Integer>();
	private Composite topComposite_1;
	private FormData fd_midComposite_1;

	public static void main(String[] args) {

		Controller.initializeEventList();

		Display display = Display.getCurrent();
		final Shell shell = new Shell(display, SWT.NONE);
		UserInterface calc = new UserInterface(shell, SWT.NONE);
		shell.setSize(X_LENGTH_OF_GUI, Y_LENGTH_OF_GUI_ONLY_COMMAND_LINE);

		logBasicSystemInfo();
		readConfigurationForGUI();
		activateHotKeyForApplication(display, shell, calc);
		commandLineInterface.forceFocus();
		setTrayIcon(display, shell);
		showAlertToolTip(shell, display);

		calc.pack();
		shell.open();
		shell.setText("To Do Manager");
		shell.setBackgroundMode(SWT.INHERIT_DEFAULT);

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}

	}

	// #############################################################################################
	// Second level abstraction
	// #############################################################################################

	private UserInterface(final Composite parent, int style) {
		super(parent, style);
		setLayout(new FillLayout(SWT.HORIZONTAL));

		// creation of main architecture of layout
		Composite composite = new Composite(this, SWT.NONE);
		composite.setLayout(new FormLayout());

		// creation of top composite
		final Composite topComposite = createTopComposite(parent, composite);

		// creation of mid composite
		FormData fd_midComposite = createMidComposite(composite, topComposite);

		// creation of bottom composite
		createBottomComposite(composite, topComposite, fd_midComposite);
	}
	
	private static void logBasicSystemInfo() {

		Controller.logSystemInfo("Operating System: "
				+ System.getProperty("os.name") + " "
				+ System.getProperty("os.version"));
		Controller.logSystemInfo("JRE: " + System.getProperty("java.version"));
		Controller.logSystemInfo("Java Launched From: "
				+ System.getProperty("java.home"));
		Controller.logSystemInfo("Class Path: "
				+ System.getProperty("java.class.path"));
		Controller.logSystemInfo("Library Path: "
				+ System.getProperty("java.library.path"));
		Controller.logSystemInfo("User Home Directory: "
				+ System.getProperty("user.home"));
		Controller.logSystemInfo("User Working Directory: "
				+ System.getProperty("user.dir"));
		Controller.logSystemInfo("User time zone: "
				+ System.getProperty("user.timezone"));
	}

	private static void readConfigurationForGUI() {
		File inFile = null;
		try {
			inFile = new File("configurationHotKeys.txt");
			inFile.createNewFile();

			LineNumberReader fileReader = new LineNumberReader(new FileReader(
					inFile));
			for (int i = 0; i < 3; i++) {
				StringTokenizer seperateDelimeter = new StringTokenizer(
						fileReader.readLine());
				seperateDelimeter.nextToken();
				String hotkey = seperateDelimeter.nextToken();
				if (i == 0) {
					quickAddHotKey = hotkey.charAt(0);
				} else if (i == 1)
					quickShowAndHideHotKey = hotkey.charAt(0);
				else if (i == 2)
					noOfDaysForAlert = Integer.parseInt(hotkey.trim());
			}

		} catch (IOException e) {
			Controller
					.logSystemExceptionError("Exception in UserInterface.readConfigurationForGUI() "
							+ e.getMessage());
		} catch (NullPointerException e2)// occurs when the files is corrupted
		{
			loadConfigurationFileWithNewData(inFile);
		}

	}

	private static void activateHotKeyForApplication(Display display,
			final Shell shell, UserInterface calc) {

		createExpandandContractBottomLayOutListener(display, shell);
		createAltF4CloseListener(display);
		createTaskListScrollListener(display);
		createTabListScrollListener(display);

		// Register global hot keys listener
		boolean hotKeySupported = checkIfGlobalHotKeySupported();
		if (hotKeySupported) {
			registerGlobalHotKey();
			calc.initJIntellitype();
		} else {
			Controller
					.logSystemExceptionError("JIntellitype not supported on user's PC");
		}

	}

	private static void setTrayIcon(Display display, final Shell shell) {

		final Tray tray = display.getSystemTray();

		final TrayItem trayIcon = new TrayItem(tray, SWT.NONE);
		trayIcon.setToolTipText("Geekdo");
		trayIcon.setImage(SWTResourceManager.getImage(UserInterface.class,
				"/GUI/icon_systemtray.png"));

		createMenuInSystemTray(shell, trayIcon, display);

		trayIcon.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(org.eclipse.swt.widgets.Event arg0) {
				if (shell.getVisible() == false) {
					shell.setVisible(true);
					shell.forceActive();
				} else
					shell.setVisible(false);
			}
		});

	}

	private static void showAlertToolTip(final Shell shell, Display display) {
		final ToolTip tip = new ToolTip(shell, SWT.BALLOON
				| SWT.ICON_INFORMATION);
		new AlertWindow(shell, SWT.NO_TRIM);

		// find number of task that are due soon
		int taskDue = AlertWindow.getNoOfTasksInAlertWindow();

		assert taskDue >= 0 : "showAlertToolTip() of UserInterface.java, taskdue return wrong value";

		if (taskDue > 0) {
			tip.setMessage("You have " + taskDue
					+ " tasks due. Click here to see tasks that are due soon");
			tip.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					AlertWindow.setSwitchToOpen(true);
					new AlertWindow(shell, SWT.NO_TRIM);
				}
			});

			display.getSystemTray().getItems()[0].setToolTip(tip);

			tip.setVisible(true);
		}
	}

	// #############################################################################################
	// Third level abstraction
	// #############################################################################################

	private Composite createTopComposite(final Composite parent,
			Composite composite) {
	
		topComposite_1 = new Composite(composite, SWT.NONE);
		FormData fd_topComposite_1 = new FormData();
		fd_topComposite_1.bottom = new FormAttachment(0, 114);
		fd_topComposite_1.right = new FormAttachment(0, 554);
		fd_topComposite_1.top = new FormAttachment(0);
		fd_topComposite_1.left = new FormAttachment(0);
		topComposite_1.setLayoutData(fd_topComposite_1);
		topComposite_1.setBackgroundImage(SWTResourceManager.getImage(
				UserInterface.class, "/GUI/CLI_background3.png"));

		createDragCapabilities(topComposite_1);

		createTopCompositeComponents(parent, topComposite_1);

		return topComposite_1;
	}

	private FormData createMidComposite(Composite composite,
			final Composite topComposite) {
		Composite midComposite = new Composite(composite, SWT.NONE);
		fd_midComposite_1 = new FormData();
		fd_midComposite_1.top = new FormAttachment(topComposite_1);
		fd_midComposite_1.right = new FormAttachment(topComposite, 0, SWT.RIGHT);
		fd_midComposite_1.left = new FormAttachment(0);
		midComposite.setLayoutData(fd_midComposite_1);

		createMidCompositeComponents(midComposite);

		
		return fd_midComposite_1;
	}

	private void createBottomComposite(Composite composite,
			final Composite topComposite, FormData fd_midComposite) {
		
		TabListWindow bottomComposite = new TabListWindow(composite, SWT.NONE);
		fd_midComposite_1.bottom = new FormAttachment(bottomComposite);
		FormData fd_bottomComposite = new FormData();
		fd_bottomComposite.right = new FormAttachment(topComposite, 0,
				SWT.RIGHT);
		fd_bottomComposite.left = new FormAttachment(0);
		fd_bottomComposite.bottom = new FormAttachment(100, -46);
		fd_bottomComposite.top = new FormAttachment(0, 430);
		bottomComposite.setLayoutData(fd_bottomComposite);
		bottomComposite.setBackgroundImage(SWTResourceManager.getImage(
				UserInterface.class, "/GUI/Tablist_background.png"));
		bottomComposite.setBackgroundMode(SWT.INHERIT_DEFAULT);

	}
	
	private static void loadConfigurationFileWithNewData(File inFile) {
		try {

			assert inFile != null : "Configuration File shouldn't be null line 1945 of UserInterface.java";
			PrintWriter fileWriter = new PrintWriter(inFile);

			fileWriter.println("QuickAdd= A");
			fileWriter.println("QuickShowAndHide= S");
			fileWriter.println("NoOfDaysForEndTask= 3");
			fileWriter.println("\n\n\n");
			fileWriter
					.println("##########################################################################################################");
			fileWriter
					.println("Warning: Please Do not Edit this file \n Editing of this file might render your application to not work");
			fileWriter
					.println("##########################################################################################################");

			fileWriter.flush();
			fileWriter.close();
		} catch (FileNotFoundException e) {
			Controller
					.logSystemExceptionError("File not found inside loadConfigurationFileWithNewData()");
		}
	}

	private static void createExpandandContractBottomLayOutListener(
			Display display, final Shell shell) {

		display.addFilter(SWT.KeyDown, new Listener() {
			public void handleEvent(Event e) {
				if (((e.stateMask & SWT.CTRL) == SWT.CTRL)
						&& (e.keyCode == 's')) {
					if (!bottomDialogOpen) {
						shell.setSize(X_LENGTH_OF_GUI, Y_LENGTH_OF_GUI);
						setBottomDialogOpen(true, shell);
					} else {
						shell.setSize(X_LENGTH_OF_GUI,
								Y_LENGTH_OF_GUI_ONLY_COMMAND_LINE);
						setBottomDialogOpen(false, shell);
					}
				}
			}
		});
	}

	private static void createAltF4CloseListener(Display display) {

		display.addFilter(SWT.KeyDown, new Listener() {
			public void handleEvent(Event e) {
				if (((e.stateMask & SWT.ALT) == SWT.ALT)
						&& (e.keyCode == SWT.F4)) {

					Shell dialogShell = new Shell(Display.getCurrent());
					MessageBox confirmExit = new MessageBox(dialogShell,
							SWT.ICON_QUESTION | SWT.YES | SWT.NO);
					confirmExit
							.setMessage("Do you really want to exit Geekdo?");
					confirmExit.setText("Closing Geekdo");
					int response = confirmExit.open();
					if (response == SWT.YES) {
						disposeSystemTray();
						System.exit(0);
					}
				}
			}
		});
	}

	private static void createTaskListScrollListener(Display display) {

		display.addFilter(SWT.KeyDown, new Listener() {
			public void handleEvent(Event e) {
				if (((e.stateMask & SWT.CTRL) == SWT.CTRL)
						&& (e.keyCode == SWT.ARROW_DOWN)) {

					if (midScrolledComposite.getSize().y > yMidIndex)
						yMidIndex = yMidIndex + 20;

					midScrolledComposite.setOrigin(new Point(0, yMidIndex));

				} else if (((e.stateMask & SWT.CTRL) == SWT.CTRL)
						&& (e.keyCode == SWT.ARROW_UP)) {

					if (yMidIndex > 0)
						yMidIndex = yMidIndex - 20;

					midScrolledComposite.setOrigin(new Point(0, yMidIndex));

				}
			}
		});

	}

	private static void createTabListScrollListener(Display display) {

		display.addFilter(SWT.KeyDown, new Listener() {
			public void handleEvent(Event e) {
				if (((e.stateMask & SWT.CTRL) == SWT.CTRL)
						&& (e.keyCode == SWT.ARROW_LEFT)) {
					TabListWindow.selectPreviousTab();

				} else if (((e.stateMask & SWT.CTRL) == SWT.CTRL)
						&& (e.keyCode == SWT.ARROW_RIGHT)) {
					TabListWindow.selectNextTab();

				}
			}
		});

	}

	private static boolean checkIfGlobalHotKeySupported() {

		if (JIntellitype
				.checkInstanceAlreadyRunning("JIntellitype Test Application")) {
			return false;
		}

		if (!JIntellitype.isJIntellitypeSupported()) {
			return false;
		}
		return true;
	}

	private static void registerGlobalHotKey() {

		final int CTRL_SHIFT_A = 90;
		final int CTRL_SHIFT_S = 91;

		// register global hotkey
		JIntellitype.getInstance().registerHotKey(
				CTRL_SHIFT_A,
				JIntellitypeConstants.MOD_CONTROL
						+ JIntellitypeConstants.MOD_SHIFT, quickAddHotKey);
		JIntellitype.getInstance().registerHotKey(
				CTRL_SHIFT_S,
				JIntellitypeConstants.MOD_CONTROL
						+ JIntellitypeConstants.MOD_SHIFT,
				quickShowAndHideHotKey);

	}

	/**
	 * Method initialize JIntellitype within the frame so all windows commands
	 * can be attached to this window
	 */
	private void initJIntellitype() {
		try {
			JIntellitype.getInstance().addHotKeyListener(this);
			JIntellitype.getInstance().addIntellitypeListener(this);
		} catch (RuntimeException ex) {
			Controller
					.logSystemExceptionError("User is not on Windows, or there is a problem with the JIntellitype library!");
		}
	}

	// #############################################################################################
	// Codes for system tray settings
	// #############################################################################################

	private static void createMenuInSystemTray(Shell shell,
			final TrayItem item, Display display) {
		final Menu menu = new Menu(shell, SWT.POP_UP);

		createMenuInSystemTrayPersonalise(menu, display);
		createMenuInSystemTrayShow(menu, shell);
		createMenuInSystemTrayMinimized(menu, shell);
		createMenuInSystemTrayExit(menu);
		item.addListener(SWT.MenuDetect, new Listener() {
			@Override
			public void handleEvent(org.eclipse.swt.widgets.Event arg0) {
				menu.setVisible(true);
			}
		});

	}

	private static void createMenuInSystemTrayPersonalise(Menu menu,
			final Display display) {

		MenuItem menuPersonalise = new MenuItem(menu, SWT.PUSH);
		menuPersonalise.setText("Personalise");
		final Shell shell = new Shell(display);

		menuPersonalise.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if (PersonaliseWindow.getIsOpen() == false)
					new PersonaliseWindow(shell, SWT.NONE);
			}
		});

	}

	private static void createMenuInSystemTrayShow(final Menu menu,
			final Shell shell) {
		MenuItem menuShow = new MenuItem(menu, SWT.PUSH);
		menuShow.setText("Show");
		menuShow.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				shell.forceActive();
				shell.setVisible(true);
			}
		});
	}

	private static void createMenuInSystemTrayExit(final Menu menu) {
		MenuItem menuExit = new MenuItem(menu, SWT.PUSH);
		menuExit.setText("Exit");
		menuExit.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				disposeSystemTray();
				System.exit(0);
			}
		});
	}

	private static void createMenuInSystemTrayMinimized(final Menu menu,
			final Shell shell) {
		MenuItem menuMinimized = new MenuItem(menu, SWT.PUSH);
		menuMinimized.setText("Minimize");
		menuMinimized.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				shell.setVisible(false);
			}
		});
	}

	private static void disposeSystemTray() {
		try {
			final Tray tray = Display.getCurrent().getSystemTray();
			tray.dispose();
		} catch (UnsupportedOperationException e) {
			Controller
					.logSystemExceptionError("System does not support system tray!");
		}
	}
	// #############################################################################################
	// Fourth Level abstraction
	// #############################################################################################

	private void createDragCapabilities(final Control composite) {
	
		composite.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				returnMouseUp = false;
				locationOfBeforeClicking = getDisplay().getCursorLocation();
			}

			@Override
			public void mouseUp(MouseEvent e) {
				returnMouseUp = true;
			}
		});

		composite.addMouseMoveListener(new MouseMoveListener() {
			@Override
			public void mouseMove(MouseEvent arg0) {
				if (returnMouseUp == false) {
					Point locationOfNewWindow = getDisplay().getCursorLocation();
					Point shellLocation = getShell().getLocation();

					locationOfNewWindow = new Point(
							shellLocation.x
									+ (locationOfNewWindow.x - locationOfBeforeClicking.x),
							shellLocation.y
									+ (locationOfNewWindow.y - locationOfBeforeClicking.y));
					
					
					getShell().setLocation(locationOfNewWindow);
					locationOfBeforeClicking = getDisplay().getCursorLocation();
				}
			}
		});
	}
	
	private void createTopCompositeComponents(final Composite parent,
			final Composite topComposite) {

		createMinimizeAndCloseButton(topComposite);
		createArrowUpAndDownButton(topComposite);
		createCommonLabel(topComposite);
		createCommandTextField(parent, topComposite);
	}

	private void createMidCompositeComponents(Composite midComposite) {
		midComposite.setLayout(new FillLayout(SWT.HORIZONTAL));

		// creation of the scrolled composite
		midScrolledComposite = new ScrolledComposite(midComposite, SWT.V_SCROLL);
		midScrolledComposite.setExpandHorizontal(true);
		midScrolledComposite.setExpandVertical(true);

		// storing a composite inside scrolled composite
		midTaskList = new Composite(midScrolledComposite, SWT.NONE);
		midTaskList.setBackgroundMode(SWT.INHERIT_DEFAULT);

		getTaskAndPopulateTask(midTaskList, midScrolledComposite, null, null);

		midScrolledComposite.setContent(midTaskList);
		midScrolledComposite.setMinSize(midTaskList.computeSize(SWT.DEFAULT,
				SWT.DEFAULT));
		midTaskList.setBounds(0, 0, X_LENGTH_OF_GUI, 370);
		midTaskList.setBackgroundImage(SWTResourceManager.getImage(
				UserInterface.class, "/GUI/GUI_background.png"));
	
		
		createDragCapabilities(midScrolledComposite);
		createDragCapabilities(midTaskList);
		
	}

	// #############################################################################################
	// Fifth Level abstraction
	// #############################################################################################

	private void createMinimizeAndCloseButton(final Composite topComposite) {

		ToolBar toolBar = new ToolBar(topComposite, SWT.FLAT);
		toolBar.setBounds(478, 10, 70, 35);

		ToolItem buttonMinimise = new ToolItem(toolBar, SWT.NO_BACKGROUND);
		buttonMinimise.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				getShell().setVisible(false);

				final ToolTip tip = new ToolTip(getShell(), SWT.BALLOON
						| SWT.ICON_INFORMATION);
				tip.setMessage("GeekDo is now minimized");
				getDisplay().getSystemTray().getItems()[0].setToolTip(tip);
				tip.setVisible(true);

			}
		});
		buttonMinimise.setImage(SWTResourceManager.getImage(
				UserInterface.class, "/GUI/button_minimise.png"));

		ToolItem buttonClose = new ToolItem(toolBar, SWT.NONE);
		buttonClose.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				JIntellitype.getInstance().cleanUp();
				disposeSystemTray();
				System.exit(0);
			}
		});
		buttonClose.setImage(SWTResourceManager.getImage(UserInterface.class,
				"/GUI/button_close.png"));
	}

	private void createArrowUpAndDownButton(final Composite topComposite) {
		ToolBar toolbarForDownArrow = new ToolBar(topComposite, SWT.FLAT);
		toolbarForDownArrow.setBackground(SWTResourceManager
				.getColor(SWT.TRANSPARENT));
		toolbarForDownArrow.setBounds(X_LENGTH_OF_GUI - 28,
				Y_LENGTH_OF_GUI_ONLY_COMMAND_LINE - 40, 28, 28);

		buttonDownArrow = new ToolItem(toolbarForDownArrow, SWT.NONE);
		buttonDownArrow.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (!bottomDialogOpen) {
					setBottomDialogOpen(true, getShell());
				} else {
					setBottomDialogOpen(false, getShell());
				}
			}
		});
		buttonDownArrow.setImage(SWTResourceManager.getImage(
				UserInterface.class, "/GUI/button_downarrow_yellow.png"));
	}

	private void createCommonLabel(final Composite topComposite) {
		commandLabel = new Label(topComposite, SWT.NONE);
		commandLabel.setForeground(SWTResourceManager.getColor(193, 187, 107));
		commandLabel.setFont(SWTResourceManager.getFont("Segoe UI", 8,
				SWT.NORMAL));
		commandLabel.setBounds(36, 94, 484, 15);
		commandLabel
				.setText("Available commands: Add, Edit, Complete, Incomplete, Delete, Search, Help and Home");
	}

	private void createCommandTextField(final Composite parent,
			final Composite topComposite) {

		commandLineInterface = new StyledText(topComposite, SWT.FULL_SELECTION);
		commandLineInterface.setForeground(SWTResourceManager.getColor(
				YELLOW_R, YELLOW_G, YELLOW_B));
		commandLineInterface.setFont(SWTResourceManager.getFont("Segoe UI", 11,
				SWT.NONE));
		commandLineInterface.setBounds(35, 65, 460, 25);
		commandLineInterface
				.setToolTipText("Available commands: Add, Edit, Complete, Incomplete, Delete, Search, Help and Home");
		createModifyListenerForCommandLabel(parent);

		createKeyListenerForCLI();

	}

	private static void getTaskAndPopulateTask(Composite taskList,
			ScrolledComposite scrolledComposite, Result openTask,
			Result completedTask) {

		Result openTaskList;
		Result completedTaskList;

		if (openTask != null) {
			openTaskList = openTask;
			completedTaskList = completedTask;
		} else {
			openTaskList = Controller.getAllOpenTasks();
			completedTaskList = Controller.getAllCompletedTask();
		}

		checkAndDisposeOpenDeleteAll();

		createCompulsoryControls(taskList);

		if (openTaskList.getData().size() > 0)
			createOpenTaskDeleteAll(taskList);

		disposeAllControls();

		int openTaskCounter = loadOpenTasks(taskList, openTaskList);

		checkAndDisposeCompletedTasksLabel();

		if (completedTaskList != null) {
			if (completedTaskList.getData().size() != 0)
				loadCompletedTasks(taskList, completedTaskList, openTaskCounter);
		}
		scrolledComposite.setContent(taskList);
		scrolledComposite.setMinSize(taskList.computeSize(SWT.DEFAULT,
				SWT.DEFAULT));

	}

	// #############################################################################################
	// Sixth Level abstraction
	// #############################################################################################
	
	private static void setBottomDialogOpen(boolean openOrClose, Shell shell) {
		bottomDialogOpen = openOrClose;
		if (openOrClose == true) {
			buttonDownArrow.setImage(SWTResourceManager.getImage(
					UserInterface.class, "/GUI/button_uparrow_yellow.png"));

			shell.setSize(X_LENGTH_OF_GUI, Y_LENGTH_OF_GUI);
		} else {
			buttonDownArrow.setImage(SWTResourceManager.getImage(
					UserInterface.class, "/GUI/button_downarrow_yellow.png"));
			shell.setSize(X_LENGTH_OF_GUI, Y_LENGTH_OF_GUI_ONLY_COMMAND_LINE);
		}
	}
	
	private void createModifyListenerForCommandLabel(final Composite parent) {
		commandLineInterface.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				checkCommandLabelAndShowAutoCompleteWindowIfNeed(parent);
			}

			private void checkCommandLabelAndShowAutoCompleteWindowIfNeed(
					final Composite parent) {

				String userInput = commandLineInterface.getText();

				String[] userInputCommand = new String[100];

				if (userInput.trim().length() != 0)
					userInputCommand = userInput.split(" ");
				else
					userInputCommand[0] = "";

				if (commandLineInterface.getText().trim().length() != 0)
					highlightSpecialSyntax();

				if (userInputCommand[0] == null) {
					userInputCommand[0] = "";
				}

				if (userInputCommand[0].equalsIgnoreCase("edit")
						|| userInputCommand[0].equalsIgnoreCase("-e")
						|| userInputCommand[0].equalsIgnoreCase("delete")
						|| userInputCommand[0].equalsIgnoreCase("-d")
						|| userInputCommand[0].equalsIgnoreCase("complete")
						|| userInputCommand[0].equalsIgnoreCase("-c")
						|| userInputCommand[0].equalsIgnoreCase("incomplete")
						|| userInputCommand[0].equalsIgnoreCase("-inc")) {

					loadAutoCompleteWindow(parent, userInput, userInputCommand);
				} else if (autoCompleteWindow != null
						&& autoCompleteWindow.getIsOpen()) {// if input not//
															// matches cases
					autoCompleteWindow.disposeWindow();
					System.out
							.println("Dispose Window since no matched keyword");
					setCommandLineInterfaceTextCursorAtEnd();
				}
			}

			private void loadAutoCompleteWindow(final Composite parent,
					String userInput, String[] userInputCommand) {

				StringTokenizer searchKeyWord = new StringTokenizer(userInput,
						" ");

				Result searchList = determineWhatToPopulate(userInputCommand);

				// removing the command syntax like "add,search,edit"
				assert searchKeyWord.countTokens() > 0 : "token must definitely be more than zero! UserInterface's line 755";
				searchKeyWord.nextToken();

				searchList = searchForTasks(userInputCommand, searchKeyWord,
						searchList);

				String autoCompleteType = setAutoCompleteType(userInputCommand);
				if (searchList.getData().size() != 0) {
					if (autoCompleteWindow == null
							|| !autoCompleteWindow.getIsOpen()) {

						Point commandLineInterfaceLocation = parent
								.getLocation();
						int x = commandLineInterfaceLocation.x;
						int y = commandLineInterfaceLocation.y + 30;
						autoCompleteWindow = new AutoCompleteWindow(getShell(),
								SWT.NONE, x, y, searchList,
								commandLineInterface, autoCompleteType);

					} else { // autocomplete is opened
						autoCompleteWindow.update(commandLineInterface,
								searchList, autoCompleteType);
					}
				} else {// if no search result from auto complete
					if (autoCompleteWindow != null) {
						if (autoCompleteWindow.getIsOpen()) {
							autoCompleteWindow.disposeWindow();
						}
					}
				}

			}

			private String setAutoCompleteType(String[] userInputCommand) {
				String autoCompleteType;
				if (userInputCommand[0].equalsIgnoreCase("delete"))
					autoCompleteType = "delete";
				else if (userInputCommand[0].equalsIgnoreCase("-d"))
					autoCompleteType = "-d";
				else if (userInputCommand[0].equalsIgnoreCase("edit"))
					autoCompleteType = "edit";
				else if (userInputCommand[0].equalsIgnoreCase("-e"))
					autoCompleteType = "-e";
				else if (userInputCommand[0].equalsIgnoreCase("complete"))
					autoCompleteType = "complete";
				else if (userInputCommand[0].equalsIgnoreCase("-c"))
					autoCompleteType = "-c";
				else if (userInputCommand[0].equalsIgnoreCase("incomplete"))
					autoCompleteType = "incomplete";
				else
					autoCompleteType = "-inc";
				return autoCompleteType;
			}

			private Result searchForTasks(String[] userInputCommand,
					StringTokenizer searchKeyWord, Result searchList) {
				ArrayList<String> concatSearchKeyWord = new ArrayList<String>();

				while (searchKeyWord.hasMoreTokens()) {
					concatSearchKeyWord.add(searchKeyWord.nextToken().trim());

					if (userInputCommand[0].equalsIgnoreCase("delete")
							|| userInputCommand[0].equalsIgnoreCase("-d"))
						searchList = Controller
								.searchForAllTask(concatSearchKeyWord);
					else if (userInputCommand[0].equalsIgnoreCase("edit")
							|| userInputCommand[0].equalsIgnoreCase("-e")
							|| userInputCommand[0].equalsIgnoreCase("complete")
							|| userInputCommand[0].equalsIgnoreCase("-c"))
						searchList = Controller
								.searchForOpenTask(concatSearchKeyWord);
					else
						searchList = Controller
								.searchForCompletedTask(concatSearchKeyWord);
				}
				return searchList;
			}

			private Result determineWhatToPopulate(String[] userInputCommand) {
				Result searchList;
				if (userInputCommand[0].equalsIgnoreCase("delete")
						|| userInputCommand[0].equalsIgnoreCase("-d"))
					searchList = Controller.getAllTasks();
				else if (userInputCommand[0].equalsIgnoreCase("incomplete")
						|| userInputCommand[0].equalsIgnoreCase("-inc"))
					searchList = Controller.getAllCompletedTask();
				else
					searchList = Controller.getAllOpenTasks();
				return searchList;
			}

		});
	}
	
	private void createKeyListenerForCLI() {

		final int LETTER_A = 97;

		commandLineInterface.addKeyListener(new KeyAdapter() {
			int previousKeyCode = 0;

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.CR) {
					manipulateStringAndSendToBackEnd();
				} else if (e.keyCode == SWT.ARROW_UP) {

					if ((e.stateMask & SWT.CTRL) == 0)
						showCommandHistoryArrowUp();

				} else if (e.keyCode == SWT.ARROW_DOWN) {
					
					if ((e.stateMask & SWT.CTRL) == 0)
						showCommandHistoryArrowDown();
				
				} else if (e.keyCode == LETTER_A) {
					if (previousKeyCode == SWT.CTRL) {
						commandLineInterface.selectAll();
					}

				}
				if(!isErrorMessage)
					changeCommandLabelBasedOnCommandText();
				
				isErrorMessage = false;
				previousKeyCode = e.keyCode;
			}

			private void changeCommandLabelBasedOnCommandText() {
				
				String userInput = commandLineInterface.getText().trim();
				String[] splitUserInput = new String[100];

				if (userInput.trim().length() != 0)
					splitUserInput = userInput.split(" ");
				else
					splitUserInput[0] = "";

				if (commandLineInterface.getText().trim().length() != 0)
					highlightSpecialSyntax();

				if (splitUserInput[0].equalsIgnoreCase("Add")) {
					commandLabel
							.setText("Add <task_description> -ed <dd/mm/yy> -sd <dd/mm/yy> -t <tag> -p <1 as important> ");
				} else if (splitUserInput[0].equalsIgnoreCase("delete")) {
					commandLabel
							.setText("delete [-c (for completed task) /-i (for incompleted task)] <taskID>. <task_name>");
				} else if (splitUserInput[0].equalsIgnoreCase("-d")) {
					commandLabel
							.setText("-d [-c (for completed task) /-i (for incompleted task)] <taskID>. <task_name>");
				} else if (splitUserInput[0].equalsIgnoreCase("edit")) {
					commandLabel
							.setText("edit <taskID>. task_description -ed <dd/mm/yy> -sd -t <tag> -p <1 as important>");
				} else if (splitUserInput[0].equalsIgnoreCase("-e")) {
					commandLabel
							.setText("-e <taskID>. task_description -ed <dd/mm/yy> -sd -t <tag> -p <1 as important>");
				} else if (splitUserInput[0].equalsIgnoreCase("complete")) {
					commandLabel.setText("complete <taskID>. <task_name> ");
				} else if (splitUserInput[0].equalsIgnoreCase("-c")) {
					commandLabel.setText("-c <taskID>. <task_name> ");
				} else if (splitUserInput[0].equalsIgnoreCase("incomplete")) {
					commandLabel.setText("incomplete <taskID>. <task_name>");
				} else if (splitUserInput[0].equalsIgnoreCase("-inc")) {
					commandLabel.setText("-inc <taskID>. <task_name>");
				} else if (splitUserInput[0].equalsIgnoreCase("search")) {
					commandLabel
							.setText("search [<task_description> / [-ed <dd/mm/yyyy>] / [-sd <dd/mm/yyyy> / [-t <tag>]  ");
				} else if (splitUserInput[0].equalsIgnoreCase("-s")) {
					commandLabel
							.setText("-s [<task_description> / [-ed <dd/mm/yyyy>] / [-sd <dd/mm/yyyy> / [-t <tag>]  ");
				} 
			}

			private void showCommandHistoryArrowDown() {
				if (historyMark == (commandHistory.size() - 1)) {
					commandLineInterface.setText(tempText);
				} else {
					historyMark++;
					if (commandHistory.isEmpty() == false)
						commandLineInterface.setText(commandHistory
								.get(historyMark));
				}
				UserInterface.setCommandLineInterfaceTextCursorAtEnd();
			}

			private void showCommandHistoryArrowUp() {
				if (historyMark == commandHistory.size() - 1) {
					tempText = commandLineInterface.getText();
				}

				if (commandHistory.isEmpty() == false)
					commandLineInterface.setText(commandHistory
							.get(historyMark));

				if (historyMark != -0)
					historyMark--;

				UserInterface.setCommandLineInterfaceTextCursorAtEnd();
			}

			private void manipulateStringAndSendToBackEnd() {
				try {

					String commandToPassToBackEnd = commandLineInterface
							.getText().trim();

					commandToPassToBackEnd = replaceTempTaskIDWithRealTaskID(commandToPassToBackEnd);

					commandToPassToBackEnd = removeEnterFromUserInput(commandToPassToBackEnd);

					Result processedResult = null;

					if (commandToPassToBackEnd.trim().equalsIgnoreCase("help")) {
						openUserGuideManualToUser();
					} else if (commandToPassToBackEnd.trim().equalsIgnoreCase(
							"home")) {
						refreshTabList(null, null);
						commandLabel.setText("Back to All tab!");
					} else if (commandToPassToBackEnd.trim().length() != 0) {
			
						commandHistory.add(commandLineInterface.getText()
								.trim());
						historyMark = commandHistory.size() - 1;
						Controller.logSystemInfo("User command ="
								+ commandLineInterface.getText().trim());

						processedResult = Controller.processText(commandToPassToBackEnd);
						checkAndPrintFeedbackToUser(processedResult);

					}

					refreshTabList(null, null);
					commandLineInterface.setText("");

					if (commandToPassToBackEnd.startsWith("search")
							|| commandToPassToBackEnd.startsWith("-s")) {
						isTaskListSearchResult = true;
						searchCommand = commandToPassToBackEnd;
						UserInterface.refreshTabList(processedResult, null);

						getShell().setSize(X_LENGTH_OF_GUI, Y_LENGTH_OF_GUI);
						setBottomDialogOpen(true, getShell());
					} else
						isTaskListSearchResult = false;

				} catch (InvalidCommandType e3) {
					setCommandLabel(e3.getError());
					commandLineInterface.setText("");
				} catch (NumberFormatException e1) {
					setCommandLabel("Invalid task ID. Check to ensure that the taskID keyed in is the correct index from the GUI.");
					commandLineInterface.setText("");
				} catch (NullPointerException e2) {
					setCommandLabel("Invalid task ID. Check to ensure that the taskID keyed in is the correct index from the GUI.");
					commandLineInterface.setText("");
				} catch (InvalidUserInput exception) {
				
					displayErrorMessageToUser(exception);
				}

			}

			private void displayErrorMessageToUser(InvalidUserInput exception) {
				
				isErrorMessage = true;
				setCommandLabel(exception.getError());

				String text = commandLineInterface.getText().trim();
				commandLineInterface.setText(text);	
				UserInterface.setCommandLineInterfaceTextCursorAtEnd();
			}

			private void openUserGuideManualToUser() {
				commandLabel
						.setText("Available commands: Add, Edit, Complete, Incomplete, Delete, Search, Help and Home.");
				try {

					if ((new File("User Guide.pdf")).exists()) {

						Process p = Runtime
								.getRuntime()
								.exec("rundll32 url.dll,FileProtocolHandler User Guide.pdf");
						p.waitFor();

					} else {
						Controller
								.logSystemExceptionError("User Guide.pdf is missing from user's directory");
					}

				} catch (Exception ex) {
					Controller
							.logSystemExceptionError("An exception occured in openUserGuideManualToUser "
									+ ex.getMessage());
				}
			}

			/**
			 * Method remove the enter in between input for example when user
			 * copy and paste content in multiple \n will be detected.
			 * 
			 * @param commandToPassToBackEnd
			 * @return
			 */
			private String removeEnterFromUserInput(
					String commandToPassToBackEnd) {
				StringTokenizer removeEnter = new StringTokenizer(
						commandToPassToBackEnd, "\r\n");
				commandToPassToBackEnd = "";
				while (removeEnter.hasMoreTokens()) {
					commandToPassToBackEnd = commandToPassToBackEnd
							+ removeEnter.nextToken();
				}
				return commandToPassToBackEnd;
			}

			/**
			 * Methods replace the task ID user entered with the real taskID for
			 * example edit 1.add will change to edit 1000013.add whereby 100013
			 * is the real taskID
			 * 
			 * @throws InvalidCommandType
			 */
			private String replaceTempTaskIDWithRealTaskID(
					String commandToPassToBackEnd) throws InvalidCommandType {

				if (commandToPassToBackEnd.startsWith("edit ")
						|| commandToPassToBackEnd.startsWith("-e ")
						|| commandToPassToBackEnd.startsWith("complete ")
						|| commandToPassToBackEnd.startsWith("-c ")
						|| commandToPassToBackEnd.startsWith("incomplete ")
						|| commandToPassToBackEnd.startsWith("-inc ")) {

					String[] splitInputRead = commandToPassToBackEnd.split(" ");

					StringTokenizer replaceWithRealID = new StringTokenizer(
							splitInputRead[1], ".");

					int tempTaskID = Integer.parseInt(replaceWithRealID
							.nextToken());

					HashMap<Integer, Integer> tempTaskIDtoRealTaskID;
					if (commandToPassToBackEnd.startsWith("incomplete")
							|| commandToPassToBackEnd.startsWith("-inc"))
						tempTaskIDtoRealTaskID = completedTempTaskIDtoRealTaskID;
					else
						tempTaskIDtoRealTaskID = openTempTaskIDtoRealTaskID;

					Integer realTaskID = tempTaskIDtoRealTaskID.get(tempTaskID);
					splitInputRead[1] = Integer.toString(realTaskID) + ".";

					commandToPassToBackEnd = splitInputRead[0];
					for (int i = 1; i < splitInputRead.length; i++)
						commandToPassToBackEnd = commandToPassToBackEnd + " "
								+ splitInputRead[i];

				} else if (commandToPassToBackEnd.startsWith("delete -i ")
						|| commandToPassToBackEnd.startsWith("delete -c ")
						|| commandToPassToBackEnd.startsWith("-d -c ")
						|| commandToPassToBackEnd.startsWith("-d -i ")) {
					String[] splitInputRead = commandToPassToBackEnd.split(" ");

					StringTokenizer replaceWithRealID = new StringTokenizer(
							splitInputRead[2], ".");

					int tempTaskID = Integer.parseInt(replaceWithRealID
							.nextToken());

					HashMap<Integer, Integer> tempTaskIDtoRealTaskID = openTempTaskIDtoRealTaskID;
					HashMap<Integer, Integer> CtempTaskIDtoRealTaskID = completedTempTaskIDtoRealTaskID;

					for (int i = 1; i < CtempTaskIDtoRealTaskID.size() + 1; i++)
						tempTaskIDtoRealTaskID.put(i + 1000,
								CtempTaskIDtoRealTaskID.get(i));

					Integer realTaskID = null;
					if (commandToPassToBackEnd.startsWith("delete -c")
							|| commandToPassToBackEnd.startsWith("-d -c")) {
						realTaskID = tempTaskIDtoRealTaskID
								.get(tempTaskID + 1000);
						commandToPassToBackEnd = splitInputRead[0] + " -c";
					} else if (commandToPassToBackEnd.startsWith("delete -i")
							|| commandToPassToBackEnd.startsWith("-d -i")) {
						realTaskID = tempTaskIDtoRealTaskID.get(tempTaskID);
						commandToPassToBackEnd = splitInputRead[0] + " -i";
					}
					assert realTaskID != null : "real task ID would never be null here";

					splitInputRead[1] = Integer.toString(realTaskID) + ".";

					splitInputRead[2] = "";

					for (int i = 1; i < splitInputRead.length; i++)
						commandToPassToBackEnd = commandToPassToBackEnd + " "
								+ splitInputRead[i];

				} else if (commandToPassToBackEnd.startsWith("delete")
						|| commandToPassToBackEnd.startsWith("-d")) {
					throw new InvalidCommandType(
							"delete must be followed with -i or -c");

				}
				return commandToPassToBackEnd;
			}

			private void checkAndPrintFeedbackToUser(Result r) {

				if (r.isSuccessful()) {
					if (r.getCommandType().equalsIgnoreCase("ADD"))
						commandLabel.setText("Task: "
								+ r.getData().get(0).getDetails()
								+ ", added successfully!");
					else if (r.getCommandType().equalsIgnoreCase("SET"))
						commandLabel.setText("Task: "
								+ r.getData().get(0).getDetails()
								+ ", edited successfully!");
					else if (r.getCommandType().equalsIgnoreCase("DELETEOPEN")
							|| r.getCommandType().equalsIgnoreCase(
									"DELETECOMPLETE"))
						commandLabel.setText("Task: "
								+ r.getData().get(0).getDetails()
								+ ", deleted successfully!");
					else if (r.getCommandType().equalsIgnoreCase("SEARCHOPEN")) {
						if (r.getData().size() == 0)
							commandLabel.setText("No search results found!");
						else
							commandLabel
									.setText("Search result populated below. "
											+ r.getData().size()
											+ " results found.");
					} else if (r.getCommandType().equalsIgnoreCase("COMPLETE"))
						commandLabel.setText("Task: "
								+ r.getData().get(0).getDetails()
								+ ", is now completed!");
					else if (r.getCommandType().equalsIgnoreCase("INCOMPLETE"))
						commandLabel.setText("Task: "
								+ r.getData().get(0).getDetails()
								+ ", is now open!");

				} else
					commandLabel
							.setText("Command didn't execute successfully. Error: "
									+ r.getError());
			}

		});
	}

	private static void checkAndDisposeOpenDeleteAll() {
		if (opendeleteAllToolBar != null)
			opendeleteAllToolBar.dispose();
	}
	
	private static void createCompulsoryControls(Composite taskList) {

		// creation of "Open Task" label
		CLabel lblOpenTasks = new CLabel(taskList, SWT.NONE);
		lblOpenTasks.setForeground(SWTResourceManager.getColor(255, 247, 153));
		lblOpenTasks.setFont(SWTResourceManager.getFont("Segoe UI Semibold",
				12, SWT.NORMAL));
		lblOpenTasks.setBounds(60, 20, 100, 23);
		lblOpenTasks.setText("Open Tasks");

		// creation of "Due date" label
		final CLabel lblDueDate = new CLabel(taskList, SWT.NONE);
		lblDueDate.setForeground(SWTResourceManager.getColor(255, 247, 153));
		lblDueDate.setFont(SWTResourceManager.getFont("Segoe UI Semibold", 12,
				SWT.NORMAL));
		lblDueDate.setBounds(410, 20, 100, 23);
		// toolkit.adapt(lblDueDate, true, true);
		lblDueDate.setText("Due Date");
		lblDueDate.addMouseListener(new MouseListener() {
			@Override
			public void mouseDoubleClick(MouseEvent arg0) {
			}

			@Override
			public void mouseDown(MouseEvent arg0) {
				// show start date for all tasks instead
				if (lblDueDate.getText().equals("Due Date")) {
					lblDueDate.setText("Start Date");
					showEndDate = false;
				} else { // show end date for all tasks instead
					lblDueDate.setText("Due Date");

					showEndDate = true;
				}

				String selectedTab = UserInterface.getSelectedTab();

				if (selectedTab.equalsIgnoreCase("Others"))
					selectedTab = "empty";

				ArrayList<String> searchKeyWord = new ArrayList<String>();
				searchKeyWord.add(selectedTab);

				Result openTaskList;
				Result completedTaskList;

				if (selectedTab.equals("All")) {
					openTaskList = Controller.getAllOpenTasks();
					completedTaskList = Controller.getAllCompletedTask();
				} else {
					openTaskList = Controller.searchForOpenTask(searchKeyWord,
							"tag");
					completedTaskList = Controller.searchForCompletedTask(
							searchKeyWord, "tag");
				}

				UserInterface.refreshTabList(openTaskList, completedTaskList);

			}

			@Override
			public void mouseUp(MouseEvent arg0) {
			}
		});
	}

	private static void createOpenTaskDeleteAll(Composite taskList) {
		// creation of "delete all" button for opentask
		opendeleteAllToolBar = new ToolBar(taskList, SWT.FLAT);
		opendeleteAllToolBar.setBounds(37, 23, 40, 23);
		ToolItem deleteAllOpenTask = new ToolItem(opendeleteAllToolBar,
				SWT.NONE);
		deleteAllOpenTask.setImage(SWTResourceManager.getImage(
				UserInterface.class, "/GUI/button_delete.png"));
		deleteAllOpenTask.setToolTipText("Delete all");
		UserInterfaceListener.addDeleteALlOpenTaskListener(deleteAllOpenTask);
	}
	
	private static void disposeAllControls() {

		for (int i = 0; i < 1000; i++) {
			if (openTaskEntireDetail[i] != null)
				openTaskEntireDetail[i].dispose();
			if (completeTaskEntireDetail[i] != null)
				completeTaskEntireDetail[i].dispose();
		}
	}
	
	private static int loadOpenTasks(Composite taskList, Result openTaskList) {

		// variables for counting no. of opentask and mapping for hashmap
		int openTaskCounter = 0;
		int openHashMapCounter = 1;

		// variable for setting the colors of the text
		int colorR = 0;
		int colorG = 0;
		int colorB = 0;

		for (int i = 0; i < openTaskList.getData().size(); i++) {

			storeOpenTaskIdInHashmap(openTaskList, openHashMapCounter, i);

			openTaskCounter++;

			// check if task is overDdue and set the correct RGB colors
			boolean overDue = isOverDue(openTaskList, i);
			if (overDue) {
				colorR = ORANGE_R;
				colorG = ORANGE_G;
				colorB = ORANGE_B;
			} else {
				colorR = YELLOW_R;
				colorG = YELLOW_G;
				colorB = YELLOW_B;
			}

			Color taskTextColor = SWTResourceManager.getColor(colorR, colorG,
					colorB);

			// load the task composite
			openTaskEntireDetail[i] = new Composite(taskList, SWT.NONE);
			openTaskEntireDetail[i].setBounds(0, 50 + i * 23, 550, 23);
			UserInterfaceListener.addCompositeMouseOverListener(
					openTaskEntireDetail, openComplete, openDelete, i,
					openPriority, "open");
			openTaskEntireDetail[i].layout();

			// loading the complete button
			openComplete[i] = new ToolBar(openTaskEntireDetail[i], SWT.NONE);
			openComplete[i].setBounds(5, -3, 23, 23);
			openComplete[i].setVisible(false);
			openComplete[i].setToolTipText("Complete task");
			openCompleteIcon[i] = new ToolItem(openComplete[i], SWT.NONE);
			openCompleteIcon[i].setImage(SWTResourceManager.getImage(
					UserInterface.class, "/GUI/button_complete.png"));
			UserInterfaceListener.addToolBarMouseOverListener(openComplete,
					openComplete, openDelete, i, openPriority, "open");
			UserInterfaceListener.addCompleteMouseListener(openCompleteIcon, i,
					openTaskList.getData().get(i).getId());

			// loading the delete button
			openDelete[i] = new ToolBar(openTaskEntireDetail[i], SWT.NONE);
			openDelete[i].setBounds(28, 0, 21, 21);
			openDelete[i].setVisible(false);
			openDelete[i].setToolTipText("Delete task");
			openDeleteIcon[i] = new ToolItem(openDelete[i], SWT.NONE);
			openDeleteIcon[i].setImage(SWTResourceManager.getImage(
					UserInterface.class, "/GUI/button_delete.png"));
			UserInterfaceListener.addToolBarMouseOverListener(openDelete,
					openComplete, openDelete, i, openPriority, "open");
			UserInterfaceListener.addOpenDeleteMouseListener(openDeleteIcon, i,
					openTaskList.getData().get(i).getId());

			// load the priority button
			openPriority[i] = new ToolBar(openTaskEntireDetail[i], SWT.NONE);
			openPriority[i].setBounds(49, 0, 14, 23);
			if (openTaskList.getData().get(i).getPriority() == 1)
				openPriority[i].setVisible(true);
			else
				openPriority[i].setVisible(false);
			openPriority[i].setToolTipText("Click to mark as important");
			openPriorityIcon[i] = new ToolItem(openPriority[i], SWT.NONE);
			openPriorityIcon[i].setImage(SWTResourceManager.getImage(
					UserInterface.class, "/GUI/button_priority.png"));
			UserInterfaceListener.addPriorityModifyListener(openPriority,
					openPriorityIcon, i, openTaskList.getData().get(i).getId(),
					openTaskList.getData().get(i).getPriority());
			UserInterfaceListener.addToolBarMouseOverListener(openPriority,
					openComplete, openDelete, i, openTaskList.getData().get(i),
					"open");

			// load the taskID
			openTaskID[i] = new CLabel(openTaskEntireDetail[i], SWT.NONE);
			openTaskID[i].setBounds(62, -3, 25, 23);
			openTaskID[i].setText(openHashMapCounter + ".");
			openTaskID[i].setFont(TASK_TEXT_FONT);
			openTaskID[i].setForeground(taskTextColor);
			UserInterfaceListener.addLabelMouseOverListener(openTaskID,
					openComplete, openDelete, i, openPriority, "open");

			// load the description
			openTaskDescription[i] = new StyledText(openTaskEntireDetail[i],
					SWT.NONE);
			openTaskDescription[i].setBounds(91, 0, 300, 23);
			openTaskDescription[i].setText(openTaskList.getData().get(i)
					.getDetails());
			openTaskDescription[i].setForeground(taskTextColor);
			openTaskDescription[i].setFont(TASK_TEXT_FONT);
			UserInterfaceListener.addTaskDescModifyListener(
					openTaskDescription, i, openTaskList.getData().get(i)
							.getId());
			UserInterfaceListener.addStyledTextMouseOverListener(
					openTaskDescription, openComplete, openDelete, i,
					openPriority, "open");

			// load the end date icon
			final String openEndDate = datePopulator(openTaskList, i);
			String openEndDateNiceForm = datePopulatorInNiceForm(openTaskList,
					i);
			openTaskEndDate[i] = new StyledText(openTaskEntireDetail[i],
					SWT.NONE);
			openTaskEndDate[i].setBounds(415, 0, 98, 23);
			openTaskEndDate[i].setText(openEndDateNiceForm);
			openTaskEndDate[i].setFont(TASK_TEXT_FONT);
			openTaskEndDate[i].setForeground(taskTextColor);
			UserInterfaceListener.addDateModifyListener(openTaskEndDate, i,
					openTaskList.getData().get(i).getId(), openEndDate,
					openEndDateNiceForm);
			UserInterfaceListener.addStyledTextMouseOverListener(
					openTaskEndDate, openComplete, openDelete, i, openPriority,
					"open");

			openHashMapCounter++;

		}
		return openTaskCounter;
	}

	private static void checkAndDisposeCompletedTasksLabel() {

		if (lblCompleteTasks != null)
			lblCompleteTasks.dispose();
		if (completeDeleteAllToolBar != null)
			completeDeleteAllToolBar.dispose();
	}

	private static void loadCompletedTasks(Composite taskList,
			Result completedTaskList, int openTaskCounter) {

		createCompulsoryControlForCompletedTask(taskList, openTaskCounter);

		int completeHashMapCounter = 1;

		for (int i = 0; i < completedTaskList.getData().size(); i++) {

			storeCompleteTaskIdInHasMap(completedTaskList,
					completeHashMapCounter, i);

			// load the composite for storing completed task detail
			completeTaskEntireDetail[i] = new Composite(taskList, SWT.NONE);
			completeTaskEntireDetail[i].setBounds(0, 70
					+ ((openTaskCounter + 1) * 23) + i * 23, 600, 23);
			UserInterfaceListener.addCompositeMouseOverListener(
					completeTaskEntireDetail, completeIncomplete,
					completeDelete, i, completePriority, "completed");
			completeTaskEntireDetail[i].layout();

			// loading the incomplete button for completed task
			completeIncomplete[i] = new ToolBar(completeTaskEntireDetail[i],
					SWT.NONE);
			completeIncomplete[i].setBounds(5, -3, 23, 23);
			completeIncomplete[i].setVisible(false);
			completeIncomplete[i].setToolTipText("Incomplete task");
			completeIncompleteIcon[i] = new ToolItem(completeIncomplete[i],
					SWT.NONE);
			completeIncompleteIcon[i].setImage(SWTResourceManager.getImage(
					UserInterface.class, "/GUI/button_incomplete.png"));
			UserInterfaceListener.addToolBarMouseOverListener(
					completeIncomplete, completeIncomplete, completeDelete, i,
					completePriority, "completed");
			UserInterfaceListener.addIncompleteMouseListener(
					completeIncompleteIcon, i,
					completedTaskList.getData().get(i).getId());

			// loading the delete button for completed task
			completeDelete[i] = new ToolBar(completeTaskEntireDetail[i],
					SWT.NONE);
			completeDelete[i].setBounds(28, 0, 21, 21);
			completeDelete[i].setVisible(false);
			completeDelete[i].setToolTipText("Delete task");

			completeDeleteIcon[i] = new ToolItem(completeDelete[i], SWT.NONE);
			completeDeleteIcon[i].setImage(SWTResourceManager.getImage(
					UserInterface.class, "/GUI/button_delete.png"));

			UserInterfaceListener.addToolBarMouseOverListener(completeDelete,
					completeIncomplete, completeDelete, i, completePriority,
					"completed");
			UserInterfaceListener.addcompleteDeleteMouseListener(
					completeDeleteIcon, i, completedTaskList.getData().get(i)
							.getId());

			// loading the priority label
			completePriority[i] = new CLabel(completeTaskEntireDetail[i],
					SWT.NONE);
			completePriority[i].setBounds(49, -5, 14, 23);
			completePriority[i].setText("!");
			if (completedTaskList.getData().get(i).getPriority() == 1)
				completePriority[i].setVisible(true);
			else
				completePriority[i].setVisible(false);
			completePriority[i].setFont(SWTResourceManager.getFont("Segoe UI",
					12, SWT.BOLD));
			completePriority[i].setForeground(SWTResourceManager.getColor(
					ORANGE_R, ORANGE_G, ORANGE_B));
			UserInterfaceListener.addLabelMouseOverListener(completePriority,
					completeIncomplete, completeDelete, i, completePriority,
					"completed");

			// load the taskID
			completeTaskID[i] = new CLabel(completeTaskEntireDetail[i],
					SWT.NONE);
			completeTaskID[i].setBounds(62, -3, 25, 23);
			completeTaskID[i].setText(completeHashMapCounter + ".");
			completeTaskID[i].setFont(TASK_TEXT_FONT);
			completeTaskID[i].setForeground(SWTResourceManager.getColor(
					YELLOW_R, YELLOW_G, YELLOW_B));
			UserInterfaceListener.addLabelMouseOverListener(completeTaskID,
					completeIncomplete, completeDelete, i, completePriority,
					"completed");

			// load the description
			completeTaskDescription[i] = new StyledText(
					completeTaskEntireDetail[i], SWT.READ_ONLY);
			completeTaskDescription[i].setBounds(91, 0, 300, 23);
			completeTaskDescription[i].setText(completedTaskList.getData()
					.get(i).getDetails());
			completeTaskDescription[i].setFont(TASK_TEXT_FONT);
			completeTaskDescription[i].setForeground(SWTResourceManager
					.getColor(YELLOW_R, YELLOW_G, YELLOW_B));

			// load the end date
			String completeEndDate = datePopulator(completedTaskList, i);
			completeTaskEndDate[i] = new StyledText(
					completeTaskEntireDetail[i], SWT.READ_ONLY);
			completeTaskEndDate[i].setBounds(415, 0, 98, 23);
			completeTaskEndDate[i].setText(completeEndDate);
			completeTaskEndDate[i].setFont(TASK_TEXT_FONT);
			completeTaskEndDate[i].setForeground(SWTResourceManager.getColor(
					YELLOW_R, YELLOW_G, YELLOW_B));

			completeHashMapCounter++;

		}
	}

	// #############################################################################################
	// Seventh Level abstraction
	// #############################################################################################
	
	/**
	 * refreshTabList refresh the tasklist in the window according to the result
	 * that the user has passed in.
	 * 
	 * @param openTaskList
	 *            - set null to show all openTask
	 * @param completedTaskList
	 *            - set null to show all completedTask
	 */
	public static void refreshTabList(Result openTaskList,
			Result completedTaskList) {

		if (openTaskList == null && completedTaskList == null)
			setSelectedTab("All");

		getTaskAndPopulateTask(midTaskList, midScrolledComposite, openTaskList,
				completedTaskList);
		TabListWindow.refreshList();
	}
	
	private static void storeOpenTaskIdInHashmap(Result openTaskList,
			int openHashMapCounter, int i) {
		openRealTaskIDtoTempTaskID.put(openTaskList.getData().get(i).getId(),
				openHashMapCounter);
		openTempTaskIDtoRealTaskID.put(openHashMapCounter, openTaskList
				.getData().get(i).getId());
	}

	private static void storeCompleteTaskIdInHasMap(Result completedTaskList,
			int completeHashMapCounter, int i) {
		completedRealTaskIDtoTempTaskID.put(completedTaskList.getData().get(i)
				.getId(), completeHashMapCounter);
		completedTempTaskIDtoRealTaskID.put(completeHashMapCounter,
				completedTaskList.getData().get(i).getId());
	}
	
	private static void createCompulsoryControlForCompletedTask(
			Composite taskList, int openTaskCounter) {

		// load the "Complete task" label
		lblCompleteTasks = new CLabel(taskList, SWT.NONE);
		lblCompleteTasks.setBounds(60, 40 + ((openTaskCounter + 1) * 23), 150,
				23);
		lblCompleteTasks.setFont(SWTResourceManager.getFont(
				"Segoe UI Semibold", 12, SWT.NORMAL));
		lblCompleteTasks.setForeground(SWTResourceManager.getColor(255, 247,
				153));
		lblCompleteTasks.setText("Completed Tasks");
		lblCompleteTasks.redraw();

		// load the delete all button
		completeDeleteAllToolBar = new ToolBar(taskList, SWT.FLAT);
		// deleteAllToolBar.setBounds(23, 30 + ((openTaskCounter + 1) * 25),
		// 40,23);
		completeDeleteAllToolBar.setBounds(37,
				44 + ((openTaskCounter + 1) * 23), 40, 23);
		ToolItem deleteAllCompleteTask = new ToolItem(completeDeleteAllToolBar,
				SWT.NONE);
		deleteAllCompleteTask.setImage(SWTResourceManager.getImage(
				UserInterface.class, "/GUI/button_delete.png"));
		deleteAllCompleteTask.setToolTipText("Delete all");
		UserInterfaceListener
				.addDeleteAllCompleteTaskListener(deleteAllCompleteTask);
		completeDeleteAllToolBar.redraw();
	}
	
	// #############################################################################################
	// Codes for highlighting multiple special syntax
	// #############################################################################################

	private void highlightSpecialSyntax() {

			clearAllStyleRange();
			checkforMultipleEndDate();
			checkforMultipleStartDate();
			checkForMultiplePriority();
			checkForMultipleTag();
		}

	private void clearAllStyleRange() {

			StyleRange[] allStyleRanges = commandLineInterface.getStyleRanges();
			for (int i = 0; i < allStyleRanges.length; i++) {
				allStyleRanges[i].length = 0;
			}
			int lengthOfCommandLine = commandLineInterface.getText().trim()
					.length();

			commandLineInterface.replaceStyleRanges(0, lengthOfCommandLine - 1,
					allStyleRanges);

		}

	private void checkforMultipleStartDate() {
			int howManyStartDateCmd = 0;
			int[] tagCounter = new int[1000];
			int[] tagCounterLength = new int[1000];
			int currentCharIndex = 0;
			boolean tagFound = false;

			StringTokenizer tagTokenizer = new StringTokenizer(commandLineInterface
					.getText().trim());
			while (tagTokenizer.hasMoreTokens()) {
				String foundWord = tagTokenizer.nextToken();

				if (foundWord.equalsIgnoreCase("-sd")) {
					tagCounter[howManyStartDateCmd] = currentCharIndex;
					tagCounterLength[howManyStartDateCmd] = 4 + tagCounterLength[howManyStartDateCmd];
					howManyStartDateCmd++;
					tagFound = true;
				} else if (!foundWord.equalsIgnoreCase("-p")
						&& !foundWord.equalsIgnoreCase("-ed")
						&& !foundWord.equalsIgnoreCase("-t")) {
					if (tagFound) {
						tagCounterLength[howManyStartDateCmd - 1] = tagCounterLength[howManyStartDateCmd - 1]
								+ foundWord.length() + 1;
					}
				} else
					tagFound = false;
				currentCharIndex = currentCharIndex + foundWord.length() + 1;
			}

			loadStyleRangeIntoCLI(howManyStartDateCmd, tagCounter, tagCounterLength);

		}

	private void checkforMultipleEndDate() {
			int howManyEndDateCmd = 0;
			int[] tagCounter = new int[1000];
			int[] tagCounterLength = new int[1000];
			int currentCharIndex = 0;
			boolean tagFound = false;

			StringTokenizer tagTokenizer = new StringTokenizer(commandLineInterface
					.getText().trim());
			while (tagTokenizer.hasMoreTokens()) {
				String foundWord = tagTokenizer.nextToken();

				if (foundWord.equalsIgnoreCase("-ed")) {
					tagCounter[howManyEndDateCmd] = currentCharIndex;
					tagCounterLength[howManyEndDateCmd] = 4 + tagCounterLength[howManyEndDateCmd];
					howManyEndDateCmd++;
					tagFound = true;
				} else if (!foundWord.equalsIgnoreCase("-p")
						&& !foundWord.equalsIgnoreCase("-sd")
						&& !foundWord.equalsIgnoreCase("-t")) {
					if (tagFound) {
						tagCounterLength[howManyEndDateCmd - 1] = tagCounterLength[howManyEndDateCmd - 1]
								+ foundWord.length() + 1;
					}
				} else
					tagFound = false;
				currentCharIndex = currentCharIndex + foundWord.length() + 1;

			}

			loadStyleRangeIntoCLI(howManyEndDateCmd, tagCounter, tagCounterLength);

		}

	private void checkForMultiplePriority() {
			int howManyPriorityCmd = 0;
			int[] tagCounter = new int[1000];
			int[] tagCounterLength = new int[1000];
			int currentCharIndex = 0;
			boolean tagFound = false;

			StringTokenizer tagTokenizer = new StringTokenizer(commandLineInterface
					.getText().trim());
			while (tagTokenizer.hasMoreTokens()) {
				String foundWord = tagTokenizer.nextToken();

				if (foundWord.equalsIgnoreCase("-p")) {
					tagCounter[howManyPriorityCmd] = currentCharIndex;
					tagCounterLength[howManyPriorityCmd] = 3 + tagCounterLength[howManyPriorityCmd];
					howManyPriorityCmd++;
					tagFound = true;
				} else if (!foundWord.equalsIgnoreCase("-ed")
						&& !foundWord.equalsIgnoreCase("-sd")
						&& !foundWord.equalsIgnoreCase("-t")) {
					if (tagFound) {
						tagCounterLength[howManyPriorityCmd - 1] = tagCounterLength[howManyPriorityCmd - 1]
								+ foundWord.length() + 1;
					}
				} else
					tagFound = false;
				currentCharIndex = currentCharIndex + foundWord.length() + 1;
			}

			loadStyleRangeIntoCLI(howManyPriorityCmd, tagCounter, tagCounterLength);

		}

	private void checkForMultipleTag() {
			int howManyTagCmd = 0;
			int[] tagCounter = new int[1000];
			int[] tagCounterLength = new int[1000];
			int currentCharIndex = 0;
			boolean tagFound = false;

			StringTokenizer tagTokenizer = new StringTokenizer(commandLineInterface
					.getText().trim());
			while (tagTokenizer.hasMoreTokens()) {
				String foundWord = tagTokenizer.nextToken();

				if (foundWord.equalsIgnoreCase("-t")) {
					tagCounter[howManyTagCmd] = currentCharIndex;
					tagCounterLength[howManyTagCmd] = 3 + tagCounterLength[howManyTagCmd];
					howManyTagCmd++;
					tagFound = true;
				} else if (!foundWord.equalsIgnoreCase("-ed")
						&& !foundWord.equalsIgnoreCase("-sd")
						&& !foundWord.equalsIgnoreCase("-p")) {
					if (tagFound) {
						tagCounterLength[howManyTagCmd - 1] = tagCounterLength[howManyTagCmd - 1]
								+ foundWord.length() + 1;
					}
				} else
					tagFound = false;
				currentCharIndex = currentCharIndex + foundWord.length() + 1;
			}

			loadStyleRangeIntoCLI(howManyTagCmd, tagCounter, tagCounterLength);

		}

	private void loadStyleRangeIntoCLI(int howManyStartDateCmd,
				int[] tagCounter, int[] tagCounterLength) {
			for (int i = 0; i < howManyStartDateCmd - 1; i++) {
				StyleRange multipleStyleRange = new StyleRange();

				multipleStyleRange.start = tagCounter[i];
				multipleStyleRange.strikeoutColor = getDisplay().getSystemColor(
						SWT.COLOR_BLUE);
				multipleStyleRange.strikeout = true;
				multipleStyleRange.length = tagCounterLength[i];

				commandLineInterface.setStyleRange(multipleStyleRange);
			}
		}
	
	/**
	 * Given a result with an event inside, methods aim to extract the event's
	 * date and put it into the form of dd/mm/yyyy back to user.
	 * 
	 * If year is not available, method aim to return only dd/mm
	 */
	protected static String datePopulator(Result taskList, int i) {

		assert taskList.getData().size() > 0 : "Parameter passed in is incorrect!";

		String details = "";

		Calendar taskDate;

		if (showEndDate == true)
			taskDate = (Calendar) taskList.getData().get(i).getEndDate();// .clone();
		else
			taskDate = (Calendar) taskList.getData().get(i).getStartDate();// .clone();

		int monthOfEndDateTask = taskDate.get(Calendar.MONTH);
		monthOfEndDateTask++;
		int dayOfEndDateTask = taskDate.get(Calendar.DATE);
		int yearOfEndDateTask = taskDate.get(Calendar.YEAR);

		if (yearOfEndDateTask != NO_YEAR_SET && monthOfEndDateTask != 0) {
			if (yearOfEndDateTask != NO_YEAR_SET)
				details = dayOfEndDateTask + "/" + monthOfEndDateTask + "/"
						+ yearOfEndDateTask;
			else
				details = dayOfEndDateTask + "/" + monthOfEndDateTask;

		}

		return details;
	}

	/**
	 * Given a result with an event inside, methods aim to extract the event's
	 * date and return a human readable form.
	 * 
	 * For events last than 8 days away, method returns "In x days" For events
	 * within the year, method return "Oct 8" for events not within the year,
	 * method return "Aug 10 2011"
	 * 
	 */
	protected static String datePopulatorInNiceForm(Result taskList, int i) {

		String details = "";
		String[] month = { "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul",
				"Aug", "Sept", "Oct", "Nov", "Dec" };

		// seperate taskEndDate out into different segment
		Calendar taskDate;

		if (showEndDate == true)
			taskDate = (Calendar) taskList.getData().get(i).getEndDate();
		else
			taskDate = (Calendar) taskList.getData().get(i).getStartDate();

		int monthOfEndDateTask = taskDate.get(Calendar.MONTH);
		int dayOfEndDateTask = taskDate.get(Calendar.DATE);
		int yearOfEndDateTask = taskDate.get(Calendar.YEAR);
		taskDate.clear();
		taskDate.set(yearOfEndDateTask, monthOfEndDateTask, dayOfEndDateTask);

		// seperate today date out into different segment
		Calendar todayDate = Calendar.getInstance();
		int monthOfToday = todayDate.get(Calendar.MONTH);
		int dayOfToday = todayDate.get(Calendar.DATE);
		int yearOfToday = todayDate.get(Calendar.YEAR);
		todayDate.clear();
		todayDate.set(yearOfToday, monthOfToday, dayOfToday);

		if (yearOfEndDateTask != NO_YEAR_SET) // no end date is set
		{
			int daysBetween = daysBetween(todayDate, taskDate);

			if (daysBetween < 8 && daysBetween >= 0) {
				if (daysBetween == 0)
					details = "Today";
				else if (daysBetween == 1)
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

	/**
	 * Methods check task end date given is overduei
	 * 
	 * @return true if over due, false if not overdue
	 */
	private static boolean isOverDue(Result taskList, int i) {

		Calendar taskEndDate = (Calendar) taskList.getData().get(i)
				.getEndDate();

		int monthOfEndDateTask = taskEndDate.get(Calendar.MONTH);
		int dayOfEndDateTask = taskEndDate.get(Calendar.DATE);
		int yearOfEndDateTask = taskEndDate.get(Calendar.YEAR);
		taskEndDate.clear();
		taskEndDate
				.set(yearOfEndDateTask, monthOfEndDateTask, dayOfEndDateTask);

		Calendar todayDate = Calendar.getInstance();
		int monthOfToday = todayDate.get(Calendar.MONTH);
		int dayOfToday = todayDate.get(Calendar.DATE);
		int yearOfToday = todayDate.get(Calendar.YEAR);
		todayDate.clear();
		todayDate.set(yearOfToday, monthOfToday, dayOfToday);

		int dayBetween = daysBetween(todayDate, taskEndDate);

		if (dayBetween >= 0)
			return false;
		else
			return true;

	}

	// #############################################################################################
	// Eight Level abstraction
	// #############################################################################################
	
	/**
	 * method calculate the days between the 2 calendar.
	 * 
	 * if the first calendar is after the second calendar, method returns -1
	 * instead of daysBetween.
	 */
	private static int daysBetween(Calendar todayDate, Calendar taskEndDate) {

		if (todayDate.equals(taskEndDate))
			return 0;

		if (todayDate.after(taskEndDate)) {
			return -1;
		}
		return (int) ((taskEndDate.getTime().getTime() - todayDate.getTime()
				.getTime()) / (1000 * 60 * 60 * 24));
	}
	

	// #############################################################################################
	// Codes for global hot keys listener
	// #############################################################################################

	@Override
	public void onIntellitype(int arg0) {
	}

	@Override
	public void onHotKey(int aIdentifier) {

		if (aIdentifier == 90) {
			String copiedContent = "";
			Clipboard clipboard = Toolkit.getDefaultToolkit()
					.getSystemClipboard();
			// odd: the Object param of getContents is not currently used
			Transferable contents = clipboard.getContents(null);
			boolean hasTransferableText = (contents != null)
					&& contents.isDataFlavorSupported(DataFlavor.stringFlavor);
			if (hasTransferableText) {
				try {
					copiedContent = (String) contents
							.getTransferData(DataFlavor.stringFlavor);
				} catch (UnsupportedFlavorException ex) {
					// highly unlikely since we are using a standard DataFlavor
					Controller
							.logSystemExceptionError("UserInterface.onHotKey() Unsupported flavor "
									+ ex.getMessage());
				} catch (IOException ex) {
					Controller
							.logSystemExceptionError("UserInterface.onHotKey() IOException "
									+ ex.getMessage());
				}
			}

			final String textToPasteToDialog = copiedContent;

			getDisplay().syncExec(new Runnable() {
				@Override
				public void run() {
					getShell().open();
					getShell().forceActive();
					getShell().setVisible(true);
					commandLineInterface.setText(textToPasteToDialog);
					setCommandLineInterfaceTextCursorAtEnd();
					commandLineInterface.setFocus();
				}
			});

		} else if (aIdentifier == 91) {
			getDisplay().syncExec(new Runnable() {
				@Override
				public void run() {

					if (getShell().isVisible() == false) // show the program
					{
						getShell().open();
						getShell().forceActive();
						getShell().setVisible(true);
					} else if (getShell().isVisible() == true) // hide the
																// program
					{
						UserInterface.getFocusToMainWindow();
						getShell().setVisible(false);

						final ToolTip tip = new ToolTip(getShell(), SWT.BALLOON
								| SWT.ICON_INFORMATION);
						tip.setMessage("GeekDo is now minimized");
						getDisplay().getSystemTray().getItems()[0]
								.setToolTip(tip);
						tip.setVisible(true);

					}
				}
			});
		}

	}
	
	
	// #############################################################################################
	// UserInterface API for class of GUI package
	// #############################################################################################

	/**
	 * Methods will set the entire task's detail/end date/task ID to overdue
	 * color(Orange)
	 * 
	 * @param index
	 *            an index in the array of open tasks
	 */
	protected static void setTaskOverDueColor(int index) {

		try {
			openTaskDescription[index].setForeground(SWTResourceManager
					.getColor(ORANGE_R, ORANGE_G, ORANGE_B));
			openTaskEndDate[index].setForeground(SWTResourceManager.getColor(
					ORANGE_R, ORANGE_G, ORANGE_B));
			openTaskID[index].setForeground(SWTResourceManager.getColor(
					ORANGE_R, ORANGE_G, ORANGE_B));
		} catch (NullPointerException e) {
			Controller
					.logSystemExceptionError("Null pointer detected in UserInterfacesetTaskOverDueColor()");
		}
	}

	protected static void setTaskNotOverDueColor(int index) {
		try {
			openTaskDescription[index].setForeground(SWTResourceManager
					.getColor(YELLOW_R, YELLOW_G, YELLOW_B));
			openTaskEndDate[index].setForeground(SWTResourceManager.getColor(
					YELLOW_R, YELLOW_G, YELLOW_B));
			openTaskID[index].setForeground(SWTResourceManager.getColor(
					YELLOW_R, YELLOW_G, YELLOW_B));
		} catch (NullPointerException e) {
			Controller
					.logSystemExceptionError("Null pointer detected in UserInterface.setTaskNotOverDueColor");
		}
	}

	protected static void getFocusToMainWindow() {
		commandLineInterface.setFocus();
	}

	/**
	 * Method will store the selected tab user clicked into UserInterface This
	 * is to facilitate refreshing of the midComposite with the selectedTab
	 * 
	 * @param selectedTab
	 */
	protected static void setSelectedTab(String selectedTab) {

		assert selectedTab.length() != 0 : "SelectedTab can never be empty";
		SelectedTab = selectedTab;
	}

	public static String getSearchCommand() {
		return searchCommand;
	}

	public static boolean isTaskListSearchResult() {
		return isTaskListSearchResult;
	}

	public static void setTaskListisSearchResult(boolean setter) {
		isTaskListSearchResult = setter;
	}

	protected static String getSelectedTab() {
		return SelectedTab;
	}

	protected static int getNoOfDaysForAlertReminder() {
		return noOfDaysForAlert;
	}

	protected static boolean isShowEndDAte() {
		return showEndDate;
	}

	protected static void setCommandLabel(String text) {
		commandLabel.setText(text);
	}

	protected static void setCommandLineInterfaceTextCursorAtEnd() {
		commandLineInterface.setSelection(commandLineInterface.getText()
				.length());
	}

	protected static HashMap<Integer, Integer> getcompletedRealTaskIDtoTempTaskID() {
		return completedRealTaskIDtoTempTaskID;
	}

	protected static HashMap<Integer, Integer> getcompletedTempTaskIDtoRealTaskID() {
		return completedTempTaskIDtoRealTaskID;
	}

	protected static HashMap<Integer, Integer> getopenRealTaskIDtoTempTaskID() {
		return openRealTaskIDtoTempTaskID;
	}

	protected static HashMap<Integer, Integer> getopenTempTaskIDtoRealTaskID() {
		return openTempTaskIDtoRealTaskID;
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
