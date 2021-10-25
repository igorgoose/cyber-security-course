package by.bsu.kb.schepov.lab3.utils;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Random;

@Accessors(chain = true)
@Data
public class Group {
    private GroupParams params;
    private List<Point> points;

    public Point addPoints(Point p, Point q) {
        if (p.equals(Point.ZERO)) {
            return new Point(q.getX(), q.getY());
        }
        if (q.equals(Point.ZERO)) {
            return new Point(p.getX(), p.getY());
        }
        int xP = p.getX();
        int yP = p.getY();
        int xQ = q.getX();
        int yQ = q.getY();
        if (xP == xQ && (yP + yQ) % params.getModulus() == 0) {
            return Point.ZERO;
        }
        int m = (p.equals(q) ?
                 (3 * xP * xP + params.getA()) * EllipticCurveUtil.getReverseModulo(EllipticCurveUtil.mod(2 * yP, params.getModulus()), params.getModulus()) :
                 (yP - yQ) * EllipticCurveUtil.getReverseModulo(EllipticCurveUtil.mod(xP - xQ, params.getModulus()), params.getModulus())
        ) % params.getModulus();
        int xR = EllipticCurveUtil.mod(m * m - xP - xQ, params.getModulus());
        int yR = EllipticCurveUtil.mod((-yP + m * (xP - xR)), params.getModulus());
        return new Point(xR, yR);
    }

    public Point multiply(int number, Point p) {
        if (number == 0) {
            return Point.ZERO;
        }
        if (number == 1) {
            return new Point(p.getX(), p.getY());
        }
        if (number == 2) {
            return addPoints(p, p);
        }
        Point half = multiply(number / 2, p);
        if (number % 2 == 0) {
            return addPoints(half, half);
        }
        return addPoints(p, addPoints(half, half));
    }

    public int getOrder() {
        return points.size();
    }

    public BasePoint generateBasePoint(int subGroupOrder) {
        int order = points.size();
        int h = order / subGroupOrder;
        Random random = new Random();
        Point p;
        Point basePoint;
        do {
          p = points.get(random.nextInt(order));
          basePoint = multiply(h, p);
        } while (basePoint.equals(Point.ZERO) || p.equals(Point.ZERO));
        return new BasePoint(basePoint.getX(), basePoint.getY(), subGroupOrder, this);
    }

}
