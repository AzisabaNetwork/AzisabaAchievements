package net.azisaba.azisabaachievements.api.util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public interface Either<L, R> {
    boolean isLeft();
    boolean isRight();

    /**
     * Returns the left.
     * @return left
     * @throws IllegalStateException If left is not present
     */
    @Contract(pure = true)
    L getLeft();

    /**
     * Returns the right.
     * @return right
     * @throws IllegalStateException If right is not present
     */
    @Contract(pure = true)
    R getRight();

    @Contract(value = "_ -> new", pure = true)
    static <L, R> @NotNull Either<L, R> left(L left) {
        return new EitherImpl<>(true, left, false, null);
    }

    @Contract(value = "_ -> new", pure = true)
    static <L, R> @NotNull Either<L, R> right(R right) {
        return new EitherImpl<>(false, null, true, right);
    }
}
