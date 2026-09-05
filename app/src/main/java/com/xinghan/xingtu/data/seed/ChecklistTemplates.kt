package com.xinghan.xingtu.data.seed

import com.xinghan.xingtu.domain.model.ChecklistCategory
import com.xinghan.xingtu.domain.model.ChecklistPriority
import com.xinghan.xingtu.domain.model.TripTemplate

/** One preset item of a checklist template. */
data class TemplateItem(
    val title: String,
    val category: ChecklistCategory,
    val priority: ChecklistPriority,
)

/**
 * Preset checklist templates for new trips. The demo dataset reuses the
 * same definitions so seeded data matches what a user would generate.
 */
class ChecklistTemplates {

    fun itemsFor(template: TripTemplate): List<TemplateItem> = when (template) {
        TripTemplate.BUSINESS -> business()
        TripTemplate.WEEKEND -> weekend()
        TripTemplate.CUSTOM -> emptyList()
    }

    fun business(): List<TemplateItem> = listOf(
        TemplateItem("确认会议资料", ChecklistCategory.WORK, ChecklistPriority.IMPORTANT),
        TemplateItem("携带演示手机", ChecklistCategory.DIGITAL, ChecklistPriority.IMPORTANT),
        TemplateItem("下载离线地图", ChecklistCategory.DIGITAL, ChecklistPriority.NORMAL),
        TemplateItem("笔记本电脑与充电器", ChecklistCategory.DIGITAL, ChecklistPriority.NORMAL),
        TemplateItem("身份证与证件照", ChecklistCategory.DOCUMENT, ChecklistPriority.IMPORTANT),
        TemplateItem("名片与工作证件", ChecklistCategory.DOCUMENT, ChecklistPriority.NORMAL),
        TemplateItem("正装与换洗衣物", ChecklistCategory.CLOTHING, ChecklistPriority.NORMAL),
        TemplateItem("洗漱用品", ChecklistCategory.OTHER, ChecklistPriority.NORMAL),
        TemplateItem("预订确认酒店行程单", ChecklistCategory.WORK, ChecklistPriority.NORMAL),
        TemplateItem("轻便背包", ChecklistCategory.OTHER, ChecklistPriority.NORMAL),
    )

    fun weekend(): List<TemplateItem> = listOf(
        TemplateItem("身份证与证件照", ChecklistCategory.DOCUMENT, ChecklistPriority.IMPORTANT),
        TemplateItem("充电器与充电宝", ChecklistCategory.DIGITAL, ChecklistPriority.NORMAL),
        TemplateItem("舒适的步行鞋", ChecklistCategory.CLOTHING, ChecklistPriority.IMPORTANT),
        TemplateItem("换洗衣物", ChecklistCategory.CLOTHING, ChecklistPriority.NORMAL),
        TemplateItem("相机与存储卡", ChecklistCategory.DIGITAL, ChecklistPriority.NORMAL),
        TemplateItem("水杯与小零食", ChecklistCategory.OTHER, ChecklistPriority.NORMAL),
        TemplateItem("规划漫游路线", ChecklistCategory.OTHER, ChecklistPriority.NORMAL),
        TemplateItem("雨伞或防晒用品", ChecklistCategory.OTHER, ChecklistPriority.NORMAL),
    )
}
