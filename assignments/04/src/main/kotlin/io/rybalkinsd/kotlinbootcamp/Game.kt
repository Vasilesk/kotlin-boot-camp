package io.rybalkinsd.kotlinbootcamp

import java.io.File
import java.util.Random

const val dictPath = "dictionary.txt"

class Game(path: String = dictPath) {
    private val allWords: List<String>

    init {
        val dictFile = File(path)
        var dataRead = emptyList<String>()
        try {
            dataRead = dictFile.readLines()
        } catch (e: Exception) {
            throw Exception("dictionary.txt was not found. Place it into the directory with jar or give a path to it as arg[0]")
        } finally {
            this.allWords = dataRead
        }
        if (this.allWords.isEmpty()) {
            throw Exception("dictionary is empty")
        }
    }

    private fun fetchWord() = this.allWords[Random().nextInt(this.allWords.size)]

    private fun getBullsCows(w1: String, w2: String): Pair<Int, Int> {
        val guessed = w1.toCharArray()
        val real = w2.toCharArray()
        val bullsMask = guessed.zip(real) { v1, v2 -> if (v1 == v2) v1 else '\n' }

        val fails = guessed.zip(bullsMask) { v1, v2 -> if (v2 == '\n') v1 else '\n' }
                .filter { it != '\n' }.groupingBy { it }.eachCount()
        val notGuessed = real.zip(bullsMask) { v1, v2 -> if (v2 == '\n') v1 else '\n' }
                .filter { it != '\n' }.groupingBy { it }.eachCount()

        val commons = fails.keys.map {
            if (notGuessed.containsKey(it)) minOf(fails[it]!!, notGuessed[it]!!) else 0
        }

        return bullsMask.fold(0) { c, v -> (c + if (v != '\n') 1 else 0) } to commons.sum()
    }

    fun run() {
        println("Welcome to Bulls and Cows game")

        var inGame = true
        while (inGame) {

            val word = this.fetchWord()
            val wordSize = word.length
            var playing = true
            var triesCount = 10
            var hasWon = false

            println("I offered a $wordSize-letter word, your guess?")
            while (playing) {
                val dataRead = readLine() ?: ""
                if (dataRead == "") {
                    playing = false
                } else {
                    if (dataRead.length != wordSize) {
                        println("Word length should be $wordSize")
                    } else {
                        if (dataRead == word) {
                            hasWon = true
                            println("You won!")
                        } else {
                            triesCount--
                            val bullsCows = this.getBullsCows(dataRead, word)
                            val bulls = bullsCows.first
                            val cows = bullsCows.second
                            println("Bulls: $bulls")
                            println("Cows: $cows")
                        }
                    }
                    playing = !hasWon && triesCount > 0
                }
            }

            if (triesCount == 0) {
                println("You`ve lost!")
            }
            if (!hasWon) {
                println("The word was: $word")
            }

            println("Wanna play again? Y/N")
            var gotAnswer = false
            while (!gotAnswer) {
                val answerRead = readLine() ?: ""
                when (answerRead.toUpperCase()) {
                    "", "N" -> {
                        inGame = false
                        gotAnswer = true
                    }
                    "Y" -> {
                        inGame = true
                        gotAnswer = true
                    }
                    else -> {
                        gotAnswer = false
                        println("Type Y or N")
                    }
                }
            }
        }

        println("The game was terminated")
    }
}

fun main(args: Array<String>) {
    val game: Game
    if (args.size == 1) {
        game = Game(args[0])
    } else {
        game = Game()
    }
    game.run()
}