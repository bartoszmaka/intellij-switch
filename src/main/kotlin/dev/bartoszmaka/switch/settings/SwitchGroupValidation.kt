package dev.bartoszmaka.switch.settings

object SwitchGroupValidation {
    private val IDENT = Regex("^[A-Za-z_][A-Za-z0-9_]*$")

    fun validateWordGroup(items: List<String>): List<String> {
        val errors = mutableListOf<String>()
        if (items.size < 2) errors += "Group must have at least 2 items"
        for ((i, item) in items.withIndex()) {
            if (!IDENT.matches(item)) {
                errors += "Item ${i + 1} (\"$item\") is not a valid identifier " +
                          "([A-Za-z_][A-Za-z0-9_]*)"
            }
        }
        val dupes = items.groupBy { it }.filter { it.value.size > 1 }.keys
        if (dupes.isNotEmpty()) errors += "duplicate items: ${dupes.joinToString()}"
        return errors
    }

    fun validateCharGroup(items: List<String>): List<String> {
        val errors = mutableListOf<String>()
        if (items.size < 2) errors += "Group must have at least 2 items"
        for ((i, item) in items.withIndex()) {
            val codePoints = item.codePointCount(0, item.length)
            if (codePoints != 1) {
                errors += "Item ${i + 1} (\"$item\") must be a single character " +
                          "(got $codePoints code points)"
                continue
            }
            val cp = item.codePointAt(0)
            if (Character.isWhitespace(cp)) {
                errors += "Item ${i + 1} cannot be whitespace"
            }
        }
        val dupes = items.groupBy { it }.filter { it.value.size > 1 }.keys
        if (dupes.isNotEmpty()) errors += "duplicate items: ${dupes.joinToString()}"
        return errors
    }
}
