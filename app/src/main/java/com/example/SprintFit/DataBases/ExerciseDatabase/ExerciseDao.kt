package com.example.SprintFit.DataBases.ExerciseDatabase


import androidx.room.*


@Dao
interface ExerciseDao {

    @Query("SELECT DISTINCT bodyPart FROM exercises")
    fun getAllBodyParts(): List<String>

    @Query("SELECT DISTINCT equipment FROM exercises")
    fun getAllEquipments(): List<String>

    @Query("SELECT * FROM exercises")
    fun getAllExercises(): List<ExerciseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(exercises: List<ExerciseEntity>)

    @Query("SELECT COUNT(*) FROM exercises")
    fun getExerciseCount(): Int


    @Query("SELECT COUNT(*) FROM exercises WHERE target IN (:selectedMuscleCategories) OR equipment IN (:selectedEquipmentTypes)")
    fun getFilteredExercisesCount(
        selectedMuscleCategories: List<String>,
        selectedEquipmentTypes: List<String>
    ): Int

}