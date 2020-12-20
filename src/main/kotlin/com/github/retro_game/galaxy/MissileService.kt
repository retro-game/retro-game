package com.github.retro_game.galaxy

import com.github.retro_game.retro_game.dto.TechnologyKindDto.IMPULSE_DRIVE
import com.github.retro_game.retro_game.service.BodyService
import com.github.retro_game.retro_game.service.TechnologyService
import org.springframework.stereotype.Service
import kotlin.math.abs

@Service
class MissileService(
    private val bodyService: BodyService,
    private val technologyService: TechnologyService
) {

    fun systemInRange(originBody: Long, targetGalaxy: Int, targetSystem: Int): Boolean {
        val origin = bodyService.getBodyBasicInfo(originBody)
        val impulseDrive = technologyService.getLevel(originBody, IMPULSE_DRIVE)

        return when {
            origin.coordinates.galaxy != targetGalaxy -> false
            impulseDrive == 0 -> false
            impulseDrive == 1 -> origin.coordinates.system == targetSystem
            else -> abs(origin.coordinates.system - targetSystem) <= (5 * impulseDrive - 1)
        }
    }
}
