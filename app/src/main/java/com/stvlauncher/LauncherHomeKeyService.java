package com.stvlauncher;

import android.content.Intent;
import android.view.KeyEvent;

/**
 * TVLauncher remote-key bridge for buttons that only the stock launcher used to answer.
 *
 * After the stock home is disabled some buttons go dead: Xiaomi remotes report Home as KEYCODE_ALL_APPS,
 * and the Settings button is handled by the stock home alone. This service watches key events and
 * redirects only these:
 * <ul>
 *   <li>Settings (KEYCODE_SETTINGS, or any code listed in tvlauncher_config.xml / config.json) to system Settings,</li>
 *   <li>KEYCODE_ALL_APPS to this launcher.</li>
 * </ul>
 * Everything else passes through. Enable it over adb with
 * {@code settings put secure enabled_accessibility_services com.tvlauncher.app/.LauncherHomeKeyService}
 * and {@code settings put secure accessibility_enabled 1}.
 */
public class LauncherHomeKeyService extends android.accessibilityservice.AccessibilityService {

    /** Results of {@link #decide}. */
    static final int PASS = 0;
    static final int EAT = 1;
    static final int GO_SETTINGS = 2;
    static final int GO_HOME = 3;

    private int[] settingsScans = new int[0];
    private int[] settingsCodes = new int[0];
    /** Loaded together with the other settings-key knobs; not acted on yet. */
    private boolean settingsOpensPanel;

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        settingsScans = getResources().getIntArray(R.array.tvlauncher_settings_scancodes);
        // config.json can replace these; theme.sh restarts the service after editing it.
        LauncherPrefs prefs = LauncherPrefs.read(this);
        settingsCodes = prefs.extraSettingsKeys;
        settingsOpensPanel = prefs.settingsKeyOpensPanel;
    }

    private static boolean contains(int[] haystack, int needle) {
        for (int i = 0; i < haystack.length; i++) {
            if (haystack[i] == needle) return true;
        }
        return false;
    }

    /**
     * Decision for a single key event. Keys this service owns are eaten on both edges; the redirect runs
     * only on a release that was not cancelled.
     */
    static int decide(int code, int scan, boolean released, boolean cancelled, int[] scans, int[] codes) {
        if (passesToSystem(code, scan)) return PASS;
        final int action;
        if (code == KeyEvent.KEYCODE_SETTINGS || contains(scans, scan) || contains(codes, code)) {
            action = GO_SETTINGS;
        } else if (code == KeyEvent.KEYCODE_ALL_APPS) {
            action = GO_HOME;
        } else {
            return PASS;
        }
        return (released && !cancelled) ? action : EAT;
    }

    private static boolean passesToSystem(int code, int scan) {
        return code == KeyEvent.KEYCODE_HOME
                || code == KeyEvent.KEYCODE_BACK
                || code == KeyEvent.KEYCODE_DPAD_UP
                || code == KeyEvent.KEYCODE_DPAD_DOWN
                || code == KeyEvent.KEYCODE_DPAD_LEFT
                || code == KeyEvent.KEYCODE_DPAD_RIGHT
                || code == KeyEvent.KEYCODE_DPAD_CENTER
                || code == KeyEvent.KEYCODE_ENTER
                || code == KeyEvent.KEYCODE_NUMPAD_ENTER
                || code == KeyEvent.KEYCODE_VOLUME_UP
                || code == KeyEvent.KEYCODE_VOLUME_DOWN
                || code == KeyEvent.KEYCODE_VOLUME_MUTE
                || code == KeyEvent.KEYCODE_BUTTON_2
                || code == KeyEvent.KEYCODE_APP_SWITCH
                || scan == 150
                || scan == 580;
    }

    @Override
    protected boolean onKeyEvent(KeyEvent key) {
        final int result = decide(key.getKeyCode(), key.getScanCode(), key.getAction() == KeyEvent.ACTION_UP,
                key.isCanceled(), settingsScans, settingsCodes);
        if (result == PASS) return false;
        Intent next = null;
        if (result == GO_SETTINGS) {
            next = new Intent(android.provider.Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        } else if (result == GO_HOME) {
            next = new Intent(Intent.ACTION_MAIN)
                    .addCategory(Intent.CATEGORY_HOME)
                    .setPackage(getPackageName())
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        }
        if (next != null) startActivity(next);
        return true;
    }

    @Override
    public void onAccessibilityEvent(android.view.accessibility.AccessibilityEvent ignored) {
        // Only key events are used.
    }

    @Override
    public void onInterrupt() {
        // Nothing running to stop.
    }
}
