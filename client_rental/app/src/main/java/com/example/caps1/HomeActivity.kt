package com.example.caps1

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeActivity : AppCompatActivity() {

    private lateinit var itemContainer: LinearLayout
    private val handler = Handler(Looper.getMainLooper())
    private val pollingInterval = 500L // 0.5초 간격으로 서버에 요청
    private val QR_SCANNER_REQUEST_CODE = 1001
    private var currentItemCode: String? = null  // 현재 대여 버튼을 누른 항목의 번호를 저장

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        itemContainer = findViewById(R.id.itemContainer)

        startPolling()
    }

    private fun startPolling() {
        handler.post(object : Runnable {
            override fun run() {
                fetchItems()
                handler.postDelayed(this, pollingInterval)
            }
        })
    }

    private fun fetchItems() {
        val call = RetrofitClient.apiService.getItems()
        call.enqueue(object : Callback<List<Item>> {
            override fun onResponse(call: Call<List<Item>>, response: Response<List<Item>>) {
                if (response.isSuccessful) {
                    val items = response.body()
                    items?.let {
                        updateItems(it)
                    }
                } else {

                }
            }

            override fun onFailure(call: Call<List<Item>>, t: Throwable) {

            }
        })
    }

    private fun updateItems(items: List<Item>) {
        itemContainer.removeAllViews()
        for (item in items) {
            addItemView(item)
        }
    }

    private fun addItemView(item: Item) {
        val itemView = layoutInflater.inflate(R.layout.item_view, itemContainer, false)

        val textView = itemView.findViewById<TextView>(R.id.textView)
        textView.text = "번호: ${item.code} 배터리: ${item.battery}% 사용여부: ${item.status}"
        itemView.tag = item.id

        val button = itemView.findViewById<Button>(R.id.buttonScan)
        if (item.status == "off") {
            button.text = "불가"
            button.setOnClickListener {
                Toast.makeText(this, "해당 기기는 배터리 잔량이 낮아 대여가 불가능합니다.", Toast.LENGTH_LONG).show()
            }
        } else {
            button.text = "대여"
            button.setOnClickListener {
                currentItemCode = item.code
                requestQrCode(item.code)
            }
        }

        itemContainer.addView(itemView)
    }

    private fun requestQrCode(code: String) {
        val request = QrCodeRequest(code)
        val call = RetrofitClient.apiService.generateQrCode(request)
        call.enqueue(object : Callback<QrCodeResponse> {
            override fun onResponse(call: Call<QrCodeResponse>, response: Response<QrCodeResponse>) {
                if (response.isSuccessful) {
                    val intent = Intent(this@HomeActivity, QRScannerActivity::class.java)
                    startActivityForResult(intent, QR_SCANNER_REQUEST_CODE)
                } else {
                    Log.e("HomeActivity", "QR 코드 생성 오류: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<QrCodeResponse>, t: Throwable) {
                Log.e("HomeActivity", "QR 코드 생성 요청 실패", t)
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == QR_SCANNER_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                val qrCodeContent = data?.getStringExtra("QR_CODE")
                qrCodeContent?.let {
                    if (it == currentItemCode) {
                        Toast.makeText(this, "인증되었습니다.", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this, "인증이 실패하였습니다.", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }
}
