package com.example.task_manager

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.seuprojeto.AppDatabase
import kotlinx.coroutines.launch

class TaskAdapter(
    private var tasks: List<Task>,
    private val onTaskCompleted: (Task) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val checkbox: CheckBox = itemView.findViewById(R.id.checkboxTask)
        val title: TextView = itemView.findViewById(R.id.textTaskTitle)
        val description: TextView = itemView.findViewById(R.id.textTaskDescription)
        val deleteButton: ImageButton = itemView.findViewById(R.id.deleteTaskButton)
        val expandButton: ImageButton = itemView.findViewById(R.id.expandButton)
        val expandableLayout: LinearLayout = itemView.findViewById(R.id.expandableLayout)
        val cardView: MaterialCardView = itemView.findViewById(R.id.cardView)

        fun bind(task: Task, position: Int, onTaskCompleted: (Task) -> Unit) {
            checkbox.setOnCheckedChangeListener(null)

            title.text = task.title
            description.text = when {
                task.description.isNullOrEmpty() -> "Sem descrição"
                else -> task.description
            }
            checkbox.isChecked = task.completed

            expandableLayout.visibility = View.GONE
            expandButton.rotation = 0f

            checkbox.setOnCheckedChangeListener { _, isChecked ->
                task.completed = isChecked
                onTaskCompleted(task)
            }

            cardView.setOnClickListener {
                toggleExpansion()
            }

            expandButton.setOnClickListener {
                toggleExpansion()
            }

            deleteButton.setOnClickListener {
                val context = itemView.context
                val db = AppDatabase.getDatabase(context)
                (context as AppCompatActivity).lifecycleScope.launch {
                    db.taskDAO().deleteTask(task)
                    val newList = tasks.toMutableList()
                    newList.removeAt(position)
                    updateData(newList)
                }
            }
        }

        private fun toggleExpansion() {
            if (expandableLayout.visibility == View.GONE) {
                // Expandir com animação
                expandableLayout.visibility = View.VISIBLE
                expandableLayout.alpha = 0f
                expandableLayout.animate()
                    .alpha(1f)
                    .setDuration(200)
                    .start()

                expandButton.animate().rotation(180f).setDuration(200).start()
            } else {
                expandableLayout.animate()
                    .alpha(0f)
                    .setDuration(200)
                    .withEndAction {
                        expandableLayout.visibility = View.GONE
                    }
                    .start()

                expandButton.animate().rotation(0f).setDuration(200).start()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]
        holder.bind(task, position, onTaskCompleted)
    }

    override fun getItemCount(): Int = tasks.size

    fun updateData(newTasks: List<Task>) {
        tasks = newTasks
        notifyDataSetChanged()
    }
}