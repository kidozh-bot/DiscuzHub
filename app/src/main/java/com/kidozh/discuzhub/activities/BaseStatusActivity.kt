package com.kidozh.discuzhub.activities

import androidx.appcompat.app.AppCompatActivity
import com.kidozh.discuzhub.entities.Discuz
import com.kidozh.discuzhub.entities.User
import okhttp3.OkHttpClient
import com.kidozh.discuzhub.results.BaseResult
import com.kidozh.discuzhub.results.VariableResults
import android.os.Bundle
import com.kidozh.discuzhub.R
import androidx.preference.PreferenceManager
import androidx.appcompat.app.AppCompatDelegate
import com.kidozh.discuzhub.utilities.ThemeUtils
import com.kidozh.discuzhub.utilities.UserPreferenceUtils

open class BaseStatusActivity : AppCompatActivity() {
    @JvmField
    var bbsInfo: Discuz? = null
    @JvmField
    public var user: User? = null
    @JvmField
    var client = OkHttpClient()
    var baseVariableResult: BaseResult? = null
    var variableResults: VariableResults? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configureTheme()
        configureDarkMode()
    }

    var themeIndex : Int = 0

    val styleList = ThemeUtils.styleList

    fun configureTheme(){
        val position = UserPreferenceUtils.getThemeIndex(this)
        themeIndex = position
        if(position < styleList.size){

            setTheme(styleList[position])
            //theme.applyStyle(styleList[position],true)

        }
    }

    // follow UTF8 default
    val charsetType: Int
        get() {
            if (baseVariableResult != null) {
                if (baseVariableResult!!.Charset == "GBK") {
                    return CHARSET_GBK
                } else if (baseVariableResult!!.Charset == "BIG5") {
                    return CHARSET_BIG5
                }
            }
            // follow UTF8 default
            return CHARSET_UTF8
        }


    private fun configureDarkMode() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val dark_mode_settings = prefs.getString(getString(R.string.preference_key_display_mode), "")
        when (dark_mode_settings) {
            "MODE_NIGHT_NO" -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                return
            }
            "MODE_NIGHT_YES" -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                return
            }
            "MODE_NIGHT_FOLLOW_SYSTEM" -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                return
            }
            "MODE_NIGHT_AUTO_BATTERY" -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY)
                return
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val position = UserPreferenceUtils.getThemeIndex(this)
        if(position < styleList.size){

            setTheme(styleList[position])
            //theme.applyStyle(styleList[position],true)
            if(themeIndex != position){
                themeIndex = position
                recreate()

            }

        }
    }



    companion object {
        private val TAG = BaseStatusActivity::class.java.simpleName
        const val CHARSET_UTF8 = 1
        const val CHARSET_GBK = 2
        const val CHARSET_BIG5 = 3
    }
}