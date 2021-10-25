package by.bsu.kb.schepov.lab3.utils;

import lombok.Getter;
import lombok.ToString;

import java.security.SecureRandom;


@Getter
@ToString
public class EncryptionConfig {
    private final BasePoint basePoint;
    private final int privateKey;
    private final Point publicKey;
    private Point commonPrivateKey;

    public EncryptionConfig(int modulus) {
        Group group;
        do {
            GroupParams groupParams = EllipticCurveUtil.findValidEllipticGroupParams(modulus);
            group = EllipticCurveUtil.findGroupStupid(groupParams);
        } while (EllipticCurveUtil.isPrime(group.getOrder()));
        SecureRandom random = new SecureRandom();
        this.basePoint = group.generateBasePoint(EllipticCurveUtil.findLargestPrimeDivider(group.getOrder()));
        int nA;
        do {
            nA = 1 + random.nextInt(modulus - 1);
        } while (nA % basePoint.getOrder() == 0);
        privateKey = nA;
        publicKey = group.multiply(nA, basePoint);
    }

    public EncryptionConfig(Group group) {
        SecureRandom random = new SecureRandom();
        this.basePoint = group.generateBasePoint(EllipticCurveUtil.findLargestPrimeDivider(group.getOrder()));
        int nA;
        do {
            nA = 1 + random.nextInt(group.getParams().getModulus() - 1);
        } while (nA % basePoint.getOrder() == 0);
        privateKey = nA;
        publicKey = group.multiply(nA, basePoint);
    }

    public EncryptionConfig(BasePoint basePoint) {
        SecureRandom random = new SecureRandom();
        this.basePoint = basePoint;
        int nA;
        do {
            nA = 1 + random.nextInt(basePoint.getGroup().getParams().getModulus() - 1);
        } while (nA % basePoint.getOrder() == 0);
        privateKey = nA;
        publicKey = basePoint.getGroup().multiply(nA, basePoint);
    }

    public Point computeCommonPrivateKey(Point otherPublicKey) {
        this.commonPrivateKey = basePoint.getGroup().multiply(privateKey, otherPublicKey);
        return commonPrivateKey;
    }
}
