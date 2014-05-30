package com.architexa.diagrams.ui;

import java.util.List;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import com.architexa.diagrams.ui.SelectableAction;

/**
 * 
 * @author Elizabeth L. Murnane
 *
 */
public abstract class RSEMenuAction extends SelectableAction implements IMenuCreator {

	public RSEMenuAction() {
		super();
	}

	public RSEMenuAction(String id, String text, ImageDescriptor image) {
		setId(id);
		setText(text);
		setImageDescriptor(image);
	}

	public void dispose() {}

	@Override
	public void run(IAction action) {}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		action.setMenuCreator(this);
	}	

	public Menu getMenu(Control parent) {
		return null;
	}

	public Menu getMenu(Menu parent) {
		Menu menu = new Menu(parent);

		menu.addMenuListener(new MenuAdapter() {
			@Override
			public void menuShown(MenuEvent e) {

				Menu m = (Menu) e.widget;

				//dispose of old menu
				MenuItem[] items = m.getItems();
				for (int i = 0; i < items.length; i++) {
					items[i].dispose();
				}

				IMenuManager mgr = new MenuManager();     
				for(IAction action : getMenuActions()) {
					mgr.add(action);
				}
				for(IContributionItem item : mgr.getItems()) {
					item.fill(m, -1);
				}	          
			}
		});

		return menu;
	}

	protected abstract List<? extends IAction> getMenuActions();

}
