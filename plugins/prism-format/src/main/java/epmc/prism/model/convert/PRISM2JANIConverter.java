/****************************************************************************

    ePMC - an extensible probabilistic model checker
    Copyright (C) 2017

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

*****************************************************************************/

package epmc.prism.model.convert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import java.util.Set;

import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.expression.ExpressionToType;
import epmc.expression.standard.CmpType;
import epmc.expression.standard.DirType;
import epmc.expression.standard.ExpressionFilter;
import epmc.expression.standard.ExpressionIdentifierStandard;
import epmc.expression.standard.ExpressionLiteral;
import epmc.expression.standard.ExpressionOperator;
import epmc.expression.standard.ExpressionQuantifier;
import epmc.expression.standard.ExpressionReward;
import epmc.expression.standard.FilterType;
import epmc.expression.standard.UtilExpressionStandard;
import epmc.graph.SemanticsCTMC;
import epmc.graph.SemanticsDTMC;
import epmc.graph.SemanticsNonDet;
import epmc.jani.extensions.derivedoperators.ModelExtensionDerivedOperators;
import epmc.jani.model.Action;
import epmc.jani.model.Actions;
import epmc.jani.model.AssignmentSimple;
import epmc.jani.model.Assignments;
import epmc.jani.model.Automata;
import epmc.jani.model.Automaton;
import epmc.jani.model.Constant;
import epmc.jani.model.Constants;
import epmc.jani.model.Destination;
import epmc.jani.model.Destinations;
import epmc.jani.model.Edge;
import epmc.jani.model.Edges;
import epmc.jani.model.Guard;
import epmc.jani.model.InitialStates;
import epmc.jani.model.Location;
import epmc.jani.model.ModelExtension;
import epmc.jani.model.ModelJANI;
import epmc.jani.model.Probability;
import epmc.jani.model.Rate;
import epmc.jani.model.TimeProgress;
import epmc.jani.model.Variable;
import epmc.jani.model.Variables;
import epmc.jani.model.property.ExpressionDeadlock;
import epmc.jani.model.property.ExpressionInitial;
import epmc.jani.model.property.JANIProperties;
import epmc.jani.model.type.JANIType;
import epmc.jani.model.type.JANITypeBool;
import epmc.jani.model.type.JANITypeInt;
import epmc.jani.model.type.JANITypeReal;
import epmc.modelchecker.Properties;
import epmc.modelchecker.RawProperty;
import epmc.options.Options;
import epmc.prism.model.Alternative;
import epmc.prism.model.Command;
import epmc.prism.model.Formulas;
import epmc.prism.model.ModelPRISM;
import epmc.prism.model.Module;
import epmc.prism.model.ModuleCommands;
import epmc.prism.model.PropertiesImpl;
import epmc.prism.model.RewardStructure;
import epmc.prism.value.OperatorPRISMPow;
import epmc.util.Util;
import epmc.value.OperatorAddInverse;
import epmc.value.OperatorCeil;
import epmc.value.OperatorPow;
import epmc.value.OperatorSubtract;
import epmc.value.Type;
import epmc.value.TypeBoolean;
import epmc.value.TypeInteger;
import epmc.value.TypeReal;
import epmc.value.ValueBoolean;

// TODO optionally use "F" and "G" operators directly using extension
// TODO maybe make usage of derived operators optional

/**
 * Class to convert PRISM model to JANI model representation.
 *
 * @author Ernst Moritz Hahn
 */
public final class PRISM2JANIConverter {
	private final static class ExpressionToTypeJANIConverter implements ExpressionToType {
		private final Map<Expression,Type> typeMap;
		
		private ExpressionToTypeJANIConverter(Variables variables) {
			assert variables != null;
			Map<Expression,Type> typeMap = new HashMap<>();
			for (Variable variable : variables) {
				try {
					typeMap.put(variable.getIdentifier(), variable.getType().toType());
				} catch (EPMCException e) {
					// in case type cannot be derived, we just leave it out
				}
			}
			this.typeMap = Collections.unmodifiableMap(typeMap);
		}
		
