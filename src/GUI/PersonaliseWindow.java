/**
 * PersonaliseWindow is the class that display to user the dialog when user
 * click on "personalize" from system tray menu
 * 
 * It allows user to change the global hot key settings as well as setting
 * the alert reminder.
 * 
 * @author      Steve Ng 
 * @version     0.2                                   
 * @since       2010-10-30    
 * 
 * 
 */
package GUI;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.util.StringTokenizer;

import logic.Controller;

import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.wb.swt.SWTResourceManager;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormAttachment;


public class PersonaliseWindow extends Composite {

	private Text txtQuickAdd;
	private Text txtQuickShow;
	private Text txtAlertDayCount;
	
	private static Shell personaliseWindowDialog;
	private static Composite bottomComposite;
	
	private static boolean isOpen = false;
	private static final int X_LENGTH_OF_GUI = 432;
	private static final int Y_LENGTH_OF_GUI = 293;
	
	private static final int YELLOW_R = 255;
	private static final int YELLOW_G = 247;
	private static final int YELLOW_B = 153;
	
	// variables for draganddrop capabilities of the composite
	private boolean isMouseNotClicked = true;
	private Point locationOfBeforeClicking;

	public PersonaliseWindow(Composite parent, int style) {
		super(parent, style);

		setLayout(new FormLayout());
		
		isOpen = true;
		
		personaliseWindowDialog = new Shell(getDisplay(), SWT.NONE);
		personaliseWindowDialog.setSize(X_LENGTH_OF_GUI,Y_LENGTH_OF_GUI);
		personaliseWindowDialog.setBackgroundImage(SWTResourceManager.getImage(UserInterface.class, "/GUI/GUI_background.png"));
		personaliseWindowDialog.setBackgroundMode(SWT.INHERIT_DEFAULT);
		personaliseWindowDialog.setLayout(new FormLayout());
		
		FormData fd_bottomComposite = createBottomCompositeAndItsControls();
		
		createTopCompositeAndItsControls(fd_bottomComposite);
		
		readConfigurationFileForHotKeysSettings();
	
		personaliseWindowDialog.pack();
		personaliseWindowDialog.open();
		
	}
	
	// #############################################################################################
	// Second level abstraction
	// #############################################################################################

	private FormData createBottomCompositeAndItsControls() {

		//creation of the composite
		bottomComposite = new Composite(personaliseWindowDialog, SWT.NONE);
		FormData fd_bottomComposite = new FormData();
		fd_bottomComposite.bottom = new FormAttachment(0);
		fd_bottomComposite.right = new FormAttachment(0);
		fd_bottomComposite.top = new FormAttachment(0);
		fd_bottomComposite.left = new FormAttachment(0);
		bottomComposite.setLayoutData(fd_bottomComposite);
		fd_bottomComposite.bottom = new FormAttachment(100, -10);
		fd_bottomComposite.left = new FormAttachment(0);
		fd_bottomComposite.right = new FormAttachment(100);
		bottomComposite.setLayoutData(fd_bottomComposite);
		bottomComposite.setBackgroundMode(SWT.INHERIT_DEFAULT);
		bottomComposite.setBackgroundImage(SWTResourceManager.getImage(UserInterface.class, "/GUI/GUI_background.png"));
		
		creationOfQuickAddControls();
		
		creationOfQuickShowControls();
		
		creationOfAlertControls();
		
		final Label outputMessage = creationOfOutputMessageLabel();
		
		createSaveAndRestoreDefaultButtons(outputMessage);
		
		return fd_bottomComposite;
	}
	
