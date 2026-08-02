package com.alser.loadflow

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class YbusInputActivity : AppCompatActivity() {

    // شبكة من حقول الإدخال: لكل خلية (i,j) حقلان -> الجزء الحقيقي G والجزء التخيلي B
    private lateinit var reCells: Array<Array<EditText>>
    private lateinit var imCells: Array<Array<EditText>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ybus_input)

        val container = findViewById<LinearLayout>(R.id.containerYbus)
        val btnSolve = findViewById<Button>(R.id.btnSolve)
        val n = AppData.n

        reCells = Array(n) { Array(n) { EditText(this) } }
        imCells = Array(n) { Array(n) { EditText(this) } }

        // صف رؤوس الأعمدة
        container.addView(buildHeaderRow(n))

        for (i in 0 until n) {
            container.addView(buildYbusRow(i, n))
        }

        btnSolve.setOnClickListener {
            if (collectYbusAndSolve()) {
                startActivity(Intent(this, ResultsActivity::class.java))
            }
        }
    }

    private fun buildHeaderRow(n: Int): View {
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        row.addView(cellLabel("Bus \\ Bus", 120))
        for (j in 0 until n) {
            row.addView(cellLabel("Bus ${j + 1}", 180))
        }
        return row
    }

    private fun cellLabel(text: String, widthDp: Int): TextView {
        return TextView(this).apply {
            this.text = text
            gravity = android.view.Gravity.CENTER
            setPadding(8, 16, 8, 8)
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(dp(widthDp), LinearLayout.LayoutParams.WRAP_CONTENT)
        }
    }

    private fun buildYbusRow(i: Int, n: Int): View {
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        row.addView(cellLabel("Bus ${i + 1}", 120))

        for (j in 0 until n) {
            val cellContainer = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(dp(180), LinearLayout.LayoutParams.WRAP_CONTENT)
                setPadding(6, 6, 6, 6)
            }

            val etRe = EditText(this).apply {
                hint = "G (حقيقي)"
                inputType = android.text.InputType.TYPE_CLASS_NUMBER or
                        android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL or
                        android.text.InputType.TYPE_NUMBER_FLAG_SIGNED
                setBackgroundResource(android.R.drawable.edit_text)
                textSize = 12f
            }
            val etIm = EditText(this).apply {
                hint = "B (تخيلي)"
                inputType = android.text.InputType.TYPE_CLASS_NUMBER or
                        android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL or
                        android.text.InputType.TYPE_NUMBER_FLAG_SIGNED
                setBackgroundResource(android.R.drawable.edit_text)
                textSize = 12f
            }

            cellContainer.addView(etRe)
            cellContainer.addView(etIm)
            row.addView(cellContainer)

            reCells[i][j] = etRe
            imCells[i][j] = etIm
        }
        return row
    }

    private fun dp(value: Int): Int {
        val density = resources.displayMetrics.density
        return (value * density).toInt()
    }

    private fun collectYbusAndSolve(): Boolean {
        val n = AppData.n
        val ybus = Array(n) { Array(n) { Complex.ZERO } }

        for (i in 0 until n) {
            for (j in 0 until n) {
                val re = reCells[i][j].text.toString().toDoubleOrNull()
                val im = imCells[i][j].text.toString().toDoubleOrNull()
                if (re == null || im == null) {
                    Toast.makeText(
                        this,
                        "أكمل كل خلايا Ybus (Bus ${i + 1}, Bus ${j + 1} فارغة)",
                        Toast.LENGTH_LONG
                    ).show()
                    return false
                }
                ybus[i][j] = Complex(re, im)
            }
        }

        AppData.Ybus = ybus

        val result = LoadFlowSolver.solve(
            n = AppData.n,
            busType = AppData.busType,
            Psp = AppData.Psp,
            Qsp = AppData.Qsp,
            VmagInit = AppData.Vmag,
            deltaInit = AppData.delta,
            Ybus = AppData.Ybus
        )
        AppData.result = result
        return true
    }
}
