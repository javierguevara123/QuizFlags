package uta.fisei.App004.androidfunwithflags

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.flagquizapp.androidfunwithflags.R

class MenuActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        // 1. BOTÓN INICIAR JUEGO
        // Nota: Asegúrate de que en tu activity_menu.xml el botón tenga el id "btn_iniciar"
        // Si en el XML le pusiste "playButton", cambia "R.id.btn_iniciar" por "R.id.playButton" aquí.
        val btnIniciar = findViewById<Button>(R.id.btn_iniciar)
        btnIniciar.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        // --- ELIMINADO EL BOTÓN DE CONFIGURACIÓN ---
        // Ya no necesitamos ir a SettingsActivity porque las reglas son fijas.

        // 2. BOTÓN ACERCA DE (Opcional, pero recomendado)
        // Asegúrate de tener este botón en tu XML con id "btn_acerca_de"
        // Si decidiste borrarlo del XML, borra también este bloque de código.
        val btnAcercaDe = findViewById<Button>(R.id.btn_acerca_de)

        // Usamos el operador seguro (?.) por si acaso borraste el botón del XML para que no se cierre la app
        btnAcercaDe?.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Acerca de")
                .setMessage("Juego de Banderas\n\nDesarrollado por: [Tu Nombre Aquí]\nVersión: 1.0")
                .setPositiveButton("Genial", null)
                .show()
        }
    }
}