package vcmsa.projects.budgettracker

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.ViewModelProvider

import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import vcmsa.projects.budgettracker.database.BudgetDatabase
import vcmsa.projects.budgettracker.viewmodel.BudgetViewModelFactory
import vcmsa.projects.budgettracker.viewmodel.BudgetViewModel
import vcmsa.projects.budgettracker.database.ExpenseDao
import vcmsa.projects.budgettracker.model.ExpenseWithCategory
import android.content.res.ColorStateList
import com.github.mikephil.charting.charts.HorizontalBarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.google.android.material.bottomnavigation.BottomNavigationView


class DashboardActivity : AppCompatActivity() {

    private lateinit var pieChart: PieChart
    private lateinit var btnAddExpense: Button
    private lateinit var btnViewExpenses: Button
    private lateinit var btnHome: Button
    private lateinit var tvSpent: TextView
    private lateinit var tvRemaining: TextView
    private lateinit var tvProjected: TextView
    private var userId: Int = 0

    private lateinit var progressBar: ProgressBar
    private lateinit var statusTextView: TextView
    private lateinit var tvLevelStatus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        userId = intent.getIntExtra("USER_ID", 0)

        val db = BudgetDatabase.getDatabase(applicationContext)
        val budgetDao = db.budgetDao()
        val expenseDao = db.expenseDao()
        val categoryDao = db.categoryDao()

        val factory = BudgetViewModelFactory(budgetDao, expenseDao, categoryDao, userId)
        val viewModel = ViewModelProvider(this, factory).get(BudgetViewModel::class.java)
        statusTextView = findViewById(R.id.statusTextView)

        tvLevelStatus = findViewById(R.id.tvLevelStatus)

        // Initialize the PieChart and Buttons
        pieChart = findViewById(R.id.pieChart)
        tvSpent = findViewById(R.id.tvSpent)
        tvRemaining = findViewById(R.id.tvRemaining)
        tvProjected = findViewById(R.id.tvProjected)
        progressBar = findViewById(R.id.budgetHealthProgressBar)
        // Setup the PieChart and Budget Health
        setupPieChart()
        setupBottomNavigation()

