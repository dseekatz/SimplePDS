package ds.simplepds.automata.demand;


import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import ds.simplepds.automata.PAutomaton;
import ds.simplepds.interfaces.ControlLocation;
import ds.simplepds.interfaces.Rule;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * This demand-driven implementation of Post-* is most useful when the following conditions hold:
 *  - A complete PDS modelling a particular system is too large for other implementations to be efficient
 *  - There is a way to generate relevant PDS rules on demand (e.g. by traversing a program's AST or Flowgraph)
 * @param <L>
 * @param <S>
 */
public class DemandPostStar<L,S>{
    private final ForwardFlowFunctions<L,S> flowFunction;
    private final PAutomaton<L,S> initialAutomaton;
    private final Function<Rule<L,S>, L> generatedStateIdentifierFunction;
    private final PAutomaton<L,S> saturatedAut = new PAutomaton<>();
    private final Multimap<ControlLocation<L>, ControlLocation<L>> incomingEpsilons = HashMultimap.create();
    private final Queue<PAutomaton.Transition<L, S>> worklist;

    public DemandPostStar(
            ForwardFlowFunctions<L,S> flowFunction,
            PAutomaton<L,S> initialAutomaton,
            Function<Rule<L,S>, L> generatedStateIdentifierFunction
    ) {
        this.flowFunction = flowFunction;
        this.initialAutomaton = initialAutomaton;
        this.generatedStateIdentifierFunction = generatedStateIdentifierFunction;
        this.worklist =
                initialAutomaton.getTransitionRelation()
                        .stream()
                        .filter(transition ->
                                initialAutomaton.getInitialStates()
                                        .contains(transition.getStartState())
                        ).collect(Collectors.toCollection(LinkedList::new));
    }

    public void apply() {

        // All transitions from the initial automaton that are not in the worklist
        // are added directly to the transition relation for the saturated automaton
        Sets.difference(initialAutomaton.getTransitionRelation(), new HashSet<>(worklist))
                .forEach(saturatedAut::addTransition);

        // Initialize the states of the saturated automaton
        initialAutomaton.getAllStates().forEach(saturatedAut::addState);
        initialAutomaton.getFinalStates().forEach(saturatedAut::addFinalState);
        initialAutomaton.getInitialStates().forEach(saturatedAut::addInitialState);

        // process the worklist
        while (!worklist.isEmpty()) {
            PAutomaton.Transition<L,S> current = worklist.remove();
            if (!saturatedAut.getTransitionRelation().contains(current)) {
                saturatedAut.addTransition(current);

                Set<Rule<L,S>> rules = flowFunction.apply(current.getStartState().unwrap());
                rules.forEach(rule -> {
                    if (rule.getEndConfiguration().getWord().size() == 2) {
                        handlePushRule(rule, current);
                    } else if (rule.getEndConfiguration().getWord().size() == 0) {
                        handlePopRule(rule, current);
                    } else if (rule.getEndConfiguration().getWord().size() == 1) {
                        handleNormalRule(rule, current);
                    }
                });
            }
        }
    }

    private void handleNormalRule(Rule<L, S> rule,  PAutomaton.Transition<L,S> current) {
        if (rule.getStartConfiguration().getStackSymbol().equals(current.getLabel())) {
            worklist.add(new PAutomaton.Transition<>(
                    rule.getEndConfiguration().getControlLocation(),
                    current.getEndState(),
                    rule.getEndConfiguration().getWord().get(0)
            ));
        }
    }

    private void handlePopRule(Rule<L, S> rule,  PAutomaton.Transition<L,S> current) {
        if (rule.getStartConfiguration().getStackSymbol().equals(current.getLabel()) &&
                !incomingEpsilons.containsEntry(
                        current.getEndState(),
                        rule.getEndConfiguration().getControlLocation()
                )
            ) {
            incomingEpsilons.put(current.getEndState(), rule.getEndConfiguration().getControlLocation());
            for (PAutomaton.Transition<L,S> transition : saturatedAut.getTransitionRelation()) {
                if (transition.getStartState().equals(current.getEndState())) {
                    worklist.add(new PAutomaton.Transition<>(
                            rule.getEndConfiguration().getControlLocation(),
                            transition.getEndState(),
                            transition.getLabel()
                    ));
                }
            }
            if (saturatedAut.getFinalStates().contains(current.getEndState())) {
                saturatedAut.addFinalState(rule.getEndConfiguration().getControlLocation());
            }
        }
    }

    private void handlePushRule(Rule<L, S> rule,  PAutomaton.Transition<L,S> current) {
        // Initial push rule processing
        GeneratedState generated = new GeneratedState(rule);
        saturatedAut.addState(generated);
        worklist.add(new PAutomaton.Transition<>(
                rule.getEndConfiguration().getControlLocation(),
                generated,
                rule.getEndConfiguration().getWord().get(0)
        ));

        if (rule.getStartConfiguration().getStackSymbol().equals(current.getLabel())) {
            GeneratedState newGenerated = new GeneratedState(rule);
            saturatedAut.addTransition(new PAutomaton.Transition<>(
                    newGenerated,
                    current.getEndState(),
                    rule.getEndConfiguration().getWord().get(1)
            ));
            for (ControlLocation<L> state : incomingEpsilons.get(newGenerated)) {
                worklist.add(new PAutomaton.Transition<>(
                        state,
                        current.getEndState(),
                        rule.getEndConfiguration().getWord().get(1)
                ));
            }
        }
    }

    public PAutomaton<L, S> getSaturatedAut() {
        return saturatedAut;
    }

    public GeneratedState createGeneratedStateFromRule(Rule<L, S> rule) {
        return new GeneratedState(rule);
    }

    public class GeneratedState implements ControlLocation<L> {

        private final Rule<L,S> generatingRule;

        protected GeneratedState(Rule<L, S> generatingRule) {
            this.generatingRule = generatingRule;
        }

        @Override
        public L unwrap() {
            return generatedStateIdentifierFunction.apply(generatingRule);
        }

        public Rule<L,S> getGeneratingRule() {
            return generatingRule;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            GeneratedState that = (GeneratedState) o;
            return Objects.equals(generatingRule, that.generatingRule);
        }

        @Override
        public int hashCode() {
            return Objects.hash(generatingRule);
        }
    }
}
