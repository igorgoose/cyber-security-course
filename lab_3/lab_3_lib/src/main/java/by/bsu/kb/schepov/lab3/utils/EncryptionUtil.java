package by.bsu.kb.schepov.lab3.utils;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;

import java.security.SecureRandom;

@Slf4j
@UtilityClass
public class EncryptionUtil {

    public static Signature sign(String message, BasePoint basePoint, int privateKey) {
        log.info("Signing message '{}'", message);
        int hash = DigestUtils.sha1Hex(message).hashCode();
        SecureRandom random = new SecureRandom();
        int k, r, s;
        do {
            do {
                k = 1 + random.nextInt(basePoint.getOrder() - 1);
                Point kG = basePoint.getGroup().multiply(k, basePoint);
                r = kG.getX() % basePoint.getOrder();
            } while (r == 0);
            int inverseK = EllipticCurveUtil.getReverseModulo(k, basePoint.getOrder());
            s = (inverseK * (EllipticCurveUtil.mod(hash, basePoint.getOrder()) + r * privateKey)) % basePoint.getOrder();
        } while (s == 0);
        Signature signature = new Signature(r, s);
        log.info("Signature for message '{}' is {}", message, signature.toString());
        return signature;
    }

    public static boolean verify(String message, Signature signature, BasePoint basePoint, Point publicKey) {
        log.info("Verifying signature {} for message '{}'", signature.toString(), message);
        int hash = DigestUtils.sha1Hex(message).hashCode();
        int r = signature.getR();
        int s = signature.getS();
        if (r < 1 || r > basePoint.getOrder() - 1 || s < 1 || s > basePoint.getOrder() - 1) {
            return false;
        }
        int w = EllipticCurveUtil.getReverseModulo(s, basePoint.getOrder());
        int u1 = (EllipticCurveUtil.mod(hash, basePoint.getOrder()) * w) % basePoint.getOrder();
        int u2 = (r * w) % basePoint.getOrder();
        Group group = basePoint.getGroup();
        Point p = group.addPoints(group.multiply(u1, basePoint), group.multiply(u2, publicKey));
        boolean result = r == p.getX() % basePoint.getOrder();
        log.info("Computed x* value is {}. Verification result is: {}", p.getX(), result);
        return result;
    }
}
