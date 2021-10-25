package by.bsu.kb.schepov.lab3.utils;

import java.util.Random;

public class Main {

    private static final int M = 71;

    public static void main(String[] args) {
        GroupParams groupParams = EllipticCurveUtil.findValidEllipticGroupParams(M);
        Group group;
        do {
            group = EllipticCurveUtil.findGroupStupid(new GroupParams(43, 22, 71));
        } while(EllipticCurveUtil.isPrime(group.getOrder()));
        System.out.println(group.getPoints());
        System.out.println(group.getPoints().size());
        Random random = new Random();
        BasePoint basePoint = group.generateBasePoint(EllipticCurveUtil.findLargestPrimeDivider(group.getOrder()));
        System.out.println(basePoint);
        int nA = 1 + random.nextInt(M - 1); // !% group order
        System.out.println(nA);
        Point pA = group.multiply(nA, basePoint);
        System.out.println(pA);
        int nB = 1 + random.nextInt(M - 1);
        System.out.println(nB);
        Point pB = group.multiply(nB, basePoint);
        System.out.println(pB);
        Point kA = group.multiply(nA, pB);
        Point kB = group.multiply(nB, pA);
        System.out.println(kA);
        System.out.println(kB);
        Signature signature = EncryptionUtil.sign("hello", basePoint, nA);
        System.out.println(signature);
        System.out.println(EncryptionUtil.verify("hello", signature, basePoint, pA));
    }
}
