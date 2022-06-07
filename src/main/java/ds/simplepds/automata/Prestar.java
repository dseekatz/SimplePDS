package ds.simplepds.automata;

import com.google.common.collect.Sets;
import ds.simplepds.interfaces.ControlLocation;
import ds.simplepds.interfaces.EndConfiguration;
import ds.simplepds.interfaces.PushdownSystem;
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
 * An instance of the prestar algorithm for a given initial configuration and pushdown system
 * @param <L>
 * @param <S>
 */
public class Prestar<L,S> {

    protected final PAutomaton<L,S> saturatedAut;
    protected final PAutomaton<L,S> initialAut;
    protected final PushdownSystem<L,S> pushdownSystem;

    public Prestar(
            PushdownSystem<L,S> pushdownSystem,
            PAutomaton<L,S> initialAutomaton
    ) {
        this.pushdownSystem = pushdownSystem;
        this.saturatedAut = new PAutomaton<>();
        this.initialAut = initialAutomaton;
    }

    /**
     * Implementation of pre-* (see Esparza, et al. (CAV00) Alg. 1)
     */
    public void apply() {
        //Initialize the worklist and the set of synthesized PDS rules (deltaPrime)
        Queue<PAutomaton.Transition<L, S>> worklist = new LinkedList<>(initialAut.getTransitionRelation());
        Set<Rule<L,S>> deltaPrime = new HashSet<>();

        // Initialize the states (and final states) of the saturated automaton
        initialAut.getAllStates().forEach(saturatedAut::addState);
        initialAut.getFinalStates().forEach(saturatedAut::addFinalState);
        initialAut.getInitialStates().forEach(saturatedAut::addInitialState);

        // Handle PDS pop rules
        for (Rule<L,S> rule : pushdownSystem.getRules()) {
            if (rule.getEndConfiguration().getWord().size()== 0) {
                worklist.add(
                        new PAutomaton.Transition<>(
                                rule.getStartConfiguration().getControlLocation(),
                                rule.getEndConfiguration().getControlLocation(),
                                rule.getStartConfiguration().getStackSymbol()
                        )
                );
            }
        }

        // Process the worklist
        while (!worklist.isEmpty()) {
            PAutomaton.Transition<L,S> current = worklist.remove();
            if (!saturatedAut.getTransitionRelation().contains(current)) {
                saturatedAut.addTransition(current);

                // Handle PDS normal Rules
                for (Rule<L,S> rule : Sets.union(pushdownSystem.getRules(), deltaPrime)) {
                    if (rule.getEndConfiguration().getWord().size() == 1 &&
                        rule.getEndConfiguration().getControlLocation().equals(current.getStartState()) &&
                        rule.getEndConfiguration().getWord().get(0).equals(current.getLabel()))
                    {
                        worklist.add(new PAutomaton.Transition<>(
                                rule.getStartConfiguration().getControlLocation(),
                                current.getEndState(),
                                rule.getStartConfiguration().getStackSymbol()
                        ));
                    }
                }

                // Handle PDS Push Rules
                for (Rule<L,S> rule : pushdownSystem.getRules()) {
                    if (rule.getEndConfiguration().getWord().size() == 2 &&
                        rule.getEndConfiguration().getControlLocation().equals(current.getStartState()) &&
                        rule.getEndConfiguration().getWord().get(1).equals(current.getLabel()))
                    {
                        deltaPrime.add(new GeneratedRule<>(
                                rule.getStartConfiguration().getControlLocation(),
                                rule.getStartConfiguration().getStackSymbol(),
                                current.getEndState(),
                                rule.getEndConfiguration().getWord().get(0)
                        ));

                        for (PAutomaton.Transition<L,S> transition : saturatedAut.getTransitionRelation()) {
                            if (transition.getStartState().equals(current.getEndState()) &&
                                transition.getLabel().equals(rule.getEndConfiguration().getWord().get(0)))
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
            }
        }
    }

    public PAutomaton<L, S> getSaturatedAut() {
        return saturatedAut;
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
