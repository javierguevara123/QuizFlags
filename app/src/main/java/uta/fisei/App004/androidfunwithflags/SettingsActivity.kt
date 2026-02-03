package uta.fisei.App004.androidfunwithflags

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.flagquizapp.androidfunwithflags.R

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Buscamos el Toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Habilitamos el botón de "Atrás" en la barra superior
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
}