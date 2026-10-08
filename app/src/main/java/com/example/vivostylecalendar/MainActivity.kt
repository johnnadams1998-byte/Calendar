package com.example.vivostylecalendar

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : Activity() {
    private val month = Calendar.getInstance()
    private val today = Calendar.getInstance()
    private var selected = Calendar.getInstance()
    private lateinit var title: TextView
    private lateinit var grid: GridLayout
    private lateinit var info: TextView

    private val bg = Color.rgb(250, 247, 250)
    private val text = Color.rgb(75, 75, 75)
    private val muted = Color.rgb(170, 170, 170)
    private val accent = Color.rgb(80, 145, 220)
    private val selectedGray = Color.rgb(105, 105, 105)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = bg
        window.navigationBarColor = bg
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        build()
        render()
    }

    private fun tv(s: String, size: Float, color: Int = text): TextView =
        TextView(this).apply {
            text = s; textSize = size; setTextColor(color)
            gravity = Gravity.CENTER_VERTICAL
            includeFontPadding = true
        }

    private fun build() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(bg)
            setPadding(22, 12, 22, 8)
        }

        val top = LinearLayout(this).apply {
            gravity = Gravity.CENTER_VERTICAL
        }
        title = tv("", 27f).apply { typeface = Typeface.create("sans", Typeface.NORMAL) }

        val calButton = tv("▣", 22f).apply {
            gravity = Gravity.CENTER
            setOnClickListener { month.time = today.time; selected.time = today.time; render() }
        }
        val addButton = tv("+", 34f).apply {
            gravity = Gravity.CENTER
            setOnClickListener { /* reserved for future events */ }
        }
        val more = tv("⋮", 28f).apply { gravity = Gravity.CENTER }

        top.addView(title, LinearLayout.LayoutParams(0, 58, 1f))
        top.addView(calButton, LinearLayout.LayoutParams(48, 58))
        top.addView(addButton, LinearLayout.LayoutParams(48, 58))
        top.addView(more, LinearLayout.LayoutParams(38, 58))
        root.addView(top)

        val week = GridLayout(this).apply { columnCount = 7 }
        listOf("Sun","Mon","Tue","Wed","Thu","Fri","Sat").forEach {
            val v = tv(it, 13f, muted)
            v.gravity = Gravity.CENTER
            week.addView(v, cell(42))
        }
        root.addView(week)

        grid = GridLayout(this).apply { columnCount = 7; useDefaultMargins = false }
        root.addView(grid, LinearLayout.LayoutParams(-1, 0, 1f))

        info = tv("", 15f, text)
        info.setPadding(4, 8, 0, 10)
        root.addView(info, LinearLayout.LayoutParams(-1, 42))

        val nav = LinearLayout(this).apply { gravity = Gravity.CENTER }
        nav.addView(navItem("⌂", "Home", true), LinearLayout.LayoutParams(0, 64, 1f))
        nav.addView(navItem("☑", "Event", false), LinearLayout.LayoutParams(0, 64, 1f))
        nav.addView(navItem("⚙", "Settings", false), LinearLayout.LayoutParams(0, 64, 1f))
        root.addView(nav)

        setContentView(root)
    }

    private fun navItem(icon: String, label: String, active: Boolean): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            val i = tv(icon, 23f, if (active) accent else muted)
            i.gravity = Gravity.CENTER
            val l = tv(label, 11f, if (active) accent else muted)
            l.gravity = Gravity.CENTER
            addView(i, LinearLayout.LayoutParams(-1, 32))
            addView(l, LinearLayout.LayoutParams(-1, 25))
        }
    }

    private fun cell(height: Int): GridLayout.LayoutParams =
        GridLayout.LayoutParams().apply {
            width = 0; this.height = height
            columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
        }

    private fun render() {
        title.text = SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(month.time)
        grid.removeAllViews()

        val first = month.clone() as Calendar
        first.set(Calendar.DAY_OF_MONTH, 1)
        val offset = first.get(Calendar.DAY_OF_WEEK) - 1
        val days = month.getActualMaximum(Calendar.DAY_OF_MONTH)

        repeat(offset) { grid.addView(Space(this), cell(72)) }

        for (d in 1..days) {
            val c = month.clone() as Calendar
            c.set(Calendar.DAY_OF_MONTH, d)
            val selectedDay = same(c, selected)
            val todayDay = same(c, today)

            val v = tv(d.toString(), 17f, when {
                selectedDay -> Color.WHITE
                todayDay -> accent
                else -> text
            })
            v.gravity = Gravity.CENTER

            if (selectedDay) {
                v.background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(selectedGray)
                }
            }

            v.setOnClickListener {
                selected.time = c.time
                render()
            }
            grid.addView(v, cell(72))
        }

        updateInfo()
    }

    private fun updateInfo() {
        val a = Calendar.getInstance().apply {
            time = today.time
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        val b = Calendar.getInstance().apply {
            time = selected.time
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        val diff = ((b.timeInMillis - a.timeInMillis) / 86400000L).toInt()
        val distance = when {
            diff == 0 -> "Today"
            diff > 0 -> "In $diff day(s)"
            else -> "${-diff} day(s) ago"
        }
        val week = b.get(Calendar.WEEK_OF_YEAR)
        info.text = "$distance    Week $week"
    }

    private fun same(a: Calendar, b: Calendar): Boolean =
        a.get(Calendar.YEAR) == b.get(Calendar.YEAR) &&
        a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR)
}
