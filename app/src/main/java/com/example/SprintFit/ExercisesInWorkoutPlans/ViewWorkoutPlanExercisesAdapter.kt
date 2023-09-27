package com.example.SprintFit.ExercisesInWorkoutPlans

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.sevenminutesworkkout.R
import com.example.SprintFit.DataBases.WorkoutPlansWithExercises.WorkoutPlanWithExercises
import java.util.Collections

class ViewWorkoutPlanAdapter(
    private var workoutPlanWithExercises: WorkoutPlanWithExercises,
    private val onItemClickListener: OnItemClickListener,
    private val onItemLongClickListener: OnItemLongClickListener
) :
    RecyclerView.Adapter<ViewWorkoutPlanAdapter.ViewWorkoutPlanViewHolder>() {

    fun updateList(newExercises: WorkoutPlanWithExercises) {
        workoutPlanWithExercises = newExercises
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewWorkoutPlanViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.workoutplan_exercises_item, parent, false)
        return ViewWorkoutPlanViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewWorkoutPlanViewHolder, position: Int) {
        val exercises = workoutPlanWithExercises.exercises[position]

        holder.targetMuscleTextView.text = exercises.target
        holder.nameOfTheExerciseTextView.text = exercises.name


        val requestOptions = RequestOptions()
            .diskCacheStrategy(DiskCacheStrategy.DATA)
            .placeholder(R.drawable.baseline_cloud_download_24)

        Glide.with(holder.itemView.context)
            .asGif()
            .load(workoutPlanWithExercises.exercises[position].gifUrl)
            .thumbnail(0.1f)
            .apply(requestOptions)
            .into(holder.gifImageView)

        holder.itemView.setOnClickListener {
            onItemClickListener.onItemClick(position)
        }

        holder.itemView.setOnLongClickListener {
            onItemLongClickListener.onItemLongClick(position)
            true
        }
    }

    override fun getItemCount(): Int {
        return workoutPlanWithExercises.exercises.size
    }

    inner class ViewWorkoutPlanViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val targetMuscleTextView: TextView = itemView.findViewById(R.id.TargetMuscleTVExercise)
        val gifImageView: ImageView = itemView.findViewById(R.id.SelectExerciseMuscleGifIVExercise)
        val nameOfTheExerciseTextView: TextView =
            itemView.findViewById(R.id.NameOfTheExerciseTVExercise)
    }

    fun swapItems(fromPosition: Int, toPosition: Int) {
        Collections.swap(workoutPlanWithExercises.exercises, fromPosition, toPosition)
        notifyItemMoved(fromPosition, toPosition)
    }

    fun getWorkoutPlanWithExercises(): WorkoutPlanWithExercises {
        return workoutPlanWithExercises
    }

    fun deleteItem(position: Int) {
        val exercisesList = ArrayList(workoutPlanWithExercises.exercises)
        exercisesList.removeAt(position)
        workoutPlanWithExercises.exercises = exercisesList
        notifyItemRemoved(position)
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    interface OnItemLongClickListener {
        fun onItemLongClick(position: Int): Boolean
    }
}
