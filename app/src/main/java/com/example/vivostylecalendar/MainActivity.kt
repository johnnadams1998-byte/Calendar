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

    private val calendar = Calendar.getInstance()
    private val today = Calendar.getInstance()
    private var selectedDate = Calendar.getInstance()

    private lateinit var monthTitle: TextView
    private lateinit var calendarGrid: LinearLayout
    private lateinit var infoText: TextView
    private lateinit var eventsArea: LinearLayout

    private val bgColor = Color.rgb(250, 250, 250)
    private val textColor = Color.rgb(45, 45, 45)
    private val secondaryColor = Color.rgb(110, 110, 110)
    private val blueColor = Color.rgb(70, 130, 220)
    private val selectedColor = Color.rgb(90, 90, 90)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = Color.WHITE
        window.navigationBarColor = Color.WHITE
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or
            View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR

        calendar.firstDayOfWeek = Calendar.SUNDAY

        buildScreen()
        updateCalendar()
    }

    private fun buildScreen() {

        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setBackgroundColor(bgColor)
        root.setPadding(dp(18), dp(8), dp(18), 0)

        // ---------------- HEADER ----------------

        val header = LinearLayout(this)
        header.orientation = LinearLayout.HORIZONTAL
        header.gravity = Gravity.CENTER_VERTICAL

        monthTitle = TextView(this)
        monthTitle.textSize = 27f
        monthTitle.setTextColor(textColor)
        monthTitle.typeface = Typeface.create("sans-serif", Typeface.BOLD)

        header.addView(
            monthTitle,
            LinearLayout.LayoutParams(0, dp(58), 1f)
        )

        val todayButton = makeHeaderButton("▣")
        todayButton.setOnClickListener {
            calendar.time = today.time
            selectedDate.time = today.time
            updateCalendar()
        }

        val addButton = makeHeaderButton("+")
        addButton.setOnClickListener {
            Toast.makeText(this, "Add event", Toast.LENGTH_SHORT).show()
        }

        val menuButton = makeHeaderButton("⋮")
        menuButton.setOnClickListener {
            Toast.makeText(this, "Calendar menu", Toast.LENGTH_SHORT).show()
        }

        header.addView(todayButton)
        header.addView(addButton)
        header.addView(menuButton)

        root.addView(header)

        // ---------------- WEEKDAYS ----------------

        val weekdays = LinearLayout(this)
        weekdays.orientation = LinearLayout.HORIZONTAL
        weekdays.gravity = Gravity.CENTER

        val names = arrayOf(
            "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"
        )

        for (name in names) {
            val day = TextView(this)
            day.text = name
            day.textSize = 13f
            day.setTextColor(secondaryColor)
            day.gravity = Gravity.CENTER
            day.typeface = Typeface.DEFAULT

            weekdays.addView(
                day,
                LinearLayout.LayoutParams(0, dp(34), 1f)
            )
        }

        root.addView(weekdays)

        // ---------------- CALENDAR ----------------

        calendarGrid = LinearLayout(this)
        calendarGrid.orientation = LinearLayout.VERTICAL

        root.addView(
            calendarGrid,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        // ---------------- INFO ----------------

        infoText = TextView(this)
        infoText.textSize = 14f
        infoText.setTextColor(secondaryColor)
        infoText.gravity = Gravity.CENTER_VERTICAL
        infoText.setPadding(dp(4), 0, dp(4), 0)

        root.addView(
            infoText,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(42)
            )
        )

        // ---------------- EVENTS ----------------

        eventsArea = LinearLayout(this)
        eventsArea.orientation = LinearLayout.VERTICAL
        eventsArea.setPadding(0, dp(2), 0, 0)

        root.addView(
            eventsArea,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(105)
            )
        )

        // ---------------- BOTTOM NAVIGATION ----------------

        val navigation = LinearLayout(this)
        navigation.orientation = LinearLayout.HORIZONTAL
        navigation.gravity = Gravity.CENTER

        navigation.addView(
            makeNavigationItem("⌂", "Home", true),
            LinearLayout.LayoutParams(0, dp(68), 1f)
        )

        navigation.addView(
            makeNavigationItem("□", "Event", false),
            LinearLayout.LayoutParams(0, dp(68), 1f)
        )

        navigation.addView(
            makeNavigationItem("⚙", "Settings", false),
            LinearLayout.LayoutParams(0, dp(68), 1f)
        )

        root.addView(navigation)

        setContentView(root)
    }

    private fun updateCalendar() {

        val monthFormat = SimpleDateFormat("MMM yyyy", Locale.getDefault())
        monthTitle.text = monthFormat.format(calendar.time)

        calendarGrid.removeAllViews()

        val firstDay = Calendar.getInstance()
        firstDay.time = calendar.time
        firstDay.set(Calendar.DAY_OF_MONTH, 1)

        val startDay = firstDay.get(Calendar.DAY_OF_WEEK) - 1
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        var dayNumber = 1

        for (week in 0 until 6) {

            val row = LinearLayout(this)
            row.orientation = LinearLayout.HORIZONTAL
            row.gravity = Gravity.CENTER_VERTICAL

            for (column in 0 until 7) {

                val cell = FrameLayout(this)

                val cellParams = LinearLayout.LayoutParams(
                    0,
                    dp(55),
                    1f
                )

                val position = week * 7 + column

                if (position >= startDay && dayNumber <= daysInMonth) {

                    val dayValue = dayNumber

                    val dayText = TextView(this)
                    dayText.text = dayValue.toString()
                    dayText.textSize = 16f
                    dayText.gravity = Gravity.CENTER
                    dayText.setTextColor(textColor)

                    val selected =
                        selectedDate.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) &&
                        selectedDate.get(Calendar.MONTH) == calendar.get(Calendar.MONTH) &&
                        selectedDate.get(Calendar.DAY_OF_MONTH) == dayValue

                    val isToday =
                        today.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) &&
                        today.get(Calendar.MONTH) == calendar.get(Calendar.MONTH) &&
                        today.get(Calendar.DAY_OF_MONTH) == dayValue

                    if (selected) {
                        val circle = GradientDrawable()
                        circle.shape = GradientDrawable.OVAL
                        circle.setColor(selectedColor)

                        dayText.background = circle
                        dayText.setTextColor(Color.WHITE)

                        val size = dp(40)

                        val params = FrameLayout.LayoutParams(size, size)
                        params.gravity = Gravity.CENTER
                        cell.addView(dayText, params)

                    } else {

                        if (isToday) {
                            dayText.setTextColor(blueColor)
                            dayText.typeface =
                                Typeface.create("sans-serif", Typeface.BOLD)
                        }

                        val params = FrameLayout.LayoutParams(
                            dp(40),
                            dp(40)
                        )
                        params.gravity = Gravity.CENTER

                        cell.addView(dayText, params)
                    }

                    cell.setOnClickListener {
                        selectedDate = Calendar.getInstance()
                        selectedDate.set(
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            dayValue
                        )

                        updateCalendar()
                    }

                    dayNumber++
                }

                row.addView(cell, cellParams)
            }

            calendarGrid.addView(row)
        }

        updateInfo()
        updateEvents()
    }

    private fun updateInfo() {

        val selected = Calendar.getInstance()
        selected.time = selectedDate.time

        val difference =
            ((selected.timeInMillis - today.timeInMillis) / 86400000L).toInt()

        val text = when {
            difference == 0 -> "Today"
            difference > 0 -> "In $difference day(s)"
            else -> "${-difference} day(s) ago"
        }

        val weekNumber =
            selected.get(Calendar.WEEK_OF_YEAR)

        infoText.text = "$text                                      Week $weekNumber"
    }

    private fun updateEvents() {

        eventsArea.removeAllViews()

        // Matches the event shown in the Vivo-style reference
        if (
            selectedDate.get(Calendar.YEAR) == 2026 &&
            selectedDate.get(Calendar.MONTH) == Calendar.DECEMBER &&
            selectedDate.get(Calendar.DAY_OF_MONTH) == 23
        ) {

            addEvent("Hazarat Ali's Birthday")
            addEvent("Hazarat Ali's Birthday")

        } else {

            val empty = TextView(this)
            empty.text = "No events"
            empty.textSize = 14f
            empty.setTextColor(Color.GRAY)
            empty.gravity = Gravity.CENTER_VERTICAL
            empty.setPadding(dp(8), 0, 0, 0)

            eventsArea.addView(
                empty,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dp(50)
                )
            )
        }
    }

    private fun addEvent(title: String) {

        val row = LinearLayout(this)
        row.orientation = LinearLayout.HORIZONTAL
        row.gravity = Gravity.CENTER_VERTICAL

        val time = TextView(this)
        time.text = "All day"
        time.textSize = 12f
        time.setTextColor(secondaryColor)
        time.gravity = Gravity.CENTER

        row.addView(
            time,
            LinearLayout.LayoutParams(dp(55), dp(48))
        )

        val line = View(this)
        line.setBackgroundColor(Color.rgb(70, 170, 110))

        row.addView(
            line,
            LinearLayout.LayoutParams(dp(3), dp(38))
        )

        val titleText = TextView(this)
        titleText.text = title
        titleText.textSize = 15f
        titleText.setTextColor(textColor)
        titleText.gravity = Gravity.CENTER_VERTICAL
        titleText.setPadding(dp(12), 0, 0, 0)

        row.addView(
            titleText,
            LinearLayout.LayoutParams(
                0,
                dp(48),
                1f
            )
        )

        eventsArea.addView(row)
    }

    private fun makeHeaderButton(symbol: String): TextView {

        val button = TextView(this)

        button.text = symbol
        button.textSize = if (symbol == "+") 28f else 21f
        button.setTextColor(textColor)
        button.gravity = Gravity.CENTER
        button.setPadding(dp(6), 0, dp(6), 0)

        return button
    }

    private fun makeNavigationItem(
        icon: String,
        label: String,
        selected: Boolean
    ): LinearLayout {

        val container = LinearLayout(this)
        container.orientation = LinearLayout.VERTICAL
        container.gravity = Gravity.CENTER

        val iconText = TextView(this)
        iconText.text = icon
        iconText.textSize = 21f
        iconText.gravity = Gravity.CENTER
        iconText.setTextColor(
            if (selected) blueColor else secondaryColor
        )

        val labelText = TextView(this)
        labelText.text = label
        labelText.textSize = 12f
        labelText.gravity = Gravity.CENTER
        labelText.setTextColor(
            if (selected) blueColor else secondaryColor
        )

        container.addView(
            iconText,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(32)
            )
        )

        container.addView(
            labelText,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(24)
            )
        )

        return container
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}
