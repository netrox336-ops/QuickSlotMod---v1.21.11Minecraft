package dev.netrox.quickslot;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public final class QuickSlotConfigInitializationTest {
    @Test
    public void staticInitializationCompletesWithoutException() throws Exception {
        Class<?> configClass = Class.forName(
            "dev.netrox.quickslot.QuickSlotConfig",
            true,
            QuickSlotConfigInitializationTest.class.getClassLoader()
        );

        assertNotNull(configClass);
    }
}
