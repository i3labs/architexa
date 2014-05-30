/* 
 * Copyright (c) 2004-2005 Massachusetts Institute of Technology. This code was
 * developed as part of the Haystack (http://haystack.lcs.mit.edu/) research 
 * project at MIT. Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation files (the 
 * "Software"), to deal in the Software without restriction, including without 
 * limitation the rights to use, copy, modify, merge, publish, distribute, 
 * sublicense, and/or sell copies of the Software, and to permit persons to whom
 * the Software is furnished to do so, subject to the following conditions: 
 * 
 * The above copyright notice and this permission notice shall be included in 
 * all copies or substantial portions of the Software. 
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER  
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, 
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE. 
 */

/*
 * Created on Jul 21, 2004
 */
package com.architexa.diagrams.relo.agent;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;

import com.architexa.diagrams.relo.eclipse.gef.EditPartListener2;
import com.architexa.diagrams.relo.parts.AbstractReloEditPart;
import com.architexa.diagrams.relo.parts.ArtifactEditPart;
import com.architexa.org.eclipse.gef.EditPart;


/**
 * @author vineet
 *  
 * Currently Each Code Unit can have a different set of agents connected to it,
 * but by default initializeAgents sets up one of the registered agents to
 * connect
 */
public class AgentManager {
    

	public static class ViewAgent {
        protected boolean enabled = true;
		protected String name = null;
		public ViewAgent() { name = getClass().toString(); }
		public ViewAgent(String name) { this.name = name; }
        
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isEnabled() {
            return enabled;
        }
        
		public void selectedStateChanged(ArtifactEditPart part) {
		}

		public void childAdded(ArtifactEditPart child, int index) {
		}
		
		// TODO Delete Me - Examine for Removal along with the rest of Agent Manager 
        public void deletingChild(ArtifactEditPart child) {
        }
		
		@Override
        public String toString() {
		    return name;
		}
	}

	public static class SystemViewAgent extends ViewAgent {
		// don't let others to change my state
	    @Override
        public void setEnabled(boolean enabled) {}
	}
	
	
	private static class ViewAgentInternal extends EditPartListener2.Stub {
        private ViewAgent agent = null;
        public ViewAgentInternal(ViewAgent agent) {
            this.agent = agent;
        }
        //public void partActivated(EditPart editpart) {}
        //public void partDeactivated(EditPart editpart) {}
        @Override
        public void selectedStateChanged(EditPart part) {
            if (agent.isEnabled() && part instanceof ArtifactEditPart) {
                agent.selectedStateChanged((ArtifactEditPart) part);
            }
        }
        @Override
        public void childAdded(EditPart child, int index) {
            if (agent.isEnabled() && child instanceof ArtifactEditPart) {
                agent.childAdded((ArtifactEditPart) child, index);
            }
        }
     // TODO Delete Me
        @Override
        public void deletingChild(EditPart child) {
            if (agent.isEnabled() && child instanceof ArtifactEditPart) {
                agent.deletingChild((ArtifactEditPart) child);
            }
        }

	}
	
	
	// for debugability
	/*
	public static List runningAgents = new LinkedList();
	public static List getRunningAgents() {
		return runningAgents;
	}
	*/
	
	
	/**
	 * @param part
	 * @param agents
	 * @return agentsTok
	 */
	public static Object initializeAgents(EditPart part, List<?> agents) {
		// connect listener		
		Vector<ViewAgentInternal> agentsTok = new Vector<ViewAgentInternal>();
		Iterator<?> agentsIt = agents.iterator();
		while (agentsIt.hasNext()) {
			ViewAgent viewAgent = (ViewAgent) agentsIt.next();
			ViewAgentInternal agentInternal = new ViewAgentInternal(viewAgent);
			
			agentsTok.add(agentInternal);
			part.addEditPartListener(agentInternal);
			//runningAgents.add(viewAgent);
		}

		return agentsTok;
	}

	/**
	 * @param part
	 * @param agentManagerToken
	 */
	public static void cleanAgents(EditPart part, Object token) {
		Vector<?> agents = (Vector<?>) token;
		Iterator<?> eplIt = agents.iterator();
		while (eplIt.hasNext()) {
		    ViewAgentInternal agentInternal = (ViewAgentInternal) eplIt.next();
			part.removeEditPartListener(agentInternal);
			
			//runningAgents.remove(agentInternal.agent);
		}
	}

	/*
	 * support for agents context menu 
	 */

	static class AgentEnablingAction extends Action {
	    ViewAgent viewAgent;
        public AgentEnablingAction(ViewAgent viewAgent) {
            super(viewAgent.toString());
            this.viewAgent = viewAgent;
			setChecked(viewAgent.isEnabled());
        }
        @Override
        public void run() {
            viewAgent.setEnabled(!viewAgent.isEnabled());
        }
	}

    public static void buildAgentsContextMenu(
            MenuManager subMenu, 
            String groupName, 
            AbstractReloEditPart part,
            Object token) {
		Vector<?> agents = (Vector<?>) token;
		Iterator<?> eplIt = agents.iterator();
		while (eplIt.hasNext()) {
		    ViewAgentInternal agentInternal = (ViewAgentInternal) eplIt.next();
			subMenu.appendToGroup(groupName, new AgentEnablingAction(agentInternal.agent));
		}
    }

}