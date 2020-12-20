package com.github.retro_game.error

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class ErrorController {

    @GetMapping("/error")
    fun error() = "error"
}

