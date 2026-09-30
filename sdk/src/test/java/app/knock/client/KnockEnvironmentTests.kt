package app.knock.client

import org.junit.Assert.assertEquals
import org.junit.Test

class KnockEnvironmentTests {
    @Test
    fun clientVersionMatchesThePublishedVersion() {
        assertEquals(System.getProperty("knock.sdkVersion"), KnockEnvironment.clientVersion)
    }
}
