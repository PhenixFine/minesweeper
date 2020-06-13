import java.util.*

object Minesweeper {
    private val scanner = Scanner(System.`in`)
    private var lines = Array(9) { Array(9) { 0 } }
    private var view = Array(9) { Array(9) { 0 } } // 0 = hidden, 1 = show, 2 = marked
    private var bombs = 0
    private var fakeBombs = 0
    private var freeLeft = 81 // number of fields not yet shown to user ( bombs will get subtracted from it )
    private var setBombs = false
    private var first = true // for first run through of the surrounding() function
    private var lost = false
    private const val strErrorNum = " was not a number, please try again: "

    private fun bombsSet() {
        repeat(bombs) {
            var changed = false
            while (!changed) {
                val num1 = (0..8).random()
                val num2 = (0..8).random()
                if (lines[num1][num2] != 11 && view[num1][num2] != 1) {
                    lines[num1][num2] = 11
                    changed = true
                    surroundCheck(num1, num2)
                }
            }
        }
    }

    private fun fieldAction() {
        var marked = false
        while (!marked) {
            print("Set/unset mine marks or claim a cell as free: ")
            val str1 = scanner.next()
            val str2 = scanner.next()
            val str3 = scanner.next().toLowerCase()
            var num2 = if (isNumber(str1)) str1.toInt() else getNum("$str1$strErrorNum")
            var num1 = if (isNumber(str2)) str2.toInt() else getNum("$str2$strErrorNum")
            if (notRange(num1, 1..9)) num1 = getRange(num1, 1..9)
            if (notRange(num2, 1..9)) num2 = getRange(num2, 1..9)
            num1 -= 1
            num2 -= 1

            when (str3) {
                "free" -> marked = free(num1, num2)
                "mine" -> marked = markMine(num1, num2)
            }
        }
    }

    private fun free(num1: Int, num2: Int): Boolean {
        if (!setBombs) { // sets up the view that bombsSet() needs to work
            if (view[num1][num2] == 2) fakeBombs -= 1
            view[num1][num2] = 1
            surroundCheck(num1, num2)
            first = false
            bombsSet()
            for (numA in view.indices) { // resets the view, so that open fields can be shown
                for (numB in view[numA].indices) {
                    if (view[numA][numB] == 1) view[numA][numB] = 0
                }
            }
            view[num1][num2] = 1
            setBombs = true
            freeLeft -= 1
            surroundCheck(num1, num2)
            return true
        } else { // frees a field if it is not a bomb or already free
            if (lines[num1][num2] == 11) {
                lost = true
                return true
            } else {
                when (view[num1][num2]) {
                    0 -> {
                        view[num1][num2] = 1
                        freeLeft -= 1
                        if (lines[num1][num2] == 0) surroundCheck(num1, num2)
                        return true
                    }
                    1 -> {
                        println("field is already free")
                        return false
                    }
                    2 -> {
                        view[num1][num2] = 1
                        freeLeft -= 1
                        if (lines[num1][num2] == 0) surroundCheck(num1, num2)
                        fakeBombs -= 1
                        return true
                    }
                }
            }
        }
        return false
    }

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
        if (notRange(bombs, 1..71)) bombs = getRange(bombs, 1..71)
        freeLeft -= bombs

        while (!setBombs) {
            printField()
            fieldAction()
        }
        for (num1 in lines.indices) { // in case user marked bombs before freeing a field
            for (num2 in lines.indices) {
                if (view[num1][num2] == 2 && lines[num1][num2] == 11) {
                    fakeBombs -= 1
                    bombs -= 1
                }
            }
        }
    }

    private fun isNumber(number: String) = number.toIntOrNull() != null

    private fun markMine(num1: Int, num2: Int): Boolean {
        if (lines[num1][num2] == 11) {
            return if (view[num1][num2] == 0) {
                bombs -= 1
                view[num1][num2] = 2
                true
            } else {
                bombs += 1
                view[num1][num2] = 0
                true
            }
        } else {
            return when (view[num1][num2]) {
                0 -> {
                    view[num1][num2] = 2
                    fakeBombs += 1
                    true
                }
                2 -> {
                    view[num1][num2] = 0
                    fakeBombs -= 1
                    true
                }
                else -> {
                    println("open field cannot be marked")
                    false
                }
            }
        }
    }

    private fun notRange(num: Int, range: IntRange) = (!range.contains(num))

    private fun printField() {
        val strLine = "—│—————————│"
        println("\n │123456789│")
        println(strLine)
        for (num in lines.indices) {
            print("${num + 1}│")
            for (num2 in lines[num].indices) {
                print(
                    when {
                        lines[num][num2] == 11 && lost -> "X"
                        view[num][num2] == 2 && !lost -> "*"
                        view[num][num2] == 1 -> {
                            if (lines[num][num2] == 0) "/" else lines[num][num2]
                        }
                        else -> "."
                    }
                )
            }
            println("│")
        }
        println(strLine)
    }

    // this useful random function was found on Stack Overflow
    private fun IntRange.random() = Random().nextInt(endInclusive + 1 - start) + start

    // checks fields around a given field and then calls surroundWork to do the work with each one
    private fun surroundCheck(num1: Int, num2: Int) {
        if (num2 != 0 && lines[num1][num2 - 1] != 11) surroundWork(num1, num2 - 1)
        if (num2 != 8 && lines[num1][num2 + 1] != 11) surroundWork(num1, num2 + 1)
        if (num1 != 0) {
            if (lines[num1 - 1][num2] != 11) surroundWork(num1 - 1, num2)
            if (num2 != 0 && lines[num1 - 1][num2 - 1] != 11) surroundWork(num1 - 1, num2 - 1)
            if (num2 != 8 && lines[num1 - 1][num2 + 1] != 11) surroundWork(num1 - 1, num2 + 1)
        }
        if (num1 != 8) {
            if (lines[num1 + 1][num2] != 11) surroundWork(num1 + 1, num2)
            if (num2 != 0 && lines[num1 + 1][num2 - 1] != 11) surroundWork(num1 + 1, num2 - 1)
            if (num2 != 8 && lines[num1 + 1][num2 + 1] != 11) surroundWork(num1 + 1, num2 + 1)
        }
    }

    // does the work for surroundCheck. Has 3 different things it does depending on if it's the first run through and if all
    // the bombs have been set yet. After first two cases have been satisfied it then is used to clear fields around an empty field.
    private fun surroundWork(num1: Int, num2: Int) {
        if (!setBombs && !first) lines[num1][num2] += 1 else {
            if (view[num1][num2] != 1) {
                if (view[num1][num2] == 2) fakeBombs -= 1
                view[num1][num2] = 1
                if (!first) freeLeft -= 1
                if (lines[num1][num2] == 0 && !first) surroundCheck(num1, num2)
            }
        }
    }

    fun run() {
        initialize()
        while ((fakeBombs > 0 || bombs > 0) && !lost && freeLeft != 0) {
            printField()
            fieldAction()
        }
        printField()
        println(if (lost) "You stepped on a mine and failed!" else "Congratulations! You found all the mines!")
    }
}

fun main() {
    Minesweeper.run()
}