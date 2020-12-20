package com.github.retro_game.galaxy

import com.github.retro_game.body.Body
import com.github.retro_game.body.Debris
import com.github.retro_game.player.NoobProtectionRank
import com.github.retro_game.player.Player

data class GalaxySlotDTO(
    val player: Player,
    val planet: Body,
    val moon: Body? = null,
    val debris: Debris,
    val own: Boolean,
) {

    fun showStatus(): Boolean = player.banned || player.onVacation || player.noobProtectionRank != NoobProtectionRank.EQUAL || player.inactiveShort
    fun disableMissileTarget() = player.banned || player.onVacation || player.isNoob()
    fun espionageDisabled() = player.banned || player.onVacation || player.isNoob()
}
