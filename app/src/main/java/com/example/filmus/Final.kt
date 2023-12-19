package com.example.filmus

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class Final : AppCompatActivity() {
    private val soloCheck by lazy { intent.getStringExtra("soloChecks")}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_final)
        recieve(this)

        val closeButton = findViewById<Button>(R.id.FinalBackButton)

        closeButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun recieve(context:AppCompatActivity) {
        val conn = SocketHandler
        val gson = Gson()
        val filmList = mutableListOf<Film>()
        val layout = findViewById<LinearLayout>(R.id.layoutFinal)
        if (soloCheck == "true") {
            runBlocking {
                val scope = CoroutineScope(Dispatchers.IO)
                val job = scope.launch {
                    // ПОДКЛЮЧАЕМСЯ
                    conn.connectSocket()
                    // ОТПРАВЛЯЕМ ФЛАГ
                    conn.writer.write("finalSolo".toByteArray())
                    conn.writer.flush()

                    // ПОЛУЧАЕМ ФИЛЬМЫ
                    val regex = Regex("\\{.*?\\}")

                    val data: String? = conn.reader.readLine()

                    if (data != null) {
                        regex.findAll(data).forEach { result ->
                            val film = gson.fromJson(result.value, Response::class.java)
                            filmList += Film(
                                fId = film.id, title = film.name.toString(), rating = film.rate,
                                ratingV2 = film.rateV2, year = film.year,
                                posterUrl = "https://kinopoiskapiunofficial.tech/images/posters/kp/" + film.id + ".jpg"
                            )
                            Log.d("FILM NAME", film.name)
                        }

                        runOnUiThread {
                            for (film in filmList) {
                                Log.d("filmCycle", film.title)

                                val textView = createTextView(context)
                                textView.text = film.title
                                layout.addView(textView)
                            }
                        }
                    }
                }
            }
        }

        else {
            runBlocking {
                val scope = CoroutineScope(Dispatchers.IO)
                val job = scope.launch {
                    // ПОДКЛЮЧАЕМСЯ
                    conn.connectSocket()
                    // ОТПРАВЛЯЕМ ФЛАГ
                    conn.writer.write("final".toByteArray())
                    conn.writer.flush()

                    // ПОЛУЧАЕМ ФИЛЬМЫ
                    val regex = Regex("\\{.*?\\}")

                    val data: String? = conn.reader.readLine()

                    Log.d("DATA_skipped", "true")
                    if (data != null) {
                        regex.findAll(data).forEach { result ->
                            val film = gson.fromJson(result.value, Response::class.java)
                            filmList += Film(
                                fId = film.id, title = film.name.toString(), rating = film.rate,
                                ratingV2 = film.rateV2, year = film.year,
                                posterUrl = "https://kinopoiskapiunofficial.tech/images/posters/kp/" + film.id + ".jpg"
                            )
                            Log.d("FILM NAME", film.name)
                        }

                        runOnUiThread {
                            for (film in filmList) {
                                Log.d("filmCycle", film.title)
                                val textView = TextView(context)
                                textView.setTextAppearance(R.style.FinalTextView)
                                textView.background = AppCompatResources.getDrawable(context, R.drawable.default_button_shape)
                                textView.text = film.title
                                layout.addView(textView)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun createTextView(context:AppCompatActivity): TextView {
        val textView = TextView(context)
        val widthInDp = 300
        val widthInPixels = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            widthInDp.toFloat(),
            resources.displayMetrics
        ).toInt()

        val heightInDp = 60
        val heightInPixels = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            heightInDp.toFloat(),
            resources.displayMetrics
        ).toInt()

        val layoutParams = LinearLayout.LayoutParams(
            widthInPixels, // Конвертируйте dp в пиксели
            heightInPixels
        )

        val marginTopInDp = 20
        val marginInPixels = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            marginTopInDp.toFloat(),
            resources.displayMetrics
        ).toInt()
        layoutParams.setMargins(0, marginInPixels, 0, 0)
        layoutParams.gravity = Gravity.CENTER

        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20F);
        val paddingInPixels = dpToPx(20)
        val paddingInPixelsTop = dpToPx(10)
        textView.setPadding(paddingInPixels, paddingInPixelsTop, paddingInPixels, 0)
        textView.layoutParams = layoutParams
        textView.background = AppCompatResources.getDrawable(context, R.drawable.default_button_shape)

        return textView
    }

    private fun AppCompatActivity.dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp.toFloat() * density).toInt()
    }
}