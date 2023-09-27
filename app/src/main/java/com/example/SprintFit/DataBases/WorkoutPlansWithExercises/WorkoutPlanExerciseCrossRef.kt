package com.example.SprintFit.DataBases.WorkoutPlansWithExercises


import androidx.room.Entity
import androidx.room.ForeignKey
import com.example.SprintFit.DataBases.ExerciseDatabase.ExerciseEntity
import com.example.SprintFit.DataBases.WorkoutPlansDataBase.WorkoutPlanEntity


@Entity(
    tableName = "workoutPlanWithExercises",
    primaryKeys = ["workoutPlanID", "id"],
    foreignKeys = [
        ForeignKey(
            entity = WorkoutPlanEntity::class,
            parentColumns = ["workoutPlanID"],
            childColumns = ["workoutPlanID"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class WorkoutPlanExerciseCrossRef(
    val workoutPlanID: Int,
    val id: Int,
    val order: Int
)





