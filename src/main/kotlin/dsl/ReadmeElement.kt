package org.tinkoff.dsl

@PrettyPrintDsl
sealed class ReadmeElement

class DividingLine : ReadmeElement() {
    override fun toString(): String = "\n---\n"
}