package com.example.filmus

import java.io.Serializable

data class Film(val id: Long = counter++, val fId: Int, val title: String, val rateKp: Float, val rateImdb: Float, val bio: String, val year: Int, val posterUrl:String) : Serializable {
    companion object {
        private var counter = 0L
    }
}

data class Params(val limit:String?, val sortField:String?, val yearFrom:String? = "default",
                  val yearTo:String? = "default", val rateFrom:String? = "default",
                  val rateTo:String? = "default", val genrePlus:Array<String?>,
                  val genreMinus:String? = "default")
data class Response(val fId: Int, val title: String, val rateKp: Float, val rateImdb: Float, val bio: String, val year: Int, val posterUrl:String)

