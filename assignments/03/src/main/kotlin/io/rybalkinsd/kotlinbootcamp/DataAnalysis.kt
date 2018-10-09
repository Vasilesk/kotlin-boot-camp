package io.rybalkinsd.kotlinbootcamp

class RawProfile constructor(data: String) {
    val rawData = data

    fun toProfile(): Pair<DataSource, Profile>? {
        val data = listOf("id", "fisrtName", "lastName", "age", "source").map { it ->
            val regex = (it + "=([^,]+)").toRegex()
            val matchResult = regex.find(rawData)
            if (matchResult != null) matchResult.groups[1]!!.value else null
        }

        val id = if (data[0] != null) data[0]!!.toLong() else -1L
        val age = if (data[3] != null) data[3]!!.toIntOrNull() else null
        val source = when (data[4]) {
            "facebook" -> DataSource.FACEBOOK
            "vk" -> DataSource.VK
            "linkedin" -> DataSource.LINKEDIN
            else -> null
        }

        var result: Pair<DataSource, Profile>?
        when (source) {
            DataSource.FACEBOOK -> result = source to FacebookProfile(id, data[1], data[2], age)
            DataSource.LINKEDIN -> result = source to LinkedinProfile(id, data[1], data[2], age)
            DataSource.VK -> result = source to VkProfile(id, data[1], data[2], age)
            else -> result = null
        }

        return result
    }
}

/**
 * Here are Raw profiles to analyse
 */
val rawProfiles = listOf(
        RawProfile("""
            fisrtName=alice,
            age=16,
            source=facebook
            """.trimIndent()
        ),
        RawProfile("""
            fisrtName=alice,
            age=16,
            source=facebook
            """.trimIndent()
        ),
        RawProfile("""
            fisrtName=Dent,
            lastName=kent,
            age=88,
            source=linkedin
            """.trimIndent()
        ),
        RawProfile("""
            fisrtName=alla,
            lastName=OloOlooLoasla,
            source=vk
            """.trimIndent()
        ),
        RawProfile("""
            fisrtName=bella,
            age=36,
            source=vk
            """.trimIndent()
        ),
        RawProfile("""
            fisrtName=angel,
            age=I will not tell you =),
            source=facebook
            """.trimIndent()
        ),

        RawProfile(
                """
            lastName=carol,
            source=vk
            age=49,
            """.trimIndent()
        ),
        RawProfile("""
            fisrtName=bob,
            lastName=John,
            age=47,
            source=linkedin
            """.trimIndent()
        ),
        RawProfile("""
            lastName=kent,
            fisrtName=dent,
            age=88,
            source=facebook
            """.trimIndent()
        )
)

enum class DataSource {
    FACEBOOK,
    VK,
    LINKEDIN
}

sealed class Profile constructor (
    var id: Long,
    var firstName: String?,
    var lastName: String?,
    var age: Int?,
    var dataSource: DataSource
) {

    override fun equals(other: Any?): Boolean {
        if (!(other is Profile)) return false
//        what should we do with nulls?
//        if (listOf(this.firstName, this.lastName, this.age, other.firstName, other.lastName, other.age).contains(null)) return false
        return this.firstName == other.firstName &&
                this.lastName == other.lastName &&
                this.age == other.age
    }

    override fun toString(): String {
        return firstName + ' ' + lastName + '(' + age.toString() + ')'
    }
}

/**
 * Task #1
 * Declare classes for all data sources
 */
class FacebookProfile(
    id: Long,
    firstName: String?,
    lastName: String?,
    age: Int?
) : Profile(dataSource = DataSource.FACEBOOK, id = id, firstName = firstName, lastName = lastName, age = age)

class VkProfile(
    id: Long,
    firstName: String?,
    lastName: String?,
    age: Int?
) : Profile(dataSource = DataSource.FACEBOOK, id = id, firstName = firstName, lastName = lastName, age = age)

class LinkedinProfile(
    id: Long,
    firstName: String?,
    lastName: String?,
    age: Int?
) : Profile(dataSource = DataSource.FACEBOOK, id = id, firstName = firstName, lastName = lastName, age = age)

/**
 * Task #2
 * Find the average age for each datasource (for profiles that has age)
 *
 * TODO
 */

fun sameSource(source: DataSource, data: List<Pair<DataSource, Profile>?>) =
        data.asSequence().filter { it != null && it.first == source }.map { it!!.second }.toList()

fun avgCount(data: List<Profile>): Double {
    val pair = data.asSequence().filter { it.age != null }.fold(Pair(0L, 0L)) { a, b -> Pair(a.first + b.age!!, a.second + 1) }
    return pair.first / pair.second.toDouble()
}

val rawTransformed = rawProfiles.map { it.toProfile() }

val sources = listOf(DataSource.FACEBOOK, DataSource.VK, DataSource.LINKEDIN)

val sourcesMap = sources.associate { Pair(it, sameSource(it, rawTransformed)) }

val avgAge: Map<DataSource, Double> = sources.associate { it to avgCount(sourcesMap[it]!!) }

/**
 * Task #3
 * Group all user ids together with all profiles of this user.
 * We can assume users equality by : firstName & lastName & age
 *
 * TODO
 */

val fromAllSources = sourcesMap.values.fold(emptyList<Profile>()) { a, b -> a + b }
val unique = fromAllSources.fold(emptyList<Profile>()) { a, b -> if (a.contains(b)) a else a + listOf(b) }

val grouped: MutableList<MutableList<Profile>> = mutableListOf()

val changes = fromAllSources.forEach { profile ->
    if (! grouped.map { group ->
        if (group[0] == profile) {
            group.add(profile)
            return@map true
        } else {
            return@map false
        }
    }.any()) grouped.add(mutableListOf(profile))
}

val groupedProfiles: Map<Long, List<Profile>> = grouped.indices.associate { it.toLong() to grouped[it] }
