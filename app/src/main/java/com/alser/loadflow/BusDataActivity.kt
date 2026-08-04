package com.alser.loadflow

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class BusDataActivity : AppCompatActivity() {
    
    private var busCount = 0
    private val busSpinners = mutableListOf<Spinner>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bus_data)

        busCount = intent.getIntExtra("BUS_COUNT", 0)
        val container = findViewById<LinearLayout>(R.id.buses_container)
        val btnNext = findViewById<Button>(R.id.btn_next_ybus)

        val types = arrayOf("اختر النوع...", "Slack Bus", "PV (Generator) Bus", "PQ (Load) Bus")

        for (i in 1..busCount) {
            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(24, 24, 24, 24)
                setBackgroundResource(R.drawable.edit_text_bg)
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.setMargins(0, 0, 0, 32)
                layoutParams = params
            }

            val title = TextView(this).apply {
                text = "البص رقم $i"
                textSize = 18f
                setTextColor(Color.parseColor("#1E88E5"))
                setTypeface(null, android.graphics.Typeface.BOLD)
                setPadding(0, 0, 0, 16)
            }
            card.addView(title)

            val spinner = Spinner(this).apply {
                val adapter = ArrayAdapter(this@BusDataActivity, android.R.layout.simple_spinner_dropdown_item, types)
                this.adapter = adapter
            }
            busSpinners.add(spinner)
            card.addView(spinner)

            // Dynamic Inputs Container
            val inputsContainer = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(0, 16, 0, 0)
            }
            card.addView(inputsContainer)

            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    inputsContainer.removeAllViews()
                    when (position) {
                        1 -> { // Slack
                            inputsContainer.addView(createInput("V (الجهد):"))
                            inputsContainer.addView(createInput("الزاوية (Delta):"))
                        }
                        2 -> { // PV
                            inputsContainer.addView(createInput("P (القدرة الفعالة):"))
                            inputsContainer.addView(createInput("V (الجهد):"))
                        }
                        3 -> { // PQ
                            inputsContainer.addView(createInput("P (القدرة الفعالة):"))
                            inputsContainer.addView(createInput("Q (القدرة غير الفعالة):"))
                        }
                    }
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

            container.addView(card)
        }

        btnNext.setOnClickListener {
            // Check if all types selected
            var allSelected = true
            for (spinner in busSpinners) {
                if (spinner.selectedItemPosition == 0) {
                    allSelected = false
                    break
                }
            }
            if (allSelected) {
                val intent = Intent(this, YbusActivity::class.java)
                intent.putExtra("BUS_COUNT", busCount)
                
                // Pass types to Results
                val typesList = busSpinners.map { it.selectedItemPosition }.toIntArray()
                intent.putExtra("BUS_TYPES", typesList)
                
                startActivity(intent)
            } else {
                Toast.makeText(this, "الرجاء تحديد نوع كل بص", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun createInput(hintText: String): EditText {
        return EditText(this).apply {
            hint = hintText
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL or android.text.InputType.TYPE_NUMBER_FLAG_SIGNED
            setTextColor(Color.BLACK)
            setHintTextColor(Color.GRAY)
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(0, 8, 0, 8)
            layoutParams = params
            setPadding(16, 24, 16, 24)
            setBackgroundColor(Color.parseColor("#F5F5F5"))
        }
    }
}