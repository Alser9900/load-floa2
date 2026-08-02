package com.alser.loadflow

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val etNumBuses = findViewById<EditText>(R.id.etNumBuses)
        val btnNext = findViewById<Button>(R.id.btnNext)

        btnNext.setOnClickListener {
            val text = etNumBuses.text.toString().trim()
            val n = text.toIntOrNull()

            if (n == null || n < 2) {
                Toast.makeText(this, "أدخل عدد بصات صحيح (2 على الأقل)", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (n > 30) {
                Toast.makeText(this, "الحد الأقصى المدعوم 30 بص", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            AppData.initBuses(n)
            startActivity(Intent(this, BusInputActivity::class.java))
        }
    }
}
