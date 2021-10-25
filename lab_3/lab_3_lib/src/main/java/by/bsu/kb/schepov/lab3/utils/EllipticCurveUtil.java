package by.bsu.kb.schepov.lab3.utils;

import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@UtilityClass
public class EllipticCurveUtil {

    private static final ThreadLocal<int[]> reverseModulo = new ThreadLocal<>();

    public static GroupParams findValidEllipticGroupParams(int modulus) {
        Random random = new Random();
        while (true) {
            int a = 1 + random.nextInt(modulus - 1);
            int b = 1 + random.nextInt(modulus - 1);
            if ((4 * a * a * a + 27 * b * b) % modulus != 0) {
                return new GroupParams(a, b, modulus);
            }
        }
    }

    public static Group findGroup(GroupParams params) {
        List<Point> groupPoints = new ArrayList<>();
        groupPoints.add(Point.ZERO);
        int a = params.getA();
        int b = params.getB();
        int modulus = params.getModulus();
        setReverseModulo(params.getModulus());
        for (int x = 0; x < modulus; x++) {
            int residue = (x * x * x + a * x + b) % modulus;
            if (!isQuadraticResidue(residue, modulus)) {
                continue;
            }
            int y = solveQuadraticModuloComparison(residue, modulus);
            groupPoints.add(new Point(x, y));
            groupPoints.add(new Point(x, modulus - y));
        }
        return new Group().setParams(params).setPoints(groupPoints);
    }

    public static Group findGroupStupid(GroupParams params) {
        List<Point> groupPoints = new ArrayList<>();
        groupPoints.add(Point.ZERO);
        int a = params.getA();
        int b = params.getB();
        int modulus = params.getModulus();
        setReverseModulo(params.getModulus());
        for (int x = 0; x < modulus; x++) {
            for (int y = 0; y < modulus; y++) {
                if ((y * y) % modulus == (x * x * x + a * x + b) % modulus) {
                    groupPoints.add(new Point(x, y));
                }
            }
        }
        return new Group().setParams(params).setPoints(groupPoints);
    }

    public static int solveQuadraticModuloComparison(int residue, int modulus) {
        Random random = new Random();
        int b;
        do {
            b = random.nextInt(modulus);
        } while (isQuadraticResidue(b, modulus));
        int s = maxPowerOf2(modulus - 1);
        int t = (modulus - 1) / pow(2, s);
        setReverseModulo(97);
        int reverseResidue = reverseModulo.get()[residue];
        int c = powModulo(b, t, modulus);
        int r = powModulo(residue, (t + 1) / 2, modulus);
        for (int i = 1; i < s; i++) {
            int e = powModulo(2, s - i - 1, modulus);
            int d = powModulo((((r * r) % modulus) * reverseResidue) % modulus, e, modulus);
            if (d == modulus - 1) {
                r = (r * c) % modulus;
            }
            c = (c * c) % modulus;
        }
        return r;
    }

    public static int mod(int number, int modulus) {
        return number >= 0 ? number % modulus : modulus + (number % modulus);
    }

    private static int pow(int number, int power) {
        if (power == 0) {
            return 1;
        }
        int result = number;
        for (int i = 1; i < power; i++) {
            result *= number;
        }
        return result;
    }

    private static boolean isQuadraticResidue(int potentialResidue, int modulus) {
        return powModulo(potentialResidue, (modulus - 1) / 2, modulus) == 1;
    }

    private static int maxPowerOf2(int number) {
        int i = 0;
        while (number % 2 == 0) {
            i++;
            number /= 2;
        }
        return i;
    }

    private static int[] reverseModulo(int modulus) {
        int[] result = new int[modulus];
        result[1] = 1;
        for (int i = 2; i < modulus; ++i) {
            result[i] = (modulus - (modulus / i) * result[modulus % i] % modulus) % modulus;
        }
        return result;
    }

    private static int powModulo(int number, int power, int modulus) {
        if (power == 0) {
            return 1;
        }
        int z = powModulo(number, power / 2, modulus);
        if (power % 2 == 0)
            return (z * z) % modulus;
        else
            return (number * z * z) % modulus;
    }

    public static void setReverseModulo(int modulus) {
        if (reverseModulo.get() != null && reverseModulo.get().length == modulus) {
            return;
        }
        reverseModulo.set(reverseModulo(modulus));
    }

    public static int getReverseModulo(int number, int modulus) {
        setReverseModulo(modulus);
        return reverseModulo.get()[number];
    }

    public static int findLargestPrimeDivider(int number) {
        for (int i = number; i > 1; i--) {
            if (number % i == 0 && isPrime(i)) {
                return i;
            }
        }
        return 1;
    }

    public static boolean isPrime(int number) {
        for (int i = 2; i < number / 2; i++) {
            if (i * i > number) {
                return true;
            }
            if (number % i == 0) {
                return false;
            }
        }
        return true;
    }
}
