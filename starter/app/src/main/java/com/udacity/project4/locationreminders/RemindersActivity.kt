package com.udacity.project4.locationreminders

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment
import com.udacity.project4.databinding.ActivityRemindersBinding

/**
 * The RemindersActivity that holds the reminders fragments
 */
class RemindersActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRemindersBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRemindersBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S_V2) {
            if (!shouldShowRequestPermissionRationale(PERMISSION_RATIONALE_CODE)) {
                if (ContextCompat.checkSelfPermission(
                        this,
                        POST_NOTIFICATION_PERMISSION
                    ) == PackageManager.PERMISSION_DENIED
                ) {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(POST_NOTIFICATION_PERMISSION),
                        NOTIFICATION_REQUEST_CODE
                    )
                } else {
                    Log.d(TAG, "Permission granted")
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                (binding.navHostFragment as NavHostFragment).navController.popBackStack()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == NOTIFICATION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Permission granted")
            }
        }
    }

    companion object {
        const val NOTIFICATION_REQUEST_CODE = 100
        const val PERMISSION_RATIONALE_CODE = "001"
        const val POST_NOTIFICATION_PERMISSION = "android.permission.POST_NOTIFICATIONS"
        const val TAG = "RemindersActivity"
    }
}
