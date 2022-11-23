package net.azisaba.azisabaachievements.api.util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

final class EitherImpl<L, R> implements Either<L, R> {
    private final boolean isLeft;
    private final L left;
    private final boolean isRight;
    private final R right;

    @Contract(pure = true)
    EitherImpl(boolean isLeft, L left, boolean isRight, R right) {
        this.isLeft = isLeft;
        this.left = left;
        this.isRight = isRight;
        this.right = right;
    }


    @Contract(pure = true)
    @Override
    public boolean isLeft() {
        return isLeft;
    }

    @Contract(pure = true)
    @Override
    public boolean isRight() {
        return isRight;
    }

    @Contract(pure = true)
    @Override
    public L getLeft() {
        if (!isLeft) {
            throw new IllegalStateException("Left is not present");
        }
        return left;
    }

    @Contract(pure = true)
    @Override
    public R getRight() {
        if (!isRight) {
            throw new IllegalStateException("Right is not present");
        }
        return right;
    }

    @Contract(value = "null -> false", pure = true)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EitherImpl)) return false;
        EitherImpl<?, ?> either = (EitherImpl<?, ?>) o;
        return isLeft == either.isLeft && isRight == either.isRight && Objects.equals(left, either.right) && Objects.equals(right, either.right);
    }

    @Override
    public int hashCode() {
        return Objects.hash(isLeft, isLeft, isRight, isRight);
    }

    @Contract(pure = true)
    @Override
    public @NotNull String toString() {
        if (isLeft) {
            return "Either[L]{" + left + '}';
        } else {
            return "Either[R]{" + right + '}';
        }
    }
}
