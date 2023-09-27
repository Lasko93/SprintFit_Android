package com.example.SprintFit.ActualWorkout

import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.OnInitListener
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AlertDialog

import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.SprintFit.BaseActivity
import com.example.sevenminutesworkkout.databinding.ActivityExerciseBinding
import com.example.SprintFit.DataBases.ExerciseDatabase.ExerciseEntity
import com.example.SprintFit.DataBases.MyAppDatabase
import com.example.SprintFit.DataBases.WorkoutPlansWithExercises.WorkoutPlanWithExercises
import com.example.SprintFit.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.Locale


class ExerciseActivity : BaseActivity() {

    private val DELAY_MILLIS :Long = 1000
    private val INITIAL_ROUND_TIME = 10

    private var actualRounds: Int = 1
    private var maxRounds: Int = 1

    private var exercisesPosition: Int = 0

    private var actualInterval: Int = 1
    private var maxIntervals: Int = 1

    private var restBetweenRounds: Int = 0

    private var restBetweenIntervals: Int = 0

    private var actualWorkoutTime: Int = 0

    private var nameOfExercise: String = ""

    private var binding: ActivityExerciseBinding? = null

    private var workoutPlanWithExercises: WorkoutPlanWithExercises? = null

    private var resting: Boolean = false

    private var handler: Handler? = null
    private var runnable: Runnable? = null
    private val mutex = Mutex()

    private var isPaused = false
    private var isRunnableRunning = false

    private lateinit var textToSpeech: TextToSpeech


    private var isInitialRound = true

    private var isExiting = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExerciseBinding.inflate(layoutInflater)
        setContentView(binding!!.root)



        //setup all bindings
        binding!!.NextIB.setOnClickListener { nextExercise() }

        binding!!.PreviousIB.setOnClickListener { previousExercise() }

        binding!!.backLogo.setOnClickListener {
            showAbortWorkoutDialog()
        }

        binding!!.PauseButton.setOnClickListener {
            pauseWorkout()
        }


