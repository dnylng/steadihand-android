package com.dnylng.steadihand.features.me

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.dnylng.steadihand.R

class MeFragment : Fragment() {

    companion object {
        fun newInstance(): Fragment {
            return MeFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_me, container, false)
        return view
    }
}