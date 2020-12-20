package com.github.retro_game.galaxy

import com.github.retro_game.retro_game.controller.activity.Activity
import com.github.retro_game.retro_game.dto.CoordinatesDto
import com.github.retro_game.retro_game.dto.CoordinatesKindDto
import com.github.retro_game.retro_game.dto.CoordinatesKindDto.PLANET
import com.github.retro_game.retro_game.service.PhalanxService
import com.github.retro_game.retro_game.service.UserService
import org.hibernate.validator.constraints.Range
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import java.time.Instant
import java.util.*

@Controller
@Validated
class GalaxyController constructor (
    private val galaxyService: GalaxyService,
    private val phalanxService: PhalanxService,
    private val userService: UserService,
    private val missileService: MissileService
) {

    @GetMapping("/galaxy")
    @PreAuthorize("hasPermission(#bodyId, 'ACCESS')")
    @Activity(bodies = ["#bodyId"])
    fun galaxy(
        @RequestParam(name = "body") bodyId: Long,
        @RequestParam @Range(min = 1, max = 5) galaxy: Int,
        @RequestParam @Range(min = 1, max = 500) system: Int,
        @RequestParam(required = false) @Range(min = 1, max = 15) position: Int?,
        @RequestParam(required = false) kind: CoordinatesKindDto?,
        model: Model
    ): String {
        model.apply {
            addAttribute("bodyId", bodyId)
            addAttribute("galaxy", galaxy)
            addAttribute("system", system)
            addAttribute("position", position)
            addAttribute("kind", kind)
            addAttribute("time", Date.from(Instant.ofEpochSecond(Instant.now().epochSecond)))
            addAttribute("slots", galaxyService.getSlots(galaxy, system))
            addAttribute("systemWithinRange", phalanxService.systemWithinRange(bodyId, galaxy, system))
            addAttribute("numProbes", userService.currentUserSettings.numProbes)
            addAttribute("inMissileRange", missileService.systemInRange(bodyId, galaxy, system))
        }

        return "galaxy"
    }
}
