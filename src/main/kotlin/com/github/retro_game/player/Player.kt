package com.github.retro_game.player

import com.github.retro_game.player.NoobProtectionRank.EQUAL
import com.github.retro_game.player.NoobProtectionRank.NOOB
import com.github.retro_game.player.NoobProtectionRank.STRONGER

class Player(
    val userId: Long,
    val userName: String,
    val rank: Int,
    val onVacation: Boolean,
    val banned: Boolean,
    val alliance: Alliance? = null,
    val noobProtectionRank: NoobProtectionRank,
    val inactiveShort: Boolean,
    val inactiveLong: Boolean
) {

    fun isNoob(): Boolean = noobProtectionRank == NOOB
    fun isEqual(): Boolean = noobProtectionRank == EQUAL
    fun isStronger(): Boolean = noobProtectionRank == STRONGER
}
