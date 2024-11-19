package com.example.barberbuzz

import android.app.DatePickerDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.firebase.database.*
import java.util.*

class BookAppointmentActivity : AppCompatActivity() {

    private lateinit var hairstyleNameTextView: TextView
    private lateinit var priceTextView: TextView
    lateinit var barberSpinner: Spinner
    lateinit var dateEditText: EditText
    lateinit var timeEditText: EditText
    lateinit var bookAppointmentButton: Button
    lateinit var databaseReference: DatabaseReference
    lateinit var barberReference: DatabaseReference

    private val CHANNEL_ID = "appointment_notifications"
    private val REQUEST_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_appointment)

        // Create Notification Channel
        createNotificationChannel()

        // Check for notification permission
        checkNotificationPermission()



        // Show back arrow in toolbar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)


        // Initialize views
        hairstyleNameTextView = findViewById(R.id.hairstyleNameTextView)
        priceTextView = findViewById(R.id.priceTextView)
        barberSpinner = findViewById(R.id.barberSpinner)
        dateEditText = findViewById(R.id.dateEditText)
        timeEditText = findViewById(R.id.timeEditText)
        bookAppointmentButton = findViewById(R.id.bookAppointmentButton)

        // Initialize Firebase Database reference for appointments
        databaseReference = FirebaseDatabase.getInstance().getReference("appointments")

        // Initialize Firebase Database reference for barbers in the users node
        barberReference = FirebaseDatabase.getInstance().getReference("users/Barbers")

        // Retrieve the hairstyle data from the intent
        val hairstyleName = intent.getStringExtra("hairstyle_name")
        val hairstylePrice = intent.getStringExtra("hairstyle_price")

        // Set intent data to the TextViews
        hairstyleNameTextView.text = hairstyleName ?: "Unknown"
        priceTextView.text = hairstylePrice ?: "Unknown"


        populateBarberSpinner()


        val sharedPreferences = getSharedPreferences("userPrefs", Context.MODE_PRIVATE)
        val username = sharedPreferences.getString("username", null)

        // Set up DatePickerDialog for the date selection
        dateEditText.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                this, { _, selectedYear, selectedMonth, selectedDay ->
                    val selectedDate = Calendar.getInstance()
                    selectedDate.set(selectedYear, selectedMonth, selectedDay)

                    // Check if the selected date is in the past
                    if (selectedDate.before(Calendar.getInstance())) {
                        Toast.makeText(this, "Please select a valid date", Toast.LENGTH_SHORT).show()
                    } else {
                        dateEditText.setText("$selectedDay/${selectedMonth + 1}/$selectedYear")
                    }
                }, year, month, day
            )
            datePickerDialog.show()
        }

        // Set up TimePickerDialog for the time selection
        timeEditText.setOnClickListener {
            val calendar = Calendar.getInstance()
            val currentYear = calendar.get(Calendar.YEAR)
            val currentMonth = calendar.get(Calendar.MONTH)
            val currentDay = calendar.get(Calendar.DAY_OF_MONTH)

            // Only validate the time for today
            val isToday = dateEditText.text.toString() == String.format("%02d/%02d/%04d", currentDay, currentMonth + 1, currentYear)

            val timePickerDialog = TimePickerDialog(this, { _, selectedHour, selectedMinute ->
                // Check if the selected time is within the allowed range
                if (selectedHour in 9..19) { // 9 AM to 7 PM
                    val selectedTime = Calendar.getInstance()
                    selectedTime.set(Calendar.HOUR_OF_DAY, selectedHour)
                    selectedTime.set(Calendar.MINUTE, selectedMinute)

                    // Only check if the time is in the past if the selected date is today
                    if (isToday && selectedTime.before(Calendar.getInstance())) {
                        Toast.makeText(this, "Please select a valid time", Toast.LENGTH_SHORT).show()
                    } else {
                        timeEditText.setText(String.format("%02d:%02d", selectedHour, selectedMinute))
                    }
                } else {
                    Toast.makeText(this, "Please select a time between 9 AM and 7 PM", Toast.LENGTH_SHORT).show()
                }
            }, 9, 0, true) // Set default to 9:00 AM
            timePickerDialog.show()
        }


        // Handle book appointment button click
        bookAppointmentButton.setOnClickListener {
            val selectedBarber = barberSpinner.selectedItem.toString()
            val selectedDate = dateEditText.text.toString()
            val selectedTime = timeEditText.text.toString()

            if (username == null) {
                Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedDate.isEmpty() || selectedTime.isEmpty()) {
                Toast.makeText(this, "Please select both date and time", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Parse selected time into Calendar object
            val selectedTimeCalendar = Calendar.getInstance()
            val selectedHour = selectedTime.split(":")[0].toInt()
            val selectedMinute = selectedTime.split(":")[1].toInt()
            selectedTimeCalendar.set(Calendar.HOUR_OF_DAY, selectedHour)
            selectedTimeCalendar.set(Calendar.MINUTE, selectedMinute)

            // Check if the selected date and time are valid
            val selectedDateParts = selectedDate.split("/")
            val selectedYear = selectedDateParts[2].toInt()
            val selectedMonth = selectedDateParts[1].toInt() - 1 // Month is 0-based
            val selectedDay = selectedDateParts[0].toInt()

            val fullSelectedDate = Calendar.getInstance()
            fullSelectedDate.set(selectedYear, selectedMonth, selectedDay, selectedHour, selectedMinute)

            if (fullSelectedDate.before(Calendar.getInstance())) {
                Toast.makeText(this, "Please select a valid date and time", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Create an end time 45 minutes after the selected time
            val endTimeCalendar = selectedTimeCalendar.clone() as Calendar
            endTimeCalendar.add(Calendar.MINUTE, 45)

            // Query Firebase to check if there's already an appointment for this barber at the selected time or within the next 45 minutes
            val barberAppointmentsReference =
                FirebaseDatabase.getInstance().getReference("barberAppointments/$selectedBarber")

            // Query appointments for the selected date
            barberAppointmentsReference.orderByChild("date").equalTo(selectedDate)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        var isTimeSlotAvailable = true

                        // Iterate through all appointments for this date and check the time
                        for (appointmentSnapshot in snapshot.children) {
                            val appointmentTime = appointmentSnapshot.child("time").value.toString()

                            // Parse the appointment time into a Calendar object
                            val appointmentTimeCalendar = Calendar.getInstance()
                            val appointmentHour = appointmentTime.split(":")[0].toInt()
                            val appointmentMinute = appointmentTime.split(":")[1].toInt()
                            appointmentTimeCalendar.set(Calendar.HOUR_OF_DAY, appointmentHour)
                            appointmentTimeCalendar.set(Calendar.MINUTE, appointmentMinute)

                            // Create an end time 45 minutes after the existing appointment
                            val appointmentEndTimeCalendar =
                                appointmentTimeCalendar.clone() as Calendar
                            appointmentEndTimeCalendar.add(Calendar.MINUTE, 45)

                            // Check if the selected time overlaps with any existing appointment within 45 minutes
                            if ((selectedTimeCalendar in appointmentTimeCalendar..appointmentEndTimeCalendar) ||
                                (endTimeCalendar in appointmentTimeCalendar..appointmentEndTimeCalendar)
                            ) {
                                isTimeSlotAvailable = false
                                break
                            }
                        }

                        if (isTimeSlotAvailable) {
                            // Proceed with booking the appointment
                            val appointment = Appointment(
                                hairstyleName = hairstyleName ?: "Unknown",
                                price = hairstylePrice ?: "Unknown",
                                barberName = selectedBarber,
                                date = selectedDate,
                                time = selectedTime,
                                username = username,

                            )

                            // Save the appointment in 'appointments' node and 'barberAppointments' node
                            val appointmentRef = databaseReference.child(username).push()
                            appointmentRef.setValue(appointment).addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val appointmentId = appointmentRef.key
                                    if (appointmentId != null) {
                                        barberAppointmentsReference.child(appointmentId)
                                            .setValue(appointment)
                                    }
                                    Toast.makeText(
                                        this@BookAppointmentActivity,
                                        "Appointment booked successfully!",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    // Send notification and proceed with any additional actions
                                    sendNotification(appointment)
                                    val intent = Intent(
                                        this@BookAppointmentActivity,
                                        AppointmentDetailsActivity::class.java
                                    )
                                    intent.putExtra("hairstyle_name", appointment.hairstyleName)
                                    intent.putExtra("barber_name", appointment.barberName)
                                    intent.putExtra("date", appointment.date)
                                    intent.putExtra("time", appointment.time)
                                    intent.putExtra("price", appointment.price)
                                    startActivity(intent)
                                    finish() // Optionally, finish the activity
                                } else {
                                    Toast.makeText(
                                        this@BookAppointmentActivity,
                                        "Failed to book appointment: ${task.exception?.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        } else {
                            // Time slot is unavailable
                            Toast.makeText(
                                this@BookAppointmentActivity,
                                "Time slot unavailable. Please choose another time.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(
                            this@BookAppointmentActivity,
                            "Failed to check availability: ${error.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
        }
        setupToolbar()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.apply {
            setDisplayShowTitleEnabled(false) // Disable default title
            setDisplayHomeAsUpEnabled(true) // Show back arrow in toolbar
            setHomeAsUpIndicator(R.drawable.baseline_arrow_back_24) // Custom back arrow icon
        }


    }

    // Handle back button action to navigate to ShopOnline
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                startActivity(Intent(this, HairstylesActivity::class.java))
                finish() // Close current activity
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun populateBarberSpinner() {
        // Retrieve barber names from Firebase database under users/barbers
        barberReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val Barbers = mutableListOf<String>()
                for (barberSnapshot in dataSnapshot.children) {
                    // Each child in barbers node contains a barberName
                    val barberName = barberSnapshot.child("barberName").value.toString()
                    Barbers.add(barberName)
                }

                // Set up the spinner with the list of barber names
                val adapter = ArrayAdapter(this@BookAppointmentActivity, android.R.layout.simple_spinner_item, Barbers)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                barberSpinner.adapter = adapter
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle any errors here
                Toast.makeText(this@BookAppointmentActivity, "Failed to load barbers: ${databaseError.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Appointment Notifications"
            val descriptionText = "Notifications for booked appointments"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), REQUEST_CODE)
            }
        }
    }

    private fun sendNotification(appointment: Appointment) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create an intent to open the AppointmentDetailsActivity when the notification is tapped
        val intent = Intent(this, AppointmentDetailsActivity::class.java).apply {
            putExtra("hairstyle_name", appointment.hairstyleName)
            putExtra("barber_name", appointment.barberName)
            putExtra("date", appointment.date)
            putExtra("time", appointment.time)
            putExtra("price", appointment.price)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        // Build the notification
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.logo)
            .setContentTitle("Appointment Booked")
            .setContentText("Appointment with ${appointment.barberName} on ${appointment.date} at ${appointment.time}")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true) // Dismiss notification when tapped
            .build()

        // Send the notification to user
        notificationManager.notify(0, notification)
    }


}
