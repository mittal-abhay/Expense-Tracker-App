package com.hcmus.group14.moneytor.ui.analysis;

import android.app.AlertDialog;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.GridView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.lifecycle.ViewModelProvider;

import com.hcmus.group14.moneytor.R;
import com.hcmus.group14.moneytor.data.model.FilterState;
import com.hcmus.group14.moneytor.data.model.Spending;
import com.hcmus.group14.moneytor.databinding.ActivityAnalysisBinding;
import com.hcmus.group14.moneytor.services.analyze.AnalyzeViewModel;
import com.hcmus.group14.moneytor.services.options.Category;
import com.hcmus.group14.moneytor.services.options.FilterViewModel;
import com.hcmus.group14.moneytor.ui.base.NoteBaseActivity;
import com.hcmus.group14.moneytor.utils.CategoriesUtils;
import com.hcmus.group14.moneytor.utils.FilterSelectUtils;
import com.hcmus.group14.moneytor.utils.InputUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AnalysisActivity extends NoteBaseActivity<ActivityAnalysisBinding> {

    private ActivityAnalysisBinding binding;
    private AnalyzeViewModel analyzeViewModel;
    // data
    private CategoryItemStatisticsAdapter categoryAdapter;
    private final FilterSelectUtils filterSelectUtils = new FilterSelectUtils(this);

    @Override
    public int getLayoutId() {
        return R.layout.activity_analysis;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setTitle(getString(R.string.toolbar_title_analysis));
        binding = getViewDataBinding();
        analyzeViewModel = new ViewModelProvider(this).get(AnalyzeViewModel.class);
        filterViewModel = new ViewModelProvider(this).get(FilterViewModel.class);
        binding.setViewModel(analyzeViewModel);
        // binding observe
        filterViewModel.getAllSpending().observe(this, this::updateNewData);
        // TODO: receive intent and show filter by FilterState()

        FilterState filterState = (FilterState)getIntent().getSerializableExtra("filter_state");
        if (filterState == null)
            filterState = new FilterState();
        filterViewModel.setFilterState(filterState);
        setCategoriesStatistics();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.analyze_visualize_menu, menu);
        MenuItem filterItem = menu.findItem(R.id.actionFilter);
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    void showDialog() {
        AlertDialog alertDialog = filterSelectUtils.createMainDialog();
        alertDialog.show();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.actionFilter) {
            showDialog();
        }
        return super.onOptionsItemSelected(item);
    }
    private void setCategoriesStatistics() {
        GridView gridView = binding.categoriesStatistics;
        final List<Category> categories = CategoriesUtils.getDefaultCategories();
        CategoryItemStatisticsAdapter adapter = new CategoryItemStatisticsAdapter(this,
                R.layout.category_item_statistics, categories);
        this.categoryAdapter = adapter;
        gridView.setAdapter(adapter);
    }

    // TODO: do everything when data change
    private void updateNewData(List<Spending> spendingList) {
        // update category statistics
        categoryAdapter.setItems(analyzeViewModel.getDetailsForCategories(spendingList));
        // update total amount
        binding.totalAmountAnalyze.setText(InputUtils.getCurrency(analyzeViewModel.getTotal(spendingList)));
        binding.averageByDateAnalyze.setText(InputUtils.getCurrency(analyzeViewModel.getAverage(spendingList)));
        binding.highestSpendingAnalyze.setText(InputUtils.getCurrency(analyzeViewModel.getMaxSpending(spendingList)));

        ArrayList<Category> highestCategory = analyzeViewModel.getMaxSpendingCategory(spendingList);
        if (!highestCategory.isEmpty()) {
            binding.highestCategoryIcon.setImageResource(highestCategory.get(0).getResourceId());
            binding.highestCategoryIcon.setBackgroundTintList(ColorStateList.valueOf(highestCategory.get(0).getColor()));
        }
    }

}