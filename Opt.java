import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.*;
import java.util.stream.Stream;

/**
 * A container object which may or may not contain a non-null value.
 *
 * @param <T> the type of the value
 * @author <a href="https://github.com/philou404">philou404</a>
 * @version 1.0
 */
public sealed abstract class Opt<T> permits Opt.Some, Opt.None, Opt.Lazy {

    private static final String THE_VALUE_CAN_T_BE_NULL = "The value can't be null";
    private static final String NO_VALUE_PRESENT = "No value present";

    /**
     * Returns an {@code Opt} with the specified non-null value.
     *
     * @param value the value to be present, must not be null
     * @param <T>   the type of the value
     * @return an {@code Opt} with the value present
     * @throws NullPointerException if the value is null
     * @since 1.0
     */
    public static <T> Opt<T> of(T value) {
        return new Some<>(Objects.requireNonNull(value, THE_VALUE_CAN_T_BE_NULL));
    }

    /**
     * Flattens a stream of {@code Opt} objects to a stream of values.
     *
     * @param opts a stream of {@code Opt} objects
     * @param <T>  the type of the values
     * @return a stream containing all the present values
     * @since 1.0
     */
    public static <T> Stream<T> flatten(Stream<Opt<T>> opts) {
        return opts.flatMap(Opt::stream);
    }


    /**
     * Map the provided {@code Optional} to an {@code Opt} with the specified non-null value.<br>
     * <p>
     * If the provided {@code Optional} is non-null, it will be mapped to an {@code Opt}
     * with the contained value (if present), or {@code none} if the {@code Optional} is empty.
     *
     * @param value the {@code Optional} to be mapped, must not be null
     * @param <T>   the type of the value inside the {@code Optional}
     * @return an {@code Opt} containing the value if present, or {@code Opt.none()} if the {@code Optional} is empty
     * @throws NullPointerException if the {@code Optional} is null
     * @since 1.0
     */
    public static <T> Opt<T> of(Optional<T> value) {
        return Objects.requireNonNull(value, THE_VALUE_CAN_T_BE_NULL)
                .map(Opt::of)
                .orElseGet(Opt::none);
    }


    /**
     * Returns an {@code Opt} describing the specified value, if non-null,
     * otherwise returns an empty {@code Opt}.
     *
     * @param value the possibly-null value to describe
     * @param <T>   the type of the value
     * @return an {@code Opt} with a present value if the specified value is non-null,
     * otherwise an empty {@code Opt}
     * @since 1.0
     */
    public static <T> Opt<T> ofNullable(T value) {
        return (value == null) ? none() : new Some<>(value);
    }

    /**
     * Returns an empty {@code Opt} instance.
     *
     * @param <T> the type of the non-existent value
     * @return an empty {@code Opt}
     * @since 1.0
     */
    @SuppressWarnings("unchecked")
    public static <T> Opt<T> none() {
        return (Opt<T>) None.INSTANCE;
    }

    /**
     * Returns a lazy {@code Opt} where the value is computed only when needed.
     *
     * @param supplier the supplier of the value
     * @param <T>      the type of the value
     * @return an {@code Opt} that lazily computes its value
     * @since 1.0
     */
    public static <T> Opt<T> lazy(Supplier<? extends T> supplier) {
        return new Lazy<>(supplier);
    }

    /**
     * Sequences a stream of {@code Opt} objects into an {@code Opt} containing a stream of values.
     * If any {@code Opt} in the stream is empty, returns an empty {@code Opt}.
     *
     * @param opts a stream of {@code Opt} objects
     * @param <T>  the type of the values
     * @return an {@code Opt} containing a stream of values if all are present, otherwise empty
     * @since 1.0
     */
    public static <T> Opt<Stream<T>> sequence(Stream<Opt<T>> opts) {
        var list = opts.toList();
        return (list.stream().allMatch(Opt::isPresent)) ? Opt.of(list.stream().map(Opt::get)) : none();
    }

    /**
     * Flattens a nested {@code Opt} into a single level {@code Opt}.
     *
     * @param nested the nested {@code Opt}
     * @param <T>    the type of the inner value
     * @return the flattened {@code Opt}
     * @since 1.0
     */
    public static <T> Opt<T> flatten(Opt<Opt<T>> nested) {
        return nested.flatMap(Function.identity());
    }

