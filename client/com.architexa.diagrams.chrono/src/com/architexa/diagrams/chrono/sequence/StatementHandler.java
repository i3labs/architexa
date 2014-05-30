package com.architexa.diagrams.chrono.sequence;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.openrdf.model.Resource;

import com.architexa.diagrams.RSECore;
import com.architexa.diagrams.chrono.controlflow.ControlFlowModel;
import com.architexa.diagrams.chrono.controlflow.IfBlockModel;
import com.architexa.diagrams.chrono.controlflow.LoopBlockModel;
import com.architexa.diagrams.chrono.models.DiagramModel;
import com.architexa.diagrams.chrono.models.InstanceModel;
import com.architexa.diagrams.chrono.models.MemberModel;
import com.architexa.diagrams.chrono.models.MethodBoxModel;
import com.architexa.diagrams.chrono.util.MethodUtil;
import com.architexa.diagrams.jdt.Invocation;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.intro.preferences.LibraryPreferences;
import com.architexa.org.eclipse.gef.commands.CompoundCommand;
import com.architexa.store.ReloRdfRepository;
import com.architexa.store.StoreUtil;


/**
 * 
 * @author Elizabeth L. Murnane
 *
 */
public class StatementHandler {

	private MethodBoxModel model;
	private DiagramModel diagram;
	private InstanceModel instance;
	private CompoundCommand addMultipleCallsCmd;

	private List<Invocation> callsList = new ArrayList<Invocation>();
	private List<ControlFlowModel> ifBlockModels = new ArrayList<ControlFlowModel>();
	private List<ControlFlowModel> loopBlockModels = new ArrayList<ControlFlowModel>();
	private ControlFlowModel previousControlFlowBlockModels = null;
	private List<MemberModel> conditionalBlockMemberModels = null;

	public StatementHandler(MethodBoxModel model, DiagramModel diagram, InstanceModel instance, CompoundCommand addMultipleCallsCmd) {
		this.model = model;
		this.diagram = diagram;
		this.instance = instance;
		this.addMultipleCallsCmd = addMultipleCallsCmd;
	}

	public void handleStatement(Statement stmt) {
		if(stmt==null) return;

		if(stmt instanceof AssertStatement) {
			handleExpression(((AssertStatement)stmt).getExpression());
			handleExpression(((AssertStatement)stmt).getMessage());
		} else if (stmt instanceof Block) {
			for(Object obj : ((Block)stmt).statements()) handleStatement((org.eclipse.jdt.core.dom.Statement)obj);
		} else if (stmt instanceof ConstructorInvocation) {
			handleConstructorInvocation((ConstructorInvocation)stmt);
		} else if (stmt instanceof DoStatement) {
			handleDoStatement((DoStatement)stmt);
		} else if (stmt instanceof EnhancedForStatement) {
			handleForStatement(((EnhancedForStatement)stmt).getExpression(), ((EnhancedForStatement)stmt).getBody());
		} else if (stmt instanceof ExpressionStatement) {
			handleExpression(((ExpressionStatement)stmt).getExpression());
		} else if (stmt instanceof ForStatement) {
			handleForStatement(((ForStatement)stmt).getExpression(), ((ForStatement)stmt).getBody());
		} else if (stmt instanceof IfStatement) {
			handleIfStatement((IfStatement)stmt, false);
		} else if (stmt instanceof LabeledStatement) {
			handleStatement(((LabeledStatement)stmt).getBody());
		} else if (stmt instanceof ReturnStatement) {
			handleExpression(((ReturnStatement)stmt).getExpression());
		} else if(stmt instanceof SuperConstructorInvocation) {
			handleSuperConstructorInvocation((SuperConstructorInvocation)stmt);
		} else if (stmt instanceof SwitchCase) {
			handleExpression(((SwitchCase)stmt).getExpression());
		} else if (stmt instanceof SwitchStatement) {
			handleExpression(((SwitchStatement)stmt).getExpression());
			for(Object obj : ((SwitchStatement)stmt).statements()) handleStatement((org.eclipse.jdt.core.dom.Statement)obj);
		} else if (stmt instanceof SynchronizedStatement) {
			handleExpression(((SynchronizedStatement)stmt).getExpression());
			handleStatement(((SynchronizedStatement)stmt).getBody());
		} else if (stmt instanceof TryStatement) {
			handleStatement(((TryStatement)stmt).getBody());
			handleStatement(((TryStatement)stmt).getFinally());
		} else if (stmt instanceof VariableDeclarationStatement) {
			for(Object obj : ((VariableDeclarationStatement)stmt).fragments()) 
				handleExpression(((VariableDeclarationFragment)obj).getInitializer());
		} else if (stmt instanceof WhileStatement) {
			handleWhileStatement((WhileStatement)stmt);
		}
	}