		@Override
		public Type getType(Expression expression) throws EPMCException {
			return typeMap.get(expression);
		}
		
	}
	
	/** name to use for newly generated location variable. */
	final static String LOCATION_NAME = "location";
	/** Empty string. */
	final static String EMPTY = "";
	/** Name of silent identifier. */
	private final static String TAU = "τ";
	/** JANI version to which the converter converts. */
	private final static int JANI_VERSION = 1;
	/** Initial states identifier name. */
	private final static String INIT = "\"init\"";
	/** Deadlock states identifier name. */
	private final static String DEADLOCK = "\"deadlock\"";
	
	// TODO :-|
	static String REWARD_PREFIX = "reward_";
	
	static String prefixRewardName(String name) {
		return REWARD_PREFIX + name;
	}
	
	public static void setRewardPrefix(String prefix) {
		REWARD_PREFIX = prefix;
	}
	
	/** PRISM model to convert. */
	private final ModelPRISM modelPRISM;
	/** JANI model created. */
	private final ModelJANI modelJANI;
	/** Tau action replacing silent action. */
	private Action tauAction;
	
	private boolean forExporting;
	private ExpressionToTypeJANIConverter expressionToType;
	
	/**
	 * Construct new converter for given PRISM model.
	 * The model must not be {@code null}.
	 * 
	 * @param modelPRISM PRISM model to convert
	 */
	public PRISM2JANIConverter(ModelPRISM modelPRISM, boolean forExporting) {
		assert modelPRISM != null;
		this.modelPRISM = modelPRISM;
    	modelJANI = new ModelJANI();
    	modelJANI.setName(modelPRISM.getIdentifier());
		this.tauAction = new Action();
		tauAction.setModel(modelJANI);
		tauAction.setName(TAU);
		this.forExporting = forExporting;
	}
	
	/**
	 * Convert PRISM model to JANI format.
	 * 
	 * @param forExporting when set to true, the method transforms the JANI structures so to agree with the JANI-specification
	 * @throws EPMCException thrown in case of problems
	 */
	public ModelJANI convert() throws EPMCException {
    	modelJANI.setSemantics(modelPRISM.getSemantics().toString());
    	modelJANI.setVersion(JANI_VERSION);
    	
    	convertExtensions();

		Variables globalVariables = buildGlobalVariables();
		this.expressionToType = new ExpressionToTypeJANIConverter(globalVariables);
    	modelJANI.setGlobalVariables(globalVariables);
    	modelJANI.setModelConstants(buildConstants());
    	Actions actions = computeActions();
    	modelJANI.setActions(actions);
    	modelJANI.setRestrictInitial(computeInitialStates());
    	modelJANI.setAutomata(computeAutomata(actions, globalVariables));

    	convertSystem();
		convertRewards();
		convertPlayers();
    	
    	modelJANI.setProperties(buildProperties());
    	return modelJANI;
	}

	private void convertExtensions() throws EPMCException {
    	List<ModelExtension> modelExtensions = new ArrayList<>();
    	ModelExtensionDerivedOperators extension = Util.getInstance(ModelExtensionDerivedOperators.class);
    	extension.setModel(modelJANI);
    	modelExtensions.add(extension);
    	modelJANI.setModelExtensions(modelExtensions);
    	modelJANI.parseBeforeModelNodeExtensions(modelJANI, null);
	}

	private void convertPlayers() {
    	PlayerConverter playerConverter = new PlayerConverter();
    	playerConverter.setJANIModel(modelJANI);
    	playerConverter.setPRISMModel(modelPRISM);
    	playerConverter.attachPlayers();
	}

	private void convertSystem() throws EPMCException {
    	SystemType systemType = Options.get().get(OptionsPRISMConverter.PRISM_CONVERTER_SYSTEM_METHOD);
    	SystemConverter systemConverter;
    	switch (systemType) {
		case SYNCHRONISATION_VECTORS:
			systemConverter = new SystemConverterSynchronisationVectors();
			break;
		case RECURSIVE:
			systemConverter = new SystemConverterRecursive();
			break;
		default:
			assert false;
			systemConverter = null;
			break;
    	}
		systemConverter.setJANIModel(modelJANI);
		systemConverter.setPRISMModel(modelPRISM);
		systemConverter.convert();
	}

