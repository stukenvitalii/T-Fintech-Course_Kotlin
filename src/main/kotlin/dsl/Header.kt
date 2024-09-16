package org.tinkoff.dsl

class Header(private val level: Int) : ReadmeElement() {
    private val content = StringBuilder()

    operator fun String.unaryPlus() {
        content.append(this)
    }

    override fun toString(): String = "#".repeat(level) + " " + content.toString()
}