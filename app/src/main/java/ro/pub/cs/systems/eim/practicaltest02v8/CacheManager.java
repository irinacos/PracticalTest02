package ro.pub.cs.systems.eim.practicaltest02v8;

import android.util.Log;

public class CacheManager {

    private static final String TAG = "CacheManager";

    private String cachedData = null;
    private long lastUpdatedTime = 0;

    // 1 minut = 60 ms
    private static final long CACHE_DURATION = 60 * 1000;

    public String getCachedData() {
        if (System.currentTimeMillis() - lastUpdatedTime < CACHE_DURATION) {
            Log.d(TAG, "Se folosesc datele din cache.");
            return cachedData;
        }
        Log.d(TAG, "Cache expirat. Se va face o cerere nouă.");
        return null;
    }

    // Actualizează cache-ul cu date noi
    public void updateCache(String newData) {
        cachedData = newData;
        lastUpdatedTime = System.currentTimeMillis();
        Log.d(TAG, "Cache actualizat cu date noi.");
    }
}