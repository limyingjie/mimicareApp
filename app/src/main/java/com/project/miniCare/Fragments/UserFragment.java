package com.project.miniCare.Fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.project.miniCare.MainActivity;
import com.project.miniCare.R;
import com.project.miniCare.Utils.SimpleToast;
import com.project.miniCare.Utils.UniversalImageLoader;

import java.util.Arrays;
import java.util.List;

public class UserFragment extends Fragment{
    private static final String TAG = "UserFragment";
    RecyclerView recyclerView;
    List<String> settings;
    ImageView profilePhoto;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user,container,false);
        profilePhoto = view.findViewById(R.id.profile_pic);
        initImageLoader();
        setProfileImage();
        settings = Arrays.asList(getResources().getStringArray(R.array.settings));

        recyclerView = view.findViewById(R.id.recycle_settings);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity()){
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(),linearLayoutManager.getOrientation()));
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(new settingsRecycleViewAdapter(settings));
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

    private void initImageLoader(){
        UniversalImageLoader universalImageLoader = new UniversalImageLoader(getActivity());
        ImageLoader.getInstance().init(universalImageLoader.getConfig());
    }
    private void setProfileImage(){
        Log.d(TAG, "setProfileImage: Called");
        String imageUrl = "https://static.independent.co.uk/s3fs-public/thumbnails/image/2019/01/31/14/panda-bamboo.jpg";
        UniversalImageLoader.setImage(imageUrl,profilePhoto,null,"");

    }
    // recycleView adapter
    class settingsRecycleViewAdapter extends RecyclerView.Adapter<settingsRecycleViewAdapter.ViewHolder>{
        List<String> msettings;

        public settingsRecycleViewAdapter(List<String> settings){
            this.msettings = settings;
        }

        @NonNull
        @Override
        public settingsRecycleViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(getActivity()).inflate(R.layout.snippet_profile_settings,viewGroup,false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull settingsRecycleViewAdapter.ViewHolder viewHolder, int i) {
            viewHolder.settingTextView.setText(msettings.get(i));
        }

        @Override
        public int getItemCount() {
            return msettings.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            TextView settingTextView;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                settingTextView = itemView.findViewById(R.id.setting_textView);
                settingTextView.setOnClickListener(this);
            }

            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: Called");
                SimpleToast.show(getActivity(),"Clicked :" + msettings.get(getAdapterPosition()),Toast.LENGTH_SHORT);
            }
        }
    }
}
