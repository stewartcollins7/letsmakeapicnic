package com.example.nunya.letsmakeapicnic

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.Html
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_about.*

/**
 * Created by Stewart Collins on 10/02/18.
 */
class AboutFragment : Fragment(){
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view: View = inflater!!.inflate(R.layout.fragment_about, container,
                false)
        return view
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)

    }

    override fun onStart() {
        super.onStart()
        val aboutTextHtml = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            Html.fromHtml(getString(R.string.aboutText),Html.FROM_HTML_MODE_LEGACY);
        } else {
            Html.fromHtml(getString(R.string.aboutText));
        }
        Log.v("About text:",aboutTextHtml.toString())
        aboutText.text = aboutTextHtml
        aboutText.movementMethod = LinkMovementMethod.getInstance()
    }

}