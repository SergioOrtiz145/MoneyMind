package com.example.moneymind

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.moneymind.databinding.ActivityDetalleModuloBinding
import com.example.moneymind.model.Leccion
import com.example.moneymind.model.MiAdapter
import com.example.moneymind.model.PreguntaQuiz
import com.example.moneymind.model.Quiz
import com.example.moneymind.model.Video
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.FullscreenListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions

class DetalleModuloActivity : AppCompatActivity() {
    //variables quiz
    private lateinit var quiz: Quiz
    private var respuestasCorrectas = 0
    private var respuestas: MutableList<Boolean> = mutableListOf()
    private var opcionesSeleccionadas: MutableList<Int> = mutableListOf() // Para almacenar la opción seleccionada de cada pregunta
    //variables video
    private lateinit var binding: ActivityDetalleModuloBinding
    private lateinit var youTubePlayer: YouTubePlayer
    private var isFullscreen = false

    private var leccion1Cargada = false
    private var leccion2Cargada = false
    private var quizCargado = false
    private var videoCargado = false
    private var juegoCargado = false
    private var youTubePlayerViewInitialized = false

    private var presupuesto = 1000
    private var ahorro = 0
    private var gastos = 0

    private lateinit var logroLauncher: ActivityResultLauncher<Intent>


    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (isFullscreen) {
                youTubePlayer.toggleFullscreen()
            } else {
                finish()
            }
        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalleModuloBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Obtener los datos desde el Intent
        val leccionesJson = intent.getStringExtra("LECCIONES")
        val quizJson = intent.getStringExtra("QUIZ")
        val videoJson = intent.getStringExtra("VIDEO")
        // Verificar si el JSON del quiz no está vacío o nulo
        if (!quizJson.isNullOrEmpty()) {
            // Inicializamos la propiedad quiz correctamente con los datos del Intent
            quiz = Gson().fromJson(quizJson, Quiz::class.java)
        } else {
            // Si el JSON está vacío o nulo, lanzar un error o manejarlo
            Log.e("DetalleModuloActivity", "El JSON del quiz está vacío o es nulo.")
            return  // Salir de la función si no podemos inicializar quiz
        }

        // Convertir los JSONs a objetos
        val lecciones: List<Leccion> = Gson().fromJson(leccionesJson, object : TypeToken<List<Leccion>>() {}.type)
        val video: Video = Gson().fromJson(videoJson, Video::class.java)

