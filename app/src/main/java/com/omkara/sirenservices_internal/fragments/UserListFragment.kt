package com.omkara.sirenservices_internal.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import com.omkara.sirenservices_internal.R
import com.omkara.sirenservices_internal.adapters.UserAdapter
import com.omkara.sirenservices_internal.viewmodels.UserListViewModel
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.omkara.sirenservices_internal.activities.UserDetailsActivity

class UserListFragment : Fragment(R.layout.fragment_user_list) {

    private val vm: UserListViewModel by viewModels()
    private lateinit var adapter: UserAdapter
    private lateinit var swipeRefresh: SwipeRefreshLayout

    override fun onViewCreated(v: View, savedInstanceState: Bundle?) {
        super.onViewCreated(v, savedInstanceState)

        val edtSearch = v.findViewById<TextInputEditText>(R.id.edtSearchUser)
        val chipGroup = v.findViewById<ChipGroup>(R.id.chipGroupFilters)
        val rv = v.findViewById<RecyclerView>(R.id.recyclerUsers)
        swipeRefresh = v.findViewById(R.id.swipeRefresh)

        // -----------------------------
        // Recycler Setup
        // -----------------------------
        adapter = UserAdapter { user ->
            // TODO: OPEN USER DETAILS PAGE
             val i = Intent(requireContext(), UserDetailsActivity::class.java)
             i.putExtra("USER_ID", user.id)
             startActivity(i)
        }

        rv.adapter = adapter
        rv.layoutManager = GridLayoutManager(requireContext(), 2)

        // -----------------------------
        // VM Observer
        // -----------------------------
        vm.users.observe(viewLifecycleOwner) { list ->
            swipeRefresh.isRefreshing = false
            if (list != null) {
                adapter.submitList(list)
            }
        }

        vm.loadUsers()  // initial load with cache

        // -----------------------------
        // Search Filtering
        // -----------------------------
        edtSearch.addTextChangedListener { text ->
            updateFilter(text.toString(), chipGroup)
        }

        // -----------------------------
        // Chip Filtering
        // -----------------------------
        chipGroup.setOnCheckedStateChangeListener { _, _ ->
            updateFilter(edtSearch.text.toString(), chipGroup)
        }

        // --------------------------------
        // Pull to Refresh
        // --------------------------------
        swipeRefresh.setOnRefreshListener {
            vm.loadUsers(forceRefresh = true)
        }

        // --------------------------------
        // Refresh when scrolled to bottom
        // --------------------------------
        rv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                if (dy > 0) { // scroll down
                    val lm = recyclerView.layoutManager as GridLayoutManager
                    val lastVisible = lm.findLastCompletelyVisibleItemPosition()
                    val total = adapter.itemCount

                    if (lastVisible == total - 1) {
                        vm.loadUsers(forceRefresh = true)
                    }
                }
            }
        })
    }

    // ------------------------------------------------
    // Filtering Logic: search + availability + status
    // ------------------------------------------------
    private fun updateFilter(query: String, chips: ChipGroup) {

        val filters = mutableSetOf<String>()

        if (chips.findViewById<Chip>(R.id.chipAvailable).isChecked) filters.add("available")
        if (chips.findViewById<Chip>(R.id.chipOccupied).isChecked) filters.add("occupied")
        if (chips.findViewById<Chip>(R.id.chipActive).isChecked) filters.add("active")
        if (chips.findViewById<Chip>(R.id.chipInactive).isChecked) filters.add("inactive")

        adapter.filter(query, filters)
    }
}
