package by.bsu.kb.schepov.lab3.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EncryptionUtilTest {

    @Test
    public void test() {
        GroupParams groupParams = new GroupParams(43, 22, 71);
        Group group = EllipticCurveUtil.findGroupStupid(groupParams);
        BasePoint basePoint = new BasePoint(4, 51, 17, group);
        int nA = 1;
    }

}