package com.example.filmus
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.TranslateAnimation
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.Serializable


class Wait : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wait)

        // UI элементы
        val returnButton = findViewById<Button>(R.id.WaitBackButton)
        val codeText = findViewById<TextView>(R.id.textViewCode)
        val createRoomButton = findViewById<Button>(R.id.WaitContinueButton)

        returnButton.setOnClickListener{ finish() }

        // Переменные для запроса
        val genreValue = intent.getStringExtra("genreValue")
        val sortValue = intent.getStringExtra("sortValue")
        val quValue = intent.getStringExtra("quanityValue")
        val waitView = findViewById<TextView>(R.id.textViewWaiting)
        val filmList = mutableListOf<Film>()
        val searchParams = Params(genreValue, sortValue)
        val gson = Gson()
        val json = gson.toJson(searchParams)
        val conn = SocketHandler

        // Создаем анимацию для появления точек
        val fadeIn = AlphaAnimation(0.0f, 1.0f)
        fadeIn.duration = 500 // Длительность анимации в миллисекундах
        fadeIn.repeatCount = Animation.INFINITE // Зацикливаем анимацию

        // Создаем анимацию для смещения точек вправо
        val translateRight = TranslateAnimation(0f, 20f, 0f, 0f)
        translateRight.duration = 500 // Длительность анимации в миллисекундах
        translateRight.repeatCount = Animation.INFINITE // Зацикливаем анимацию

        // Объединяем обе анимации
        val animationSet = AnimationSet(true)
        animationSet.addAnimation(fadeIn)
        animationSet.addAnimation(translateRight)

        // Запускаем анимацию
        waitView.startAnimation(animationSet)

        runBlocking {
            val scope = CoroutineScope(Dispatchers.IO)
            scope.launch {
                // ПОДКЛЮЧАЕМСЯ
                conn.connectSocket()
                // ОТПРАВЛЯЕМ ФЛАГ
                conn.writer.write("start".toByteArray())
                conn.writer.flush()
                // ПОЛУЧАЕМ КОД ДЛЯ ПОДКЛЮЧЕНИЯ
                val code = conn.reader.readLine()
                codeText.text = code

                // ОТПРАВЛЯЕМ ДАННЫЕ ДЛЯ ЗАПРОСА
                conn.writer.writeUTF(json)
                conn.writer.flush()

                // ПОЛУЧАЕМ ФИЛЬМЫ
                val regex = Regex("\\{.*?\\}")
                val data = withContext(Dispatchers.IO) {
                    conn.reader.readLine()
                }
                Log.d("reader conn", data)

                regex.findAll(data).forEach { result ->
                    val film = gson.fromJson(result.value, Response::class.java)
                    filmList += Film(fId=film.id, title=film.name, rating = film.rate,
                        ratingV2 = film.rateV2, year = film.year,
                        posterUrl = "https://kinopoiskapiunofficial.tech/images/posters/kp/"+ film.id +".jpg")
                }
                conn.closeSocket()

                conn.connectSocket()
                conn.writer.write("ready".toByteArray())
                conn.writer.flush()
                conn.closeSocket()

                conn.connectSocket()
                conn.writer.write("ready2".toByteArray())
                conn.writer.flush()
                val flag = conn.reader.readLine()
                if (flag == "ready"){
                    runOnUiThread{
                        createRoomButton.isEnabled = true
                    }
                }
            }
        }

        createRoomButton.setOnClickListener {
            val intent = Intent(this, Filmus::class.java)
            intent.putExtra("filmList", filmList as Serializable?)
            for (film in filmList){
                Log.d("FILM_LIST", film.title)
            }
            startActivity(intent)
        }
    }
}

