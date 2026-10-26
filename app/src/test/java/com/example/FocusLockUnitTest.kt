package com.example

import com.example.data.model.BlockListEntity
import com.example.data.model.FocusMode
import com.example.data.model.ScheduleEntity
import org.junit.Assert.*
import org.junit.Test

class FocusLockUnitTest {

    @Test
    fun testBlockListPackageParsing() {
        val blockList = BlockListEntity(
            id = 1,
            name = "Social Detox",
            packages = "com.instagram.android, com.zhiliaoapp.musically, , com.twitter.android"
        )
        val packages = blockList.getPackageList()
        assertEquals(3, packages.size)
        assertTrue(packages.contains("com.instagram.android"))
        assertTrue(packages.contains("com.zhiliaoapp.musically"))
        assertTrue(packages.contains("com.twitter.android"))
        assertFalse(packages.contains(""))
    }

    @Test
    fun testScheduleDaysParsing() {
        val schedule = ScheduleEntity(
            id = 1,
            name = "Work Hours",
            startHour = 9,
            startMinute = 0,
            endHour = 17,
            endMinute = 0,
            daysOfWeek = "1,2,3,4,5"
        )
        val days = schedule.getDaysList()
        assertEquals(5, days.size)
        assertTrue(days.contains(1))
        assertTrue(days.contains(5))
        assertFalse(days.contains(6))
    }

    @Test
    fun testFocusModesUnlocked() {
        val modes = FocusMode.values()
        assertEquals(8, modes.size)
        modes.forEach { mode ->
            assertTrue(mode.defaultDurationMinutes > 0)
            assertTrue(mode.title.isNotBlank())
            assertTrue(mode.subtitle.isNotBlank())
        }
        assertTrue(FocusMode.STRICT.isStrictByDefault)
    }

    @Test
    fun testScheduleConflictDetectionLogic() {
        val scheduleA = ScheduleEntity(
            id = 1,
            name = "Morning Sprint",
            startHour = 9,
            startMinute = 0,
            endHour = 11,
            endMinute = 0,
            daysOfWeek = "1,2,3"
        )
        val scheduleB = ScheduleEntity(
            id = 2,
            name = "Overlapping Sprint",
            startHour = 10,
            startMinute = 0,
            endHour = 12,
            endMinute = 0,
            daysOfWeek = "2,4"
        )

        val daysA = scheduleA.getDaysList().toSet()
        val daysB = scheduleB.getDaysList().toSet()
        val commonDays = daysA.intersect(daysB)
        assertTrue(commonDays.contains(2))

        val startA = scheduleA.startHour * 60 + scheduleA.startMinute
        val endA = scheduleA.endHour * 60 + scheduleA.endMinute
        val startB = scheduleB.startHour * 60 + scheduleB.startMinute
        val endB = scheduleB.endHour * 60 + scheduleB.endMinute

        val overlaps = startA < endB && endA > startB
        assertTrue("Schedules should overlap in time", overlaps)
    }
}
