package com.liugs.recycleviewtest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setAdapter(new Adapter() {
            @Override
            public View onCreateViewHolder(int position, View convertView, ViewGroup parent) {
                convertView = getLayoutInflater().inflate(R.layout.item, parent, false);
                TextView textView = convertView.findViewById(R.id.textView);
                textView.setText("item ----- " + position);
                return convertView;
            }

            @Override
            public View onBinderViewHolder(int position, View convertView, ViewGroup parent) {
                TextView textView = convertView.findViewById(R.id.textView);
                textView.setText("item ----- " + position);
                return convertView;
            }

            @Override
            public int getItemViewType(int row) {
                return 0;
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public int getCount() {
                return 40;
            }

            @Override
            public int getHeight(int position) {
                return 100;
            }
        });
    }
}
