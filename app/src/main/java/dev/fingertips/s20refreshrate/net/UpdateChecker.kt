package dev.fingertips.s20refreshrate.net

import dev.fingertips.s20refreshrate.Preferences
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.lang.Exception
import java.net.MalformedURLException
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton
import javax.net.ssl.HttpsURLConnection

@Singleton
class UpdateChecker @Inject constructor(
    private val preferences: Preferences
) {
    data class LatestVersion(
        val code: Int,
        val version: String
    ) {
        fun toPreferenceString(): String = "$code;$version"

        companion object {
            fun fromPreferenceString(data: String): LatestVersion {
                val items = data.split(";")
                return LatestVersion(items[0].toInt(), items[1])
            }
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    fun checkVersion(): LatestVersion? {
        var con: HttpsURLConnection? = null

        try {
            val url = URL("https://fingertips.dev/DynamicDisplay/version.json")
            con = url.openConnection() as HttpsURLConnection
            con.connect()

            val br = BufferedReader(InputStreamReader(con.inputStream))
            val sb = StringBuilder()
            br.forEachLine {  line ->
                sb.append(line + "\n")
            }

            val json = JSONObject(sb.toString())
            val latestVersion = LatestVersion(code = json.getInt("code"), version = json.getString("version"))
            preferences.latestVersion = latestVersion.toPreferenceString()
            return latestVersion
        } catch (e: MalformedURLException) {
            e.printStackTrace()
            return null
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        } finally {
            if (con != null) {
                try {
                    con.disconnect()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}