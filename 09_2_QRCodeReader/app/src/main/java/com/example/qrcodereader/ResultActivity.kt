package com.example.qrcodereader

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.qrcodereader.databinding.ActivityResultBinding

class ResultActivity : AppCompatActivity() {

    lateinit var binding: ActivityResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityResultBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val result = intent.getStringExtra("msg") ?: "데이터가 존재하지 않습니다." // ❷

        setUI(result)// UI를 초기화합니다.
    }

    private fun setUI(result: String) {
        binding.tvContent.text = result // 넘어온 QR 코드 속 데이터를 텍스트뷰에 설정합니다.
        binding.btnGoBack.setOnClickListener {
            finish() // 돌아가기 버튼을 눌러줬을 때 ResultActivity를 종료합니다.
        }
    }

}
