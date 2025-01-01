package com.pynide.ui.settings

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.TwoStatePreference
import androidx.recyclerview.widget.RecyclerView

import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.ToastUtils

import com.pynide.BuildVars
import com.pynide.IDESettings
import com.pynide.R
import com.pynide.app.ThemeHelper
import com.pynide.app.ThemeHelper.KEY_BLACK_NIGHT_THEME
import com.pynide.app.ThemeHelper.KEY_DYNAMIC_COLORS
import com.pynide.editor.EditorHelper
import com.pynide.terminal.TerminalHelper
import com.pynide.utils.AndroidUtilities

import rikka.preference.IntegerSimpleMenuPreference

import com.pynide.IDESettings.NIGHT_MODE as KEY_NIGHT_MODE
import com.pynide.terminal.TerminalHelper.KEY_COLOR_SCHEME as KEY_TERMINAL_COLOR_SCHEME
import com.pynide.terminal.TerminalHelper.KEY_FONT_SIZE as KEY_TERMINAL_FONT_SIZE
import com.pynide.terminal.TerminalHelper.KEY_FONT_STYLE as KEY_TERMINAL_FONT_STYLE
import com.pynide.terminal.TerminalHelper.KEY_KEEP_SCREEN_ON as KEY_TERMINAL_KEEP_SCREEN_ON

class SettingsFragment : PreferenceFragmentCompat() {
    private lateinit var nightModePreference: IntegerSimpleMenuPreference
    private lateinit var blackNightThemePreference: TwoStatePreference
    private lateinit var dynamicColorsPreference: TwoStatePreference
    private lateinit var terminalKeepScreenOnPreference: TwoStatePreference
    private lateinit var terminalFontSizePreference: IntegerSimpleMenuPreference
    private lateinit var terminalFontStylePreference: ListPreference
    private lateinit var terminalColorSchemePreference: ListPreference
    private lateinit var aboutVersionPreference: Preference
    private lateinit var aboutGithubPreference: Preference
    private lateinit var aboutOssLicencesPreference: Preference
    private lateinit var aboutPrivacyPolicyPreference: Preference

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.setStorageDeviceProtected()
        preferenceManager.sharedPreferencesName = IDESettings.NAME
        preferenceManager.sharedPreferencesMode = Context.MODE_PRIVATE
        setPreferencesFromResource(R.xml.settings, null)

        nightModePreference = findPreference(KEY_NIGHT_MODE)!!
        blackNightThemePreference = findPreference(KEY_BLACK_NIGHT_THEME)!!
        dynamicColorsPreference = findPreference(KEY_DYNAMIC_COLORS)!!
        terminalKeepScreenOnPreference = findPreference(KEY_TERMINAL_KEEP_SCREEN_ON)!!
        terminalFontSizePreference = findPreference(KEY_TERMINAL_FONT_SIZE)!!
        terminalFontStylePreference = findPreference(KEY_TERMINAL_FONT_STYLE)!!
        terminalColorSchemePreference = findPreference(KEY_TERMINAL_COLOR_SCHEME)!!
        aboutVersionPreference = findPreference("about_version")!!
        aboutGithubPreference = findPreference("about_github")!!
        aboutOssLicencesPreference = findPreference("about_oss_licenses")!!
        aboutPrivacyPolicyPreference = findPreference("about_privacy_policy")!!

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
            blackNightThemePreference.isEnabled = false
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
            dynamicColorsPreference.isEnabled = false
        }

        terminalKeepScreenOnPreference.isChecked = TerminalHelper.isKeepScreenOn()
        terminalFontSizePreference.setDefaultValue(TerminalHelper.DEFAULT_FONT_SIZE)
        terminalFontSizePreference.value = TerminalHelper.getFontSize()

        terminalFontStylePreference.setDefaultValue(TerminalHelper.DEFAULT_FONT_STYLE)
        terminalFontStylePreference.value = TerminalHelper.getFontStyle()
        setupTerminalFontStylePreference()

        terminalColorSchemePreference.setDefaultValue(TerminalHelper.DEFAULT_COLOR_SCHEME)
        terminalColorSchemePreference.value = TerminalHelper.getColorScheme()
        setupTerminalColorSchemePreference()

        aboutVersionPreference.summary = String.format("%s (%s)", BuildVars.VERSION_NAME, BuildVars.VERSION_CODE)

        aboutGithubPreference.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            ToastUtils.showShort(R.string.coming_soon)
            true
        }

        aboutOssLicencesPreference.onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                ToastUtils.showShort(R.string.coming_soon)
                true
            }

        aboutPrivacyPolicyPreference.onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                ToastUtils.showShort(R.string.coming_soon)
                true
            }
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
        val fonts = EditorHelper.getAssetFonts()
        val fontNames = fonts.map{it.assetName}.toTypedArray()
        val displayFonts = fonts.map { it.displayName }.toTypedArray()
        terminalFontStylePreference.entries = displayFonts
        terminalFontStylePreference.entryValues = fontNames
    }

    private fun setupTerminalColorSchemePreference() {
        val colorSchemes = TerminalHelper.getAssetColorSchemes()
        val colorSchemeNames = colorSchemes.map { it.assetName }.toTypedArray()
        val displayColorSchemes = colorSchemes.map { it.displayName }.toTypedArray()
        terminalColorSchemePreference.entries = displayColorSchemes
        terminalColorSchemePreference.entryValues = colorSchemeNames
    }
}