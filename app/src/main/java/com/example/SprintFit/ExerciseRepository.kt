import com.example.sevenminutesworkkout.BuildConfig
import com.example.SprintFit.DataBases.ExerciseDatabase.ExerciseEntity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request


class ExerciseRepository {

    val equipmentListHiitWorkout = listOf(
        "assisted", "band", "body weight", "bosu ball",
        "kettlebell", "medicine ball", "resistance band", "roller", "rope",
        "stability ball", "tire", "wheel roller", "weighted"
    )


    //Download the Jsonfile and formatting it into a List for my Database
    private val client = OkHttpClient()
    private val url = "https://exercisedb.p.rapidapi.com/exercises"
    private val apiKey = BuildConfig.API_KEY
    private val apiHost = "exercisedb.p.rapidapi.com"

    suspend fun fetchExercises(): List<ExerciseEntity>? = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(url)
            .get()
            .addHeader("X-RapidAPI-Key", apiKey)
            .addHeader("X-RapidAPI-Host", apiHost)
            .build()

        val response = client.newCall(request).execute()
        val json = response.body?.string()
        val allExercises = Gson().fromJson<List<ExerciseEntity>>(
            json,
            object : TypeToken<List<ExerciseEntity>>() {}.type
        )

        // Filter exercises based on equipment list
        val filteredExercises = allExercises?.filter { exercise ->
            exercise.equipment in equipmentListHiitWorkout
        }

        return@withContext filteredExercises
    }

}


