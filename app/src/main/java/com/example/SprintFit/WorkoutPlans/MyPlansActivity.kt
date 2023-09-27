package com.example.SprintFit.WorkoutPlans

import WorkoutPlanAdapter
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.SprintFit.BaseActivity
import com.example.sevenminutesworkkout.R
import com.example.sevenminutesworkkout.databinding.ActivityMyPlansBinding
import com.example.SprintFit.DataBases.MyAppDatabase
import com.example.SprintFit.DataBases.WorkoutPlansWithExercises.WorkoutPlanExerciseCrossRef
import com.example.SprintFit.DataBases.WorkoutPlansWithExercises.WorkoutPlanWithExercises
import com.example.SprintFit.ExercisesInWorkoutPlans.ViewWorkoutPlanExercises
import com.example.SprintFit.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Collections

class MyPlansActivity : BaseActivity(), WorkoutPlanAdapter.WorkoutPlanOptionsClickListener {


    private var binding: ActivityMyPlansBinding? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var workoutPlanAdapter: WorkoutPlanAdapter
    private var workoutPlansWithExercises: List<WorkoutPlanWithExercises>? = null


    companion object {
        const val EXTRA_WORKOUT_PLAN = "EXTRA_WORKOUT_PLAN"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyPlansBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        recyclerView = findViewById(R.id.recyclerView)


       setupBindings()
       setupItemTouchHelper()


    }


