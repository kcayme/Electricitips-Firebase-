package com.example.electricitips.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.electricitips.R
import com.example.electricitips.databinding.FragmentLinksBinding

class Links :  Fragment(){
    override fun onCreateView(
        inflater: LayoutInflater, container:ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // inflate layout for this fragment
        val view = inflater.inflate(R.layout.fragment_links, container, false)


        return view
    }

}