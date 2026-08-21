package dev.netrox.quickslot;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public final class ProfileContextKeyTest {
    @Test
    public void serverAddressIsNormalized() {
        assertEquals("server:play.example.net:25565", ProfileContextKey.server("  PLAY.Example.Net:25565  "));
    }

    @Test
    public void blankServerAddressIsIgnored() {
        assertNull(ProfileContextKey.server("   "));
        assertNull(ProfileContextKey.server(null));
    }

    @Test
    public void singleplayerWorldKeepsReadableName() {
        assertEquals("singleplayer:BedWars Practice", ProfileContextKey.singleplayer(" BedWars Practice "));
        assertEquals("singleplayer", ProfileContextKey.singleplayer("  "));
    }
}
