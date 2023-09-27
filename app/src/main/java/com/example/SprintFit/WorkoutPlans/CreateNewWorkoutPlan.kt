    package com.example.SprintFit.WorkoutPlans

    import android.content.Context
    import android.content.Intent
    import android.os.Bundle
    import android.text.Editable
    import android.text.TextWatcher
    import android.view.View
    import android.view.Window
    import android.view.WindowManager
    import android.view.inputmethod.InputMethodManager
    import android.widget.TextView
    import android.widget.Toast
    import androidx.appcompat.app.AppCompatActivity
    import androidx.lifecycle.lifecycleScope
    import com.example.SprintFit.BaseActivity
    import com.example.sevenminutesworkkout.databinding.ActivityCreateNewWorkoutPlanBinding
    import com.example.SprintFit.DataBases.MyAppDatabase
    import com.example.SprintFit.DataBases.WorkoutPlansDataBase.WorkoutPlanEntity
    import com.example.SprintFit.MainActivity
    import kotlinx.coroutines.Dispatchers
    import kotlinx.coroutines.launch
    import kotlinx.coroutines.withContext
    import java.text.DecimalFormat
    import java.text.NumberFormat
    import kotlin.reflect.KMutableProperty0

    private const val MAX_ROUNDS = 99
    private const val MIN_ROUNDS = 0

    private const val MAX_MINUTES_REST_TIME = 5
    private const val MAX_SECONDS_REST_TIME = 59

    private const val MAX_WORKOUT_TIME_MINUTES = 5
    private const val MAX_WORKOUT_TIME_SECONDS = 59
    class CreateNewWorkoutPlan : BaseActivity() {

        private lateinit var binding: ActivityCreateNewWorkoutPlanBinding

        private val formatter: NumberFormat = DecimalFormat("00")

        private var actualRounds = 0

        private var actualWorkoutTimeMinutes = 0
        private var actualWorkoutTimeSeconds = 0

        private var actualMinutesRestRound = 0
        private var actualSecondsRestRound = 0

        private var actualMinutesRestInterval = 0
        private var actualSecondsRestInterval = 0

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            binding = ActivityCreateNewWorkoutPlanBinding.inflate(layoutInflater)
            setContentView(binding.root)



            binding.logoToTitleScreen.setOnClickListener {
                val intent = Intent(this, MyPlansActivity::class.java)
                startActivity(intent)
            }

            val workoutPlan: WorkoutPlanEntity? =
                intent.getParcelableExtra(MyPlansActivity.EXTRA_WORKOUT_PLAN) as? WorkoutPlanEntity
            if (workoutPlan != null) {
                actualRounds = workoutPlan.rounds
                actualWorkoutTimeMinutes = workoutPlan.actualWorkoutTime / 60
                actualWorkoutTimeSeconds = workoutPlan.actualWorkoutTime % 60
                actualMinutesRestRound = workoutPlan.restBetweenRounds / 60
                actualSecondsRestRound = workoutPlan.restBetweenRounds % 60
                actualMinutesRestInterval = workoutPlan.restBetweenIntervals / 60
                actualSecondsRestInterval = workoutPlan.restBetweenIntervals % 60
                // Bind other values
                binding.WorkoutPlanCreationTitleET.setText(workoutPlan.title)
                binding.WorkoutPlanCreationDescriptionET.setText(workoutPlan.description)
                binding.ETCreateWorkoutPlanCountRounds.setText(formatter.format(actualRounds))
                binding.ETRestBetweenRoundsMinutes.setText(formatter.format(actualMinutesRestRound))
                binding.ETRestBetweenRoundsSeconds.setText(formatter.format(actualSecondsRestRound))
                binding.ETRestBetweenIntervalsMinutes.setText(formatter.format(actualMinutesRestInterval))
                binding.ETRestBetweenIntervalsSeconds.setText(formatter.format(actualSecondsRestInterval))
                binding.ETWorkoutTimeMinutes.setText(formatter.format(actualWorkoutTimeMinutes))
                binding.ETWorkoutTimeSeconds.setText(formatter.format(actualWorkoutTimeSeconds))
                displayRounds()


            }


            setupRoundControls()
            setupWorkoutTimeControls()
            setupRestBetweenRoundsControls()
            setupRestBetweenIntervalsControls()
            setupFinishButtonImplementation()
            updateRoundControls()
        }


        override fun onBackPressed() {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)

        }


        private fun setupRoundControls() {
            binding.ETCreateWorkoutPlanCountRounds.addTextChangedListener(getTextWatcher(MAX_ROUNDS) {
                actualRounds = it
            })
            binding.MinusButtonFLRounds.setOnClickListener { view ->
                updateValue(::actualRounds, -1, MIN_ROUNDS, ::displayRounds)
                hideKeyboard(view)
            }
            binding.PlusButtonFLRounds.setOnClickListener { view ->
                updateValue(::actualRounds, 1, MAX_ROUNDS, ::displayRounds)
                hideKeyboard(view)
            }

        }


        private fun setupWorkoutTimeControls() {
            binding.ETWorkoutTimeSeconds.addTextChangedListener(
                getTimeTextWatcher(
                    MAX_WORKOUT_TIME_SECONDS
                ) { actualWorkoutTimeSeconds = it })
            binding.ETWorkoutTimeMinutes.addTextChangedListener(
                getTimeTextWatcher(
                    MAX_WORKOUT_TIME_MINUTES
                ) { actualWorkoutTimeMinutes = it })
            binding.PlusButtonFLAddWorkoutTime.setOnClickListener { view ->
                increaseTime(
                    ::actualWorkoutTimeMinutes,
                    ::actualWorkoutTimeSeconds,
                    binding.ETWorkoutTimeMinutes,
                    binding.ETWorkoutTimeSeconds
                )
                hideKeyboard(view)
            }
            binding.MinusButtonFLDecreaseWorkoutTime.setOnClickListener { view ->
                decreaseTime(
                    ::actualWorkoutTimeMinutes,
                    ::actualWorkoutTimeSeconds,
                    binding.ETWorkoutTimeMinutes,
                    binding.ETWorkoutTimeSeconds
                )
                hideKeyboard(view)
            }
        }

        private fun setupRestBetweenRoundsControls() {
            binding.ETRestBetweenRoundsSeconds.addTextChangedListener(
                getTimeTextWatcher(
                    MAX_SECONDS_REST_TIME
                ) { actualSecondsRestRound = it })
            binding.ETRestBetweenRoundsMinutes.addTextChangedListener(
                getTimeTextWatcher(
                    MAX_MINUTES_REST_TIME
                ) { actualMinutesRestRound = it })
            binding.PlusButtonFLAddRestBetweenRounds.setOnClickListener { view ->
                increaseTime(
                    ::actualMinutesRestRound,
                    ::actualSecondsRestRound,
                    binding.ETRestBetweenRoundsMinutes,
                    binding.ETRestBetweenRoundsSeconds
                )
                hideKeyboard(view)
            }
            binding.MinusButtonFLDecreaseRestBetweenRounds.setOnClickListener { view ->
                decreaseTime(
                    ::actualMinutesRestRound,
                    ::actualSecondsRestRound,
                    binding.ETRestBetweenRoundsMinutes,
                    binding.ETRestBetweenRoundsSeconds
                )
                hideKeyboard(view)
            }
        }

        private fun setupRestBetweenIntervalsControls() {
            binding.ETRestBetweenIntervalsSeconds.addTextChangedListener(
                getTimeTextWatcher(
                    MAX_SECONDS_REST_TIME
                ) { actualSecondsRestInterval = it })
            binding.ETRestBetweenIntervalsMinutes.addTextChangedListener(
                getTimeTextWatcher(
                    MAX_MINUTES_REST_TIME
                ) { actualMinutesRestInterval = it })
            binding.PlusButtonFLAddRestBetweenIntervals.setOnClickListener { view ->
                increaseTime(
                    ::actualMinutesRestInterval,
                    ::actualSecondsRestInterval,
                    binding.ETRestBetweenIntervalsMinutes,
                    binding.ETRestBetweenIntervalsSeconds
                )
                hideKeyboard(view)
            }
            binding.MinusButtonFLDecreaseRestBetweenIntervals.setOnClickListener { view ->
                decreaseTime(
                    ::actualMinutesRestInterval,
                    ::actualSecondsRestInterval,
                    binding.ETRestBetweenIntervalsMinutes,
                    binding.ETRestBetweenIntervalsSeconds
                )
                hideKeyboard(view)
            }
        }

        private fun setupFinishButtonImplementation() {
            binding.SaveWorkoutPlanButton.setOnClickListener { saveUpInDatabase() }
        }

        private fun updateValue(
            property: KMutableProperty0<Int>,
            delta: Int,
            boundary: Int,
            updateFunction: () -> Unit
        ) {
            property.get().let {
                if ((delta > 0 && it < boundary) || (delta < 0 && it > boundary)) {
                    property.set(property.get() + delta)
                    updateFunction()
                    updateRoundControls()

                }
            }
        }


        private fun increaseTime(
            minutesProperty: KMutableProperty0<Int>,
            secondsProperty: KMutableProperty0<Int>,
            minutesView: TextView,
            secondsView: TextView
        ) {
            if (minutesProperty.get() != MAX_MINUTES_REST_TIME) {
                secondsProperty.get().let {
                    if (it + 5 >= MAX_SECONDS_REST_TIME + 1) {
                        minutesProperty.set(minutesProperty.get() + 1)
                        secondsProperty.set((it + 5) - (MAX_SECONDS_REST_TIME + 1))
                    } else {
                        secondsProperty.set(secondsProperty.get() + 5)
                    }
                    minutesView.text = formatter.format(minutesProperty.get())
                    secondsView.text = formatter.format(secondsProperty.get())
                }
            }

        }


        private fun decreaseTime(
            minutesProperty: KMutableProperty0<Int>,
            secondsProperty: KMutableProperty0<Int>,
            minutesView: TextView,
            secondsView: TextView
        ) {
            if (!(secondsProperty.get() == 0 && minutesProperty.get() == 0)) {
                if (secondsProperty.get() < 5 && minutesProperty.get() != 0) {
                    minutesProperty.set(minutesProperty.get() - 1)
                    secondsProperty.set(55 + secondsProperty.get()) // Since we are decreasing by 5, we add 55 to the remaining seconds
                } else {
                    secondsProperty.set(secondsProperty.get() - 5)
                }
                minutesView.text = formatter.format(minutesProperty.get())
                secondsView.text = formatter.format(secondsProperty.get())
            }
        }


        private fun getTextWatcher(maxValue: Int, updateAction: (Int) -> Unit) = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                s?.let {
                    if (it.isNotEmpty()) {
                        val value = it.toString().toInt()
                        if (value > maxValue) {
                            Toast.makeText(
                                this@CreateNewWorkoutPlan,
                                "Maximum value exceeded",
                                Toast.LENGTH_SHORT
                            ).show()
                            s.clear()
                            s.append("")
                        } else {
                            updateAction(value)
                        }
                    }
                }
            }
        }

        private fun getTimeTextWatcher(maxValue: Int, updateAction: (Int) -> Unit) =
            object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    s?.let {
                        if (it.isNotEmpty()) {
                            val value = it.toString().toInt()
                            if (value > maxValue) {
                                Toast.makeText(
                                    this@CreateNewWorkoutPlan,
                                    "Maximum value exceeded",
                                    Toast.LENGTH_SHORT
                                ).show()
                                s.clear()
                                s.append("00")
                            } else {
                                updateAction(value)
                            }
                        }
                    }
                }
            }


        private fun saveUpInDatabase() {
            val name: String = binding.WorkoutPlanCreationTitleET.text.toString()
            var description: String = binding.WorkoutPlanCreationDescriptionET.text.toString().trim()

            if (name.isEmpty() || description.isEmpty()) {
                Toast.makeText(this, "Enter a valid name and description", Toast.LENGTH_SHORT).show()
                return
            }

            val rounds = actualRounds
            val actualWorkoutTime = actualWorkoutTimeMinutes * 60 + actualWorkoutTimeSeconds
            val restRounds = actualMinutesRestRound * 60 + actualSecondsRestRound
            val restIntervals = actualMinutesRestInterval * 60 + actualSecondsRestInterval

            val existingWorkoutPlan: WorkoutPlanEntity? =
                intent.getParcelableExtra(MyPlansActivity.EXTRA_WORKOUT_PLAN) as? WorkoutPlanEntity

            val workoutPlan = existingWorkoutPlan?.let {
                WorkoutPlanEntity(
                    it.workoutPlanID,
                    name,
                    description,
                    actualWorkoutTime,
                    rounds,
                    restRounds,
                    restIntervals,
                    it.order,
                    it.currentlyActive
                )
            } ?: WorkoutPlanEntity(
                0,
                name,
                description,
                actualWorkoutTime,
                rounds,
                restRounds,
                restIntervals,
                0,
                false
            )


            lifecycleScope.launch(Dispatchers.IO) {
                val db = MyAppDatabase.getInstance(this@CreateNewWorkoutPlan)
                val workoutPlanDao = db.WorkoutPlanDao()

                // First, set all workout plans to inactive
                workoutPlanDao.setAllWorkoutPlansInactive()

                if (existingWorkoutPlan != null) {
                    // Update the existing workout plan
                    workoutPlanDao.update(workoutPlan)
                } else {
                    // Insert the new workout plan
                    workoutPlanDao.insertOne(workoutPlan)

                    // Get the highest workoutPlanID
                    val newWorkoutPlanId = workoutPlanDao.getHighestWorkoutPlanId()

                    // Set the new workout plan to active
                    if (newWorkoutPlanId != null) {
                        workoutPlanDao.setActiveWorkoutPlan(newWorkoutPlanId, true)
                    }
                }
                withContext(Dispatchers.Main) {
                    finish()
                }
            }
        }


        private fun updateRoundControls() {
            binding.PlusButtonFLRounds.isEnabled = actualRounds < MAX_ROUNDS
        }


        private fun displayRounds() {
            binding.ETCreateWorkoutPlanCountRounds.setText(formatter.format(actualRounds))
        }

        private fun hideKeyboard(view: View) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(view.windowToken, 0)
        }


    }
