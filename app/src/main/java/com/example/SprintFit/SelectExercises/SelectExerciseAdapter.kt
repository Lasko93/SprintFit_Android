import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.sevenminutesworkkout.R
import com.example.SprintFit.DataBases.ExerciseDatabase.ExerciseEntity
import com.example.SprintFit.DataBases.MyAppDatabase
import com.example.SprintFit.DataBases.WorkoutPlansWithExercises.WorkoutPlanExerciseCrossRef
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class SelectExerciseAdapter(
    private var allExercises: List<ExerciseEntity>,
    private val onItemClickListener: OnItemClickListener,
    private val onItemLongClickListener: OnItemLongClickListener?,
    private val workoutPlanID: Int,
    private val myAppDatabase: MyAppDatabase,
    private val lifecycleScope: LifecycleCoroutineScope
) :
    RecyclerView.Adapter<SelectExerciseAdapter.SelectExerciseViewHolder>() {

    fun updateList(newList: List<ExerciseEntity>) {
        allExercises = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectExerciseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.select_exercises_item, parent, false)
        return SelectExerciseViewHolder(view)
    }

    override fun onBindViewHolder(holder: SelectExerciseViewHolder, position: Int) {
        holder.targetMuscleTextView.text = allExercises[position].name
        holder.nameOfTheExerciseTextView.text = allExercises[position].bodyPart

        if (allExercises[position].isChecked) {
            holder.background.setBackgroundColor(Color.parseColor("#DBEBF9"))
        } else {
            holder.background.setBackgroundColor(Color.WHITE)
        }

        val requestOptions = RequestOptions()
            .diskCacheStrategy(DiskCacheStrategy.DATA)
            .placeholder(R.drawable.baseline_cloud_download_24)

        Glide.with(holder.itemView.context)
            .asGif()
            .load(allExercises[position].gifUrl)
            .thumbnail(0.1f)
            .apply(requestOptions)
            .into(holder.gifImageView)

        holder.itemView.setOnClickListener {
            if (allExercises[position].isChecked) {
                // Deselect the exercise and remove it from the workout plan
                removeExerciseFromWorkoutPlan(position)
            } else {
                // Select the exercise and update the checkbox state
                allExercises[position].isChecked = true
                addSelectedExercisesToWorkoutPlan()
                notifyItemChanged(position)
            }

            // Notify the custom item click listener
            onItemClickListener.onItemClick(position)
        }



        holder.itemView.setOnLongClickListener {
            onItemLongClickListener?.onItemLongClick(position) ?: false
        }
    }


    override fun getItemCount(): Int {
        return allExercises.size
    }

    inner class SelectExerciseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val targetMuscleTextView: TextView = itemView.findViewById(R.id.TargetMuscleTV)
        val gifImageView: ImageView = itemView.findViewById(R.id.SelectExerciseMuscleGifIV)
        val nameOfTheExerciseTextView: TextView = itemView.findViewById(R.id.NameOfTheExerciseTV)
        val background: LinearLayout = itemView.findViewById(R.id.backgroundchecked)
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    interface OnItemLongClickListener {
        fun onItemLongClick(position: Int): Boolean
    }

    private fun addSelectedExercisesToWorkoutPlan() {
      // identify selected exercises
        val selectedExercises = allExercises.filter { it.isChecked }

        // insert the selected exercises into the workoutPlanWithExercises table
        lifecycleScope.launch {
            if (workoutPlanID != -1) {
                withContext(Dispatchers.IO) {
                    // Get the current maximum order value for the exercises in the workout plan
                    val maxOrder =
                        myAppDatabase.WorkoutPlanWithExercisesDao().getMaxOrder(workoutPlanID) ?: 0

                    // Add the new exercises with order values greater than the current maximum
                    selectedExercises.forEachIndexed { index, exercise ->
                        val newOrder = maxOrder + index + 1
                        val crossRef =
                            WorkoutPlanExerciseCrossRef(workoutPlanID, exercise.id, newOrder)
                        myAppDatabase.WorkoutPlanWithExercisesDao()
                            .insertExerciseToWorkoutPlan(crossRef)
                    }
                }
            }
        }
    }


    private fun removeExerciseFromWorkoutPlan(position: Int) {
        val exerciseId = allExercises[position].id
        val planId = workoutPlanID


        lifecycleScope.launch(Dispatchers.IO) {
            myAppDatabase.WorkoutPlanWithExercisesDao()
                .deleteExerciseFromWorkoutPlan(planId, exerciseId)


            withContext(Dispatchers.Main) {
                // Deselect the exercise and update the checkbox state
                allExercises[position].isChecked = false
                notifyItemChanged(position)
            }
        }
    }


}
