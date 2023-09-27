import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sevenminutesworkkout.R
import com.example.SprintFit.DataBases.WorkoutPlansWithExercises.WorkoutPlanWithExercises
import java.util.Collections


class WorkoutPlanAdapter(
    private var workoutPlansWithExercises: List<WorkoutPlanWithExercises>,
    private val optionsClickListener: WorkoutPlanOptionsClickListener
) :
    RecyclerView.Adapter<WorkoutPlanAdapter.WorkoutPlanViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkoutPlanViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.workout_plan_item, parent, false)
        return WorkoutPlanViewHolder(view)
    }

    override fun onBindViewHolder(holder: WorkoutPlanViewHolder, position: Int) {
        val currentWorkoutPlanWithExercises = workoutPlansWithExercises[position]
        val currentWorkoutPlan = currentWorkoutPlanWithExercises.workoutplan
        holder.titleTextView.text = currentWorkoutPlan.title
        holder.descriptionTextView.text = currentWorkoutPlan.description
        holder.roundsCount.text = currentWorkoutPlan.rounds.toString()
        holder.exerciseCount.text = currentWorkoutPlanWithExercises.exercises.size.toString()



        if (currentWorkoutPlan.currentlyActive) {
            holder.backgroundItem.setBackgroundColor(Color.parseColor("#DBEBF9"))
        } else {
            holder.backgroundItem.setBackgroundColor(Color.parseColor("#FFFFFF"))
        }


        // Adding click listener to the options_menu_myplan
        holder.itemView.findViewById<FrameLayout>(R.id.options_menu_myplan)
            .setOnClickListener { view ->
                // Creating a PopupMenu
                val popup = PopupMenu(view.context, view)
                // Inflating the Popup using XML file
                popup.menuInflater.inflate(R.menu.menu_workout_plans, popup.menu)

                // Registering popup with OnMenuItemClickListener
                popup.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.action_edit -> {
                            optionsClickListener.onEdit(position)
                            true
                        }

                        R.id.action_copy -> {
                            optionsClickListener.onCopy(position)
                            true
                        }

                        R.id.action_addExercises -> {
                            optionsClickListener.onAddExercises(position)
                            true
                        }

                        else -> false
                    }
                }
                popup.show() // Showing popup menu
            }
    }

    override fun getItemCount(): Int {
        return workoutPlansWithExercises.size
    }

    inner class WorkoutPlanViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.workout_title)
        val descriptionTextView: TextView = itemView.findViewById(R.id.workout_description)
        val backgroundItem: FrameLayout = itemView.findViewById(R.id.BackgroundItem)
        val roundsCount: TextView = itemView.findViewById(R.id.rounds_tv)
        val exerciseCount: TextView = itemView.findViewById(R.id.exercises_tv)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    optionsClickListener.onSetActive(position)
                }
            }
        }
    }

    interface WorkoutPlanOptionsClickListener {
        fun onEdit(position: Int)
        fun onCopy(position: Int)
        fun onAddExercises(position: Int)

        fun onSetActive(position: Int)
    }

    fun moveItem(fromPosition: Int, toPosition: Int) {
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                Collections.swap(workoutPlansWithExercises, i, i + 1)
                // Here, update the order attribute in your entities if any
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                Collections.swap(workoutPlansWithExercises, i, i - 1)
                // Here, update the order attribute in your entities if any
            }
        }
        notifyItemMoved(fromPosition, toPosition)
    }


}
