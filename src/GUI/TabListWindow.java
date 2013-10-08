/**
 * TabListWindow is the class that lods what all the user's tags in the bottomComposite of UserInterface
 * 
 * @author      Steve Ng 
 * @CoAuthor	Lee Zhi Xin
 * @version     0.2                                   
 * @since       2010-10-30    
 * 
 */

package GUI;

import java.util.ArrayList;

import logic.Controller;
import objects.Result;

import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.wb.swt.SWTResourceManager;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

public class TabListWindow extends Composite {

	private static Composite tagListComposite;
	private static ScrolledComposite tagsListScrolledComposite;
	private static ArrayList<CLabel> tabDetail = new ArrayList<CLabel>();
	private static CLabel lblAllTags;
	private static final int LENGTH_OF_TAGLIST = 450;
	
	// Final variables for RGB colors of orange and yellow
	// Orange for tasks that are overdue
	private static final int ORANGE_R = 239;
	private static final int ORANGE_G = 171;
	private static final int ORANGE_B = 8;
	// Yellow for tasks that are not overdue
	private static final int YELLOW_R = 253;
	private static final int YELLOW_G = 222;
	private static final int YELLOW_B = 96;

	// for scrolling of tabwindow
	private static int mouseXIndex = 0;
	private static final int BUFFER_FOR_TAGLIST =50;
	
	// variables for drag and drop capabilities of the composite
	private boolean returnMouseUp = true;
	private Point locationOfBeforeClicking;
	
	public TabListWindow(Composite parent, int style) {
		super(parent, SWT.NONE);

		setLayout(null);

		this.setBackgroundMode(SWT.INHERIT_DEFAULT);
		this.setBackgroundImage(SWTResourceManager.getImage(
				UserInterface.class, "/GUI/Tablist_background.png"));

		tagsListScrolledComposite = new ScrolledComposite(this, SWT.H_SCROLL
				| SWT.V_SCROLL);
		tagsListScrolledComposite.setBounds(44, 0, 450, 83);
		tagsListScrolledComposite.setExpandHorizontal(true);
		tagsListScrolledComposite.setExpandVertical(true);
		tagsListScrolledComposite.setBackgroundMode(SWT.INHERIT_DEFAULT);
		tagsListScrolledComposite.setBackground(SWTResourceManager.getColor(0,
				0, 0));
		tagsListScrolledComposite.setBackgroundImage(SWTResourceManager
				.getImage(UserInterface.class, "/GUI/Tablist_background.png"));
		tagListComposite = new Composite(tagsListScrolledComposite, SWT.NONE);
		tagListComposite.setBackgroundMode(SWT.INHERIT_DEFAULT);
		tagsListScrolledComposite.setContent(tagListComposite);

		loadTags(tagListComposite, tagsListScrolledComposite);

		creationOfLeftAndRightArrowButton();
		
		createDragCapabilities(tagsListScrolledComposite);
		createDragCapabilities(tagListComposite);
		
	}

	// #############################################################################################
	// Second level abstraction
	// #############################################################################################
	
	private static void loadTags(Composite composite,
			ScrolledComposite scrolledComposite) {

		Result allOpenTasks = Controller.getAllTasks();

		ArrayList<String> tagList = new ArrayList<String>();
		addTagsToArrayList(tagList, allOpenTasks);

		ArrayList<Integer> noOfEventPerTag = checkNoOfTasksWithEachTag(tagList);

		populateTagLabelWithTagsFound(composite, tagList, noOfEventPerTag);

		creationOfDefaultAllTag(composite);

		scrolledComposite.setContent(composite);
		scrolledComposite.setMinSize(composite.computeSize(SWT.DEFAULT,
				SWT.DEFAULT));

	}
	
