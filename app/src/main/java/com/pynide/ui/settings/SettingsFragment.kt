package com.pynide.ui.settings

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.ViewGroup

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.text.HtmlCompat
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.TwoStatePreference
import androidx.recyclerview.widget.RecyclerView

import com.blankj.utilcode.util.SizeUtils

import com.pynide.BuildVars
import com.pynide.IDELocales
import com.pynide.IDESettings
import com.pynide.R
import com.pynide.app.ThemeHelper
import com.pynide.app.ThemeHelper.KEY_BLACK_NIGHT_THEME
import com.pynide.app.ThemeHelper.KEY_DYNAMIC_COLORS
import com.pynide.terminal.TerminalHelper
import com.pynide.utils.AndroidUtilities
import com.pynide.utils.FileLog
import com.pynide.utils.LocaleDelegate
import com.pynide.utils.Utilities

import java.util.Locale

import com.pynide.IDESettings.LANGUAGE as KEY_LANGUAGE
import com.pynide.IDESettings.NIGHT_MODE as KEY_NIGHT_MODE
import com.pynide.terminal.TerminalHelper.KEY_FONT_SIZE as KEY_TERMINAL_FONT_SIZE
import com.pynide.terminal.TerminalHelper.KEY_FONT_STYLE as KEY_TERMINAL_FONT_STYLE
import com.pynide.terminal.TerminalHelper.KEY_KEEP_SCREEN_ON as KEY_TERMINAL_KEEP_SCREEN_ON

class SettingsFragment : PreferenceFragmentCompat() {
    private lateinit var nightModePreference: IntegerSimpleMenuPreference
    private lateinit var blackNightThemePreference: TwoStatePreference
    private lateinit var dynamicColorsPreference: TwoStatePreference
    private lateinit var languagePreference: ListPreference
    private lateinit var terminalKeepScreenOnPreference: TwoStatePreference
    private lateinit var terminalFontSizePreference: IntegerSimpleMenuPreference
    private lateinit var terminalFontStylePreference: ListPreference
    private lateinit var aboutVersionPreference: Preference
    private lateinit var aboutGithubPreference: Preference
    private lateinit var aboutReportBugPreference: Preference
    private lateinit var aboutOssLicencesPreference: Preference

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.setStorageDeviceProtected()
        preferenceManager.sharedPreferencesName = IDESettings.NAME
        preferenceManager.sharedPreferencesMode = Context.MODE_PRIVATE
        setPreferencesFromResource(R.xml.settings, null)

        nightModePreference = findPreference(KEY_NIGHT_MODE)!!
        blackNightThemePreference = findPreference(KEY_BLACK_NIGHT_THEME)!!
        dynamicColorsPreference = findPreference(KEY_DYNAMIC_COLORS)!!
        languagePreference = findPreference(KEY_LANGUAGE)!!
        terminalKeepScreenOnPreference = findPreference(KEY_TERMINAL_KEEP_SCREEN_ON)!!
        terminalFontSizePreference = findPreference(KEY_TERMINAL_FONT_SIZE)!!
        terminalFontStylePreference = findPreference(KEY_TERMINAL_FONT_STYLE)!!
        aboutVersionPreference = findPreference("about_version")!!
        aboutGithubPreference = findPreference("about_github")!!
        aboutReportBugPreference = findPreference("about_report_bug")!!
        aboutOssLicencesPreference = findPreference("about_oss_licenses")!!