	private void convertRewards() throws EPMCException {
    	RewardsConverter rewardsConverter = new RewardsConverter();
    	rewardsConverter.setJANIModel(modelJANI);
    	rewardsConverter.setPRISMModel(modelPRISM);
    	rewardsConverter.setTauAction(tauAction);
    	rewardsConverter.setForExporting(forExporting);
    	rewardsConverter.attachRewards();
	}

	private Constants buildConstants() {
		PropertiesImpl properties = modelPRISM.getPropertyList();
		Set<String> prismConstants = properties.getConstants().keySet();
		Constants janiConstants = new Constants();
		janiConstants.setModel(modelJANI);
		Formulas formulas = modelPRISM.getFormulas();
		for (String name : prismConstants) {
			ExpressionIdentifierStandard identifier = new ExpressionIdentifierStandard.Builder()
					.setName(name)
					.build();
			JANIType type = formulas.getConstantType(name);
			if (type == null) {
				Type typeNative = properties.getConstantType(name);
				if (TypeInteger.isInteger(typeNative)) {
					type = new JANITypeInt();
				} else if (TypeBoolean.isBoolean(typeNative)) {
					type = new JANITypeBool();
				} else if (TypeReal.isReal(typeNative)) {
					type = new JANITypeReal();
				} else {
					assert false;
				}
			}
			type.setModel(modelJANI);
			Constant constant = new Constant();
			constant.setModel(modelJANI);
			constant.setName(name);
			constant.setIdentifier(identifier);
			constant.setType(type);
			Expression value = properties.getConstantValue(name);
			constant.setValue(value);
			janiConstants.put(name, constant);
		}
		return janiConstants;
	}

	private JANIProperties buildProperties() throws EPMCException {
		JANIProperties properties = new JANIProperties();
		properties.setModel(modelJANI);
		//TODO: get the right valid identifiers
		properties.setValidIdentifiers(Collections.emptyMap()); 
		Properties oldProperties = modelPRISM.getPropertyList();
		for (RawProperty raw : oldProperties.getRawProperties()) {
			Expression property = oldProperties.getParsedProperty(raw);; 
			if (forExporting) { 
				property = useOnlyPrefixedNamedRewards(modelPRISM, property);
			}
			property = replaceSpecialIdentifiers(property);
			if (forExporting) {
				property = useQuantitativePropertiesOnly(property);
				List<Expression> wrapped = wrapWithFilter(property);
				if (wrapped.size() == 1) {
					properties.addProperty(raw.getName(), property, raw.getDefinition());
				} else if (wrapped.size() == 2) {
					properties.addProperty(raw.getName(), wrapped.get(0), raw.getDefinition());
					properties.addProperty(raw.getName(), wrapped.get(1), raw.getDefinition());
				} else {
					assert false;
				}
			} else {
				properties.addProperty(raw.getName(), property, null);
			}
		}
		return properties;
	}
	
    private Expression replaceSpecialIdentifiers(Expression property) {
    	assert property != null;
    	Expression initLabel = new ExpressionIdentifierStandard.Builder()
    			.setName(INIT)
    			.build();
    	Expression deadlockLabel = new ExpressionIdentifierStandard.Builder()
    			.setName(DEADLOCK)
    			.build();
    	Expression initJani = new ExpressionInitial(null);
    	Expression deadlockJani = new ExpressionDeadlock(null);
    	Map<Expression,Expression> replacement = new HashMap<>();
    	replacement.put(initLabel, initJani);
    	replacement.put(deadlockLabel, deadlockJani);
    	return UtilExpressionStandard.replace(property, replacement);
	}

