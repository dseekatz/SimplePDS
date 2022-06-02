package ds.simplepds.automata;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import ds.simplepds.interfaces.ControlLocation;
import ds.simplepds.interfaces.PushdownSystem;
import ds.simplepds.interfaces.Rule;

import java.util.Collection;

public class FastLookupRuleMap<L,S> {
    private final PushdownSystem<L,S> pds;
    private final Multimap<Integer, Rule<L,S>> wordSizeMultimap =
            HashMultimap.create();
    private final Multimap<ControlLocation<L>, Rule<L,S>> startStateMultimap = HashMultimap.create();
    private final Multimap<ControlLocation<L>, Rule<L,S>> endStateMultimap = HashMultimap.create();

    public FastLookupRuleMap(PushdownSystem<L,S> pds) {
        this.pds = pds;
        pds.getRules().forEach(rule -> {
            wordSizeMultimap.put(
                    rule.getEndConfiguration().getWord().size(),
                    rule
            );
            startStateMultimap.put(
                    rule.getStartConfiguration().getControlLocation(),
                    rule
            );
            endStateMultimap.put(
                    rule.getEndConfiguration().getControlLocation(),
                    rule
            );
        });
    }

    public Collection<Rule<L,S>> lookupByWordSize(int size) {
        return wordSizeMultimap.get(size);
    }

    public Collection<Rule<L,S>> lookupByStartState(ControlLocation<L> controlLocation) {
        return startStateMultimap.get(controlLocation);
    }

    public Collection<Rule<L,S>> lookupByEndState(ControlLocation<L> controlLocation) {
        return endStateMultimap.get(controlLocation);
    }
}
