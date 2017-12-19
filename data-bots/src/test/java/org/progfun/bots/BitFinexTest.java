package org.progfun.bots;

import org.junit.Test;
import org.progfun.InvalidFormatException;
import static org.junit.Assert.*;
import org.progfun.bots.bitfinex.BitFinexHandler;

public class BitFinexTest {

    // Test a realistic scenario with predefined messages stored in a file
    @Test
    public void testScenario() throws InvalidFormatException {
        // Load test scenario
        TestScenario scenario = TestScenario.loadFromFile(
                "/bitfinex/bitfinex-test-messages.test");
        assertNotNull(scenario);
        scenario.runTest(new BitFinexHandler());
    }
}
