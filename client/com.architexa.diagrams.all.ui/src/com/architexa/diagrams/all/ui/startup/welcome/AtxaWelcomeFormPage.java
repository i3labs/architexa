/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.architexa.diagrams.all.ui.startup.welcome;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.cheatsheets.OpenCheatSheetAction;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.ImageHyperlink;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.eclipse.ui.internal.intro.impl.util.Util;

import com.architexa.diagrams.ui.FontCache;
import com.architexa.diagrams.ui.ImageCache;
import com.architexa.intro.AtxaIntroPlugin;
/**
 * @author dejan
 * 
 * To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Generation - Code and Comments
 */
public class AtxaWelcomeFormPage extends FormPage {
	/**
	 * @param id
	 * @param title
	 */
	
	private static final String INTRO_PARA = "<form><p><b>Getting Started</b></p>" +
			"<p>To help you get up to speed quickly using Architexa we strongly recommend going through the Quickstart Guide " +
			"(found in the Architexa pull-down menu).</p>" +
			"<p>We want you to be happy so let us know if there is anything we can do to make your experience better.</p></form>"; //$NON-NLS-1$
	
	
	private String body = "Get up to speed quickly";
	private String header = "Quick Start";
	private String image = "icons/play.png";
	private String body2 = "- Understand Code Using Architexa\n- Document Code\n- Share Diagrams";
	private String header2 = "User Guide";
	private String image2 = "icons/Help.png";
	private String link2 = "http://www.architexa.com/user-guide/Table_of_Contents";
	private Image IMG_1;
	private Image IMG_2;


	private String[] texts = {"Layered architectural diagrams","Class and sequence diagrams","Document code architecture","Share and collaborate visually"};
	private String[] urls = {"http://www.architexa.com/learn-more/layered-diagrams","http://www.architexa.com/learn-more/understand","http://www.architexa.com/learn-more/document","http://www.architexa.com/learn-more/share"};
	private String[] imgStrings = {"icons/folder.png","icons/class2.png","icons/notepad.png","icons/globe.jpg"};


	private Color WHITE;
	private Image[] imgs = new Image[4];

	public AtxaWelcomeFormPage(FormEditor editor) {
		super(editor, "first", "FORM"); //$NON-NLS-1$ //$NON-NLS-2$
		
		//init cached images/colors
		WHITE = new Color(Display.getDefault(), new RGB(255,255,255));
		IMG_1 = ImageCache.calcImageFromDescriptor(AtxaIntroPlugin.getImageDescriptor(image));
		IMG_2 = ImageCache.calcImageFromDescriptor(AtxaIntroPlugin.getImageDescriptor(image2));
		for (int i=0; i<imgStrings.length; i++) {
			imgs[i] = ImageCache.calcImageFromDescriptor(AtxaIntroPlugin.getImageDescriptor(imgStrings[i]));
		}
	}
	@Override
	protected void createFormContent(IManagedForm managedForm) {
		ScrolledForm form = managedForm.getForm();
		FormToolkit toolkit = managedForm.getToolkit();
		
		
		// WELCOME
		form.setText( "Welcome to Architexa" ); //$NON-NLS-1$
		form.setFont(FontCache.font16);
//		form.setBackgroundImage(FormArticlePlugin.getDefault().getImage(FormArticlePlugin.IMG_FORM_BG));
		TableWrapLayout layout = new TableWrapLayout();
		layout.leftMargin = 40;
		layout.rightMargin = 40;
		layout.topMargin = 10;
		layout.numColumns = 2;
		layout.horizontalSpacing = 20;
		form.getBody().setLayout(layout);
		
		
		// INTRO TEXT
		FormText rtext = toolkit.createFormText(form.getBody(), false);
		rtext.setText(INTRO_PARA, true, false);
		rtext.setFont(FontCache.font10);
		TableWrapData td = new TableWrapData();
		td.colspan = 2;
		td.maxWidth = 700;
		rtext.setLayoutData(td);
		createMidLayout(toolkit, form);
		createBottomLayout(toolkit, form);
		
	}

