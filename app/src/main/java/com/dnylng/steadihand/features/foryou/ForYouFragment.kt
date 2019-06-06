package com.dnylng.steadihand.features.foryou

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.dnylng.steadihand.R
import com.google.android.material.button.MaterialButton

class ForYouFragment : Fragment() {

    companion object {
        fun newInstance(): Fragment {
            return ForYouFragment()
        }
    }

    private lateinit var readBtn: MaterialButton

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_foryou, container, false)
        readBtn = view.findViewById<MaterialButton>(R.id.read_btn).also { it.setOnClickListener { findNavController().navigate(R.id.action_foryou_to_pdfreader) } }
        return view
    }
}