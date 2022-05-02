package ds.simplepds.interfaces;

/**
 * A control location & stack symbol/word pair
 * @param <L>
 * @param <S>
 */
public interface Configuration<L,S> {

    ControlLocation<L> getControlLocation();
}