	private void creationOfLeftAndRightArrowButton() {

		Composite leftArrowComposite = new Composite(this, SWT.NONE);
		leftArrowComposite.setBounds(0, -4, 584, 87);
		leftArrowComposite.setBackgroundMode(SWT.INHERIT_DEFAULT);
		leftArrowComposite.setBackgroundImage(SWTResourceManager.getImage(
				UserInterface.class, "/GUI/Tablist_background.png"));

		
		ToolBar toolBarForLeftArrow = new ToolBar(leftArrowComposite, SWT.FLAT);
		toolBarForLeftArrow.setBounds(10, 10, 70, 70);
		ToolItem btnLeftArrow = new ToolItem(toolBarForLeftArrow,
				SWT.NO_BACKGROUND);
		btnLeftArrow.setImage(SWTResourceManager.getImage(UserInterface.class,
				"/GUI/button_leftarrow.png"));
			btnLeftArrow.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				selectPreviousTab();
			}
		});
	
		
		// creation of right arrow button
		ToolBar toolBarForRightArrow = new ToolBar(leftArrowComposite,SWT.FLAT);
		toolBarForRightArrow.setBounds(514,10,70, 75);
		ToolItem btnRightArrow = new ToolItem(toolBarForRightArrow, SWT.NO_BACKGROUND);
		btnRightArrow.setImage(SWTResourceManager.getImage(UserInterface.class,
				"/GUI/button_rightarrow.png"));
		btnRightArrow.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				selectNextTab();
			}
		});
	
		createDragCapabilities(leftArrowComposite);
		
	}

	// #############################################################################################
	// Third level abstraction
	// #############################################################################################

	private static void addTagsToArrayList(ArrayList<String> tagList,
			Result allOpenTasks) {

		for (int i = 0; i < allOpenTasks.getData().size(); i++) {

			boolean isTagFoundInTagArrayList = false;
			String taskTag = allOpenTasks.getData().get(i).getTag();

			for (int y = 0; y < tagList.size(); y++) {
				if (tagList.get(y).equalsIgnoreCase(taskTag)) {
					isTagFoundInTagArrayList = true;
					break;
				}
			}

			if (!isTagFoundInTagArrayList
					&& !(taskTag.equalsIgnoreCase("empty"))) {
				tagList.add(taskTag);
			}

		}

		tagList.add("Others");
	}
	
	private static ArrayList<Integer> checkNoOfTasksWithEachTag(
			ArrayList<String> tagList) {

		ArrayList<Integer> noOfEventPerTag = new ArrayList<Integer>();

		for (int i = 0; i < tagList.size(); i++) {

			ArrayList<String> searchKeyWords = new ArrayList<String>();

			if (tagList.get(i).compareTo("Others") != 0)
				searchKeyWords.add(tagList.get(i));
			else
				searchKeyWords.add("empty");

			Result noOfOpenTaskWithThisTag = Controller.searchForOpenTask(
					searchKeyWords, "tag");
			Result noOfCompletedTaskWithThisTag = Controller
					.searchForCompletedTask(searchKeyWords, "tag");

			int totalSize = noOfOpenTaskWithThisTag.getData().size()
					+ noOfCompletedTaskWithThisTag.getData().size();

			noOfEventPerTag.add(totalSize);
		}
		return noOfEventPerTag;
	}
	
	private static void populateTagLabelWithTagsFound(Composite composite,
			ArrayList<String> tagList, ArrayList<Integer> noOfEventPerTag) {
		int accumulatedLengthOfWords = 0;

		assert tagList.size() == noOfEventPerTag.size() : "TAGLIST and NoOfEventPerTag should be the same here!";

		try {
			for (int i = 0; i < tagList.size(); i++) {

				if (noOfEventPerTag.get(i) > 0) {

					CLabel tabDetailLabel = new CLabel(composite, SWT.NONE);
					int lengthOfTag = (int)(tagList.get(i).length() * 10) + determineNumberOfDigits(noOfEventPerTag.get(i))*15 + 18;
					
					tabDetailLabel.setBounds(70 + accumulatedLengthOfWords, 10,
							lengthOfTag, 23);
					if (tagList.get(i).equalsIgnoreCase(
							UserInterface.getSelectedTab()))
						tabDetailLabel.setForeground(SWTResourceManager.getColor(ORANGE_R, ORANGE_G, ORANGE_B));
					else
						tabDetailLabel.setForeground(SWTResourceManager
								.getColor(YELLOW_R, YELLOW_G, YELLOW_B));
					tabDetailLabel.setFont(SWTResourceManager.getFont(
							"Segoe UI Semibold", 11, SWT.NORMAL));
					tabDetailLabel.setText(tagList.get(i) + " ("
							+ noOfEventPerTag.get(i) + ")");
					accumulatedLengthOfWords += (lengthOfTag + 20);
					addMouseListener(tabDetailLabel, i);
					tabDetail.add(tabDetailLabel);

					tabDetailLabel.redraw();

				}
			}
		} catch (NullPointerException e) {
			Controller.logSystemExceptionError("TabListWindow.populateTagLabelWithTagsFound() NullPointer Exception" +e.getMessage());
		}
	}
	
	private static void creationOfDefaultAllTag(Composite composite) {

		Result getAllTask = Controller.getAllTasks();
		lblAllTags = new CLabel(composite, SWT.NONE);
		lblAllTags.setBounds(10, 10, 60, 23);
		if (UserInterface.getSelectedTab().equalsIgnoreCase("all"))
			lblAllTags.setForeground(SWTResourceManager.getColor(ORANGE_R,
					ORANGE_G, ORANGE_B));
		else
			lblAllTags.setForeground(SWTResourceManager.getColor(YELLOW_R,
					YELLOW_G, YELLOW_B));
		lblAllTags.setFont(SWTResourceManager.getFont("Segoe UI Semibold", 11,
				SWT.NORMAL));
		lblAllTags.setText("All (" + getAllTask.getData().size() + ")");
		lblAllTags.addMouseListener(new MouseListener() {

			@Override
			public void mouseDoubleClick(MouseEvent arg0) {
			}

			@Override
			public void mouseDown(MouseEvent arg0) {
				UserInterface.setTaskListisSearchResult(false);
				UserInterface.setSelectedTab("All");
				UserInterface.refreshTabList(null, null);
			}

			@Override
			public void mouseUp(MouseEvent arg0) {
			}

		});
	}

	/**
	 * Select next tab is both an API for tablist window. It simply highlight
	 * the next tab inside the tabList
	 */
	public static void selectNextTab()
	{
		ArrayList<String> tabList = getAllTabsName();
			
		if(UserInterface.getSelectedTab().equals("All"))
		{
			if(tabList.size() >0)
			{
				
				String tabSelected = tabList.get(0);
				if(tabSelected.equalsIgnoreCase("Others "))
				{
					UserInterface.setSelectedTab("empty");
					refreshTabListWithAllTaskOfNoTags();
				}
				else
				{
					UserInterface.setSelectedTab(tabSelected);
					refreshTabListWithSelectedTag(tabDetail.get(0));
				}
				
			}
			mouseXIndex =0;
			tagsListScrolledComposite.setOrigin(new Point(mouseXIndex,0));
		}
		else
		{
			int currentIndex = getCurrentSelectedTabIndex(tabList);			
			
			if(currentIndex!=(tabList.size()-1))
			{
				
				String tabSelected = tabList.get(currentIndex+1);
				if(tabSelected.equalsIgnoreCase("Others "))
				{
					UserInterface.setSelectedTab("empty");
					refreshTabListWithAllTaskOfNoTags();
				}
				else
				{
					UserInterface.setSelectedTab(tabSelected);
					refreshTabListWithSelectedTag(tabDetail.get(currentIndex+1));
				}
				
				checkIfTabOutsideView(currentIndex);
				
				tagsListScrolledComposite.setOrigin(new Point(mouseXIndex,0));
			}
		}
	}



	/**
	 * Select previous tab is both an API for tablist window. It simply highlight
	 * the previous tab inside the tabList
	 */
	public static void selectPreviousTab()
	{
		ArrayList<String> tabList = getAllTabsName();

		if(!UserInterface.getSelectedTab().equals("All"))
		{
			int currentIndex = getCurrentSelectedTabIndex(tabList);
			
			if(currentIndex!=(tabList.size()) && currentIndex >0)
			{			
				UserInterface.setSelectedTab(tabList.get(currentIndex-1));
				refreshTabListWithSelectedTag(tabDetail.get(currentIndex-1));
				
				Point sizeOfTab = tabDetail.get(currentIndex-1).getSize();				
				Point locationOfTab = tabDetail.get(currentIndex-1).getLocation(); 
				
				mouseXIndex = mouseXIndex - sizeOfTab.x - BUFFER_FOR_TAGLIST;
				
				if(mouseXIndex < LENGTH_OF_TAGLIST && locationOfTab.x > LENGTH_OF_TAGLIST) 
				{
					mouseXIndex = locationOfTab.x;
				}
						
				if(mouseXIndex <0)
					mouseXIndex =0;
				
				tagsListScrolledComposite.setOrigin(new Point(mouseXIndex,0));
			}
			else if(currentIndex ==0)
			{
				UserInterface.setSelectedTab("All");
				UserInterface.refreshTabList(null, null);
			}
		}
	}
	
	// #############################################################################################
	// Forth level abstraction
	// #############################################################################################
	
	/**
	 * addMouseListener is the mouse listener for the tab detail labels
	 */
	private static void addMouseListener(final CLabel tabDetailLabel,
			final int i) {
		tabDetailLabel.addMouseListener(new MouseListener() {
			@Override
			public void mouseDown(MouseEvent arg0) {

				UserInterface.setTaskListisSearchResult(false);

				if (tabDetailLabel.getText().contains("Others")) {

					refreshTabListWithAllTaskOfNoTags();

				} else {
					refreshTabListWithSelectedTag(tabDetailLabel);
				}

			}

			public void mouseDoubleClick(MouseEvent arg0) {
			}

			public void mouseUp(MouseEvent arg0) {
			}
		});
	}
	
	private static void refreshTabListWithSelectedTag(
			final CLabel tabDetailLabel) {

		String[] splittedTag = tabDetailLabel.getText().split(
				"\\(\\d+\\)");
		ArrayList<String> searchKeyWord = new ArrayList<String>();
		searchKeyWord.add(splittedTag[0].trim());

		Result openTaskList = Controller.searchForOpenTask(
				searchKeyWord, "tag");
		Result completedTaskList = Controller.searchForCompletedTask(
				searchKeyWord, "tag");

		UserInterface.setSelectedTab(splittedTag[0].trim());
		UserInterface.refreshTabList(openTaskList, completedTaskList);

	}
	
	private static void refreshTabListWithAllTaskOfNoTags() {
		ArrayList<String> searchKeyWord = new ArrayList<String>();
		searchKeyWord.add("empty");

		Result openTaskList = Controller.searchForOpenTask(
				searchKeyWord, "tag");
		Result completedTaskList = Controller.searchForCompletedTask(
				searchKeyWord, "tag");

		UserInterface.setSelectedTab("Others");
		UserInterface.refreshTabList(openTaskList, completedTaskList);
	}
	
	private static int getCurrentSelectedTabIndex(ArrayList<String> tabList) {
		String currentSelectedTab = UserInterface.getSelectedTab();
		int currentIndex;
		
		for(currentIndex=0;currentIndex<tabList.size();currentIndex++)
		{
			if(currentSelectedTab.equalsIgnoreCase(tabList.get(currentIndex).trim()))
				break;
		}
		return currentIndex;
	}
	
	private static int determineNumberOfDigits(double x){
		int counter = 0;
		assert (x>=0): "Tag list size is less than 0!";
		if (x==0)
			return 1;
		while (x >= 1){
			x = x/10;
			counter++;
		}
		return counter;
	}
	
	private static void checkIfTabOutsideView(int currentIndex) {
		Point locationOfTab = tabDetail.get(currentIndex+1).getLocation(); 
		Point sizeOfTab = tabDetail.get(currentIndex+1).getSize();				
			
		if(locationOfTab.x > LENGTH_OF_TAGLIST)
		{
			mouseXIndex = mouseXIndex + sizeOfTab.x + BUFFER_FOR_TAGLIST;
		}
			
		if(locationOfTab.x < mouseXIndex)
		{
			mouseXIndex = locationOfTab.x;
		}
	}

	// #############################################################################################
	// Package GUI API
	// #############################################################################################

	/**
	 * refreshList is package GUI's API. It aims to refresh the available
	 * tabs inside the tab list
	 */
	public static void refreshList() {
		reLoadTags(tagListComposite, tagsListScrolledComposite);
	}

	private static void reLoadTags(Composite tagListComposite,
			ScrolledComposite tagListScrolledComposite) {

		Result allOpenTasks = Controller.getAllTasks();

		ArrayList<String> tagList = new ArrayList<String>();

		addTagsToArrayList(tagList, allOpenTasks);

		ArrayList<Integer> noOfEventPerTag = checkNoOfTasksWithEachTag(tagList);

		clearAndDisposeAllOldControls();

		populateTagLabelWithTagsFound(tagListComposite, tagList,
				noOfEventPerTag);

		redrawDefaultAllTag(tagListComposite);

		tagListScrolledComposite.setContent(tagListComposite);
		tagListScrolledComposite.setMinSize(tagListComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		tagListScrolledComposite.setOrigin(new Point(mouseXIndex, 0));
	}

	private static void clearAndDisposeAllOldControls() {

		for (int i = 0; i < tabDetail.size(); i++) {
			tabDetail.get(i).setText("");
			tabDetail.get(i).dispose();
		}
		tabDetail.clear();
	}

	private static void redrawDefaultAllTag(Composite composite) {

		lblAllTags.dispose();
		creationOfDefaultAllTag(composite);
		lblAllTags.redraw();
	}
	
	private static ArrayList<String> getAllTabsName()
	{
		ArrayList<String> tabList = new ArrayList<String>();
		
		for(int i =0;i<tabDetail.size();i++)
		{
			String[] splittedTag = tabDetail.get(i).getText().split("\\(\\d+\\)");
			tabList.add(splittedTag[0]);
		}
		
		return tabList;
		
	}
	
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

	
}