    /**
     * Accepts a visitor to process this {@code Opt} instance.
     *
     * @param visitor the visitor instance
     * @param <R>     the type of the result produced by the visitor
     * @return the result produced by the visitor
     * @since 1.0
     */
    public abstract <R> R accept(OptVisitor<T, R> visitor);

    /**
     * Applies the function contained in {@code optFn} to this value if both are present.
     *
     * @param optFn an {@code Opt} containing a function to apply
     * @param <U>   the type of the result after applying the function
     * @return an {@code Opt} containing the result, or empty if either is empty
     * @since 1.0
     */
    public <U> Opt<U> ap(Opt<Function<? super T, ? extends U>> optFn) {
        return (this.isPresent() && optFn.isPresent()) ? Opt.ofNullable(optFn.get().apply(this.get())) : Opt.none();
    }

    /**
     * Combines this {@code Opt} with another {@code Opt} using the provided function.
     * If both values are present, the function is applied to produce a new result.
     * Otherwise, an empty {@code Opt} is returned.
     *
     * @param other  the other {@code Opt} to combine with
     * @param zipper the function that combines both values
     * @param <U>    the type of the other value
     * @param <R>    the type of the result
     * @return an {@code Opt} containing the combined result if both values are present, otherwise an empty {@code Opt}
     * @since 1.0
     */
    public <U, R> Opt<R> zip(Opt<U> other, BiFunction<? super T, ? super U, ? extends R> zipper) {
        return (this.isPresent() && other.isPresent()) ? Opt.ofNullable(zipper.apply(this.get(), other.get())) : Opt.none();
    }

    /**
     * If no value is present, executes the provided {@code action}.
     *
     * @param action the action to be executed if no value is present
     * @return this {@code Opt}
     * @since 1.0
     */
    public Opt<T> ifEmpty(Runnable action) {
        if (isEmpty()) {
            action.run();
        }
        return this;
    }

    /**
     * Returns a sequential {@code Stream} containing the value if present,
     * otherwise an empty {@code Stream}.
     *
     * @return a {@code Stream} of the value, or empty if not present
     * @since 1.0
     */
    public abstract Stream<T> stream();

    /**
     * Returns {@code true} if there is a value present, otherwise {@code false}.
     *
     * @return {@code true} if a value is present, otherwise {@code false}
     * @since 1.0
     */
    public abstract boolean isPresent();

    /**
     * Returns {@code true} if there is no value present.
     *
     * @return {@code true} if a value is not present, otherwise {@code false}
     * @since 1.0
     */
    public boolean isEmpty() {
        return !isPresent();
    }

    /**
     * Returns the contained value if present.
     *
     * @return the non-null value held by this {@code Opt}
     * @throws NoSuchElementException if there is no value present
     * @since 1.0
     */
    public abstract T get();

    /**
     * Returns the value if present, otherwise returns {@code other}.
     *
     * @param other the value to be returned if there is no value present
     * @return the value, if present, otherwise {@code other}
     * @since 1.0
     */
    public abstract T orElse(T other);

    /**
     * Returns the value if present, otherwise invokes {@code supplier} and returns the result.
     *
     * @param supplier the supplier whose result is returned if no value is present
     * @return the value if present, otherwise the result from {@code supplier}
     * @since 1.0
     */
    public abstract T orElseGet(Supplier<? extends T> supplier);

    /**
     * Returns the contained value if present, otherwise throws a {@code NoSuchElementException}.
     *
     * @return the value if present
     * @throws NoSuchElementException if no value is present
     * @since 1.0
     */
    public T orElseThrow() {
        if (isPresent()) {
            return get();
        }
        throw new NoSuchElementException(NO_VALUE_PRESENT);
    }

    /**
     * Returns the result of applying {@code mapper} to the contained value if present,
     * otherwise returns the result produced by {@code defaultSupplier}.
     *
     * @param defaultSupplier the supplier providing the default value
     * @param mapper          the function to apply to the value if present
     * @param <U>             the type of the result
     * @return the mapped value if present, otherwise the default value
     * @since 1.0
     */
    public <U> U mapOrElse(Supplier<? extends U> defaultSupplier, Function<? super T, ? extends U> mapper) {
        return isPresent() ? mapper.apply(get()) : defaultSupplier.get();
    }

