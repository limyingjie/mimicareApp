package com.project.mimiCare;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.project.mimiCare.Data.Assignment;

import java.util.ArrayList;

public class AssignmentSummaryActivity extends AppCompatActivity {
    Button go_back;
    TextView target,pt,g,p;
    HorizontalBarChart horizontalBarChart;
    Assignment assignment;
    int position;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.snippet_assignment_summary);
        addTransparentStatusBar();
        // intent
        Intent intent = getIntent();
        if (intent!=null){
            assignment = intent.getParcelableExtra("assignment");
            position = intent.getIntExtra("position",-1);
        }
        else{
            finish();
        }
        // UI
        horizontalBarChart = findViewById(R.id.result_barChart);
        go_back = findViewById(R.id.go_back);
        target = findViewById(R.id.step);
        pt = findViewById(R.id.perfect);
        g = findViewById(R.id.good);
        p = findViewById(R.id.poor);
        initProgressBar();

        // set the goal and the result
        target.setText(Integer.toString(assignment.getTarget()));
        pt.setText(Integer.toString(assignment.getPerfect()));
        g.setText(Integer.toString(assignment.getGood()));
        p.setText(Integer.toString(assignment.getPoor()));

        go_back.setOnClickListener((View v)->{
            onBackPressed();
        });
    }

    private void initProgressBar(){
        ArrayList<BarEntry> barEntries = setGraphData();
        BarDataSet barDataSet = new BarDataSet(barEntries,"Result");

        BarData barData = new BarData(barDataSet);
        barData.setDrawValues(false);

        // Adjust the UI of barChart
        horizontalBarChart.setDrawBarShadow(false);
        horizontalBarChart.setDrawGridBackground(false);
        horizontalBarChart.getDescription().setEnabled(false);
        horizontalBarChart.getLegend().setEnabled(false);
        // remove top and right axis and remove the grid
        // y axis should always start from zero
        YAxis yl = horizontalBarChart.getAxisLeft();
        yl.setEnabled(false);
        yl.setDrawAxisLine(false);
        YAxis yr = horizontalBarChart.getAxisRight();
        yr.setDrawGridLines(false);
        yr.setAxisMinimum(0f);
        XAxis xAxis = horizontalBarChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        // xAxis Label
        String[] label = {"Target","Perfect","Good","Poor"};
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                try{
                    return label[Math.round(value-1)];
                }
                catch (Exception e){
                    return "";
                }

            }
        });
        xAxis.setGranularity(1);
        xAxis.setAxisMinimum(0f);
        // disable zoom
        horizontalBarChart.setTouchEnabled(false);
        horizontalBarChart.setData(barData);
        horizontalBarChart.invalidate();
    }

    private ArrayList<BarEntry> setGraphData(){
        ArrayList<BarEntry> barEntries = new ArrayList<>();
        // add target,perfect,good,poor
        barEntries.add(new BarEntry(1,assignment.getTarget()));
        barEntries.add(new BarEntry(2,assignment.getPerfect()));
        barEntries.add(new BarEntry(3,assignment.getGood()));
        barEntries.add(new BarEntry(4,assignment.getPoor()));
        return barEntries;
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }

    // Util
    private void addTransparentStatusBar(){
        //make translucent statusBar on kitkat devices
        if (Build.VERSION.SDK_INT >= 19 && Build.VERSION.SDK_INT < 21) {
            setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, true);
        }
        if (Build.VERSION.SDK_INT >= 19) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
        //make fully Android Transparent Status bar
        if (Build.VERSION.SDK_INT >= 21) {
            setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
    }

    // possibly can be made into its own class
    public static void setWindowFlag(Activity activity, final int bits, boolean on) {
        Window win = activity.getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }

}
