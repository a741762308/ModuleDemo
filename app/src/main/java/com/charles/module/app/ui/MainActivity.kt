package com.charles.module.app.ui

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.charles.route.RouteManager

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Greeting()
            }
        }
    }

    @Preview
    @Composable
    private fun Greeting() {
        Column() {
            Text(text = "美食", modifier = Modifier
                .padding(all = 10.dp)
                .clickable {
                    toast("点击美食")
                    start("/food/main")
                })
            Text(text = "外卖", modifier = Modifier
                .padding(all = 10.dp)
                .clickable {
                    toast("点击外卖")
                    start("/takeout/main")
                })
        }

    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    private fun start(path: String) {
        RouteManager.startActivity(this, path)
    }
}