	private void handleExpression(Expression expression) {
		if(expression == null) return;

		if(expression instanceof Assignment) {
			handleExpression(((Assignment)expression).getLeftHandSide());
			handleExpression(((Assignment)expression).getRightHandSide());
		} else if(expression instanceof CastExpression) {
			handleExpression(((CastExpression)expression).getExpression());
		} else if (expression instanceof ClassInstanceCreation) {
			handleClassInstanceCreation((ClassInstanceCreation)expression);
		} else if(expression instanceof ConditionalExpression) {
			handleExpression(((ConditionalExpression)expression).getExpression());
			handleExpression(((ConditionalExpression)expression).getThenExpression());
			handleExpression(((ConditionalExpression)expression).getElseExpression());
		} else if (expression instanceof FieldAccess) {
			handleExpression(((FieldAccess)expression).getExpression());
		} else if (expression instanceof InfixExpression) {
			handleExpression(((InfixExpression)expression).getLeftOperand());
			handleExpression(((InfixExpression)expression).getRightOperand());
		} else if (expression instanceof InstanceofExpression) {
			handleExpression(((InstanceofExpression)expression).getLeftOperand());
		} else if (expression instanceof MethodInvocation) {
			handleMethodInvocation((MethodInvocation)expression);
		} else if (expression instanceof ParenthesizedExpression) {
			handleExpression(((ParenthesizedExpression)expression).getExpression());
		} else if (expression instanceof PostfixExpression) {
			handleExpression(((PostfixExpression)expression).getOperand());
		} else if (expression instanceof PrefixExpression) {
			handleExpression(((PrefixExpression)expression).getOperand());
		} else if (expression instanceof SuperMethodInvocation) {
			handleSuperMethodInvocation((SuperMethodInvocation)expression);
		} else if (expression instanceof VariableDeclarationExpression) {
			for(Object obj : ((VariableDeclarationExpression)expression).fragments())
				handleExpression(((VariableDeclarationFragment)obj).getInitializer());
		}
	}

	public List<Invocation> getCallsList() {
		return callsList;
	}

	public List<ControlFlowModel> getIfBlockModels() {
		return ifBlockModels;
	}

	public List<ControlFlowModel> getLoopBlockModels() {
		return loopBlockModels;
	}

	private void handleDoStatement(DoStatement doStmt) {
		handleStatement(doStmt.getBody()); // executes loop once before evaluating condition 

		Expression condition = doStmt.getExpression();
		handleExpression(condition); // evaluates condition before executing loop again

		LoopBlockModel doModel = new LoopBlockModel(diagram, condition.toString());
		loopBlockModels.add(doModel);

		if(conditionalBlockMemberModels == null) {
			conditionalBlockMemberModels = new ArrayList<MemberModel>();
		} else if(previousControlFlowBlockModels!=null) {
			previousControlFlowBlockModels.addInnerConditionalModel(doModel);
			doModel.setOuterConditionalModel(previousControlFlowBlockModels);
		}

		List<MemberModel> conditionalBlockCopy = new ArrayList<MemberModel>(conditionalBlockMemberModels);
		conditionalBlockMemberModels = new ArrayList<MemberModel>();

		ControlFlowModel previousBlockCopy = previousControlFlowBlockModels;
		previousControlFlowBlockModels = doModel;

		handleStatement(doStmt.getBody());
		handleExpression(condition); // must reevaluate expression every iteration

		doModel.setLoopStmts(conditionalBlockMemberModels);
		
		conditionalBlockCopy.addAll(conditionalBlockMemberModels);

		if(previousBlockCopy==null) conditionalBlockMemberModels = null;
		else conditionalBlockMemberModels = conditionalBlockCopy;

		previousControlFlowBlockModels = previousBlockCopy;
	}

	private void handleForStatement(Expression condition, Statement body) {
		handleExpression(condition); // evaluates condition before executing loop

		String conditionString = condition==null ? "" : condition.toString();
		LoopBlockModel forModel = new LoopBlockModel(diagram, condition.toString());
		loopBlockModels.add(forModel);

		if(conditionalBlockMemberModels == null) {
			conditionalBlockMemberModels = new ArrayList<MemberModel>();
		} else if(previousControlFlowBlockModels!=null) {
			previousControlFlowBlockModels.addInnerConditionalModel(forModel);
			forModel.setOuterConditionalModel(previousControlFlowBlockModels);
		}

		List<MemberModel> conditionalBlockCopy = new ArrayList<MemberModel>(conditionalBlockMemberModels);
		conditionalBlockMemberModels = new ArrayList<MemberModel>();

		ControlFlowModel previousBlockCopy = previousControlFlowBlockModels;
		previousControlFlowBlockModels = forModel;

		handleStatement(body);
		handleExpression(condition); // must reevaluate expression every iteration

		forModel.setLoopStmts(conditionalBlockMemberModels);
		conditionalBlockCopy.addAll(conditionalBlockMemberModels);

		if(previousBlockCopy==null) conditionalBlockMemberModels = null;
		else conditionalBlockMemberModels = conditionalBlockCopy;

		previousControlFlowBlockModels = previousBlockCopy;
	}

