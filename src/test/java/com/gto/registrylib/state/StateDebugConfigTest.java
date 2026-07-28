package com.gto.registrylib.state;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StateDebugConfigTest {

    @Test
    void disabledDefaultsAreReadOnly() {
        StateDebugConfig config = StateDebugConfig.disabled();

        assertFalse(config.enabled());
        assertFalse(config.writable());
        assertEquals(2, config.readPermission());
        assertEquals(4, config.writePermission());
    }

    @Test
    void configureReturnsMutatedConfig() {
        StateDebugConfig config = StateDebugConfig.createEnabled();

        StateDebugConfig returned = config.configure(value -> value.writable(true).readPermission(1).writePermission(3));

        assertSame(config, returned);
        assertTrue(config.enabled());
        assertTrue(config.writable());
        assertEquals(1, config.readPermission());
        assertEquals(3, config.writePermission());
    }
}
