package com.architexa.collab.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.architexa.intro.preferences.LibraryPreferences;
import com.architexa.intro.preferences.PreferenceConstants;

public class LibraryCodePreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private static List<Button> layeredButtons = new ArrayList<Button>();
	private static List<Button> classButtons = new ArrayList<Button>();
	private static List<Button> sequenceButtons = new ArrayList<Button>();

	// Radio button choices
	private static String[] choices = {PreferenceConstants.hide, 
		PreferenceConstants.showInMenu, PreferenceConstants.showInDiagram};

	// More info descriptions
	private static String completelyHideDesc = "A diagram and its button menus will " +
	"not contain any library code references.";

	private static String showInMenuDesc = "Button menus will contain library code " +
	"references, but their selection and addition to the diagram will be disabled.";

	private static String showInDiagramDesc = "Library code references will " +
	"appear in button menus and be enabled for addition to the diagram.";

	private static String explorableDesc = "To explore library code items present " +
	"in a diagram (for example, to view the members of a library class or the calls that " +
	"a library method makes): " +
	"\nRight click the project containing the library jar file. Go to " +
	"Properties > Architexa Build Properties > Jar Filters. Select the library " +
	"jar of interest and press OK.";

	public LibraryCodePreferencePage() {
		super();
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}

	public void init(IWorkbench workbench) {}

	@Override
	protected Control createContents(final Composite parent) {

		// Preference for the display of library code in diagrams
		Composite container = new Composite(parent, SWT.NONE);
		GridLayout containerLayout = new GridLayout();
		containerLayout.marginTop = 10;
		containerLayout.verticalSpacing = 10;
		container.setLayout(containerLayout);
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Label libraryCodePrefLabel = new Label(container, SWT.LEFT);
		libraryCodePrefLabel.setText("Please choose the visibility level of library code in a diagram: ");
		GridData topLabelData = new GridData();
		topLabelData.horizontalSpan = 4;
		libraryCodePrefLabel.setLayoutData(topLabelData);

		Composite buttonArea = new Composite(container, SWT.NONE);
		GridLayout buttonAreaLayout = new GridLayout(4, false);
		buttonAreaLayout.marginTop = 10;
		buttonAreaLayout.horizontalSpacing = 15;
		buttonArea.setLayout(buttonAreaLayout);

		// Row of choices
		new Label(buttonArea, SWT.NONE); // make first column empty
		for(String choice : choices) {
			Label choiceLabel = new Label(buttonArea, SWT.CENTER);
			choiceLabel.setText(choice);
			choiceLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, true));
		}

		layeredButtons = createButtonGroup(buttonArea, "Layered Diagram", LibraryPreferences.getStrataLibraryCodeDisplay());
		makeSeparator(buttonArea);
		classButtons = createButtonGroup(buttonArea, "Class Diagram", LibraryPreferences.getReloLibraryCodeDisplay());
		makeSeparator(buttonArea);
		sequenceButtons = createButtonGroup(buttonArea, "Sequence Diagram", LibraryPreferences.getChronoLibraryCodeDisplay());

		// Expandable more info section
		createMoreInfoSection(container);

		container.setSize(container.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		return parent;
	}

	private List<Button> createButtonGroup(final Composite parent, 
			String diagramType, String selectedChoice) {
		Label diagramLabel = new Label(parent, SWT.LEFT);
		diagramLabel.setText(diagramType);

		final Composite buttonGroup = new Composite(parent, SWT.NONE);
		final GridLayout groupLayout = new GridLayout(3, true);
		buttonGroup.setLayout(groupLayout);
		GridData groupData = new GridData(SWT.CENTER, SWT.CENTER, true, true);
		groupData.horizontalSpan = 3;
		groupData.horizontalAlignment = SWT.FILL;
		buttonGroup.setLayoutData(groupData);

		Rectangle bounds = parent.getBounds();
		groupLayout.horizontalSpacing = bounds.width / 4;
		buttonGroup.addListener(SWT.Resize, new Listener() {
			public void handleEvent(Event event) {
				int width = buttonGroup.getBounds().width;
				groupLayout.marginLeft = 5;
				groupLayout.horizontalSpacing = (width-10*2) / 3;
				buttonGroup.layout();
			}
		});

		List<Button> buttons = new ArrayList<Button>();
		for(int i=0; i<choices.length; i++) {
			Button b = new Button(buttonGroup, SWT.RADIO);
			buttons.add(b);

			// select button if it corresponds to the stored selected preference
			if(choices[i].equals(selectedChoice)) b.setSelection(true);
		}
		return buttons;
	}

	private void makeSeparator(Composite parent) {
		Label separator = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL | SWT.LINE_SOLID);
		GridData separatorData = new GridData(SWT.CENTER, SWT.CENTER, true, true);
		separatorData.horizontalSpan = 4;
		separatorData.horizontalAlignment = SWT.FILL;
		separator.setLayoutData(separatorData);
	}

	private void createMoreInfoSection(final Composite parent) {

		// Create description of choices
		Composite descriptionArea = new Composite(parent, SWT.NONE);
		GridLayout areaLayout = new GridLayout();
		areaLayout.marginHeight = 0;
		areaLayout.marginWidth = 0;
		areaLayout.marginTop = 20;
		areaLayout.horizontalSpacing = 30;
		descriptionArea.setLayout(areaLayout);

		createDescrWithBoldHeading(descriptionArea, PreferenceConstants.hide, completelyHideDesc);
		createDescrWithBoldHeading(descriptionArea, PreferenceConstants.showInMenu, showInMenuDesc);
		createDescrWithBoldHeading(descriptionArea, PreferenceConstants.showInDiagram, showInDiagramDesc);
		createDescrWithBoldHeading(descriptionArea, null, explorableDesc);

		final GridData descriptionData = new GridData();
		descriptionData.horizontalAlignment = SWT.FILL;
		descriptionData.horizontalSpan = 2;
		descriptionArea.setLayoutData(descriptionData);
	}

	private void createDescrWithBoldHeading(final Composite parent, 
			String heading, String descr) {
		Composite container = new Composite(parent, SWT.NONE);
		GridLayout containerLayout = new GridLayout(2, false);
		containerLayout.marginHeight = 0;
		containerLayout.marginWidth = 0;
		container.setLayout(containerLayout);
		container.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));

		if(heading!=null && !"".equals(heading)) {
			// Make bold heading
			Label headingLabel = new Label(container, SWT.NONE);
			headingLabel.setText(heading+": ");
			headingLabel.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
			FontData[] fontData = headingLabel.getFont().getFontData();
			for(int i = 0; i < fontData.length; ++i)
				fontData[i].setStyle(SWT.BOLD);
			final Font newFont = new Font(parent.getDisplay(), fontData);
			headingLabel.setFont(newFont);
			headingLabel.addDisposeListener(new DisposeListener() {
				public void widgetDisposed(DisposeEvent e) {
					newFont.dispose(); // Since we created the font, we must dispose it
				}
			});
		}

		// Make description that wraps
		Label descrLabel = new Label(container, SWT.WRAP);
		descrLabel.setText(descr);

		final GridData descrData = new GridData(SWT.FILL, SWT.LEFT, true, false);
		descrLabel.setLayoutData(descrData);

		Rectangle rect = parent.getMonitor().getClientArea();
		descrData.widthHint = rect.width / 4;
		parent.addListener(SWT.Resize, new Listener() {
			public void handleEvent(Event event) {
				Rectangle bounds = parent.getBounds();
				descrData.widthHint = bounds.width * 9/10;
				parent.layout();
			}
		});
	}

	@Override
	public boolean performOk() {
		for(Button button : layeredButtons) {
			if(button.getSelection()) {
				LibraryPreferences.setStrataLibraryCodeDisplay(choices[layeredButtons.indexOf(button)]);
				break;
			}
		}
		for(Button button : classButtons) {
			if(button.getSelection()) {
				LibraryPreferences.setReloLibraryCodeDisplay(choices[classButtons.indexOf(button)]);
				break;
			}
		}
		for(Button button : sequenceButtons) {
			if(button.getSelection()) {
				LibraryPreferences.setChronoLibraryCodeDisplay(choices[sequenceButtons.indexOf(button)]);
				break;
			}
		}
		return super.performOk();
	}

	@Override
	protected void performDefaults() {

		String defaultStrataVal = LibraryPreferences.getStrataDefaultLibCodeDisplay();
		for(Button button : layeredButtons) {
			if(choices[layeredButtons.indexOf(button)].equals(defaultStrataVal))
				button.setSelection(true);
			else button.setSelection(false);
		}

		String defaultReloVal = LibraryPreferences.getReloDefaultLibCodeDisplay();
		for(Button button : classButtons) {
			if(choices[classButtons.indexOf(button)].equals(defaultReloVal))
				button.setSelection(true);
			else button.setSelection(false);
		}

		String defaultChronoVal = LibraryPreferences.getChronoDefaultLibCodeDisplay();
		for(Button button : sequenceButtons) {
			if(choices[sequenceButtons.indexOf(button)].equals(defaultChronoVal))
				button.setSelection(true);
			else button.setSelection(false);
		}
		super.performDefaults();
	}

}