        // Mostrar los título del modulo y su descripción
        binding.tituloModulo.text = intent.getStringExtra("TITULO")
        binding.descripcionModulo.text = intent.getStringExtra("DESCRIPCION")
        // Mostrar los datos de Lección 1
        binding.cardLeccion1.setOnClickListener {
            toggleVisibility(binding.expandableLeccion1)
            if (!leccion1Cargada && binding.expandableLeccion1.visibility == View.VISIBLE) {
                binding.leccion1Layout.textTituloLeccion1.text = lecciones[0].titulo
                binding.leccion1Layout.textDefinicionLeccion1.text = lecciones[0].definicion
                binding.leccion1Layout.listViewBeneficiosLeccion1.adapter = MiAdapter(lecciones[0].beneficios)
                binding.leccion1Layout.listViewBeneficiosLeccion1.layoutManager = LinearLayoutManager(this)
                binding.leccion1Layout.listViewPasosLeccion1.adapter = MiAdapter(lecciones[0].pasos)
                binding.leccion1Layout.listViewPasosLeccion1.layoutManager = LinearLayoutManager(this)
                binding.leccion1Layout.listViewConsejosLeccion1.adapter = MiAdapter(lecciones[0].consejos)
                binding.leccion1Layout.listViewConsejosLeccion1.layoutManager = LinearLayoutManager(this)
                binding.leccion1Layout.textConclusionLeccion1.text = lecciones[0].conclusion
                leccion1Cargada = true
            }
        }
        //mostrar los datos de la lección 2
        binding.cardLeccion2.setOnClickListener {
            toggleVisibility(binding.expandableLeccion2)
            if (!leccion2Cargada && binding.expandableLeccion2.visibility == View.VISIBLE) {
                binding.leccion2Layout.textTituloLeccion2.text = lecciones[1].titulo
                binding.leccion2Layout.textDefinicionLeccion2.text = lecciones[1].definicion
                binding.leccion2Layout.listViewBeneficiosLeccion2.adapter = MiAdapter(lecciones[1].beneficios)
                binding.leccion2Layout.listViewBeneficiosLeccion2.layoutManager = LinearLayoutManager(this)
                binding.leccion2Layout.listViewPasosLeccion2.adapter = MiAdapter(lecciones[1].pasos)
                binding.leccion2Layout.listViewPasosLeccion2.layoutManager = LinearLayoutManager(this)
                binding.leccion2Layout.listViewConsejosLeccion2.adapter = MiAdapter(lecciones[1].consejos)
                binding.leccion2Layout.listViewConsejosLeccion2.layoutManager = LinearLayoutManager(this)
                binding.leccion2Layout.textConclusionLeccion2.text = lecciones[1].conclusion
                leccion2Cargada = true
            }
        }
        //Mostrar el Quiz
        binding.cardQuiz.setOnClickListener{
            toggleVisibility(binding.expandableQuiz)
            if (!quizCargado && binding.expandableQuiz.visibility == View.VISIBLE) {
                mostrarPreguntas()
                // Configurar el botón "Siguiente"
                binding.quizLayout.buttonSiguiente.setOnClickListener {
                    verificarRespuestas()
                    mostrarResultado()
                }
                quizCargado = true
            }
        }
        //Mostrar el juego
        binding.cardJuego.setOnClickListener{
            toggleVisibility(binding.expandableJuego)
            if(!juegoCargado && binding.expandableJuego.visibility == View.VISIBLE){
                binding.juegoLayout.opcionAlquiler.setOnClickListener {
                    gastar(400)
                }
                binding.juegoLayout.opcionRopa.setOnClickListener {
                    gastar(150)
                }
                binding.juegoLayout.opcionComer.setOnClickListener {
                    gastar(100)
                }
                binding.juegoLayout.opcionAhorro.setOnClickListener {
                    ahorrar(200)
                }
                binding.juegoLayout.botonVerResumen.setOnClickListener {
                    mostrarResumen()
                }
                juegoCargado = true
            }
        }
        binding.atrasButton.setOnClickListener {
            if (::youTubePlayer.isInitialized) {
                binding.videoLayout.youtubePlayerView.release() // ✅ Libera el YouTubePlayerView
                youTubePlayerViewInitialized = false            // ✅ Permite reinicializar en el próximo módulo
            }
            finish() // Termina la actividad
        }
        val youTubePlayerView = binding.videoLayout.youtubePlayerView
        val fullscreenContainer = findViewById<FrameLayout>(R.id.full_screen_view_container)
        val videoId = getYoutubeVideoId(video.url)

        youTubePlayerView.enableAutomaticInitialization = false

        youTubePlayerView.addFullscreenListener(object : FullscreenListener {
            override fun onEnterFullscreen(fullscreenView: View, exitFullscreen: () -> Unit) {
                isFullscreen = true
                youTubePlayerView.visibility = View.GONE
                fullscreenContainer.visibility = View.VISIBLE
                fullscreenContainer.removeAllViews()
                fullscreenContainer.addView(fullscreenView)
                window.decorView.systemUiVisibility = (
                        View.SYSTEM_UI_FLAG_FULLSCREEN
                                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        )
            }

            override fun onExitFullscreen() {
                isFullscreen = false
                youTubePlayerView.visibility = View.VISIBLE
                fullscreenContainer.visibility = View.GONE
                fullscreenContainer.removeAllViews()
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
            }
        })

