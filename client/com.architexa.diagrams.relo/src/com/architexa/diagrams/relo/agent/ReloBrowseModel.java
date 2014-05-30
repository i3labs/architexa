package com.architexa.diagrams.relo.agent;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.action.MenuManager;

import com.architexa.diagrams.model.BrowseModel;
import com.architexa.diagrams.relo.agent.AgentManager.ViewAgent;
import com.architexa.diagrams.relo.parts.AbstractReloEditPart;

/**
 * Functionality here should eventually move into the BrowseModel. In particular
 * these capabilities need to move from working on the Controller to working on
 * the Model.
 */
public abstract class ReloBrowseModel extends BrowseModel {

    public static List<ReloBrowseModel> models = new LinkedList<ReloBrowseModel>();
	public List<ViewAgent> agents = new LinkedList<ViewAgent>();

    public ReloBrowseModel() {
        models.add(this);
		// add system agents (these need to not be shown in the context menu)
		//agents.add(DebugModeAgent.singleton);	// debug agent (for i/f to change flag)
        agents.add(new ScriptManager());
    }

    /**
     * Called by agents (in their constructor) that need to be initialized by other sources.
     * 
     * Note: New browseModels will not connect to such an agent automatically
     *  
     * @param agent
     */
    public static final void connectToAllModels(ViewAgent agent) {
        for (ReloBrowseModel bm : ReloBrowseModel.models) {
            bm.agents.add(agent);
        }
    }
    
	public Object initializePart(AbstractReloEditPart part) {
		return AgentManager.initializeAgents(part, agents);
	}

	public void cleanPart(AbstractReloEditPart part, Object agentManagerToken) {
		AgentManager.cleanAgents(part, agentManagerToken);
	}

	public void buildModelContextMenu(
			MenuManager subMenu, 
			String groupName,
			AbstractReloEditPart part, 
			Object agentManagerToken) {
		AgentManager.buildAgentsContextMenu(subMenu, groupName, part, agentManagerToken);
	}
}
