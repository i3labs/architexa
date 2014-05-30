package com.architexa.diagrams.eclipse.gef;

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.util.Policy;

import com.architexa.org.eclipse.gef.ContextMenuProvider;
import com.architexa.org.eclipse.gef.EditPartViewer;

public abstract class ContextMenuProvider2 extends ContextMenuProvider {

	public ContextMenuProvider2(EditPartViewer viewer) {
		super(viewer);
	}
	
	// below copied from CoolBarManager.allowItem - makes sure that we don't have duplicates in the menu

    /* (non-Javadoc)
     * @see org.eclipse.jface.action.ContributionManager#checkDuplication(org.eclipse.jface.action.IContributionItem)
     */
    @Override
	protected boolean allowItem(IContributionItem itemToAdd) {
        /* We will allow as many null entries as they like, though there should
         * be none.
         */
        if (itemToAdd == null) {
            return true;
        }

        /* Null identifiers can be expected in generic contribution items.
         */
        String firstId = itemToAdd.getId();
        if (firstId == null) {
            return true;
        }

        // Cycle through the current list looking for duplicates.
        IContributionItem[] currentItems = getItems();
        for (int i = 0; i < currentItems.length; i++) {
            IContributionItem currentItem = currentItems[i];

            // We ignore null entries.
            if (currentItem == null) {
                continue;
            }

            String secondId = currentItem.getId();
            if (firstId.equals(secondId)) {
                if (Policy.TRACE_TOOLBAR) { 
                    System.out.println("Trying to add a duplicate item."); //$NON-NLS-1$
                    new Exception().printStackTrace(System.out);
                    System.out.println("DONE --------------------------"); //$NON-NLS-1$
                }
                return false;
            }
        }

        return true;
    }

}