        youTubePlayerView.initialize(object : AbstractYouTubePlayerListener() {
            override fun onReady(player: YouTubePlayer) {
                youTubePlayer = player
                videoId?.let {
                    player.cueVideo(it, 0f)
                }
            }
        }, IFramePlayerOptions.Builder().controls(1).fullscreen(1).build())

        youTubePlayerViewInitialized = true
        lifecycle.addObserver(youTubePlayerView)

        binding.cardVideo.setOnClickListener {
            toggleVisibility(binding.expandableVideo)
            if (::youTubePlayer.isInitialized && binding.expandableVideo.visibility == View.VISIBLE) {
                videoId?.let {
                    youTubePlayer.cueVideo(it, 0f)
                }
            }
        }
        // 1. Registrar el launcher
        logroLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // ¡Se recibió una respuesta correcta desde LogroActivity!
                val data = result.data
                val resultado = data?.getBooleanExtra("cargar",false)
                val returnIntent = Intent()
                returnIntent.putExtra("cargar", resultado)
                setResult(Activity.RESULT_OK, returnIntent)
                finish()
            }
        }
        // 2. Usar el launcher cuando se presiona el botón
        binding.buttonCompletarModulo.setOnClickListener {
            val i = Intent(this, LogroActivity::class.java)
            val titulo = intent.getStringExtra("TITULO")
            i.putExtra("titulo", titulo)
            logroLauncher.launch(i)
        }


    }

    // Función para alternar la visibilidad de un layout expandible
    private fun toggleVisibility(view: LinearLayout) {
        if (view.visibility == View.VISIBLE) {
            view.visibility = View.GONE
        } else {
            view.visibility = View.VISIBLE
        }
    }
    fun getYoutubeVideoId(url: String): String? {
        val regex = Regex(
            "(?:youtu\\.be/|youtube\\.com/(?:watch\\?(?:.*&)?v=|embed/|v/))([\\w-]{11})",
            RegexOption.IGNORE_CASE
        )
        val match = regex.find(url)
        return match?.groups?.get(1)?.value
    }
    override fun onDestroy() {
        super.onDestroy()
        binding.videoLayout.youtubePlayerView.release()  // ✅ Liberar el YouTubePlayerView correctamente
        youTubePlayerViewInitialized = false             // ✅ Permitir re-inicialización futura
    }

    private fun mostrarPreguntas() {
        // Mostrar la primera pregunta y opciones
        val pregunta1 = quiz.preguntas[0]
        binding.quizLayout.textViewPregunta1.text = pregunta1.pregunta
        agregarOpciones(pregunta1, binding.quizLayout.opcionesPregunta1)

        // Mostrar la segunda pregunta y opciones
        val pregunta2 = quiz.preguntas[1]
        binding.quizLayout.textViewPregunta2.text = pregunta2.pregunta
        agregarOpciones(pregunta2, binding.quizLayout.opcionesPregunta2)

        // Mostrar la tercera pregunta y opciones
        val pregunta3 = quiz.preguntas[2]
        binding.quizLayout.textViewPregunta3.text = pregunta3.pregunta
        agregarOpciones(pregunta3, binding.quizLayout.opcionesPregunta3)
    }
    private fun agregarOpciones(pregunta: PreguntaQuiz, radioGroup: RadioGroup) {
        radioGroup.removeAllViews() // Limpiar las opciones previas
        for (i in pregunta.opciones.indices) {
            val radioButton = RadioButton(this)
            radioButton.text = pregunta.opciones[i]
            radioButton.id = i
            radioGroup.addView(radioButton)
        }
    }
    private fun verificarRespuestas() {
        // Verificar si las respuestas son correctas para cada pregunta
        val pregunta1 = quiz.preguntas[0]
        val seleccion1 = binding.quizLayout.opcionesPregunta1.checkedRadioButtonId
        if (seleccion1 != -1) {
            val respuesta1 = binding.quizLayout.opcionesPregunta1.findViewById<RadioButton>(seleccion1).text.toString()
            respuestas.add(respuesta1 == pregunta1.respuesta_correcta)
            opcionesSeleccionadas.add(seleccion1)
            if (respuesta1 == pregunta1.respuesta_correcta) respuestasCorrectas++
        }

        val pregunta2 = quiz.preguntas[1]
        val seleccion2 = binding.quizLayout.opcionesPregunta2.checkedRadioButtonId
        if (seleccion2 != -1) {
            val respuesta2 = binding.quizLayout.opcionesPregunta2.findViewById<RadioButton>(seleccion2).text.toString()
            respuestas.add(respuesta2 == pregunta2.respuesta_correcta)
            opcionesSeleccionadas.add(seleccion2)
            if (respuesta2 == pregunta2.respuesta_correcta) respuestasCorrectas++
        }

        val pregunta3 = quiz.preguntas[2]
        val seleccion3 = binding.quizLayout.opcionesPregunta3.checkedRadioButtonId
        if (seleccion3 != -1) {
            val respuesta3 = binding.quizLayout.opcionesPregunta3.findViewById<RadioButton>(seleccion3).text.toString()
            respuestas.add(respuesta3 == pregunta3.respuesta_correcta)
            opcionesSeleccionadas.add(seleccion3)
            if (respuesta3 == pregunta3.respuesta_correcta) respuestasCorrectas++
        }
    }

    private fun mostrarResultado() {
        // Mostrar el resultado en un TextView
        val resultado = "Tu puntaje es: $respuestasCorrectas de ${quiz.preguntas.size}"
        findViewById<TextView>(R.id.textResultado).text = resultado

        // Mostrar las respuestas con colores correctos o incorrectos
        mostrarRespuestasConColores()
    }
    private fun mostrarRespuestasConColores() {
        // Mostrar las respuestas con colores verde para correcta y rojo para incorrecta
        for (index in quiz.preguntas.indices) {
            val pregunta = quiz.preguntas[index]
            val radioGroup = when (index) {
                0 -> binding.quizLayout.opcionesPregunta1
                1 -> binding.quizLayout.opcionesPregunta2
                2 -> binding.quizLayout.opcionesPregunta3
                else -> null
            }

            radioGroup?.let {
                for (i in 0 until it.childCount) {
                    val radioButton = it.getChildAt(i) as RadioButton
                    if (radioButton.text.toString() == pregunta.respuesta_correcta) {
                        radioButton.setTextColor(Color.GREEN)  // Respuesta correcta
                    } else if (opcionesSeleccionadas.size > index && radioButton.id == opcionesSeleccionadas[index]) {
                        radioButton.setTextColor(Color.RED)  // Respuesta incorrecta
                    }
                }
            }
        }
    }
    private fun actualizarDineroRestante() {
        binding.juegoLayout.textoDineroRestante.text = "Dinero restante: $$presupuesto"
    }

    private fun gastar(monto: Int) {
        if (presupuesto >= monto) {
            presupuesto -= monto
            gastos += monto
            actualizarDineroRestante()
        } else {
            Toast.makeText(this, "No tienes suficiente dinero", Toast.LENGTH_SHORT).show()
        }
    }

    private fun ahorrar(monto: Int) {
        if (presupuesto >= monto) {
            presupuesto -= monto
            ahorro += monto
            actualizarDineroRestante()
        } else {
            Toast.makeText(this, "No tienes suficiente dinero", Toast.LENGTH_SHORT).show()
        }
    }

    private fun mostrarResumen() {
        val resultado = """
            Resumen del mes:
            - Gastaste: $$gastos
            - Ahorraste: $$ahorro
            - Dinero restante: $$presupuesto
            ${if (ahorro >= 100) "¡Buen manejo del presupuesto!" else "⚠️ Puedes ahorrar más."}
        """.trimIndent()
        binding.juegoLayout.textResultado.text = resultado
    }



}
