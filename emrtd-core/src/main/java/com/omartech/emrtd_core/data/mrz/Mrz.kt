package com.omartech.emrtd_core.data.mrz

internal object Mrz {
    data class Parsed(val docNumber: String, val dob: String, val doe: String)

    fun parse(lines: List<String>): Parsed? {
        val l1 = lines.firstOrNull { it.length >= 44 && it.startsWith("P<") } ?: return null
        val l2 = lines.firstOrNull { it.length == 44 && it.any(Char::isDigit) } ?: return null
        val docNumber = l2.substring(0, 9).replace("<", "")
        val dob = l2.substring(13, 19) // YYMMDD
        val doe = l2.substring(21, 27) // YYMMDD
        return Parsed(docNumber, dob, doe)
    }
}