    private fun setupBindings() {
        binding?.myplansclickable!!.setOnClickListener {
            val intent = Intent(this@MyPlansActivity, CreateNewWorkoutPlan::class.java)
            startActivity(intent)

        }

        binding!!.logoToTitleScreen.setOnClickListener {
            val intent = Intent(this@MyPlansActivity, MainActivity::class.java)
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
                // Get positions
                val fromPos = viewHolder.adapterPosition
                val toPos = target.adapterPosition

                // Swap items in the list
                Collections.swap(workoutPlansWithExercises, fromPos, toPos)

                // Notify the adapter
                workoutPlanAdapter.notifyItemMoved(fromPos, toPos)

                // Update the database with new order
                updateWorkoutPlanOrderInDatabase(fromPos, toPos)

                return true
            }


            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position =
                    viewHolder.bindingAdapterPosition // get the position of the swiped item

                // Show confirmation dialog
                val dialog = AlertDialog.Builder(this@MyPlansActivity, R.style.MyAlertDialogStyle)
                    .setTitle("Delete Plan")
                    .setMessage("Are you sure you want to delete this workout?")
                    // Set up the buttons
                    .setPositiveButton("Yes") { dialog, _ ->
                        deleteSelectedWorkoutPlan(position) // call your method to delete the item
                        workoutPlanAdapter.notifyItemRemoved(position) // notify the adapter about the removed item
                        dialog.dismiss()
                    }
                    .setNegativeButton("No") { dialog, _ ->
                        workoutPlanAdapter.notifyItemChanged(position) // Reset the swiped item
                        dialog.cancel()
                    }
                    // Show the dialog
                    .show()

                dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    .setTextColor(Color.BLACK) // Change to your desired color
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                    .setTextColor(Color.BLACK) // Change to your desired color

            }
        }


        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)



    }
    override fun onResume() {
        super.onResume()
        loadWorkoutPlans()

    }

    override fun onBackPressed() {

        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)

    }

    private fun setActiveWorkoutPlan(position: Int) {
        lifecycleScope.launch(Dispatchers.IO) {
            val selectedWorkoutPlan = workoutPlansWithExercises?.get(position)?.workoutplan
            val myAppDatabase = MyAppDatabase.getInstance(applicationContext)
            val workoutPlanDao = myAppDatabase.WorkoutPlanDao()

            // First, set all workout plans to inactive
            workoutPlanDao.setAllWorkoutPlansInactive()

            // Then, set the selected workout plan to active
            if (selectedWorkoutPlan != null) {
                workoutPlanDao.setActiveWorkoutPlan(selectedWorkoutPlan.workoutPlanID, true)
            }

            // Refresh the workout plan list
            withContext(Dispatchers.Main) {
                loadWorkoutPlans()
                // Display the Toast message
                selectedWorkoutPlan?.title?.let { title ->
                    Toast.makeText(
                        this@MyPlansActivity,
                        "$title has been set active!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }


    private fun deleteSelectedWorkoutPlan(selectedPosition: Int) {
        val selectedWorkoutPlanWithExercises =
            workoutPlansWithExercises?.get(selectedPosition)?.workoutplan
        if (selectedWorkoutPlanWithExercises != null) {
            lifecycleScope.launch(Dispatchers.IO) {
                val workoutPlanDao = MyAppDatabase.getInstance(applicationContext).WorkoutPlanDao()
                workoutPlanDao.deleteWorkoutPlan(selectedWorkoutPlanWithExercises)

                withContext(Dispatchers.Main) {
                    loadWorkoutPlans() // Refresh the workout plan list
                }
            }
        }
    }


    private fun editSelectedWorkoutPlan(selectedPosition: Int) {
        val selectedWorkoutPlan = workoutPlansWithExercises?.get(selectedPosition)?.workoutplan
        if (selectedWorkoutPlan != null) {
            val intent = Intent(this@MyPlansActivity, CreateNewWorkoutPlan::class.java)
            intent.putExtra(EXTRA_WORKOUT_PLAN, selectedWorkoutPlan)
            startActivity(intent)
        }
    }


    private fun loadWorkoutPlans() {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val workoutPlanWithExercisesDao =
                    MyAppDatabase.getInstance(applicationContext).WorkoutPlanWithExercisesDao()
                workoutPlansWithExercises =
                    workoutPlanWithExercisesDao.getAllWorkoutPlansWithExercisesOrdered()

                workoutPlanAdapter =
                    WorkoutPlanAdapter(workoutPlansWithExercises!!, this@MyPlansActivity)

                runOnUiThread {
                    recyclerView.apply {
                        layoutManager = LinearLayoutManager(this@MyPlansActivity)
                        adapter = workoutPlanAdapter
                    }
                }
            }
        }
    }


    private fun copySelectedWorkoutPlan(selectedPosition: Int) {
        val selectedWorkoutPlan = workoutPlansWithExercises?.get(selectedPosition)?.workoutplan
        if (selectedWorkoutPlan != null) {
            lifecycleScope.launch(Dispatchers.IO) {
                val myAppDatabase = MyAppDatabase.getInstance(applicationContext)
                val workoutPlanDao = myAppDatabase.WorkoutPlanDao()
                val workoutPlanWithExercisesDao = myAppDatabase.WorkoutPlanWithExercisesDao()

                // Fetch the highest workoutplanid
                val lastWorkoutPlanId = workoutPlanDao.getHighestWorkoutPlanId() ?: 0

                // Create a new workout plan object based on the selected one, with a new ID
                val newWorkoutPlan = selectedWorkoutPlan.copy(
                    workoutPlanID = lastWorkoutPlanId + 1,
                    currentlyActive = false
                )

                // Insert the new workout plan into the database
                workoutPlanDao.insertOne(newWorkoutPlan)

                // Copy the exercises associated with the original workout plan
                val originalExercises =
                    workoutPlanWithExercisesDao.getWorkoutPlanWithExercises(selectedWorkoutPlan.workoutPlanID)
                originalExercises?.exercises?.forEachIndexed { order, exercise ->
                    val crossRef = WorkoutPlanExerciseCrossRef(
                        newWorkoutPlan.workoutPlanID,
                        exercise.id,
                        order
                    )
                    workoutPlanWithExercisesDao.insertExerciseToWorkoutPlan(crossRef)
                }

                // Refresh the workout plan list
                withContext(Dispatchers.Main) {
                    loadWorkoutPlans()

                }
            }
        }
    }

    private fun updateWorkoutPlanOrderInDatabase(fromPos: Int, toPos: Int) {
        lifecycleScope.launch(Dispatchers.IO) {
            val myAppDatabase = MyAppDatabase.getInstance(applicationContext)
            val workoutPlanDao = myAppDatabase.WorkoutPlanDao()

            // Loop through the reordered list and update the order in the database
            workoutPlansWithExercises?.forEachIndexed { index, workoutPlanWithExercises ->
                workoutPlanDao.updateOrder(
                    workoutPlanWithExercises.workoutplan.workoutPlanID,
                    index
                )
            }

        }
    }


    override fun onEdit(position: Int) {
        editSelectedWorkoutPlan(position)
    }

    override fun onCopy(position: Int) {
        copySelectedWorkoutPlan(position)
    }

    override fun onAddExercises(position: Int) {
        val selectedWorkoutPlan =
            workoutPlansWithExercises?.get(position)?.workoutplan?.workoutPlanID
        if (selectedWorkoutPlan != null) {
            val intent = Intent(this@MyPlansActivity, ViewWorkoutPlanExercises::class.java)
            intent.putExtra("EXTRA_WORKOUT_PLAN", selectedWorkoutPlan)
            startActivity(intent)
        }
    }

    override fun onSetActive(position: Int) {
        setActiveWorkoutPlan(position)
    }

}







