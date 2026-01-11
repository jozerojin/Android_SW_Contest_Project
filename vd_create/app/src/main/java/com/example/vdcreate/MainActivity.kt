package com.example.vdcreate

import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class MainActivity : AppCompatActivity() {
    private var itemCount = 0 // 생성된 항목의 개수를 추적하는 변수

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val addButton = findViewById<Button>(R.id.myButton)
        val itemContainer = findViewById<LinearLayout>(R.id.itemContainer)

        addButton.setOnClickListener {
            if (itemCount < 10) {
                val randomCode = generateRandomCode()
                val randomBattery = (0..100).random()
                val randomStatus = if (randomBattery <= 25) "off" else "on"
                val id = UUID.randomUUID().toString()
                val newItem = Item(id, randomCode, randomBattery, randomStatus)

                RetrofitClient.apiService.addItem(newItem).enqueue(object : Callback<Item> {
                    override fun onResponse(call: Call<Item>, response: Response<Item>) {
                        if (response.isSuccessful) {
                            addNewItem(itemContainer, newItem)
                            itemCount++
                        } else {
                            Toast.makeText(this@MainActivity, "항목 추가 실패", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<Item>, t: Throwable) {
                        Toast.makeText(this@MainActivity, "서버 오류: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            } else {
                Toast.makeText(this, "더 이상 항목을 추가할 수 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun generateRandomCode(): String {
        val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        return (1..5).map { charPool.random() }.joinToString("")
    }

    private fun addNewItem(itemContainer: LinearLayout, item: Item) {
        val relativeLayout = RelativeLayout(this)
        relativeLayout.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        )

        val textView = TextView(this)
        textView.text = "번호: ${item.code} 배터리: ${item.battery}% 상태: ${item.status}"
        textView.textSize = 15f
        textView.setPadding(16, 16, 16, 16)

        val deleteButton = Button(this)
        deleteButton.text = "X"
        val deleteParams = RelativeLayout.LayoutParams(
                dpToPx(80), // 너비 80dp
                dpToPx(50) // 높이 50dp
        )
        deleteParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
        deleteButton.layoutParams = deleteParams

        deleteButton.setOnClickListener {
            RetrofitClient.apiService.deleteItem(item.id).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        itemContainer.removeView(relativeLayout)
                        itemCount-- // 항목 삭제 시 개수 감소
                    } else {
                        Toast.makeText(this@MainActivity, "항목 삭제 실패", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(this@MainActivity, "서버 오류: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }

        relativeLayout.addView(textView)
        relativeLayout.addView(deleteButton)

        itemContainer.addView(relativeLayout)
    }

    // dp 값을 px로 변환하는 함수
    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }
}
