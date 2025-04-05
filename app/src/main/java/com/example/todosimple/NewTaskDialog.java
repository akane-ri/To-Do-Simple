package com.example.todosimple;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class NewTaskDialog extends BottomSheetDialogFragment {

    public static final String TAG = "NewTaskDialog";

    private EditText editTextNewTask;
    private RelativeLayout buttonSaveTask;
    private TextView textViewDate;
    private TextView textViewTime;
    private TextView buttonSaveTaskText;
    private ImageView imageViewPriority;
    private Spinner spinnerCategories;

    private String date = "";
    private String time = "10:00";
    private Boolean priority = false;

    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference referenceTasks = firebaseDatabase.getReference("Tasks");

    private String currentUserId;
    private String idOwner;

    private CategoriesViewModel categoriesViewModel;

    private MutableLiveData<Category> category = new MutableLiveData<>();
    private ArrayAdapter<Category> adapter;
    private int amountCategoriesInAdapter = 1;

    private Task taskToUpdate; // Поле для хранения задачи, которую нужно обновить

    private boolean categoriesLoaded = false; // Флаг для отслеживания загрузки категорий

    public void setTaskToUpdate(Task task) { //установка задачи для обновления
        this.taskToUpdate = task;
    }

    public NewTaskDialog(String currentUserId, String todoOwner) { //конструктор
        this.currentUserId = currentUserId;
        this.idOwner = todoOwner;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_new_task, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);

        categoriesViewModel = new ViewModelProvider(this).get(CategoriesViewModel.class);

        setupOnClickListener();
        observeViewModel();

        if (taskToUpdate != null) {
            editTextNewTask.setText(taskToUpdate.getText());
            date = taskToUpdate.getDate();
            if (taskToUpdate.getPriority()) {
                changePriorityRed();
                priority = true;
            } else {
                changePriorityGrey();
                priority = false;
            }

            if (!Objects.equals(taskToUpdate.getDate(), "")) {
                SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");
                Date date = null;
                try {
                    date = dateTimeFormat.parse(taskToUpdate.getDate());
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

                assert date != null;
                String datePart = dateFormat.format(date); // Дата
                String timePart = timeFormat.format(date); // Время

                Log.d("Date", datePart);
                Log.d("Time", timePart);

                textViewDate.setText(datePart);
                textViewTime.setVisibility(View.VISIBLE);
                textViewTime.setText(timePart);
            }

            buttonSaveTaskText.setText("Обновить");
            buttonSaveTask.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateTaskInDatabase();
                    dismiss();
                }
            });

        } else {
            buttonSaveTask.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (!Objects.equals(date, "")) {
                        date = date + " " + time;
                    }

                    String taskId = referenceTasks.child(idOwner).push().getKey();

                    Task task = new Task(
                            taskId,
                            editTextNewTask.getText().toString().trim(),
                            priority,
                            false,
                            date,
                            spinnerCategories.getSelectedItem().toString(),
                            idOwner,
                            1
                    );
                    assert taskId != null;
                    referenceTasks.child(task.getIdOwner()).child(taskId).setValue(task);
                    Bundle resultBundle = new Bundle();
                    getParentFragmentManager().setFragmentResult("REQUEST_KEY_NEW_TASK", resultBundle);
                    dismiss();
                }
            });
        }
    }

    private void observeViewModel() {
        categoriesViewModel.getCategories().observe(this, new Observer<List<Category>>() {
            @Override
            public void onChanged(List<Category> categories) {
                adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, categories);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCategories.setAdapter(adapter);

                categoriesLoaded = true; // Категории загружены

                if (taskToUpdate != null) {
                    String taskCategory = taskToUpdate.getCategory();
                    for (int i = 0; i < adapter.getCount(); i++) {
                        if (adapter.getItem(i).toString().equals(taskCategory)) {
                            spinnerCategories.setSelection(i);
                            break;
                        }
                    }
                }
            }
        });
    }

    private void setupOnClickListener() {
        editTextNewTask.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                buttonSaveTask.setVisibility(View.VISIBLE);
                if (s.toString().equals("")) {
                    buttonSaveTask.setEnabled(false);
                    buttonSaveTask.setVisibility(View.GONE);
                } else {
                    buttonSaveTask.setEnabled(true);
                    buttonSaveTask.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        textViewDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                int month = calendar.get(Calendar.MONTH);
                int year = calendar.get(Calendar.YEAR);
                int day = calendar.get(Calendar.DATE);

                DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        month = month + 1;
                        String formattedDate = String.format("%02d.%02d.%04d", dayOfMonth, month, year);
                        textViewDate.setText(formattedDate);
                        date = formattedDate;
                        textViewTime.setVisibility(View.VISIBLE);
                    }
                }, year, month, day);

                datePickerDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        textViewTime.setVisibility(View.GONE);
                    }
                });

                datePickerDialog.show();
            }
        });

        textViewTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);

                TimePickerDialog timePickerDialog = new TimePickerDialog(
                        requireContext(),
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                textViewTime.setText(String.format("%02d:%02d", hourOfDay, minute));
                                time = String.format("%02d:%02d", hourOfDay, minute);
                            }
                        },
                        hour, minute, true);

                timePickerDialog.show();
            }
        });

        imageViewPriority.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imageViewPriority.getTag().equals("gray")) {
                    changePriorityRed();
                    priority = true;
                } else {
                    changePriorityGrey();
                    priority = false;
                }
            }
        });
    }

    private void changePriorityRed() {
        imageViewPriority.setImageResource(R.drawable.icon_red_priority);
        imageViewPriority.setTag("red");
    }

    private void changePriorityGrey() {
        imageViewPriority.setImageResource(R.drawable.icon_gray_priority);
        imageViewPriority.setTag("gray");
    }

    private void updateTaskInDatabase() { //обновление задачи
        if (taskToUpdate != null) {
            String newText = editTextNewTask.getText().toString().trim();
            String newDate = "";
            if (!Objects.equals(date, "")) {
                newDate = textViewDate.getText().toString() + " " + textViewTime.getText().toString();
            }
            String newCategory = spinnerCategories.getSelectedItem().toString();
            taskToUpdate.setText(newText);
            taskToUpdate.setDate(newDate);
            taskToUpdate.setCategory(newCategory);
            taskToUpdate.setPriority(priority);
            referenceTasks
                    .child(taskToUpdate.getIdOwner())
                    .child(taskToUpdate.getId())
                    .setValue(taskToUpdate);
        }
    }

    private void initViews(View view) {
        editTextNewTask = view.findViewById(R.id.editTextNewTask);
        buttonSaveTask = view.findViewById(R.id.buttonSaveTask);
        spinnerCategories = view.findViewById(R.id.spinnerCategories);
        textViewDate = view.findViewById(R.id.textViewDate);
        textViewTime = view.findViewById(R.id.textViewTime);
        buttonSaveTaskText = view.findViewById(R.id.buttonSaveTaskText);
        imageViewPriority = view.findViewById(R.id.imageViewPriority);
    }

    public static NewTaskDialog newInstance(String currentUserId, String todoOwner) {
        return new NewTaskDialog(currentUserId, todoOwner);
    }
}
