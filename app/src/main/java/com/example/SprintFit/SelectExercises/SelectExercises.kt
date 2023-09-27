package com.example.SprintFit.SelectExercises

import SelectExerciseAdapter
import android.content.Intent
import android.os.Bundle
import android.widget.SearchView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.SprintFit.BaseActivity
import com.example.SquareFrameLayout
import com.example.sevenminutesworkkout.R
import com.example.sevenminutesworkkout.databinding.ActivitySelectExercisesBinding
import com.example.SprintFit.DataBases.ExerciseDatabase.ExerciseEntity
import com.example.SprintFit.DataBases.MyAppDatabase
import com.example.SprintFit.ExercisesInWorkoutPlans.ViewWorkoutPlanExercises
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class SelectExercises : BaseActivity() {

    private lateinit var binding: ActivitySelectExercisesBinding
    private var showAllExercises: List<ExerciseEntity> = listOfNotNull()
    private var filteredExercises: List<ExerciseEntity> = listOf()
    private var workoutPlanID: Int? = null


    private var selectedMuscleCategories: List<String> = listOf()
    private var selectedEquipmentTypes: List<String> = listOf()


    companion object {
        const val EXTRA_WORKOUT_PLAN = "EXTRA_WORKOUT_PLAN"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectExercisesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUIElements()

    }

    private fun setupUIElements() {
        receiveWorkoutPlanID()
        getAllExercisesFromDatabase()
        settingUpSearchbar()

        binding.filterExercises.setOnClickListener {
            val filterFragment = FilterFragment()
            val args = Bundle()
            args.putStringArrayList("selectedMuscleCategories", ArrayList(selectedMuscleCategories))
            args.putStringArrayList("selectedEquipmentTypes", ArrayList(selectedEquipmentTypes))
            args.putInt("exerciseCount", filteredExercises.size)  // Pass the count
            filterFragment.arguments = args
            filterFragment.show(supportFragmentManager, "filterDialog")
        }

        binding.backLogo.setOnClickListener {
            navigateToWorkoutPlanExercises()
        }
        //Make the innersquareframe half the size of the outersquareframe so that the icon is centered
        val innerSquareFrameLayout = findViewById<SquareFrameLayout>(R.id.Inner_Icon_Frame)
        innerSquareFrameLayout.isHalfSize = true

    }

    override fun onBackPressed() {
        navigateToWorkoutPlanExercises()
    }


    private fun navigateToWorkoutPlanExercises(){
     val workoutPlanID = intent.getIntExtra(EXTRA_WORKOUT_PLAN, -1)
     val intent = Intent(this@SelectExercises, ViewWorkoutPlanExercises::class.java)
     intent.putExtra(EXTRA_WORKOUT_PLAN, workoutPlanID)
     startActivity(intent)
 }

    private fun receiveWorkoutPlanID() {
        workoutPlanID = intent.getIntExtra(EXTRA_WORKOUT_PLAN, -1)
        if (workoutPlanID != -1) {
            //Add code here if information couldn't be passed
        }
    }


    private fun settingUpSearchbar() {
        fun searchExercises(query: String?) {
            filteredExercises = showAllExercises.filter {
                it.bodyPart.contains(query ?: "", ignoreCase = true) ||
                        it.name.contains(query ?: "", ignoreCase = true) ||
                        it.target.contains(query ?: "", ignoreCase = true)

            }
            (binding.BrowseExercisesRecylcerView.adapter as SelectExerciseAdapter).updateList(
                filteredExercises
            )

        }
        binding.ExerciseSearchView.setOnClickListener {
            binding.ExerciseSearchView.isIconified = false // This will expand the SearchView
        }
        binding.ExerciseSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                searchExercises(newText)
                return false
            }

        })
    }


    private fun getAllExercisesFromDatabase() {
        lifecycleScope.launch {
            val myAppDatabase: MyAppDatabase = MyAppDatabase.getInstance(this@SelectExercises)
            val workoutPlanWithExercises = withContext(Dispatchers.IO) {
                myAppDatabase.WorkoutPlanWithExercisesDao()
                    .getWorkoutPlanWithExercises(workoutPlanID ?: -1)
            }
            withContext(Dispatchers.IO) {
                val exercisesInWorkoutPlan = workoutPlanWithExercises?.exercises ?: listOf()
                showAllExercises = myAppDatabase.exerciseDao()
                    .getAllExercises()

                filteredExercises = ArrayList(showAllExercises)
                // Mark exercises that are part of the workout plan as checked
                exercisesInWorkoutPlan.forEach { exerciseInWorkoutPlan ->
                    showAllExercises.find { it.id == exerciseInWorkoutPlan.id }
                        ?.let { it.isChecked = true }
                }

                withContext(Dispatchers.Main) {
                    val adapter = SelectExerciseAdapter(
                        showAllExercises,
                        object : SelectExerciseAdapter.OnItemClickListener {
                            override fun onItemClick(position: Int) {
                                // handle item click event
                            }
                        },
                        object : SelectExerciseAdapter.OnItemLongClickListener {
                            override fun onItemLongClick(position: Int): Boolean {
                                // handle item long click event
                                return true
                            }
                        }, workoutPlanID = workoutPlanID!!,
                        myAppDatabase = MyAppDatabase.getInstance(this@SelectExercises),
                        lifecycleScope = lifecycleScope
                    )
                    binding.BrowseExercisesRecylcerView.adapter = adapter
                    val layoutManager = LinearLayoutManager(
                        this@SelectExercises,
                        LinearLayoutManager.VERTICAL,
                        false
                    )
                    binding.BrowseExercisesRecylcerView.layoutManager = layoutManager
                    adapter.notifyDataSetChanged()
                }
            }
        }

    }

    private fun setFilterCountInFragment(count: Int) {
        (supportFragmentManager.findFragmentByTag("filterDialog") as? FilterFragment)?.updateFilterCount(
            count
        )
    }


    fun updateFilteredExercises(
        selectedMuscleCategories: List<String>,
        selectedEquipmentTypes: List<String>
    ) {
        this.selectedMuscleCategories = selectedMuscleCategories
        this.selectedEquipmentTypes = selectedEquipmentTypes

        if (selectedMuscleCategories.isEmpty() && selectedEquipmentTypes.isEmpty()) {
            // If both filter lists are empty, show all exercises
            filteredExercises = showAllExercises
        } else if (selectedMuscleCategories.isNotEmpty() && selectedEquipmentTypes.isNotEmpty()) {
            // If both categories are not empty, use AND logic
            filteredExercises = showAllExercises.filter {
                selectedMuscleCategories.contains(it.bodyPart) && selectedEquipmentTypes.contains(it.equipment)
            }
        } else {
            // If one of the categories is empty, use OR logic
            filteredExercises = showAllExercises.filter {
                selectedMuscleCategories.contains(it.bodyPart) || selectedEquipmentTypes.contains(it.equipment)
            }
        }

        setFilterCountInFragment(filteredExercises.size)
        (binding.BrowseExercisesRecylcerView.adapter as? SelectExerciseAdapter)?.updateList(
            filteredExercises
        )
    }


}