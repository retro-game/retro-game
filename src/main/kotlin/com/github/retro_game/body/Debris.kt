package com.github.retro_game.body

class Debris(val metal: Long = 0, val crystal: Long = 0) {

    fun showInOverview(): Boolean = metal > 300 || crystal > 300
}
