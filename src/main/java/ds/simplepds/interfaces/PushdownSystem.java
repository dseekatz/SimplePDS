package ds.simplepds.interfaces;

import java.util.Set;

/**
 * A Pushdown system is a collection of rules.
 * @param <L>
 * @param <S>
 */
public interface PushdownSystem<L,S> {

    Set<Rule<L,S>> getRules();
}
