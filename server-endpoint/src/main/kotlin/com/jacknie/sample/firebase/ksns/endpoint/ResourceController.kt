package com.jacknie.sample.firebase.ksns.endpoint

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/resources")
class ResourceController {

    @GetMapping
    fun list(): ResponseEntity<List<String>> {
        return ResponseEntity.ok(listOf("data", "test", "success"))
    }
}
