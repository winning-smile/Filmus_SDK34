package com.example.filmus

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.Serializable

val codeDict = mapOf('A' to '0', 'B' to '1', 'C' to '2', 'D' to '3', 'E' to '4',
    'F' to '5', 'G' to '6', 'H' to '7', 'I' to '8', 'J' to '9')

class Join : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_join)

        val gson = Gson()
        val filmList = mutableListOf<Film>()

        val returnButton = findViewById<Button>(R.id.JoinBackButton)
        val joinButton = findViewById<Button>(R.id.JoinJoinButton)
        val connectButton = findViewById<Button>(R.id.JoinConnectionButton)
        val codeJoinField = findViewById<TextView>(R.id.JoinTextInput)
        val joinCode = codeJoinField.text
        var test = 50000
        val conn = SocketHandler

        returnButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        connectButton.setOnClickListener{
            runBlocking {
                val scope = CoroutineScope(Dispatchers.IO)
                scope.launch {
                    val port = codeToPort(joinCode.toString())
                    // ПОДКЛЮЧАЕМСЯ
                    conn.connectSocket(port)
                    // ОТПРАВЛЯЕМ ФЛАГ
                    conn.writer.write("join".toByteArray())
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
                    runOnUiThread {
                        connectButton.isEnabled = false
                        joinButton.isEnabled = true
                    }
                }
            }
        }


        joinButton.setOnClickListener {
            val context = this
            runBlocking {
                val scope = CoroutineScope(Dispatchers.IO)
                scope.launch {
                    // ПОДКЛЮЧАЕМСЯ
                    conn.connectSocket()
                    // ОТПРАВЛЯЕМ ФЛАГ
                    conn.writer.write("ready".toByteArray())
                    conn.writer.flush()
                    conn.closeSocket()
                    val intent = Intent(context, Filmus::class.java)
                    intent.putExtra("filmList", filmList as Serializable?)
                    startActivity(intent)
                }
            }

        }
    }

    private fun codeToPort(code: String): Int {
        var num: String = ""
        val mapping = mapOf('A' to 0, 'B' to 1, 'C' to 2, 'D' to 3, 'E' to 4
            , 'F' to 5, 'G' to 6, 'H' to 7, 'I' to 8, 'J' to 9)

        for (letter in code){
            num += mapping[letter]
        }
        Log.d("PortToCde", num)
        return num.toInt()
    }
}