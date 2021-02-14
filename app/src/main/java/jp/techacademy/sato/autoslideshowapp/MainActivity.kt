package jp.techacademy.sato.autoslideshowapp

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.pm.PackageManager
import android.provider.MediaStore
import android.content.ContentUris
import android.database.Cursor
import android.os.Build
import android.os.Handler
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    private val PERMISSIONS_REQUEST_CODE = 100
    private var mTimer: Timer? = null
    private var mHandler = Handler()
    private var cursor:Cursor? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                getContentsInfo()
            } else {
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSIONS_REQUEST_CODE)
            }
        } else {
            getContentsInfo()
        }

        slide_button.setOnClickListener {

            if (next_button.isEnabled) {
                prev_button.isEnabled = false
                next_button.isEnabled = false
                slide_button.text = "停止"

                mTimer = Timer()
                mTimer!!.schedule(object : TimerTask() {
                    override fun run() {
                        mHandler.post {
                            if (cursor!!.isLast) {
                                cursor!!.moveToFirst()
                            } else {
                                cursor!!.moveToNext()
                            }
                            setImage()
                        }
                    }
                }, 2000, 2000)
            } else {
                mTimer!!.cancel()
                prev_button.isEnabled = true
                next_button.isEnabled = true
                slide_button.text = "再生"
            }
        }

        prev_button.setOnClickListener {
            if (cursor!!.isFirst) {
                cursor!!.moveToLast();
            } else {
                cursor!!.moveToPrevious();
            }
            setImage()
        }

        next_button.setOnClickListener {
            if (cursor!!.isLast) {
                cursor!!.moveToFirst()
            } else {
                cursor!!.moveToNext();
            }
            setImage()
        }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE ->
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContentsInfo()
                }
        }
    }

    private fun getContentsInfo() {
        // 画像の情報を取得する
        val resolver = contentResolver
        cursor = resolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null,
                null,
                null,
                null
        )

        //最初の画像を表示
        if (cursor!!.moveToFirst()) {
            slide_button.isEnabled = true
            prev_button.isEnabled = true
            next_button.isEnabled = true
            setImage()
        }
    }

    private fun setImage() {
        val fieldIndex = cursor!!.getColumnIndex(MediaStore.Images.Media._ID)
        val id = cursor!!.getLong(fieldIndex)
        val imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
        imageView.setImageURI(imageUri)
    }
}