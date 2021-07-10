package br.com.orangetalent05

import io.micronaut.runtime.Micronaut.*
fun main(args: Array<String>) {
	build()
	    .args(*args)
		.packages("br.com.orangetalent05")
		.start()
}

