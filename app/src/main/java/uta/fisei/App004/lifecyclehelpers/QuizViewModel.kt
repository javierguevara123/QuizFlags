package uta.fisei.App004.lifecyclehelpers

import android.content.res.AssetManager
import android.os.CountDownTimer
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.io.IOException
import java.util.Collections
import kotlin.collections.ArrayList

class QuizViewModel : ViewModel() {

    val fileNameList: MutableList<String>
    var quizCountriesList: MutableList<String>
    private var regionsSet: MutableSet<String>? = null

    private var correctAnswer: String? = null
    private var totalGuesses = 0
    private var correctAnswers = 0
    var guessRows: Int = 0
        private set

    // VARIABLES DEL TIMER
    private var timer: CountDownTimer? = null
    private var timeLeftInMillis = TOTAL_TIME_IN_MILLIS

    private val _timeLeftString = MutableLiveData<String>()
    val timeLeftString: LiveData<String> get() = _timeLeftString

    private val _isGameOver = MutableLiveData<Boolean>()
    val isGameOver: LiveData<Boolean> get() = _isGameOver

    init {
        fileNameList = ArrayList()
        quizCountriesList = ArrayList()
    }

    companion object {
        const val tag: String = "FlagQuiz Activity"
        const val flagsInQuiz: Int = 20
        const val TOTAL_TIME_IN_MILLIS = 90000L
        const val PENALTY_TIME = 3000L
    }

    fun startQuiz() {
        correctAnswers = 0
        totalGuesses = 0
        timeLeftInMillis = TOTAL_TIME_IN_MILLIS
        _isGameOver.value = false
        startTimer()
    }

    private fun startTimer() {
        timer?.cancel()
        timer = object : CountDownTimer(timeLeftInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                updateTimerText()
            }

            override fun onFinish() {
                timeLeftInMillis = 0
                updateTimerText()
                _isGameOver.value = true
            }
        }.start()
    }

    fun stopTimer() { timer?.cancel() }

    fun applyPenalty() {
        if (timeLeftInMillis > PENALTY_TIME) {
            timeLeftInMillis -= PENALTY_TIME
            startTimer()
        } else {
            timeLeftInMillis = 0
            _isGameOver.value = true
        }
    }

    private fun updateTimerText() {
        val minutes = (timeLeftInMillis / 1000) / 60
        val seconds = (timeLeftInMillis / 1000) % 60
        _timeLeftString.value = String.format("Tiempo: %02d:%02d", minutes, seconds)
    }

    fun resetMistakesForRevive() {
        timeLeftInMillis += 15000
        _isGameOver.value = false
        startTimer()
    }

    // --- CORRECCIÓN CLAVE 1: GUARDAR COMO REGION-NOMBRE ---
    fun setFileNameList(assets: AssetManager) {
        fileNameList.clear()
        try {
            for (region in regionsSet!!) {
                val paths = assets.list(region)
                if (paths != null) {
                    for (path in paths) {
                        // Guardamos: "Africa-Algeria"
                        fileNameList.add("$region-${path.replace(".png", "")}")
                    }
                }
            }
        } catch (e: IOException) {
            Log.e(tag, "Error loading image file names", e)
        }
    }

    fun clearFileNameList() { fileNameList.clear() }

    fun shuffleFilenameList() {
        Collections.shuffle(fileNameList)
        val correctIndex = fileNameList.indexOf(correctAnswer)
        if (correctIndex >= 0) {
            fileNameList.add(fileNameList.removeAt(correctIndex))
        }
    }

    fun clearQuizCountriesList() { quizCountriesList.clear() }

    fun setRegionsSet(regions: MutableSet<String>) { this.regionsSet = regions }

    // --- CORRECCIÓN CLAVE 2: OBTENER EL NOMBRE CORRECTAMENTE ---
    val correctCountryName: String
        get() {
            if (correctAnswer == null) return ""
            // Formato "Region-Nombre". Queremos lo que está DESPUÉS del guion.
            return correctAnswer!!.substring(correctAnswer!!.indexOf('-') + 1).replace('_', ' ')
        }

    fun setCorrectAnswer(correctAnswer: String) { this.correctAnswer = correctAnswer }
    fun getCorrectAnswers(): Int { return correctAnswers }
    fun incrementCorrectAnswers() { correctAnswers++ }
    fun resetCorrectAnswers() { correctAnswers = 0 }
    fun resetTotalGuesses() { totalGuesses = 0 }

    fun setGuessRows(choices: String?) {
        this.guessRows = choices?.toInt()?.div(2) ?: 2
    }

    val nextCountryFlag: String?
        get() = if (quizCountriesList.isNotEmpty()) quizCountriesList.removeAt(0) else null

    var currentMistakes = 0
        private set
    fun incrementMistakes() { currentMistakes++ }
}