package com.example.SprintFit

import ExerciseRepository
import android.content.Intent
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.sevenminutesworkkout.R
import com.example.sevenminutesworkkout.databinding.ActivityMainBinding
import com.example.SprintFit.ActualWorkout.ExerciseActivity
import com.example.SprintFit.DataBases.ExerciseDatabase.ExerciseEntity
import com.example.SprintFit.DataBases.MyAppDatabase
import com.example.SprintFit.DataBases.WorkoutPlansDataBase.WorkoutPlanEntity
import com.example.SprintFit.ExercisesInWorkoutPlans.ViewWorkoutPlanExercises
import com.example.SprintFit.SelectExercises.SelectExercises
import com.example.SprintFit.WorkoutPlans.CreateNewWorkoutPlan
import com.example.SprintFit.WorkoutPlans.MyPlansActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MainActivity : BaseActivity() {

    private var binding: ActivityMainBinding? = null

    private var workoutPlan: WorkoutPlanEntity? = null

    private var exerciseList: List<ExerciseEntity> = emptyList()


    companion object {

        const val EXTRA_WORKOUT_PLAN = "EXTRA_WORKOUT_PLAN"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding!!.root)



        downloadTheJsonAndSaveItUp()
        preloadGifs()
        setButtonText()



        //Directing to MyPlans
        binding!!.myplansclickable.setOnClickListener {
            val intent = Intent(this, MyPlansActivity::class.java)
            startActivity(intent)
        }


        //Directing to different Activities depending on whats in the Database
        binding!!.startworkout.setOnClickListener {
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    val workoutPlanDao =
                        MyAppDatabase.getInstance(applicationContext).WorkoutPlanDao()
                    val workoutPlanWithExercisesDao =
                        MyAppDatabase.getInstance(applicationContext).WorkoutPlanWithExercisesDao()
                    val workoutPlanID =
                        MyAppDatabase.getInstance(applicationContext).WorkoutPlanDao()
                            .getActiveWorkoutPlan()?.workoutPlanID
                    val countWorkoutPlans =
                        MyAppDatabase.getInstance(applicationContext).WorkoutPlanDao()
                            .getWorkoutPlanCount()
                    // Fetch the active workout plan
                    val activeWorkoutPlan = workoutPlanDao.getActiveWorkoutPlan()

                    if (countWorkoutPlans == 0) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@MainActivity,
                                "Create your first workout",
                                Toast.LENGTH_SHORT
                            ).show()
                            startActivity(
                                Intent(
                                    this@MainActivity,
                                    CreateNewWorkoutPlan::class.java
                                )
                            )
                        }
                    } else if (activeWorkoutPlan == null) {
                        // No active workout plan, navigate to MyPlansActivity
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@MainActivity,
                                "Tap to select a workout",
                                Toast.LENGTH_SHORT
                            ).show()
                            startActivity(Intent(this@MainActivity, MyPlansActivity::class.java))
                        }
                    } else {
                        // Fetch exercises for the active workout plan
                        val exercisesInActivePlan =
                            workoutPlanWithExercisesDao.getWorkoutPlanWithExercises(
                                activeWorkoutPlan.workoutPlanID
                            )?.exercises

                        if (exercisesInActivePlan.isNullOrEmpty()) {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    this@MainActivity,
                                    "Select your exercises!",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                            // No exercises in active workout plan, navigate to ViewWorkoutPlanExercises
                            startActivity(
                                Intent(
                                    this@MainActivity,
                                    SelectExercises::class.java
                                ).putExtra(EXTRA_WORKOUT_PLAN, workoutPlanID)
                            )

                        } else {
                            // Exercises exist in active workout plan, navigate to ExerciseActivity
                            startActivity(Intent(this@MainActivity, ExerciseActivity::class.java))
                        }
                    }
                }
            }
        }


    }

    override fun onBackPressed() {

        val dialog = AlertDialog.Builder(this@MainActivity, R.style.MyAlertDialogStyle)
            .setTitle("Exit App")
            .setMessage("Are you sure you want to exit?")
            .setPositiveButton("Yes") { _, _ -> finishAffinity() }
            .setNegativeButton("No", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        setButtonText()
    }


    private fun setButtonText() {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val workoutPlanDao = MyAppDatabase.getInstance(applicationContext).WorkoutPlanDao()
                val workoutPlanWithExercisesDao =
                    MyAppDatabase.getInstance(applicationContext).WorkoutPlanWithExercisesDao()
                val countWorkoutPlans =
                    MyAppDatabase.getInstance(applicationContext).WorkoutPlanDao()
                        .getWorkoutPlanCount()

                val activeWorkoutPlan = workoutPlanDao.getActiveWorkoutPlan()

                if (countWorkoutPlans == 0) {
                    withContext(Dispatchers.Main) {
                        binding!!.StartWorkoutText.text = "Create workout"
                    }
                } else if (activeWorkoutPlan == null) {
                    withContext(Dispatchers.Main) {
                        binding!!.StartWorkoutText.text = "Choose workout"
                    }
                } else {
                    // Fetch exercises for the active workout plan
                    val exercisesInActivePlan =
                        workoutPlanWithExercisesDao.getWorkoutPlanWithExercises(activeWorkoutPlan.workoutPlanID)?.exercises

                    if (exercisesInActivePlan.isNullOrEmpty()) {
                        withContext(Dispatchers.Main) {
                            binding!!.StartWorkoutText.text = "Add exercises"
                        }
                        // No exercises in active workout plan, navigate to ViewWorkoutPlanExercises


                    } else {
                        withContext(Dispatchers.Main) {
                            binding!!.StartWorkoutText.text = "Start workout"
                        }
                    }
                }
            }
        }
    }

    private fun downloadTheJsonAndSaveItUp() {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val exerciseDao = MyAppDatabase.getInstance(application).exerciseDao()
                val exerciseCount = exerciseDao.getExerciseCount()
                if (exerciseCount == 0) {
                    try {
                        val repository = ExerciseRepository()
                        exerciseList = repository.fetchExercises()!!
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        if (exerciseList.isNotEmpty()) {
                            exerciseDao.insertAll(exerciseList)
                            preloadGifs()
                        }
                    }
                }
            }
        }

    }

    private fun preloadGifs() {
        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                val exerciseDao = MyAppDatabase.getInstance(applicationContext).exerciseDao()
                val exercises =
                    exerciseDao.getAllExercises() // Assuming you have a method to get all exercises
                for (exercise in exercises) {
                    val gifUrl = exercise.gifUrl // Assuming gifUrl is the property name
                    Glide.with(applicationContext)
                        .load(gifUrl)
                        .diskCacheStrategy(DiskCacheStrategy.DATA)
                        .preload()
                }
            }
        }
    }

    private fun addExercisesToWorkoutPlan() {
        val workoutPlanID = workoutPlan!!.workoutPlanID
        val intent = Intent(this@MainActivity, ViewWorkoutPlanExercises::class.java)
        intent.putExtra(ViewWorkoutPlanExercises.EXTRA_WORKOUT_PLAN, workoutPlanID)
        startActivity(intent)

    }

}
