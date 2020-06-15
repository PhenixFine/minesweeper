import java.util.*

object Minesweeper {
    private val SCANNER = Scanner(System.`in`)
    private var LINES = Array(9) { Array(9) { 0 } }
    private var VIEW = Array(9) { Array(9) { 0 } } // 0 = hidden, 1 = show, 2 = marked
    private var BOMBS = 0
    private var FAKE_BOMBS = 0
    private var FREE_LEFT = 81 // number of fields not yet shown to user ( bombs will get subtracted from it )
    private var SET_BOMBS = false
    private var FIRST = true // for first run through of the surrounding() function
    private var LOST = false

    fun run() {
        initialize()
        while ((FAKE_BOMBS > 0 || BOMBS > 0) && !LOST && FREE_LEFT != 0) {
            printField()
            fieldAction()
        }
        printField()
        println(if (LOST) "You stepped on a mine and failed!" else "Congratulations! You found all the mines!")
    }

    private fun initialize() {
        BOMBS = getNum("How many mines do you want on the field? ", false)
        if (notRange(BOMBS, 1..71)) BOMBS = getRange(BOMBS, 1..71)
        FREE_LEFT -= BOMBS

        while (!SET_BOMBS) {
            printField()
            fieldAction()
        }
        for (num1 in LINES.indices) { // in case user marked bombs before freeing a field
            for (num2 in LINES.indices) {
                if (VIEW[num1][num2] == 2 && LINES[num1][num2] == 11) {
                    FAKE_BOMBS -= 1
                    BOMBS -= 1
                }
            }
        }
    }

    private fun printField() {
        val strLine = "—│—————————│"
        println("\n │123456789│")
        println(strLine)
        for (num in LINES.indices) {
            print("${num + 1}│")
            for (num2 in LINES[num].indices) {
                print(
                    when {
                        LINES[num][num2] == 11 && LOST -> "X"
                        VIEW[num][num2] == 2 && !LOST -> "*"
                        VIEW[num][num2] == 1 -> {
                            if (LINES[num][num2] == 0) "/" else LINES[num][num2]
                        }
                        else -> "."
                    }
                )
            }
            println("│")
        }
        println(strLine)
    }

