import oracle.jdbc.OracleCallableStatement
import oracle.jdbc.OracleTypes
import java.sql.Connection
import java.sql.Date
import java.sql.DriverManager
import java.sql.SQLException

data class SqlResult<out T>(val value: T, val errored: Boolean = false, val errorMessage: String = "")

class DBWrapper {
    private val conn: Connection

    init {
        Class.forName("oracle.jdbc.driver.OracleDriver")
        conn = DriverManager.getConnection("jdbc:oracle:thin:@oracle.spengergasse.at:1521:orcl", "5EHIF20178_24", "asus")
    }

    fun createAccount(customerId: Int): SqlResult<Int> {
        val query = "{? = call OUTER_LAYER.create_account(?)}"
        val cstmt = conn.prepareCall(query)
        cstmt.registerOutParameter(1, OracleTypes.INTEGER)
        cstmt.setInt(2, customerId)
        try {
            cstmt.execute()
        } catch (e: SQLException) {
            return SqlResult(-1, true)
        }
        val accountId = cstmt.getInt(1)
        if (accountId == -1) {
            return SqlResult(-1, true, "Invalid customer ID.")
        }
        return SqlResult(accountId)
    }

    fun deleteAccount(accountId: Int): SqlResult<Unit> {
        val query = "{? = call OUTER_LAYER.delete_account(?)}"
        val cstmt = conn.prepareCall(query)
        cstmt.registerOutParameter(1, OracleTypes.INTEGER)
        cstmt.setInt(2, accountId)
        try {
            cstmt.execute()
        } catch (e: SQLException) {
            return SqlResult(Unit, true)
        }
        val retval = cstmt.getInt(1)
        return when (retval) {
            0 -> SqlResult(Unit)
            -1 -> SqlResult(Unit, true, "Invalid account.")
            -2 -> SqlResult(Unit, true, "Account is already marked as deleted.")
            else -> SqlResult(Unit, true, "Unknown error.")
        }
    }

    fun getBalance(accountId: Int): SqlResult<Int> {
        val query = "{? = call OUTER_LAYER.get_balance(?)}"
        val cstmt = conn.prepareCall(query)
        cstmt.registerOutParameter(1, OracleTypes.INTEGER)
        cstmt.setInt(2, accountId)
        try {
            cstmt.execute()
        } catch (e: SQLException) {
            return SqlResult(-1, true)
        }

        return SqlResult(cstmt.getInt(1))
    }

    fun doTransfer(accountIdFrom: Int, accountIdTo: Int, amount: Int): SqlResult<Unit> {
        val query = "{? = call OUTER_LAYER.do_transfer(?, ?, ?)}"
        val cstmt = conn.prepareCall(query)
        cstmt.registerOutParameter(1, OracleTypes.INTEGER)
        cstmt.setInt(2, accountIdFrom)
        cstmt.setInt(3, accountIdTo)
        cstmt.setInt(4, amount)
        try {
            cstmt.execute()
        } catch (e: SQLException) {
            return SqlResult(Unit, true)
        }

        val retval = cstmt.getInt(1)
        return when (retval) {
            0 -> SqlResult(Unit)
            -1 -> SqlResult(Unit, true, "Account $accountIdFrom has insufficient funds.")
            else -> SqlResult(Unit, true, "Unknown error.")
        }
    }

    fun getTransferHistory(accountIdFrom: Int): SqlResult<List<Transaction>> {
        val query = "{? = call OUTER_LAYER.get_transfer_history(?)}"
        val cstmt = conn.prepareCall(query)
        cstmt.registerOutParameter(1, OracleTypes.CURSOR)
        cstmt.setInt(2, accountIdFrom)
        try {
            cstmt.execute()
        } catch (e: SQLException) {
            return SqlResult(emptyList(), true)
        }
        val cursor = (cstmt as OracleCallableStatement).getCursor(1)
        val retList: ArrayList<Transaction> = ArrayList()
        while (cursor.next()) {
            retList.add(Transaction(cursor.getInt(1), cursor.getInt(2), cursor.getDate(3), cursor.getInt(4)))
        }
        cursor.close()
        cstmt.close()
        return SqlResult(retList)
    }
}

data class Transaction(val accountIdFrom: Int, val accountIdTo: Int, val date: Date, val amount: Int)