	/**
     * Transform the expression so to be wrapped by a filter construction.
     * 
     * @param expression the expression to wrap
     * @return a list of filter-wrapped expressions
     */
	private List<Expression> wrapWithFilter(Expression expression) {
		assert expression != null;
		List<Expression> list = new LinkedList<>();
		if (ExpressionFilter.isFilter(expression)) {
			list.add(expression);
		} else {
			if (ExpressionQuantifier.isQuantifier(expression)
					&& ((ExpressionQuantifier) expression).getCompareType().isIs()) {
				list.add(new ExpressionFilter.Builder()
						.setFilterType(FilterType.MIN)
						.setProp(expression)
						.setStates(ExpressionInitial.getExpressionInitial())
						.build());
                list.add(new ExpressionFilter.Builder()
                        .setFilterType(FilterType.MAX)
                        .setProp(expression)
                        .setStates(ExpressionInitial.getExpressionInitial())
                        .build());
			} else {
				// it is not a quantitative quantifier, so it should be a boolean operator
                list.add(new ExpressionFilter.Builder()
                        .setFilterType(FilterType.FORALL)
                        .setProp(expression)
                        .setStates(ExpressionInitial.getExpressionInitial())
                        .build());
			}
		}
		return list;
	}
	
    /**
     * Transform the expression so to use only prefixed named reward structures.
     * 
     * Properties can refer to reward structures by name, number, or nothing (defaulting to the first reward structure).
     * By calling this method, the obtained {@link Expression} refers to reward structures by name only, with the name being prefixed by {@link #REWARD_PREFIX}.
     * 
     * @param expression the expression to convert
     * @return an equivalent expression using only named reward structures
     */
	private Expression useOnlyPrefixedNamedRewards(ModelPRISM modelPRISM, Expression expression) throws EPMCException {
		assert expression != null;
		List<Expression> oldChildren = expression.getChildren();
		List<Expression> newChildren = new ArrayList<>(oldChildren.size());
		if (expression instanceof ExpressionReward) {
	        RewardStructure reward = modelPRISM.getReward(((ExpressionReward) expression).getReward());
	        String name;
	        if (reward == null) {
	        	name = "";
	        } else {
	        	name = reward.getName();
	        }
	        newChildren.add(new ExpressionIdentifierStandard.Builder()
	        		.setName(prefixRewardName(name))
	        		.build());
	        newChildren.add(useOnlyPrefixedNamedRewards(modelPRISM, oldChildren.get(1)));
	        newChildren.add(oldChildren.get(2));
	        newChildren.add(oldChildren.get(3));
	        return expression.replaceChildren(newChildren);
		} else {
			for (Expression child:oldChildren) {
				newChildren.add(useOnlyPrefixedNamedRewards(modelPRISM, child));
			}
			return expression.replaceChildren(newChildren);
		}
	}
	
    /**
     * Transform the expression so to use only named reward structures.
     * 
     * Properties can refer to reward structures by name, number, or nothing (defaulting to the first reward structure).
     * By calling this method, the obtained {@link Expression} refers to reward structures by name only.
     * 
     * @param expression the expression to convert
     * @return an equivalent expression using only named reward structures
     */
	public static Expression useOnlyNamedRewards(ModelPRISM modelPRISM, Expression expression) throws EPMCException {
		assert expression != null;
		List<Expression> oldChildren = expression.getChildren();
		List<Expression> newChildren = new ArrayList<>(oldChildren.size());
		if (expression instanceof ExpressionReward) {
	        RewardStructure reward = modelPRISM.getReward(((ExpressionReward) expression).getReward());
	        String name;
	        if (reward == null) {
	        	name = "";
	        } else {
	        	name = reward.getName();
	        }
	        newChildren.add(new ExpressionIdentifierStandard.Builder()
	        		.setName(name)
	        		.build());
	        newChildren.add(useOnlyNamedRewards(modelPRISM, oldChildren.get(1)));
	        newChildren.add(oldChildren.get(2));
	        newChildren.add(oldChildren.get(3));
	        return expression.replaceChildren(newChildren);
		} else {
			for (Expression child:oldChildren) {
				newChildren.add(useOnlyNamedRewards(modelPRISM, child));
			}
			return expression.replaceChildren(newChildren);
		}
	}
	
