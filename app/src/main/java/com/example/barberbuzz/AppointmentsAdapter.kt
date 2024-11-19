import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.DatePicker
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.barberbuzz.Appointment
import com.example.barberbuzz.AppointmentDetailsActivity
import com.example.barberbuzz.R
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*

class AppointmentsAdapter(private val appointments: List<Appointment>, private val context: Context) :
    RecyclerView.Adapter<AppointmentsAdapter.AppointmentViewHolder>() {

    private val database = FirebaseDatabase.getInstance().reference

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppointmentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_appointment, parent, false)
        return AppointmentViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppointmentViewHolder, position: Int) {
        val appointment = appointments[position]
        holder.bind(appointment)

        holder.cancelBtn.setOnClickListener {
            showDeleteDialog(appointment)
        }

        holder.rescheduleBtn.setOnClickListener {
            showRescheduleDialog(appointment)
        }
    }

    override fun getItemCount(): Int = appointments.size

    private fun showDeleteDialog(appointment: Appointment) {
        val dialogBuilder = AlertDialog.Builder(context)
        dialogBuilder.setMessage("Are you sure you want to cancel this appointment?")
            .setCancelable(false)
            .setPositiveButton("Yes") { _, _ -> deleteAppointment(appointment) }
            .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }

        val alert = dialogBuilder.create()
        alert.setTitle("Cancel Appointment")
        alert.show()
    }

    private fun deleteAppointment(appointment: Appointment) {
        val sharedPreferences = context.getSharedPreferences("userPrefs", Context.MODE_PRIVATE)
        val username = sharedPreferences.getString("username", null)

        username?.let {
            val userAppointmentRef = database.child("appointments").child(it).child(appointment.id!!)
            val barberAppointmentRef = database.child("barberAppointments").child(appointment.barberName).child(appointment.id!!)

            userAppointmentRef.removeValue().addOnSuccessListener {
                barberAppointmentRef.removeValue().addOnSuccessListener {
                    Toast.makeText(context, "Appointment cancelled.", Toast.LENGTH_SHORT).show()
                    (context as? AppointmentDetailsActivity)?.recreate()
                }.addOnFailureListener {
                    Toast.makeText(context, "Failed to cancel appointment at barber's end.", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                Toast.makeText(context, "Failed to cancel appointment.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showRescheduleDialog(appointment: Appointment) {
        val dialogBuilder = AlertDialog.Builder(context)
        dialogBuilder.setMessage("Do you want to reschedule this appointment?")
            .setCancelable(false)
            .setPositiveButton("Yes") { _, _ -> showDatePickerDialog(appointment) }
            .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }

        val alert = dialogBuilder.create()
        alert.setTitle("Reschedule Appointment")
        alert.show()
    }

    private fun showDatePickerDialog(appointment: Appointment) {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val newDate = "${String.format("%02d", dayOfMonth)}/${String.format("%02d", month + 1)}/${year}"
                showTimePickerDialog(appointment, newDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun showTimePickerDialog(appointment: Appointment, newDate: String) {
        val calendar = Calendar.getInstance()
        val timePickerDialog = TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                val newTime = String.format("%02d:%02d", hourOfDay, minute)
                if (isValidTime(hourOfDay, minute)) {
                    checkAndRescheduleAppointment(appointment, newDate, newTime)
                } else {
                    Toast.makeText(context, "You can only reschedule appointments between 9 AM and 7 PM.", Toast.LENGTH_SHORT).show()
                }
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        )
        timePickerDialog.show()
    }

    private fun isValidTime(hour: Int, minute: Int): Boolean {
        return (hour == 9 || (hour in 10..18) || (hour == 19 && minute == 0))
    }

    private fun checkAndRescheduleAppointment(appointment: Appointment, newDate: String, newTime: String) {
        val selectedTime = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).parse("$newDate $newTime")!!
        if (selectedTime.before(Date())) {
            Toast.makeText(context, "You cannot reschedule to a past time.", Toast.LENGTH_SHORT).show()
            return
        }

        val barberAppointmentsRef = database.child("barberAppointments").child(appointment.barberName)
        barberAppointmentsRef.get().addOnSuccessListener { snapshot ->
            var isConflict = false

            snapshot.children.forEach { appointmentSnapshot ->
                val existingDate = appointmentSnapshot.child("date").getValue(String::class.java)
                val existingTime = appointmentSnapshot.child("time").getValue(String::class.java)

                if (existingDate == newDate && existingTime != null) {
                    val existingDateTime = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).parse("$existingDate $existingTime")!!
                    val bufferEndTime = Calendar.getInstance().apply {
                        time = existingDateTime
                        add(Calendar.MINUTE, 45)
                    }.time

                    if (selectedTime in existingDateTime..bufferEndTime) {
                        isConflict = true
                        return@forEach
                    }
                }
            }

            if (isConflict) {
                Toast.makeText(context, "This barber is already booked during the selected date and time.", Toast.LENGTH_SHORT).show()
            } else {
                updateAppointment(appointment, newDate, newTime)
            }
        }.addOnFailureListener {
            Toast.makeText(context, "Error checking availability.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateAppointment(appointment: Appointment, newDate: String, newTime: String) {
        val sharedPreferences = context.getSharedPreferences("userPrefs", Context.MODE_PRIVATE)
        val username = sharedPreferences.getString("username", null)

        username?.let {
            val userAppointmentRef = database.child("appointments").child(it).child(appointment.id!!)
            val barberAppointmentRef = database.child("barberAppointments").child(appointment.barberName).child(appointment.id!!)

            appointment.date = newDate
            appointment.time = newTime

            userAppointmentRef.setValue(appointment).addOnSuccessListener {
                barberAppointmentRef.setValue(appointment).addOnSuccessListener {
                    Toast.makeText(context, "Appointment rescheduled.", Toast.LENGTH_SHORT).show()
                    (context as? AppointmentDetailsActivity)?.recreate()
                }.addOnFailureListener {
                    Toast.makeText(context, "Failed to reschedule at barber's end.", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                Toast.makeText(context, "Failed to reschedule.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    class AppointmentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val hairstyleNameTextView: TextView = itemView.findViewById(R.id.hairstyleNameTextView)
        private val barberNameTextView: TextView = itemView.findViewById(R.id.barberNameTextView)
        private val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)
        private val timeTextView: TextView = itemView.findViewById(R.id.timeTextView)
        private val priceTextView: TextView = itemView.findViewById(R.id.priceTextView)
        val cancelBtn: Button = itemView.findViewById(R.id.cancelbtn)
        val rescheduleBtn: Button = itemView.findViewById(R.id.reschedulebtn)

        fun bind(appointment: Appointment) {
            hairstyleNameTextView.text = "Hairstyle: ${appointment.hairstyleName}"
            barberNameTextView.text = "Barber: ${appointment.barberName}"
            dateTextView.text = "Date: ${appointment.date}"
            timeTextView.text = "Time: ${appointment.time}"
            priceTextView.text = "Price: ${appointment.price}"
        }
    }
}
