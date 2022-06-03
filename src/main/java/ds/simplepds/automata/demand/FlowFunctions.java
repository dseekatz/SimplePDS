package ds.simplepds.automata.demand;

import ds.simplepds.interfaces.Rule;

import java.util.Set;

public interface FlowFunctions<L,S> {
    Set<Rule<L,S>>  apply(L currentLocation);
}

