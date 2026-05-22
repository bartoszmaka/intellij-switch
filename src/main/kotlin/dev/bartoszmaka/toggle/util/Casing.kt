package dev.bartoszmaka.toggle.util

enum class Casing {
    LOWER, UPPER, TITLE, OTHER;

    companion object {
        private val LOWER_RX = Regex("^[a-z]+$")
        private val UPPER_RX = Regex("^[A-Z]+$")
        private val TITLE_RX = Regex("^[A-Z][a-z]*$")

        fun detect(token: String): Casing = when {
            token.isEmpty() -> OTHER
            LOWER_RX.matches(token) -> LOWER
            UPPER_RX.matches(token) -> UPPER
            TITLE_RX.matches(token) -> TITLE
            else -> OTHER
        }

        fun apply(token: String, casing: Casing): String = when (casing) {
            LOWER -> token.lowercase()
            UPPER -> token.uppercase()
            TITLE -> if (token.isEmpty()) token
                     else token.first().uppercaseChar() + token.drop(1).lowercase()
            OTHER -> token
        }
    }
}
