package com.github.retro_game.galaxy

import com.nhaarman.mockitokotlin2.mock
import io.kotest.matchers.shouldBe
import org.junit.Test

class GalaxyServiceTest {

    private val service = GalaxyService(
            galaxySlotRepository = mock(),
            allianceTagCache = mock(),
            statisticsCache = mock(),
            userAllianceCache = mock(),
            activityService = mock(),
            noobProtectionService = mock(),
            userServiceInternal = mock()
    )

    @Test
    fun `should move one galaxy forward when on at the end`(){
        service.nextGalaxy(1) shouldBe 2
    }

    @Test
    fun `should move to start galaxy when at the end`(){
        service.nextGalaxy(5) shouldBe 1
    }

    @Test
    fun `should move one galaxy backwards when not at start`(){
        service.previousGalaxy(2) shouldBe 1
    }

    @Test
    fun `should move to end galaxy when at the start`(){
        service.previousGalaxy(1) shouldBe 5
    }

    @Test
    fun `should move one system forward when on at the end`(){
        service.nextSystem(1) shouldBe 2
    }

    @Test
    fun `should move to start system when at the end`(){
        service.nextSystem(500) shouldBe 1
    }

    @Test
    fun `should move one system backwards when not at start`(){
        service.previousSystem(2) shouldBe 1
    }

    @Test
    fun `should move to end system when at the start`(){
        service.previousSystem(1) shouldBe 500
    }
}
