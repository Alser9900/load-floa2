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

        val etBusCount = findViewById<EditText>(R.id.et_bus_count)
        val btnNext = findViewById<Button>(R.id.btn_next)

        btnNext.setOnClickListener {
            val countStr = etBusCount.text.toString()
            if (countStr.isNotEmpty()) {
                val n = countStr.toIntOrNull()
                if (n != null && n > 1) {
                    val intent = Intent(this, BusDataActivity::class.java)
                    intent.putExtra("BUS_COUNT", n)
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "أدخل عدد صحيح أكبر من 1", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "الرجاء إدخال عدد البصات", Toast.LENGTH_SHORT).show()
            }
        }
    }
}