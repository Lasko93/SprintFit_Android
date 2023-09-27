package com.example.SprintFit.DataBases.WorkoutPlansDataBase


import androidx.room.*


@Dao
interface WorkoutPlanDao {

    @Query("SELECT * FROM WorkoutPlans")
    fun getAllWorkoutPlans(): List<WorkoutPlanEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOne(workoutPlan: WorkoutPlanEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(workoutPlans: List<WorkoutPlanEntity>)

    @Query("SELECT COUNT(*) FROM WorkoutPlans")
    fun getWorkoutPlanCount(): Int

    @Delete
    fun deleteWorkoutPlan(workoutPlan: WorkoutPlanEntity)

    @Update
    fun update(workoutPlan: WorkoutPlanEntity): Int

    // Query to find the highest workoutplanid in the table
    @Query("SELECT MAX(workoutplanid) FROM WorkoutPlans")
    fun getHighestWorkoutPlanId(): Int?

    @Query("UPDATE workoutplans SET currentlyActive = :isActive WHERE workoutPlanID = :workoutPlanID")
    fun setActiveWorkoutPlan(workoutPlanID: Int, isActive: Boolean)


    @Query("UPDATE workoutplans SET currentlyActive = :isActive")
    fun setAllWorkoutPlansInactive(isActive: Boolean = false)

    @Query("SELECT * FROM workoutplans WHERE currentlyActive = 1 LIMIT 1")
    fun getActiveWorkoutPlan(): WorkoutPlanEntity?

    @Query("UPDATE WorkoutPlans SET `order` = :newOrder WHERE workoutPlanID = :workoutPlanID")
    fun updateOrder(workoutPlanID: Int, newOrder: Int)

    @Query("SELECT * FROM WorkoutPlans ORDER BY `order`")
    fun getAllWorkoutPlansOrdered(): List<WorkoutPlanEntity>


}
