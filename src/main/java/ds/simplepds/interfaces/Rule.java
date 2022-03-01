package ds.simplepds.interfaces;

/**
 * A Rule describes a transition from a start configuration to an end configuration
 * @param <L>
 * @param <S>
 */
public interface Rule<L,S> {

    Configuration<L,S> getStartConfiguration();

    Configuration<L,S> getEndConfiguration();
}
