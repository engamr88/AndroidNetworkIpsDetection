package io.qteam.networkipdetection

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import io.qteam.localnetworkipdetection.NetworkIpDetection
import io.qteam.localnetworkipdetection.ProgressListener
import io.qteam.networkipdetection.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private var _binding: ActivityMainBinding? = null

    private val binding get() = _binding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        processFetchNetworkIps()
    }

    private fun processFetchNetworkIps() {
        binding.preStartInfoTv.text = "Starting to fetch Network IPs ***"
        val stringBuilder = StringBuilder()
        // Create instance from library with desired options
        val instance =
            NetworkIpDetection.Builder().startRange(100).endRange(120)
                .build()
        // start fetching network ips and listen to changes..
        instance.fetchNetworkIps(object : ProgressListener {
            override fun onStart(message: String) {
                stringBuilder.append(message)
                stringBuilder.appendLine()
                runOnUiThread {
                    binding.preStartInfoTv.text = stringBuilder.toString()
                }
            }

            override fun onUpdate(percentage: Int) {
                stringBuilder.clear()
                showPercentageLayout(percentage)
            }

            override fun onComplete(result: MutableList<String>) {
                hidePercentageLayout()
                stringBuilder.append("Searching is done, Found ${result.size} devices")
                stringBuilder.appendLine()
                result.forEach {
                    stringBuilder.append(it)
                    stringBuilder.appendLine()
                }
                runOnUiThread {
                    binding.infoTv.text = stringBuilder.toString()
                }
            }

        })

    }

    private fun hidePercentageLayout() {
        runOnUiThread {
            binding.loadingProgressLayout.visibility = View.GONE
            binding.progressTv.text = ""
            binding.loadingProgress.progress = 0
        }
    }

    private fun showPercentageLayout(percentage: Int) {
        runOnUiThread {
            binding.loadingProgressLayout.visibility = View.VISIBLE
            binding.progressTv.text = "$percentage%"
            binding.loadingProgress.progress = percentage
        }
    }
}