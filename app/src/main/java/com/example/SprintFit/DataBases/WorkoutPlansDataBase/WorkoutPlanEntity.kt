package com.example.SprintFit.DataBases.WorkoutPlansDataBase

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "WorkoutPlans")
data class WorkoutPlanEntity(
    @PrimaryKey(autoGenerate = true)
    var workoutPlanID: Int,
    var title: String,
    var description: String,
    var actualWorkoutTime: Int,
    var rounds: Int,
    var restBetweenRounds: Int,
    var restBetweenIntervals: Int,
    var order: Int,
    var currentlyActive: Boolean = false
) : Parcelable
