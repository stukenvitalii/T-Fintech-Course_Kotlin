package org.tinkoff.dsl

@DslMarker
annotation class PrettyPrintDsl

@PrettyPrintDsl
class Readme {
    private val elements = mutableListOf<ReadmeElement>()

    fun header(level: Int, init: Header.() -> Unit) {
        elements += Header(level).apply(init)
    }

    fun text(init: Text.() -> Unit) {
        elements += Text().apply(init)
    }

    fun dividingLine() {
        elements += DividingLine()
    }

    override fun toString(): String = elements.joinToString("\n")
}

fun readme(init: Readme.() -> Unit): Readme = Readme().apply(init)