    /**
     * Returns {@code other} if a value is present, otherwise returns an empty {@code Opt}.
     *
     * @param other the alternative {@code Opt} to return if this is present
     * @param <U>   the type of the value in the alternative {@code Opt}
     * @return {@code other} if a value is present, otherwise an empty {@code Opt}
     * @since 1.0
     */
    public <U> Opt<U> and(Opt<U> other) {
        return isPresent() ? other : Opt.none();
    }

    /**
     * Returns {@code this} if a value is present and {@code other} is empty,
     * returns {@code other} if this is empty and a value is present in {@code other},
     * otherwise returns an empty {@code Opt}.
     *
     * @param other the other {@code Opt} to compare with
     * @return the exclusive value between {@code this} and {@code other}, or empty if both are present or both are empty
     * @since 1.0
     */
    public Opt<T> xor(Opt<T> other) {
        if (isPresent() && other.isEmpty()) {
            return this;
        } else if (isEmpty() && other.isPresent()) {
            return other;
        } else {
            return Opt.none();
        }
    }

    /**
     * Returns the contained value if present, otherwise returns {@code null}.
     *
     * @return the value if present, otherwise {@code null}
     * @since 1.0
     */
    public T orNull() {
        return isPresent() ? get() : null;
    }

    /**
     * Returns an {@code Optional} describing the value if present, otherwise an empty {@code Optional}.
     *
     * @return an {@code Optional} with a present value if this {@code Opt} is non-empty, otherwise an empty {@code Optional}
     * @since 1.0
     */
    public Optional<T> toOptional() {
        return isPresent() ? Optional.of(get()) : Optional.empty();
    }

    /**
     * Returns {@code true} if a value is present and the predicate returns {@code true} for it.
     *
     * @param predicate the predicate to apply to the value
     * @return {@code true} if the value is present and matches the predicate, otherwise {@code false}
     * @since 1.0
     */
    public boolean exists(Predicate<? super T> predicate) {
        return isPresent() && predicate.test(get());
    }

    /**
     * Returns the contained value if present, otherwise throws a {@code NoSuchElementException}
     * with the provided message.
     *
     * @param message the exception message to use if no value is present
     * @return the value if present
     * @throws NoSuchElementException if no value is present
     * @since 1.0
     */
    public T expect(String message) {
        if (isPresent()) {
            return get();
        }
        throw new NoSuchElementException(message);
    }

    /**
     * Returns {@code true} if the contained value is equal to {@code value}.
     *
     * @param value the value to compare with the contained value
     * @return {@code true} if the contained value equals {@code value}, otherwise {@code false
     * @since 1.0
     */
    public boolean contains(T value) {
        return isPresent() && Objects.equals(get(), value);
    }

    /**
     * Returns the contained value if present, otherwise throws an exception produced by {@code exceptionSupplier}.
     *
     * @param exceptionSupplier the supplier of the exception to be thrown
     * @param <X>               the type of the exception to be thrown
     * @return the value if present
     * @throws X if no value is present
     * @since 1.0
     */
    public abstract <X extends Throwable> T orElseThrow(Supplier<? extends X> exceptionSupplier) throws X;

    /**
     * Applies the provided {@code mapper} function to the contained value if present,
     * and returns an {@code Opt} containing the result.
     *
     * @param mapper the function to apply to the value if present
     * @param <U>    the type of the result of the mapping
     * @return an {@code Opt} containing the result of applying the mapper, or an empty {@code Opt} if no value is present
     * @since 1.0
     */
    public abstract <U> Opt<U> map(Function<? super T, ? extends U> mapper);

    /**
     * Applies the provided {@code mapper} function to the contained value if present,
     * and returns the result directly.
     *
     * @param mapper the function to apply to the value if present
     * @param <U>    the type of the result
     * @return the result of applying the mapper, or an empty {@code Opt} if no value is present
     * @since 1.0
     */
    public abstract <U> Opt<U> flatMap(Function<? super T, Opt<U>> mapper);

    /**
     * Returns an {@code Opt} describing the value if it matches the given predicate,
     * otherwise returns an empty {@code Opt}.
     *
     * @param predicate the predicate to apply to the value, if present
     * @return {@code this} if the value matches the predicate, otherwise an empty {@code Opt}
     * @since 1.0
     */
    public abstract Opt<T> filter(Predicate<? super T> predicate);

