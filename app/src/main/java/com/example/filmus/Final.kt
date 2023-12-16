package com.example.filmus

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.TextView
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class Final : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_final)
        recieve(this)
    }

    fun recieve(context:AppCompatActivity) {
        val conn = SocketHandler
        val gson = Gson()
        val filmList = mutableListOf<Film>()
        val layout = findViewById<LinearLayout>(R.id.layoutFinal)
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
                        filmList += Film(fId=film.id, title=film.name.toString(), rating = film.rate,
                            ratingV2 = film.rateV2, year = film.year,
                            posterUrl = "https://kinopoiskapiunofficial.tech/images/posters/kp/"+ film.id +".jpg")
                        Log.d("FILM NAME", film.name)
                    }

                    runOnUiThread {
                        for (film in filmList) {
                            Log.d("filmCycle", film.title)
                            val textView = TextView(context)
                            textView.text = film.title
                            textView.textSize = 20f
                            textView.layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            )
                            layout.addView(textView)
                        }
                    }
                }
            }
        }
    }
}