    private fun fieldAction() {
        var marked = false
        while (!marked) {
            print("Set/unset mine marks or claim a cell as free: ")
            val str1 = SCANNER.next()
            val str2 = SCANNER.next()
            val str3 = SCANNER.next().toLowerCase()
            var num2 = if (isNumber(str1)) str1.toInt() else getNum(str1)
            var num1 = if (isNumber(str2)) str2.toInt() else getNum(str2)
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
        if (!SET_BOMBS) { // sets up the view that bombsSet() needs to work
            if (VIEW[num1][num2] == 2) FAKE_BOMBS -= 1
            VIEW[num1][num2] = 1
            surroundCheck(num1, num2)
            FIRST = false
            bombsSet()
            for (numA in VIEW.indices) { // resets the view, so that open fields can be shown
                for (numB in VIEW[numA].indices) {
                    if (VIEW[numA][numB] == 1) VIEW[numA][numB] = 0
                }
            }
            VIEW[num1][num2] = 1
            SET_BOMBS = true
            FREE_LEFT -= 1
            surroundCheck(num1, num2)
            return true
        } else { // frees a field if it is not a bomb or already free
            if (LINES[num1][num2] == 11) {
                LOST = true
                return true
            } else {
                when (VIEW[num1][num2]) {
                    0 -> {
                        VIEW[num1][num2] = 1
                        FREE_LEFT -= 1
                        if (LINES[num1][num2] == 0) surroundCheck(num1, num2)
                        return true
                    }
                    1 -> {
                        println("field is already free")
                        return false
                    }
                    2 -> {
                        VIEW[num1][num2] = 1
                        FREE_LEFT -= 1
                        if (LINES[num1][num2] == 0) surroundCheck(num1, num2)
                        FAKE_BOMBS -= 1
                        return true
                    }
                }
            }
        }
        return false
    }

    private fun markMine(num1: Int, num2: Int): Boolean {
        if (LINES[num1][num2] == 11) {
            return if (VIEW[num1][num2] == 0) {
                BOMBS -= 1
                VIEW[num1][num2] = 2
                true
            } else {
                BOMBS += 1
                VIEW[num1][num2] = 0
                true
            }
        } else {
            return when (VIEW[num1][num2]) {
                0 -> {
                    VIEW[num1][num2] = 2
                    FAKE_BOMBS += 1
                    true
                }
                2 -> {
                    VIEW[num1][num2] = 0
                    FAKE_BOMBS -= 1
                    true
                }
                else -> {
                    println("open field cannot be marked")
                    false
                }
            }
        }
    }

    private fun bombsSet() {
        repeat(BOMBS) {
            var changed = false
            while (!changed) {
                val num1 = (0..8).random()
                val num2 = (0..8).random()
                if (LINES[num1][num2] != 11 && VIEW[num1][num2] != 1) {
                    LINES[num1][num2] = 11
                    changed = true
                    surroundCheck(num1, num2)
                }
            }
        }
    }

    // checks fields around a given field and then calls surroundWork to do the work with each one
    private fun surroundCheck(num1: Int, num2: Int) {
        if (num2 != 0 && LINES[num1][num2 - 1] != 11) surroundWork(num1, num2 - 1)
        if (num2 != 8 && LINES[num1][num2 + 1] != 11) surroundWork(num1, num2 + 1)
        if (num1 != 0) {
            if (LINES[num1 - 1][num2] != 11) surroundWork(num1 - 1, num2)
            if (num2 != 0 && LINES[num1 - 1][num2 - 1] != 11) surroundWork(num1 - 1, num2 - 1)
            if (num2 != 8 && LINES[num1 - 1][num2 + 1] != 11) surroundWork(num1 - 1, num2 + 1)
        }
        if (num1 != 8) {
            if (LINES[num1 + 1][num2] != 11) surroundWork(num1 + 1, num2)
            if (num2 != 0 && LINES[num1 + 1][num2 - 1] != 11) surroundWork(num1 + 1, num2 - 1)
            if (num2 != 8 && LINES[num1 + 1][num2 + 1] != 11) surroundWork(num1 + 1, num2 + 1)
        }
    }

    // Has 3 different things it does depending on if it's the first run through and if all the bombs have been set yet.
    // After first two cases have been satisfied it then is used to clear fields around an empty field.
    private fun surroundWork(num1: Int, num2: Int) {
        if (!SET_BOMBS && !FIRST) LINES[num1][num2] += 1 else {
            if (VIEW[num1][num2] != 1) {
                if (VIEW[num1][num2] == 2) FAKE_BOMBS -= 1
                VIEW[num1][num2] = 1
                if (!FIRST) FREE_LEFT -= 1
                if (LINES[num1][num2] == 0 && !FIRST) surroundCheck(num1, num2)
            }
        }
    }

    private fun getNum(text: String, defaultMessage: Boolean = true): Int {
        val strErrorNum = " was not a number, please try again: "
        var num = text
        var default = defaultMessage

        do {
            print(if (default) num + strErrorNum else num)
            if (!default) default = true
            num = readLine()!!
        } while (!isNumber(num))

        return num.toInt()
    }

    private fun getRange(num: Int, range: IntRange): Int {
        var num2 = num
        do {
            num2 = getNum("$num2 was out of range. Please enter a number ${range.first} to ${range.last}: ", false)
        } while (notRange(num2, range))
        return num2
    }

    private fun notRange(num: Int, range: IntRange) = (!range.contains(num))

    private fun isNumber(number: String) = number.toIntOrNull() != null

    // this useful random function was found on Stack Overflow
    private fun IntRange.random() = Random().nextInt(endInclusive + 1 - start) + start
}

fun main() {
    Minesweeper.run()
}