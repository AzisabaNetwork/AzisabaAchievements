package net.azisaba.azisabaachievements.api.serialization;

import org.jetbrains.annotations.NotNull;
import xyz.acrylicstyle.util.serialization.decoder.ValueDecoder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

public class ResultSetValueDecoder implements ValueDecoder, AutoCloseable {
    private final ResultSet rs;
    private String label;

    public ResultSetValueDecoder(@NotNull ResultSet rs) {
        this.rs = rs;
    }

    @Override
    public void push(@NotNull String label) {
        this.label = label;
    }

    @Override
    public void pop() {
        label = null;
    }

    public @NotNull String getLabel() {
        return Objects.requireNonNull(label, "label is null");
    }

    @Override
    public @NotNull String decodeString() {
        try {
            return rs.getString(getLabel());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int decodeInt() {
        try {
            return rs.getInt(getLabel());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public long decodeLong() {
        try {
            return rs.getLong(getLabel());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public float decodeFloat() {
        try {
            return rs.getFloat(getLabel());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public double decodeDouble() {
        try {
            return rs.getDouble(getLabel());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean decodeBoolean() {
        try {
            return rs.getBoolean(getLabel());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte decodeByte() {
        try {
            return rs.getByte(getLabel());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public char decodeChar() {
        try {
            return rs.getString(getLabel()).charAt(0);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public short decodeShort() {
        try {
            return rs.getShort(getLabel());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void decodeNull() {
    }

    @Override
    public void close() throws SQLException {
        rs.close();
    }
}
