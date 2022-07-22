package ds.simplepds.automata.demand;

import ds.simplepds.automata.PAutomaton;
import ds.simplepds.interfaces.ControlLocation;
import ds.simplepds.interfaces.EndConfiguration;
import ds.simplepds.interfaces.Rule;
import ds.simplepds.interfaces.StackSymbol;
import ds.simplepds.interfaces.StartConfiguration;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

/**
 * This demand-driven implementation of Pre-* is most useful when the following conditions hold:
 *  - A complete PDS modelling a particular system is too large for other implementations to be efficient
 *  - There is a way to generate relevant PDS rules on demand (e.g. by traversing a program's AST or Flowgraph)
 * @param <L>
 * @param <S>
 */
public class DemandPreStar<L,S> {
    private final BackwardFlowFunctions<L,S> flowFunction;
    private final PAutomaton<L,S> initialAutomaton;
    protected final PAutomaton<L,S> saturatedAut = new PAutomaton<>();
    protected final Queue<PAutomaton.Transition<L, S>> worklist;
    protected final Set<Rule<L,S>> deltaPrime = new HashSet<>();

    public DemandPreStar(BackwardFlowFunctions<L, S> flowFunction, PAutomaton<L, S> initialAutomaton) {
        this.flowFunction = flowFunction;
        this.initialAutomaton = initialAutomaton;
        this.worklist = new LinkedList<>(initialAutomaton.getTransitionRelation());
    }

    public void apply() {
        // Initialize the states (and final states) of the saturated automaton
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
                // Handle extra rules generated by pop rule
                deltaPrime.forEach(rule -> {
                    if (rule.getEndConfiguration().getControlLocation().equals(current.getStartState())) {
                        handleNormalRule(rule, current);
                    }
                });
            }
        }
    }

    protected void handleNormalRule(Rule<L, S> rule, PAutomaton.Transition<L, S> current) {
        if (rule.getEndConfiguration().getWord().get(0).equals(current.getLabel())) {
            worklist.add(new PAutomaton.Transition<>(
                    rule.getStartConfiguration().getControlLocation(),
                    current.getEndState(),
                    rule.getStartConfiguration().getStackSymbol()
            ));
        }
    }

    protected void handlePopRule(Rule<L, S> rule, PAutomaton.Transition<L, S> current) {
        worklist.add(
                new PAutomaton.Transition<>(
                        rule.getStartConfiguration().getControlLocation(),
                        rule.getEndConfiguration().getControlLocation(),
                        rule.getStartConfiguration().getStackSymbol()
                )
        );
    }

    protected void handlePushRule(Rule<L, S> rule, PAutomaton.Transition<L, S> current) {
        if (rule.getEndConfiguration().getWord().get(0).equals(current.getLabel())) {
            deltaPrime.add(new GeneratedRule<>(
                    rule.getStartConfiguration().getControlLocation(),
                    rule.getStartConfiguration().getStackSymbol(),
                    current.getEndState(),
                    rule.getEndConfiguration().getWord().get(1)
            ));

            for (PAutomaton.Transition<L,S> transition : saturatedAut.getTransitionRelation()) {
                if (transition.getStartState().equals(current.getEndState()) &&
                        transition.getLabel().equals(rule.getEndConfiguration().getWord().get(1)))
                {
                    worklist.add(new PAutomaton.Transition<>(
                            rule.getStartConfiguration().getControlLocation(),
                            transition.getEndState(),
                            rule.getStartConfiguration().getStackSymbol()
                    ));
                }
            }
        }
    }

    public PAutomaton<L, S> getSaturatedAut() {
        return saturatedAut;
    }

    public PAutomaton<L, S> getInitialAutomaton() {
        return initialAutomaton;
    }

    protected static class GeneratedRule<L,S> implements Rule<L,S> {

        private final StartConfiguration<L,S> startConfiguration;
        private final EndConfiguration<L,S> endConfiguration;

        protected GeneratedRule(
                ControlLocation<L> startLoc,
                StackSymbol<S> startSym,
                ControlLocation<L> endLoc,
                StackSymbol<S> endSym
        ) {
            this.startConfiguration = new StartConfiguration<L, S>() {
                @Override
                public StackSymbol<S> getStackSymbol() {
                    return startSym;
                }

                @Override
                public ControlLocation<L> getControlLocation() {
                    return startLoc;
                }
            };
            this.endConfiguration = new EndConfiguration<L, S>() {
                @Override
                public List<StackSymbol<S>> getWord() {
                    return Collections.singletonList(endSym);
                }

                @Override
                public ControlLocation<L> getControlLocation() {
                    return endLoc;
                }
            };
        }

        @Override
        public StartConfiguration<L, S> getStartConfiguration() {
            return startConfiguration;
        }

        @Override
        public EndConfiguration<L, S> getEndConfiguration() {
            return endConfiguration;
        }
    }
}
