package ds.simplepds.automata;

import ds.simplepds.interfaces.ControlLocation;
import ds.simplepds.interfaces.Rule;
import ds.simplepds.interfaces.StackSymbol;

import java.util.HashSet;
import java.util.Set;

public class PAutomaton<L,S> {

    private final Set<ControlLocation<L>> initialStates = new HashSet<>();
    private final Set<ControlLocation<L>> finalStates = new HashSet<>();
    private final Set<ControlLocation<L>> states = new HashSet<>();
    private final Set<Transition<L,S>> transitionRelation = new HashSet<>();
    private final AcceptingState acceptingState = new AcceptingState();
    private final EpsilonStackSymbol epsilonStackSymbol = new EpsilonStackSymbol();


    public void addInitialState(ControlLocation<L> initialState) {
        initialStates.add(initialState);
        states.add(initialState);
    }

    public void addFinalState(ControlLocation<L> finalState) {
        finalStates.add(finalState);
        states.add(finalState);
    }

    public void addTransition(ControlLocation<L> start, ControlLocation<L> end, StackSymbol<S> label) {
        transitionRelation.add(
                new Transition<>(
                        start,
                        end,
                        label
                )
        );
        states.add(start);
        states.add(end);
    }

    public Set<ControlLocation<L>> getInitialStates() {
        return initialStates;
    }

    public Set<ControlLocation<L>> getFinalStates() {
        return finalStates;
    }

    public Set<Transition<L, S>> getTransitionRelation() {
        return transitionRelation;
    }

    public AcceptingState getDummyAcceptingState() {
        return acceptingState;
    }


    public EpsilonStackSymbol getEpsilonStackSymbol() {
        return epsilonStackSymbol;
    }

    public Set<ControlLocation<L>> getAllStates() {
        return states;
    }

    public CreatedState generateNewState(Rule<L,S> generatingRule) {
        return generateNewState(generatingRule);
    }

    public ControlLocation<L> getGeneratedStateFromRule(Rule<L, S> rule) {
        return states.stream()
                .filter(state -> state instanceof PAutomaton.CreatedState cs &&
                                    cs.generatingRule.equals(rule))
                .findAny()
                .orElseThrow(() -> new RuntimeException("Failed to find state generated by rule"));
    }

    public static class Transition<L,S> {

        private ControlLocation<L> startState;
        private ControlLocation<L> endState;
        private StackSymbol<S> label;

        private Transition(ControlLocation<L> startState, ControlLocation<L> endState, StackSymbol<S> label) {
            this.startState = startState;
            this.endState = endState;
            this.label = label;
        }

        public ControlLocation<L> getStartState() {
            return startState;
        }

        public ControlLocation<L> getEndState() {
            return endState;
        }

        public StackSymbol<S> getLabel() {
            return label;
        }
    }

    public class AcceptingState implements ControlLocation<L> {

        /**
         * TODO: figure out a better way to do this
         * @return
         */
        @Override
        public L unwrap() {
            return null;
        }
    }

    public class CreatedState implements ControlLocation<L> {


        private final Rule<L,S> generatingRule;

        private CreatedState(Rule<L,S> generatingRule) {
            this.generatingRule = generatingRule;
        }

        /**
         * TODO: figure out a better way to do this
         * @return
         */
        @Override
        public L unwrap() {
            return null;
        }
    }

    public class EpsilonStackSymbol implements StackSymbol<S> {

        /**
         * TODO: figure out a better way to do this
         * @return
         */
        @Override
        public S unwrap() {
            return null;
        }
    }
}
