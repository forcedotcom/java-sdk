package com.force.sdk.connector;

/**
 * ForceServiceConnectorCacheTestUtil
 *
 * @author naamannewbold
 */
public class ForceServiceConnectorCacheTestUtil {
    public static void clearForceServiceConnectorCache() {
        ForceServiceConnector.clearCache();
    }

    public static int forceServiceConnectorCachedConfigCount() {
        return ForceServiceConnector.getCachedConfigs().size();
    }
}