	private void createMidLayout(FormToolkit toolkit, ScrolledForm form) {

		// LEFT COL
		Composite left = new Composite(form.getBody(), SWT.TOP);
		TableWrapLayout layout = new TableWrapLayout();
		layout.numColumns = 2;
		layout.topMargin = 20;
		layout.leftMargin = 20;
		left.setLayout(layout);
		left.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		left.setBackground(WHITE);
		
		createImageLinkItem(toolkit, left, IMG_1, header, body, new HyperlinkAdapter() {
					@Override
					public void linkActivated(HyperlinkEvent e) {
						OpenCheatSheetAction openAction = new OpenCheatSheetAction("com.architexa.diagrams.all.ui.tutorialCheatSheet");
						openAction.run();
					}
				});

		// RIGHT COL
		Composite right = new Composite(form.getBody(), SWT.RIGHT | SWT.TOP);
		layout = new TableWrapLayout();
		layout.numColumns = 2;
		layout.topMargin = 20;
		right.setLayout(layout);
		right.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		right.setBackground(WHITE);
		
		createImageLinkItem(toolkit, right, IMG_2, header2, body2, new HyperlinkAdapter() {
					@Override
					public void linkActivated(HyperlinkEvent e) {
						 Util.openBrowser(link2);
					}
				});

	}
	
	private void createBottomLayout(FormToolkit toolkit, ScrolledForm form) {

		// LEFT COL
		Composite linkMenu = new Composite(form.getBody(), SWT.TOP | SWT.CENTER);
		TableWrapLayout layout = new TableWrapLayout();
		layout.numColumns = 4;
		layout.topMargin = 80;
		layout.horizontalSpacing = 5;
		layout.leftMargin = 0;
		linkMenu.setLayout(layout);
		TableWrapData td = new TableWrapData(TableWrapData.FILL_GRAB );
		td.grabHorizontal = true;
		td.colspan = 2;
		td.align = TableWrapData.CENTER;
		linkMenu.setLayoutData(td);
		linkMenu.setBackground(WHITE);
		
		for (int i=0; i<urls.length; i++) {
			
			final String url = urls[i];
			HyperlinkAdapter link = new HyperlinkAdapter() {
				@Override
				public void linkActivated(HyperlinkEvent e) {
					 Util.openBrowser(url);
				}
			};
			
			ImageHyperlink imageHyperlink = toolkit.createImageHyperlink(linkMenu, SWT.WRAP | SWT.TOP);
			imageHyperlink.addHyperlinkListener(link);
			imageHyperlink.setToolTipText(texts[i]);
			imageHyperlink.setSize(100,100);
			imageHyperlink.setLayoutData(new TableWrapData(TableWrapData.CENTER));
			imageHyperlink.setImage(imgs[i]);
		}

		for (int i=0; i<urls.length; i++) {
			final String url = urls[i];
			HyperlinkAdapter link = new HyperlinkAdapter() {
				@Override
				public void linkActivated(HyperlinkEvent e) {
					 Util.openBrowser(url);
				}
			};
			
			Hyperlink textHyperlink = toolkit.createHyperlink(linkMenu, texts[i], SWT.FILL | SWT.CENTER | SWT.WRAP);
			textHyperlink.setToolTipText(texts[i]);
			textHyperlink.setUnderlined(false);
			textHyperlink.setFont(FontCache.font10);
			textHyperlink.setLayoutData(new TableWrapData(TableWrapData.CENTER));
			textHyperlink.addHyperlinkListener(link);
		}
		
		
		

	}
	
	
	private void createImageLinkItem(FormToolkit toolkit, Composite container, Image image, String header, String body, HyperlinkAdapter hyperlinkAdapter) {
		//IMAGE
		ImageHyperlink imageHyperlink = toolkit.createImageHyperlink(container, SWT.WRAP | SWT.TOP);
		imageHyperlink.addHyperlinkListener(hyperlinkAdapter);
		imageHyperlink.setToolTipText(body);
		imageHyperlink.setImage(image);
		imageHyperlink.addHyperlinkListener(hyperlinkAdapter);
		TableWrapData td = new TableWrapData();
		td.rowspan = 2;
		imageHyperlink.setLayoutData(td);
		
		//HEADER
		Hyperlink textHyperlink = toolkit.createHyperlink(container, header, SWT.FILL);
		textHyperlink.setToolTipText(body);
		textHyperlink.setUnderlined(false);
		textHyperlink.setFont(FontCache.fontArial10Bold);
		textHyperlink.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		textHyperlink.addHyperlinkListener(hyperlinkAdapter);
		
		//BODY
		Label label = toolkit.createLabel(container, body, SWT.NONE);
		label.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
	}
	
//	@Override
//	public void dispose() {
//		super.dispose();
//		WHITE.dispose();
//		IMG_1.dispose();
//		IMG_2.dispose();
//		for (int i=0; i<imgStrings.length; i++) {
//			imgs[i].dispose();
//		}
//	}
}