    /**
     * Transform the expression so to use only quantitative properties, i.e., properties of the form {P,S}[min/max]=?.
     * 
     * Probabilistic and steady-state properties can be quantitative (e.g., P=?) or bounded (e.g., P&gt;=0.7).
     * By calling this method, the obtained {@link Expression} involves only quantitative properties, e.g., P&gt;=p[phi] is transformed to p &lt;=P=?[phi].
     * 
     * @param expression the expression to convert
     * @return an equivalent expression using only quantitative properties
     */
	private Expression useQuantitativePropertiesOnly(Expression expression) {
		assert expression != null;
		ExpressionQuantifier expressionQuantifier = ExpressionQuantifier.asQuantifier(expression);
		if ((expressionQuantifier != null)
				&& !expressionQuantifier.getCompareType().isIs()) {
    		CmpType cmpType = expressionQuantifier.getCompareType();
	        DirType dirType = expressionQuantifier.getDirType();
	        Expression quantified = useQuantitativePropertiesOnly(expressionQuantifier.getQuantified());
	        String operator = cmpType.asExOpType();
        	if (dirType.equals(DirType.NONE)
        			&& SemanticsNonDet.isNonDet(modelPRISM.getSemantics())) {
        		if (cmpType.equals(CmpType.GE) || cmpType.equals(CmpType.GT)) {
        			dirType = DirType.MIN;
        		} else if (cmpType.equals(CmpType.LE) || cmpType.equals(CmpType.LT)) {
        			dirType = DirType.MAX;        			
        		}
        	}
        	Expression qExp = new ExpressionQuantifier.Builder()
					.setDirType(dirType)
					.setCmpType(CmpType.IS)
					.setQuantified(quantified)
					.build();
            return new ExpressionOperator.Builder()
            		.setOperator(operator)
            		.setOperands(qExp, useQuantitativePropertiesOnly(expressionQuantifier.getCompare()))
            		.build();
		} else {
			List<Expression> oldChildren = expression.getChildren();
			List<Expression> newChildren = new ArrayList<>(oldChildren.size());
			for (Expression child:oldChildren) {
				newChildren.add(useQuantitativePropertiesOnly(child));
			}
			return expression.replaceChildren(newChildren);
		}
	}
	
	private Automata computeAutomata(Actions actions, Variables globalVariables) throws EPMCException {
		Automata automata = new Automata();
		automata.setModel(modelJANI);
		int number = 0;
    	for (Module module : modelPRISM.getModules()) {
    		assert module.isCommands();
    		ModuleCommands moduleCommands = (ModuleCommands) module;
    		Automaton automaton = moduleToAutomaton(moduleCommands, actions, globalVariables);
    		automaton.setNumber(number);
    		assert automaton != null;
        	modelJANI.getAutomata().addAutomaton(automaton);
        	automata.addAutomaton(automaton);
        	number++;
    	}
    	return automata;
	}

	private InitialStates computeInitialStates() {
		InitialStates initialStates = new InitialStates();
		initialStates.setModel(modelJANI);
		initialStates.setExp(modelPRISM.getInitialNodes());
		return initialStates;
	}

	/**
	 * Collect global variables.
	 * This includes the global variables of the PRISM model. It currently also 
	 * includes all local variables of modules, because these are potentially
	 * read and written by several modules.
	 * 
	 * @throws EPMCException thrown in case of problems
	 */
	private Variables buildGlobalVariables() throws EPMCException {
		Variables globalVariables = new Variables();

    	for (Entry<Expression, JANIType> entry : modelPRISM.getGlobalVariables().entrySet()) {
    		Variable variable = convertVariable((ExpressionIdentifierStandard) entry.getKey(), entry.getValue(), null);
    		globalVariables.addVariable(variable);
    	}
    	for (Module module : modelPRISM.getModules()) {
    		assert module.isCommands();
    		for (Entry<Expression, JANIType> entry : module.getVariables().entrySet()) {
    			Expression varInit = module.getInitValues().get(entry.getKey());
    			Variable variable = convertVariable((ExpressionIdentifierStandard) entry.getKey(), entry.getValue(), varInit);
    			globalVariables.addVariable(variable);
    		}
		}
    	return globalVariables;
	}

