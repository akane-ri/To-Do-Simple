package com.example.todosimple;

import static com.example.todosimple.NewCategoryDialog.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.google.firebase.auth.FirebaseUser;

import java.util.List;
import java.util.Locale;

public class ToDoListActivity extends AppCompatActivity {

    private static final String EXTRA_CURRENT_USER_ID = "current_id";

    private View hintLayout;

    private ImageView buttonTools;
    private ImageView buttonCategories;
    private ImageView buttonAddTask;
    private ProgressBar progressBarRecyclers;

    private String currentUserId;
    private String todoOwnerId;

    private ToDoListViewModel viewModel;
    private CategoriesViewModel viewModelCategories;

    private TasksAdapter tasksAdapter;
    private CategoriesAdapter categoriesAdapter;
    private RecyclerView recyclerViewTasks;
    private RecyclerView recyclerViewCategories;

    private Category currentSelectedCategory;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.Base_Theme_ToDoSimple);
        SharedPreferences preferences = getSharedPreferences("Settings", MODE_PRIVATE);
        String language = preferences.getString("App_Language", "ru");
        setLocale(language);
        
        setContentView(R.layout.activity_to_do_list);
        initViews();
        progressBarRecyclers.setVisibility(View.VISIBLE);

        currentUserId = getIntent().getStringExtra(EXTRA_CURRENT_USER_ID);

        viewModel = new ViewModelProvider(this).get(ToDoListViewModel.class);
        viewModelCategories = new ViewModelProvider(this).get(CategoriesViewModel.class);

        tasksAdapter = new TasksAdapter();
        recyclerViewTasks.setAdapter(tasksAdapter);
        categoriesAdapter = new CategoriesAdapter();
        recyclerViewCategories.setAdapter(categoriesAdapter);

        observeViewModel();
        setupClickListeners();
        setupItemTouchHelper();

        hintLayout = findViewById(R.id.hintLayout);

/*        if (!PreferenceManager.isHintShown(this)) {
            hintLayout.setVisibility(View.VISIBLE);
            hintLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    hintLayout.setVisibility(View.GONE);
                    PreferenceManager.setHintShown(ToDoListActivity.this, true);
                }
            });
        } else {
            hintLayout.setVisibility(View.GONE);
        }*/

    }

    private void setLocale(String language) {
        Locale newLocale = new Locale(language);
        Locale.setDefault(newLocale);
        Configuration configuration = new Configuration();
        configuration.setLocale(newLocale);
        Resources resources = getResources();
        resources.updateConfiguration(configuration, resources.getDisplayMetrics());
    }

    private void setupClickListeners() {

        tasksAdapter.setOnTaskClickListener(new TasksAdapter.OnTaskClickListener() {
            @Override
            public void onTaskClick(Task task) {
                NewTaskDialog newTaskDialog = NewTaskDialog.newInstance(currentUserId, todoOwnerId);
                newTaskDialog.setTaskToUpdate(task);
                newTaskDialog.show(getSupportFragmentManager(), NewTaskDialog.TAG);
            }
        });

        categoriesAdapter.setOnCategoryClickListener(new CategoriesAdapter.OnCategoryClickListener() {
            @Override
            public void onCategoryClick(Category category) {
                tasksAdapter.setSelectedCategory(category);
                currentSelectedCategory = category;
            }

            @Override
            public void onCategoryLongClick(Category category) {
                NewCategoryDialog newCategoryDialog = NewCategoryDialog.newInstance(currentUserId, todoOwnerId);
                newCategoryDialog.setCategoryToUpdate(category);
                newCategoryDialog.show(getSupportFragmentManager(), TAG);
            }
        });

        buttonAddTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NewTaskDialog dialog = NewTaskDialog.newInstance(currentUserId, todoOwnerId);
                dialog.show(getSupportFragmentManager(), NewTaskDialog.TAG);
            }
        });

        buttonCategories.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NewCategoryDialog.newInstance(currentUserId, todoOwnerId).show(getSupportFragmentManager(), TAG);
            }
        });

        buttonTools.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SettingsDialog settingsDialog = SettingsDialog.newInstance(currentUserId);
                settingsDialog.show(getSupportFragmentManager(), "SettingsDialog");
            }
        });

    }

    private void setupItemTouchHelper() {
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.START | ItemTouchHelper.END, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                int fromPosition = viewHolder.getAdapterPosition();
                int toPosition = target.getAdapterPosition();

                viewModel.moveItem(fromPosition, toPosition);
                tasksAdapter.notifyItemMoved(fromPosition, toPosition);
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Task task = tasksAdapter.getTasks(position);
                viewModel.deleteTask(task.getId(), todoOwnerId);

                tasksAdapter.notifyItemRemoved(position);
            }
        });

        itemTouchHelper.attachToRecyclerView(recyclerViewTasks);
    }

    private void observeViewModel() {

        viewModel.getTasks().observe(this, new Observer<List<Task>>() {
            @Override
            public void onChanged(List<Task> tasks) {
                tasksAdapter.setTasks(tasks);
                if(currentSelectedCategory != null) {
                    tasksAdapter.setSelectedCategory(currentSelectedCategory);
                }
                progressBarRecyclers.setVisibility(View.GONE);
            }
        });

        viewModelCategories.getCategories().observe(this, new Observer<List<Category>>() {
            @Override
            public void onChanged(List<Category> categories) {
                categoriesAdapter.setCategories(categories);
            }
        });

        viewModel.getUser().observe(this, new Observer<FirebaseUser>() {
            @Override
            public void onChanged(FirebaseUser firebaseUser) {
                if (firebaseUser == null) {
                    Intent intent = LoginActivity.newIntent(ToDoListActivity.this);
                    startActivity(intent);
                    finish();
                }
            }
        });

        viewModel.getTodoList().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String todoList) {
                progressBarRecyclers.setVisibility(View.VISIBLE);
                todoOwnerId = todoList;
                viewModel.loadTasksFromDB();
                viewModelCategories.loadCategoriesFromDB();
                tasksAdapter.notifyDataSetChanged();
            }
        });

    }

    private void initViews() {
        recyclerViewTasks = findViewById(R.id.recyclerViewTasks);
        recyclerViewCategories = findViewById(R.id.recyclerViewCategories);
        buttonTools = findViewById(R.id.buttonTools);
        buttonAddTask = findViewById(R.id.buttonAddTask);
        buttonCategories = findViewById(R.id.buttonCategories);
        progressBarRecyclers = findViewById(R.id.progressBarRecyclers);
    }

    public static Intent newIntent(Context context, String currentUserId) {
        Intent intent = new Intent(context, ToDoListActivity.class);
        intent.putExtra(EXTRA_CURRENT_USER_ID, currentUserId);
        return intent;
    }

}