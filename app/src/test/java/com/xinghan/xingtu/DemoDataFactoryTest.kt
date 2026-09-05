package com.xinghan.xingtu

import com.xinghan.xingtu.data.seed.DemoDataFactory
import com.xinghan.xingtu.domain.model.ChecklistPriority
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DemoDataFactoryTest {

    private val factory = DemoDataFactory()
    private val today = 20_000L
    private val now = 1_700_000_000_000L

    private val seeds = factory.build(today, now)

    @Test
    fun `fixed demo trip ids are stable`() {
        assertEquals(
            listOf(DemoDataFactory.DEMO_TRIP_SHENZHEN_ID, DemoDataFactory.DEMO_TRIP_HANGZHOU_ID),
            seeds.map { it.tripId },
        )
    }

    @Test
    fun `shenzhen trip is d plus 7 to d plus 9`() {
        val shenzhen = seeds.first()
        assertEquals(today + 7, shenzhen.input.startDate)
        assertEquals(today + 9, shenzhen.input.endDate)
        assertEquals("深圳技术交流行程", shenzhen.input.title)
        assertEquals("深圳", shenzhen.input.destination)
    }

    @Test
    fun `shenzhen trip has ten items with six completed`() {
        val shenzhen = seeds.first()
        assertEquals(10, shenzhen.items.size)
        assertEquals(6, shenzhen.items.count { it.isCompleted })
        assertEquals(60, (shenzhen.items.count { it.isCompleted } * 100) / shenzhen.items.size)
    }

    @Test
    fun `shenzhen pending list leads with the three demo items`() {
        val shenzhen = seeds.first()
        val pending = shenzhen.items
            .filter { !it.isCompleted }
            .sortedWith(compareBy({ -it.priority.rawValue }, { it.sortOrder }))
        assertEquals(
            listOf("确认会议资料", "携带演示手机", "下载离线地图"),
            pending.take(3).map { it.title },
        )
    }

    @Test
    fun `completed items carry a completion timestamp`() {
        val shenzhen = seeds.first()
        shenzhen.items.filter { it.isCompleted }.forEach {
            assertTrue(it.completedAt != null)
        }
        shenzhen.items.filter { !it.isCompleted }.forEach {
            assertEquals(null, it.completedAt)
        }
    }

    @Test
    fun `hangzhou trip is d minus 14 to d minus 12 and fully completed`() {
        val hangzhou = seeds[1]
        assertEquals(today - 14, hangzhou.input.startDate)
        assertEquals(today - 12, hangzhou.input.endDate)
        assertEquals("杭州周末漫游", hangzhou.input.title)
        assertEquals(8, hangzhou.items.size)
        assertTrue(hangzhou.items.all { it.isCompleted })
    }

    @Test
    fun `demo item ids are unique and stable`() {
        val ids = seeds.flatMap { it.items.map { item -> item.itemId } }
        assertEquals(ids.size, ids.toSet().size)
        assertTrue("demo-item-sz-1" in ids)
        assertTrue("demo-item-hz-8" in ids)
    }

    @Test
    fun `important priority is preserved in demo seeds`() {
        val shenzhen = seeds.first()
        assertTrue(
            shenzhen.items.any { it.priority == ChecklistPriority.IMPORTANT && !it.isCompleted }
        )
    }
}
