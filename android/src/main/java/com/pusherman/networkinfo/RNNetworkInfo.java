package com.pusherman.networkinfo;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteOrder;

public class RNNetworkInfo extends ReactContextBaseJavaModule {
    public static final String TAG = "RNNetworkInfo";

    WifiManager mWifiManager;
    private final ConnectivityManager mConnManager;

    public RNNetworkInfo(ReactApplicationContext reactContext) {
        super(reactContext);
        Context context = reactContext.getApplicationContext();
        mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        mConnManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    @Override
    public String getName() {
        return TAG;
    }

    @ReactMethod
    public void getSSID(final Promise promise) {
        if (isConnectedToWifi()) {
            WifiInfo info = mWifiManager.getConnectionInfo();

            // This value should be wrapped in double quotes, so we need to unwrap it.
            String ssid = info.getSSID();
            if (ssid.startsWith("\"") && ssid.endsWith("\"")) {
                ssid = ssid.substring(1, ssid.length() - 1);
            }
            promise.resolve(ssid);
        } else {
            promise.resolve(null);
        }
    }

    private boolean isConnectedToWifi() {
        NetworkInfo mWifi = mConnManager.getActiveNetworkInfo();
        if (mWifi != null) {
            return mWifi.getType() == ConnectivityManager.TYPE_WIFI && mWifi.isConnected();
        } else {
            return false;
        }
    }

    @ReactMethod
    public void getBSSID(final Promise promise) {
        if (isConnectedToWifi()) {
            WifiInfo info = mWifiManager.getConnectionInfo();
            promise.resolve(info.getBSSID());
        } else {
            promise.resolve(null);
        }
    }

    @ReactMethod
    public void getSignalLevel(int totalLevel, final Promise promise) {
        WifiInfo info = mWifiManager.getConnectionInfo();
        WifiManager.calculateSignalLevel(info.getRssi(), totalLevel);
        promise.resolve(info.getRssi());
    }

    @ReactMethod
    public void getIPAddress(final Promise promise) {
        WifiInfo info = mWifiManager.getConnectionInfo();

        // The following is courtesy of Digital Rounin at
        //   http://stackoverflow.com/a/18638588 .

        // The endian-ness of `ip` is potentially varying, but we need it to be big-
        // endian.
        int ip = info.getIpAddress();

        // Convert little-endian to big-endian if needed.
        if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
            ip = Integer.reverseBytes(ip);
        }

        // Now that the value is guaranteed to be big-endian, we can convert it to
        // an array whose first element is the high byte.
        byte[] ipByteArray = BigInteger.valueOf(ip).toByteArray();

        String ipAddressString;
        try {
            // `getByAddress()` wants network byte-order, aka big-endian.
            // Good thing we planned ahead!
            ipAddressString = InetAddress.getByAddress(ipByteArray).getHostAddress();
        } catch (UnknownHostException ex) {
            Log.e(TAG, "Unable to determine IP address.");
            ipAddressString = null;
        }

        promise.resolve(ipAddressString);
    }
}
