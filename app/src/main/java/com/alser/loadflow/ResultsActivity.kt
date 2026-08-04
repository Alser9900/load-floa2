package com.alser.loadflow

import android.graphics.Color
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ResultsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_results)

        val container = findViewById<LinearLayout>(R.id.results_container)
        val busCount = intent.getIntExtra("BUS_COUNT", 0)
        val busTypes = intent.getIntArrayExtra("BUS_TYPES") ?: IntArray(busCount)

        // Dummy processing to show direct results based on bus type
        for (i in 0 until busCount) {
            val type = busTypes[i]
            
            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(32, 32, 32, 32)
                setBackgroundResource(R.drawable.edit_text_bg)
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.setMargins(0, 0, 0, 32)
                layoutParams = params
            }

            val title = TextView(this).apply {
                text = "البص رقم ${i+1}"
                textSize = 20f
                setTextColor(Color.parseColor("#1E88E5"))
                setTypeface(null, android.graphics.Typeface.BOLD)
                setPadding(0, 0, 0, 16)
            }
            card.addView(title)

            // Logic to show MISSING variables directly
            when (type) {
                1 -> { // Slack - Missing P, Q
                    card.addView(createResultText("P = 1.25 p.u."))
                    card.addView(createResultText("Q = 0.45 p.u."))
                }
                2 -> { // PV - Missing Q, Delta
                    card.addView(createResultText("Q = 0.60 p.u."))
                    card.addView(createResultText("الزاوية = -2.5°"))
                }
                3 -> { // PQ - Missing V, Delta
                    card.addView(createResultText("V = 0.95 p.u."))
                    card.addView(createResultText("الزاوية = -4.1°"))
                }
                else -> {
                    card.addView(createResultText("تم الحساب بنجاح"))
                }
            }
            container.addView(card)
        }
    }

    private fun createResultText(result: String): TextView {
        return TextView(this).apply {
            text = result
            textSize = 22f // كبير ومباشر
            setTextColor(Color.BLACK)
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(0, 8, 0, 8)
        }
    }
}