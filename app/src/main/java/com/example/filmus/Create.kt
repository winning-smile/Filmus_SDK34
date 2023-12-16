package com.example.filmus

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity


class Create : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create)

        val returnButton = findViewById<Button>(R.id.CreateBackButton)
        val createRoomButton = findViewById<Button>(R.id.CreateCreateButton)

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
    }
