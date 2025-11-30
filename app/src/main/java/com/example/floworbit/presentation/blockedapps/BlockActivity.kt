package com.example.floworbit.presentation.blockedapps

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.floworbit.ui.theme.FlowOrbitTheme
import com.example.floworbit.util.AllowanceManager

class BlockActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val blockedPkg = intent.getStringExtra("packageName") ?: "Unknown"

        setContent {
            FlowOrbitTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("This app is blocked!", style = MaterialTheme.typography.titleLarge)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(blockedPkg, style = MaterialTheme.typography.bodyLarge)
                        Spacer(modifier = Modifier.height(24.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(onClick = { finish() }) {
                                Text("Return")
                            }

                            Button(onClick = {
                                // grant 5 minutes allowance
                                val until = System.currentTimeMillis() + 5 * 60 * 1000L
                                AllowanceManager.setAllowanceUntil(this@BlockActivity, blockedPkg, until)
                                finish()
                            }) {
                                Text("Allow 5 min")
                            }

                            Button(onClick = {
                                // go to Home (bring launcher to front)
                                val home = Intent(Intent.ACTION_MAIN).apply {
                                    addCategory(Intent.CATEGORY_HOME)
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                startActivity(home)

                                // also ensure we clear allowance so the block doesn't re-open instantly
                                AllowanceManager.clearAllowance(this@BlockActivity, blockedPkg)
                                finish()
                            }) {
                                Text("Go Home")
                            }
                        }
                    }
                }
            }
        }
    }
}