        setUpTextToSpeech()
        settingUpTheUiAndWorkoutPlan()

    }

    override fun onBackPressed() {
        showAbortWorkoutDialog()
    }

    //CleanUp
    override fun onDestroy() {
        cancelExistingHandlers()
        binding = null // Clearing the binding reference
        super.onDestroy()
    }


    private fun pauseWorkout() {
        isPaused = !isPaused
        if (isPaused) {
            binding!!.DisplayLeftTimeTV.text = "| |"
            textToSpeech.speak("Pause", TextToSpeech.QUEUE_FLUSH, null, "")
        } else {
            textToSpeech.speak("Start", TextToSpeech.QUEUE_FLUSH, null, "")
            // Only post the runnable if it's not already running
            if (!isRunnableRunning) {
                handler?.post(runnable!!)

            }
        }
    }

    private fun setUpTextToSpeech(){
        textToSpeech = TextToSpeech(this, OnInitListener { status ->
            if (status != TextToSpeech.ERROR) {
                textToSpeech.language = Locale.US
            }
        })
    }
    private fun showAbortWorkoutDialog() {
        isPaused = true
        binding!!.DisplayLeftTimeTV.text = "| |"
        textToSpeech.speak("Pause", TextToSpeech.QUEUE_FLUSH, null, "")
        val dialog = AlertDialog.Builder(this)
            .setTitle("Abort Workout")
            .setMessage("Are you sure you want to exit?")
            .setPositiveButton("Yes") { _, _ ->
                isExiting = true
                super.onBackPressed()
            }
            .setNegativeButton("No", null)
            .setOnDismissListener {
                if (!isExiting) {
                    isPaused = false
                    textToSpeech.speak("Start", TextToSpeech.QUEUE_FLUSH, null, "")
                }
            }
            .show()

        dialog.findViewById<TextView>(android.R.id.title)?.setTextColor(Color.BLACK)
        dialog.findViewById<TextView>(android.R.id.message)?.setTextColor(Color.BLACK)
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(Color.BLACK)
        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(Color.BLACK)
    }

    private fun cancelExistingHandlers() {
        handler?.removeCallbacks(runnable!!)
        runnable = null
        handler = null
        isPaused = false
        isRunnableRunning = true
    }

    private fun updateIntervalsAndRoundsUI() {
        binding!!.displayRoundsTV.text =
            "Rounds \n" + actualRounds.toString() + "/" + maxRounds.toString()
        binding!!.displayIntervalsTV.text =
            "Exercise \n" + actualInterval.toString() + "/" + maxIntervals.toString()
    }

    private fun settingUpTheUiAndWorkoutPlan() {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val workoutPlanWithExercisesDao =
                    MyAppDatabase.getInstance(applicationContext).WorkoutPlanWithExercisesDao()
                val workoutPlanWithExercisesTemp: WorkoutPlanWithExercises? =
                    workoutPlanWithExercisesDao.getActiveWorkoutPlanWithExercisesOrdered()

                if (workoutPlanWithExercisesTemp != null) {
                    // Explicitly setting the ordered exercises
                    workoutPlanWithExercisesTemp.exercises =
                        workoutPlanWithExercisesDao.getExercisesForWorkoutPlanOrdered(
                            workoutPlanWithExercisesTemp.workoutplan.workoutPlanID
                        )
                    actualWorkoutTime = workoutPlanWithExercisesTemp.workoutplan.actualWorkoutTime
                    maxRounds = workoutPlanWithExercisesTemp.workoutplan.rounds
                    restBetweenRounds = workoutPlanWithExercisesTemp.workoutplan.restBetweenRounds
                    restBetweenIntervals =
                        workoutPlanWithExercisesTemp.workoutplan.restBetweenIntervals
                    maxIntervals = workoutPlanWithExercisesTemp.exercises.size
                    nameOfExercise = workoutPlanWithExercisesTemp.exercises[0].name
                }
                withContext(Dispatchers.Main) {
                    binding!!.DisplayLeftTimeTV.text = actualWorkoutTime.toString()
                    binding!!.displayRoundsTV.text = "Rounds \n" +
                            actualRounds.toString() + "/" + maxRounds.toString()
                    binding!!.displayIntervalsTV.text = "Exercise \n" +
                            actualInterval.toString() + "/" + maxIntervals.toString()
                    binding!!.displayExerciseNameTv.text = nameOfExercise
                    workoutPlanWithExercises = workoutPlanWithExercisesTemp
                    startWorkout()

                }
            }
        }

    }

    private fun runInitialRound(onComplete: () -> Unit) {
        lifecycleScope.launch {
            cancelExistingHandlers()
            resting = true
            binding?.apply {
                displayExerciseNameTv.text = "Get ready for your workout"
                DisplayLeftTimeTV.text = "10"

                handler = Handler(Looper.getMainLooper())
                runnable = object : Runnable {
                    var timeLeft = INITIAL_ROUND_TIME

                    override fun run() {
                        isRunnableRunning = true
                        if (isPaused) {
                            handler?.postDelayed(this, DELAY_MILLIS)
                            return
                        }
                        DisplayLeftTimeTV.text = timeLeft.toString()
                        progressBar.progress = (timeLeft * 100) / 10
                        if (timeLeft == 9) {
                            textToSpeech.speak(
                                "Get ready for your workout. First exercise is" + workoutPlanWithExercises?.exercises?.get(
                                    exercisesPosition
                                )?.name,
                                TextToSpeech.QUEUE_FLUSH,
                                null,
                                ""
                            )
                        }
                        if (timeLeft in 1..3) {
                            textToSpeech.speak(
                                timeLeft.toString(),
                                TextToSpeech.QUEUE_FLUSH,
                                null,
                                ""
                            )
                        }
                        if (timeLeft == 0) {
                            textToSpeech.speak("Start", TextToSpeech.QUEUE_FLUSH, null, "")
                        }
                        if (timeLeft > 0) {
                            timeLeft--
                            handler?.postDelayed(this, DELAY_MILLIS)
                        } else {
                            onComplete()
                            isRunnableRunning = false
                        }
                    }
                }
                handler?.post(runnable!!)
            }
        }
    }
    private fun startWorkout() {
        lifecycleScope.launch {
            if (isInitialRound) {
                runInitialRound {
                    isInitialRound = false
                    startWorkout()
                }
            } else {
                cancelExistingHandlers()
                delay(500) // Delay for 0,5 second

                // If it's the last round and the last interval, finish the workout without resting
                if (actualRounds > maxRounds || (actualRounds == maxRounds && actualInterval > maxIntervals)) {
                    finishWorkout()
                    return@launch
                }

                // If it's time for the next round, increment the round and reset the interval and exercisesPosition
                if (actualInterval > maxIntervals) {
                    actualInterval = 1
                    exercisesPosition = 0
                    actualRounds++

                    // If it's not the last round, rest between rounds and then continue with the next round
                    if (actualRounds <= maxRounds) {
                        runRest(restBetweenRounds) {
                            updateIntervalsAndRoundsUI()
                            startWorkout() // Recurse only after the rest between rounds is done
                        }
                        return@launch
                    }
                }

                // Run the exercise
                runExercise(workoutPlanWithExercises?.exercises?.get(exercisesPosition)) {
                    // Increment interval and exercise position after the exercise is done
                    actualInterval++
                    exercisesPosition++

                    // If it's not the last exercise of the last round, rest between intervals
                    if (actualRounds <= maxRounds && actualInterval <= maxIntervals) {
                        runRest(restBetweenIntervals) {
                            updateIntervalsAndRoundsUI()
                            startWorkout() // Recurse only after the rest between intervals is done
                        }
                    } else {
                        startWorkout() // Continue without rest if it's the last exercise of the last round
                    }
                }
            }

        }
    }

    private fun startCurrentRound() {
        lifecycleScope.launch {
            mutex.withLock {
                cancelExistingHandlers()
                // Run the exercise
                runExercise(workoutPlanWithExercises?.exercises?.get(exercisesPosition)) {
                    // Increment interval and exercise position after the exercise is done
                    actualInterval++
                    exercisesPosition++

                    // If it's not the last exercise of the last round, rest between intervals
                    if (actualRounds <= maxRounds && actualInterval <= maxIntervals) {
                        runRest(restBetweenIntervals) {
                            updateIntervalsAndRoundsUI()
                            startCurrentRound() // Recurse only after the rest between intervals is done
                        }
                    } else {
                        startWorkout() // Continue without rest if it's the last exercise of the last round
                    }
                }
            }
        }
    }

    private fun previousExercise() {
        lifecycleScope.launch {

            cancelExistingHandlers()
            // If it's the first interval of the first round
            if (actualRounds == 1 && actualInterval == 1) {
                startCurrentRound()// Exit the function if it's the first round and first interval
            } else if (actualInterval <= 1 && actualRounds <= 1) {
                startWorkout() // Restart the workout
            }

            // If it's the first interval in the current round, move to the previous round
            else if (actualInterval <= 1) {
                actualRounds--
                exercisesPosition = maxIntervals - 1
                actualInterval = maxIntervals
            } else {
                exercisesPosition--
                actualInterval--
            }

            updateIntervalsAndRoundsUI()
            startCurrentRound()
        }

    }


    private fun nextExercise() {
        lifecycleScope.launch {

            cancelExistingHandlers()
            // If you're currently resting, you should just continue with the next exercise without incrementing the counters.
            if (!resting) {
                // If it's the last interval of the last round
                if (actualInterval >= maxIntervals && actualRounds >= maxRounds) {
                    // You can decide the behavior here, e.g., finish the workout
                    finishWorkout()
                }

                // If it's the last interval in the current round, move to the next round
                if (actualInterval >= maxIntervals) {
                    actualRounds++
                    exercisesPosition = 0
                    actualInterval = 1
                } else {
                    exercisesPosition++
                    actualInterval++
                }
            }

            resting = false // Reset the resting flag
            updateIntervalsAndRoundsUI()
            startCurrentRound()
        }

    }

    private fun runExercise(exercise: ExerciseEntity?, onComplete: () -> Unit) {
        lifecycleScope.launch {

            cancelExistingHandlers()
            exercise?.let {
                resting = false
                binding?.apply {
                    displayExerciseNameTv.text = it.name
                    DisplayLeftTimeTV.text = actualWorkoutTime.toString()

                    Glide.with(this@ExerciseActivity)
                        .asGif()
                        .load(it.gifUrl)
                        .into(ExerciseGifWV)

                    handler = Handler(Looper.getMainLooper())
                    runnable = object : Runnable {
                        var timeLeft = actualWorkoutTime

                        override fun run() {
                            isRunnableRunning = true
                            if (isPaused) {
                                handler?.postDelayed(this, DELAY_MILLIS)
                                return
                            }
                            binding?.apply {
                                DisplayLeftTimeTV.text = timeLeft.toString()
                                progressBar.progress = (timeLeft * 100) / actualWorkoutTime
                            }
                            if (timeLeft in 1..3) {
                                textToSpeech.speak(
                                    timeLeft.toString(),
                                    TextToSpeech.QUEUE_FLUSH,
                                    null,
                                    ""
                                )
                            }
                            if (timeLeft == 0) {
                                textToSpeech.speak("Rest", TextToSpeech.QUEUE_FLUSH, null, "")
                            }
                            if (timeLeft == 10 && !resting) {
                                textToSpeech.speak(
                                    "Ten seconds Left",
                                    TextToSpeech.QUEUE_FLUSH,
                                    null,
                                    ""
                                )
                            }
                            if (timeLeft > 0) {
                                timeLeft--
                                handler?.postDelayed(this, DELAY_MILLIS)
                            } else {
                                onComplete()
                                isRunnableRunning = false
                            }
                        }

                    }
                    handler?.post(runnable!!)
                }
            }
        }
    }

    private fun runRest(restTime: Int, onComplete: () -> Unit) {
        lifecycleScope.launch {

            cancelExistingHandlers()
            resting = true
            binding?.apply {
                displayExerciseNameTv.text = "Resting Time"
                DisplayLeftTimeTV.text = restTime.toString()


                Glide.with(this@ExerciseActivity)
                    .asGif()
                    .load("https://media0.giphy.com/media/WTu1HMIWUHk0gKtB2m/giphy.gif?cid=ecf05e4741lm4uhmdma8kc1hcspbt5qw5h1s6uwpwd386h4g&ep=v1_gifs_search&rid=giphy.gif&ct=g")
                    .into(binding!!.ExerciseGifWV)

                handler = Handler(Looper.getMainLooper())
                runnable = object : Runnable {
                    var timeLeft = restTime

                    override fun run() {
                        isRunnableRunning = true
                        if (isPaused) {
                            handler?.postDelayed(this, DELAY_MILLIS)
                            return
                        }
                        DisplayLeftTimeTV.text = timeLeft.toString()
                        progressBar.progress = (timeLeft * 100) / restTime
                        if (timeLeft in 1..3) {
                            textToSpeech.speak(
                                timeLeft.toString(),
                                TextToSpeech.QUEUE_FLUSH,
                                null,
                                ""
                            )
                        }
                        if (timeLeft == 0) {
                            textToSpeech.speak("Go", TextToSpeech.QUEUE_FLUSH, null, "")
                        }
                        if (timeLeft == 10 && resting) {
                            textToSpeech.speak(
                                "Get Ready. Next exercise is" + workoutPlanWithExercises?.exercises?.get(
                                    exercisesPosition
                                )?.name,
                                TextToSpeech.QUEUE_FLUSH,
                                null,
                                ""
                            )
                        }
                        if (timeLeft > 0) {
                            timeLeft--
                            handler?.postDelayed(this, DELAY_MILLIS)
                        } else {
                            onComplete()
                            isRunnableRunning = false
                        }
                    }

                }
                handler?.post(runnable!!)
            }

        }
    }


    private fun finishWorkout() {
        // Create an Intent to start MainActivity
        val intent = Intent(this, MainActivity::class.java)
        isInitialRound = true

        // Add the following flags to the intent:
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK

        // Start MainActivity
        startActivity(intent)

        // Finish current activity
        finish()
    }

}




