/**
 * Visitor interface for processing {@code Opt} instances.
 *
 * @param <T> the type of the contained value
 * @param <R> the return type of the visitor
 */
public interface OptVisitor<T, R> {
    /**
     * Processes a {@code Some} instance.
     *
     * @param some the {@code Some} instance
     * @return the result of processing
     */
    R visit(Opt.Some<T> some);

    /**
     * Processes a {@code None} instance.
     *
     * @param none the {@code None} instance
     * @return the result of processing
     */
    R visit(Opt.None<T> none);
}
