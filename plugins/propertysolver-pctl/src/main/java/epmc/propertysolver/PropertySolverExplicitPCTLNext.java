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

package epmc.propertysolver;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import epmc.error.Positional;
import epmc.expression.Expression;
import epmc.expression.standard.DirType;
import epmc.expression.standard.ExpressionOperator;
import epmc.expression.standard.ExpressionQuantifier;
import epmc.expression.standard.ExpressionTemporal;
import epmc.expression.standard.TemporalType;
import epmc.expression.standard.TimeBound;
import epmc.expression.standard.evaluatorexplicit.EvaluatorExplicitBoolean;
import epmc.graph.CommonProperties;
import epmc.graph.Semantics;
import epmc.graph.SemanticsContinuousTime;
import epmc.graph.SemanticsDiscreteTime;
import epmc.graph.StateMap;
import epmc.graph.StateSet;
import epmc.graph.UtilGraph;
import epmc.graph.explicit.EdgeProperty;
import epmc.graph.explicit.GraphExplicit;
import epmc.graph.explicit.StateMapExplicit;
import epmc.graph.explicit.StateSetExplicit;
import epmc.graphsolver.GraphSolverConfigurationExplicit;
import epmc.graphsolver.UtilGraphSolver;
import epmc.graphsolver.objective.GraphSolverObjectiveExplicitBounded;
import epmc.modelchecker.EngineExplicit;
import epmc.modelchecker.ModelChecker;
import epmc.modelchecker.PropertySolver;
import epmc.util.BitSet;
import epmc.util.UtilBitSet;
import epmc.value.ContextValue;
import epmc.value.Operator;
import epmc.value.OperatorEvaluator;
import epmc.value.TypeAlgebra;
import epmc.value.TypeArray;
import epmc.value.TypeBoolean;
import epmc.value.TypeInteger;
import epmc.value.TypeReal;
import epmc.value.TypeWeight;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueAlgebra;
import epmc.value.ValueArray;
import epmc.value.ValueArrayAlgebra;
import epmc.value.ValueBoolean;
import epmc.value.ValueObject;
import epmc.value.operator.OperatorAdd;
import epmc.value.operator.OperatorAddInverse;
import epmc.value.operator.OperatorExp;
import epmc.value.operator.OperatorMultiply;
import epmc.value.operator.OperatorNot;
import epmc.value.operator.OperatorSet;
import epmc.value.operator.OperatorSubtract;

public final class PropertySolverExplicitPCTLNext implements PropertySolver {
    public final static String IDENTIFIER = "pctl-explicit-next";
    private ModelChecker modelChecker;
    private GraphExplicit graph;
    private StateSetExplicit computeForStates;
    private boolean negate;
    private Expression property;
    private StateSet forStates;

    @Override
    public void setModelChecker(ModelChecker modelChecker) {
        assert modelChecker != null;
        this.modelChecker = modelChecker;
        if (modelChecker.getEngine() instanceof EngineExplicit) {
            this.graph = modelChecker.getLowLevel();
        }
    }

    @Override
    public void setProperty(Expression property) {
        this.property = property;
    }

    @Override
    public void setForStates(StateSet forStates) {
        this.forStates = forStates;
    }

    @Override
    public StateMap solve() {
        assert property != null;
        assert forStates != null;
        assert property instanceof ExpressionQuantifier;
        StateSetExplicit forStatesExplicit = (StateSetExplicit) forStates;
        graph.explore(forStatesExplicit.getStatesExplicit());
        ExpressionQuantifier propertyQuantifier = (ExpressionQuantifier) property;
        Expression quantifiedProp = propertyQuantifier.getQuantified();
        DirType dirType = ExpressionQuantifier.computeQuantifierDirType(propertyQuantifier);
        StateMap result = doSolve(quantifiedProp, forStates, dirType.isMin());
        if (!propertyQuantifier.getCompareType().isIs()) {
            StateMap compare = modelChecker.check(propertyQuantifier.getCompare(), forStates);
            Operator op = propertyQuantifier.getCompareType().asExOpType();
            assert op != null;
            result = result.applyWith(op, compare);
        }
        return result;
    }

    public StateMap doSolve(Expression property, StateSet states, boolean min) {
        if (isNot(property)) {
            ExpressionOperator propertyOperator = ExpressionOperator.asOperator(property);
            property = propertyOperator.getOperand1();
            negate = true;
            min = !min;
        } else {
            negate = false;
        }
        StateSet allStates = UtilGraph.computeAllStatesExplicit(modelChecker.getLowLevel());
        ExpressionTemporal propertyTemporal = (ExpressionTemporal) property;
        Expression inner = propertyTemporal.getOperand1();
        StateMapExplicit innerResult = (StateMapExplicit) modelChecker.check(inner, allStates);
        UtilGraph.registerResult(graph, inner, innerResult);
        allStates.close();
        this.computeForStates = (StateSetExplicit) states;
        return solve(propertyTemporal, min, innerResult);
    }

