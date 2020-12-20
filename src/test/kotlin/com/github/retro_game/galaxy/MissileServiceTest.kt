package com.github.retro_game.galaxy

import com.github.retro_game.retro_game.dto.BodyInfoDto
import com.github.retro_game.retro_game.dto.CoordinatesDto
import com.github.retro_game.retro_game.dto.CoordinatesKindDto.PLANET
import com.github.retro_game.retro_game.service.BodyService
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import io.kotest.matchers.shouldBe
import org.junit.Test
import org.mockito.ArgumentMatchers.anyLong

class MissileServiceTest {

    private var impulseDriveLevel: Int = 0

    private val bodyService: BodyService = mock {
        on { getBodyBasicInfo(anyLong()) } doReturn planetInSystem(250)
    }

    private val noDriveService = MissileService(
        bodyService = bodyService,
        technologyService = mock {
            on { getLevel(anyLong(), any()) } doReturn 0
        }
    )

    private val lv1DriveService = MissileService(
        bodyService = bodyService,
        technologyService = mock {
            on { getLevel(anyLong(), any()) } doReturn 1
        }
    )

    private val lv6DriveService = MissileService(
        bodyService = bodyService,
        technologyService = mock {
            on { getLevel(anyLong(), any()) } doReturn 6
        }
    )

    @Test
    fun `should not be able to launch when no impulse drive`() {
        impulseDriveLevel = 0

        noDriveService.systemInRange(originBody = 1, targetGalaxy = 1, targetSystem = 250) shouldBe false
    }

    @Test
    fun `should not be able to launch to another galaxy`() {
        impulseDriveLevel = 0

        lv6DriveService.systemInRange(originBody = 1, targetGalaxy = 2, targetSystem = 250) shouldBe false
    }

    @Test
    fun `should be able to launch inside the same system when impulse drive lv 1`() {
        impulseDriveLevel = 1

        lv1DriveService.systemInRange(originBody = 1, targetGalaxy = 1, targetSystem = 250) shouldBe true

        lv1DriveService.systemInRange(originBody = 1, targetGalaxy = 1, targetSystem = 251) shouldBe false
        lv1DriveService.systemInRange(originBody = 1, targetGalaxy = 1, targetSystem = 249) shouldBe false
    }

    @Test
    fun `should be able to launch 29 systems with impulse drive lv6`() {
        impulseDriveLevel = 6

        lv6DriveService.systemInRange(originBody = 1, targetGalaxy = 1, targetSystem = 221) shouldBe true
        lv6DriveService.systemInRange(originBody = 1, targetGalaxy = 1, targetSystem = 279) shouldBe true

        lv6DriveService.systemInRange(originBody = 1, targetGalaxy = 1, targetSystem = 220) shouldBe false
        lv6DriveService.systemInRange(originBody = 1, targetGalaxy = 1, targetSystem = 280) shouldBe false
    }

    private fun planetInSystem(system: Int) = BodyInfoDto(1, 1, "", CoordinatesDto(1, system, 6, PLANET))
}