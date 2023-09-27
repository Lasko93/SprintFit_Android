package com.example.SprintFit.DataBases.ExerciseDatabase

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "exercises")

data class ExerciseEntity(
    @PrimaryKey(autoGenerate = false)
    val id: Int,
    var name: String,
    val bodyPart: String,
    val equipment: String,
    var gifUrl: String,
    var target: String,
    var isChecked: Boolean = false
)
