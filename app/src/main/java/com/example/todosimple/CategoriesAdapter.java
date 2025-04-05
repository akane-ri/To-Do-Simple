package com.example.todosimple;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class CategoriesAdapter extends RecyclerView.Adapter<CategoriesAdapter.CategoryViewHolder> {

    private OnCategoryClickListener onCategoryClickListener;
    private int selectedPosition = RecyclerView.NO_POSITION;
    private List<Category> categories = new ArrayList<>();

    public void setCategories(List<Category> categories) {
        this.categories = categories;
        // Установите позицию selectedPosition на "All"
        for (int i = 0; i < categories.size(); i++) {
            if (categories.get(i).getName().equals("All")) {
                selectedPosition = i;
                break;
            }
        }
        notifyDataSetChanged();
    }

    public void setOnCategoryClickListener(OnCategoryClickListener onCategoryClickListener) {
        this.onCategoryClickListener = onCategoryClickListener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view;
        if (viewType == 1) {
            view = inflater.inflate(R.layout.category_item, parent, false);
        } else {
            view = inflater.inflate(R.layout.category_item_noactive, parent, false);
        }
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categories.get(position);
        holder.textViewCategory.setText(category.getName());

        // Устанавливаем фон для выбранного элемента
        holder.itemView.setSelected(position == selectedPosition);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onCategoryClickListener != null) {
                    selectedPosition = holder.getAdapterPosition();
                    onCategoryClickListener.onCategoryClick(category);
                    notifyDataSetChanged();
                }
            }
        });

        // Запрещаем долгое нажатие для категории "All"
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (category.getName().equals("All")) {
                    return false; // Возвращаем false, чтобы долгого нажатия не было
                }

                if (onCategoryClickListener != null) {
                    onCategoryClickListener.onCategoryLongClick(category);
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public int getItemViewType(int position) {
        return position == selectedPosition ? 1 : 0;
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public interface OnCategoryClickListener {
        void onCategoryClick(Category category);
        void onCategoryLongClick(Category category);
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewCategory;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewCategory = itemView.findViewById(R.id.textViewCategory);
        }
    }
}