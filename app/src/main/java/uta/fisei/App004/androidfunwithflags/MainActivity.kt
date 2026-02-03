package uta.fisei.App004.androidfunwithflags

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import com.example.flagquizapp.androidfunwithflags.R
import uta.fisei.App004.lifecyclehelpers.QuizViewModel
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

class MainActivity : AppCompatActivity() {
    private var deviceIsPhone = true
    private lateinit var quizViewModel: QuizViewModel
    private var quizFragment: MainActivityFragment? = null
    private var rewardedAd: RewardedAd? = null
    private val TAG = "MainActivity"

    // Variable para controlar si el usuario vio el video completo
    private var recompensaGanada = false

    fun isAdReady(): Boolean {
        return rewardedAd != null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        quizViewModel = ViewModelProvider(this)[QuizViewModel::class.java]
        screenSetUp()

        // Botón atrás (Confirmación de salida)
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                mostrarAlertaDeSalida()
            }
        })

        // Inicializar AdMob
        MobileAds.initialize(this) {}
        cargarAnuncio()
    }

    fun cargarAnuncio() {
        val adRequest = AdRequest.Builder().build()
        // ID DE PRUEBA
        RewardedAd.load(this, "ca-app-pub-3940256099942544/5224354917",
            adRequest, object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.d(TAG, adError.toString())
                    rewardedAd = null
                }

                override fun onAdLoaded(ad: RewardedAd) {
                    Log.d(TAG, "Anuncio cargado correctamente")
                    rewardedAd = ad
                }
            })
    }

    fun mostrarAnuncioRevivir() {
        if (rewardedAd != null) {
            // 1. Reiniciamos la bandera
            recompensaGanada = false

            // 2. Configuramos qué pasa al CERRAR el anuncio
            rewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    // Esto se ejecuta cuando das clic a la "X"
                    rewardedAd = null
                    cargarAnuncio() // Pre-cargar el siguiente

                    if (recompensaGanada) {
                        // SI VIO EL VIDEO COMPLETO:
                        // Llamamos al Fragmento para que sume tiempo Y desbloquee botones
                        val fragment = supportFragmentManager.findFragmentById(R.id.quizFragment) as? MainActivityFragment
                        fragment?.reviveGame()

                        Toast.makeText(this@MainActivity, "¡Tiempo extra añadido! (+15s)", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@MainActivity, "No terminaste de ver el video", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                    rewardedAd = null
                }
            }

            // 3. Mostramos el anuncio
            rewardedAd?.show(this) { _ ->
                // Esto se ejecuta cuando el video termina (antes de cerrar)
                recompensaGanada = true
                Log.d(TAG, "Recompensa ganada, esperando cierre del anuncio...")
            }
        } else {
            Toast.makeText(this, "El anuncio no estaba listo. Intenta de nuevo.", Toast.LENGTH_SHORT).show()
            cargarAnuncio()
        }
    }

    private fun screenSetUp() {
        val screenSize = resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK
        if (screenSize == Configuration.SCREENLAYOUT_SIZE_LARGE ||
            screenSize == Configuration.SCREENLAYOUT_SIZE_XLARGE) {
            deviceIsPhone = false
        }
        if (deviceIsPhone) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }

    override fun onStart() {
        super.onStart()

        quizFragment = supportFragmentManager
            .findFragmentById(R.id.quizFragment) as? MainActivityFragment

        if (quizFragment != null) {
            // --- REGLAS FIJAS (MODO ARCADE) ---
            // 1. Forzamos siempre 4 opciones
            quizViewModel.setGuessRows("4")

            // 2. Forzamos siempre TODAS las regiones
            val allRegions = mutableSetOf("Africa", "Asia", "Europe", "North_America", "Oceania", "South_America")
            quizViewModel.setRegionsSet(allRegions)

            // Reiniciamos el Quiz (Esto inicia el Temporizador también)
            try {
                // Solo reiniciamos si es el inicio de la app, no al volver de un anuncio
                // (AdMob a veces pausa la actividad, pero no queremos reiniciar el juego al volver)
                if (quizViewModel.getCorrectAnswers() == 0) {
                    quizFragment?.resetQuiz()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun mostrarAlertaDeSalida() {
        AlertDialog.Builder(this)
            .setTitle(R.string.exit_title)
            .setMessage("Si sales ahora, se perderá tu progreso actual. ¿Estás seguro?")
            .setPositiveButton("Salir") { _, _ ->
                finish()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onSupportNavigateUp(): Boolean {
        mostrarAlertaDeSalida()
        return true
    }

    fun getQuizFragment(): MainActivityFragment? {
        return quizFragment
    }

    fun getQuizViewModel(): QuizViewModel {
        return quizViewModel
    }
}