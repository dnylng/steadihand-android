package com.dnylng.steadihand.features.foryou

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.dnylng.steadihand.R
import com.dnylng.steadihand.features.pdfreader.PdfReaderFragment
import com.google.android.material.button.MaterialButton

class ForYouFragment : Fragment() {

    companion object {
        private const val KEY = "FragmentKey"
        fun newInstance(key: String): Fragment {
            val fragment = ForYouFragment()
            val arguments = Bundle()
            arguments.putString(KEY, key)
            fragment.arguments = arguments
            return fragment
        }
    }

    private lateinit var readBtn: MaterialButton

    private val readBtnListener = View.OnClickListener {
        val key = "PDF Reader"
        fragmentManager
            ?.beginTransaction()
            ?.replace(R.id.fragment_container, PdfReaderFragment.newInstance(key), key)
            ?.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            ?.addToBackStack(key)
            ?.commit()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_foryou, container, false)
        readBtn = view.findViewById<MaterialButton>(R.id.read_btn).also { it.setOnClickListener(readBtnListener) }
        return view
    }
}