package uta.fisei.App004.androidfunwithflags

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.flagquizapp.androidfunwithflags.R
import uta.fisei.App004.eventhandlers.GuessButtonListener
import uta.fisei.App004.lifecyclehelpers.QuizViewModel
import java.io.IOException
import java.security.SecureRandom
import kotlin.math.max

class MainActivityFragment : Fragment() {
    private var random: SecureRandom? = null
    private var shakeAnimation: Animation? = null
    private var quizConstraintLayout: ConstraintLayout? = null
    private var questionNumberTextView: TextView? = null
    private var flagImageView: ImageView? = null
    private var timerTextView: TextView? = null

    private lateinit var guessTableRows: Array<TableRow?>
    private var answerTextView: TextView? = null
    private lateinit var quizViewModel: QuizViewModel

    // --- NUEVO: VARIABLE PARA CONTROLAR EL DIÁLOGO DUPLICADO ---
    private var gameOverDialog: android.app.AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.quizViewModel = ViewModelProvider(requireActivity())[QuizViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater.inflate(R.layout.fragment_main, container, false)

        val guessButtonListener: View.OnClickListener = GuessButtonListener(this)
        val answersTableLayout = view.findViewById<TableLayout>(R.id.answersTableLayout)

        this.random = SecureRandom()
        this.shakeAnimation = AnimationUtils.loadAnimation(activity, R.anim.incorrect_shake)
        this.shakeAnimation!!.repeatCount = 3

        this.quizConstraintLayout = view.findViewById(R.id.quizConstraintLayout)
        this.questionNumberTextView = view.findViewById(R.id.questionNumberTextView)
        this.flagImageView = view.findViewById(R.id.flagImageView)
        this.timerTextView = view.findViewById(R.id.timerTextView)

        this.guessTableRows = arrayOfNulls(4)
        this.answerTextView = view.findViewById(R.id.answerTextView)

        for (i in 0 until answersTableLayout.childCount) {
            try {
                if (answersTableLayout.getChildAt(i) is TableRow) {
                    this.guessTableRows[i] = answersTableLayout.getChildAt(i) as TableRow
                }
            } catch (e: ArrayStoreException) {
                Log.e(QuizViewModel.tag, "Error getting button rows on loop #$i", e)
            }
        }

        for (row in this.guessTableRows) {
            if (row != null) {
                for (column in 0 until row.childCount) {
                    row.getChildAt(column).setOnClickListener(guessButtonListener)
                }
            }
        }

        this.questionNumberTextView!!.text = getString(R.string.question, 1, QuizViewModel.flagsInQuiz)

        quizViewModel.timeLeftString.observe(viewLifecycleOwner) { timeText ->
            timerTextView?.text = timeText
        }

        quizViewModel.isGameOver.observe(viewLifecycleOwner) { isGameOver ->
            if (isGameOver) {
                disableButtons()
                showGameOverDialog()
            }
        }

        return view
    }

    fun updateGuessRows() {
        val numberOfGuessRows = this.quizViewModel.guessRows
        for (row in this.guessTableRows) {
            row?.visibility = View.GONE
        }
        for (rowNumber in 0 until numberOfGuessRows) {
            guessTableRows[rowNumber]?.visibility = View.VISIBLE
        }
    }

    fun resetQuiz() {
        // Aseguramos que cualquier diálogo anterior se cierre
        gameOverDialog?.dismiss()
        gameOverDialog = null

        this.quizViewModel.clearFileNameList()
        this.quizViewModel.setFileNameList(requireActivity().assets)
        this.quizViewModel.resetTotalGuesses()
        this.quizViewModel.resetCorrectAnswers()
        this.quizViewModel.clearQuizCountriesList()

        var flagCounter = 1
        val numberOfFlags = this.quizViewModel.fileNameList.size

        while (flagCounter <= QuizViewModel.flagsInQuiz) {
            val randomIndex = this.random!!.nextInt(numberOfFlags)
            val filename = this.quizViewModel.fileNameList[randomIndex]

            if (!this.quizViewModel.quizCountriesList.contains(filename)) {
                this.quizViewModel.quizCountriesList.add(filename)
                ++flagCounter
            }
        }

        this.updateGuessRows()
        this.quizViewModel.startQuiz()
        this.loadNextFlag()
    }

