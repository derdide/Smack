package net.derdide.smack.Model

class Channel(val name: String, description: String, id: String) {
    override fun toString(): String {
        return "#$name"
    }
}