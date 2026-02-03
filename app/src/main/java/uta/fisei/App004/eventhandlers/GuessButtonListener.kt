package uta.fisei.App004.eventhandlers

import android.app.AlertDialog
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.View.OnClickListener
import android.widget.Button
import androidx.core.content.ContextCompat
import uta.fisei.App004.androidfunwithflags.MainActivityFragment
import uta.fisei.App004.lifecyclehelpers.QuizViewModel
import com.example.flagquizapp.androidfunwithflags.R

class GuessButtonListener(private val mainActivityFragment: MainActivityFragment) : OnClickListener {

    private val handler = Handler(Looper.getMainLooper())

    override fun onClick(v: View) {
        val guessButton = v as Button
        val rawGuess = guessButton.text.toString() // Esto ya viene limpio desde el botón (Ej: Yibuti)

        // Obtenemos la respuesta cruda (Ej: Africa-Africa-Yibuti)
        val rawAnswer = mainActivityFragment.getQuizViewModel().correctCountryName

        // Limpiamos la respuesta cruda con la MISMA lógica del fragmento
        val cleanAnswer = mainActivityFragment.cleanString(rawAnswer)

        // Comparamos
        if (rawGuess.equals(cleanAnswer, ignoreCase = true)) {
            // --- CORRECTO ---
            val viewModel = mainActivityFragment.getQuizViewModel()
            viewModel.incrementCorrectAnswers()

            val answerTextView = mainActivityFragment.getAnswerTextView()
            answerTextView.text = "$cleanAnswer!"
            answerTextView.setTextColor(ContextCompat.getColor(mainActivityFragment.requireContext(), R.color.correct_answer))

            mainActivityFragment.disableButtons()

            if (viewModel.getCorrectAnswers() == QuizViewModel.flagsInQuiz) {
                viewModel.stopTimer()
                AlertDialog.Builder(mainActivityFragment.requireContext())
                    .setTitle("¡Felicidades!")
                    .setMessage("Has completado las 20 banderas.")
                    .setCancelable(false)
                    .setPositiveButton("Reiniciar") { _, _ ->
                        mainActivityFragment.resetQuiz()
                    }
                    .show()
            } else {
                handler.postDelayed({
                    mainActivityFragment.animate(true)
                }, 1000)
            }
        } else {
            // --- INCORRECTO ---
            mainActivityFragment.incorrectAnswerAnimation()
            guessButton.isEnabled = false
        }
    }
}