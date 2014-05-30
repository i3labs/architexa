package com.architexa.rse;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

public class BuildScheduleWizardPage extends WizardPage{
	public static final String PAGE_NAME = "Index Schedule";
	
	private Combo timeDropDown;
    private Combo AMPMDropDown;
    private Button daily;
    private Button onRequest;
	private Button instantly;
	private Button silently;
	private Label textLabel;
	private Table checkTable;
	TableItem item;
	
	protected BuildScheduleWizardPage() {
		super(PAGE_NAME, "Index Schedule", null);
	}

	public void createControl(Composite parent) {
		Composite topLevel = new Composite(parent, SWT.NONE);
		topLevel.setLayout(new GridLayout());
		Control buildSchedule = createBuildScheduleContents(topLevel);
		
		textLabel = new Label(topLevel, SWT.LEFT);
	    textLabel.setText("Architexa can now index your workspace. " +
				"\n(If you are using an empty workspace Architexa will automatically index " +
				"\nnew projects/files as they are added)\n");
	   
	    checkTable = new Table(topLevel, SWT.CHECK);
	    checkTable.setBackground(topLevel.getBackground());
	    item = new TableItem(checkTable, SWT.NONE);
	    item.setText("Index the selected projects now.");
	    item.setChecked(true);
	    
	    Label endLabel = new Label(topLevel, SWT.LEFT);
	    endLabel.setText("Please be paitent as Architexa Indexes. When the indexing completes " +
	    		"\nyou can explore the power of Architexa by utilizing the cheat sheets provided.");
	    setControl(buildSchedule);
	    
		setPageComplete(true);
		
	}

	
	public boolean isBuildNowChecked() {
		return item.getChecked();
	}
	
	public void setBuildSchedule() {
		if (daily.getSelection())
			BuildSettings.setBuildSchedule(BuildSettings.BUILD_DAILY);
		else if (onRequest.getSelection())
			BuildSettings.setBuildSchedule(BuildSettings.BUILD_REQUEST);
		else if (silently.getSelection())
			BuildSettings.setBuildSchedule(BuildSettings.BUILD_SILENTLY);
		else
			BuildSettings.setBuildSchedule(BuildSettings.BUILD_INSTANTLY);
		BuildSettings.setBuildTime(getBuildTimeFromDialog());
	}
	
	private String getBuildTimeFromDialog() {
		int retVal = 0;
		if (timeDropDown.isEnabled() && AMPMDropDown.isEnabled()) {
			retVal = Integer.parseInt(timeDropDown.getText().substring(0,timeDropDown.getText().indexOf(":")));
			if (AMPMDropDown.getSelectionIndex() == 1) 
				retVal +=12;
		}
		return Integer.toString(retVal);
        
	}
	
	private Control createBuildScheduleContents(Composite topLevel) {
		Composite container = new Composite(topLevel,SWT.NONE);
		GridLayout layout = new GridLayout(1, false);
		layout.marginHeight=0;
		container.setLayout(layout);
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		data.heightHint=350;
		container.setLayoutData(data);
		
		Label desc = new Label(container, SWT.NONE);
		desc.setText("Select how often Architexa updates your indexes.");
		desc.setLayoutData(new GridData(SWT.LEFT, SWT.NONE, false, false, 2, 1));
		
		Group g = new Group(container, SWT.SHADOW_ETCHED_IN);
//		g.setText("Schedule");
		GridLayout gl = new GridLayout();
		gl.marginLeft =10;
		g.setLayout(gl);
		addScheduleSelectionArea(g);
		return container;
	}
	
	private void addScheduleSelectionArea(Composite container) {
		Composite fullTabArea = new Composite(container, SWT.NONE);
		GridLayout containerLayout = new GridLayout();
		fullTabArea.setLayout(containerLayout);
		
		String schedule = BuildSettings.getBuildSchedule();
		if (schedule==null || schedule.equals(""))
			BuildSettings.setBuildSchedule(BuildSettings.DEFAULT_BUILD_SCHEDULE);
		schedule = BuildSettings.getBuildSchedule();
		
		instantly = new Button(fullTabArea, SWT.RADIO | SWT.LEFT);
		instantly.setText("Immediately as a change is made");
		instantly.setSelection(schedule.equals(BuildSettings.BUILD_INSTANTLY));
		
		silently = new Button(fullTabArea, SWT.RADIO | SWT.LEFT);
		silently.setText("Silently using minimal resources");
		silently.setSelection(schedule.equals(BuildSettings.BUILD_SILENTLY));
		
		daily = new Button(fullTabArea, SWT.RADIO | SWT.LEFT);
		daily.setText("Once a Day");
		
		daily.setSelection(schedule.equals(BuildSettings.BUILD_DAILY));
		
		Composite dailyArea = new Composite(fullTabArea, SWT.NONE);
		GridLayout dailyLayout = new GridLayout(2, false);
		dailyArea.setLayout(dailyLayout);
		
        timeDropDown = new Combo(dailyArea, SWT.DROP_DOWN|SWT.READ_ONLY| SWT.LEFT);
        timeDropDown.setLayoutData(new GridData());
        timeDropDown.add("1:00");
        timeDropDown.add("2:00");
        timeDropDown.add("3:00");
        timeDropDown.add("4:00");
        timeDropDown.add("5:00");
        timeDropDown.add("6:00");
        timeDropDown.add("7:00");
        timeDropDown.add("8:00");
        timeDropDown.add("9:00");
        timeDropDown.add("10:00");
        timeDropDown.add("11:00");
        timeDropDown.add("12:00");
		
        AMPMDropDown = new Combo(dailyArea, SWT.DROP_DOWN|SWT.READ_ONLY| SWT.LEFT);
        AMPMDropDown.setLayoutData(new GridData());
        AMPMDropDown.add("AM");
        AMPMDropDown.add("PM");
        AMPMDropDown.select(0);
        timeDropDown.select(1);
        if (!BuildSettings.getBuildDailyTime().equals("")) {
	        int savedTime = Integer.parseInt(BuildSettings.getBuildDailyTime());
	        if (savedTime <13) {
	        	timeDropDown.select(savedTime-1);
	        	AMPMDropDown.select(0);
	        }
	        else {
	         	timeDropDown.select(savedTime-13);
	         	AMPMDropDown.select(1);
	        }
        }
		
		onRequest = new Button(fullTabArea, SWT.RADIO);
		onRequest.setText("Only on my request");
		onRequest.setSelection(schedule.equals(BuildSettings.BUILD_REQUEST));
		
		timeDropDown.setEnabled(false);
		AMPMDropDown.setEnabled(false);
		if (daily.getSelection()) {
			timeDropDown.setEnabled(true);
			AMPMDropDown.setEnabled(true);
		}
		fullTabArea.pack();
		fullTabArea.update();
		
		daily.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				if (daily.getSelection()) {
					timeDropDown.setEnabled(true);
					AMPMDropDown.setEnabled(true);
				} else {
					timeDropDown.setEnabled(false);
					AMPMDropDown.setEnabled(false);
				}
			}
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
	}
}
