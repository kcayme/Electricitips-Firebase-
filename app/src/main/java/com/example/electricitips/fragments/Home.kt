package com.example.electricitips.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.electricitips.R

class Home :  Fragment(){
    override fun onCreateView(
        inflater: LayoutInflater, container:ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // inflate layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home, container, false)


        return view
    }

}