	private Variable convertVariable(ExpressionIdentifierStandard identifier, JANIType type,
			Expression varInit) throws EPMCException {
		String varName = identifier.getName();
		type.setModel(modelJANI);
		Variable variable = new Variable();
		variable.setModel(modelJANI);
		variable.setName(varName);
		variable.setIdentifier(identifier);
		variable.setAutomaton(null);
		variable.setType(type);
		variable.setInitial(varInit);
		return variable;
	}

	private Actions computeActions() {
		Actions result = new Actions();
		for (Module module : modelPRISM.getModules()) {
			ModuleCommands moduleCommands = (ModuleCommands) module;
			for (Command command : moduleCommands.getCommands()) {
				ExpressionIdentifierStandard exprAct = (ExpressionIdentifierStandard) command.getAction();
				if (!result.containsKey(exprAct)
						&& !exprAct.getName().equals(EMPTY)) {
					Action action = new Action();
					action.setName(exprAct.getName());
					result.addAction(action);
					result.addAction(action);
				} else if (!result.containsKey(exprAct)
						&& exprAct.getName().equals(EMPTY)) {
					result.addAction(tauAction);
				}
			}
		}
		return result;
	}

	/**
	 * Convert a single module to an automaton.
	 * 
	 * @param module module to convert to automaton
	 * @param actions
	 * @param globalVariables 
	 * @return automaton converted from module
	 * @throws EPMCException thrown in case of problems
	 */
	private Automaton moduleToAutomaton(ModuleCommands module, Actions actions,
			Variables globalVariables) throws EPMCException {
		Automaton automaton = new Automaton();
		Rate rateOne = new Rate();
		rateOne.setExp(ExpressionLiteral.getOne());
		rateOne.setModel(modelJANI);
		
		Edges edges = new Edges();
		edges.setModel(modelJANI);
		automaton.setModel(modelJANI);
		Location location = new Location();
		location.setModel(modelJANI);
		location.setName(LOCATION_NAME);
		automaton.getLocations().add(location);
		if (module.getInvariants() != null
			&& !isTrue(module.getInvariants())) {
			//AT: in PRISM, these are called invariants; 
			//in JANI, time progress (conditions)
			TimeProgress timeProgress = new TimeProgress();
			timeProgress.setModel(modelJANI);
			Expression invEx = prism2jani(module.getInvariants());
			timeProgress.setExp(invEx);
			location.setTimeProgress(timeProgress);
		}
		Set<Location> initialLocations = new LinkedHashSet<>();
		initialLocations.add(location);
		automaton.setInitialLocations(initialLocations);
	
		for (Command command : module.getCommands()) {
			Edge edge = new Edge();
			edge.setModel(modelJANI);
			Action action = actions.get(((ExpressionIdentifierStandard) (command.getAction())).getName());
			if (action == null) {
				action = tauAction;
			}
			edge.setAction(action);
			edge.setLocation(location);
			if (SemanticsDTMC.isDTMC(modelPRISM.getSemantics())) {
				edge.setRate(rateOne);
			}
			Guard guard = new Guard();
			guard.setModel(modelJANI);
			guard.setExp(prism2jani(command.getGuard()));
			edge.setGuard(guard);
			Destinations destinations = edge.getDestinations();
			
			Expression totalWeight = null;
			if (SemanticsCTMC.isCTMC(modelPRISM.getSemantics())) {
				for (Alternative alternative : command.getAlternatives()) {
					Expression weight = prism2jani(alternative.getWeight());
					if (totalWeight == null) {
						totalWeight = weight;
					} else {
						totalWeight = UtilExpressionStandard.opAdd(totalWeight, weight);
					}
				}
				Rate rate = new Rate();
				rate.setModel(modelJANI);
				rate.setExp(totalWeight);
				edge.setRate(rate);
			}
			
			for (Alternative alternative : command.getAlternatives()) {
				Destination destination = new Destination();
				destination.setModel(modelJANI);
				Expression probability = prism2jani(alternative.getWeight());
				if (totalWeight != null) {
					probability = UtilExpressionStandard.opDivide(probability, totalWeight);
				}
				Probability probabilityJ = new Probability();
				probabilityJ.setModel(modelJANI);
				probabilityJ.setExp(probability);
				destination.setProbability(probabilityJ);
				destination.setLocation(location);
				Assignments assignments = new Assignments();
				assignments.setModel(modelJANI);
				for (Entry<Expression, Expression> entry : alternative.getEffect().entrySet()) {
					Variable variable = globalVariables.get(((ExpressionIdentifierStandard) entry.getKey()).getName());
					assert variable != null;
					AssignmentSimple assignment = new AssignmentSimple();
					assignment.setModel(modelJANI);
					assignment.setRef(variable);
					assignment.setValue(entry.getValue());
					assignments.addAssignment(assignment);
				}
				destination.setAssignments(assignments);
				destinations.addDestination(destination);
			}
			edges.addEdge(edge);
		}
		automaton.setEdges(edges);
		automaton.setName(module.getName());
		return automaton;
	}

