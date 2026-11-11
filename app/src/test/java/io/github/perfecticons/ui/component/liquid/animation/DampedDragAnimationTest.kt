package io.github.perfecticons.ui.component.liquid.animation

import androidx.compose.runtime.MonotonicFrameClock
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import org.junit.Assert.*
import org.junit.Test

class DampedDragAnimationTest {
    private class Frames : MonotonicFrameClock {
        private val times = Channel<Long>(Channel.UNLIMITED)
        private var time = 0L
        override suspend fun <R> withFrameNanos(onFrame: (Long) -> R): R = onFrame(times.receive())
        suspend fun tick() { time += 16_666_667L; times.send(time); repeat(4) { yield() } }
    }
    @Test fun `retarget through intermediate tabs preserves continuous position`() = runBlocking {
        val frames = Frames()
        withContext(frames) {
            coroutineScope {
                val motion = DampedDragAnimation(this, 0f, 0f..4f, 0.001f, 1f, 1.2f,
                    onDragStarted = {}, onDragStopped = {}, onDrag = { _, _ -> })
                val samples = mutableListOf(0f)
                for (target in listOf(0.4f, 0.8f, 1.2f, 1.6f, 2f, 2.4f, 2.8f, 3.2f)) {
                    val before = motion.value
                    motion.updateValue(target)
                    assertEquals(target, motion.targetValue, 0.0001f)
                    assertEquals("No snap when a pointer target changes", before, motion.value, 0.0001f)
                    repeat(5) { frames.tick(); samples += motion.value }
                }
                // Release snaps the target to a tab, not the current animated position.
                val beforeRelease = motion.value
                motion.updateValue(3f)
                assertEquals(beforeRelease, motion.value, 0.0001f)
                repeat(90) { frames.tick() }
                assertEquals(3f, motion.value, 0.001f)
                assertTrue(samples.any { it in 0.9f..1.1f })
                assertTrue(samples.any { it in 1.9f..2.1f })
                assertTrue(samples.zipWithNext().all { (a, b) -> b >= a && b - a < 0.3f })
            }
        }
    }
    @Test fun `new target retains momentum instead of restarting from rest`() = runBlocking {
        val frames = Frames()
        withContext(frames) {
            coroutineScope {
                val motion = DampedDragAnimation(this, 0f, 0f..4f, 0.001f, 1f, 1.2f,
                    onDragStarted = {}, onDragStopped = {}, onDrag = { _, _ -> })
                motion.updateValue(2f)
                repeat(6) { frames.tick() }
                val previous = motion.value
                frames.tick()
                val current = motion.value
                val oldStep = current - previous
                motion.updateValue(2.2f)
                frames.tick()
                val nextStep = motion.value - current
                assertTrue("Retarget must keep moving: old=$oldStep next=$nextStep previous=$previous current=$current", oldStep > 0f && nextStep > oldStep * 0.4f)
                repeat(90) { frames.tick() }
                assertEquals(2.2f, motion.value, 0.001f)
            }
        }
    }
}