	private void createTopCompositeAndItsControls(FormData fd_bottomComposite) {
		
		//creation of top composite
		Composite topComposite = new Composite(personaliseWindowDialog, SWT.NONE);
		FormData fd_topComposite = new FormData();
		fd_topComposite.bottom = new FormAttachment(0);
		fd_topComposite.right = new FormAttachment(0);
		fd_topComposite.top = new FormAttachment(0);
		fd_topComposite.left = new FormAttachment(0);
		topComposite.setLayoutData(fd_topComposite);
		fd_bottomComposite.top = new FormAttachment(0, 80);
			fd_topComposite.bottom = new FormAttachment(bottomComposite, -6);
		fd_topComposite.right = new FormAttachment(0, 445);
		fd_topComposite.top = new FormAttachment(0);
		fd_topComposite.left = new FormAttachment(0);
		topComposite.setLayoutData(fd_topComposite);
		topComposite.setBackgroundImage(SWTResourceManager.getImage(UserInterface.class, "/GUI/Alertwindow_background.png"));
		topComposite.setBackgroundMode(SWT.INHERIT_DEFAULT);
		createDragCapabilities(topComposite);
		
		//creation of the close button
		ToolBar toolBarForCloseButton = new ToolBar(topComposite, SWT.FLAT);
		toolBarForCloseButton.setBackground(SWTResourceManager.getColor(SWT.TRANSPARENT));
		toolBarForCloseButton.setBounds(390,7, 40, 40);
		ToolItem buttonClose = new ToolItem(toolBarForCloseButton, SWT.NONE);
		buttonClose.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				isOpen = false;
				personaliseWindowDialog.close();
				
			}
		});
		buttonClose.setImage(SWTResourceManager.getImage(UserInterface.class, "/GUI/button_close.png"));
		
		//creation of the label "Hotkey Settings"
		Label lblHotkeys = new Label(topComposite, SWT.NONE);
		lblHotkeys.setFont(SWTResourceManager.getFont("Segoe UI", 14, SWT.NORMAL));
		lblHotkeys.setForeground(SWTResourceManager.getColor(255,255,0));
		lblHotkeys.setBounds(10, 7, 160, 25);
		lblHotkeys.setText("Hotkey settings");
		createDragCapabilities(lblHotkeys);
		
		//creation of the label "Please ensure..."
		Label lblPleaseEnsreThat = new Label(topComposite, SWT.NONE);
		lblPleaseEnsreThat.setBounds(10, 53, 408, 15);
		lblPleaseEnsreThat.setFont(SWTResourceManager.getFont("Segoe UI",8, SWT.NORMAL));
		lblPleaseEnsreThat.setForeground(SWTResourceManager.getColor(255,247,153));
		lblPleaseEnsreThat.setText("Please ensure that the hotkey selected does not clash with other applications");
		createDragCapabilities(lblPleaseEnsreThat);
	}
	
	private void readConfigurationFileForHotKeysSettings() {
		File inFile = createFile();
		
		assert inFile!=null:"ConfigurationHotKeys file error, should have been handled from UserInterface at startup!";
		
		try {
			LineNumberReader fileReader = new LineNumberReader(new FileReader(inFile));
			for(int i=0;i<3;i++)
			{
				StringTokenizer seperateDelimeter = new StringTokenizer(fileReader.readLine());
				seperateDelimeter.nextToken();
				String hotkey = seperateDelimeter.nextToken();
				if(i==0)
					txtQuickAdd.setText(hotkey);
				else if(i==1)
					txtQuickShow.setText(hotkey);
				else if(i==2)
					txtAlertDayCount.setText(hotkey);
			}
			
		}catch (IOException e1){
			Controller.logSystemExceptionError("PersonaliseWindow.readConfigurationFileForHotKeysSettings IOException " + e1.getMessage());
		}catch (NullPointerException e){
			Controller.logSystemExceptionError("PersonaliseWindow.readConfigurationFileForHotKeysSettings NullPointer "+e.getMessage());
		}
	}

	// #############################################################################################
	// Third level abstraction
	// #############################################################################################

	private void creationOfQuickAddControls() {
		Label lblQuickAdd = new Label(bottomComposite, SWT.NONE);
		lblQuickAdd.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.NORMAL));
		lblQuickAdd.setForeground(SWTResourceManager.getColor(YELLOW_R, YELLOW_G, YELLOW_B));
		lblQuickAdd.setBounds(10, 13, 72, 20);
		lblQuickAdd.setText("Quick Add :");
		
		Label lblQuickAddCtrlShift = new Label(bottomComposite, SWT.NONE);
		lblQuickAddCtrlShift.setBounds(136, 13, 76, 20);
		lblQuickAddCtrlShift.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.NORMAL));
		lblQuickAddCtrlShift.setForeground(SWTResourceManager.getColor(YELLOW_R,YELLOW_G, YELLOW_B));
		lblQuickAddCtrlShift.setText("Ctrl + Shift +");
		
		txtQuickAdd = new Text(bottomComposite, SWT.BORDER);
		txtQuickAdd.setBounds(218, 10, 29, 21);
	}
	
	private void creationOfQuickShowControls() {
		Label lblQuickShow = new Label(bottomComposite, SWT.NONE);
		lblQuickShow.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.NORMAL));
		lblQuickShow.setForeground(SWTResourceManager.getColor(YELLOW_R, YELLOW_G, YELLOW_B));
		lblQuickShow.setBounds(10, 39, 102, 20);
		lblQuickShow.setText("Quick Show/Hide : ");
		
		Label lblQuickShowCtrlShift = new Label(bottomComposite, SWT.NONE);
		lblQuickShowCtrlShift.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.NORMAL));
		lblQuickShowCtrlShift.setForeground(SWTResourceManager.getColor(YELLOW_R, YELLOW_G, YELLOW_B));
		lblQuickShowCtrlShift.setBounds(136, 39, 76, 20);
		lblQuickShowCtrlShift.setText("Ctrl + Shift +");
		
		txtQuickShow = new Text(bottomComposite, SWT.BORDER);
		txtQuickShow.setBounds(218, 36, 29, 21);
	}

	private void creationOfAlertControls() {
		Label lblAlerts = new Label(bottomComposite, SWT.NONE);
		lblAlerts.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
		lblAlerts.setForeground(SWTResourceManager.getColor(255,247,153));
		lblAlerts.setBounds(10, 75, 55, 20);
		lblAlerts.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.BOLD | SWT.ITALIC));
		lblAlerts.setText("Alerts");
		
		Label lblShowTasks = new Label(bottomComposite, SWT.NONE);
		lblShowTasks.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.NORMAL));
		lblShowTasks.setForeground(SWTResourceManager.getColor(YELLOW_R, YELLOW_G, YELLOW_B));
		lblShowTasks.setBounds(10, 96, 64, 20);
		lblShowTasks.setText("Show tasks ");
		
		txtAlertDayCount = new Text(bottomComposite, SWT.BORDER);
		txtAlertDayCount.setBounds(80, 93, 29, 21);
		
		Label lblDaysBeforeDue = new Label(bottomComposite, SWT.NONE);
		lblDaysBeforeDue.setBounds(113, 96, 125, 20);
		lblDaysBeforeDue.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.NORMAL));
		lblDaysBeforeDue.setForeground(SWTResourceManager.getColor(YELLOW_R, YELLOW_G, YELLOW_B));
		lblDaysBeforeDue.setText("days before due date");
	}

	private Label creationOfOutputMessageLabel() {
		final Label outputMessage = new Label(bottomComposite, SWT.NONE);
		outputMessage.setBounds(10, 129, 365, 31);
		outputMessage.setForeground(SWTResourceManager.getColor(255,247,153));
		outputMessage.setForeground(SWTResourceManager.getColor(239,171,8));
		return outputMessage;
	}

	private void createSaveAndRestoreDefaultButtons(final Label outputMessage) {
	
		//creation of restore default button
		ToolBar toolBarForRestoreDefaultButton = new ToolBar(bottomComposite, SWT.FLAT);
		toolBarForRestoreDefaultButton.setBounds(10,160, 120, 36);
		toolBarForRestoreDefaultButton.setBackgroundMode(SWT.INHERIT_DEFAULT);
		ToolItem buttonRestoreDefault = new ToolItem(toolBarForRestoreDefaultButton, SWT.NONE);
		buttonRestoreDefault.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				txtQuickAdd.setText("A");
				txtQuickShow.setText("S");
				txtAlertDayCount.setText("3");
			}
		});
		buttonRestoreDefault.setImage(SWTResourceManager.getImage(UserInterface.class, "/GUI/button_restoredefault.png"));

		//creation of the save changes button
		ToolBar toolBarForSaveChangesButton = new ToolBar(bottomComposite, SWT.FLAT);
		toolBarForSaveChangesButton.setBounds(300,160, 120, 36);
		toolBarForSaveChangesButton.setBackgroundMode(SWT.INHERIT_DEFAULT);
		ToolItem buttonSaveChanges = new ToolItem(toolBarForSaveChangesButton, SWT.NONE);
		buttonSaveChanges.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					PrintWriter fileWriter = new PrintWriter(createFile());
					String[] hotkeys = new String[3];
					hotkeys[0] = txtQuickAdd.getText();
					hotkeys[1] = txtQuickShow.getText();
					hotkeys[2] = txtAlertDayCount.getText();
					
					
					Integer.parseInt(hotkeys[2]);
					
					if(hotkeys[0].length()==1 && hotkeys[1].length()==1 && hotkeys[2].length()==1)
					{
					fileWriter.println("QuickAdd= "+hotkeys[0]);
					fileWriter.println("QuickShowAndHide= "+hotkeys[1]);
					fileWriter.println("NoOfDaysForEndTask= "+hotkeys[2]);
					
					fileWriter.println("\n\n\n");
					fileWriter.println("##########################################################################################################");
					fileWriter.println("Warning: Please Do not Edit this file \n Editing of this file might render your application to not work");
					fileWriter.println("##########################################################################################################");
					
					fileWriter.flush();
					fileWriter.close();
					outputMessage.setText("Updated changes.\n Please reset the program to apply the change.");
					}
					else outputMessage.setText("Please check your input. \n Must have only one character for hotkey and only digit for alert's days");

				} catch (NumberFormatException e1) {
					outputMessage.setText("Please check your input. \n Only one character for hotkey and only digit for alert's days");
				} catch (FileNotFoundException e2) {
					Controller.logSystemExceptionError("File not found in PersonaliseWindow.buttonSaveChanges.widgetSelected() " +e2.getMessage());
				}
			}
		});
		buttonSaveChanges.setImage(SWTResourceManager.getImage(UserInterface.class, "/GUI/button_savechanges.png"));
	}

	private File createFile() {
		File inFile = null;
		try {
			inFile = new File("configurationHotKeys.txt");
			inFile.createNewFile();
		} catch (IOException e) {
			Controller.logSystemExceptionError("PersonaliseWindow.createFile() IOException " +e.getMessage());
		}
		
		assert inFile!=null:"inFile should be not null at this point";
		
		return inFile;
	}

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
	
	// #############################################################################################
	// Package GUI API
	// #############################################################################################
	public static boolean getIsOpen()
	{
		return isOpen;
	}
	
}


