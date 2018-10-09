package io.rybalkinsd.kotlinbootcamp

import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Test

class DataAnalysisTest {

    @Test
    fun `check avg age`() {
        assertTrue(avgAge.isNotEmpty())
    }

    @Test
    fun `some more tests`() {
        assertEquals(8, fromAllSources.size)
        assertEquals(7, unique.size)
        assertEquals(40.0, avgAge[DataSource.FACEBOOK])
    }

    @Test
    fun `check grouped profiles`() {
        assertTrue(groupedProfiles.isNotEmpty())
    }
}
