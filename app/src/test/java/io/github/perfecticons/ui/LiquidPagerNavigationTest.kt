package io.github.perfecticons.ui

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import org.junit.Assert.*
import org.junit.Test

class LiquidPagerNavigationTest {
    @Test fun `intermediate page cannot overwrite requested tab during animation`() = runBlocking {
        coroutineScope {
            var current = 0
            val finish = CompletableDeferred<Unit>()
            val navigation = LiquidPagerNavigation({ current }, { 3 }, { target -> finish.await(); current = target }, this)
            navigation.animateToPage(2)
            yield()
            current = 1
            navigation.syncPage()
            assertEquals(2, navigation.selectedPage)
            assertTrue(navigation.isNavigating)
            finish.complete(Unit)
            repeat(4) { yield() }
            assertFalse(navigation.isNavigating)
            assertEquals(2, current)
            current = 1
            navigation.syncPage()
            assertEquals(1, navigation.selectedPage)
        }
    }

    @Test fun `cancelled navigation cannot clear ownership of rapid reverse request`() = runBlocking {
        coroutineScope {
            var current = 0
            val finish = List(3) { CompletableDeferred<Unit>() }
            val navigation = LiquidPagerNavigation({ current }, { 3 }, { target -> finish[target].await(); current = target }, this)
            navigation.animateToPage(2)
            yield()
            navigation.animateToPage(0)
            repeat(4) { yield() }
            assertEquals(0, navigation.selectedPage)
            assertTrue(navigation.isNavigating)
            finish[0].complete(Unit)
            repeat(4) { yield() }
            assertFalse(navigation.isNavigating)
            assertEquals(0, current)
        }
    }
}
