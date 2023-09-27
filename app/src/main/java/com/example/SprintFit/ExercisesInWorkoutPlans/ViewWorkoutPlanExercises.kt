package com.example.SprintFit.ExercisesInWorkoutPlans

import android.content.Intent
import android.os.Bundle
import android.widget.FrameLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.SprintFit.BaseActivity
import com.example.sevenminutesworkkout.R
import com.example.sevenminutesworkkout.databinding.ActivityViewWorkoutPlanExercisesBinding
import com.example.SprintFit.DataBases.MyAppDatabase
import com.example.SprintFit.DataBases.WorkoutPlansWithExercises.WorkoutPlanWithExercisesDao
import com.example.SprintFit.MainActivity
import com.example.SprintFit.SelectExercises.SelectExercises
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class ViewWorkoutPlanExercises : BaseActivity() {
    private lateinit var binding: ActivityViewWorkoutPlanExercisesBinding
    private var workoutPlanId: Int = -1
    private lateinit var adapter: ViewWorkoutPlanAdapter
    private lateinit var workoutPlanWithExercisesDao: WorkoutPlanWithExercisesDao

    companion object {
        const val EXTRA_WORKOUT_PLAN = "EXTRA_WORKOUT_PLAN"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewWorkoutPlanExercisesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initUi()

    }


    private fun initUi() {
        setupExerciseAddButton()
        setupTheExercises()
        setupItemTouchHelper()

        binding.logoToTitleScreen.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

    }
    private fun setupItemTouchHelper() {
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN,
            ItemTouchHelper.LEFT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val fromPosition = viewHolder.bindingAdapterPosition
                val toPosition = target.bindingAdapterPosition
                adapter.swapItems(fromPosition, toPosition)
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        workoutPlanWithExercisesDao.updateWorkoutPlanWithExercises(adapter.getWorkoutPlanWithExercises())
                    }
                }
                return true
            }


            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.bindingAdapterPosition
                val exerciseId = adapter.getWorkoutPlanWithExercises().exercises[position].id

                // Remove the item from the adapter
                adapter.deleteItem(position)

                // Launch coroutine to delete the item from the database
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        workoutPlanWithExercisesDao.deleteExerciseFromWorkoutPlan(
                            workoutPlanId,
                            exerciseId
                        )
                    }
                }
            }
        }

        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(binding.WorkoutPlanExercisesRecycler)

    }


    private fun setupExerciseAddButton() {
        val addExerciseToPlanButton: FrameLayout = findViewById(R.id.AddExercisesButton)
        addExerciseToPlanButton.setOnClickListener {
            val intent = Intent(this@ViewWorkoutPlanExercises, SelectExercises::class.java)
            intent.putExtra("EXTRA_WORKOUT_PLAN", workoutPlanId)
            startActivity(intent)
        }
    }

    private fun setupTheExercises() {
        lifecycleScope.launch {
            val workoutPlanWithExercises = withContext(Dispatchers.IO) {
                workoutPlanId = intent.getIntExtra(EXTRA_WORKOUT_PLAN, -1)
                val db = MyAppDatabase.getInstance(this@ViewWorkoutPlanExercises)
                workoutPlanWithExercisesDao = db.WorkoutPlanWithExercisesDao()
                workoutPlanWithExercisesDao.getWorkoutPlanWithExercisesOrdered(workoutPlanId)
            }

            // Check if workoutPlanWithExercises is not null
            workoutPlanWithExercises?.let {
                // Initialize the adapter
                adapter =
                    ViewWorkoutPlanAdapter(it, object : ViewWorkoutPlanAdapter.OnItemClickListener {
                        override fun onItemClick(position: Int) {
                            // Handle click event here
                        }
                    }, object : ViewWorkoutPlanAdapter.OnItemLongClickListener {
                        override fun onItemLongClick(position: Int): Boolean {
                            // Handle long click event here
                            return true
                        }
                    })

                binding.WorkoutPlanExercisesRecycler.adapter = adapter
                binding.WorkoutPlanExercisesRecycler.layoutManager =
                    LinearLayoutManager(this@ViewWorkoutPlanExercises)
                binding.TitleWorkoutPlan.text = workoutPlanWithExercises.workoutplan.title
            }
        }
    }


}

