package com.example.filmus

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.marginStart


class Create : AppCompatActivity() {

    private lateinit var rootView: LinearLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create)

        val returnButton = findViewById<Button>(R.id.CreateBackButton)
        val createRoomButton = findViewById<Button>(R.id.CreateCreateButton)
        val addGenreButton = findViewById<Button>(R.id.CreateNewButton)
        rootView = findViewById(R.id.spinnerLayout)

        addGenreButton.setOnClickListener {
            addGenre()
        }

        returnButton.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        createRoomButton.setOnClickListener {
            val intent = Intent(this, Wait::class.java)
            val genreValue: String = findViewById<Spinner>(R.id.genreSpinner).selectedItem.toString()
            val sortValue: String = findViewById<Spinner>(R.id.sortSpinner).selectedItem.toString()
            val quanityValue: String = findViewById<Spinner>(R.id.quanitySpinner).selectedItem.toString()
            intent.putExtra("genreValue", genreValue)
            intent.putExtra("sortValue", sortValue)
            intent.putExtra("quanityValue", quanityValue)
            startActivity(intent)
            }
        }

    private fun addGenre(){
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
            // Удаляем горизонтальный LinearLayout при нажатии кнопки
            rootView.removeView(horizontalLayout)
        }

        horizontalLayout.addView(spinner)
        horizontalLayout.addView(switch)
        horizontalLayout.addView(delButton)

        rootView.addView(horizontalLayout)
    }
}
