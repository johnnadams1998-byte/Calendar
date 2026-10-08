package com.example.vivostylecalendar

import android.app.Activity
import android.app.AlertDialog
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.*
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit
import java.time.temporal.WeekFields
import java.util.Locale
import kotlin.math.abs

class MainActivity : Activity() {

    // ------------------------------------------------------------
    // DATE
    // ------------------------------------------------------------

    private val today = LocalDate.now()
    private var selectedDate = today
    private var displayedMonth = YearMonth.from(today)

    // ------------------------------------------------------------
    // UI
    // ------------------------------------------------------------

    private lateinit var root: LinearLayout
    private lateinit var contentArea: FrameLayout
    private lateinit var monthTitle: TextView
    private lateinit var calendarGrid: LinearLayout
    private lateinit var infoText: TextView
    private lateinit var eventsArea: LinearLayout

    private lateinit var homeTab: LinearLayout
    private lateinit var eventTab: LinearLayout
    private lateinit var settingsTab: LinearLayout

    private var downX = 0f

    // ------------------------------------------------------------
    // COLORS
    // ------------------------------------------------------------

    private var backgroundColor = Color.rgb(250, 250, 250)
    private var primaryTextColor = Color.rgb(38, 38, 38)
    private var secondaryTextColor = Color.rgb(90, 90, 90)

    private var accentColor = Color.rgb(55, 115, 215)
    private var selectedColor = Color.rgb(82, 82, 82)
    private var dividerColor = Color.rgb(225, 225, 225)

    private var currentTheme = "Light"

    private val prefsName = "calendar_preferences"
    private val themeKey = "theme"

    // ------------------------------------------------------------
    // HOLIDAY MODEL
    // ------------------------------------------------------------

    data class Holiday(
        val month: Int,
        val day: Int,
        val name: String,
        val detail: String
    )

    // 2026 India holidays / festivals.
    private val indiaHolidays2026 = listOf(

        Holiday(1, 1, "New Year's Day", "Indian observance"),
        Holiday(1, 13, "Lohri", "Indian festival"),
        Holiday(1, 14, "Makar Sankranti", "Indian festival"),
        Holiday(1, 14, "Pongal", "Indian festival"),
        Holiday(1, 23, "Vasant Panchami", "Indian festival"),
        Holiday(1, 26, "Republic Day", "National holiday"),

        Holiday(2, 1, "Guru Ravidas Jayanti", "Indian observance"),
        Holiday(2, 15, "Maha Shivaratri", "Indian festival"),
        Holiday(2, 19, "Shivaji Jayanti", "Indian observance"),
        Holiday(2, 19, "Ramadan Start", "Indian observance"),

        Holiday(3, 3, "Holika Dahana", "Indian festival"),
        Holiday(3, 4, "Holi", "Indian festival"),
        Holiday(3, 19, "Ugadi", "Indian festival"),
        Holiday(3, 19, "Gudi Padwa", "Indian festival"),
        Holiday(3, 21, "Ramzan Id", "Indian festival"),
        Holiday(3, 26, "Rama Navami", "Indian festival"),
        Holiday(3, 31, "Mahavir Jayanti", "Indian festival"),

        Holiday(4, 3, "Good Friday", "Gazetted holiday"),
        Holiday(4, 14, "Vaisakhi", "Indian festival"),
        Holiday(4, 14, "Vishu", "Indian festival"),
        Holiday(4, 14, "Tamil New Year", "Indian festival"),
        Holiday(4, 14, "Ambedkar Jayanti", "Indian observance"),

        Holiday(5, 1, "Buddha Purnima", "Indian festival"),
        Holiday(5, 1, "International Workers' Day", "Observance"),
        Holiday(5, 28, "Bakrid", "Indian festival"),

        Holiday(6, 26, "Muharram / Ashura", "Indian observance"),

        Holiday(7, 16, "Rath Yatra", "Indian festival"),
        Holiday(7, 29, "Guru Purnima", "Indian festival"),

        Holiday(8, 15, "Independence Day", "National holiday"),
        Holiday(8, 15, "Parsi New Year", "Indian observance"),
        Holiday(8, 26, "Onam", "Indian festival"),
        Holiday(8, 26, "Milad un-Nabi", "Indian observance"),
        Holiday(8, 28, "Raksha Bandhan", "Indian festival"),

        Holiday(9, 4, "Janmashtami", "Indian festival"),
        Holiday(9, 14, "Ganesh Chaturthi", "Indian festival"),

        Holiday(10, 2, "Mahatma Gandhi Jayanti", "National holiday"),
        Holiday(10, 11, "Sharad Navratri Begins", "Indian festival"),
        Holiday(10, 17, "Durga Puja Begins", "Indian festival"),
        Holiday(10, 18, "Maha Saptami", "Indian festival"),
        Holiday(10, 19, "Maha Ashtami", "Indian festival"),
        Holiday(10, 20, "Dussehra", "National holiday"),
        Holiday(10, 26, "Maharishi Valmiki Jayanti", "Indian observance"),
        Holiday(10, 29, "Karaka Chaturthi", "Indian festival"),

        Holiday(11, 8, "Naraka Chaturdasi", "Indian festival"),
        Holiday(11, 8, "Diwali / Deepavali", "Indian festival"),
        Holiday(11, 9, "Govardhan Puja", "Indian festival"),
        Holiday(11, 11, "Bhai Duj", "Indian festival"),
        Holiday(11, 15, "Chhath Puja", "Indian festival"),
        Holiday(11, 24, "Guru Nanak Jayanti", "Indian festival"),
        Holiday(11, 24, "Guru Tegh Bahadur Martyrdom Day", "Indian observance"),

        Holiday(12, 23, "Hazarat Ali's Birthday", "Indian observance"),
        Holiday(12, 24, "Christmas Eve", "Indian observance"),
        Holiday(12, 25, "Christmas", "National holiday")
    )

