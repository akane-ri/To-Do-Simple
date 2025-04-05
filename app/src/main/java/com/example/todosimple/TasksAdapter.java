package com.example.todosimple;

import android.graphics.Paint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TasksAdapter extends RecyclerView.Adapter<TasksAdapter.TaskViewHolder> {

    private OnTaskClickListener onTaskClickListener;
    private List<Task> tasks = new ArrayList<>();
    private List<Task> filteredTasks = new ArrayList<>(); // Инициализируем список filteredTasks
    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference referenceTasks = firebaseDatabase.getReference("Tasks");

    public Task getTasks(int position) {
        return tasks.get(position);
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
        this.filteredTasks = new ArrayList<>(tasks); // Обновляем filteredTasks при установке нового списка задач
        notifyDataSetChanged();
        Log.d("da", "Tasks set: " + tasks.size());
    }

    public void setSelectedCategory(Category category) {
        filterTasksByCategory(category);
    }

    public void setOnTaskClickListener(OnTaskClickListener onTaskClickListener) {
        this.onTaskClickListener = onTaskClickListener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.task_item, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = filteredTasks.get(position); // Используем filteredTasks для отображения данных
        holder.textViewToDoText.setText(task.getText());
        if (task.getPriority()) {
            holder.imageViewPriority.setVisibility(View.VISIBLE);
        } else {
            holder.imageViewPriority.setVisibility(View.GONE);
        }
        if (!Objects.equals(task.getDate(), "")) {
            holder.textViewDateTime.setVisibility(View.VISIBLE);
            holder.textViewDateTime.setText(task.getDate());
        } else {
            holder.textViewDateTime.setVisibility(View.GONE);
        }
        holder.checkBoxTask.setChecked(task.getStatus());
        if (task.getStatus()) {
            holder.textViewToDoText.setPaintFlags(holder.textViewToDoText.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.textViewToDoText.setPaintFlags(holder.textViewToDoText.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onTaskClickListener != null) {
                    onTaskClickListener.onTaskClick(task);
                }
            }
        });

        holder.checkBoxTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.checkBoxTask.isChecked()) {
                    referenceTasks.child(task.getIdOwner()).child(task.getId()).child("status").setValue(true);
                    holder.textViewToDoText.setPaintFlags(holder.textViewToDoText.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                } else {
                    referenceTasks.child(task.getIdOwner()).child(task.getId()).child("status").setValue(false);
                    holder.textViewToDoText.setPaintFlags(holder.textViewToDoText.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return filteredTasks.size(); // Возвращаем количество элементов в filteredTasks
    }

    // Метод для фильтрации задач по категории
    public void filterTasksByCategory(Category category) {
        filteredTasks.clear(); // Очищаем отфильтрованные задачи

        // Если название категории "All", выводим все задачи
        if (category.getName().equals("All")) {
            filteredTasks.addAll(tasks);
        } else {
            // В противном случае фильтруем задачи по выбранной категории
            for (Task task : tasks) {
                if (task.getCategory().equals(category.getName())) {
                    filteredTasks.add(task);
                }
            }
        }
        notifyDataSetChanged(); // Обновляем RecyclerView
    }

    interface OnTaskClickListener {
        void onTaskClick(Task task);
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {

        private CheckBox checkBoxTask;
        private TextView textViewToDoText;
        private ImageView imageViewPriority;
        private TextView textViewDateTime;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBoxTask = itemView.findViewById(R.id.checkBoxTask);
            textViewToDoText = itemView.findViewById(R.id.textViewToDoText);
            imageViewPriority = itemView.findViewById(R.id.imageViewPriority);
            textViewDateTime = itemView.findViewById(R.id.textViewDateTime);
        }
    }

}