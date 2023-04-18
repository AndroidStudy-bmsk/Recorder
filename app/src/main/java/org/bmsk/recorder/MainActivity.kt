package org.bmsk.recorder

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.bmsk.recorder.databinding.ActivityMainBinding
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var recorder: MediaRecorder? = null
    private var fileName = ""
    private var state = State.RELEASE
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fileName = "${externalCacheDir?.absolutePath}/audiorecordtest.3gp"

        binding.btnRecord.setOnClickListener {
            when (state) {
                State.PLAYING -> {

                }

                State.RELEASE -> {
                    record()
                }

                State.RECORDING -> {
                    onRecord(false)
                }
            }

        }
    }

    private fun record() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED -> {
                // 이제 실제로 녹음을 시작할 수 있음.
                onRecord(true)
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.RECORD_AUDIO
            ) -> {
                showPermissionRationalDialog()
            }

            else -> {
                ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.RECORD_AUDIO),
                    REQUEST_RECORD_AUDIO_CODE
                )
            }
        }
    }

    /**
     * 왜 권한이 필요한 지 띄우기 위함
     */
    private fun showPermissionRationalDialog() {
        AlertDialog.Builder(this)
            .setMessage("녹음 권한을 켜야 앱을 사용할 수 있습니다.")
            .setPositiveButton("권한 허용하기") { _, _ ->
                ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.RECORD_AUDIO),
                    REQUEST_RECORD_AUDIO_CODE
                )
            }.setNegativeButton("취소") { dialogInterface, _ ->
                dialogInterface.cancel()
            }.show()
    }

    private fun showPermissionSettingDialog() {
        AlertDialog.Builder(this)
            .setMessage("녹음 권한을 켜야 앱을 정상적으로 사용할 수 있습니다. 앱 설정 화면으로 진입하여 권한을 켜 주세요.")
            .setPositiveButton("권한 변경하러 가기") { _, _ ->
                navigateToAppSetting()
            }.setNegativeButton("취소") { dialogInterface, _ ->
                dialogInterface.cancel()
            }.show()
    }

    private fun navigateToAppSetting() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
        }
        startActivity(intent)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        val audioRecordPermissionGranted = requestCode == REQUEST_RECORD_AUDIO_CODE
                && grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED

        if (audioRecordPermissionGranted) {
            // 녹음 작업을 시작.
            onRecord(true)
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.RECORD_AUDIO
                )
            ) {
                showPermissionSettingDialog()
            }
        }
    }

    private fun onRecord(start: Boolean) = if (start) {
        startRecording()
    } else {
        stopRecording()
    }

    private fun stopRecording() {
        recorder?.apply {
            stop()
            release()
        }
        recorder = null
        state = State.RELEASE

        binding.btnRecord.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_record))
        binding.btnRecord.imageTintList =
            ColorStateList.valueOf(ContextCompat.getColor(this, R.color.red))
        binding.btnPlay.alpha = 1.0f
        binding.btnPlay.isEnabled = true
    }

    private fun startRecording() {
        state = State.RECORDING

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            recorder = MediaRecorder(this)
        } else {
            recorder = MediaRecorder()
        }
        recorder?.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setOutputFile(fileName)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

            try {
                prepare()
            } catch (e: IOException) {
                Log.e("APP", "prepare() failed")
            }

            start()
        }

        binding.btnRecord.setImageDrawable(
            ContextCompat.getDrawable(
                this,
                R.drawable.ic_stop
            )
        )
        binding.btnRecord.imageTintList =
            ColorStateList.valueOf(ContextCompat.getColor(this, R.color.black))
        binding.btnPlay.isEnabled = false
        binding.btnPlay.alpha = 0.3f
    }

    companion object {
        private const val REQUEST_RECORD_AUDIO_CODE = 100
    }

    // 상태관리가 필요함
    // 릴리즈 -> 녹음중 -> 저장(릴리즈)
    // 릴리즈 -> 재생 -> 릴리즈
    private enum class State {
        RELEASE, RECORDING, PLAYING
    }
}