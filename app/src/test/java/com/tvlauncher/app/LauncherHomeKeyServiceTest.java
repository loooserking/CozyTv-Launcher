package com.tvlauncher.app;

import android.view.KeyEvent;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LauncherHomeKeyServiceTest {

    @Test
    public void systemNavigationAndMediaKeysPassThrough() {
        int[] noCustomScans = new int[0];
        int[] noCustomCodes = new int[0];
        assertEquals(LauncherHomeKeyService.PASS, decide(KeyEvent.KEYCODE_HOME, 0, noCustomScans, noCustomCodes));
        assertEquals(LauncherHomeKeyService.PASS, decide(KeyEvent.KEYCODE_BACK, 0, noCustomScans, noCustomCodes));
        assertEquals(LauncherHomeKeyService.PASS, decide(KeyEvent.KEYCODE_DPAD_LEFT, 0, noCustomScans, noCustomCodes));
        assertEquals(LauncherHomeKeyService.PASS, decide(KeyEvent.KEYCODE_DPAD_CENTER, 0, noCustomScans, noCustomCodes));
        assertEquals(LauncherHomeKeyService.PASS, decide(KeyEvent.KEYCODE_VOLUME_UP, 0, noCustomScans, noCustomCodes));
        assertEquals(LauncherHomeKeyService.PASS, decide(KeyEvent.KEYCODE_BUTTON_2, 150, noCustomScans, noCustomCodes));
        assertEquals(LauncherHomeKeyService.PASS, decide(KeyEvent.KEYCODE_APP_SWITCH, 580, noCustomScans, noCustomCodes));
    }

    @Test
    public void customSettingsMappingsCannotConsumeSystemKeys() {
        int[] scans = {150, 580};
        int[] codes = {
                KeyEvent.KEYCODE_HOME, KeyEvent.KEYCODE_BACK, KeyEvent.KEYCODE_DPAD_RIGHT,
                KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_VOLUME_DOWN, KeyEvent.KEYCODE_APP_SWITCH
        };
        assertEquals(LauncherHomeKeyService.PASS, decide(KeyEvent.KEYCODE_HOME, 0, scans, codes));
        assertEquals(LauncherHomeKeyService.PASS, decide(KeyEvent.KEYCODE_BACK, 0, scans, codes));
        assertEquals(LauncherHomeKeyService.PASS, decide(KeyEvent.KEYCODE_DPAD_RIGHT, 0, scans, codes));
        assertEquals(LauncherHomeKeyService.PASS, decide(KeyEvent.KEYCODE_VOLUME_DOWN, 0, scans, codes));
        assertEquals(LauncherHomeKeyService.PASS, decide(KeyEvent.KEYCODE_UNKNOWN, 150, scans, codes));
    }

    @Test
    public void configuredSettingsKeyStillRoutesToSettings() {
        assertEquals(LauncherHomeKeyService.GO_SETTINGS,
                LauncherHomeKeyService.decide(KeyEvent.KEYCODE_SETTINGS, 0, true, false,
                        new int[0], new int[0]));
    }

    private static int decide(int keyCode, int scanCode, int[] scans, int[] codes) {
        return LauncherHomeKeyService.decide(keyCode, scanCode, true, false, scans, codes);
    }
}