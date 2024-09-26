package org.tinkoff.dsl

class Text : ReadmeElement() {
    private val content = StringBuilder()

    operator fun String.unaryPlus() {
        content.append(this)
    }

    fun bold(text: String) = "**$text**"
    fun underlined(text: String) = "<ins>$text</ins>"
    fun link(link: String, text: String) = "[$text]($link)"
    fun code(language: String, code: String) = "\n```$language\n$code\n```\n"

    override fun toString(): String = content.toString()
}