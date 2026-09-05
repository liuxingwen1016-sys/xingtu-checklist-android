package com.xinghan.xingtu

import com.xinghan.xingtu.data.seed.ChecklistTemplates
import com.xinghan.xingtu.domain.model.ChecklistCategory
import com.xinghan.xingtu.domain.model.ChecklistPriority
import com.xinghan.xingtu.domain.model.TripTemplate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ChecklistTemplatesTest {

    private val templates = ChecklistTemplates()

    @Test
    fun `business template has ten items`() {
        assertEquals(10, templates.itemsFor(TripTemplate.BUSINESS).size)
    }

    @Test
    fun `business template contains the demo required items`() {
        val titles = templates.business().map { it.title }
        assertTrue("确认会议资料" in titles)
        assertTrue("携带演示手机" in titles)
        assertTrue("下载离线地图" in titles)
    }

    @Test
    fun `business template uses several categories`() {
        val categories = templates.business().map { it.category }.toSet()
        assertTrue(ChecklistCategory.DOCUMENT in categories)
        assertTrue(ChecklistCategory.DIGITAL in categories)
        assertTrue(ChecklistCategory.CLOTHING in categories)
        assertTrue(ChecklistCategory.WORK in categories)
        assertTrue(ChecklistCategory.OTHER in categories)
    }

    @Test
    fun `business template marks key items important`() {
        val importantTitles = templates.business()
            .filter { it.priority == ChecklistPriority.IMPORTANT }
            .map { it.title }
        assertTrue("确认会议资料" in importantTitles)
        assertTrue("携带演示手机" in importantTitles)
    }

    @Test
    fun `weekend template has eight items`() {
        assertEquals(8, templates.itemsFor(TripTemplate.WEEKEND).size)
    }

    @Test
    fun `custom template has no items`() {
        assertTrue(templates.itemsFor(TripTemplate.CUSTOM).isEmpty())
    }
}
