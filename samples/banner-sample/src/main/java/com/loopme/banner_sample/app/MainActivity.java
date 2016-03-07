package com.loopme.banner_sample.app;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private List<String> mData = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.main_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        initList();
        MainAdapter adapter = new MainAdapter(this, mData);
        recyclerView.setAdapter(adapter);
    }

    private void initList() {
        mData.add(Constants.SIMPLE);
        mData.add(Constants.LISTVIEW);
        mData.add(Constants.LISTVIEW_SHRINK);
        mData.add(Constants.RECYCLERVIEW);
        mData.add(Constants.RECYCLERVIEW_SHRINK);
    }
}