        setupBudgetHealth()

    }

    /*********
    Title: <PieChart>
    Author: </almic (Mick A.)>
    Date: <2019>
    Code version <3.1.0>
    Availability:<https://github.com/PhilJay/MPAndroidChart/blob/master/MPChartLib/src/main/java/com/github/mikephil/charting/charts/PieChart.java>
     **********/
    private fun setupPieChart() {
        lifecycleScope.launch(Dispatchers.Main) {
            val expenseDao = BudgetDatabase.getDatabase(applicationContext).expenseDao()
            expenseDao.getAllExpensesWithCategoryLive().observe(this@DashboardActivity) { expensesWithCategories ->

                val categoryMap = mutableMapOf<String, Float>()
                var totalSpent = 0f

                for (item in expensesWithCategories) {
                    val category = item.category.name
                    val amount = item.expense.amount.toFloat()
                    categoryMap[category] = categoryMap.getOrDefault(category, 0f) + amount
                    totalSpent += amount
                }

                // Pie Chart setup
                val entries = categoryMap.map { (category, totalAmount) ->
                    PieEntry(totalAmount, category)
                }

                val dataSet = PieDataSet(entries, "Expenses").apply {
                    colors = listOf(
                        Color.parseColor("#FFA500"),
                        Color.parseColor("#FFFFFF"),
                        Color.parseColor("#FF6347"),
                        Color.parseColor("#87CEEB")
                    )
                    setSliceSpace(3f)
                    setSelectionShift(5f)
                    setDrawValues(false)
                }

                val data = PieData(dataSet).apply {
                    setValueTextColor(Color.WHITE)
                    setValueTextSize(14f)
                }

                runOnUiThread {
                    pieChart.data = data
                    pieChart.description.isEnabled = false
                    pieChart.setCenterText("Expenses")
                    pieChart.setCenterTextColor(Color.WHITE)
                    pieChart.setRotationAngle(0f)
                    pieChart.animateY(1000)
                    pieChart.invalidate()

                    addLegend(categoryMap)

                    val budgetLimit = 5000f
                    val remaining = budgetLimit - totalSpent
                    val projected = if (remaining < 0) -remaining else 0f

                    setupHorizontalBarChart(totalSpent.toDouble(), remaining.toDouble(), projected.toDouble())

                    tvSpent.text = "R%.2f".format(totalSpent)
                    tvRemaining.text = "R%.2f".format(remaining)
                    tvProjected.text = "R%.2f".format(projected)

                    // 💡 Gamification: Level Calculation
                    val savings = if (remaining > 0) remaining else 0f
                    val savingsRate = if (totalSpent == 0f) 100f else (savings / totalSpent) * 100f

                    var level = "Bronze"
                    var progress = 25

                    when {
                        savingsRate >= 100f -> {
                            level = "Platinum"
                            progress = 100
                        }
                        savingsRate >= 50f -> {
                            level = "Gold"
                            progress = 75
                        }
                        savingsRate >= 25f -> {
                            level = "Silver"
                            progress = 50
                        }
                    }

                    progressBar.progress = progress
                    tvLevelStatus.text = "Level: $level (${"%.0f".format(savingsRate)}% Savings Rate)"
                }
            }
        }
    }





    private fun addLegend(categoryMap: Map<String, Float>) {
        val legendLayout = findViewById<LinearLayout>(R.id.legendLayout)
        legendLayout.removeAllViews() // Clear the legend before adding items

        categoryMap.forEach { (category, totalAmount) ->
            // Create a layout for each legend item
            val legendItem = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(0, 10, 0, 10)
            }

            // Create colored box
            val colorBox = View(this).apply {
                layoutParams = LinearLayout.LayoutParams(30, 30).apply {
                    marginEnd = 10
                }
                setBackgroundColor(getCategoryColor(category)) // Set color based on category
            }

            // Create a TextView for the category name and amount
            val textView = TextView(this).apply {
                text = "$category: R${totalAmount}"  // Change currency symbol to R
                setTextColor(Color.WHITE)
                textSize = 14f
            }

            // Add color box and text to the layout
            legendItem.addView(colorBox)
            legendItem.addView(textView)

            // Add the legend item to the layout
            legendLayout.addView(legendItem)
        }
    }

    // Helper function to map category to corresponding color
    private fun getCategoryColor(category: String): Int {
        return when (category) {
            "Orange Category" -> Color.parseColor("#FFA500")
            "White Category" -> Color.parseColor("#FFFFFF")
            "Tomato Category" -> Color.parseColor("#FF6347")
            "Sky Blue Category" -> Color.parseColor("#87CEEB")
            else -> Color.GRAY // Default color if category not found
        }
    }

    /*********
    Title: <HorizontalBarChart>
    Author: </danielgindi (Daniel Cohen Gindi)>
    Date: <2019>
    Code version <3.1.0>
    Availability:<https://github.com/PhilJay/MPAndroidChart/blob/master/MPChartLib/src/main/java/com/github/mikephil/charting/charts/HorizontalBarChart.java>
     **********/

    private fun setupHorizontalBarChart(totalSpent: Double, remainingBudget: Double, projectedOverspend: Double) {
        val barChart = findViewById<HorizontalBarChart>(R.id.horizontalBarChart)

        val entries = listOf(
            BarEntry(0f, totalSpent.toFloat()), // Spent
            BarEntry(1f, remainingBudget.toFloat()), // Remaining
            BarEntry(2f, projectedOverspend.toFloat()) // Projected
        )

        val dataSet = BarDataSet(entries, "Budget Overview").apply {
            colors = listOf(
                Color.parseColor("#FFA500"), // Orange for Spent
                Color.parseColor("#87CEEB"), // Light Blue for Remaining
                Color.parseColor("#FF6347")  // Red for Projected
            )
            valueTextColor = Color.WHITE
            valueTextSize = 12f
        }

        val data = BarData(dataSet)

        barChart.data = data
        barChart.setFitBars(true)
        barChart.description.isEnabled = false
        barChart.xAxis.valueFormatter = IndexAxisValueFormatter(listOf("Spent", "Remaining", "Projected"))
        barChart.xAxis.granularity = 1f
        barChart.xAxis.setDrawGridLines(false)
        barChart.axisLeft.setDrawGridLines(false)
        barChart.axisRight.isEnabled = false
        barChart.legend.isEnabled = false
        barChart.animateY(1000)
        barChart.invalidate()
    }

    private fun setupBudgetHealth() {
        lifecycleScope.launch(Dispatchers.IO) {
            val expenseDao = BudgetDatabase.getDatabase(applicationContext).expenseDao()
            val totalSpent = expenseDao.getTotalExpensesAmount()
            val monthlyBudget = 5000f // Static budget or fetch dynamically

            val spentPercentage = (totalSpent / monthlyBudget) * 100

            withContext(Dispatchers.Main) {
                tvSpent.text = "Spent: R${String.format("%.2f", totalSpent)}"
                tvRemaining.text = "Remaining: R${String.format("%.2f", monthlyBudget - totalSpent)}"

                if (totalSpent > monthlyBudget) {
                    tvProjected.text = "Projected Overspending: R${String.format("%.2f", totalSpent - monthlyBudget)}"
                    statusTextView.text = "Budget Status: Overspending!"
                    progressBar.progressTintList = ColorStateList.valueOf(Color.RED)
                } else {
                    tvProjected.text = "Projected Overspending: R0"
                    statusTextView.text = "Budget Status: Healthy"
                    progressBar.progressTintList = ColorStateList.valueOf(Color.parseColor("#00FF00"))
                }

                progressBar.progress = spentPercentage.toInt().coerceAtMost(100)
            }
        }
    }
    private fun setupBottomNavigation() {
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNavigation.selectedItemId = R.id.nav_dashboard

        bottomNavigation.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_home -> startActivity(Intent(this, HomeActivity::class.java))
                R.id.nav_dashboard -> return@setOnItemSelectedListener true
                R.id.nav_add_expense -> startActivity(Intent(this, AddExpenseActivity::class.java))
               // R.id.nav_budget_health -> startActivity(Intent(this, BudgetHealthActivity::class.java))
                R.id.nav_view_expenses -> startActivity(Intent(this, ExpensesListActivity::class.java))
            }
            true
        }
    }


}