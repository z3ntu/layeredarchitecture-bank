fun promptCreateAccount(dbWrapper: DBWrapper) {
    println("=== CREATE ACCOUNT ===")
    println("Please enter your customer ID.")
    print("> ")
    val customerId = readLine()?.toIntOrNull()
    if (customerId == null) {
        println("ERROR: Please enter a valid integer.")
        return
    }

    val (accountId, errored, errorMessage) = dbWrapper.createAccount(customerId)
    if (errored) {
        println("ERROR: $errorMessage")
        return
    }
    println("Account with ID $accountId was created.")
}

fun promptDeleteAccount(dbWrapper: DBWrapper) {
    println("=== DELETE ACCOUNT ===")
    println("Please enter your account ID.")
    print("> ")
    val accountId = readLine()?.toIntOrNull()
    if (accountId == null) {
        println("ERROR: Please enter a valid integer.")
        return
    }

    val (_, errored, errorMessage) = dbWrapper.deleteAccount(accountId)
    if (errored) {
        println("ERROR: $errorMessage")
        return
    }
    println("Account with ID $accountId was marked as deleted.")
}

fun promptGetBalance(dbWrapper: DBWrapper) {
    println("=== GET BALANCE ===")
    println("Please enter your account ID.")
    print("> ")
    val accountId = readLine()?.toIntOrNull()
    if (accountId == null) {
        println("ERROR: Please enter a valid integer.")
        return
    }

    val (balance, errored) = dbWrapper.getBalance(accountId)
    if (errored) {
        println("ERROR: Account was not found.")
        return
    }
    println("Balance: $balance")
}

fun promptDoTransfer(dbWrapper: DBWrapper) {
    println("=== DO TRANSFER ===")
    println("Please enter your account ID.")
    print("> ")
    val accountIdFrom = readLine()?.toIntOrNull()
    if (accountIdFrom == null) {
        println("ERROR: Please enter a valid integer.")
        return
    }
    println("Please enter the account ID to send the money to.")
    print("> ")
    val accountIdTo = readLine()?.toIntOrNull()
    if (accountIdTo == null) {
        println("ERROR: Please enter a valid integer.")
        return
    }
    println("Please enter the amount to transfer.")
    print("> ")
    val amount = readLine()?.toIntOrNull()
    if (amount == null) {
        println("ERROR: Please enter a valid integer.")
        return
    }

    val (_, errored, errorMessage) = dbWrapper.doTransfer(accountIdFrom, accountIdTo, amount)
    if (errored) {
        println("ERROR: $errorMessage")
        return
    }
    println("Transfer completed.")
}

fun promptShowHistory(dbWrapper: DBWrapper) {
    println("=== SHOW TRANSACTION HISTORY ===")
    println("Please enter your account ID.")
    print("> ")
    val accountIdFrom = readLine()?.toIntOrNull()
    if (accountIdFrom == null) {
        println("ERROR: Please enter a valid integer.")
        return
    }

    val (transactions, errored, errorMessage) = dbWrapper.getTransferHistory(accountIdFrom)
    if (errored) {
        println("ERROR: $errorMessage")
        return
    }
    if (transactions.isEmpty()) {
        println("No transactions found for account $accountIdFrom.")
        return
    }

    println("""
            }|----|----|----------|------|
            }|From| To |   Date   |Amount|
        """.trimMargin("}"))
    transactions.forEach {
        println("|----|----|----------|------|")
        println("|" + it.accountIdFrom.toString().padEnd(4) +
                "|" + it.accountIdTo.toString().padEnd(4) +
                "|" + it.date.toString().padEnd(10) +
                "|" + it.amount.toString().padEnd(6) + "|")

    }

}