	/**
	 * Replaces parts of expression from PRISM model not supported in JANI.
	 * The expression parameter must no be {@code null}.
	 * 
	 * @param expression expression to be converted
	 * @return converted expression directly representable in JANI
	 * @throws EPMCException thrown in case of problems
	 */
	Expression prism2jani(Expression expression) throws EPMCException {
		assert expression != null;
		if (ExpressionOperator.isOperator(expression)) {
			return prism2jani(ExpressionOperator.asOperator(expression));
		} else {
			List<Expression> newChildren = new ArrayList<>();
			for (Expression child : expression.getChildren()) {
				newChildren.add(prism2jani(child));
			}
			return expression.replaceChildren(newChildren);
		}
	}
	
	private Expression prism2jani(ExpressionOperator expression) throws EPMCException {
		assert expression != null;
		String operator = expression.getOperator();
		switch (operator) {
		case OperatorAddInverse.IDENTIFIER: {
			Expression operand = prism2jani(expression.getOperand1());
			Expression zero = new ExpressionLiteral.Builder()
					.setValueProvider(() -> TypeReal.get().getZero())
					.build();
			return new ExpressionOperator.Builder()
					.setOperator(OperatorSubtract.IDENTIFIER)
					.setOperands(zero, operand)
					.build();
		}
		case OperatorPRISMPow.IDENTIFIER: {
			boolean allInteger = true;
			for (Expression operand : expression.getOperands()) {
				allInteger &= TypeInteger.isInteger(operand.getType(expressionToType));
			}
			List<Expression> newChildren = new ArrayList<>();
			for (Expression child : expression.getChildren()) {
				newChildren.add(prism2jani(child));
			}
			Expression result;
			if (allInteger) {
				result = new ExpressionOperator.Builder()
						.setOperands(newChildren)
						.setOperator(OperatorPow.IDENTIFIER)
						.build();
				result = new ExpressionOperator.Builder()
						.setOperands(result)
						.setOperator(OperatorCeil.IDENTIFIER)
						.build();
			} else {
				result = expression.replaceChildren(newChildren);
			}
			return result;
		}
		default: {
			List<Expression> newChildren = new ArrayList<>();
			for (Expression child : expression.getChildren()) {
				newChildren.add(prism2jani(child));
			}
			return expression.replaceChildren(newChildren);
		}
		}
	}
	
	Action getTauAction() {
		return tauAction;
	}
	
    private static boolean isTrue(Expression expression) throws EPMCException {
        assert expression != null;
        if (!(expression instanceof ExpressionLiteral)) {
            return false;
        }
        ExpressionLiteral expressionLiteral = (ExpressionLiteral) expression;
        return ValueBoolean.isTrue(expressionLiteral.getValue());
    }
}
