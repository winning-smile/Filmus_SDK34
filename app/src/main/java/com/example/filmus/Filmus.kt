package com.example.filmus

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.DefaultItemAnimator
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.gson.Gson
import com.yuyakaido.android.cardstackview.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.*

class Filmus : AppCompatActivity(), CardStackListener {
    private val filmList: MutableList<Film>? by lazy { intent.getSerializableExtra("filmList") as? MutableList<Film> }
    private val drawerLayout by lazy { findViewById<DrawerLayout>(R.id.drawer_layout) }
    private val cardStackView by lazy { findViewById<CardStackView>(R.id.card_stack_view) }
    private val manager by lazy { CardStackLayoutManager(this, this) }
    private val adapter by lazy { CardStackAdapter(createSpots()) }
    private val idList = mutableListOf<String>()
    private val dirList = mutableListOf<String>()
    private val soloCheck by lazy { intent.getStringExtra("soloChecks")}
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_filmus)
        setupCardStackView()
        val bottomSheet = findViewById<View>(R.id.bottom_sheet)
        val behavior = BottomSheetBehavior.from(bottomSheet)
        behavior.state = BottomSheetBehavior.STATE_HIDDEN
        setupButton()
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawers()
        } else {
            super.onBackPressed()
        }
    }

    override fun onCardDragging(direction: Direction, ratio: Float) {
        Log.d("CardStackView", "onCardDragging: d = ${direction.name}, r = $ratio")
    }

    override fun onCardSwiped(direction: Direction) {
        dirList.add(direction.toString())
    }

    override fun onCardRewound() {
        Log.d("CardStackView", "onCardRewound: ${manager.topPosition}")
    }

    override fun onCardCanceled() {
        Log.d("CardStackView", "onCardCanceled: ${manager.topPosition}")
    }

    override fun onCardAppeared(view: View, position: Int) {
        updateBottomSheet(view.findViewById<TextView>(R.id.item_name).text.toString(),
            view.findViewById<TextView>(R.id.item_year).text.toString(),
            view.findViewById<TextView>(R.id.item_rate).text.toString(),
            view.findViewById<TextView>(R.id.item_ratev2).text.toString())
        idList.add(view.findViewById<TextView>(R.id.item_id).text.toString())
    }

    private fun updateBottomSheet(p1:String, p2:String, p3:String, p4:String){
        val view = findViewById<View>(R.id.bottom_sheet)
        val titleView = view.findViewById<TextView>(R.id.bottom_sheet_title)
        titleView.text = p1
        val yearView = view.findViewById<TextView>(R.id.bottom_sheet_year)
        yearView.text = p2
        val rateView = view.findViewById<TextView>(R.id.bottom_sheet_rating)
        rateView.text = p3
        val rate2View = view.findViewById<TextView>(R.id.bottom_sheet_rating_v2)
        rate2View.text = p4
    }

    override fun onCardDisappeared(view: View, position: Int) {
        Log.d("FILM COUNT", manager.topPosition.toString())
        if (manager.topPosition == adapter.itemCount - 1) {
            val context = this
            if (soloCheck == "false") {
                runBlocking {
                    val conn = SocketHandler
                    val scope = CoroutineScope(Dispatchers.IO)
                    val data: Map<String, String> = idList.zip(dirList).toMap()
                    val gson = Gson()
                    val json = gson.toJson(data)

                    val job = scope.launch {
                        // ПОДКЛЮЧАЕМСЯ
                        conn.connectSocket()
                        // ОТПРАВЛЯЕМ ФЛАГ
                        conn.writer.write("result".toByteArray())
                        conn.writer.flush()
                        // ОТПРАВЛЯЕМ ДАННЫЕ О ФИЛЬМАХ
                        conn.writer.writeUTF(json)
                        conn.writer.flush()
                        val intent = Intent(context, Final::class.java)
                        intent.putExtra("soloChecks", "false")
                        startActivity(intent)
                    }
                }

            } else {
                runBlocking {
                    val conn = SocketHandler
                    val scope = CoroutineScope(Dispatchers.IO)
                    val data: Map<String, String> = idList.zip(dirList).toMap()
                    val gson = Gson()
                    val json = gson.toJson(data)

                    val job = scope.launch {
                        // ПОДКЛЮЧАЕМСЯ
                        conn.connectSocket()
                        // ОТПРАВЛЯЕМ ФЛАГ
                        conn.writer.write("resultSolo".toByteArray())
                        conn.writer.flush()
                        // ОТПРАВЛЯЕМ ДАННЫЕ О ФИЛЬМАХ
                        conn.writer.writeUTF(json)
                        conn.writer.flush()

                        val intent = Intent(context, Final::class.java)
                        intent.putExtra("soloChecks", "true")
                        startActivity(intent)
                    }
                }
            }
        }
    }
    private fun setupCardStackView() {
        initialize()
    }

    private fun setupButton() {
        val skip = findViewById<View>(R.id.skip_button)
        skip.setOnClickListener {
            val setting = SwipeAnimationSetting.Builder()
                .setDirection(Direction.Left)
                .setDuration(Duration.Normal.duration)
                .setInterpolator(AccelerateInterpolator())
                .build()
            manager.setSwipeAnimationSetting(setting)
            cardStackView.swipe()
        }

        val rewind = findViewById<View>(R.id.rewind_button)
        val bottomSheet = findViewById<View>(R.id.bottom_sheet)
        val behavior = BottomSheetBehavior.from(bottomSheet)

        rewind.setOnClickListener {
            Log.d("REWIND BUTTON CLICK", "YEAH")

            if (behavior.state != BottomSheetBehavior.STATE_EXPANDED) {
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                Log.d("expanded", "true")
            } else {
                behavior.state = BottomSheetBehavior.STATE_COLLAPSED
                behavior.state = BottomSheetBehavior.STATE_HIDDEN
                Log.d("expanded", "false")
            }
        }

        val like = findViewById<View>(R.id.like_button)
        like.setOnClickListener {
            val setting = SwipeAnimationSetting.Builder()
                .setDirection(Direction.Right)
                .setDuration(Duration.Normal.duration)
                .setInterpolator(AccelerateInterpolator())
                .build()
            manager.setSwipeAnimationSetting(setting)
            cardStackView.swipe()
        }
    }

    private fun initialize() {
        manager.setStackFrom(StackFrom.None)
        manager.setVisibleCount(3)
        manager.setTranslationInterval(8.0f)
        manager.setScaleInterval(0.95f)
        manager.setSwipeThreshold(0.3f)
        manager.setMaxDegree(20.0f)
        manager.setDirections(Direction.HORIZONTAL)
        manager.setCanScrollHorizontal(true)
        manager.setCanScrollVertical(false)
        manager.setSwipeableMethod(SwipeableMethod.AutomaticAndManual)
        manager.setOverlayInterpolator(LinearInterpolator())
        cardStackView.layoutManager = manager
        cardStackView.adapter = adapter
        cardStackView.itemAnimator.apply {
            if (this is DefaultItemAnimator) {
                supportsChangeAnimations = false
            }
        }
    }


    private fun createSpots(): List<Film> {
        val spots = ArrayList<Film>()
        Log.d("FILM_LIST_SIZE", filmList!!.size.toString())
        for (i in 0 until filmList!!.size) {
            spots.add(
                Film(
                    fId = filmList!![i].fId,
                    title = filmList!![i].title,
                    rateKp = filmList!![i].rateKp,
                    rateImdb = filmList!![i].rateImdb,
                    bio = filmList!![i].bio,
                    year = filmList!![i].year,
                    posterUrl = filmList!![i].posterUrl
                )
            )
        }

        spots.add(Film(fId=666, title="THE END OF SWIPING", rateKp = 0F, rateImdb = 0F, year = 0, bio = "poop", posterUrl = "https://images-na.ssl-images-amazon.com/images/I/71P0iCaWUTL._SL1000_.jpg"))
        return spots
    }

}
