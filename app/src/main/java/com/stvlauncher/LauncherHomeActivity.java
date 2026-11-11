package com.stvlauncher;

import static android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET;
import static android.net.NetworkCapabilities.NET_CAPABILITY_NOT_VPN;
import static android.net.NetworkCapabilities.TRANSPORT_ETHERNET;
import static android.net.NetworkCapabilities.TRANSPORT_VPN;
import static android.net.NetworkCapabilities.TRANSPORT_WIFI;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.provider.Settings;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class LauncherHomeActivity extends android.app.Activity {

    private static final long NETWORK_REFRESH_MS = 60_000L;
    private static final String XIAOMI_SETTINGS_PACKAGE = "com.xiaomi.mitv.settings";
    private static final ComponentName XIAOMI_SETTINGS = new ComponentName(
            XIAOMI_SETTINGS_PACKAGE, XIAOMI_SETTINGS_PACKAGE + ".entry.MainActivity");

    private String vpnPackage;
    private TextView networkStatus;
    private LauncherHomeController home;

    private final android.os.Handler ui = new android.os.Handler(android.os.Looper.getMainLooper());
    private final ConnectivityManager.NetworkCallback defaultNetworkCallback = new NetworkWatcher();
    private final ConnectivityManager.NetworkCallback vpnNetworkCallback = new NetworkWatcher();
    private final android.content.IntentFilter packageChanges = new android.content.IntentFilter();
    private final android.content.BroadcastReceiver packageWatcher = new android.content.BroadcastReceiver() {
        @Override
        public void onReceive(android.content.Context context, Intent intent) {
            if (Intent.ACTION_PACKAGE_REMOVED.equals(intent.getAction())
                    && intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)) return;
            if (home != null) home.reload();
        }
    };
    private final Runnable networkRefresh = new Runnable() {
        @Override
        public void run() {
            paintNetworkStatus();
            ui.postDelayed(this, NETWORK_REFRESH_MS);
        }
    };

    private final class NetworkWatcher extends ConnectivityManager.NetworkCallback {
        @Override
        public void onAvailable(Network network) {
            ui.post(LauncherHomeActivity.this::paintNetworkStatus);
        }

        @Override
        public void onLost(Network network) {
            ui.post(LauncherHomeActivity.this::paintNetworkStatus);
        }
    }

    @Override
    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

        LauncherPrefs prefs = LauncherPrefs.read(this);
        vpnPackage = prefs.vpnPackage;
        networkStatus = findViewById(R.id.networkStatus);
        home = new LauncherHomeController(this, prefs, new LauncherHomeController.Actions() {
            @Override
            public void inputs() {
                launchIntent(Settings.ACTION_SETTINGS);
            }

            @Override
            public void network() {
                launchNetworkTarget();
            }

            @Override
            public void bluetooth() {
                launchIntent(Settings.ACTION_BLUETOOTH_SETTINGS);
            }

            @Override
            public void settings() {
                launchSettings();
            }
        });


        packageChanges.addAction(Intent.ACTION_PACKAGE_ADDED);
        packageChanges.addAction(Intent.ACTION_PACKAGE_REMOVED);
        packageChanges.addAction(Intent.ACTION_PACKAGE_CHANGED);
        packageChanges.addDataScheme("package");

        ConnectivityManager manager = getSystemService(ConnectivityManager.class);
        if (manager != null) {
            manager.registerDefaultNetworkCallback(defaultNetworkCallback);
            manager.registerNetworkCallback(new android.net.NetworkRequest.Builder()
                    .addTransportType(TRANSPORT_VPN)
                    .removeCapability(NET_CAPABILITY_NOT_VPN)
                    .build(), vpnNetworkCallback);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(packageWatcher, packageChanges, RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(packageWatcher, packageChanges);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (home != null) home.onResume();
        paintNetworkStatus();
        ui.postDelayed(networkRefresh, NETWORK_REFRESH_MS);
    }

    @Override
    protected void onPause() {
        super.onPause();
        ui.removeCallbacks(networkRefresh);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(packageWatcher);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ConnectivityManager manager = getSystemService(ConnectivityManager.class);
        if (manager != null) {
            manager.unregisterNetworkCallback(defaultNetworkCallback);
            manager.unregisterNetworkCallback(vpnNetworkCallback);
        }
    }

    private void paintNetworkStatus() {
        if (networkStatus == null) return;
        String text = getString(R.string.tvlauncher_net_wifi);
        boolean reachable = false;
        boolean wifi = false;
        boolean ethernet = false;
        boolean vpn = false;
        ConnectivityManager manager = getSystemService(ConnectivityManager.class);
        if (manager != null) {
            try {
                for (Network network : manager.getAllNetworks()) {
                    NetworkCapabilities caps = manager.getNetworkCapabilities(network);
                    if (caps == null) continue;
                    if (caps.hasTransport(TRANSPORT_VPN)) {
                        vpn = true;
                    } else if (caps.hasCapability(NET_CAPABILITY_INTERNET)) {
                        reachable = true;
                        wifi |= caps.hasTransport(TRANSPORT_WIFI);
                        ethernet |= caps.hasTransport(TRANSPORT_ETHERNET);
                    }
                }
                if (!reachable) {
                    text = getString(R.string.tvlauncher_net_offline);
                } else if (ethernet) {
                    text = getString(R.string.tvlauncher_net_ethernet);
                } else if (wifi) {
                    android.net.wifi.WifiManager radio = (android.net.wifi.WifiManager)
                            getApplicationContext().getSystemService(WIFI_SERVICE);
                    android.net.wifi.WifiInfo connection = radio == null ? null : radio.getConnectionInfo();
                    String ssid = ssidOrNull(connection == null ? null : connection.getSSID());
                    if (ssid != null) text = ssid;
                }
            } catch (Exception ignored) {
                // Show the most useful network state collected so far.
            }
        }

        boolean configuredVpnDown = !vpn && reachable && vpnPackage != null && !vpnPackage.isEmpty();
        if (vpn || configuredVpnDown) {
            text += "  " + getString(vpn ? R.string.tvlauncher_vpn_on : R.string.tvlauncher_vpn_off);
        }
        networkStatus.setText(text);
    }

    static String ssidOrNull(String raw) {
        if (raw == null) return null;
        String clean = raw.replace("\"", "").trim();
        return clean.isEmpty() || clean.contains("unknown") || clean.equals("0x") ? null : clean;
    }

    private void launchNetworkTarget() {
        Intent target = null;
        if (vpnPackage != null && !vpnPackage.isEmpty()) {
            PackageManager manager = getPackageManager();
            target = manager.getLeanbackLaunchIntentForPackage(vpnPackage);
            if (target == null) target = manager.getLaunchIntentForPackage(vpnPackage);
        }
        start(target == null ? settingsIntent(Settings.ACTION_WIFI_SETTINGS) : target);
    }

    private void launchSettings() {
        start(settingsIntent(Settings.ACTION_SETTINGS));
    }
    
    private void launchIntent(String action) {
        start(settingsIntent(action));
    }

    private Intent settingsIntent(String action) {
        PackageManager manager = getPackageManager();
        Intent standard = new Intent(action);
        if (standard.resolveActivity(manager) != null) return standard;
        Intent xiaomi = new Intent().setComponent(XIAOMI_SETTINGS);
        return xiaomi.resolveActivity(manager) != null ? xiaomi : standard;
    }

    private void start(Intent target) {
        target.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            startActivity(target);
        } catch (ActivityNotFoundException missing) {
            Toast.makeText(this, R.string.tvlauncher_unavailable, Toast.LENGTH_SHORT).show();
        }
    }
}