package com.example.barberbuzz

import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(manifest = Config.NONE)  // This will skip the manifest requirements
class BookAppointmentActivityTest {

    private lateinit var activityScenario: ActivityScenario<BookAppointmentActivity>

    @Before
    fun setup() {
        // Launch the activity
        activityScenario = ActivityScenario.launch(BookAppointmentActivity::class.java)
    }

    @Test
    fun testBookingAppointmentSuccess() {
        activityScenario.onActivity { activity ->
            // Find the views
            val bookAppointmentButton: Button = activity.findViewById(R.id.bookAppointmentButton)
            val barberSpinner: Spinner = activity.findViewById(R.id.barberSpinner)
            val dateEditText: EditText = activity.findViewById(R.id.dateEditText)
            val timeEditText: EditText = activity.findViewById(R.id.timeEditText)

            // Simulate filling out the form
            barberSpinner.setSelection(0) // Select first barber in the list
            dateEditText.setText("19/11/2024") // Set valid date
            timeEditText.setText("10:00") // Set valid time

            // Mock Toast to verify it is triggered
            val toastMock = Mockito.mock(Toast::class.java)
            whenever(Toast.makeText(activity, "Appointment booked successfully", Toast.LENGTH_SHORT)).thenReturn(toastMock)

            // Simulate button click to book appointment
            bookAppointmentButton.performClick()

            // Verify that Toast was shown
            verify(toastMock).show()
        }
    }
}
