package ds.simplepds.automata;

import com.google.common.collect.Sets;
import ds.simplepds.interfaces.PushdownSystem;
import ds.simplepds.interfaces.Rule;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

/**
 * An alternative prestar implementation that is faster but less space efficient
 *
 * @param <L>
 * @param <S>
 */
public class HashBasedPreStar<L,S> extends Prestar<L,S>{

    private final FastLookupRuleMap<L,S> fastLookupMap;

    public HashBasedPreStar(
            PushdownSystem<L, S> pushdownSystem,
            PAutomaton<L, S> initialAutomaton,
            FastLookupRuleMap<L,S> fastLookupMap
    ) {
        super(pushdownSystem, initialAutomaton);
        this.fastLookupMap = fastLookupMap;
    }

    /**
     * Implementation of pre-* (see Esparza, et al. (CAV00) Alg. 1)
     */
    private void apply() {
        //Initialize the worklist and the set of synthesized PDS rules (deltaPrime)
        Queue<PAutomaton.Transition<L, S>> worklist = new LinkedList<>(initialAut.getTransitionRelation());

        // Initialize the states (and final states) of the saturated automaton
        initialAut.getAllStates().forEach(saturatedAut::addState);
        initialAut.getFinalStates().forEach(saturatedAut::addFinalState);
        initialAut.getInitialStates().forEach(saturatedAut::addInitialState);

        // Handle PDS pop rules
        for (Rule<L, S> rule : fastLookupMap.lookupByWordSize(0)) {
                worklist.add(
                        new PAutomaton.Transition<>(
                                rule.getStartConfiguration().getControlLocation(),
                                rule.getEndConfiguration().getControlLocation(),
                                rule.getStartConfiguration().getStackSymbol()
                        )
                );
        }

        // Process the worklist
        while (!worklist.isEmpty()) {
            PAutomaton.Transition<L, S> current = worklist.remove();
            if (!saturatedAut.getTransitionRelation().contains(current)) {
                saturatedAut.addTransition(current);

                // Handle PDS normal Rules
                for (Rule<L, S> rule : fastLookupMap.lookupByStartState(current.getStartState())) {
                    if (rule.getEndConfiguration().getWord().size() == 1 &&
                            rule.getEndConfiguration().getWord().get(0).equals(current.getLabel())) {
                        worklist.add(new PAutomaton.Transition<>(
                                rule.getStartConfiguration().getControlLocation(),
                                current.getEndState(),
                                rule.getStartConfiguration().getStackSymbol()
                        ));
                    }
                }

                // Handle PDS Push Rules
                for (Rule<L, S> rule : fastLookupMap.lookupByStartState(current.getStartState())) {
                    if (rule.getEndConfiguration().getWord().size() == 2 &&
                            rule.getEndConfiguration().getWord().get(0).equals(current.getLabel())) {
                        fastLookupMap.addRule(new GeneratedRule<>(
                                rule.getStartConfiguration().getControlLocation(),
                                rule.getStartConfiguration().getStackSymbol(),
                                current.getEndState(),
                                rule.getEndConfiguration().getWord().get(1)
                        ));

                        for (PAutomaton.Transition<L, S> transition : saturatedAut.getTransitionRelation()) {
                            if (transition.getStartState().equals(current.getEndState()) &&
                                    transition.getLabel().equals(rule.getEndConfiguration().getWord().get(1))) {
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
}
