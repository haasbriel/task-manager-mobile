package com.example.task_manager

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.seuprojeto.AppDatabase
import kotlinx.coroutines.launch

class TaskAdapter(
    private var tasks: List<Task>,
    private val onTaskCompleted: (Task) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val checkbox: CheckBox = itemView.findViewById(R.id.checkboxTask)
        val title: TextView = itemView.findViewById(R.id.textTaskTitle)
        val deleteButton: ImageButton = itemView.findViewById(R.id.deleteTaskButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]

        // Primeiro remove listener antigo para evitar chamadas indesejadas
        holder.checkbox.setOnCheckedChangeListener(null)

        holder.title.text = task.title
        holder.checkbox.isChecked = task.completed

        holder.checkbox.setOnCheckedChangeListener { _, isChecked ->
            // Atualiza o objeto task
            task.completed = isChecked
            // Chama callback para atualizar no banco
            onTaskCompleted(task)
        }

        holder.deleteButton.setOnClickListener {
            val context = holder.itemView.context
            val db = AppDatabase.getDatabase(context)
            (context as AppCompatActivity).lifecycleScope.launch {
                db.taskDAO().deleteTask(task)
                val newList = tasks.toMutableList()
                newList.removeAt(position)
                updateData(newList)
            }
        }
    }

    override fun getItemCount(): Int = tasks.size

    fun updateData(newTasks: List<Task>) {
        tasks = newTasks
        notifyDataSetChanged()
    }
}
