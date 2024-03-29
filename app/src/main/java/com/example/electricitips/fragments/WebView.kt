package com.example.electricitips.fragments

import android.os.Bundle
import android.view.KeyEvent.*
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.navigation.fragment.navArgs
import com.example.electricitips.R
import com.example.electricitips.databinding.FragmentWebViewBinding

class WebView :  Fragment(R.layout.fragment_web_view){
    private val args: WebViewArgs by navArgs()
    private var binding: FragmentWebViewBinding? = null
    private var webView: WebView? = null

    override fun onCreateView(
        inflater: LayoutInflater, container:ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // inflate layout for this fragment
        binding = FragmentWebViewBinding.inflate(inflater, container, false)

        return binding!!.root
    }

    override fun onViewCreated (view:View, savedInstanceState: Bundle? ) {
        super.onViewCreated(view, savedInstanceState)

        webView = binding!!.webView
        webView!!.webViewClient = WebViewClient()

        val webSettings = binding!!.webView.settings
        webSettings.javaScriptEnabled = true

        binding!!.webView.loadUrl(args.url)

        binding!!.webView.setOnKeyListener { _, i, keyEvent ->
            // check if back button is pressed and has been pressed down
            if (i == KEYCODE_BACK && keyEvent.action == ACTION_DOWN){
                if(binding!!.webView.canGoBack()) {
                    // do webview go back
                    binding!!.webView.goBack()
                }
                // else do default main activity on back pressed
                else{
                    requireActivity().onBackPressed()
                }
            }
            true
        }

    }

    // binding must be set to null on fragment destroy to prevent memory leaks
    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}