	private void handleIfStatement(IfStatement ifStmt, boolean isElseIf) {

		Expression condition = ifStmt.getExpression();
		ControlFlowModel ifModel = null;
		if(!isElseIf) {
			ifModel = new IfBlockModel(diagram, condition.toString());
			ifBlockModels.add(ifModel);
		}

		if(conditionalBlockMemberModels == null) {
			conditionalBlockMemberModels = new ArrayList<MemberModel>();
		} else if(previousControlFlowBlockModels!=null && !isElseIf) {
			previousControlFlowBlockModels.addInnerConditionalModel(ifModel);
			ifModel.setOuterConditionalModel(previousControlFlowBlockModels);
		}

		ControlFlowModel previousBlockCopy = previousControlFlowBlockModels;
		if(ifModel!=null) previousControlFlowBlockModels = ifModel;

		handleExpression(condition); // must reevaluate expression every iteration
		List<MemberModel> conditionalBlockComplete = new ArrayList<MemberModel>(conditionalBlockMemberModels);
		conditionalBlockMemberModels = new ArrayList<MemberModel>();
		handleStatement(ifStmt.getThenStatement());

		if(!isElseIf) {
			((IfBlockModel)ifModel).setThenStmts(conditionalBlockMemberModels);
		} else {
			((IfBlockModel)previousControlFlowBlockModels).addElseIfStmts(condition.toString(), conditionalBlockMemberModels);
		}
		conditionalBlockComplete.addAll(conditionalBlockMemberModels);

		conditionalBlockMemberModels = new ArrayList<MemberModel>();
		if(ifStmt.getElseStatement() instanceof IfStatement) {
			handleIfStatement((IfStatement)ifStmt.getElseStatement(), true);
		} else if(!isElseIf){
			handleStatement(ifStmt.getElseStatement());
			((IfBlockModel) ifModel).setElseStmts(conditionalBlockMemberModels);
		} else {
			handleStatement(ifStmt.getElseStatement());
			((IfBlockModel)previousControlFlowBlockModels).setElseStmts(conditionalBlockMemberModels);
		}

		conditionalBlockComplete.addAll(conditionalBlockMemberModels);

		if(previousBlockCopy==null) conditionalBlockMemberModels = null;
		else conditionalBlockMemberModels = conditionalBlockComplete;

		previousControlFlowBlockModels = previousBlockCopy;
	}

	private void handleWhileStatement(WhileStatement whileStmt) {
		Expression condition = whileStmt.getExpression();
		handleExpression(condition); // evaluates condition before executing loop

		LoopBlockModel whileModel = new LoopBlockModel(diagram, condition.toString());
		loopBlockModels.add(whileModel);

		if(conditionalBlockMemberModels == null) {
			conditionalBlockMemberModels = new ArrayList<MemberModel>();
		} else if(previousControlFlowBlockModels!=null) {
			previousControlFlowBlockModels.addInnerConditionalModel(whileModel);
			whileModel.setOuterConditionalModel(previousControlFlowBlockModels);
			
		}

		List<MemberModel> conditionalBlockCopy = new ArrayList<MemberModel>(conditionalBlockMemberModels);
		conditionalBlockMemberModels = new ArrayList<MemberModel>();

		ControlFlowModel previousBlockCopy = previousControlFlowBlockModels;
		previousControlFlowBlockModels = whileModel;

		handleStatement(whileStmt.getBody());
		handleExpression(condition); // must reevaluate expression every iteration

		whileModel.setLoopStmts(conditionalBlockMemberModels);
		conditionalBlockCopy.addAll(conditionalBlockMemberModels);

		if(previousBlockCopy==null) conditionalBlockMemberModels = null;
		else conditionalBlockMemberModels = conditionalBlockCopy;

		previousControlFlowBlockModels = previousBlockCopy;
	}

	// We don't need to add these recursive calls in all the invocation methods 
	// as we handle the multiple calls using the chained call feature
	private void handleMethodInvocation(MethodInvocation methodInvocation) {
//		handleExpression(methodInvocation.getExpression());
//		for(Object obj : methodInvocation.arguments()) handleExpression((Expression)obj);

		Invocation invocation = new Invocation(methodInvocation);
		handleInvocation(invocation);
	}

