package com.pswidersk.demoapp

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class DemoAppApplication

fun main(args: Array<String>) {
	runApplication<DemoAppApplication>(*args)
}
