package com.omkara.sirenservices_internal.fragments

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.omkara.sirenservices_internal.R
import com.omkara.sirenservices_internal.models.AttendanceModel
import android.view.*
import android.widget.*
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.applandeo.materialcalendarview.CalendarView
import com.applandeo.materialcalendarview.EventDay
import com.omkara.sirenservices_internal.adapter.AttendanceAdapter
import com.omkara.sirenservices_internal.viewmodels.UserDetailsViewModel
import java.text.SimpleDateFormat
import java.util.*

class UserAttendanceFragment : Fragment() {

    private val vm: UserDetailsViewModel by activityViewModels()
    private lateinit var userId: String

    private lateinit var calendar: CalendarView
    private lateinit var txtMonth: TextView
    private lateinit var txtStats: TextView
    private lateinit var adapter: AttendanceAdapter

    private var currentMonth = Calendar.getInstance()

    companion object {
        fun newInstance(userId: String): UserAttendanceFragment {
            val f = UserAttendanceFragment()
            f.arguments = Bundle().apply { putString("USER_ID", userId) }
            return f
        }
    }

    override fun onCreate(savedInstance: Bundle?) {
        super.onCreate(savedInstance)
        userId = arguments?.getString("USER_ID") ?: ""
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, saved: Bundle?): View {
        return inflater.inflate(R.layout.fragment_user_attendance, container, false)
    }

    override fun onViewCreated(v: View, saved: Bundle?) {

        calendar = v.findViewById(R.id.calendarView)
        txtMonth = v.findViewById(R.id.txtMonth)
        txtStats = v.findViewById(R.id.txtStats)

        val rv = v.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.recyclerAttendance)
        rv.layoutManager = LinearLayoutManager(requireContext())
        adapter = AttendanceAdapter(emptyList())
        rv.adapter = adapter

        updateMonthHeader()
        loadAttendance()




        v.findViewById<ImageButton>(R.id.btnPrevMonth).setOnClickListener {
            currentMonth.add(Calendar.MONTH, -1)
            updateMonthHeader()
            loadAttendance()
        }

        v.findViewById<ImageButton>(R.id.btnNextMonth).setOnClickListener {
            currentMonth.add(Calendar.MONTH, 1)
            updateMonthHeader()
            loadAttendance()
        }

        v.findViewById<Button>(R.id.btnMarkPresent).setOnClickListener {
            vm.markAttendance(userId = userId,
                yearMonth = todayMonth(),
                date = todayDate(),
                status = "present")
        }

        v.findViewById<Button>(R.id.btnMarkAbsent).setOnClickListener {
            vm.markAttendance(userId = userId,
                yearMonth = todayMonth(),
                date = todayDate(),
                status = "absent")
        }

        v.findViewById<Button>(R.id.btnMarkLeave).setOnClickListener {
            showLeaveSelectorDialog()
        }

        vm.attendanceMonthly.observe(viewLifecycleOwner) { list ->
            adapter.update(list)
            showCalendarMarkers(list)
            updateStats(list)
        }
    }

    private fun todayDate(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    private fun todayMonth(): String =
        SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())

    private fun updateMonthHeader() {
        txtMonth.text = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(currentMonth.time)
    }

    private fun loadAttendance() {
        val fmt = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        vm.loadMonthlyAttendance(userId, fmt.format(currentMonth.time))
    }

    private fun today(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    private fun showCalendarMarkers(list: List<AttendanceModel>) {
        val events = mutableListOf<EventDay>()

        list.forEach {
            val cal = Calendar.getInstance()
            val parts = it.date.split("-")
            cal.set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt())

            val icon = when (it.status) {
                "present" -> R.drawable.ic_att_present
                "leave" -> R.drawable.ic_att_leave
                else -> R.drawable.ic_att_absent
            }

            events.add(EventDay(cal, icon))
        }

        calendar.setEvents(events)
    }

    private fun updateStats(list: List<AttendanceModel>) {
        val present = list.count { it.status == "present" }
        val absent = list.count { it.status == "absent" }
        val leave = list.count { it.status == "leave" }

        txtStats.text = "Present: $present | Absent: $absent | Leave: $leave"
    }

    private fun showLeaveSelectorDialog() {
        val types = arrayOf("Sick Leave", "Casual Leave", "Paid Leave", "Other")

        AlertDialog.Builder(requireContext())
            .setTitle("Select Leave Type")
            .setItems(types) { _, p ->
                vm.markAttendance(userId = userId,
                    yearMonth = todayMonth(),
                    date = todayDate(), "leave:${types[p]}")
            }
            .show()
    }
}
