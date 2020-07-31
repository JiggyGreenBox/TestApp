package com.example.testapp;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class HistoryFragment extends Fragment {

    // Add RecyclerView member
    private RecyclerView recyclerView;

    public HistoryFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        // Add the following lines to create RecyclerView
        recyclerView = view.findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);
//        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
//        recyclerView.setLayoutManager(new CustomGridLayoutManager(view.getContext()));
        recyclerView.setAdapter(new RandomNumListAdapter(1234));


        return view;
    }

//    public class CustomGridLayoutManager extends LinearLayoutManager {
//
//        public CustomGridLayoutManager(Context context) {
//            super(context);
//        }
//
//
//        @Override
//        public boolean canScrollVertically() {
//            return false;
//        }
//    }
}