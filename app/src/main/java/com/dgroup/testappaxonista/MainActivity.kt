package com.dgroup.testappaxonista

import android.content.Intent
import android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.activity_main.*
import android.util.Log
import androidx.core.content.FileProvider

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val mainViewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)

        mainViewModel.resultData.observe(this, Observer {
            progress.hide()
            it.data?.let {
                val contentUri = FileProvider.getUriForFile(this, "com.dgroup.testappaxonista.fileprovider", it)
                Log.i("MainActivity", "resultData ${it.path} contentUri $contentUri")
                val shareIntent: Intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_STREAM, contentUri)
                    flags = FLAG_GRANT_READ_URI_PERMISSION
                    type = "text/*"
                }
                startActivity(Intent.createChooser(shareIntent, resources.getText(R.string.send_to)))
            } ?: run {
                Toast.makeText(this, it.throwable!!.message, Toast.LENGTH_LONG).show()
            }
        })

        calcBtn.setOnClickListener {
            if (progress.isShown) {
                return@setOnClickListener
            }
            progress.show()
            mainViewModel.calcConferenceDates(assets.open("TestNew.json"), getExternalFilesDir(null)!!)
        }

    }
}
