package jp.juggler.letsencryptcertificateinstaller

import android.Manifest
import android.app.DownloadManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
	
	companion object{
		const val REQUEST_CODE_PERMISSION =1
	}

	private fun showToast(ex : Throwable) {
		Toast.makeText(this, "${ex.javaClass.simpleName} ${ex.message}", Toast.LENGTH_LONG).show()
	}

	private fun showToast(text:String) {
		Toast.makeText(this, text,Toast.LENGTH_LONG ).show()
	}

	override fun onCreate(savedInstanceState : Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)

		findViewById<View>(R.id.btnUsage).setOnClickListener {
			openBrowser("http://juggler.jp/letsencryptcertificateinstaller/")
		}


		findViewById<View>(R.id.btnTestSite).setOnClickListener {
			openBrowser(getString(R.string.url_test))
		}
		
		findViewById<View>(R.id.btnDownload).setOnClickListener {
			download()
		}
		
		findViewById<View>(R.id.btnSecuritySetting).setOnClickListener {
			try {
				startActivity(Intent(Settings.ACTION_SECURITY_SETTINGS))
			} catch(ex : Throwable) {
				showToast(ex)
			}
		}
	}
	
	override fun onRequestPermissionsResult(
		requestCode : Int,
		permissions : Array<out String>,
		grantResults : IntArray
	) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults)
		if( requestCode == REQUEST_CODE_PERMISSION){
			if( grantResults.firstOrNull() != PackageManager.PERMISSION_GRANTED ) {
				showToast( getString(R.string.please_grant_permission_in_app_setting))
			}else {
				download()
			}
		}
	}
	
	private fun download(){
		try {
			val perm = Manifest.permission.WRITE_EXTERNAL_STORAGE
			if( PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this,perm ) ) {
				// パーミッションのリクエストを表示
				ActivityCompat.requestPermissions(this, arrayOf(perm), REQUEST_CODE_PERMISSION)
				return
			}

			val url = getString(R.string.url_certificate)
			val fileName = getString(R.string.file_name)
			
			val request = DownloadManager.Request(Uri.parse(url)).apply{
				setTitle("Download ISRG Root X1 Certificate")
				setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
				setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE or DownloadManager.Request.NETWORK_WIFI)
				//ダウンロード中・ダウンロード完了時にも通知を表示する
				setNotificationVisibility( DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
				
				// Android 10 以降では allowScanningByMediaScanner は無視される
				if(Build.VERSION.SDK_INT < 29) {
					//メディアスキャンを許可する
					@Suppress("DEPRECATION")
					allowScanningByMediaScanner()
				}
			}
			
			ContextCompat.getSystemService(this, DownloadManager::class.java)!!
				.enqueue(request)
		} catch(ex : Throwable) {
			showToast(ex)
		}
	}

	private fun openBrowser(url:String){
		try {
			startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
		} catch(ex : Throwable) {
			showToast(ex)
		}
	}
}