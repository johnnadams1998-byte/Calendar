package com.example.vivostylecalendar

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowInsets
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
    // VIEWS
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

    // ------------------------------------------------------------
    // COLORS
    // ------------------------------------------------------------

    private var backgroundColor = Color.rgb(250, 250, 250)
    private var primaryTextColor = Color.rgb(45, 45, 45)
    private var secondaryTextColor = Color.rgb(105, 105, 105)
    private var accentColor = Color.rgb(65, 125, 220)
    private var selectedColor = Color.rgb(88, 88, 88)
    private var dividerColor = Color.rgb(225, 225, 225)

    // ------------------------------------------------------------
    // THEME
    // ------------------------------------------------------------

    private var currentTheme = "Light"

    private val prefsName = "calendar_preferences"
    private val themeKey = "theme"

    // ------------------------------------------------------------
    // SWIPE
    // ------------------------------------------------------------

    private var downX = 0f

    // ------------------------------------------------------------
    // ON CREATE
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

        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or
            View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR

        /*
         * Make the app respect the phone's system navigation area.
         * This prevents Home / Event / Settings from being hidden
         * behind the Moto's navigation buttons.
         */
        window.decorView.setOnApplyWindowInsetsListener { view, insets ->

            val navigationBottom =
                insets.getInsets(
                    WindowInsets.Type.navigationBars()
                ).bottom

            view.setPadding(
                view.paddingLeft,
                view.paddingTop,
                view.paddingRight,
                navigationBottom
            )

            insets
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
            dp(12),
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
    // HOME SCREEN
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
            Typeface.create(
                "sans-serif",
                Typeface.BOLD
            )

        monthTitle.gravity =
            Gravity.CENTER_VERTICAL

        /*
         * Tap month title to open month/year picker.
         */
        monthTitle.setOnClickListener {
            showMonthYearPicker()
        }

        header.addView(
            monthTitle,
            LinearLayout.LayoutParams(
                0,
                dp(58),
                1f
            )
        )

        // Today button

        val todayButton =
            headerButton("▣", 21f)

        todayButton.setOnClickListener {

            displayedMonth =
                YearMonth.from(today)

            selectedDate = today

            refreshCalendar()
        }

        // Add button

        val addButton =
            headerButton("+", 28f)

        addButton.setOnClickListener {

            showAddEventDialog()
        }

        // Menu button

        val menuButton =
            headerButton("⋮", 23f)

        menuButton.setOnClickListener {

            showCalendarMenu()
        }

        header.addView(todayButton)
        header.addView(addButton)
        header.addView(menuButton)

        screen.addView(header)

        // --------------------------------------------------------
        // WEEKDAY ROW
        // --------------------------------------------------------

        val weekdayRow = LinearLayout(this)

        weekdayRow.orientation =
            LinearLayout.HORIZONTAL

        weekdayRow.gravity =
            Gravity.CENTER

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
            day.setTextColor(
                secondaryTextColor
            )

            day.gravity =
                Gravity.CENTER

            weekdayRow.addView(
                day,
                LinearLayout.LayoutParams(
                    0,
                    dp(38),
                    1f
                )
            )
        }

        screen.addView(weekdayRow)

        // --------------------------------------------------------
        // CALENDAR
        // --------------------------------------------------------

        calendarGrid = LinearLayout(this)

        calendarGrid.orientation =
            LinearLayout.VERTICAL

        calendarGrid.setOnTouchListener { _, event ->

            when (event.action) {

                MotionEvent.ACTION_DOWN -> {

                    downX = event.x

                    true
                }

                MotionEvent.ACTION_UP -> {

                    val difference =
                        event.x - downX

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
        // DATE INFORMATION
        // --------------------------------------------------------

        infoText = TextView(this)

        infoText.textSize = 14f

        infoText.setTextColor(
            secondaryTextColor
        )

        infoText.gravity =
            Gravity.CENTER_VERTICAL

        screen.addView(
            infoText,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(42)
            )
        )

        // --------------------------------------------------------
        // EVENTS
        // --------------------------------------------------------

        eventsArea = LinearLayout(this)

        eventsArea.orientation =
            LinearLayout.VERTICAL

        screen.addView(
            eventsArea,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(100)
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

        if (!::calendarGrid.isInitialized) {
            return
        }

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

        val firstDay =
            displayedMonth.atDay(1)

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

        val days =
            displayedMonth.lengthOfMonth()

        val rows =
            if (firstColumn + days <= 35) {
                5
            } else {
                6
            }

        /*
         * Fixed but comfortable row height.
         * This prevents dates disappearing because of weight
         * measurement problems.
         */
        val rowHeight =
            if (rows == 5) dp(62)
            else dp(52)

        for (rowIndex in 0 until rows) {

            val row =
                LinearLayout(this)

            row.orientation =
                LinearLayout.HORIZONTAL

            row.gravity =
                Gravity.CENTER_VERTICAL

            calendarGrid.addView(
                row,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    rowHeight
                )
            )

            for (column in 0 until 7) {

                val cell =
                    FrameLayout(this)

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
                        position -
                        firstColumn +
                        1

                    val date =
                        displayedMonth.atDay(number)

                    val dateText =
                        TextView(this)

                    dateText.text =
                        number.toString()

                    dateText.textSize =
                        16f

                    dateText.gravity =
                        Gravity.CENTER

                    dateText.includeFontPadding =
                        true

                    // ------------------------------------------------
                    // SELECTED DATE
                    // ------------------------------------------------

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

                    // ------------------------------------------------
                    // TODAY
                    // ------------------------------------------------

                    } else if (date == today) {

                        dateText.setTextColor(
                            accentColor
                        )

                        dateText.typeface =
                            Typeface.create(
                                "sans-serif",
                                Typeface.BOLD
                            )

                    } else {

                        dateText.setTextColor(
                            primaryTextColor
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

                    cell.setOnClickListener {

                        selectedDate =
                            date

                        refreshCalendar()
                    }
                }
            }
        }

        updateInformation()
        updateEvents()
    }

    // ------------------------------------------------------------
    // DATE INFORMATION
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
                WeekFields.ISO
                    .weekOfWeekBasedYear()
            )

        infoText.text =
            "$description                              Week $week"
    }

    // ------------------------------------------------------------
    // EVENTS
    // ------------------------------------------------------------

    private fun updateEvents() {

        eventsArea.removeAllViews()

        /*
         * The reference screenshot shows this event.
         * It is kept as a visual example for Dec 23, 2026.
         */

        if (
            selectedDate.year == 2026 &&
            selectedDate.monthValue == 12 &&
            selectedDate.dayOfMonth == 23
        ) {

            addEvent(
                "Hazarat Ali's Birthday"
            )

            addEvent(
                "Hazarat Ali's Birthday"
            )

        } else {

            val empty =
                TextView(this)

            empty.text =
                "No events"

            empty.textSize =
                14f

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
        }
    }

    private fun addEvent(title: String) {

        val row =
            LinearLayout(this)

        row.orientation =
            LinearLayout.HORIZONTAL

        row.gravity =
            Gravity.CENTER_VERTICAL

        val time =
            TextView(this)

        time.text =
            "All day"

        time.textSize =
            12f

        time.setTextColor(
            secondaryTextColor
        )

        time.gravity =
            Gravity.CENTER

        row.addView(
            time,
            LinearLayout.LayoutParams(
                dp(58),
                dp(46)
            )
        )

        val line =
            View(this)

        line.setBackgroundColor(
            Color.rgb(70, 170, 110)
        )

        row.addView(
            line,
            LinearLayout.LayoutParams(
                dp(3),
                dp(34)
            )
        )

        val titleText =
            TextView(this)

        titleText.text =
            title

        titleText.textSize =
            15f

        titleText.setTextColor(
            primaryTextColor
        )

        titleText.gravity =
            Gravity.CENTER_VERTICAL

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
                dp(46),
                1f
            )
        )

        eventsArea.addView(row)
    }

    // ------------------------------------------------------------
    // BOTTOM NAVIGATION
    // ------------------------------------------------------------

    private fun buildBottomNavigation() {

        val navigation =
            LinearLayout(this)

        navigation.orientation =
            LinearLayout.HORIZONTAL

        navigation.gravity =
            Gravity.CENTER

        navigation.setBackgroundColor(
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

        homeTab.setOnClickListener {
            selectTab(0)
            showHome()
        }

        eventTab.setOnClickListener {
            selectTab(1)
            showEventsScreen()
        }

        settingsTab.setOnClickListener {
            selectTab(2)
            showSettingsScreen()
        }

        navigation.addView(
            homeTab,
            LinearLayout.LayoutParams(
                0,
                dp(68),
                1f
            )
        )

        navigation.addView(
            eventTab,
            LinearLayout.LayoutParams(
                0,
                dp(68),
                1f
            )
        )

        navigation.addView(
            settingsTab,
            LinearLayout.LayoutParams(
                0,
                dp(68),
                1f
            )
        )

        /*
         * This navigation is outside contentArea.
         * Therefore it cannot be hidden behind the calendar.
         */
        root.addView(
            navigation,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(68)
            )
        )
    }

    private fun navigationItem(
        icon: String,
        label: String,
        selected: Boolean
    ): LinearLayout {

        val container =
            LinearLayout(this)

        container.orientation =
            LinearLayout.VERTICAL

        container.gravity =
            Gravity.CENTER

        val iconText =
            TextView(this)

        iconText.text =
            icon

        iconText.textSize =
            21f

        iconText.gravity =
            Gravity.CENTER

        iconText.setTextColor(
            if (selected)
                accentColor
            else
                secondaryTextColor
        )

        val labelText =
            TextView(this)

        labelText.text =
            label

        labelText.textSize =
            12f

        labelText.gravity =
            Gravity.CENTER

        labelText.setTextColor(
            if (selected)
                accentColor
            else
                secondaryTextColor
        )

        container.addView(
            iconText,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(34)
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

    private fun selectTab(selected: Int) {

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

        val icon =
            tab.getChildAt(0) as TextView

        val label =
            tab.getChildAt(1) as TextView

        val color =
            if (selected)
                accentColor
            else
                secondaryTextColor

        icon.setTextColor(color)
        label.setTextColor(color)
    }

    // ------------------------------------------------------------
    // EVENT SCREEN
    // ------------------------------------------------------------

    private fun showEventsScreen() {

        contentArea.removeAllViews()

        val screen =
            LinearLayout(this)

        screen.orientation =
            LinearLayout.VERTICAL

        screen.setBackgroundColor(
            backgroundColor
        )

        val title =
            TextView(this)

        title.text =
            "Events"

        title.textSize =
            28f

        title.typeface =
            Typeface.DEFAULT_BOLD

        title.setTextColor(
            primaryTextColor
        )

        title.setPadding(
            0,
            dp(10),
            0,
            dp(20)
        )

        screen.addView(
            title,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(65)
            )
        )

        val add =
            TextView(this)

        add.text =
            "+  Add event"

        add.textSize =
            16f

        add.setTextColor(
            accentColor
        )

        add.gravity =
            Gravity.CENTER_VERTICAL

        add.setPadding(
            dp(12),
            0,
            0,
            0
        )

        add.setOnClickListener {
            showAddEventDialog()
        }

        screen.addView(
            add,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(55)
            )
        )

        val info =
            TextView(this)

        info.text =
            "Your events will appear here."

        info.textSize =
            14f

        info.setTextColor(
            secondaryTextColor
        )

        info.setPadding(
            dp(12),
            dp(20),
            0,
            0
        )

        screen.addView(
            info,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(60)
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
    // SETTINGS SCREEN
    // ------------------------------------------------------------

    private fun showSettingsScreen() {

        contentArea.removeAllViews()

        val screen =
            LinearLayout(this)

        screen.orientation =
            LinearLayout.VERTICAL

        screen.setBackgroundColor(
            backgroundColor
        )

        val title =
            TextView(this)

        title.text =
            "Settings"

        title.textSize =
            28f

        title.typeface =
            Typeface.DEFAULT_BOLD

        title.setTextColor(
            primaryTextColor
        )

        title.setPadding(
            0,
            dp(10),
            0,
            dp(20)
        )

        screen.addView(
            title,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(65)
            )
        )

        // Theme setting

        val themeRow =
            LinearLayout(this)

        themeRow.orientation =
            LinearLayout.HORIZONTAL

        themeRow.gravity =
            Gravity.CENTER_VERTICAL

        themeRow.setPadding(
            dp(8),
            0,
            dp(8),
            0
        )

        val themeLabel =
            TextView(this)

        themeLabel.text =
            "Theme"

        themeLabel.textSize =
            17f

        themeLabel.setTextColor(
            primaryTextColor
        )

        themeRow.addView(
            themeLabel,
            LinearLayout.LayoutParams(
                0,
                dp(60),
                1f
            )
        )

        val currentThemeText =
            TextView(this)

        currentThemeText.text =
            currentTheme

        currentThemeText.textSize =
            15f

        currentThemeText.setTextColor(
            accentColor
        )

        currentThemeText.gravity =
            Gravity.CENTER

        themeRow.addView(
            currentThemeText,
            LinearLayout.LayoutParams(
                dp(100),
                dp(60)
            )
        )

        themeRow.setOnClickListener {

            showThemePicker()
        }

        screen.addView(
            themeRow,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(60)
            )
        )

        // Month selector

        val monthRow =
            LinearLayout(this)

        monthRow.orientation =
            LinearLayout.HORIZONTAL

        monthRow.gravity =
            Gravity.CENTER_VERTICAL

        monthRow.setPadding(
            dp(8),
            0,
            dp(8),
            0
        )

        val monthLabel =
            TextView(this)

        monthLabel.text =
            "Choose month & year"

        monthLabel.textSize =
            17f

        monthLabel.setTextColor(
            primaryTextColor
        )

        monthRow.addView(
            monthLabel,
            LinearLayout.LayoutParams(
                0,
                dp(60),
                1f
            )
        )

        monthRow.setOnClickListener {

            showMonthYearPicker()
        }

        screen.addView(
            monthRow,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(60)
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
    // MONTH / YEAR PICKER
    // ------------------------------------------------------------

    private fun showMonthYearPicker() {

        val dialogView =
            LinearLayout(this)

        dialogView.orientation =
            LinearLayout.HORIZONTAL

        dialogView.gravity =
            Gravity.CENTER

        dialogView.setPadding(
            dp(20),
            dp(15),
            dp(20),
            dp(10)
        )

        val monthSpinner =
            Spinner(this)

        val yearSpinner =
            Spinner(this)

        val months =
            arrayOf(
                "January",
                "February",
                "March",
                "April",
                "May",
                "June",
                "July",
                "August",
                "September",
                "October",
                "November",
                "December"
            )

        val years =
            (2020..2040)
                .map { it.toString() }
                .toTypedArray()

        monthSpinner.adapter =
            ArrayAdapter(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                months
            )

        yearSpinner.adapter =
            ArrayAdapter(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                years
            )

        monthSpinner.setSelection(
            displayedMonth.monthValue - 1
        )

        yearSpinner.setSelection(
            displayedMonth.year - 2020
        )

        dialogView.addView(
            monthSpinner,
            LinearLayout.LayoutParams(
                0,
                dp(55),
                1.5f
            )
        )

        dialogView.addView(
            yearSpinner,
            LinearLayout.LayoutParams(
                0,
                dp(55),
                1f
            )
        )

        val dialog =
            AlertDialog.Builder(this)
                .setTitle("Choose month")
                .setView(dialogView)
                .setNegativeButton(
                    "Cancel",
                    null
                )
                .setPositiveButton(
                    "OK"
                ) { _, _ ->

                    val month =
                        monthSpinner.selectedItemPosition + 1

                    val year =
                        2020 +
                        yearSpinner.selectedItemPosition

                    displayedMonth =
                        YearMonth.of(
                            year,
                            month
                        )

                    /*
                     * If the previously selected date doesn't
                     * belong to the chosen month, select day 1.
                     */
                    val maxDay =
                        displayedMonth.lengthOfMonth()

                    val day =
                        selectedDate.dayOfMonth
                            .coerceAtMost(maxDay)

                    selectedDate =
                        LocalDate.of(
                            year,
                            month,
                            day
                        )

                    if (::calendarGrid.isInitialized) {
                        refreshCalendar()
                    }
                }
                .create()

        dialog.show()
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

        var selected =
            themes.indexOf(currentTheme)

        if (selected < 0) {
            selected = 0
        }

        AlertDialog.Builder(this)
            .setTitle("Choose theme")
            .setSingleChoiceItems(
                themes,
                selected
            ) { dialog, which ->

                applyTheme(
                    themes[which]
                )

                dialog.dismiss()
            }
            .setNegativeButton(
                "Cancel",
                null
            )
            .show()
    }

    // ------------------------------------------------------------
    // APPLY THEME
    // ------------------------------------------------------------

    private fun applyTheme(theme: String) {

        currentTheme = theme

        when (theme) {

            "Light" -> {

                backgroundColor =
                    Color.rgb(250, 250, 250)

                primaryTextColor =
                    Color.rgb(45, 45, 45)

                secondaryTextColor =
                    Color.rgb(105, 105, 105)

                accentColor =
                    Color.rgb(65, 125, 220)

                selectedColor =
                    Color.rgb(88, 88, 88)

                dividerColor =
                    Color.rgb(225, 225, 225)
            }

            "Dark" -> {

                backgroundColor =
                    Color.rgb(25, 25, 25)

                primaryTextColor =
                    Color.rgb(240, 240, 240)

                secondaryTextColor =
                    Color.rgb(170, 170, 170)

                accentColor =
                    Color.rgb(100, 165, 255)

                selectedColor =
                    Color.rgb(105, 105, 105)

                dividerColor =
                    Color.rgb(65, 65, 65)
            }

            "Blue" -> {

                backgroundColor =
                    Color.rgb(245, 249, 255)

                primaryTextColor =
                    Color.rgb(30, 45, 65)

                secondaryTextColor =
                    Color.rgb(90, 110, 135)

                accentColor =
                    Color.rgb(45, 115, 220)

                selectedColor =
                    Color.rgb(55, 105, 180)

                dividerColor =
                    Color.rgb(215, 225, 240)
            }

            "Green" -> {

                backgroundColor =
                    Color.rgb(246, 251, 247)

                primaryTextColor =
                    Color.rgb(35, 55, 40)

                secondaryTextColor =
                    Color.rgb(95, 115, 100)

                accentColor =
                    Color.rgb(45, 145, 80)

                selectedColor =
                    Color.rgb(65, 120, 80)

                dividerColor =
                    Color.rgb(215, 230, 218)
            }

            "Purple" -> {

                backgroundColor =
                    Color.rgb(249, 247, 252)

                primaryTextColor =
                    Color.rgb(50, 40, 60)

                secondaryTextColor =
                    Color.rgb(110, 100, 120)

                accentColor =
                    Color.rgb(125, 80, 190)

                selectedColor =
                    Color.rgb(105, 75, 135)

                dividerColor =
                    Color.rgb(225, 215, 235)
            }
        }

        saveTheme()

        configureSystemBars()

        root.setBackgroundColor(
            backgroundColor
        )

        if (::contentArea.isInitialized) {
            contentArea.setBackgroundColor(
                backgroundColor
            )
        }

        /*
         * Rebuild whichever screen is currently selected.
         */
        if (::settingsTab.isInitialized) {

            val settingsSelected =
                settingsTab
                    .getChildAt(1)
                    ?.let {
                        (it as TextView)
                            .currentTextColor == accentColor
                    } == true

            if (settingsSelected) {
                showSettingsScreen()
                selectTab(2)
            } else {
                showHome()
                selectTab(0)
            }
        }
    }

    // ------------------------------------------------------------
    // SAVE THEME
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

    // ------------------------------------------------------------
    // LOAD THEME
    // ------------------------------------------------------------

    private fun loadTheme() {

        currentTheme =
            getSharedPreferences(
                prefsName,
                MODE_PRIVATE
            )
                .getString(
                    themeKey,
                    "Light"
                )
                ?: "Light"

        /*
         * Set colors without rebuilding the UI.
         */
        when (currentTheme) {

            "Dark" -> {

                backgroundColor =
                    Color.rgb(25, 25, 25)

                primaryTextColor =
                    Color.rgb(240, 240, 240)

                secondaryTextColor =
                    Color.rgb(170, 170, 170)

                accentColor =
                    Color.rgb(100, 165, 255)

                selectedColor =
                    Color.rgb(105, 105, 105)
            }

            "Blue" -> {

                backgroundColor =
                    Color.rgb(245, 249, 255)

                primaryTextColor =
                    Color.rgb(30, 45, 65)

                secondaryTextColor =
                    Color.rgb(90, 110, 135)

                accentColor =
                    Color.rgb(45, 115, 220)

                selectedColor =
                    Color.rgb(55, 105, 180)
            }

            "Green" -> {

                backgroundColor =
                    Color.rgb(246, 251, 247)

                primaryTextColor =
                    Color.rgb(35, 55, 40)

                secondaryTextColor =
                    Color.rgb(95, 115, 100)

                accentColor =
                    Color.rgb(45, 145, 80)

                selectedColor =
                    Color.rgb(65, 120, 80)
            }

            "Purple" -> {

                backgroundColor =
                    Color.rgb(249, 247, 252)

                primaryTextColor =
                    Color.rgb(50, 40, 60)

                secondaryTextColor =
                    Color.rgb(110, 100, 120)

                accentColor =
                    Color.rgb(125, 80, 190)

                selectedColor =
                    Color.rgb(105, 75, 135)
            }
        }
    }

    // ------------------------------------------------------------
    // ADD EVENT
    // ------------------------------------------------------------

    private fun showAddEventDialog() {

        val input =
            EditText(this)

        input.hint =
            "Event name"

        input.setSingleLine(true)

        val container =
            LinearLayout(this)

        container.setPadding(
            dp(24),
            dp(5),
            dp(24),
            dp(5)
        )

        container.addView(
            input,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(55)
            )
        )

        AlertDialog.Builder(this)
            .setTitle(
                "Add event"
            )
            .setView(container)
            .setNegativeButton(
                "Cancel",
                null
            )
            .setPositiveButton(
                "Save"
            ) { _, _ ->

                val name =
                    input.text.toString().trim()

                if (name.isNotEmpty()) {

                    Toast.makeText(
                        this,
                        "Event saved",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .show()
    }

    // ------------------------------------------------------------
    // MENU
    // ------------------------------------------------------------

    private fun showCalendarMenu() {

        val options =
            arrayOf(
                "Choose month & year",
                "Go to today",
                "Settings"
            )

        AlertDialog.Builder(this)
            .setItems(
                options
            ) { _, which ->

                when (which) {

                    0 ->
                        showMonthYearPicker()

                    1 -> {

                        displayedMonth =
                            YearMonth.from(today)

                        selectedDate =
                            today

                        refreshCalendar()
                    }

                    2 -> {

                        selectTab(2)

                        showSettingsScreen()
                    }
                }
            }
            .show()
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

        button.text =
            symbol

        button.textSize =
            size

        button.setTextColor(
            primaryTextColor
        )

        button.gravity =
            Gravity.CENTER

        button.setPadding(
            dp(5),
            0,
            dp(5),
            0
        )

        return button
    }

    // ------------------------------------------------------------
    // DP
    // ------------------------------------------------------------

    private fun dp(value: Int): Int {

        return (
            value *
            resources.displayMetrics.density
        ).toInt()
    }
}
