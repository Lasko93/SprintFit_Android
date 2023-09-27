package com.example.SprintFit.SelectExercises

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.example.sevenminutesworkkout.R
import com.example.SprintFit.DataBases.MyAppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FilterFragment : DialogFragment() {

    private lateinit var muscleCategories: MutableList<MuscleCategory>
    private lateinit var equipmentTypes: MutableList<EquipmentType>
    private lateinit var myAppDatabase: MyAppDatabase
    private lateinit var exerciseCountTextView: TextView


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.fragment_filter, null)


        exerciseCountTextView = view.findViewById(R.id.TV_Exercise_Count)

        val exerciseCount = arguments?.getInt("exerciseCount", 0) ?: 0
        exerciseCountTextView.text = "Filtered Exercises: $exerciseCount"


        myAppDatabase = MyAppDatabase.getInstance(requireContext())

        builder.setView(view)
            .setPositiveButton("Select") { _, _ ->

            }
            .setNegativeButton("reset") { _, _ ->
                resetFilters()
            }


        val dialog = builder.create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK)
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK)
        }


        // Fetch distinct muscle categories and equipment types from the database
        fetchFilterDataFromDatabase(view)

        val selectedMuscleCat =
            arguments?.getStringArrayList("selectedMuscleCategories") ?: listOf()
        val selectedEquipTypes = arguments?.getStringArrayList("selectedEquipmentTypes") ?: listOf()

        return dialog
    }

    private fun resetFilters() {
        // Reset the selections in muscleCategories and equipmentTypes
        muscleCategories.forEach { it.isSelected = false }
        equipmentTypes.forEach { it.isSelected = false }

        // Update the exercises to reflect the reset filters
        updateFilteredExercises()
    }

    private fun fetchFilterDataFromDatabase(view: View) {
        lifecycleScope.launch {
            val distinctBodyParts = withContext(Dispatchers.IO) {
                myAppDatabase.exerciseDao().getAllBodyParts()
            }
            val distinctEquipments = withContext(Dispatchers.IO) {
                myAppDatabase.exerciseDao().getAllEquipments()
            }

            // Initialize muscleCategories and equipmentTypes here
            muscleCategories = distinctBodyParts.map { MuscleCategory(it) }.toMutableList()
            equipmentTypes = distinctEquipments.map { EquipmentType(it) }.toMutableList()

            val selectedMuscleCat =
                arguments?.getStringArrayList("selectedMuscleCategories") ?: listOf()
            val selectedEquipTypes =
                arguments?.getStringArrayList("selectedEquipmentTypes") ?: listOf()

            // Now populate your ListViews
            populateListViews(view, selectedMuscleCat, selectedEquipTypes)
        }
    }

    fun updateFilterCount(count: Int) {
        exerciseCountTextView.text = "Filtered Exercises: $count"
    }


    private fun populateListViews(
        view: View,
        selectedMuscleCategories: List<String>,
        selectedEquipmentTypes: List<String>
    ) {
        val muscleAdapter = ArrayAdapter(
            requireContext(),
            R.layout.custom_checked_item,
            muscleCategories.map { it.name })
        val equipmentAdapter = ArrayAdapter(
            requireContext(),
            R.layout.custom_checked_item,
            equipmentTypes.map { it.name })

        val muscleListView: ListView = view.findViewById(R.id.muscleCategoryList)
        val equipmentListView: ListView = view.findViewById(R.id.equipmentTypeList)

        muscleListView.adapter = muscleAdapter
        equipmentListView.adapter = equipmentAdapter

        muscleListView.choiceMode = ListView.CHOICE_MODE_MULTIPLE
        equipmentListView.choiceMode = ListView.CHOICE_MODE_MULTIPLE

        muscleListView.setOnItemClickListener { _, _, position, _ ->
            muscleCategories[position].isSelected = !muscleCategories[position].isSelected
            updateFilteredExercises()
        }

        equipmentListView.setOnItemClickListener { _, _, position, _ ->
            equipmentTypes[position].isSelected = !equipmentTypes[position].isSelected
            updateFilteredExercises()
        }

        muscleCategories.forEachIndexed { index, muscleCategory ->
            if (selectedMuscleCategories.contains(muscleCategory.name)) {
                muscleListView.setItemChecked(index, true)
                muscleCategory.isSelected = true
            }
        }

        equipmentTypes.forEachIndexed { index, equipmentType ->
            if (selectedEquipmentTypes.contains(equipmentType.name)) {
                equipmentListView.setItemChecked(index, true)
                equipmentType.isSelected = true
            }
        }
    }


    private fun updateFilteredExercises() {
        val selectedMuscleCategories = muscleCategories.filter { it.isSelected }.map { it.name }
        val selectedEquipmentTypes = equipmentTypes.filter { it.isSelected }.map { it.name }


        (activity as? SelectExercises)?.updateFilteredExercises(
            selectedMuscleCategories,
            selectedEquipmentTypes
        )
    }
}

data class MuscleCategory(val name: String, var isSelected: Boolean = false)
data class EquipmentType(val name: String, var isSelected: Boolean = false)
