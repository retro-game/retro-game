package com.github.retro_game.body

import com.github.retro_game.body.BodyType.MOON

class Body(
    val name: String,
    val type: BodyType,
    val image: Int,
    val activity: Int
    ) {

    fun isMoon(): Boolean = type == MOON
}
