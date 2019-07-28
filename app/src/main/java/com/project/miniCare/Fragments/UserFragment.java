package com.project.miniCare.Fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.project.miniCare.MainActivity;
import com.project.miniCare.R;
import com.project.miniCare.Utils.SimpleToast;
import com.project.miniCare.Utils.UniversalImageLoader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class UserFragment extends Fragment{
    private static final String TAG = "UserFragment";
    private BarChart setting_barChart;
    private RecyclerView recyclerView;
    private List<SettingClass> settings;
    private List<StepData> progress_data;
    private ImageView profilePhoto;
    private TextView barChart_current_textView;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user,container,false);

        // init UI
        profilePhoto = view.findViewById(R.id.profile_pic);
        setting_barChart = view.findViewById(R.id.setting_barChart_progress);
        barChart_current_textView = view.findViewById(R.id.setting_barChart_current);

        initImageLoader();
        initProgressBar();
        initSetting();
        setProfileImage();

        recyclerView = view.findViewById(R.id.recycle_settings);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity()){
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        //recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(),linearLayoutManager.getOrientation()));
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(new settingsRecycleViewAdapter(settings));

        // UI of progress
        TextView average_step = view.findViewById(R.id.progressBar_stepAverage_textView);
        TextView personal_best = view.findViewById(R.id.progressBar_personalBest_textView);
        TextView total_step = view.findViewById(R.id.progressBar_totalStep_textView);
        ListStepData listStepData = new ListStepData(progress_data);
        average_step.setText(Integer.toString(listStepData.getAverageStep()));
        personal_best.setText(Integer.toString(listStepData.getPersonalBest()));
        total_step.setText(Integer.toString(listStepData.getTotalStep()));
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity)getActivity()).getSupportActionBar().hide();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((MainActivity)getActivity()).changeTitle(R.string.user);
    }

    @Override
    public void onStop() {
        super.onStop();
        ((MainActivity)getActivity()).getSupportActionBar().show();
    }

    private void initSetting(){
        settings = new ArrayList<>();
        List<String> title = Arrays.asList(getResources().getStringArray(R.array.settingTitle));
        List<String> value = Arrays.asList(getResources().getStringArray(R.array.settingValue));

        for (int i = 0; i < title.size();i++){
            settings.add(new SettingClass(title.get(i),value.get(i)));
        }
    }

    private void initImageLoader(){
        UniversalImageLoader universalImageLoader = new UniversalImageLoader(getActivity());
        ImageLoader.getInstance().init(universalImageLoader.getConfig());
    }

    private void initStepData(){
        progress_data = new ArrayList<>();
        Random random = new Random();

        for (int i = 1; i < 20; i++){
            progress_data.add(new StepData(i,random.nextInt(50)));
        }
    }
    private void initProgressBar(){
        initStepData();

        ArrayList<BarEntry> barEntries = new ArrayList<>();
        for (int i = 0; i < progress_data.size(); i++){
            barEntries.add(new BarEntry(progress_data.get(i).day,progress_data.get(i).step));
        }
        BarDataSet barDataSet = new BarDataSet(barEntries,"Step");

        BarData barData = new BarData(barDataSet);
        barData.setDrawValues(false);

        setting_barChart.setData(barData);

        // Adjust the UI of barChart
        setting_barChart.setDrawBarShadow(false);
        setting_barChart.setDrawValueAboveBar(true);
        setting_barChart.setDrawGridBackground(false);
        setting_barChart.getDescription().setEnabled(false);
        setting_barChart.getLegend().setEnabled(false);
        // remove top and right axis and remove the grid
        // y axis should always start from zero
        YAxis yr = setting_barChart.getAxisRight();
        yr.setEnabled(false);
        yr.setDrawAxisLine(false);
        YAxis yl = setting_barChart.getAxisLeft();
        yl.setDrawGridLines(false);
        yl.setAxisMinimum(0f);
        XAxis xAxis = setting_barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        // disable zoom, set only 7 data is visible, and allow drag
        setting_barChart.setVisibleXRangeMaximum(7);
        setting_barChart.setScaleEnabled(false);


        // listener for onHighlighted and show the highlighted value
        setting_barChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                barChart_current_textView.setText(String.format("Steps: %d",Math.round(e.getY())));
            }

            @Override
            public void onNothingSelected() {
                barChart_current_textView.setText("");
            }
        });
        setting_barChart.invalidate();
    }
    private void setProfileImage(){
        Log.d(TAG, "setProfileImage: Called");
        String imageUrl = "https://static.independent.co.uk/s3fs-public/thumbnails/image/2019/01/31/14/panda-bamboo.jpg";
        UniversalImageLoader.setImage(imageUrl,profilePhoto,null,"");

    }

    // Data
    // Too lazy to use getter and setter
    class SettingClass {
        public String title;
        public String value;

        public SettingClass(String title, String value) {
            this.title = title;
            this.value = value;
        }
    }

    class StepData {
        public int day;
        public int step;

        public StepData(int day, int step) {
            this.day = day;
            this.step = step;
        }
    }

    class ListStepData{
        private List<StepData> listStepData;
        private ArrayList<Integer> step;
        private int total;
        private int highest;
        public ListStepData(List<StepData> listStepData){
            this.listStepData = listStepData;
            step = new ArrayList<>();
            total = 0;
            highest = 0;
        }

        private void setUpStep(){
            if (step==null){
                for (StepData stepData: listStepData){
                    step.add(stepData.step);
                    total+=stepData.step;
                    if (stepData.step>highest){
                        highest = stepData.step;
                    }
                }
            }
            if (step.size()!= listStepData.size()){
                step.clear();
                total = 0;
                highest = 0;
                for (StepData stepData: listStepData){
                    step.add(stepData.step);
                    total+=stepData.step;
                    if (stepData.step>highest){
                        highest = stepData.step;
                    }
                }
            }
            return;
        }
        public int getAverageStep(){
            setUpStep();
            return total/step.size();
        }

        public int getPersonalBest(){
            setUpStep();
            return highest;
        }

        public int getTotalStep(){
            setUpStep();
            return total;
        }
    }


    // general recycleView adapter
    class settingsRecycleViewAdapter extends RecyclerView.Adapter<settingsRecycleViewAdapter.ViewHolder>{
        List<SettingClass> mSettings;

        public settingsRecycleViewAdapter(List<SettingClass> settings){
            this.mSettings = settings;
        }

        @NonNull
        @Override
        public settingsRecycleViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(getActivity()).inflate(R.layout.snippet_profile_settings,viewGroup,false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull settingsRecycleViewAdapter.ViewHolder viewHolder, int i) {
            viewHolder.settingTitleTextView.setText(mSettings.get(i).title);
            viewHolder.settingValueTextView.setText(mSettings.get(i).value);
        }

        @Override
        public int getItemCount() {
            return mSettings.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            RelativeLayout parent;
            TextView settingTitleTextView;
            TextView settingValueTextView;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                settingTitleTextView = itemView.findViewById(R.id.setting_title_textView);
                settingValueTextView = itemView.findViewById(R.id.setting_value_textView);
                parent = itemView.findViewById(R.id.setting_parent);
                parent.setOnClickListener(this);
            }

            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: Called");
                SimpleToast.show(getActivity(),"Clicked :" + mSettings.get(getAdapterPosition()).value,Toast.LENGTH_SHORT);
            }
        }
    }
}
