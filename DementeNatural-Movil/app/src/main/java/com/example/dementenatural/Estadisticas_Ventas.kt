package com.example.dementenatural

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.firebase.database.*

class Estadisticas_Ventas : AppCompatActivity() {

    private lateinit var mDBRef: DatabaseReference
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

        mDBRef = FirebaseDatabase.getInstance().reference
        monthlySalesChart = findViewById(R.id.monthlySalesChart)
        objectsSoldChart  = findViewById(R.id.objectsSoldChart)
        backButton        = findViewById(R.id.backButton)

        // 1) Cargo inventario
        mDBRef.child("Inventario")
            .addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(invSnap: DataSnapshot) {
                    val invMap = mutableMapOf<String, Product>()
                    for (c in invSnap.children) {
                        c.getValue(Product::class.java)?.let { p ->
                            invMap[c.key!!] = p.copy(id = c.key!!)
                        }
                    }
                    // 2) Cargo ventas y construyo gráficas
                    loadSalesAndRenderCharts(invMap)
                }
                override fun onCancelled(err: DatabaseError) { /*…*/ }
            })

        backButton.setOnClickListener { finish() }
    }

    private fun loadSalesAndRenderCharts(inventory: Map<String, Product>) {
        mDBRef.child("Ventas")
            .addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(salesSnap: DataSnapshot) {
                    val qtyPerProduct = mutableMapOf<String, Int>()
                    val revenuePerProduct = mutableMapOf<String, Float>()

                    // Acumulo unidades y ganancias
                    for (c in salesSnap.children) {
                        val sale = c.getValue(Sale::class.java) ?: continue
                        sale.products.forEach { (prodId, qty) ->
                            val prod = inventory[prodId] ?: return@forEach
                            qtyPerProduct[prodId] =
                                qtyPerProduct.getOrDefault(prodId, 0) + qty
                            val rev =
                                revenuePerProduct.getOrDefault(prodId, 0f) +
                                        (prod.price * qty).toFloat()
                            revenuePerProduct[prodId] = rev
                        }
                    }

                    // --- TOP 5 Ganancia ---
                    val topRevenue = revenuePerProduct.entries
                        .sortedByDescending { it.value }
                        .take(5)
                    val revLabels = topRevenue.map { inventory[it.key]?.name ?: it.key }
                    val revEntries = topRevenue.mapIndexed { idx, e ->
                        BarEntry(idx.toFloat(), e.value)
                    }
                    val barSet = BarDataSet(revEntries, "Ganancia").apply {
                        colors = ColorTemplate.MATERIAL_COLORS.toList()
                        valueTextSize = 12f
                    }
                    monthlySalesChart.apply {
                        data = BarData(barSet)
                        xAxis.apply {
                            granularity = 1f
                            valueFormatter = IndexAxisValueFormatter(revLabels)
                            setDrawGridLines(false)
                        }
                        axisLeft.axisMinimum = 0f
                        axisRight.isEnabled = false
                        description = Description().apply { text = "" }
                        animateY(800)
                        invalidate()
                    }

                    // --- TOP 5 Unidades Vendidas ---
                    val topQty = qtyPerProduct.entries
                        .sortedByDescending { it.value }
                        .take(5)
                    val pieEntries = topQty.map {
                        PieEntry(it.value.toFloat(),
                            inventory[it.key]?.name ?: it.key)
                    }
                    val pieSet = PieDataSet(pieEntries, "Unidades").apply {
                        colors = ColorTemplate.COLORFUL_COLORS.toList()
                        sliceSpace = 2f
                        valueTextSize = 12f
                    }
                    objectsSoldChart.apply {
                        data = PieData(pieSet)
                        isDrawHoleEnabled = true
                        holeRadius = 40f
                        transparentCircleRadius = 45f
                        description = Description().apply { text = "" }
                        centerText = "Vendidos"
                        animateY(800)
                        invalidate()
                    }
                }
                override fun onCancelled(error: DatabaseError) { /*…*/ }
            })
    }
}
