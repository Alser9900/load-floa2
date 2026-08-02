package com.alser.loadflow

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class BusInputActivity : AppCompatActivity() {

    private val busTypeLabels = arrayOf("Slack (المرجعي)", "PV", "PQ")

    // عناصر إدخال كل صف بص
    private data class BusRowViews(
        val spinner: Spinner,
        val etP: EditText,
        val etQ: EditText,
        val etV: EditText
    )

    private val rows = mutableListOf<BusRowViews>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bus_input)

        val container = findViewById<LinearLayout>(R.id.containerBuses)
        val btnNext = findViewById<Button>(R.id.btnNextYbus)

        for (i in 0 until AppData.n) {
            container.addView(buildBusRow(i))
        }

        btnNext.setOnClickListener {
            if (collectAndValidate()) {
                startActivity(Intent(this, YbusInputActivity::class.java))
            }
        }
    }

    private fun buildBusRow(index: Int): View {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 20, 20, 20)
            setBackgroundColor(0xFFFFFFFF.toInt())
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            lp.setMargins(0, 0, 0, 24)
            layoutParams = lp
            elevation = 4f
        }

        val title = TextView(this).apply {
            text = "Bus ${index + 1}"
            textSize = 16f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            setTextColor(0xFF1565C0.toInt())
        }
        card.addView(title)

        val spinnerLabel = TextView(this).apply {
            text = "نوع البص:"
            setPadding(0, 12, 0, 4)
        }
        card.addView(spinnerLabel)

        val spinner = Spinner(this).apply {
            adapter = ArrayAdapter(this@BusInputActivity, android.R.layout.simple_spinner_dropdown_item, busTypeLabels)
            setSelection(2) // افتراضي PQ
        }
        card.addView(spinner)

        val etP = labeledEditText("القدرة الفعالة المحددة P (pu):", "0.0")
        val etQ = labeledEditText("القدرة غير الفعالة المحددة Q (pu):", "0.0")
        val etV = labeledEditText("|V| (محدد لـ Slack/PV، ابتدائي لـ PQ):", "1.0")

        card.addView(etP.first); card.addView(etP.second)
        card.addView(etQ.first); card.addView(etQ.second)
        card.addView(etV.first); card.addView(etV.second)

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                when (pos) {
                    0 -> { // Slack
                        etP.second.isEnabled = false
                        etQ.second.isEnabled = false
                        etV.second.isEnabled = true
                    }
                    1 -> { // PV
                        etP.second.isEnabled = true
                        etQ.second.isEnabled = false
                        etV.second.isEnabled = true
                    }
                    else -> { // PQ
                        etP.second.isEnabled = true
                        etQ.second.isEnabled = true
                        etV.second.isEnabled = true
                    }
                }
            }
            override fun onNothingSelected(p: AdapterView<*>?) {}
        }
        // تفعيل الحالة الافتراضية (PQ) عند البناء
        etP.second.isEnabled = true
        etQ.second.isEnabled = true
        etV.second.isEnabled = true

        rows.add(BusRowViews(spinner, etP.second, etQ.second, etV.second))
        return card
    }

    private fun labeledEditText(label: String, hint: String): Pair<TextView, EditText> {
        val tv = TextView(this).apply {
            text = label
            setPadding(0, 12, 0, 4)
        }
        val et = EditText(this).apply {
            this.hint = hint
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or
                    android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL or
                    android.text.InputType.TYPE_NUMBER_FLAG_SIGNED
            setBackgroundResource(android.R.drawable.edit_text)
            setPadding(16, 12, 16, 12)
        }
        return Pair(tv, et)
    }

    private fun collectAndValidate(): Boolean {
        var slackCount = 0

        for (i in 0 until AppData.n) {
            val row = rows[i]
            val typePos = row.spinner.selectedItemPosition
            val type = when (typePos) { 0 -> 1; 1 -> 2; else -> 3 } // 1=Slack,2=PV,3=PQ
            if (type == 1) slackCount++

            AppData.busType[i] = type
            AppData.Psp[i] = row.etP.text.toString().toDoubleOrNull() ?: 0.0
            AppData.Qsp[i] = row.etQ.text.toString().toDoubleOrNull() ?: 0.0
            AppData.Vmag[i] = row.etV.text.toString().toDoubleOrNull() ?: 1.0
            AppData.delta[i] = 0.0
        }

        if (slackCount != 1) {
            Toast.makeText(this, "يجب اختيار بص Slack واحد بالضبط", Toast.LENGTH_LONG).show()
            return false
        }
        return true
    }
}
