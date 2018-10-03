package io.rybalkinsd.kotlinbootcamp.practice

/**
 * NATO phonetic alphabet
 */
val alphabet = setOf("Alfa", "Bravo", "Charlie", "Delta", "Echo", "Foxtrot", "Golf", "Hotel", "India", "Juliett", "Kilo", "Lima", "Mike", "November", "Oscar", "Papa", "Quebec", "Romeo", "Sierra", "Tango", "Uniform", "Victor", "Whiskey", "Xray", "Yankee", "Zulu")

/**
 * A mapping for english characters to phonetic alphabet.
 * [ a -> Alfa, b -> Bravo, ...]
 */
val association: Map<Char, String> = alphabet.associateBy { it[0].toLowerCase() }

/**
 * Extension function for String which encode it according to `association` mapping
 *
 * @return encoded string
 *
 * Example:
 * "abc".encode() == "AlfaBravoCharlie"
 *
 */
fun String.encode(): String = map { it.toLowerCase() }. map { association[it] ?: it }.joinToString("")

/**
 * A reversed mapping for association
 * [ alpha -> a, bravo -> b, ...]
 */
val reversedAssociation: Map<String, Char> = hashMapOf(*alphabet.map(transform = { it -> Pair(it, it[0].toLowerCase()) }).toTypedArray())

/**
 * Extension function for String which decode it according to `reversedAssociation` mapping
 *
 * @return encoded string or null if it is impossible to decode
 *
 * Example:
 * "alphabravocharlie".encode() == "abc"
 * "charliee".decode() == null
 *
 */
fun String.decode(): String? {
    if (this.isEmpty()) return ""
    val fstChar = this[0]
    when (fstChar) {
        in '0'..'9', ' ' -> {
            val tail = this.substring(1).decode()
            tail ?: return null
            return fstChar + tail
        }
    }
    val lowerStr = this.toLowerCase().capitalize()
    val matches = reversedAssociation.keys.asSequence().map { Pair(lowerStr.indexOf(it), it) }.filter { it.first == 0 }
    val result = matches.firstOrNull()
    result ?: return null
    val tail = lowerStr.substring(result.second.length).decode()
    tail ?: return null
    return reversedAssociation[result.second]!! + tail
}