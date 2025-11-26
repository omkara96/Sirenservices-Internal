package com.omkara.sirenservices_internal.fragments

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.applandeo.materialcalendarview.CalendarView
import com.applandeo.materialcalendarview.CalendarDay
import com.applandeo.materialcalendarview.listeners.OnCalendarDayClickListener
import com.omkara.sirenservices_internal.R
import com.omkara.sirenservices_internal.adapter.AttendanceAdapter
import com.omkara.sirenservices_internal.models.AttendanceModel
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
    private lateinit var loadingOverlay: View

    private var currentMonth = Calendar.getInstance()

    companion object {
        fun newInstance(userId: String): UserAttendanceFragment {
            val f = UserAttendanceFragment()
            f.arguments = Bundle().apply { putString("USER_ID", userId) }
            return f
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userId = arguments?.getString("USER_ID") ?: ""
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, saved: Bundle?): View {
        return inflater.inflate(R.layout.fragment_user_attendance, container, false)
    }

    override fun onViewCreated(v: View, saved: Bundle?) {

        calendar = v.findViewById(R.id.calendarView)
        txtMonth = v.findViewById(R.id.txtMonth)
        txtStats = v.findViewById(R.id.txtStats)
        loadingOverlay = v.findViewById(R.id.loadingOverlay)


        val rv = v.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.recyclerAttendance)
        rv.layoutManager = LinearLayoutManager(requireContext())
        adapter = AttendanceAdapter(emptyList())
        rv.adapter = adapter

        updateMonthHeader()
        loadAttendance()

        // NEXT/PREV month selector
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

        // Today attendance
        v.findViewById<Button>(R.id.btnMarkPresent).setOnClickListener {
            vm.markAttendance(userId, todayMonth(), todayDate(), "present")
        }

        v.findViewById<Button>(R.id.btnMarkAbsent).setOnClickListener {
            vm.markAttendance(userId, todayMonth(), todayDate(), "absent")
        }

        v.findViewById<Button>(R.id.btnMarkLeave).setOnClickListener {
            showLeaveSelectorDialog(todayYearMonth(), todayDate())
        }

        // ⭐ UPDATED: correct listener to allow past-day selection
        calendar.setOnCalendarDayClickListener(object : OnCalendarDayClickListener {
            override fun onClick(day: CalendarDay) {

                val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(day.calendar.time)
                val ym = date.substring(0, 7)

                val today = Date()

                if (day.calendar.time.after(today)) {
                    // FUTURE → only allow leave
                    showLeaveSelectorDialog(ym, date)
                } else {
                    // PAST or TODAY → allow full attendance options
                    showDayActionDialog(ym, date)
                }
            }
        })

        vm.attendanceMonthly.observe(viewLifecycleOwner) { list ->
            hideLoading()
            adapter.update(list)
            showCalendarMarkers(list)
            updateStats(list)
        }
    }

    private fun todayDate(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    private fun todayMonth(): String =
        SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date()).substring(0, 7)

    private fun todayYearMonth() = todayMonth()

    private fun updateMonthHeader() {
        txtMonth.text = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(currentMonth.time)
    }

    private fun loadAttendance() {
        showLoading()
        val fmt = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        vm.loadMonthlyAttendance(userId, fmt.format(currentMonth.time))
    }

    // ---------------------------
    // Calendar marking
    // ---------------------------
    private fun showCalendarMarkers(list: List<AttendanceModel>) {

        val events = list.map {
            val cal = Calendar.getInstance()
            val p = it.date.split("-")
            cal.set(p[0].toInt(), p[1].toInt() - 1, p[2].toInt())

            val icon = when {
                it.status == "present" -> R.drawable.ic_att_present
                it.status.startsWith("leave") -> R.drawable.ic_att_leave
                it.status == "absent" -> R.drawable.ic_att_absent
                else -> R.drawable.ic_att_absent
            }

            CalendarDay(cal).apply { imageResource = icon }
        }

        calendar.setCalendarDays(events)
    }

    private fun updateStats(list: List<AttendanceModel>) {
        val present = list.count { it.status == "present" }
        val absent = list.count { it.status == "absent" }
        val leave = list.count { it.status.startsWith("leave") }

        txtStats.text = "Present: $present  |  Absent: $absent  |  Leave: $leave"
    }

    // ----------------------------------------------
    // Dialog for attendance marking on PAST days
    // ----------------------------------------------
    private fun showDayActionDialog(yearMonth: String, date: String) {
        val options = arrayOf("Present", "Absent", "Leave")

        AlertDialog.Builder(requireContext())
            .setTitle("Mark attendance for $date")
            .setItems(options) { _, p ->
                when (p) {
                    0 -> vm.markAttendance(userId, yearMonth, date, "present")
                    1 -> vm.markAttendance(userId, yearMonth, date, "absent")
                    2 -> showLeaveSelectorDialog(yearMonth, date)
                }
            }.show()
    }

    // ----------------------------------------------
    // Leave type dialog
    // ----------------------------------------------
    private fun showLeaveSelectorDialog(yearMonth: String, date: String) {
        val types = arrayOf("Sick Leave", "Casual Leave", "Paid Leave", "Other")

        AlertDialog.Builder(requireContext())
            .setTitle("Leave type for $date")
            .setItems(types) { _, p ->
                vm.markAttendance(userId, yearMonth, date, "leave:${types[p]}")
            }.show()
    }

    private fun showLoading() {
        loadingOverlay.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        loadingOverlay.visibility = View.GONE
    }

}