	private void handleSuperMethodInvocation(SuperMethodInvocation superMethodInvocation) {
//		for(Object obj : superMethodInvocation.arguments()) handleExpression((Expression)obj);

		Invocation invocation = new Invocation(superMethodInvocation);
		handleInvocation(invocation);
	}

	private void handleConstructorInvocation(ConstructorInvocation constructorInvocation) {
//		for(Object obj : constructorInvocation.arguments()) handleExpression((Expression)obj);
		Invocation invocation = new Invocation(constructorInvocation);
		handleInvocation(invocation);
	}

	private void handleSuperConstructorInvocation(SuperConstructorInvocation superConstructorInvocation) {
//		handleExpression(superConstructorInvocation.getExpression());
//		for(Object obj : superConstructorInvocation.arguments()) handleExpression((Expression)obj);
		Invocation invocation = new Invocation(superConstructorInvocation);
		handleInvocation(invocation);
	}

	private void handleClassInstanceCreation(ClassInstanceCreation classInstanceCreation) {
//		handleExpression(classInstanceCreation.getExpression());
//		for(Object obj : classInstanceCreation.arguments()) handleExpression((Expression)obj);
		Invocation invocation = new Invocation(classInstanceCreation);
		handleInvocation(invocation);
	}

	public void handleInvocation(Invocation invocation) {

		ReloRdfRepository repo = StoreUtil.getDefaultStoreRepository();

		Resource containerClass = MethodUtil.getContainerClass(invocation, model.getInstanceModel());
		if((containerClass==null || !RSECore.isInitialized(repo, containerClass)) 
				&& !LibraryPreferences.isChronoLibCodeInDiagram()) 
			return; // invocation is to lib code and user has pref set to not show lib code in diagram

		// Check for Spring Injection
		boolean classOfInstanceIsBean = false;
		Resource classOfInstanceCalledOn = MethodUtil.findInjectedBeanClass(invocation, model.getMethod());
		if (classOfInstanceCalledOn == null)
			classOfInstanceCalledOn = MethodUtil.getClassOfInstanceCalledOn(invocation, model.getInstanceModel());
		else
			classOfInstanceIsBean = true;
		
		if (classOfInstanceCalledOn == null) return;

		if(instance!=null) {
			String instanceName = MethodUtil.getInstanceCalledOn(invocation);
			boolean sameInstance = 
				(instance.getInstanceName()==null /*&& instanceName==null*/) || 
				(instance.getInstanceName()!=null && instanceName!=null && instance.getInstanceName().equals(instanceName));
			if(!sameInstance) return;

			boolean sameClass = classOfInstanceCalledOn.equals(instance.getResource());
			if(!sameClass) return;
		}

		callsList.add(invocation);

		InstanceModel instanceOfCalled = instance==null || instance.getInstanceName()==null ? null : instance;
		MethodBoxModel methodModel = null;
		// if method call is already in the diagram, don't re-add it
		for(ArtifactFragment child : model.getChildren()){
			if(child instanceof MethodBoxModel){
				if(((MethodBoxModel)child).getCharStart()!=-1 && ((MethodBoxModel)child).getCharStart()==invocation.getStartPosition()
						&& ((MethodBoxModel)child).getCharEnd()!=-1 && ((MethodBoxModel)child).getCharEnd()==invocation.getStartPosition()+invocation.getLength()) {
					methodModel = (MethodBoxModel)child;
					break;
				}	
			}
		}	
		if(methodModel==null) {
//			addMultipleCallsCmd.addBreakPlace(AnimateCallMadeCommand.class);
			methodModel = MethodUtil.createModelsForMethodRes(invocation, model, diagram, classOfInstanceCalledOn, instanceOfCalled, false, addMultipleCallsCmd, conditionalBlockMemberModels, classOfInstanceIsBean);
		} else {
			if (conditionalBlockMemberModels!=null)
				conditionalBlockMemberModels.add(methodModel);
		}
	}

	public static MemberModel createCallFromInvocation(Invocation invocation, InstanceModel instance, MethodBoxModel model, DiagramModel diagram, CompoundCommand command, List<MemberModel> stmtsToAdd){
		Resource containerClass = MethodUtil.getContainerClass(invocation, model.getInstanceModel());
		if(containerClass==null) return null;

		Resource classOfInstanceCalledOn = MethodUtil.getClassOfInstanceCalledOn(invocation, model.getInstanceModel());
		if(classOfInstanceCalledOn==null) return null;
		InstanceModel instanceOfCalled = instance==null || instance.getInstanceName()==null ? null : instance;
		MethodBoxModel methodModel = null;
		if(methodModel==null) {
			methodModel = MethodUtil.createModelsForMethodRes(invocation, model, diagram, classOfInstanceCalledOn, instanceOfCalled, false, command, stmtsToAdd, false);
		}
		return methodModel;
	}
}
