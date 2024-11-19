package com.example.barberbuzz

import org.junit.Assert.*
import org.junit.Test
import java.util.*

class BookAppointmentActivityTest {

    @Test
    fun `validate appointment date and time - valid input`() {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, 1) // Set to tomorrow
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val hour = 10
        val minute = 30

        val isValid = validateDateTime("$day/$month/$year", "$hour:$minute")
        assertTrue("The date and time should be valid", isValid)
    }

    @Test
    fun `validate appointment date and time - past input`() {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val hour = 10
        val minute = 30

        val isValid = validateDateTime("$day/$month/$year", "$hour:$minute")
        assertFalse("The date and time should be invalid", isValid)
    }

    private fun validateDateTime(date: String, time: String): Boolean {
        val dateParts = date.split("/")
        val day = dateParts[0].toInt()
        val month = dateParts[1].toInt() - 1 // Month is 0-based
        val year = dateParts[2].toInt()

        val timeParts = time.split(":")
        val hour = timeParts[0].toInt()
        val minute = timeParts[1].toInt()

        val selectedDateTime = Calendar.getInstance().apply {
            set(year, month, day, hour, minute)
        }

        return selectedDateTime.after(Calendar.getInstance())
    }
}
