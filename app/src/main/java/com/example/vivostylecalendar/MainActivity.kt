package com.example.vivostylecalendar

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.*
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.WeekFields
import java.util.Locale
import kotlin.math.abs

class MainActivity : Activity() {

    private val today = LocalDate.now()
    private var selectedDate = today
    private var displayedMonth = YearMonth.from(today)

    private lateinit var monthTitle: TextView
    private lateinit var calendarGrid: LinearLayout
    private lateinit var infoText: TextView
    private lateinit var eventsArea: LinearLayout

    private val background = Color.rgb(250, 250, 250)
    private val mainText = Color.rgb(45, 45, 45)
    private val secondaryText = Color.rgb(110, 110, 110)
    private val blue = Color.rgb(65, 125, 220)
    private val selectedGray = Color.rgb(88, 88, 88)

    private var downX = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = Color.WHITE
        window.navigationBarColor = Color.WHITE
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or
            View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR

        buildScreen()
        refreshCalendar()
    }

    private fun buildScreen() {

        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setBackgroundColor(background)

        // Proper space below the status bar
        root.setPadding(dp(18), dp(10), dp(18), 0)

        // ================= HEADER =================

        val header = LinearLayout(this)
        header.orientation = LinearLayout.HORIZONTAL
        header.gravity = Gravity.CENTER_VERTICAL

        monthTitle = TextView(this)
        monthTitle.textSize = 27f
        monthTitle.setTextColor(mainText)
        monthTitle.typeface =
            Typeface.create("sans-serif", Typeface.BOLD)
        monthTitle.gravity = Gravity.CENTER_VERTICAL

        header.addView(
            monthTitle,
            LinearLayout.LayoutParams(0, dp(58), 1f)
        )

        val todayButton = headerButton("▣", 21f)
        todayButton.setOnClickListener {
            displayedMonth = YearMonth.from(today)
            selectedDate = today
            refreshCalendar()
        }

        val addButton = headerButton("+", 28f)
        addButton.setOnClickListener {
            Toast.makeText(
                this,
                "Add event",
                Toast.LENGTH_SHORT
            ).show()
        }

        val menuButton = headerButton("⋮", 23f)
        menuButton.setOnClickListener {
            Toast.makeText(
                this,
                "Calendar menu",
                Toast.LENGTH_SHORT
            ).show()
        }

        header.addView(todayButton)
        header.addView(addButton)
        header.addView(menuButton)

        root.addView(header)

        // ================= WEEK DAYS =================

        val weekdayRow = LinearLayout(this)
        weekdayRow.orientation = LinearLayout.HORIZONTAL
        weekdayRow.gravity = Gravity.CENTER

        val names = arrayOf(
            "Sun", "Mon", "Tue",
            "Wed", "Thu", "Fri", "Sat"
        )

        for (name in names) {

            val day = TextView(this)
            day.text = name
            day.textSize = 13f
            day.setTextColor(secondaryText)
            day.gravity = Gravity.CENTER

            weekdayRow.addView(
                day,
                LinearLayout.LayoutParams(
                    0,
                    dp(38),
                    1f
                )
            )
        }

        root.addView(weekdayRow)

        // ================= CALENDAR =================

        calendarGrid = LinearLayout(this)
        calendarGrid.orientation = LinearLayout.VERTICAL
        calendarGrid.setOnTouchListener { _, event ->

            when (event.action) {

                MotionEvent.ACTION_DOWN -> {
                    downX = event.x
                    true
                }

                MotionEvent.ACTION_UP -> {

                    val difference = event.x - downX

                    if (abs(difference) > dp(70)) {

                        if (difference < 0) {
                            displayedMonth =
                                displayedMonth.plusMonths(1)
                        } else {
                            displayedMonth =
                                displayedMonth.minusMonths(1)
                        }

                        refreshCalendar()
                    }

                    true
                }

                else -> true
            }
        }

        root.addView(
            calendarGrid,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        // ================= DATE INFO =================

        infoText = TextView(this)
        infoText.textSize = 14f
        infoText.setTextColor(secondaryText)
        infoText.gravity = Gravity.CENTER_VERTICAL
        infoText.setPadding(dp(5), 0, dp(5), 0)

        root.addView(
            infoText,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(42)
            )
        )

        // ================= EVENTS =================

        eventsArea = LinearLayout(this)
        eventsArea.orientation = LinearLayout.VERTICAL
        eventsArea.setPadding(0, dp(3), 0, 0)

        root.addView(
            eventsArea,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(105)
            )
        )

        // ================= BOTTOM NAV =================

        val navigation = LinearLayout(this)
        navigation.orientation = LinearLayout.HORIZONTAL
        navigation.gravity = Gravity.CENTER

        navigation.addView(
            navigationItem("⌂", "Home", true),
            LinearLayout.LayoutParams(0, dp(68), 1f)
        )

        navigation.addView(
            navigationItem("□", "Event", false),
            LinearLayout.LayoutParams(0, dp(68), 1f)
        )

        navigation.addView(
            navigationItem("⚙", "Settings", false),
            LinearLayout.LayoutParams(0, dp(68), 1f)
        )

        root.addView(navigation)

        setContentView(root)
    }

    private fun refreshCalendar() {

        monthTitle.text =
            "${displayedMonth.month.name.lowercase(Locale.getDefault())
                .replaceFirstChar { it.uppercase() }
                .take(3)} ${displayedMonth.year}"

        calendarGrid.removeAllViews()

        val firstDay = displayedMonth.atDay(1)

        val firstColumn =
            when (firstDay.dayOfWeek) {
                DayOfWeek.SUNDAY -> 0
                DayOfWeek.MONDAY -> 1
                DayOfWeek.TUESDAY -> 2
                DayOfWeek.WEDNESDAY -> 3
                DayOfWeek.THURSDAY -> 4
                DayOfWeek.FRIDAY -> 5
                DayOfWeek.SATURDAY -> 6
            }

        val totalDays = displayedMonth.lengthOfMonth()

        val totalCells =
            if (firstColumn + totalDays <= 35) 35 else 42

        val rows = totalCells / 7

        for (rowNumber in 0 until rows) {

            val row = LinearLayout(this)
            row.orientation = LinearLayout.HORIZONTAL
            row.gravity = Gravity.CENTER_VERTICAL

            for (column in 0 until 7) {

                val position =
                    rowNumber * 7 + column

                val cell = FrameLayout(this)

                row.addView(
                    cell,
                    LinearLayout.LayoutParams(
                        0,
                        0,
                        1f
                    )
                )

                if (
                    position >= firstColumn &&
                    position < firstColumn + totalDays
                ) {

                    val number =
                        position - firstColumn + 1

                    val date =
                        displayedMonth.atDay(number)

                    val dateText = TextView(this)

                    dateText.text = number.toString()
                    dateText.textSize = 16f
                    dateText.gravity = Gravity.CENTER

                    val isSelected =
                        date == selectedDate

                    val isToday =
                        date == today

                    if (isSelected) {

                        val circle =
                            GradientDrawable()

                        circle.shape =
                            GradientDrawable.OVAL

                        circle.setColor(selectedGray)

                        dateText.background = circle
                        dateText.setTextColor(Color.WHITE)

                    } else if (isToday) {

                        dateText.setTextColor(blue)
                        dateText.typeface =
                            Typeface.create(
                                "sans-serif",
                                Typeface.BOLD
                            )

                    } else {

                        dateText.setTextColor(mainText)
                    }

                    // Properly centered date
                    val dateParams =
                        FrameLayout.LayoutParams(
                            dp(42),
                            dp(42)
                        )

                    dateParams.gravity =
                        Gravity.CENTER

                    cell.addView(
                        dateText,
                        dateParams
                    )

                    cell.setOnClickListener {

                        selectedDate = date

                        refreshCalendar()
                    }
                }
            }

            calendarGrid.addView(
                row,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    0,
                    1f
                )
            )
        }

        updateInformation()
        updateEvents()
    }

    private fun updateInformation() {

        val difference =
            java.time.temporal.ChronoUnit.DAYS.between(
                today,
                selectedDate
            )

        val dateDescription =
            when {
                difference == 0L ->
                    "Today"

                difference > 0 ->
                    "In ${difference} day(s)"

                else ->
                    "${-difference} day(s) ago"
            }

        val week =
            selectedDate.get(
                WeekFields.ISO.weekOfWeekBasedYear()
            )

        infoText.text =
            "$dateDescription                                      Week $week"
    }

    private fun updateEvents() {

        eventsArea.removeAllViews()

        // Example matching the reference layout.
        // No event is shown on other dates.
        if (
            selectedDate.year == 2026 &&
            selectedDate.monthValue == 12 &&
            selectedDate.dayOfMonth == 23
        ) {

            addEvent("Hazarat Ali's Birthday")
            addEvent("Hazarat Ali's Birthday")

        } else {

            val empty = TextView(this)

            empty.text = "No events"
            empty.textSize = 14f
            empty.setTextColor(secondaryText)
            empty.gravity = Gravity.CENTER_VERTICAL

            empty.setPadding(
                dp(8),
                0,
                0,
                0
            )

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
        time.setTextColor(secondaryText)
        time.gravity = Gravity.CENTER

        row.addView(
            time,
            LinearLayout.LayoutParams(
                dp(58),
                dp(48)
            )
        )

        val line = View(this)

        line.setBackgroundColor(
            Color.rgb(70, 170, 110)
        )

        row.addView(
            line,
            LinearLayout.LayoutParams(
                dp(3),
                dp(36)
            )
        )

        val titleText = TextView(this)

        titleText.text = title
        titleText.textSize = 15f
        titleText.setTextColor(mainText)
        titleText.gravity = Gravity.CENTER_VERTICAL

        titleText.setPadding(
            dp(12),
            0,
            0,
            0
        )

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

    private fun headerButton(
        symbol: String,
        size: Float
    ): TextView {

        val button = TextView(this)

        button.text = symbol
        button.textSize = size
        button.setTextColor(mainText)
        button.gravity = Gravity.CENTER

        return button
    }

    private fun navigationItem(
        icon: String,
        label: String,
        selected: Boolean
    ): LinearLayout {

        val container = LinearLayout(this)

        container.orientation =
            LinearLayout.VERTICAL

        container.gravity =
            Gravity.CENTER

        val iconText = TextView(this)

        iconText.text = icon
        iconText.textSize = 21f
        iconText.gravity = Gravity.CENTER

        iconText.setTextColor(
            if (selected) blue
            else secondaryText
        )

        val labelText = TextView(this)

        labelText.text = label
        labelText.textSize = 12f
        labelText.gravity = Gravity.CENTER

        labelText.setTextColor(
            if (selected) blue
            else secondaryText
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

        return (
            value *
            resources.displayMetrics.density
        ).toInt()
    }
}
