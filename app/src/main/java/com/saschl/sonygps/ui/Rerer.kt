package com.saschl.sonygps.ui

import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.saschl.sonygps.service.FileTree
import kotlinx.coroutines.launch

class LogViewerActivity : ComponentActivity() {
    private lateinit var logTextView: TextView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 16, 16, 16)
        }

        // Title bar
        val titleView = TextView(this).apply {
            text = "Application Logs"
            textSize = 24f
            setPadding(0, 0, 0, 16)
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        // Clear logs button
        val clearButton = Button(this).apply {
            text = "Clear All Logs"
            setOnClickListener {
                lifecycleScope.launch {
                    FileTree.clearLogs()
                    refreshLogs()
                }

            }
        }

        // SwipeRefreshLayout containing the log content
        swipeRefreshLayout = SwipeRefreshLayout(this).apply {
            setOnRefreshListener {
                refreshLogs()
            }
            // Set refresh colors
            setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light
            )
        }

        // Log content in scrollable view
        logTextView = TextView(this).apply {
            setTextIsSelectable(true)
            setPadding(16, 16, 16, 16)
           // setBackgroundColor(0xFFF5F5F5.toInt()) // Light gray background
            textSize = 12f
        }

        // Load initial logs
        refreshLogs()

        swipeRefreshLayout.addView(logTextView)
        mainLayout.addView(titleView)
        mainLayout.addView(clearButton)
        mainLayout.addView(swipeRefreshLayout)

        setContentView(mainLayout)

        // Handle system bars (status bar, navigation bar)
        ViewCompat.setOnApplyWindowInsetsListener(mainLayout) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(16, systemBars.top + 16, 16, systemBars.bottom + 16)
            insets
        }
    }

    private fun refreshLogs() {
        val logs = FileTree.getLogs()
        logTextView.text = if (logs.isEmpty()) {
            "No logs available yet. Logs will appear here as the app runs.\n\nPull down to refresh and fetch new logs."
        } else {
            logs.joinToString("\n\n")
        }

        // Stop the refresh animation
        swipeRefreshLayout.isRefreshing = false
    }
}
