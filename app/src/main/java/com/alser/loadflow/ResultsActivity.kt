package com.alser.loadflow

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ResultsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_results)

        val tvStatus = findViewById<TextView>(R.id.tvStatus)
        val tvResults = findViewById<TextView>(R.id.tvResults)
        val btnRestart = findViewById<Button>(R.id.btnRestart)

        val result = AppData.result

        if (result == null) {
            tvStatus.text = "لا توجد نتائج"
        } else {
            if (result.converged) {
                tvStatus.text = "✔ تم التقارب بعد ${result.iterations} تكرار (iteration)"
                tvStatus.setTextColor(Color.parseColor("#2E7D32"))
            } else {
                tvStatus.text = "✘ لم يتم التقارب خلال ${result.iterations} تكرار — تحقق من بيانات الإدخال"
                tvStatus.setTextColor(Color.parseColor("#C62828"))
            }

            val sb = StringBuilder()
            sb.append("---- الفولت والزوايا النهائية ----\n\n")
            for (i in 0 until AppData.n) {
                sb.append("Bus ${i + 1}:\n")
                sb.append("  |V|    = ${"%.4f".format(result.Vmag[i])} pu\n")
                sb.append("  delta  = ${"%.4f".format(result.deltaDeg[i])} deg\n")
                sb.append("  P      = ${"%.4f".format(result.Pfinal[i])} pu\n")
                sb.append("  Q      = ${"%.4f".format(result.Qfinal[i])} pu\n")
                sb.append("  النوع  = ${busTypeName(AppData.busType[i])}\n\n")
            }
            tvResults.text = sb.toString()
        }

        btnRestart.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }
    }

    private fun busTypeName(type: Int) = when (type) {
        1 -> "Slack"
        2 -> "PV"
        else -> "PQ"
    }
}
