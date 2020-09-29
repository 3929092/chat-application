package com.rca.chatapplication

import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/")
class TestController {

    @RequestMapping("test")
    fun helloWorld(): String {
        return "ok"
    }
}