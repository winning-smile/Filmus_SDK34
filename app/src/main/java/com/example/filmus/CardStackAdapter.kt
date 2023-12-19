package com.example.filmus

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class CardStackAdapter(
    private var films: List<Film> = emptyList()
) : RecyclerView.Adapter<CardStackAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ViewHolder(inflater.inflate(R.layout.item_spot, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val film = films[position]
        holder.name.text = film.title.toString()
        holder.fid.text = film.fId.toString()
        holder.fyear.text = film.year.toString()
        holder.rate.text = film.rateKp.toString()
        holder.ratev2.text = film.rateImdb.toString()
        Glide.with(holder.image)
            .load(film.posterUrl)
            .into(holder.image)
        holder.itemView.setOnClickListener { v ->
            Toast.makeText(v.context, film.title, Toast.LENGTH_SHORT).show()
        }
    }

    override fun getItemCount(): Int {
        return films.size
    }

    fun setSpots(spots: List<Film>) {
        this.films = spots
    }

    fun getSpots(): List<Film> {
        return films
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.item_name)
        var fid: TextView = view.findViewById(R.id.item_id)
        var fyear: TextView = view.findViewById(R.id.item_year)
        var rate: TextView = view.findViewById(R.id.item_rate)
        var ratev2: TextView = view.findViewById(R.id.item_ratev2)
        var image: ImageView = view.findViewById(R.id.item_image)
    }

}