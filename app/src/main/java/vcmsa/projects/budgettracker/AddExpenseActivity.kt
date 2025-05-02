package vcmsa.projects.budgettracker

import android.Manifest
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import vcmsa.projects.budgettracker.adapter.ExpenseAdapter
import vcmsa.projects.budgettracker.database.BudgetDatabase
import vcmsa.projects.budgettracker.model.Expense
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class AddExpenseActivity : AppCompatActivity() {
    // Views
    private lateinit var etAmount: EditText
    private lateinit var spinnerCategory: Spinner
    private lateinit var etDescription: EditText
    private lateinit var btnSelectDate: Button
    private lateinit var tvDate: TextView
    private lateinit var btnSaveExpense: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var spinnerPayment: Spinner
    private lateinit var etTags: EditText
    private lateinit var etNotes: EditText
    private lateinit var btnAttachReceipt: Button
    private lateinit var btnCapturePhoto: Button
    private lateinit var btnSelectStartDate: Button
    private lateinit var tvStartDate: TextView
    private lateinit var btnSelectEndDate: Button
    private lateinit var tvEndDate: TextView

    // Variables
    private val categoryMap = mutableMapOf<String, Int>()
    private var selectedDateMillis: Long = System.currentTimeMillis()
    private var selectedStartDateMillis: Long? = null
    private var selectedEndDateMillis: Long? = null
    private var currentPhotoPath: String? = null
    private var receiptUri: String? = null
    private var latitude: Double? = null
    private var longitude: Double? = null

    private lateinit var expenseAdapter: ExpenseAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_expense)

        requestPermissions()
        bindViews()
        setupRecyclerView()
        setupPaymentSpinner()
        loadCategories()
        fetchLastLocation()

        // Date selection
        btnSelectDate.setOnClickListener {
            showDatePicker { millis ->
                selectedDateMillis = millis
                tvDate.text = "Date: ${formatDate(millis)}"
            }
        }

        btnSelectStartDate.setOnClickListener {
            showDatePicker { millis ->
                selectedStartDateMillis = millis
                tvStartDate.text = "Start: ${formatDate(millis)}"
            }
        }

        btnSelectEndDate.setOnClickListener {
            showDatePicker { millis ->
                selectedEndDateMillis = millis
                tvEndDate.text = "End: ${formatDate(millis)}"
            }
        }

        btnAttachReceipt.setOnClickListener { pickReceiptFromGallery() }
        btnCapturePhoto.setOnClickListener { dispatchTakePictureIntent() }
        btnSaveExpense.setOnClickListener { saveExpense() }
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
            ),
            1001
        )
    }

    private fun bindViews() {
        etAmount = findViewById(R.id.etAmount)
        spinnerCategory = findViewById(R.id.spinnerCategory)
        etDescription = findViewById(R.id.etDescription)
        btnSelectDate = findViewById(R.id.btnSelectDate)
        tvDate = findViewById(R.id.tvDate)
        btnSaveExpense = findViewById(R.id.btnSaveExpense)
        recyclerView = findViewById(R.id.rvExpenses)
        spinnerPayment = findViewById(R.id.spinnerPayment)
        etTags = findViewById(R.id.etTags)
        etNotes = findViewById(R.id.etNotes)
        btnAttachReceipt = findViewById(R.id.btnAttachReceipt)
        btnCapturePhoto = findViewById(R.id.btnCapturePhoto)
        btnSelectStartDate = findViewById(R.id.btnSelectStartDate)
        tvStartDate = findViewById(R.id.tvStartDate)
        btnSelectEndDate = findViewById(R.id.btnSelectEndDate)
        tvEndDate = findViewById(R.id.tvEndDate)
    }

    /*********
    Title: <Android RecyclerView in Kotlin>
    Author: <GeeksForGeeks>
    Date: <2025>
    Code version <1>
    Availability:<https://www.geeksforgeeks.org/android-recyclerview-in-kotlin/>
     **********/
    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        expenseAdapter = ExpenseAdapter()
        recyclerView.adapter = expenseAdapter
    }


    private fun setupPaymentSpinner() {
        val methods = listOf("Cash", "Card", "Mobile Pay")
        spinnerPayment.adapter = ArrayAdapter(
            this,
            R.layout.spinner_item,
            methods
        ).also { it.setDropDownViewResource(R.layout.spinner_dropdown_item) }
    }

    /*********
    Title: <ROOM Database - #2 Read and Write Data | Android Studio Tutorial | Kotlin | 2022>
    Author: <FoxAndroid>
    Date: <2022>
    Code version: <video published on  Jun 22, 2022>
    Availability:<https://www.youtube.com/watch?v=-dm4cYlKiAA>
     **********/
    private fun loadCategories() {
        lifecycleScope.launch(Dispatchers.IO) {
            val db = BudgetDatabase.getDatabase(applicationContext)
            val categories = db.categoryDao().getAllCategories()
            val names = categories.map {
                categoryMap[it.name] = it.id
                it.name
            }
            withContext(Dispatchers.Main) {
                spinnerCategory.adapter = ArrayAdapter(
                    this@AddExpenseActivity,
                    R.layout.spinner_item,
                    names
                ).also {
                    it.setDropDownViewResource(R.layout.spinner_dropdown_item)
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun fetchLastLocation() {
        LocationServices.getFusedLocationProviderClient(this)
            .lastLocation
            .addOnSuccessListener { location ->
                location?.let {
                    latitude = it.latitude
                    longitude = it.longitude
                }
            }
    }

    private fun showDatePicker(onDateSelected: (Long) -> Unit) {
        val cal = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                cal.set(year, month, dayOfMonth)
                onDateSelected(cal.timeInMillis)
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun formatDate(millis: Long): String {
        return SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(millis))
    }

    private fun getCurrentUserId(): Int {
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        return sharedPreferences.getInt("user_id", 0)
    }

    private fun saveExpense() {
        val amount = etAmount.text.toString().trim().toDoubleOrNull()
        val description = etDescription.text.toString().trim()
        val categoryName = spinnerCategory.selectedItem?.toString()
        val paymentMethod = spinnerPayment.selectedItem?.toString()
        val tags = etTags.text.toString().takeIf(String::isNotBlank)
        val notes = etNotes.text.toString().takeIf(String::isNotBlank)

        if (amount == null || categoryName == null || paymentMethod == null) {
            Toast.makeText(this, "Please fill in all required fields.", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedStartDateMillis == null || selectedEndDateMillis == null) {
            Toast.makeText(this, "Select start and end dates.", Toast.LENGTH_SHORT).show()
            return
        }

        val categoryId = categoryMap[categoryName] ?: run {
            Toast.makeText(this, "Invalid category.", Toast.LENGTH_SHORT).show()
            return
        }

        val expense = Expense(
            userId = getCurrentUserId(),
            amount = amount,
            date = selectedDateMillis,
            startTime = selectedStartDateMillis,
            endTime = selectedEndDateMillis,
            description = description,
            categoryId = categoryId,
            category = categoryName,
            paymentMethod = paymentMethod,
            latitude = latitude,
            longitude = longitude,
            tags = tags,
            notes = notes,
            receiptPhoto = receiptUri
        )

        lifecycleScope.launch(Dispatchers.IO) {
            BudgetDatabase.getDatabase(applicationContext)
                .expenseDao()
                .insertExpense(expense)
            withContext(Dispatchers.Main) {
                Toast.makeText(this@AddExpenseActivity, "Expense saved", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    /*********
    Title: <how to get image from gallery in android 10 and above>
    Author: <Sanjay S Gangwar>
    Date: <2021>
    Code version: <version from article published on Feb 12, 2021>
    Availability:<https://sanjaysgangwar.medium.com/how-to-get-image-from-gallery-in-android-10-and-above-e5f75a41a865>
     **********/
    private fun pickReceiptFromGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        startActivityForResult(Intent.createChooser(intent, "Select Receipt Image"), 2001)
    }

    /*********
    Title: <Using FileProvider properly in Android>
    Author: <Aung Kyaw Myint>
    Date: <2019>
    Code version: <version from article published on May 16, 2019>
    Availability:<https://medium.com/@aungkyawmyint_26195/using-implicit-camera-intent-with-fileprovider-8182f59ee972>
     **********/
    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takeIntent ->
            takeIntent.resolveActivity(packageManager)?.also {
                createImageFile()?.also { file ->
                    val uri = FileProvider.getUriForFile(
                        this,
                        "$packageName.fileprovider",
                        file
                    )
                    takeIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
                    startActivityForResult(takeIntent, 2002)
                }
            }
        }
    }

    /*********
    Title: <Android Photo App 05 save image file>
    Author: <Programming w/ Professor Sluiter>
    Date: <2019>
    Code version: <version from video published on Feb 21, 2019>
    Availability:<https://www.youtube.com/watch?v=ibL9c1IfeIQ>
     **********/
    private fun createImageFile(): File? {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir).apply {
            currentPhotoPath = absolutePath
        }
    }
    private fun copyToInternalStorage(uri: android.net.Uri): String? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val fileName = "receipt_${System.currentTimeMillis()}.jpg"
            val file = File(filesDir, fileName)

            inputStream?.use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_OK) return

        when (requestCode) {
            2001 -> data?.data?.let { uri ->
                val storedPath = copyToInternalStorage(uri)
                if (storedPath != null) {
                    receiptUri = storedPath
                    Toast.makeText(this, "Receipt saved internally", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to save receipt", Toast.LENGTH_SHORT).show()
                }
            }
            2002 -> currentPhotoPath?.let {
                receiptUri = it
                Toast.makeText(this, "Photo captured", Toast.LENGTH_SHORT).show()
            }
        }

    }
}