    /**
     * Performs the given action with the contained value if present,
     * and returns this {@code Opt} unchanged.
     *
     * @param action the action to be performed on the contained value
     * @return this {@code Opt}
     * @since 1.0
     */
    public Opt<T> peek(Consumer<? super T> action) {
        if (isPresent()) {
            action.accept(get());
        }
        return this;
    }

    /**
     * Returns an {@code Opt} with the value filtered out if it satisfies the given predicate.
     *
     * @param predicate the predicate to test the value against
     * @return this {@code Opt} if the value does not satisfy the predicate,
     * otherwise an empty {@code Opt}
     * @since 1.0
     */
    public Opt<T> filterNot(Predicate<? super T> predicate) {
        return filter(predicate.negate());
    }

    /**
     * Reduces this {@code Opt} to a single value by applying the {@code mapper} function to the contained value
     * if present, or returns {@code defaultValue} if not.
     *
     * @param defaultValue the value to return if no value is present
     * @param mapper       the mapping function to apply to the value, if present
     * @param <U>          the type of the result
     * @return the result of the mapping function if a value is present, otherwise {@code defaultValue}
     * @since 1.0
     */
    public <U> U fold(U defaultValue, Function<? super T, ? extends U> mapper) {
        return isPresent() ? mapper.apply(get()) : defaultValue;
    }

    /**
     * Returns this {@code Opt} if a value is present, otherwise returns {@code alternative}.
     *
     * @param alternative the alternative {@code Opt} to return if no value is present
     * @return this {@code Opt} if a value is present, otherwise {@code alternative}
     * @since 1.0
     */
    public abstract Opt<T> or(Opt<T> alternative);

    /**
     * If a value is present, performs the given action with the value,
     * otherwise does nothing.
     *
     * @param consumer the action to be performed, if a value is present
     * @since 1.0
     */
    public abstract void ifPresent(Consumer<? super T> consumer);

    /**
     * If a value is present, performs the given action with the value,
     * otherwise runs the provided alternative.
     *
     * @param consumer    the action to be performed if a value is present
     * @param alternative the runnable to run if no value is present
     * @since 1.0
     */
    public abstract void ifPresentOrElse(Consumer<? super T> consumer, Runnable alternative);

    // Inner classes

    /**
     * Functional pattern matching.
     * Executes {@code some} if a value is present, otherwise executes {@code none}.
     *
     * @param some a function to apply if a value is present
     * @param none a supplier to invoke if no value is present
     * @param <R>  the type of the result
     * @return the result of applying {@code some} or {@code none}
     * @since 1.0
     */
    public <R> R match(Function<? super T, ? extends R> some, Supplier<? extends R> none) {
        return isPresent() ? some.apply(get()) : none.get();
    }

    /**
     * Represents the presence of a value.
     *
     * @param <T> the type of the contained value
     * @since 1.0
     */
    public static final class Some<T> extends Opt<T> {
        private final T value;

        private Some(T value) {
            this.value = value;
        }

        @Override
        public <R> R accept(OptVisitor<T, R> visitor) {
            return visitor.visit(this);
        }

        @Override
        public Stream<T> stream() {
            return Stream.of(value);
        }

        @Override
        public boolean isPresent() {
            return true;
        }

        @Override
        public T get() {
            return value;
        }

        @Override
        public T orElse(T other) {
            return value;
        }

        @Override
        public T orElseGet(Supplier<? extends T> supplier) {
            return value;
        }

        @Override
        public <X extends Throwable> T orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
            return value;
        }

        @Override
        public <U> Opt<U> map(Function<? super T, ? extends U> mapper) {
            return Opt.ofNullable(mapper.apply(value));
        }

        @Override
        public <U> Opt<U> flatMap(Function<? super T, Opt<U>> mapper) {
            return mapper.apply(value);
        }

        @Override
        public Opt<T> filter(Predicate<? super T> predicate) {
            return predicate.test(value) ? this : Opt.none();
        }

        @Override
        public Opt<T> or(Opt<T> alternative) {
            return this;
        }

        @Override
        public void ifPresent(Consumer<? super T> consumer) {
            consumer.accept(value);
        }

