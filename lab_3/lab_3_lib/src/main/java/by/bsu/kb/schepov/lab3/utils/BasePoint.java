package by.bsu.kb.schepov.lab3.utils;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Getter
public class BasePoint extends Point{
    private int order;
    private Group group;

    public BasePoint(int x, int y, int order, Group group) {
        super(x, y);
        this.order = order;
        this.group = group;
    }
}
