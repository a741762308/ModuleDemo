package com.charles.module.takeout

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.charles.route.api.RoutInject

@RoutInject(path = "/takeout/main")
class TakeoutMainActivity : AppCompatActivity() {
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
        Text(text = "外卖主页")
    }
}