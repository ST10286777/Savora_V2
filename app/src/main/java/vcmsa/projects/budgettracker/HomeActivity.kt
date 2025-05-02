package vcmsa.projects.budgettracker

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.android.material.bottomnavigation.BottomNavigationView


class HomeActivity : AppCompatActivity() {

    private lateinit var cardCategories: CardView
    private lateinit var cardAddExpense: CardView
    private lateinit var cardViewExpenses: CardView
    private lateinit var cardBudget: CardView
    private lateinit var cardInputIncome: CardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        setupBottomNavigation()

        // Initialize CardViews instead of Buttons
        cardCategories = findViewById(R.id.cardCategories)
        cardAddExpense = findViewById(R.id.cardAddExpense)
        cardViewExpenses = findViewById(R.id.cardViewExpenses)
        cardBudget = findViewById(R.id.cardBudget)
        cardInputIncome = findViewById(R.id.cardInputIncome)

        // Load the button press animation
        val pressAnim = AnimationUtils.loadAnimation(this, R.anim.button_press)

        // Apply animation when cards are pressed
        cardCategories.setOnClickListener {
            cardCategories.startAnimation(pressAnim)
            startActivity(Intent(this, CategoryActivity::class.java))
        }

        cardAddExpense.setOnClickListener {
            cardAddExpense.startAnimation(pressAnim)
            startActivity(Intent(this, AddExpenseActivity::class.java))
        }

        cardViewExpenses.setOnClickListener {
            cardViewExpenses.startAnimation(pressAnim)
            startActivity(Intent(this, ExpensesListActivity::class.java))
        }

        cardBudget.setOnClickListener {
            cardBudget.startAnimation(pressAnim)
            startActivity(Intent(this, BudgetDescriptionActivity::class.java))
        }

        cardInputIncome.setOnClickListener {
            cardInputIncome.startAnimation(pressAnim)
            val intent = Intent(this, SetBudgetActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupBottomNavigation() {
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNavigation.selectedItemId = R.id.nav_dashboard

        bottomNavigation.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_home -> startActivity(Intent(this, HomeActivity::class.java))
                R.id.nav_dashboard -> startActivity(Intent(this, DashboardActivity::class.java))
                R.id.nav_add_expense -> startActivity(Intent(this, AddExpenseActivity::class.java))
                R.id.nav_view_expenses -> startActivity(Intent(this, ExpensesListActivity::class.java))
            }
            true
        }
    }
}