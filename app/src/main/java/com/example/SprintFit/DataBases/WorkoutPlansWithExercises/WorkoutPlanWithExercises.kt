package com.example.SprintFit.DataBases.WorkoutPlansWithExercises

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.example.SprintFit.DataBases.ExerciseDatabase.ExerciseEntity
import com.example.SprintFit.DataBases.WorkoutPlansDataBase.WorkoutPlanEntity

data class WorkoutPlanWithExercises(
    @Embedded val workoutplan: WorkoutPlanEntity,
    @Relation(
        parentColumn = "workoutPlanID",
        entityColumn = "id",
        associateBy = Junction(WorkoutPlanExerciseCrossRef::class)
    )
    var exercises: List<ExerciseEntity>
)
