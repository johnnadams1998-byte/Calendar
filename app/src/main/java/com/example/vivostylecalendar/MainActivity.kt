    private fun applyThemeColors() {

        when (currentTheme) {

            "Dark" -> {
                backgroundColor = Color.rgb(25, 25, 25)
                primaryTextColor = Color.rgb(235, 235, 235)
                secondaryTextColor = Color.rgb(175, 175, 175)
                accentColor = Color.rgb(90, 150, 235)
                selectedColor = Color.rgb(90, 90, 90)
                dividerColor = Color.rgb(60, 60, 60)
            }

            "Blue" -> {
                backgroundColor = Color.rgb(248, 251, 255)
                primaryTextColor = Color.rgb(35, 45, 60)
                secondaryTextColor = Color.rgb(85, 100, 120)
                accentColor = Color.rgb(50, 115, 220)
                selectedColor = Color.rgb(80, 95, 115)
                dividerColor = Color.rgb(220, 228, 240)
            }

            "Green" -> {
                backgroundColor = Color.rgb(248, 252, 248)
                primaryTextColor = Color.rgb(35, 50, 40)
                secondaryTextColor = Color.rgb(85, 105, 90)
                accentColor = Color.rgb(55, 145, 85)
                selectedColor = Color.rgb(78, 92, 82)
                dividerColor = Color.rgb(220, 232, 222)
            }

            "Purple" -> {
                backgroundColor = Color.rgb(251, 249, 253)
                primaryTextColor = Color.rgb(45, 40, 55)
                secondaryTextColor = Color.rgb(95, 88, 105)
                accentColor = Color.rgb(115, 85, 190)
                selectedColor = Color.rgb(82, 78, 90)
                dividerColor = Color.rgb(230, 224, 238)
            }

            else -> {
                backgroundColor = Color.rgb(250, 250, 250)
                primaryTextColor = Color.rgb(40, 40, 40)
                secondaryTextColor = Color.rgb(90, 90, 90)
                accentColor = Color.rgb(55, 115, 215)
                selectedColor = Color.rgb(82, 82, 82)
                dividerColor = Color.rgb(225, 225, 225)
            }
        }
    }

    private fun loadTheme() {

        val prefs =
            getSharedPreferences(
                prefsName,
                MODE_PRIVATE
            )

        currentTheme =
            prefs.getString(
                themeKey,
                "Light"
            ) ?: "Light"

        applyThemeColors()
    }

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

    // ============================================================
    // ABOUT
    // ============================================================

    private fun showAbout() {

        AlertDialog.Builder(this)
            .setTitle("Calendar")
            .setMessage(
                "A clean, simple calendar with Indian " +
                        "festivals and holidays.\n\n" +
                        "Offline • No login • No ads"
            )
            .setPositiveButton("OK", null)
            .show()
    }

    // ============================================================
    // UTILITY
    // ============================================================

    private fun dp(value: Int): Int {
        return (
            value *
                    resources.displayMetrics.density
            ).toInt()
    }
}
