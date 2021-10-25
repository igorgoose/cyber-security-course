import by.bsu.kb.schepov.lab3.utils.EllipticCurveUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EllipticCurveUtilTest {

    @Test
    public void testSolveQuadraticModuloComparison() {
        EllipticCurveUtil.setReverseModulo(97);
        int result = EllipticCurveUtil.solveQuadraticModuloComparison(36, 97);
        assertTrue(result == 91 || result == 6);
    }
}