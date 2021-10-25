import by.bsu.kb.schepov.lab3.utils.Group;
import by.bsu.kb.schepov.lab3.utils.GroupParams;
import by.bsu.kb.schepov.lab3.utils.Point;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GroupTest {

    @Test
    void testAddPoints() {
        Group group = new Group().setParams(new GroupParams(2, 3, 97));
        Point a = group.addPoints(new Point(3, 6), new Point(3, 6));
        assertEquals(new Point(80, 10), a);
        Point b = group.addPoints(new Point(3, 6), new Point(80, 10));
        assertEquals(new Point(80, 87), b);
        Group group1 = new Group().setParams(new GroupParams(9, 17, 23));
        assertEquals(new Point(20, 20), group1.addPoints(new Point(16, 5), new Point(16, 5)));
    }

    @Test
    void testMultiply() {
        Group group = new Group().setParams(new GroupParams(2, 3, 97));
        Point p = new Point(3, 6);
        assertEquals(new Point(80, 10), group.multiply(2, p));
        assertEquals(new Point(80, 87), group.multiply(3, p));
        assertEquals(new Point(3, 91), group.multiply(9, p));
        assertEquals(Point.ZERO, group.multiply(5, p));
        Group group1 = new Group().setParams(new GroupParams(43, 22, 71));
        assertEquals(new Point(4, 51), group1.multiply(3, new Point(3, 65)));
    }
}