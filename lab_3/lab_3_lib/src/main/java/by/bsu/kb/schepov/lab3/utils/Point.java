package by.bsu.kb.schepov.lab3.utils;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@EqualsAndHashCode
@AllArgsConstructor
@Getter
public class Point {
    public static final Point ZERO = new Point(-1, -1);
    private int x;
    private int y;

    @Override
    public String toString() {
        return String.format("(%d, %d)", x, y);
    }
}
