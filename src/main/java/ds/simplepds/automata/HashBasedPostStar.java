package ds.simplepds.automata;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import ds.simplepds.interfaces.ControlLocation;
import ds.simplepds.interfaces.PushdownSystem;
import ds.simplepds.interfaces.Rule;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * An alternative poststar implementation that is faster but less space efficient
 *
 * @param <L>
 * @param <S>
 */
public class HashBasedPostStar<L,S> extends Poststar<L,S>{

    private final FastLookupRuleMap<L,S> fastLookupMap;

    public HashBasedPostStar(
            PushdownSystem<L, S> pushdownSystem,
            PAutomaton<L, S> initialAutomaton,
            Function<Rule<L, S>, L> generatedStateIdentifierFunction,
            FastLookupRuleMap<L,S> fastLookupMap
    ) {
        super(pushdownSystem, initialAutomaton, generatedStateIdentifierFunction);
        this.fastLookupMap = fastLookupMap;
    }

    /**
     * Implementation of post-*.  See Esparza, et al. (CAV00) Alg. 3.
     */
    @Override
    public void apply() {
        // Initialize the worklist with transitions from the initial automaton
        // that start at an initial state
        Queue<PAutomaton.Transition<L, S>> worklist =
                initialAut.getTransitionRelation()
                        .stream()
                        .filter(transition ->
                                initialAut.getInitialStates()
                                        .contains(transition.getStartState())
                        ).collect(Collectors.toCollection(LinkedList::new));

        // All transitions from the initial automaton that are not in the worklist
        // are added directly to the transition relation for the saturated automaton
        Sets.difference(initialAut.getTransitionRelation(), new HashSet<>(worklist))
                .forEach(saturatedAut::addTransition);

        // Initialize the states of the saturated automaton
        initialAut.getAllStates().forEach(saturatedAut::addState);
        initialAut.getFinalStates().forEach(saturatedAut::addFinalState);
        initialAut.getInitialStates().forEach(saturatedAut::addInitialState);

        // Initial processing of push rules. For each push rule we create a new
        // state and add a transition from the push rule's end location
        for (Rule<L,S> rule : fastLookupMap.lookupByWordSize(2)) {
            GeneratedState generated = new GeneratedState(rule);
            saturatedAut.addState(generated);
            worklist.add(new PAutomaton.Transition<>(
                    rule.getEndConfiguration().getControlLocation(),
                    generated,
                    rule.getEndConfiguration().getWord().get(1)
            ));
        }

        // Initialize a data structure to keep track of incoming
        // epsilon transitions for each state
        Multimap<ControlLocation<L>, ControlLocation<L>> incomingEpsilons = HashMultimap.create();

        // Process the worklist
        while (!worklist.isEmpty()) {
            PAutomaton.Transition<L,S> current = worklist.remove();
            if (!saturatedAut.getTransitionRelation().contains(current)) {
                saturatedAut.addTransition(current);

                // Handle PDS pop rules
                for (Rule<L,S> rule : fastLookupMap.lookupByStartState(current.getStartState())) {
                    if (rule.getEndConfiguration().getWord().size() == 0 &&
                            rule.getStartConfiguration().getStackSymbol().equals(current.getLabel()) &&
                            !incomingEpsilons.containsEntry(
                                    current.getEndState(),
                                    rule.getEndConfiguration().getControlLocation()
                            ))
                    {
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

                // Handle PDS normal rules
                for (Rule<L,S> rule : fastLookupMap.lookupByStartState(current.getStartState())) {
                    if (rule.getEndConfiguration().getWord().size() == 1 &&
                            rule.getStartConfiguration().getStackSymbol().equals(current.getLabel()))
                    {
                        worklist.add(new PAutomaton.Transition<>(
                                rule.getEndConfiguration().getControlLocation(),
                                current.getEndState(),  // Is this correct? CAV00 paper presumably has a typo
                                // and uses an unbound symbol as the end of this transition.
                                rule.getEndConfiguration().getWord().get(0)
                        ));
                    }
                }

                // Handle PDS push rules
                for (Rule<L,S> rule : fastLookupMap.lookupByStartState(current.getStartState())) {
                    if (rule.getEndConfiguration().getWord().size() == 2 &&
                            rule.getStartConfiguration().getStackSymbol().equals(current.getLabel()))
                    {
                        GeneratedState generated = new GeneratedState(rule);
                        saturatedAut.addTransition(new PAutomaton.Transition<>(
                                generated,
                                current.getEndState(),
                                rule.getEndConfiguration().getWord().get(0)
                        ));
                        for (ControlLocation<L> state : incomingEpsilons.get(generated)) {
                            worklist.add(new PAutomaton.Transition<>(
                                    state,
                                    current.getEndState(),
                                    rule.getEndConfiguration().getWord().get(0)
                            ));
                        }
                    }
                }
            }
        }
    }
}
