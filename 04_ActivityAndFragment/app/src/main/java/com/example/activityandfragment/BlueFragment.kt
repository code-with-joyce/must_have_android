package com.example.activityandfragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
/**
 * @author Joyce Hong
 * @email joycehong0524@gmail.com
 * @created 2021/09/14
 * @desc
 */

class BlueFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_blue, container, false);
    }
}
