package com.example.SprintFit.DataBases

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.SprintFit.DataBases.ExerciseDatabase.ExerciseDao
import com.example.SprintFit.DataBases.ExerciseDatabase.ExerciseEntity
import com.example.SprintFit.DataBases.WorkoutPlansDataBase.WorkoutPlanDao
import com.example.SprintFit.DataBases.WorkoutPlansDataBase.WorkoutPlanEntity
import com.example.SprintFit.DataBases.WorkoutPlansWithExercises.WorkoutPlanExerciseCrossRef
import com.example.SprintFit.DataBases.WorkoutPlansWithExercises.WorkoutPlanWithExercisesDao


@Database(
    entities = [
        ExerciseEntity::class,
        WorkoutPlanEntity::class,
        WorkoutPlanExerciseCrossRef::class
    ],
    version = 1
)
abstract class MyAppDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao
    abstract fun WorkoutPlanDao(): WorkoutPlanDao

    abstract fun WorkoutPlanWithExercisesDao(): WorkoutPlanWithExercisesDao

    companion object {
        private const val DATABASE_NAME = "myapp_Database"

        @Volatile
        private var instance: MyAppDatabase? = null

        fun getInstance(context: Context): MyAppDatabase {
            synchronized(this) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        MyAppDatabase::class.java,
                        DATABASE_NAME
                    )
                        .fallbackToDestructiveMigration() // Add this line
                        .build()
                }

                return instance!!
            }
        }

    }
}