        nightModePreference.value = IDESettings.getNightMode()
        nightModePreference.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _, value: Any? ->
                if (value is Int) {
                    if (IDESettings.getNightMode() != value) {
                        AppCompatDelegate.setDefaultNightMode(value)
                        ActivityCompat.recreate(requireActivity())
                    }
                }
                true
            }

        if (IDESettings.getNightMode() != AppCompatDelegate.MODE_NIGHT_NO) {
            blackNightThemePreference.isChecked = ThemeHelper.isBlackNightTheme()
            blackNightThemePreference.onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { _, _ ->
                    if (AndroidUtilities.isNightMode(context)) {
                        ActivityCompat.recreate(requireActivity())
                    }
                    true
                }
        } else {
            blackNightThemePreference.isVisible = false
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            dynamicColorsPreference.isChecked = ThemeHelper.isDynamicColors()
            dynamicColorsPreference.onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { _, value: Any? ->
                    if (value is Boolean) {
                        if (ThemeHelper.isDynamicColors() != value) {
                            ActivityCompat.recreate(requireActivity())
                        }
                    }
                    true
                }
        } else {
            dynamicColorsPreference.isVisible = false
        }

        languagePreference.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _: Preference?, newValue: Any ->
                if (newValue is String) {
                    val locale = if ("SYSTEM" == newValue) {
                        LocaleDelegate.systemLocale
                    } else {
                        Locale.forLanguageTag(newValue)
                    }
                    LocaleDelegate.defaultLocale = locale
                    ActivityCompat.recreate(requireActivity())
                }
                true
            }
        setupLocalePreference()

        terminalKeepScreenOnPreference.isChecked = TerminalHelper.isKeepScreenOn()
        terminalFontSizePreference.setDefaultValue(TerminalHelper.DEFAULT_FONT_SIZE)
        terminalFontSizePreference.value = TerminalHelper.getFontSize()

        setupTerminalFontStylePreference()

        aboutVersionPreference.summary =
            String.format("%s (%s)", BuildVars.VERSION_NAME, BuildVars.VERSION_CODE)
    }

    override fun onCreateRecyclerView(
        inflater: LayoutInflater,
        parent: ViewGroup,
        savedInstanceState: Bundle?
    ): RecyclerView {
        val recyclerView = super.onCreateRecyclerView(inflater, parent, savedInstanceState)
        recyclerView.clipToPadding = false
        recyclerView.setPaddingRelative(0, 0, 0, SizeUtils.dp2px(8f))
        return recyclerView
    }

    private fun setupTerminalFontStylePreference() {
        val fontStylesList = mutableListOf<String>()
        val displayFontStylesList = mutableListOf<String>()

        requireContext().assets.list("fonts")!!
            .filter { it.endsWith(".ttf") }
            .forEach { assetsName ->
                fontStylesList.add(assetsName)
                FileLog.d(assetsName)

                var name = assetsName.replace('-', ' ')
                val dotIndex = name.lastIndexOf('.')
                if (dotIndex != -1) name = name.substring(0, dotIndex)
                val displayName = Utilities.capitalize(name)
                displayFontStylesList.add(displayName)
            }

        val fontStyles = fontStylesList.toTypedArray()
        val displayFontStyles = displayFontStylesList.toTypedArray()

        terminalFontStylePreference.setDefaultValue(TerminalHelper.DEFAULT_FONT_STYLE)
        terminalFontStylePreference.entries = displayFontStyles
        terminalFontStylePreference.entryValues = fontStyles

        val currentFontStyleValue = terminalFontStylePreference.value
        val currentFontStyleIndex = fontStyles.indexOf(currentFontStyleValue)
        val currentFontStyle = TerminalHelper.getFontStyle()

        terminalFontStylePreference.summary = when {
            TextUtils.isEmpty(currentFontStyleValue) || "reddit" == currentFontStyleValue -> {
                "Reddit"
            }

            currentFontStyleIndex != -1 -> {
                displayFontStyles[currentFontStyleIndex]
            }

            else -> {
                ""
            }
        }
    }

    private fun setupLocalePreference() {
        val localeTags = IDELocales.LOCALES
        val displayLocaleTags = IDELocales.DISPLAY_LOCALES

        languagePreference.entries = displayLocaleTags
        languagePreference.entryValues = localeTags

        val currentLocaleTag = languagePreference.value
        val currentLocaleIndex = localeTags.indexOf(currentLocaleTag)
        val currentLocale = IDESettings.getLocale()
        val localizedLocales = mutableListOf<CharSequence>()

        for ((index, displayLocale) in displayLocaleTags.withIndex()) {
            if (index == 0) {
                localizedLocales.add(getString(R.string.follow_system))
                continue
            }

            val locale = Locale.forLanguageTag(displayLocale.toString())
            val localeName = if (!TextUtils.isEmpty(locale.script)) {
                locale.getDisplayScript(locale)
            } else {
                locale.getDisplayName(locale)
            }

            val localizedLocaleName = if (!TextUtils.isEmpty(locale.script)) {
                locale.getDisplayScript(currentLocale)
            } else {
                locale.getDisplayName(currentLocale)
            }

            localizedLocales.add(
                if (index != currentLocaleIndex) {
                    HtmlCompat.fromHtml(
                        "$localeName<br><small>$localizedLocaleName<small>",
                        HtmlCompat.FROM_HTML_MODE_COMPACT
                    )
                } else {
                    localizedLocaleName
                }
            )
        }

        languagePreference.entries = localizedLocales.toTypedArray()

        languagePreference.summary = when {
            TextUtils.isEmpty(currentLocaleTag) || "SYSTEM" == currentLocaleTag -> {
                getString(R.string.follow_system)
            }

            currentLocaleIndex != -1 -> {
                val localizedLocale = localizedLocales[currentLocaleIndex]
                val newLineIndex = localizedLocale.indexOf('\n')
                if (newLineIndex == -1) {
                    localizedLocale.toString()
                } else {
                    localizedLocale.subSequence(0, newLineIndex).toString()
                }
            }

            else -> {
                ""
            }
        }
    }
}