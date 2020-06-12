import java.util.*

object Minesweeper {
    private val scanner = Scanner(System.`in`)
    private var lines = Array(9) { Array(9) { 0 } }
    private var bombs = 0
    private var fakeBombs = 0
    private const val strErrorNum = " was not a number, please try again: "
    private const val strLine = "—│—————————│"

    private fun getNum(text: String): Int {
        print(text)
        var num = readLine()!!
        while (!isNumber(num)) {
            print(num + strErrorNum)
            num = readLine()!!
        }
        return num.toInt()
    }

    private fun getRange(num: Int, range: IntRange): Int {
        var num2 = num
        do {
            num2 = getNum("$num2 was out of range. Please enter a number ${range.first} to ${range.last}: ")
        } while (notRange(num2, range))
        return num2
    }

    private fun initialize() {
        bombs = getNum("How many mines do you want on the field? ")
        if (notRange(bombs, 1..81)) bombs = getRange(bombs, 1..81)

        repeat(bombs) {
            var changed = false
            while (!changed) {
                val num = (0..8).random()
                val num2 = (0..8).random()
                if (lines[num][num2] != 11) {
                    lines[num][num2] = 11
                    changed = true
                    if (num2 != 0) if (lines[num][num2 - 1] != 11) lines[num][num2 - 1] += 1
                    if (num2 != 8) if (lines[num][num2 + 1] != 11) lines[num][num2 + 1] += 1
                    if (num != 0) {
                        if (lines[num - 1][num2] != 11) lines[num - 1][num2] += 1
                        if (num2 != 0) if (lines[num - 1][num2 - 1] != 11) lines[num - 1][num2 - 1] += 1
                        if (num2 != 8) if (lines[num - 1][num2 + 1] != 11) lines[num - 1][num2 + 1] += 1
                    }
                    if (num != 8) {
                        if (lines[num + 1][num2] != 11) lines[num + 1][num2] += 1
                        if (num2 != 0) if (lines[num + 1][num2 - 1] != 11) lines[num + 1][num2 - 1] += 1
                        if (num2 != 8) if (lines[num + 1][num2 + 1] != 11) lines[num + 1][num2 + 1] += 1
                    }
                }
            }
        }
    }

    private fun isNumber(number: String) = number.toIntOrNull() != null

    private fun markMine() {
        var marked = false
        while (!marked) {
            print("Set/delete mines marks (x and y coordinates): ")
            val str1 = scanner.next()
            val str2 = scanner.next()
            var num1 = if (isNumber(str1)) str1.toInt() else getNum("$str1$strErrorNum")
            var num2 = if (isNumber(str2)) str2.toInt() else getNum("$str2$strErrorNum")
            if (notRange(num1, 1..9)) num1 = getRange(num1, 1..9)
            if (notRange(num2, 1..9)) num2 = getRange(num2, 1..9)
            num1 -= 1
            num2 -= 1

            when (lines[num2][num1]) {
                -1 -> {
                    fakeBombs -= 1
                    lines[num2][num1] += 1
                    marked = true
                }
                0 -> {
                    fakeBombs += 1
                    lines[num2][num1] -= 1
                    marked = true
                }
                10 -> {
                    bombs += 1
                    lines[num2][num1] += 1
                    marked = true
                }
                11 -> {
                    bombs -= 1
                    lines[num2][num1] -= 1
                    marked = true
                }
                else -> println("There is a number here!")
            }
        }
    }

    private fun notRange(num: Int, range: IntRange) = (!range.contains(num))

    private fun printField() {
        println("\n │123456789│")
        println(strLine)
        for (num in lines.indices) {
            print("${num + 1}│")
            lines[num].forEach { print(if (it == 0 || it == 11) '.' else if (it == -1 || it == 10) '*' else it) }
            println("│")
        }
        println(strLine)
    }

    // this useful function was found on Stack Overflow
    private fun IntRange.random() = Random().nextInt(endInclusive + 1 - start) + start

    fun run() {
        initialize()
        while (fakeBombs > 0 || bombs > 0) {
            printField()
            markMine()
        }
        printField()
        println("Congratulations! You found all the mines!")
    }
}

fun main() {
    Minesweeper.run()
}