        @Override
        public void ifPresentOrElse(Consumer<? super T> consumer, Runnable alternative) {
            consumer.accept(value);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof Some<?> other)) return false;
            return Objects.equals(value, other.value);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(value);
        }

        @Override
        public String toString() {
            return "Some(%s)".formatted(value);
        }
    }

    /**
     * Represents the absence of a value.
     *
     * @param <T> the type of the non-existent value
     * @since 1.0
     */
    public static final class None<T> extends Opt<T> {
        private static final None<?> INSTANCE = new None<>();

        private None() {
        }

        @Override
        public <R> R accept(OptVisitor<T, R> visitor) {
            return visitor.visit(this);
        }

        @Override
        public Stream<T> stream() {
            return Stream.empty();
        }

        @Override
        public boolean isPresent() {
            return false;
        }

        @Override
        public T get() {
            throw new NoSuchElementException(NO_VALUE_PRESENT);
        }

        @Override
        public T orElse(T other) {
            return other;
        }

        @Override
        public T orElseGet(Supplier<? extends T> supplier) {
            return supplier.get();
        }

        @Override
        public <X extends Throwable> T orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
            throw exceptionSupplier.get();
        }

        @Override
        public <U> Opt<U> map(Function<? super T, ? extends U> mapper) {
            return Opt.none();
        }

        @Override
        public <U> Opt<U> flatMap(Function<? super T, Opt<U>> mapper) {
            return Opt.none();
        }

        @Override
        public Opt<T> filter(Predicate<? super T> predicate) {
            return this;
        }

        @Override
        public Opt<T> or(Opt<T> alternative) {
            return alternative;
        }

        @Override
        public void ifPresent(Consumer<? super T> consumer) {
            // Does nothing, following the behavior of Optional
        }

        @Override
        public void ifPresentOrElse(Consumer<? super T> consumer, Runnable alternative) {
            alternative.run();
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof None;
        }

        @Override
        public int hashCode() {
            return 0;
        }

        @Override
        public String toString() {
            return "None";
        }
    }

    /**
     * A {@code Lazy} wrapper for an {@code Opt} that defers the evaluation of its value
     * until it is accessed for the first time. This class extends {@code Opt} and provides
     * a mechanism for lazily loading the contained value.
     *
     * <p> This is useful for situations where the value is expensive to compute or
     * should only be computed under certain conditions.
     *
     * @param <T> the type of the value contained within the {@code Lazy}
     * @since 1.0
     */
    public static final class Lazy<T> extends Opt<T> {
        private final Supplier<? extends T> supplier;
        private Opt<T> computed = null;

        private Lazy(Supplier<? extends T> supplier) {
            this.supplier = supplier;
        }

        private Opt<T> compute() {
            if (computed == null) {
                T value = supplier.get();
                computed = (value == null) ? Opt.none() : Opt.of(value);
            }
            return computed;
        }

        @Override
        public <R> R accept(OptVisitor<T, R> visitor) {
            return compute().accept(visitor);
        }

        @Override
        public Stream<T> stream() {
            return compute().stream();
        }

        @Override
        public boolean isPresent() {
            return compute().isPresent();
        }

        @Override
        public T get() {
            return compute().get();
        }

        @Override
        public T orElse(T other) {
            return compute().orElse(other);
        }

        @Override
        public T orElseGet(Supplier<? extends T> supplier) {
            return compute().orElseGet(supplier);
        }

        @Override
        public <X extends Throwable> T orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
            return compute().orElseThrow(exceptionSupplier);
        }

        @Override
        public <U> Opt<U> map(Function<? super T, ? extends U> mapper) {
            return compute().map(mapper);
        }

        @Override
        public <U> Opt<U> flatMap(Function<? super T, Opt<U>> mapper) {
            return compute().flatMap(mapper);
        }

        @Override
        public Opt<T> filter(Predicate<? super T> predicate) {
            return compute().filter(predicate);
        }

        @Override
        public Opt<T> or(Opt<T> alternative) {
            return compute().or(alternative);
        }

        @Override
        public void ifPresent(Consumer<? super T> consumer) {
            compute().ifPresent(consumer);
        }

        @Override
        public void ifPresentOrElse(Consumer<? super T> consumer, Runnable alternative) {
            compute().ifPresentOrElse(consumer, alternative);
        }
    }

}
