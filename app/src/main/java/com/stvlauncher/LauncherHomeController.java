package com.stvlauncher;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

final class LauncherHomeController {

    interface Actions {
        void inputs();
        void network();
        void bluetooth();
        void settings();
    }

    private static String lastFocusPackage;

    private final Activity activity;
    private final LauncherPrefs prefs;
    private final Actions actions;
    private final Set<String> hidden;
    private final RecyclerView appList;
    private final LinearLayoutManager layoutManager;
    private final AppTileAdapter adapter;
    private final TextView emptyState;
    private final View inputButton;
    private final View networkButton;
    private final View bluetoothButton;
    private final View settingsButton;

    LauncherHomeController(Activity activity, LauncherPrefs prefs, Actions actions) {
        this.activity = activity;
        this.prefs = prefs;
        this.actions = actions;
        hidden = new HashSet<>(prefs.hiddenPackages);

        appList = activity.findViewById(R.id.appsRecyclerView);
        emptyState = activity.findViewById(R.id.emptyApps);
        inputButton = activity.findViewById(R.id.inputButton);
        networkButton = activity.findViewById(R.id.networkStatus);
        bluetoothButton = activity.findViewById(R.id.bluetoothButton);
        settingsButton = activity.findViewById(R.id.settingsButton);
        FlipClockView clock = activity.findViewById(R.id.flipClock);
        clock.setAnimations(prefs.motionOn && !prefs.motionGentle);
        layoutManager = new LinearLayoutManager(activity, RecyclerView.HORIZONTAL, false);
        adapter = new AppTileAdapter(activity, this::launchApp, this::showAppDetails,
                item -> lastFocusPackage = item.packageName, this::focusNetwork);

        appList.setLayoutManager(layoutManager);
        appList.setAdapter(adapter);
        appList.setItemAnimator(null);
        appList.setHasFixedSize(true);
        inputButton.setOnClickListener(view -> actions.inputs());
        networkButton.setOnClickListener(view -> actions.network());
        bluetoothButton.setOnClickListener(view -> actions.bluetooth());
        settingsButton.setOnClickListener(view -> actions.settings());

        View[] headerButtons = {inputButton, networkButton, bluetoothButton, settingsButton};
        View.OnFocusChangeListener headerFocus = (view, focused) -> {
            ((TextView) view).setTextColor(activity.getColor(
                focused ? R.color.launcher_text_primary : R.color.launcher_text_secondary));
            view.animate().scaleX(focused ? 1.04f : 1f).scaleY(focused ? 1.04f : 1f)
                .setDuration(140).start();
        };

        for (View b : headerButtons) {
            b.setOnFocusChangeListener(headerFocus);
            // Simple key navigation between buttons
        }
    }

    void onResume() {
        reload();
    }

    void reload() {
        boolean restoreRowFocus = activity.getCurrentFocus() == null || appList.hasFocus();
        List<AppTileAdapter.AppItem> apps = loadApps();
        adapter.setItems(apps);
        emptyState.setVisibility(apps.isEmpty() ? View.VISIBLE : View.GONE);
        if (restoreRowFocus) focusRememberedApp();
    }

    private void focusNetwork() {
        networkButton.requestFocus();
    }

    private boolean onHeaderKey(int keyCode, KeyEvent event, boolean first) {
        if (event.getAction() != KeyEvent.ACTION_DOWN) return false;
        if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
            focusRememberedApp();
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_DPAD_UP) return true;
        if (first && keyCode == KeyEvent.KEYCODE_DPAD_LEFT) return true;
        return !first && keyCode == KeyEvent.KEYCODE_DPAD_RIGHT;
    }

    private void focusRememberedApp() {
        if (adapter.getItemCount() == 0) {
            settingsButton.requestFocus();
            return;
        }
        int position = 0;
        if (lastFocusPackage != null) {
            for (int i = 0; i < adapter.getItemCount(); i++) {
                if (lastFocusPackage.equals(adapter.getItem(i).packageName)) {
                    position = i;
                    break;
                }
            }
        }
        int target = position;
        layoutManager.scrollToPositionWithOffset(target, 0);
        appList.post(() -> {
            RecyclerView.ViewHolder holder = appList.findViewHolderForAdapterPosition(target);
            if (holder != null) holder.itemView.requestFocus();
        });
    }

    private List<AppTileAdapter.AppItem> loadApps() {
        PackageManager manager = activity.getPackageManager();
        Intent query = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER);
        List<ResolveInfo> results = manager.queryIntentActivities(query, 0);
        List<AppTileAdapter.AppItem> apps = new ArrayList<>();
        Set<String> seenPackages = new HashSet<>();
        for (ResolveInfo result : results) {
            if (result.activityInfo == null || result.activityInfo.applicationInfo == null) continue;
            String packageName = result.activityInfo.packageName;
            if (packageName.equals(activity.getPackageName()) || hidden.contains(packageName)
                    || !seenPackages.add(packageName)) continue;

            ComponentName component = new ComponentName(packageName, result.activityInfo.name);
            CharSequence actualLabel = result.loadLabel(manager);
                String label = actualLabel == null || actualLabel.length() == 0
                    ? packageName : actualLabel.toString();
            Drawable icon = result.loadIcon(manager);
            apps.add(new AppTileAdapter.AppItem(packageName, label, component, icon));
        }

        List<String> favorites = prefs.dockPackages;
        Collections.sort(apps, (left, right) -> {
            int leftFavorite = favorites.indexOf(left.packageName);
            int rightFavorite = favorites.indexOf(right.packageName);
            if (leftFavorite >= 0 || rightFavorite >= 0) {
                if (leftFavorite < 0) return 1;
                if (rightFavorite < 0) return -1;
                return Integer.compare(leftFavorite, rightFavorite);
            }
            return left.label.compareToIgnoreCase(right.label);
        });
        return apps;
    }

    private void launchApp(AppTileAdapter.AppItem item) {
        Intent launch = new Intent(Intent.ACTION_MAIN)
                .addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)
                .setComponent(item.component)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        try {
            activity.startActivity(launch);
        } catch (Exception failure) {
            Toast.makeText(activity, activity.getString(R.string.tvlauncher_cant_open, item.label), Toast.LENGTH_SHORT).show();
        }
    }

    private void showAppDetails(AppTileAdapter.AppItem item) {
        Intent details = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.parse("package:" + item.packageName)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            activity.startActivity(details);
        } catch (Exception ignored) {
            // Some TV builds do not provide an app-details screen.
        }
    }
}