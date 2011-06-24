package com.force.sdk.connector;

/**
 * ForceServiceConnectorCacheTestUtil
 *
 * @author Naaman Newbold
 */
public class ForceServiceConnectorCacheTestUtil {

    private ForceServiceConnectorCacheTestUtil() {}

    /**
     * Test utility method that clears ForceServiceConnector cache.
     * This is only intended for testing purposes.
     */
    public static void clearForceServiceConnectorCache() {
        ForceServiceConnector.clearCache();
    }

    /**
     * Test utility method that gets the size of ForceServiceConnector
     * Configurations.
     * @return Size of the cached configurations in the
     * ForceServiceConnector.
     */
    public static int forceServiceConnectorCachedConfigCount() {
        return ForceServiceConnector.getCachedConfigs().size();
    }
}
