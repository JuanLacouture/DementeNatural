package com.example.dementenatural

import android.os.Bundle
import android.content.Intent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate

class Estadisticas_Ventas : AppCompatActivity() {

    private lateinit var monthlySalesChart: BarChart
    private lateinit var objectsSoldChart: PieChart
    private lateinit var backButton: CardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_estadisticas_ventas)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        monthlySalesChart = findViewById(R.id.monthlySalesChart)
        objectsSoldChart  = findViewById(R.id.objectsSoldChart)
        backButton        = findViewById(R.id.backButton)

        setupMonthlySalesChart()
        setupObjectsSoldChart()

        backButton.setOnClickListener { finish() }
    }

    private fun setupMonthlySalesChart() {
        val entries = listOf(
            BarEntry(0f, 12000f),
            BarEntry(1f, 18000f),
            BarEntry(2f, 8000f),
            BarEntry(3f, 20000f),
            BarEntry(4f, 15000f),
            BarEntry(5f, 22000f)
        )
        val set = BarDataSet(entries, "Ventas (COP)").apply {
            colors = ColorTemplate.MATERIAL_COLORS.toList()
            valueTextSize = 12f
        }
        monthlySalesChart.apply {
            data = BarData(set)
            xAxis.apply {
                granularity = 1f
                valueFormatter = IndexAxisValueFormatter(
                    listOf("Ene","Feb","Mar","Abr","May","Jun")
                )
                setDrawGridLines(false)
            }
            axisLeft.axisMinimum = 0f
            axisRight.isEnabled = false
            description = Description().apply { text = "" }
            animateY(1000)
            invalidate()
        }
    }

    private fun setupObjectsSoldChart() {
        val entries = listOf(
            PieEntry(40f, "Collar"),
            PieEntry(60f, "Pulsera"),
            PieEntry(20f, "Anillo"),
            PieEntry(30f, "Aretes")
        )
        val set = PieDataSet(entries, "Unidades vendidas").apply {
            colors = ColorTemplate.COLORFUL_COLORS.toList()
            sliceSpace = 2f
            valueTextSize = 12f
        }
        objectsSoldChart.apply {
            data = PieData(set)
            isDrawHoleEnabled = true
            holeRadius = 40f
            transparentCircleRadius = 45f
            description = Description().apply { text = "" }
            centerText = "Objetos"
            setEntryLabelColor(ColorTemplate.getHoloBlue())
            animateY(1000)
            invalidate()
        }
    }
}
