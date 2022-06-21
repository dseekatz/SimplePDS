package ds.simplepds.automata.demand;

import ds.simplepds.interfaces.StackSymbol;

public abstract class Wildcard<S> implements StackSymbol<S> {

    @Override
    public boolean equals(Object o) {
        return o instanceof StackSymbol<?>;
    }
}
