package com.omkara.sirenservices_internal.fragments

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.textfield.TextInputLayout
import com.omkara.sirenservices_internal.R
import com.omkara.sirenservices_internal.adapter.SalaryEntryAdapter
import com.omkara.sirenservices_internal.models.SalaryEntry
import com.omkara.sirenservices_internal.models.SalarySummary
import com.omkara.sirenservices_internal.viewmodels.UserDetailsViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

class UserSalaryFragment : Fragment() {

    private val vm: UserDetailsViewModel by activityViewModels()
    private val firestore = FirebaseFirestore.getInstance()

    private lateinit var adapter: SalaryEntryAdapter
    private lateinit var userId: String

    private lateinit var txtNet: TextView
    private lateinit var txtSummary: TextView
    private lateinit var monthPicker: AutoCompleteTextView
    private lateinit var btnAdd: Button

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    companion object {
        fun newInstance(userId: String): UserSalaryFragment {
            return UserSalaryFragment().apply {
                arguments = Bundle().apply { putString("USER_ID", userId) }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userId = arguments?.getString("USER_ID") ?: ""
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, saved: Bundle?): View {
        return inflater.inflate(R.layout.fragment_user_salary, container, false)
    }

    override fun onViewCreated(v: View, saved: Bundle?) {

        txtNet = v.findViewById(R.id.txtNetSalary)
        txtSummary = v.findViewById(R.id.txtSummaryDetails)
        monthPicker = v.findViewById(R.id.autoMonthPicker)
        btnAdd = v.findViewById(R.id.btnAddEntry)

        val rv = v.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.recyclerSalary)
        rv.layoutManager = LinearLayoutManager(requireContext())
        adapter = SalaryEntryAdapter(emptyList())
        rv.adapter = adapter

        setupMonthSelector()

        btnAdd.setOnClickListener { showAddMultiEntryDialog() }

        // Observe entries
        vm.salaryEntries.observe(viewLifecycleOwner) {
            adapter.update(it)
        }

        // Observe summary
        vm.salarySummary.observe(viewLifecycleOwner) { s: SalarySummary ->
            txtNet.text = "₹${s.netSalary}"
            txtSummary.text =
                "Base: ₹${s.totalBase} | Allowance: ₹${s.totalAllowances} | Bonus: ₹${s.totalBonus}\n" +
                        "Overtime: ₹${s.totalOvertime} | Deductions: ₹${s.totalDeductions}"
        }
    }

    // ----------------------------------------------------------------------------------
    // MONTH SELECTOR — FIXED
    // ----------------------------------------------------------------------------------
    private fun setupMonthSelector() {
        val fmt = SimpleDateFormat("yyyy-MM", Locale.getDefault())

        val now = Calendar.getInstance()
        val months = mutableListOf<String>()

        repeat(12) {
            months.add(fmt.format(now.time))
            now.add(Calendar.MONTH, -1)
        }

        scope.launch {

            // fetch paid status for month
            val labeledMonths = months.map { ym ->
                async(Dispatchers.IO) {
                    val doc = firestore.collection("users")
                        .document(userId)
                        .collection("salary")
                        .document(ym)
                        .get()
                        .await()

                    val paid = doc.getBoolean("isPaid") ?: false
                    if (paid) "$ym (PAID)" else ym
                }
            }.awaitAll()

            // attach adapter to dropdown
            val monthAdapter =
                ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, labeledMonths)

            monthPicker.setAdapter(monthAdapter)

            // default = current month
            monthPicker.setText(labeledMonths[0], false)

            // load entries
            vm.loadMonthlyEntries(userId, months[0])

            monthPicker.setOnItemClickListener { _, _, pos, _ ->
                val actualMonth = months[pos] // without (PAID)
                vm.loadMonthlyEntries(userId, actualMonth)
            }
        }
    }

    // ----------------------------------------------------------------------------------
    // SALARY MULTI-ENTRY DIALOG — FIXED
    // ----------------------------------------------------------------------------------
    private fun showAddMultiEntryDialog() {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_salary_multi_entry)
        dialog.setCancelable(true)

        val txtMonth = dialog.findViewById<TextView>(R.id.txtDialogMonth)
        val container = dialog.findViewById<LinearLayout>(R.id.containerRows)
        val btnAddRow = dialog.findViewById<Button>(R.id.btnAddRow)
        val btnSave = dialog.findViewById<Button>(R.id.btnSaveDialog)
        val btnCancel = dialog.findViewById<Button>(R.id.btnCancelDialog)

        // Selected month (strip "(PAID)")
        val selectedTxt = monthPicker.text.toString()
        val yearMonth = selectedTxt.split(" ")[0]

        txtMonth.text = "Month: $yearMonth"

        // --- FUNCTION: add one row ---
        fun addRow(defaultType: String? = null) {
            val row = layoutInflater.inflate(R.layout.view_salary_entry_row, container, false)

            val autoType = row.findViewById<AutoCompleteTextView>(R.id.autoType)
            val edtAmount = row.findViewById<EditText>(R.id.edtAmount)
            val edtNote = row.findViewById<EditText>(R.id.edtNote)
            val btnRemove = row.findViewById<ImageButton>(R.id.btnRemoveRow)

            val types = listOf("base", "allowance", "bonus", "overtime", "deduction", "fine")
            autoType.setAdapter(
                ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, types)
            )

            if (defaultType != null)
                autoType.setText(defaultType, false)

            // Remove row
            btnRemove.setOnClickListener {
                container.removeView(row)
            }

            container.addView(row)
        }

        // default first row
        addRow("base")

        btnAddRow.setOnClickListener { addRow(null) }

        btnCancel.setOnClickListener { dialog.dismiss() }

        btnSave.setOnClickListener {

            val list = mutableListOf<SalaryEntry>()

            for (i in 0 until container.childCount) {

                val row = container.getChildAt(i)

                val type = row.findViewById<AutoCompleteTextView>(R.id.autoType)
                    .text.toString().trim()

                val amount = row.findViewById<EditText>(R.id.edtAmount)
                    .text.toString().toDoubleOrNull() ?: 0.0

                val note = row.findViewById<EditText>(R.id.edtNote)
                    .text.toString().trim()

                if (type.isEmpty()) {
                    Toast.makeText(requireContext(), "Select type in row ${i + 1}", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                if (amount == 0.0) {
                    Toast.makeText(requireContext(), "Enter amount in row ${i + 1}", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val finalAmount =
                    if (type == "deduction" || type == "fine") -Math.abs(amount) else amount

                list.add(
                    SalaryEntry(
                        id = "",
                        type = type,
                        amount = finalAmount,
                        note = note,
                        timestamp = System.currentTimeMillis()
                    )
                )
            }

            if (list.isEmpty()) {
                Toast.makeText(requireContext(), "Add at least one entry", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            vm.addSalaryEntriesBatch(userId, yearMonth, list)

            Toast.makeText(requireContext(), "Saved", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.show()
    }
}
