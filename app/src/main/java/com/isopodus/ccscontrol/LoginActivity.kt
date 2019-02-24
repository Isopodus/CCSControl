package com.isopodus.ccscontrol

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Resources
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_login.*
import java.net.ConnectException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import kotlin.concurrent.thread


class LoginActivity : AppCompatActivity() {
    private val host = "http://ccsystem.in/stat2/ccscontrol/"

    lateinit var sp: SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        sp = getSharedPreferences("SP", Context.MODE_PRIVATE)

        if (sp.getBoolean("LOGIN", false)) {
            goToMainActivity()
        }

        loginBtn.setOnClickListener {
            thread {
                try {
                    val encodedPassword = md5(passwordView.text.toString())
                    val payload = mapOf("login" to loginView.text.toString(), "password" to encodedPassword)
                    val response = khttp.post(host + "login.php", data = payload)

                    runOnUiThread {
                        when {
                            response.text == "1" -> {
                                sp.edit().putBoolean("LOGIN", true).apply()
                                sp.edit().putString("USERNAME", loginView.text.toString()).apply()

                                goToMainActivity()
                            }

                            response.text == "0" -> Toast.makeText(
                                applicationContext,
                                "Неправильный логин!",
                                Toast.LENGTH_SHORT
                            ).show()

                            response.text == "-1" -> Toast.makeText(
                                applicationContext,
                                "Неправильный пароль!",
                                Toast.LENGTH_SHORT
                            ).show()

                            else -> Toast.makeText(
                                applicationContext,
                                "Что-то пошло не так...",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } catch (e: Resources.NotFoundException) {
                    runOnUiThread {
                        Toast.makeText(
                            this,
                            "Сервер недоступен, попробуйте позже",
                            Toast.LENGTH_SHORT).show()
                    }
                } catch (e: ConnectException) {
                    runOnUiThread {
                        Toast.makeText(
                            this,
                            "Не удалось получить данные, \nпроверьте подключение к сети",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: Exception) {
                    Log.d("ERR", e.toString())
                }
            }
        }
    }

    private fun goToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun md5(s: String): String {
        val MD5 = "MD5"
        try {
            // Create MD5 Hash
            val digest = java.security.MessageDigest
                .getInstance(MD5)
            digest.update(s.toByteArray())
            val messageDigest = digest.digest()

            // Create Hex String
            val hexString = StringBuilder()
            for (aMessageDigest in messageDigest) {
                var h = Integer.toHexString(0xFF and aMessageDigest.toInt())
                while (h.length < 2)
                    h = "0$h"
                hexString.append(h)
            }
            return hexString.toString()

        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }

        return ""
    }
}