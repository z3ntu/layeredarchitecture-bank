fun printMenu(): Int {
    println("""
        |===== LAYERED ARCH EXAMPLE - BANK =====
        |Welcome to the bank! You can do the following actions here:
        |1. Create an account
        |2. Delete an account
        |3. Show your current balance
        |4. Do a transaction
        |5. Show the history of transactions
        |6. Exit the application
    """.trimMargin())
    print("> ")
    return readLine()?.toIntOrNull() ?: return -1
}

fun main(args: Array<String>) {
    val dbWrapper = DBWrapper()
    loop@ while (true) {
        val option = printMenu()
        when (option) {
            1 -> promptCreateAccount(dbWrapper)
            2 -> promptDeleteAccount(dbWrapper)
            3 -> promptGetBalance(dbWrapper)
            4 -> promptDoTransfer(dbWrapper)
            5 -> promptShowHistory(dbWrapper)
            6 -> break@loop
            -1 -> println("You have not entered a number. Please try again.")
            else -> println("You have entered an invalid option. Please try again.")
        }
        println("Press 'Enter' to continue.")
        readLine()
    }
}
