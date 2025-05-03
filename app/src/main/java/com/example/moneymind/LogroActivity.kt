package com.example.moneymind

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.moneymind.databinding.ActivityLogroBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class LogroActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLogroBinding

    private var primerLogro: String? = null
    private var segundoLogro: String? = null
    private var mostrandoSegundoLogro = false
    private var presupuestoPersonal = false
    private var comentarioPublicacion = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLogroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val titulo = intent.getStringExtra("titulo")
        val user = FirebaseAuth.getInstance().currentUser
        val uid = user?.uid

        if (user != null && uid != null) {
            val database = FirebaseDatabase.getInstance()
            val userRef = database.getReference("users/$uid")

            when (titulo) {
                "Presupuesto Personal" -> {
                    binding.textFelicitaciones.text = "Felicidades has desbloqueado el logro: Completar el primer módulo."
                    primerLogro = "primerModulo"
                    presupuestoPersonal=true
                }
                "Entendiendo los Créditos y las Deudas" -> {
                    binding.textFelicitaciones.text = "Felicidades has desbloqueado el logro: Completar 3 módulos."
                    primerLogro = "modulos3"
                    segundoLogro = "completarNivel"
                    binding.imageLogro1.setImageResource(R.drawable.logro2)
                    binding.textLogro1.text="Completar 3 módulos."
                }
                "Primera publicación" ->{
                    binding.textFelicitaciones.text = "Felicidades has desbloqueado el logro: Realizar una publicación en el foro."
                    primerLogro = "primeraPublicacion"
                    binding.imageLogro1.setImageResource(R.drawable.logro4)
                    binding.textLogro1.text="Realizar una publicación"
                    comentarioPublicacion=true
                }
                "Primer comentario" ->{
                    binding.textFelicitaciones.text = "Felicidades has desbloqueado el logro: Realizar un comentario en el foro."
                    primerLogro = "primerComentario"
                    binding.imageLogro1.setImageResource(R.drawable.logro6)
                    binding.textLogro1.text="Realizar un comentario"
                    comentarioPublicacion=true
                }
                else -> {
                    binding.textFelicitaciones.text = "No se reconoció un logro específico."
                }
            }

            binding.button.setOnClickListener {
                if(presupuestoPersonal){
                    // Finalizar actividad y regresar respuesta
                    val returnIntent = Intent()
                    returnIntent.putExtra("cargar",false)
                    setResult(Activity.RESULT_OK, returnIntent)
                    finish()
                }
                if(comentarioPublicacion){
                    val returnIntent = Intent()
                    setResult(Activity.RESULT_OK, returnIntent)
                    finish()
                }
                if (!mostrandoSegundoLogro && segundoLogro != null) {
                    // Mostrar segundo logro
                    binding.textFelicitaciones.text = "Felicidades has desbloqueado el logro: Completar un nivel."
                    binding.imageLogro1.setImageResource(R.drawable.logro3)
                    binding.textLogro1.text="Completar un nivel."
                    actualizarLogro(userRef, segundoLogro!!)
                    mostrandoSegundoLogro = true
                } else{
                    // Finalizar actividad y regresar respuesta
                    val returnIntent = Intent()
                    returnIntent.putExtra("cargar",true)
                    setResult(Activity.RESULT_OK, returnIntent)
                    finish()
                }
            }

            // Actualizar primer logro al entrar
            if (primerLogro != null) {
                actualizarLogro(userRef, primerLogro!!)
            } else {
                Toast.makeText(this, "No se desbloqueó ningún logro inicial.", Toast.LENGTH_SHORT).show()
            }

        } else {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
        }
    }

    private fun actualizarLogro(userRef: com.google.firebase.database.DatabaseReference, logro: String) {
        if(logro == "completarNivel"){
            userRef.child("nivel").setValue("intermedio")
                .addOnSuccessListener {
                    Log.e("Act", "se ha actualizado el nivel a intermedio.")
                }.addOnFailureListener{
                    Log.e("Act", "no se actualizo el nivel a intermedio.")
                }
        }

        userRef.child(logro).setValue(true)
    }
}
