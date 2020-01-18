package com.restaurantfinder;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private List<String> myDatas = new ArrayList<String>();
    private MyAdapter myAdapter;
    private Button addButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        Intent intent = new Intent(MainActivity.this, MapsActivity.class);
//        startActivity(intent);
        initView();
        initRecycle();
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myAdapter.addItem(myDatas.size());
            }
        });
    }

    protected ArrayList<String> initData(){
        ArrayList<String> myDatas = new ArrayList<String>();
        for(int i=0;i<1;i++){
            myDatas.add("this is person"+ (i+1));
        }
        return myDatas;

    }

    private void initView(){
        addButton = (Button) findViewById(R.id.my_recycler_view);
        recyclerView = (RecyclerView)findViewById(R.id.my_recycler_view);

    }

    private void initRecycle(){
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        myDatas = initData();
        myAdapter = new MyAdapter(MainActivity.this,myDatas);
        recyclerView.setAdapter(myAdapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

    }
}
