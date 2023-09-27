package com.example.SprintFit.DataBases.WorkoutPlansWithExercises

import androidx.room.*
import com.example.SprintFit.DataBases.ExerciseDatabase.ExerciseEntity

@Dao
interface WorkoutPlanWithExercisesDao {

    @Transaction
    @Query("SELECT * FROM WorkoutPlans WHERE workoutPlanID = :planId")
    fun getWorkoutPlanWithExercises(planId: Int): WorkoutPlanWithExercises?

    @Query("SELECT MAX(`order`) FROM WorkoutPlans WHERE workoutPlanId = :workoutPlanId")
    fun getMaxOrder(workoutPlanId: Int): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertExerciseToWorkoutPlan(crossRef: WorkoutPlanExerciseCrossRef)

    @Transaction
    fun updateWorkoutPlanWithExercises(workoutPlanWithExercises: WorkoutPlanWithExercises) {
        // Delete existing join table entries for this workout plan
        val planId = workoutPlanWithExercises.workoutplan.workoutPlanID
        deleteAllExercisesFromWorkoutPlan(planId)

        // Insert updated join table entries with new order
        workoutPlanWithExercises.exercises.forEachIndexed { order, exercise ->
            val crossRef = WorkoutPlanExerciseCrossRef(
                workoutPlanID = planId,
                id = exercise.id,
                order = order
            )
            insertExerciseToWorkoutPlan(crossRef)
        }
    }


    @Query("DELETE FROM workoutPlanWithExercises WHERE workoutPlanID = :planId")
    fun deleteAllExercisesFromWorkoutPlan(planId: Int)


    @Transaction
    @Query(
        """
SELECT * FROM exercises 
JOIN workoutPlanWithExercises ON exercises.id = workoutPlanWithExercises.id 
WHERE workoutPlanWithExercises.workoutPlanID = :workoutPlanID 
ORDER BY `order` ASC
"""
    )
    fun getExercisesForWorkoutPlanOrdered(workoutPlanID: Int): List<ExerciseEntity>


    @Transaction
    fun getWorkoutPlanWithExercisesOrdered(planId: Int): WorkoutPlanWithExercises? {
        val workoutPlanWithExercises = getWorkoutPlanWithExercises(planId)
        workoutPlanWithExercises?.exercises = getExercisesForWorkoutPlanOrdered(planId)
        return workoutPlanWithExercises
    }

    @Query("DELETE FROM workoutPlanWithExercises WHERE workoutPlanID = :planId AND id = :exerciseId")
    fun deleteExerciseFromWorkoutPlan(planId: Int, exerciseId: Int)

    @Transaction
    @Query("SELECT * FROM WorkoutPlans WHERE currentlyActive = 1 ORDER BY `order` ASC")
    fun getActiveWorkoutPlanWithExercisesOrdered(): WorkoutPlanWithExercises?


    @Transaction
    @Query("SELECT * FROM WorkoutPlans")
    fun getAllWorkoutPlansWithExercises(): List<WorkoutPlanWithExercises>

    @Transaction
    @Query("SELECT * FROM WorkoutPlans ORDER BY `order` ASC")
    fun getAllWorkoutPlansWithExercisesOrdered(): List<WorkoutPlanWithExercises>

}



