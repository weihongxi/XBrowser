package com.weihongxi.xbrowser

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.*
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ArrayAdapter
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.ref.WeakReference
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.concurrent.schedule

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private val TAG = "MainActivity"

    private val MSG_WHAT = 0x01
    private val HOME_URL = "http://m.baidu.com"

    private val booksMark = arrayOf("www.baidu.com", "www.google.com", "www.aust.edu.cn")

    private var exit: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d(TAG, "onCreate")

        initView()
        initButtons()
        changeButtonState()

    }

    override fun onClick(p0: View?) {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        Log.d(TAG, "id: before")
        val id = p0?.id
        Log.d(TAG, "id: $id")
        when (id) {
            R.id.btnGo -> {
                hideSoftInput()
                loadUrlAndShow()
            }

            R.id.btnBack -> {
                hideSoftInput()
                wvShow.goBack()
            }

            R.id.btnForward -> {
                hideSoftInput()
                wvShow.goForward()
            }

            R.id.btnRefresh -> {
                hideSoftInput()
//                val url = atvUrl.text.toString()
//                wvShow.loadUrl(url)
                loadUrlAndShow()
            }

            R.id.btnHome -> {
                hideSoftInput()
                wvShow.loadUrl(HOME_URL)
            }

            else -> {
                Log.d(TAG, "onclick else")

            }
        }


    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        Log.d(TAG, "onKeyDown")
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            doubleClickExit()
        }
        return false
    }

    private fun initView() {
        wvShow.settings.javaScriptEnabled = true
        wvShow.settings.builtInZoomControls = true
        wvShow.settings.displayZoomControls = false
//        wvShow.setWebViewClient(WebViewClient())
        if (Intent.ACTION_VIEW == intent.action) {
            wvShow.loadUrl(intent.dataString)
        } else {
            wvShow.loadUrl(HOME_URL)
        }
        wvShow.webViewClient = object : WebViewClient() {

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                atvUrl.setText(url)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                hideOperation(true)
                super.onPageFinished(view, url)
            }

            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {

                Toast.makeText(applicationContext,
                        //request.toString() + error.toString(),
                        R.string.webviewError,
                        Toast.LENGTH_SHORT).show()
            }
        }
        gestureListener(wvShow)

        val aa = ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, booksMark)
        atvUrl.setAdapter(aa)
        atvUrl.setOnKeyListener({ _, keyCode, _ ->
            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                hideSoftInput()
                loadUrlAndShow()
                return@setOnKeyListener true
            }

            return@setOnKeyListener false
        })

    }

    private fun initButtons() {
        btnGo.setOnClickListener(this)
        btnBack.setOnClickListener(this)
        btnForward.setOnClickListener(this)
        btnRefresh.setOnClickListener(this)
        btnHome.setOnClickListener(this)
    }

    private fun changeButtonState() {

        val handler = object : Handler() {
            override fun handleMessage(msg: Message?) {
                if (msg?.what == MSG_WHAT) {
                    /*Log.d(TAG,"handler msg")

                    Log.d(TAG, "can back: " + wvShow.canGoBack()
                            +", can forward: " + wvShow.canGoForward())*/

                    if (wvShow.canGoBack()) {
                        btnBack.isEnabled = true
                    } else {
                        btnBack.isEnabled = false
                    }

                    if (wvShow.canGoForward()) {
                        btnForward.isEnabled = true
                    } else {
                        btnForward.isEnabled = false
                    }
                }

                super.handleMessage(msg)
            }

        }

        Timer().schedule(0, 1000) {
            val msg = Message()
            msg.what = MSG_WHAT
            handler.sendMessage(msg)
//            Log.d(TAG, "send message")

        }
    }

    private fun hideSoftInput() {
        if (currentFocus == null) {
            return
        }
        val view: View = currentFocus
        val token: IBinder = view.windowToken
        if (token != null) {
            val manager: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            manager.hideSoftInputFromWindow(token, InputMethodManager.HIDE_NOT_ALWAYS)
        }

    }

    private fun loadUrlAndShow() {
        var url = atvUrl.text.toString()
        val urlRule = ("^((https?|ftp|news):\\/\\/)?([a-z]([a-z0-9\\-]*[\\.。])"
                + "+([a-z]{2}|aero|arpa|biz|com|coop|edu|gov|info|int|jobs|mil|museum|name|nato|net|org|pro|travel)"
                + "|(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5]))"
                + "(\\/[a-z0-9_\\-\\.~]+)*(\\/([a-z0-9_\\-\\.]*)(\\?[a-z0-9+_\\-\\.%=&]*)?)?(#[a-z][a-z0-9_]*)?$")
        val p: Pattern = Pattern.compile(urlRule)
        val m: Matcher = p.matcher(url)
        if (m.find()) {
            if (url.substring(0, 4) != "http") {
                url = "http://" + url
                Log.d(TAG, "url: $url")
            } else {
                Toast.makeText(applicationContext, R.string.urlError, Toast.LENGTH_LONG).show()
            }
            Log.d(TAG, "url2: $url")
            wvShow.loadUrl(url)
        }
    }

    private fun doubleClickExit() {

        Log.d(TAG, "doubleClickExit: in")
        if (exit) {
            exit = false
            Toast.makeText(applicationContext, R.string.doubleClickExit, Toast.LENGTH_SHORT).show()
            Timer().schedule(1000) {
                exit = true
            }
        } else {
            finish()
            System.exit(0)
        }
    }

    private fun gestureListener(view: View) {
        var downPosX = 0f
        var downPosY = 0f
        var curPosX = 0f
        var curPosY = 0f
        view.setOnTouchListener({ v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    downPosX = event.x
                    downPosY = event.y
                }
                MotionEvent.ACTION_MOVE -> {
                    curPosX = event.x
                    curPosY = event.y
                }
                MotionEvent.ACTION_UP -> {
                    if (Math.abs(curPosY - downPosY) > 20) {

                        if (curPosY > downPosY) {//move down
                            hideOperation(false)

                        } else {//move up
                            hideOperation(true)

                        }
                    }
                }
            }
            return@setOnTouchListener false
        })
    }

    private fun hideOperation(hide: Boolean) {
        if (hide) {
            rlTopNav.visibility = View.GONE
            llBtnGroup.visibility = View.GONE
        } else {
            rlTopNav.visibility = View.VISIBLE
            llBtnGroup.visibility = View.VISIBLE
        }

    }

/*    private class MyHandler(activity: MainActivity) : Handler(Looper.getMainLooper()) {
        private val mActivity: WeakReference<MainActivity> = WeakReference(activity)

        override fun handleMessage(msg: Message?) {

            if (mActivity.get() == null) {
                return
            }

            val activity = mActivity.get()

            if (msg?.what == 0x02) {

            }

            super.handleMessage(msg)
        }
    }*/

    override fun onDestroy() {
        Log.d(TAG, "onDestroy.")
        System.exit(0)
        super.onDestroy()
    }

}