    private fun loadNextFlag() {
        val nextImage = this.quizViewModel.nextCountryFlag

        if (nextImage == null) {
            answerTextView!!.text = "¡Completaste el Quiz!"
            return
        }

        val region = nextImage.substringBefore("-")
        val name = nextImage.substringAfter("-")

        this.quizViewModel.setCorrectAnswer(nextImage)
        answerTextView!!.text = ""

        questionNumberTextView!!.text = getString(
            R.string.question,
            (quizViewModel.getCorrectAnswers() + 1), QuizViewModel.flagsInQuiz
        )

        try {
            requireActivity().assets.open("$region/$name.png").use { stream ->
                val flag = Drawable.createFromStream(stream, name)
                flagImageView!!.setImageDrawable(flag)
                animate(false)
            }
        } catch (e: IOException) {
            Log.e(QuizViewModel.tag, "Error Loading $nextImage", e)
        }

        this.quizViewModel.shuffleFilenameList()

        // Llenar botones
        for (rowNumber in 0 until this.quizViewModel.guessRows) {
            for (column in 0 until guessTableRows[rowNumber]!!.childCount) {
                val guessButton = guessTableRows[rowNumber]!!.getChildAt(column) as Button
                guessButton.isEnabled = true
                val rawName = this.quizViewModel.fileNameList[(rowNumber * 2) + column]

                if (rawName != null) {
                    guessButton.text = cleanString(rawName)
                }
            }
        }

        // Respuesta correcta
        val row = this.random!!.nextInt(this.quizViewModel.guessRows)
        val column = this.random!!.nextInt(2)
        val randomRow = guessTableRows[row]!!

        (randomRow.getChildAt(column) as Button).text = cleanString(this.quizViewModel.correctCountryName)
    }

    fun cleanString(original: String): String {
        val parts = original.split("-")
        return if (parts.size >= 3 && parts[0] == parts[1]) {
            original.substringAfter("-").substringAfter("-")
        } else {
            original.substringAfter("-")
        }.replace('_', ' ')
    }

    fun animate(animateOut: Boolean) {
        if (this.quizViewModel.getCorrectAnswers() == 0) return
        val centreX = (quizConstraintLayout!!.left + quizConstraintLayout!!.right) / 2
        val centreY = (quizConstraintLayout!!.top + quizConstraintLayout!!.bottom) / 2
        val radius = max(quizConstraintLayout!!.width, quizConstraintLayout!!.height)
        val animator: Animator
        if (animateOut) {
            animator = ViewAnimationUtils.createCircularReveal(
                quizConstraintLayout, centreX, centreY, radius.toFloat(), 0f
            )
            animator.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    loadNextFlag()
                }
            })
        } else {
            animator = ViewAnimationUtils.createCircularReveal(
                quizConstraintLayout, centreX, centreY, 0f, radius.toFloat()
            )
        }
        animator.duration = 500
        animator.start()
    }

    fun incorrectAnswerAnimation() {
        flagImageView!!.startAnimation(shakeAnimation)
        quizViewModel.applyPenalty()
        answerTextView!!.setText(R.string.incorrect_answer)
        requireContext().let {
            answerTextView!!.setTextColor(ContextCompat.getColor(it, R.color.wrong_answer))
        }
    }

    fun disableButtons() {
        for (row in this.guessTableRows) {
            if (row != null) {
                for (column in 0 until row.childCount) {
                    row.getChildAt(column).isEnabled = false
                }
            }
        }
    }

    // --- FUNCIÓN PARA REACTIVAR BOTONES AL REVIVIR ---
    private fun enableButtons() {
        for (row in this.guessTableRows) {
            if (row != null) {
                for (column in 0 until row.childCount) {
                    row.getChildAt(column).isEnabled = true
                }
            }
        }
    }

    // --- FUNCIÓN PÚBLICA PARA REVIVIR ---
    fun reviveGame() {
        // Aseguramos que el diálogo se destruya al revivir
        gameOverDialog?.dismiss()
        gameOverDialog = null

        quizViewModel.resetMistakesForRevive() // Añade tiempo
        enableButtons() // Desbloquea botones
    }

    // --- AQUÍ ESTÁ LA SOLUCIÓN DEL DOBLE DIÁLOGO ---
    fun showGameOverDialog() {
        // EL PORTERO: Si ya hay un diálogo mostrándose, ¡no dejes pasar otro!
        if (gameOverDialog != null && gameOverDialog!!.isShowing) {
            return
        }

        val builder = android.app.AlertDialog.Builder(requireContext())
        builder.setTitle(R.string.game_over_title)
        builder.setMessage(R.string.game_over_message)
        builder.setCancelable(false)

        builder.setPositiveButton(R.string.reset_game) { _, _ ->
            resetQuiz()
        }

        val mainActivity = activity as? MainActivity
        if (mainActivity != null && mainActivity.isAdReady()) {
            builder.setNeutralButton(R.string.watch_ad) { _, _ ->
                mainActivity.mostrarAnuncioRevivir()
            }
        }

        // Guardamos la referencia del diálogo para vigilarlo
        gameOverDialog = builder.show()
    }

    override fun onStop() {
        super.onStop()
        quizViewModel.stopTimer()
    }

    fun getAnswerTextView(): TextView { return answerTextView!! }
    fun getQuizViewModel(): QuizViewModel { return quizViewModel }
}