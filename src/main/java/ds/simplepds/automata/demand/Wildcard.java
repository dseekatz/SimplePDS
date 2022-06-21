package ds.simplepds.automata.demand;

import ds.simplepds.interfaces.StackSymbol;

public interface Wildcard<S> extends StackSymbol<S> {

    @Override
    public boolean equals(Object o);
}