    private StateMap solve(ExpressionTemporal pathTemporal, boolean min, StateMapExplicit inner) {
        assert pathTemporal != null;
        TypeAlgebra typeWeight = TypeWeight.get();
        Value one = UtilValue.newValue(typeWeight, 1);
        ValueArray resultValues = newValueArrayWeight(computeForStates.size());
        //        ValueArray result = typeArray.newValue(computeForStates.length());

        solveNext(pathTemporal, inner, min);
        OperatorEvaluator subtract = ContextValue.get().getEvaluator(OperatorSubtract.SUBTRACT, TypeWeight.get(), TypeWeight.get());
        if (negate) {
            ValueAlgebra entry = typeWeight.newValue();            
            for (int i = 0; i < resultValues.size(); i++) {
                resultValues.get(entry, i);
                subtract.apply(entry, one, entry);
                resultValues.set(entry, i);
            }
        }
        return UtilGraph.newStateMap(computeForStates.clone(), resultValues);
    }

    private void solveNext(ExpressionTemporal pathTemporal, StateMapExplicit inner, boolean min) {
        TypeAlgebra typeWeight = TypeWeight.get();
        ValueAlgebra zero = UtilValue.newValue(typeWeight, 0);
        ValueAlgebra one = UtilValue.newValue(typeWeight, 1);
        Semantics semanticsType = ValueObject.as(graph.getGraphProperty(CommonProperties.SEMANTICS)).getObject();
        BitSet allNodes = UtilBitSet.newBitSetUnbounded();
        allNodes.set(0, graph.getNumNodes());
        List<Object> nodeProperties = new ArrayList<>();
        nodeProperties.add(CommonProperties.STATE);
        nodeProperties.add(CommonProperties.PLAYER);
        List<Object> edgeProperties = new ArrayList<>();
        edgeProperties.add(CommonProperties.WEIGHT);
        GraphSolverConfigurationExplicit configuration = UtilGraphSolver.newGraphSolverConfigurationExplicit();
        int iterNumStates = graph.computeNumStates();
        ValueArrayAlgebra values = UtilValue.newArray(TypeWeight.get().getTypeArray(), iterNumStates);
        ValueBoolean valueInner = TypeBoolean.get().newValue();
        for (int i = 0; i < inner.size(); i++) {
            int state = inner.getExplicitIthState(i);
            inner.getExplicitIthValue(valueInner, i);
            boolean innerBoolean = valueInner.getBoolean();
            values.set(innerBoolean ? one : zero, state);
        }
        GraphSolverObjectiveExplicitBounded objective = new GraphSolverObjectiveExplicitBounded();
        objective.setValues(values);
        objective.setGraph(graph);
        objective.setMin(min);
        objective.setValues(values);
        objective.setTime(TypeInteger.get().getOne());
        configuration.setObjective(objective);
        configuration.solve();
        values = objective.getResult();
        TimeBound timeBound = pathTemporal.getTimeBound();
        if (SemanticsContinuousTime.isContinuousTime(semanticsType)) {
            OperatorEvaluator exp = ContextValue.get().getEvaluator(OperatorExp.EXP, TypeReal.get());
            Value rightValue = timeBound.getRightValue();
            ValueAlgebra entry = typeWeight.newValue();
            BitSet iterStates = UtilBitSet.newBitSetUnbounded();
            iterStates.set(0, graph.getNumNodes());
            ValueAlgebra leftValue = TypeWeight.get().newValue();
            OperatorEvaluator setLV = ContextValue.get().getEvaluator(OperatorSet.SET, leftValue.getType(), timeBound.getLeftValue().getType());
            setLV.apply(leftValue, timeBound.getLeftValue());
            ValueAlgebra sum = TypeWeight.get().newValue();
            ValueAlgebra jump = TypeWeight.get().newValue();
            EdgeProperty weight = graph.getEdgeProperty(CommonProperties.WEIGHT);
            OperatorEvaluator add = ContextValue.get().getEvaluator(OperatorAdd.ADD, TypeWeight.get(), TypeWeight.get());
            OperatorEvaluator addInverse = ContextValue.get().getEvaluator(OperatorAddInverse.ADD_INVERSE, jump.getType());
            OperatorEvaluator multiply = ContextValue.get().getEvaluator(OperatorMultiply.MULTIPLY, entry.getType(), jump.getType());
            OperatorEvaluator setW = ContextValue.get().getEvaluator(OperatorSet.SET, TypeWeight.get(), TypeWeight.get());
            for (int state = 0; state < iterNumStates; state++) {
                setW.apply(sum, TypeWeight.get().getZero());
                for (int succNr = 0; succNr < graph.getNumSuccessors(state); succNr++) {
                    Value succWeight = weight.get(state, succNr);
                    add.apply(sum, sum, succWeight);
                }
                multiply.apply(jump, leftValue, sum);
                addInverse.apply(jump, jump);
                exp.apply(jump, jump);
                values.get(entry, state);
                multiply.apply(entry, entry, jump);
                values.set(entry, state);
            }
        }
    }

