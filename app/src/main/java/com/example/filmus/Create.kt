package com.example.filmus

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.Serializable



class Create : AppCompatActivity() {
    private val genres = mutableListOf<LinearLayout>()
    private lateinit var rootView: LinearLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create)

        val limitDict = mapOf("10 фильмов" to "10", "25 фильмов" to "25",
            "50 фильмов" to "50", "75 фильмов" to "75", "100 фильмов" to "100")

        val sortDict = mapOf("По названию" to "name", "По дате выхода" to "year",
            "По рейтингу КиноПоиска" to "rating.kp", "По рейтингу IMDB" to "rating.imdb",
            "По бюджету" to "budget.value", "По длинне фильма" to "movieLenght")

        val returnButton = findViewById<Button>(R.id.CreateBackButton)
        val createRoomButton = findViewById<Button>(R.id.CreateCreateButton)
        val addGenreButton = findViewById<Button>(R.id.CreateNewButton)
        val soloCheck = findViewById<Button>(R.id.soloSwitch)
        var soloCheckBool = false
        val genreField = findViewById<Spinner>(R.id.genreSpinner)
        val sortField = findViewById<Spinner>(R.id.sortSpinner)
        val yearFromField = findViewById<TextInputEditText>(R.id.yearFrom)
        val yearToField = findViewById<TextInputEditText>(R.id.yearTo)
        val rateFromField = findViewById<TextInputEditText>(R.id.ratingFrom)
        val rateToField = findViewById<TextInputEditText>(R.id.ratingTo)
        val quanityField  = findViewById<Spinner>(R.id.quanitySpinner)
        var newGenres = mutableListOf<LinearLayout>()

        rootView = findViewById(R.id.spinnerLayout)

        addGenreButton.setOnClickListener {
            newGenres = addGenre()
        }

        soloCheck.setOnClickListener {
            soloCheckBool = !soloCheckBool
            Log.d("SoloCheck", soloCheckBool.toString())
        }

        returnButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        createRoomButton.setOnClickListener {
            val context = this
            if (soloCheckBool) {
                runBlocking {
                    val scope = CoroutineScope(Dispatchers.IO)
                    scope.launch {
                        val intent = Intent(context, Filmus::class.java)
                        var genreMinus = arrayOf<String>()
                        var genrePlus = arrayOf<String>()
                        genrePlus += genreField.selectedItem.toString()

                        for (obj in newGenres){
                            if ((obj.getChildAt(1) as Switch).isChecked){
                                genreMinus += (obj.getChildAt(0) as Spinner).selectedItem.toString()
                                Log.d("Minus", (obj.getChildAt(0) as Spinner).selectedItem.toString())
                            }
                            else{
                                genrePlus += (obj.getChildAt(0) as Spinner).selectedItem.toString()
                                Log.d("Plus", (obj.getChildAt(0) as Spinner).selectedItem.toString())
                            }
                        }



                        val filmList = mutableListOf<Film>()
                        val searchParams = Params(limitDict[quanityField.selectedItem.toString()],
                            sortDict[sortField.selectedItem.toString()], yearFromField.text.toString(),
                            yearToField.text.toString(), rateFromField.text.toString(),
                            rateToField.text.toString(), genrePlus, genreMinus)

                        val gson = Gson()
                        val json = gson.toJson(searchParams)
                        val conn = SocketHandler
                        val flag = "true"
                        // ПОДКЛЮЧАЕМСЯ
                        conn.connectSocket()
                        // ОТПРАВЛЯЕМ ФЛАГ
                        conn.writer.write("startSolo".toByteArray())
                        conn.writer.flush()

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
                            filmList += Film(
                                fId = film.fId, title = film.title, rateKp = film.rateKp,
                                rateImdb = film.rateImdb, bio = film.bio, year = film.year,
                                posterUrl = film.posterUrl
                            )
                        }

                        intent.putExtra("soloChecks", "true")
                        intent.putExtra("filmList", filmList as Serializable?)
                        for (film in filmList){
                            Log.d("FILM_LIST", film.title)
                        }

                        startActivity(intent)
                    }
                }
            }

            else{
                val intent = Intent(this, Wait::class.java)
                var genreMinus = arrayOf<String>()
                var genrePlus = arrayOf<String>()
                genrePlus += genreField.selectedItem.toString()

                for (obj in newGenres){
                    if ((obj.getChildAt(1) as Switch).isChecked){
                        genreMinus += (obj.getChildAt(0) as Spinner).selectedItem.toString()
                        Log.d("Minus", (obj.getChildAt(0) as Spinner).selectedItem.toString())
                    }
                    else{
                        genrePlus += (obj.getChildAt(0) as Spinner).selectedItem.toString()
                        Log.d("Plus", (obj.getChildAt(0) as Spinner).selectedItem.toString())
                    }
                }
                intent.putExtra("limit", limitDict[quanityField.selectedItem.toString()])
                intent.putExtra("sortField", sortDict[sortField.selectedItem.toString()])
                intent.putExtra("yearFrom", yearFromField.text.toString())
                intent.putExtra("yearTo", yearToField.text.toString())
                intent.putExtra("rateFrom", rateFromField.text.toString())
                intent.putExtra("rateTo", rateToField.text.toString())
                intent.putExtra("genres", genrePlus)
                intent.putExtra("genresMinus", genreMinus)
                intent.putExtra("soloChecks", "false")
                startActivity(intent)
            }
        }
    }

    private fun addGenre(): MutableList<LinearLayout> {
        // ГОРИЗОНТАЛЬНЫЙ СЛОЙ
        val horizontalLayout = LinearLayout(this)
        horizontalLayout.orientation = LinearLayout.HORIZONTAL
        horizontalLayout.gravity = Gravity.CENTER

        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(60, 30, 60, 30) // левый, верхний, правый, нижний отступы
        horizontalLayout.layoutParams = layoutParams

        // СПИНЕР
        val spinner = Spinner(this)
        val spinnerParams = LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1f
        )
        spinner.layoutParams = spinnerParams

        val spinnerItems = resources.getStringArray(R.array.genreItems)
        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, spinnerItems)
        spinner.adapter = adapter

        // СВИТЧ
        val switch = Switch(this)
        val switchParams = LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1f
        )
        switch.layoutParams = switchParams

        // КНОПКА
        val delButton = Button(this)
        delButton.setBackgroundResource(R.drawable.ic_action_name)
        val drawable = resources.getDrawable(R.drawable.ic_action_name, null)
        val imageWidth = drawable.intrinsicWidth
        val imageHeight = drawable.intrinsicHeight
        val delButtonParams = LinearLayout.LayoutParams(imageWidth, imageHeight)
        delButton.layoutParams = delButtonParams
        delButton.setOnClickListener {
            genres.remove(horizontalLayout)
            // Удаляем горизонтальный LinearLayout при нажатии кнопки
            rootView.removeView(horizontalLayout)
        }

        horizontalLayout.addView(spinner)
        horizontalLayout.addView(switch)
        horizontalLayout.addView(delButton)

        genres += horizontalLayout
        rootView.addView(horizontalLayout)

        return genres
    }
}
