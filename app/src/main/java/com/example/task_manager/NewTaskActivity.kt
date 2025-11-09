package com.example.task_manager

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.seuprojeto.AppDatabase
import kotlinx.coroutines.launch

class NewTaskActivity : AppCompatActivity() {
    private lateinit var editTitleInput: EditText
    private lateinit var editDescriptionInput: EditText
    private lateinit var checkboxPersonalCategory: MaterialCheckBox
    private lateinit var checkboxWorkCategory: MaterialCheckBox
    private lateinit var checkboxSocialCategory: MaterialCheckBox
    private lateinit var checkboxStudyCategory: MaterialCheckBox
    private lateinit var buttonSaveTask: MaterialButton
    private lateinit var buttonCancelTask: MaterialButton
    private lateinit var settingsButton: ImageButton
    private lateinit var googleSignInClient: GoogleSignInClient

    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_new_task)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

         editTitleInput = findViewById<EditText>(R.id.titleNewTaskInput)
            settingsButton = findViewById<ImageButton>(R.id.settingsButton)
          editDescriptionInput = findViewById<EditText>(R.id.descriptionNewTaskInput)
          checkboxPersonalCategory = findViewById<MaterialCheckBox>(R.id.checkboxPersonalCategory)
          checkboxWorkCategory = findViewById<MaterialCheckBox>(R.id.checkboxWorkCategory)
          checkboxSocialCategory = findViewById<MaterialCheckBox>(R.id.checkboxSocialCategory)
         checkboxStudyCategory = findViewById<MaterialCheckBox>(R.id.checkboxStudyCategory)
          buttonSaveTask = findViewById<MaterialButton>(R.id.buttonSaveTask)
          buttonCancelTask = findViewById<MaterialButton>(R.id.buttonCancelTask)
            db = AppDatabase.getDatabase(this)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        setupCategoryCheckboxes()

        buttonCancelTask.setOnClickListener {
            editTitleInput.text.clear()
            editDescriptionInput.text.clear()
            checkboxPersonalCategory.isChecked = false
            checkboxWorkCategory.isChecked = false
            checkboxSocialCategory.isChecked = false
            checkboxStudyCategory.isChecked = false
            checkboxStudyCategory.isChecked = false

            val intent = Intent(this, ActivityTask::class.java)
            startActivity(intent)
            finish()
        }

        buttonSaveTask.setOnClickListener {
            val title = editTitleInput.text.toString()
            val description = editDescriptionInput.text.toString()
            val type = when {
                checkboxPersonalCategory.isChecked -> TaskType.PERSONAL
                checkboxWorkCategory.isChecked -> TaskType.WORK
                checkboxSocialCategory.isChecked -> TaskType.SOCIAL
                checkboxStudyCategory.isChecked -> TaskType.STUDY
                else -> null
            }

            if (title.isEmpty()) {
                Toast.makeText(this, "Digite um título para a tarefa!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (type == null) {
                Toast.makeText(this, "Selecione uma categoria para a tarefa!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newTask = Task(title = title, description = description, type = type)



            lifecycleScope.launch {
                db.taskDAO().create(newTask)
                Toast.makeText(this@NewTaskActivity, "Tarefa salva com sucesso!", Toast.LENGTH_SHORT).show()
                editTitleInput.text.clear()
                editDescriptionInput.text.clear()
                checkboxPersonalCategory.isChecked = false
                checkboxWorkCategory.isChecked = false
                checkboxSocialCategory.isChecked = false
                checkboxStudyCategory.isChecked = false
                checkboxStudyCategory.isChecked = false
            }
        }

        settingsButton.setOnClickListener { view ->
            val popup = PopupMenu(this, view)
            popup.menuInflater.inflate(R.menu.settings_menu, popup.menu)

            popup.setOnMenuItemClickListener {
                    menuItem ->
                when (menuItem.itemId) {
                    R.id.logoutMenu -> {
                        performLogout()
                        true
                    }
                    else -> false
                }
            }

            popup.show()
        }

    }

    private fun performLogout() {
        googleSignInClient.signOut().addOnCompleteListener(this) {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
    private fun setupCategoryCheckboxes() {
        val checkboxes = listOf(
            checkboxPersonalCategory,
            checkboxWorkCategory,
            checkboxSocialCategory,
            checkboxStudyCategory
        )

        checkboxes.forEach { checkbox ->
            checkbox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    // Desmarca todos os outros checkboxes
                    checkboxes.filter { it != checkbox }.forEach { it.isChecked = false }
                }
            }
        }
    }
}