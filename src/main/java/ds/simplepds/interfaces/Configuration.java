package ds.simplepds.interfaces;

/**
 * A control location & stack symbol pair
 * @param <L>
 * @param <S>
 */
public interface Configuration<L,S> {

    ControlLocation<L> getControlLocation();

    StackSymbol<S> getStackSymbol();
}
