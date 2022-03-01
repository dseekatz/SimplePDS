package ds.simplepds.interfaces;

import java.util.Collection;

/**
 * A Pushdown system is a collection of rules.
 * @param <L>
 * @param <S>
 */
public interface PushdownSystem<L,S> {

    Collection<Rule<L,S>> getRules();
}