    // ------------------------------------------------------------
    // ACTIVITY
    // ------------------------------------------------------------

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        loadTheme()
        configureSystemBars()
        buildApplication()
        showHome()
    }

    // ------------------------------------------------------------
    // SYSTEM BARS
    // ------------------------------------------------------------

    private fun configureSystemBars() {

        window.statusBarColor = backgroundColor
        window.navigationBarColor = backgroundColor

        if (currentTheme == "Dark") {
            window.decorView.systemUiVisibility = 0
        } else {
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or
                        View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        }
    }

    // ------------------------------------------------------------
    // MAIN APPLICATION
    // ------------------------------------------------------------

    private fun buildApplication() {

        root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setBackgroundColor(backgroundColor)

        root.setPadding(
            dp(18),
            dp(10),
            dp(18),
            0
        )

        contentArea = FrameLayout(this)

        root.addView(
            contentArea,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        buildBottomNavigation()

        setContentView(root)
    }

    // ------------------------------------------------------------
    // HOME
    // ------------------------------------------------------------

    private fun showHome() {

        contentArea.removeAllViews()

        val screen = LinearLayout(this)
        screen.orientation = LinearLayout.VERTICAL
        screen.setBackgroundColor(backgroundColor)

        // --------------------------------------------------------
        // HEADER
        // --------------------------------------------------------

        val header = LinearLayout(this)
        header.orientation = LinearLayout.HORIZONTAL
        header.gravity = Gravity.CENTER_VERTICAL

        monthTitle = TextView(this)
        monthTitle.textSize = 27f
        monthTitle.setTextColor(primaryTextColor)
        monthTitle.typeface =
            Typeface.create("sans-serif", Typeface.BOLD)
        monthTitle.gravity = Gravity.CENTER_VERTICAL

        monthTitle.setOnClickListener {
            showMonthYearPicker()
        }

        header.addView(
            monthTitle,
            LinearLayout.LayoutParams(
                0,
                dp(62),
                1f
            )
        )

        // --------------------------------------------------------
        // TODAY BOX
        // --------------------------------------------------------

        val todayButton = createTodayButton()

        todayButton.setOnClickListener {

            displayedMonth = YearMonth.from(today)
            selectedDate = today

            refreshCalendar()
        }

        header.addView(todayButton)

        // --------------------------------------------------------
        // PLUS
        // --------------------------------------------------------

        val addButton = headerButton("+", 28f)

        addButton.setOnClickListener {
            showAddEventDialog()
        }

        header.addView(addButton)

        // --------------------------------------------------------
        // MENU
        // --------------------------------------------------------

        val menuButton = headerButton("⋮", 23f)

        menuButton.setOnClickListener {
            showCalendarMenu()
        }

        header.addView(menuButton)

        screen.addView(header)

        // --------------------------------------------------------
        // WEEKDAYS
        // --------------------------------------------------------

        val weekdayRow = LinearLayout(this)
        weekdayRow.orientation = LinearLayout.HORIZONTAL
        weekdayRow.gravity = Gravity.CENTER

        val weekdays = arrayOf(
            "Sun",
            "Mon",
            "Tue",
            "Wed",
            "Thu",
            "Fri",
            "Sat"
        )

        for (name in weekdays) {

            val day = TextView(this)

            day.text = name
            day.textSize = 13f
            day.setTextColor(secondaryTextColor)
            day.gravity = Gravity.CENTER

            weekdayRow.addView(
                day,
                LinearLayout.LayoutParams(
                    0,
                    dp(40),
                    1f
                )
            )
        }

        screen.addView(weekdayRow)

        // --------------------------------------------------------
        // CALENDAR GRID
        // --------------------------------------------------------

        calendarGrid = LinearLayout(this)
        calendarGrid.orientation = LinearLayout.VERTICAL

        // IMPORTANT:
        // Centers the whole calendar vertically instead of
        // leaving all dates at the top.
        calendarGrid.gravity = Gravity.CENTER_VERTICAL

        calendarGrid.setOnTouchListener { _, event ->

            when (event.action) {

                MotionEvent.ACTION_DOWN -> {
                    downX = event.x
                    true
                }

                MotionEvent.ACTION_UP -> {

                    val difference = event.x - downX

                    if (abs(difference) > dp(70)) {

                        displayedMonth =
                            if (difference < 0) {
                                displayedMonth.plusMonths(1)
                            } else {
                                displayedMonth.minusMonths(1)
                            }

                        refreshCalendar()
                    }

                    true
                }

                else -> true
            }
        }

        screen.addView(
            calendarGrid,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        // --------------------------------------------------------
        // INFORMATION
        // --------------------------------------------------------

        infoText = TextView(this)

        infoText.textSize = 15f
        infoText.setTextColor(secondaryTextColor)
        infoText.gravity = Gravity.CENTER_VERTICAL

        screen.addView(
            infoText,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(45)
            )
        )

        // --------------------------------------------------------
        // EVENTS
        // --------------------------------------------------------

        eventsArea = LinearLayout(this)
        eventsArea.orientation = LinearLayout.VERTICAL

        screen.addView(
            eventsArea,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(120)
            )
        )

        contentArea.addView(
            screen,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        refreshCalendar()
    }

    // ------------------------------------------------------------
    // CALENDAR
    // ------------------------------------------------------------

    private fun refreshCalendar() {

        if (!::calendarGrid.isInitialized) return

        val monthName =
            displayedMonth.month.name
                .lowercase(Locale.getDefault())
                .replaceFirstChar {
                    it.uppercase()
                }
                .take(3)

        monthTitle.text =
            "$monthName ${displayedMonth.year}"

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

        val days = displayedMonth.lengthOfMonth()

        val rows =
            if (firstColumn + days <= 35) 5 else 6

        // Larger, more spacious calendar.
        val rowHeight =
            if (rows == 5) dp(67) else dp(58)

        for (rowIndex in 0 until rows) {

            val row = LinearLayout(this)

            row.orientation = LinearLayout.HORIZONTAL
            row.gravity = Gravity.CENTER_VERTICAL

            calendarGrid.addView(
                row,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    rowHeight
                )
            )

            for (column in 0 until 7) {

                val cell = FrameLayout(this)

                row.addView(
                    cell,
                    LinearLayout.LayoutParams(
                        0,
                        rowHeight,
                        1f
                    )
                )

                val position =
                    rowIndex * 7 + column

                if (
                    position >= firstColumn &&
                    position < firstColumn + days
                ) {

                    val number =
                        position - firstColumn + 1

                    val date =
                        displayedMonth.atDay(number)

                    // ------------------------------------------------
                    // DATE NUMBER
                    // ------------------------------------------------

                    val dateText = TextView(this)

                    dateText.text =
                        number.toString()

                    dateText.textSize = 16f

                    dateText.gravity =
                        Gravity.CENTER

                    dateText.includeFontPadding = true

                    dateText.setTextColor(
                        primaryTextColor
                    )

                    // Selected date
                    if (date == selectedDate) {

                        val circle =
                            GradientDrawable()

                        circle.shape =
                            GradientDrawable.OVAL

                        circle.setColor(
                            selectedColor
                        )

                        dateText.background =
                            circle

                        dateText.setTextColor(
                            Color.WHITE
                        )

                    } else if (date == today) {

                        dateText.setTextColor(
                            accentColor
                        )

                        dateText.typeface =
                            Typeface.create(
                                "sans-serif",
                                Typeface.BOLD
                            )
                    }

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

                    // ------------------------------------------------
                    // HOLIDAY DOT
                    // ------------------------------------------------

                    if (holidayFor(date).isNotEmpty()) {

                        val dot = View(this)

                        val dotDrawable =
                            GradientDrawable()

                        dotDrawable.shape =
                            GradientDrawable.OVAL

                        dotDrawable.setColor(
                            holidayColor(date)
                        )

                        dot.background =
                            dotDrawable

                        val dotParams =
                            FrameLayout.LayoutParams(
                                dp(6),
                                dp(6)
                            )

                        dotParams.gravity =
                            Gravity.CENTER_HORIZONTAL or
                                    Gravity.BOTTOM

                        dotParams.bottomMargin =
                            dp(5)

                        cell.addView(
                            dot,
                            dotParams
                        )
                    }

                    // ------------------------------------------------
                    // CLICK
                    // ------------------------------------------------

                    cell.setOnClickListener {

                        selectedDate = date

                        refreshCalendar()
                    }
                }
            }
        }

        updateInformation()
        updateEvents()
    }

    // ------------------------------------------------------------
    // HOLIDAY LOOKUP
    // ------------------------------------------------------------

    private fun holidayFor(
        date: LocalDate
    ): List<Holiday> {

        if (date.year != 2026) {
            return emptyList()
        }

        return indiaHolidays2026.filter {

            it.month == date.monthValue &&
                    it.day == date.dayOfMonth
        }
    }

    private fun holidayColor(
        date: LocalDate
    ): Int {

        val list = holidayFor(date)

        if (list.isEmpty()) {
            return accentColor
        }

        return when (list.first().month) {

            10 -> Color.rgb(225, 55, 65)
            11 -> Color.rgb(245, 145, 25)
            else -> Color.rgb(50, 165, 105)
        }
    }

    // ------------------------------------------------------------
    // INFORMATION
    // ------------------------------------------------------------

    private fun updateInformation() {

        val difference =
            ChronoUnit.DAYS.between(
                today,
                selectedDate
            )

        val description =
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

        val left =
            TextView(this)

        infoText.text =
            "$description                                      Week $week"

        // Slightly darker than before.
        infoText.setTextColor(
            if (currentTheme == "Dark")
                Color.rgb(180, 180, 180)
            else
                Color.rgb(75, 75, 75)
        )
    }

    // ------------------------------------------------------------
    // EVENTS
    // ------------------------------------------------------------

    private fun updateEvents() {

        eventsArea.removeAllViews()

        val holidays =
            holidayFor(selectedDate)

        if (holidays.isEmpty()) {

            val empty =
                TextView(this)

            empty.text = "No events"
            empty.textSize = 14f

            empty.setTextColor(
                secondaryTextColor
            )

            empty.gravity =
                Gravity.CENTER_VERTICAL

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
                    dp(48)
                )
            )

            return
        }

        for (holiday in holidays) {

            addHolidayEvent(
                holiday
            )
        }
    }

    private fun addHolidayEvent(
        holiday: Holiday
    ) {

        val container =
            LinearLayout(this)

        container.orientation =
            LinearLayout.HORIZONTAL

        container.gravity =
            Gravity.CENTER_VERTICAL

        container.setPadding(
            dp(5),
            dp(3),
            0,
            dp(3)
        )

        // --------------------------------------------------------
        // ALL DAY
        // --------------------------------------------------------

        val allDay =
            TextView(this)

        allDay.text = "All day"
        allDay.textSize = 14f
        allDay.setTextColor(
            secondaryTextColor
        )

        allDay.gravity =
            Gravity.CENTER_VERTICAL

        container.addView(
            allDay,
            LinearLayout.LayoutParams(
                dp(67),
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        )

        // --------------------------------------------------------
        // COLORED LINE
        // --------------------------------------------------------

        val line =
            View(this)

        val lineDrawable =
            GradientDrawable()

        lineDrawable.cornerRadius =
            dp(4).toFloat()

        lineDrawable.setColor(
            holidayColor(
                selectedDate
            )
        )

        line.background =
            lineDrawable

        container.addView(
            line,
            LinearLayout.LayoutParams(
                dp(5),
                dp(40)
            )
        )

        // --------------------------------------------------------
        // TEXT
        // --------------------------------------------------------

        val textBox =
            LinearLayout(this)

        textBox.orientation =
            LinearLayout.VERTICAL

        textBox.gravity =
            Gravity.CENTER_VERTICAL

        textBox.setPadding(
            dp(12),
            0,
            dp(5),
            0
        )

        val title =
            TextView(this)

        title.text =
            holiday.name

        title.textSize = 15f

        title.setTextColor(
            primaryTextColor
        )

        title.typeface =
            Typeface.create(
                "sans-serif",
                Typeface.BOLD
            )

        val detail =
            TextView(this)

        detail.text =
            holiday.detail

        detail.textSize = 13f

        detail.setTextColor(
            secondaryTextColor
        )

        textBox.addView(title)
        textBox.addView(detail)

        container.addView(
            textBox,
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1f
            )
        )

        // --------------------------------------------------------
        // ARROW
        // --------------------------------------------------------

        val arrow =
            TextView(this)

        arrow.text = "›"
        arrow.textSize = 25f

        arrow.setTextColor(
            secondaryTextColor
        )

        arrow.gravity =
            Gravity.CENTER

        container.addView(
            arrow,
            LinearLayout.LayoutParams(
                dp(32),
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        )

        eventsArea.addView(
            container,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(58)
            )
        )

        // Divider
        val divider =
            View(this)

        divider.setBackgroundColor(
            dividerColor
        )

        eventsArea.addView(
            divider,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(1)
            )
        )
    }

    // ------------------------------------------------------------
    // TODAY BUTTON
    // ------------------------------------------------------------

    private fun createTodayButton(): TextView {

        val button =
            TextView(this)

        val dayName =
            today.dayOfWeek.name
                .lowercase(Locale.getDefault())
                .replaceFirstChar {
                    it.uppercase()
                }
                .take(3)

        button.text =
            "$dayName\n${today.dayOfMonth}"

        button.textSize = 10f

        button.setTextColor(
            primaryTextColor
        )

        button.gravity =
            Gravity.CENTER

        button.setLineSpacing(
            0f,
            0.85f
        )

        button.typeface =
            Typeface.create(
                "sans-serif",
                Typeface.BOLD
            )

        val background =
            GradientDrawable()

        background.cornerRadius =
            dp(8).toFloat()

        background.setColor(
            if (currentTheme == "Dark")
                Color.rgb(45, 45, 45)
            else
                Color.TRANSPARENT
        )

        background.setStroke(
            dp(1),
            if (currentTheme == "Dark")
                Color.rgb(130, 130, 130)
            else
                Color.rgb(60, 60, 60)
        )

        button.background =
            background

        val params =
            LinearLayout.LayoutParams(
                dp(48),
                dp(52)
            )

        params.setMargins(
            dp(3),
            0,
            dp(3),
            0
        )

        button.layoutParams =
            params

        return button
    }

    // ------------------------------------------------------------
    // HEADER BUTTON
    // ------------------------------------------------------------

    private fun headerButton(
        symbol: String,
        size: Float
    ): TextView {

        val button =
            TextView(this)

        button.text = symbol
        button.textSize = size

        button.setTextColor(
            primaryTextColor
        )

        button.gravity =
            Gravity.CENTER

        button.setPadding(
            dp(3),
            0,
            dp(3),
            0
        )

        button.layoutParams =
            LinearLayout.LayoutParams(
                dp(42),
                dp(58)
            )

        return button
    }

    // ------------------------------------------------------------
    // MONTH / YEAR PICKER
    // ------------------------------------------------------------

    private fun showMonthYearPicker() {

        val container =
            LinearLayout(this)

        container.orientation =
            LinearLayout.HORIZONTAL

        container.gravity =
            Gravity.CENTER

        val monthPicker =
            NumberPicker(this)

        monthPicker.minValue = 1
        monthPicker.maxValue = 12
        monthPicker.value =
            displayedMonth.monthValue

        val yearPicker =
            NumberPicker(this)

        yearPicker.minValue = 2000
        yearPicker.maxValue = 2100
        yearPicker.value =
            displayedMonth.year

        container.addView(
            monthPicker,
            LinearLayout.LayoutParams(
                dp(110),
                dp(180)
            )
        )

        container.addView(
            yearPicker,
            LinearLayout.LayoutParams(
                dp(120),
                dp(180)
            )
        )

        AlertDialog.Builder(this)
            .setTitle("Select month")
            .setView(container)
            .setNegativeButton(
                "Cancel",
                null
            )
            .setPositiveButton(
                "OK"
            ) { _, _ ->

                displayedMonth =
                    YearMonth.of(
                        yearPicker.value,
                        monthPicker.value
                    )

                // Keep the selected date sensible.
                val maxDay =
                    displayedMonth.lengthOfMonth()

                val newDay =
                    selectedDate.dayOfMonth
                        .coerceAtMost(maxDay)

                selectedDate =
                    displayedMonth.atDay(
                        newDay
                    )

                refreshCalendar()
            }
            .show()
    }

    // ------------------------------------------------------------
    // ADD EVENT
    // ------------------------------------------------------------

    private fun showAddEventDialog() {

        val input =
            EditText(this)

        input.hint =
            "Event name"

        AlertDialog.Builder(this)
            .setTitle("Add event")
            .setView(input)
            .setNegativeButton(
                "Cancel",
                null
            )
            .setPositiveButton(
                "Save"
            ) { _, _ ->

                Toast.makeText(
                    this,
                    "Event added",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .show()
    }

    // ------------------------------------------------------------
    // MENU
    // ------------------------------------------------------------

    private fun showCalendarMenu() {

        val options =
            arrayOf(
                "Today",
                "Select date",
                "Settings"
            )

        AlertDialog.Builder(this)
            .setItems(options) { _, which ->

                when (which) {

                    0 -> {

                        displayedMonth =
                            YearMonth.from(today)

                        selectedDate =
                            today

                        refreshCalendar()
                    }

                    1 -> showMonthYearPicker()

                    2 -> showSettingsScreen()
                }
            }
            .show()
    }

    // ------------------------------------------------------------
    // BOTTOM NAVIGATION
    // ------------------------------------------------------------

    private fun buildBottomNavigation() {

        val nav =
            LinearLayout(this)

        nav.orientation =
            LinearLayout.HORIZONTAL

        nav.gravity =
            Gravity.CENTER

        nav.setBackgroundColor(
            backgroundColor
        )

        homeTab =
            navigationItem(
                "⌂",
                "Home",
                true
            )

        eventTab =
            navigationItem(
                "□",
                "Event",
                false
            )

        settingsTab =
            navigationItem(
                "⚙",
                "Settings",
                false
            )

        nav.addView(
            homeTab,
            LinearLayout.LayoutParams(
                0,
                dp(68),
                1f
            )
        )

        nav.addView(
            eventTab,
            LinearLayout.LayoutParams(
                0,
                dp(68),
                1f
            )
        )

        nav.addView(
            settingsTab,
            LinearLayout.LayoutParams(
                0,
                dp(68),
                1f
            )
        )

        homeTab.setOnClickListener {
            selectTab(0)
        }

        eventTab.setOnClickListener {
            selectTab(1)
        }

        settingsTab.setOnClickListener {
            selectTab(2)
        }

        root.addView(nav)
    }

    private fun navigationItem(
        icon: String,
        label: String,
        selected: Boolean
    ): LinearLayout {

        val item =
            LinearLayout(this)

        item.orientation =
            LinearLayout.VERTICAL

        item.gravity =
            Gravity.CENTER

        val iconView =
            TextView(this)

        iconView.text = icon
        iconView.textSize = 23f
        iconView.gravity =
            Gravity.CENTER

        val labelView =
            TextView(this)

        labelView.text = label
        labelView.textSize = 13f
        labelView.gravity =
            Gravity.CENTER

        item.addView(
            iconView,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(32)
            )
        )

        item.addView(
            labelView,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(24)
            )
        )

        item.tag =
            arrayOf(
                iconView,
                labelView
            )

        updateTabAppearance(
            item,
            selected
        )

        return item
    }

    private fun selectTab(
        selected: Int
    ) {

        when (selected) {

            0 -> {
                showHome()
            }

            1 -> {
                showEventsScreen()
            }

            2 -> {
                showSettingsScreen()
            }
        }

        updateTabAppearance(
            homeTab,
            selected == 0
        )

        updateTabAppearance(
            eventTab,
            selected == 1
        )

        updateTabAppearance(
            settingsTab,
            selected == 2
        )
    }

    private fun updateTabAppearance(
        tab: LinearLayout,
        selected: Boolean
    ) {

        val views =
            tab.tag as Array<*>

        val icon =
            views[0] as TextView

        val label =
            views[1] as TextView

        val color =
            if (selected)
                accentColor
            else
                secondaryTextColor

        icon.setTextColor(color)
        label.setTextColor(color)
    }

    // ------------------------------------------------------------
    // EVENTS SCREEN
    // ------------------------------------------------------------

    private fun showEventsScreen() {

        contentArea.removeAllViews()

        val screen =
            LinearLayout(this)

        screen.orientation =
            LinearLayout.VERTICAL

        val title =
            TextView(this)

        title.text =
            "Events"

        title.textSize = 27f

        title.typeface =
            Typeface.DEFAULT_BOLD

        title.setTextColor(
            primaryTextColor
        )

        title.gravity =
            Gravity.CENTER_VERTICAL

        screen.addView(
            title,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(62)
            )
        )

        val scroll =
            ScrollView(this)

        val list =
            LinearLayout(this)

        list.orientation =
            LinearLayout.VERTICAL

        for (holiday in indiaHolidays2026) {

            val row =
                TextView(this)

            row.text =
                "${holiday.day} ${monthShort(holiday.month)}  •  ${holiday.name}"

            row.textSize = 15f

            row.setTextColor(
                primaryTextColor
            )

            row.gravity =
                Gravity.CENTER_VERTICAL

            row.setPadding(
                dp(10),
                0,
                dp(10),
                0
            )

            list.addView(
                row,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dp(52)
                )
            )
        }

        scroll.addView(list)

        screen.addView(
            scroll,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        contentArea.addView(
            screen,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )
    }

    private fun monthShort(
        month: Int
    ): String {

        return java.time.Month.of(month)
            .name
            .lowercase(Locale.getDefault())
            .replaceFirstChar {
                it.uppercase()
            }
            .take(3)
    }

    // ------------------------------------------------------------
    // SETTINGS
    // ------------------------------------------------------------

    private fun showSettingsScreen() {

        contentArea.removeAllViews()

        val screen =
            LinearLayout(this)

        screen.orientation =
            LinearLayout.VERTICAL

        val title =
            TextView(this)

        title.text =
            "Settings"

        title.textSize = 27f

        title.typeface =
            Typeface.DEFAULT_BOLD

        title.setTextColor(
            primaryTextColor
        )

        title.gravity =
            Gravity.CENTER_VERTICAL

        screen.addView(
            title,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(62)
            )
        )

        val themeRow =
            LinearLayout(this)

        themeRow.orientation =
            LinearLayout.HORIZONTAL

        themeRow.gravity =
            Gravity.CENTER_VERTICAL

        themeRow.setPadding(
            dp(5),
            0,
            dp(5),
            0
        )

        val themeText =
            TextView(this)

        themeText.text =
            "Theme\n$currentTheme"

        themeText.textSize = 16f

        themeText.setTextColor(
            primaryTextColor
        )

        themeRow.addView(
            themeText,
            LinearLayout.LayoutParams(
                0,
                dp(65),
                1f
            )
        )

        val arrow =
            TextView(this)

        arrow.text = "›"
        arrow.textSize = 26f

        arrow.setTextColor(
            secondaryTextColor
        )

        themeRow.addView(
            arrow,
            LinearLayout.LayoutParams(
                dp(40),
                dp(65)
            )
        )

        themeRow.setOnClickListener {
            showThemePicker()
        }

        screen.addView(
            themeRow,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(65)
            )
        )

        contentArea.addView(
            screen,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )
    }

    // ------------------------------------------------------------
    // THEME PICKER
    // ------------------------------------------------------------

    private fun showThemePicker() {

        val themes =
            arrayOf(
                "Light",
                "Dark",
                "Blue",
                "Green",
                "Purple"
            )

        AlertDialog.Builder(this)
            .setTitle("Theme")
            .setSingleChoiceItems(
                themes,
                themes.indexOf(currentTheme)
            ) { dialog, which ->

                applyTheme(
                    themes[which]
                )

                dialog.dismiss()
            }
            .show()
    }

    // ------------------------------------------------------------
    // APPLY THEME
    // ------------------------------------------------------------

    private fun applyTheme(
        theme: String
    ) {

        currentTheme = theme

        when (theme) {

            "Light" -> {

                backgroundColor =
                    Color.rgb(250, 250, 250)

                primaryTextColor =
                    Color.rgb(38, 38, 38)

                secondaryTextColor =
                    Color.rgb(90, 90, 90)

                accentColor =
                    Color.rgb(55, 115, 215)

                selectedColor =
                    Color.rgb(82, 82, 82)

                dividerColor =
                    Color.rgb(225, 225, 225)
            }

            "Dark" -> {

                backgroundColor =
                    Color.rgb(25, 25, 25)

                primaryTextColor =
                    Color.rgb(240, 240, 240)

                secondaryTextColor =
                    Color.rgb(175, 175, 175)

                accentColor =
                    Color.rgb(90, 150, 245)

                selectedColor =
                    Color.rgb(95, 95, 95)

                dividerColor =
                    Color.rgb(65, 65, 65)
            }

            "Blue" -> {

                backgroundColor =
                    Color.rgb(245, 249, 255)

                primaryTextColor =
                    Color.rgb(30, 45, 65)

                secondaryTextColor =
                    Color.rgb(80, 100, 125)

                accentColor =
                    Color.rgb(45, 115, 220)

                selectedColor =
                    Color.rgb(65, 100, 150)

                dividerColor =
                    Color.rgb(215, 225, 240)
            }

            "Green" -> {

                backgroundColor =
                    Color.rgb(246, 251, 247)

                primaryTextColor =
                    Color.rgb(35, 55, 40)

                secondaryTextColor =
                    Color.rgb(85, 110, 90)

                accentColor =
                    Color.rgb(45, 150, 85)

                selectedColor =
                    Color.rgb(65, 105, 75)

                dividerColor =
                    Color.rgb(215, 230, 218)
            }

            "Purple" -> {

                backgroundColor =
                    Color.rgb(249, 246, 252)

                primaryTextColor =
                    Color.rgb(50, 40, 60)

                secondaryTextColor =
                    Color.rgb(100, 90, 110)

                accentColor =
                    Color.rgb(125, 75, 190)

                selectedColor =
                    Color.rgb(90, 70, 105)

                dividerColor =
                    Color.rgb(225, 215, 232)
            }
        }

        saveTheme()

        configureSystemBars()

        root.setBackgroundColor(
            backgroundColor
        )

        showHome()

        updateTabAppearance(
            homeTab,
            true
        )
    }

    // ------------------------------------------------------------
    // PREFERENCES
    // ------------------------------------------------------------

    private fun saveTheme() {

        getSharedPreferences(
            prefsName,
            MODE_PRIVATE
        )
            .edit()
            .putString(
                themeKey,
                currentTheme
            )
            .apply()
    }

    private fun loadTheme() {

        currentTheme =
            getSharedPreferences(
                prefsName,
                MODE_PRIVATE
            )
                .getString(
                    themeKey,
                    "Light"
                ) ?: "Light"

        // Set initial colors.
        applyThemeColorsOnly()
    }

    private fun applyThemeColorsOnly() {

        when (currentTheme) {

            "Dark" -> {

                backgroundColor =
                    Color.rgb(25, 25, 25)

                primaryTextColor =
                    Color.rgb(240, 240, 240)

                secondaryTextColor =
                    Color.rgb(175, 175, 175)

                accentColor =
                    Color.rgb(90, 150, 245)

                selectedColor =
                    Color.rgb(95, 95, 95)

                dividerColor =
                    Color.rgb(65, 65, 65)
            }

            "Blue" -> {

                backgroundColor =
                    Color.rgb(245, 249, 255)

                primaryTextColor =
                    Color.rgb(30, 45, 65)

                secondaryTextColor =
                    Color.rgb(80, 100, 125)

                accentColor =
                    Color.rgb(45, 115, 220)

                selectedColor =
                    Color.rgb(65, 100, 150)

                dividerColor =
                    Color.rgb(215, 225, 240)
            }

            "Green" -> {

                backgroundColor =
                    Color.rgb(246, 251, 247)

                primaryTextColor =
                    Color.rgb(35, 55, 40)

                secondaryTextColor =
                    Color.rgb(85, 110, 90)

                accentColor =
                    Color.rgb(45, 150, 85)

                selectedColor =
                    Color.rgb(65, 105, 75)

                dividerColor =
                    Color.rgb(215, 230, 218)
            }

            "Purple" -> {

                backgroundColor =
                    Color.rgb(249, 246, 252)

                primaryTextColor =
                    Color.rgb(50, 40, 60)

                secondaryTextColor =
                    Color.rgb(100, 90, 110)

                accentColor =
                    Color.rgb(125, 75, 190)

                selectedColor =
                    Color.rgb(90, 70, 105)

                dividerColor =
                    Color.rgb(225, 215, 232)
            }

            else -> {

                backgroundColor =
                    Color.rgb(250, 250, 250)

                primaryTextColor =
                    Color.rgb(38, 38, 38)

                secondaryTextColor =
                    Color.rgb(90, 90, 90)

                accentColor =
                    Color.rgb(55, 115, 215)

                selectedColor =
                    Color.rgb(82, 82, 82)

                dividerColor =
                    Color.rgb(225, 225, 225)
            }
        }
    }

    // ------------------------------------------------------------
    // DP
    // ------------------------------------------------------------

    private fun dp(
        value: Int
    ): Int {

        return (
                value *
                        resources.displayMetrics.density
                ).toInt()
    }
}
