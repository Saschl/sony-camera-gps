package com.saschl.sonygps.ui

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.saschl.sonygps.service.FileTree

class LogViewerActivity : ComponentActivity() {
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
            setPadding(0, 0, 0, 32)
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        // Log content in scrollable view
        val scrollView = ScrollView(this)
        val logTextView = TextView(this).apply {
            text = if (FileTree.getLogs().isEmpty()) {
                "No logs available yet. Logs will appear here as the app runs."
            } else {
                FileTree.getLogs().joinToString("\n\n")
            }
            setTextIsSelectable(true)
            setPadding(16, 16, 16, 16)
            setBackgroundColor(0xFFF5F5F5.toInt()) // Light gray background
            textSize = 12f
        }

        scrollView.addView(logTextView)
        mainLayout.addView(titleView)
        mainLayout.addView(scrollView)

        setContentView(mainLayout)

        // Handle system bars (status bar, navigation bar)
        ViewCompat.setOnApplyWindowInsetsListener(mainLayout) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(16, systemBars.top + 16, 16, systemBars.bottom + 16)
            insets
        }
    }
}