    @Override
    public boolean canHandle() {
        assert property != null;
        if (!(modelChecker.getEngine() instanceof EngineExplicit)) {
            return false;
        }
        Semantics semantics = modelChecker.getModel().getSemantics();
        if (!SemanticsDiscreteTime.isDiscreteTime(semantics)
                && !SemanticsContinuousTime.isContinuousTime(semantics)) {
            return false;
        }
        if (!(property instanceof ExpressionQuantifier)) {
            return false;
        }
        ExpressionQuantifier propertyQuantifier = (ExpressionQuantifier) property;
        if (!isNext(propertyQuantifier.getQuantified())) {
            return false;
        }
        Set<Expression> inners = UtilPCTL.collectPCTLInner(propertyQuantifier.getQuantified());
        StateSet allStates = UtilGraph.computeAllStatesExplicit(modelChecker.getLowLevel());
        for (Expression inner : inners) {
            modelChecker.ensureCanHandle(inner, allStates);
        }
        if (allStates != null) {
            allStates.close();
        }
        return true;
    }

    @Override
    public Set<Object> getRequiredGraphProperties() {
        Set<Object> required = new LinkedHashSet<>();
        required.add(CommonProperties.SEMANTICS);
        return required;
    }

    @Override
    public Set<Object> getRequiredNodeProperties() {
        Set<Object> required = new LinkedHashSet<>();
        required.add(CommonProperties.STATE);
        required.add(CommonProperties.PLAYER);
        ExpressionQuantifier propertyQuantifier = (ExpressionQuantifier) property;
        Set<Expression> inners = UtilPCTL.collectPCTLInner(propertyQuantifier.getQuantified());
        StateSet allStates = UtilGraph.computeAllStatesExplicit(modelChecker.getLowLevel());
        for (Expression inner : inners) {
            required.addAll(modelChecker.getRequiredNodeProperties(inner, allStates));
        }
        return required;
    }

    @Override
    public Set<Object> getRequiredEdgeProperties() {
        Set<Object> required = new LinkedHashSet<>();
        required.add(CommonProperties.WEIGHT);
        return required;
    }

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    private ValueArray newValueArrayWeight(int size) {
        TypeArray typeArray = TypeWeight.get().getTypeArray();
        return UtilValue.newArray(typeArray, size);
    }

    private Expression not(Expression expression) {
        return new ExpressionOperator.Builder()
                .setOperator(OperatorNot.NOT)
                .setPositional(expression.getPositional())
                .setOperands(expression)
                .build();
    }

    private static boolean isNot(Expression expression) {
        if (!(expression instanceof ExpressionOperator)) {
            return false;
        }
        ExpressionOperator expressionOperator = (ExpressionOperator) expression;
        return expressionOperator.getOperator()
                .equals(OperatorNot.NOT);
    }

    private static boolean isNext(Expression expression) {
        if (!(expression instanceof ExpressionTemporal)) {
            return false;
        }
        ExpressionTemporal expressionTemporal = (ExpressionTemporal) expression;
        return expressionTemporal.getTemporalType() == TemporalType.NEXT;
    }

    private static boolean isFinally(Expression expression) {
        if (!(expression instanceof ExpressionTemporal)) {
            return false;
        }
        ExpressionTemporal expressionTemporal = (ExpressionTemporal) expression;
        return expressionTemporal.getTemporalType() == TemporalType.FINALLY;
    }

    private static boolean isGlobally(Expression expression) {
        if (!(expression instanceof ExpressionTemporal)) {
            return false;
        }
        ExpressionTemporal expressionTemporal = (ExpressionTemporal) expression;
        return expressionTemporal.getTemporalType() == TemporalType.GLOBALLY;
    }

    private static boolean isRelease(Expression expression) {
        if (!(expression instanceof ExpressionTemporal)) {
            return false;
        }
        ExpressionTemporal expressionTemporal = (ExpressionTemporal) expression;
        return expressionTemporal.getTemporalType() == TemporalType.RELEASE;
    }

    private static ExpressionTemporal newTemporal
    (TemporalType type, Expression op1, Expression op2,
            TimeBound bound, Positional positional) {
        assert type != null;
        assert bound != null;
        return new ExpressionTemporal
                (op1, op2, type, bound, positional);
    }
}
