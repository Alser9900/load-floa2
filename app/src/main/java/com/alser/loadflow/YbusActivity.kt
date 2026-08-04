package com.alser.loadflow

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.GridLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class YbusActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ybus)

        val busCount = intent.getIntExtra("BUS_COUNT", 2)
        val busTypes = intent.getIntArrayExtra("BUS_TYPES")
        val grid = findViewById<GridLayout>(R.id.ybus_grid)
        val btnSolve = findViewById<Button>(R.id.btn_solve)

        grid.rowCount = busCount
        grid.columnCount = busCount

        for (i in 0 until busCount) {
            for (j in 0 until busCount) {
                val editText = EditText(this).apply {
                    hint = "Y${i+1}${j+1}"
                    inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL or android.text.InputType.TYPE_NUMBER_FLAG_SIGNED
                    gravity = Gravity.CENTER
                    setBackgroundResource(R.drawable.edit_text_bg)
                    setTextColor(Color.BLACK)
                    setHintTextColor(Color.GRAY)
                    setPadding(16, 16, 16, 16)
                    val params = GridLayout.LayoutParams()
                    params.width = 200
                    params.height = 120
                    params.setMargins(8, 8, 8, 8)
                    layoutParams = params
                }
                grid.addView(editText)
            }
        }

        btnSolve.setOnClickListener {
            val intent = Intent(this, ResultsActivity::class.java)
            intent.putExtra("BUS_COUNT", busCount)
            intent.putExtra("BUS_TYPES", busTypes)
            startActivity(intent)
        }
    }
}