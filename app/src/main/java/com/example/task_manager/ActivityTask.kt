package com.example.task_manager

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.seuprojeto.AppDatabase
import kotlinx.coroutines.launch

class ActivityTask : AppCompatActivity() {
    private lateinit var db: AppDatabase
    private lateinit var settingsButton: ImageButton
    private lateinit var buttonAddTask: MaterialButton
    private lateinit var cardContainer: MaterialCardView
    private lateinit var textCategoryTitle: TextView
    private lateinit var recyclerTasks: RecyclerView
    private lateinit var buttonNextCategory: MaterialButton

    private lateinit var googleSignInClient: GoogleSignInClient

    private var categoryIndex = 0
    private var categoriesWithTasks = listOf<TaskType>()
    private lateinit var adapter: TaskAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task)

        db = AppDatabase.getDatabase(this)
        settingsButton = findViewById(R.id.settingsButton)
        buttonAddTask = findViewById(R.id.buttonAddTask)
        buttonNextCategory = findViewById(R.id.buttonGoToNextCategory)
        recyclerTasks = findViewById(R.id.recyclerTasks)
        textCategoryTitle = findViewById(R.id.textCategoryTitle)
        cardContainer = findViewById(R.id.cardContainer)
        recyclerTasks.layoutManager = LinearLayoutManager(this)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        adapter = TaskAdapter(emptyList()) { task ->
            lifecycleScope.launch {
                if (task.completed) {
                    db.taskDAO().markAsCompleted(task.id)
                } else {
                    db.taskDAO().markAsUncompleted(task.id)
                }
                showTasksForCategory(task.type)
            }
        }
        recyclerTasks.adapter = adapter

        loadCategoriesWithTasks()

        buttonNextCategory.setOnClickListener {
            if (categoriesWithTasks.isNotEmpty()) {
                categoryIndex = (categoryIndex + 1) % categoriesWithTasks.size
                showTasksForCategory(categoriesWithTasks[categoryIndex])
            }
        }


        buttonAddTask.setOnClickListener {
            val intent = Intent(this, NewTaskActivity::class.java)
            startActivity(intent)
            finish()
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
    private fun loadCategoriesWithTasks() {
        lifecycleScope.launch {
            val allTasks = db.taskDAO().getAllTasks()

            val grouped = allTasks.groupBy {
                it.type
            }
            val categories = grouped.keys.toMutableList()

            if (allTasks.any { it.completed }) {
                categories.add(TaskType.COMPLETED)
            }

            categoriesWithTasks = categories

            if (categoriesWithTasks.isNotEmpty()) {
                showTasksForCategory(categoriesWithTasks[0])
            } else {
                textCategoryTitle.text = "Nenhuma tarefa cadastrada"
                adapter.updateData(emptyList())
                cardContainer.setCardBackgroundColor(getColor(android.R.color.darker_gray))
            }
        }
    }



    private fun showTasksForCategory(type: TaskType) {
        lifecycleScope.launch {
            val tasks = when(type) {
                TaskType.COMPLETED -> db.taskDAO().getCompletedTasks()
                else -> db.taskDAO().getTasksByType(type)
            }

            textCategoryTitle.text = when(type) {
                TaskType.PERSONAL -> "Pessoais"
                TaskType.WORK -> "Trabalho"
                TaskType.SOCIAL -> "Social"
                TaskType.STUDY -> "Estudo"
                TaskType.COMPLETED -> "Concluídas"
            }

            val colorRes = when(type) {
                TaskType.PERSONAL -> R.color.personalCategory
                TaskType.WORK -> R.color.workCategory
                TaskType.SOCIAL -> R.color.socialCategory
                TaskType.STUDY -> R.color.studyCategory
                TaskType.COMPLETED -> R.color.completedCategory
            }
            cardContainer.setCardBackgroundColor(getColor(colorRes))

            adapter.updateData(tasks)
        }
    }


}
