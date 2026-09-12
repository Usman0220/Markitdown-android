package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.ViewerScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.Sky400
import com.example.ui.viewmodel.MarkItDownViewModel

sealed class Screen(val route: String, val title: String, val selectedIcon: ImageVector, val unselectedIcon: ImageVector) {
    object Dashboard : Screen("dashboard", "Convert", Icons.Default.SwapHoriz, Icons.Outlined.SwapHoriz)
    object Viewer : Screen("viewer", "Viewer", Icons.Default.Description, Icons.Outlined.Description)
    object History : Screen("history", "Vault", Icons.Default.Folder, Icons.Outlined.Folder)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings, Icons.Outlined.Settings)
}

class MainActivity : ComponentActivity() {

    private val viewModel: MarkItDownViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        handleIncomingIntent(intent)

        setContent {
            MyApplicationTheme {
                MainApp(viewModel = viewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent?) {
        if (intent == null) return

        when (intent.action) {
            Intent.ACTION_SEND -> {
                if (intent.type == "text/plain") {
                    val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
                    if (!sharedText.isNullOrBlank()) {
                        viewModel.convertRawText(
                            text = sharedText,
                            formatHint = "AUTO",
                            title = "Shared Content",
                            onComplete = {}
                        )
                    }
                } else {
                    val uri = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
                    if (uri != null) {
                        val fileName = getFileNameFromUri(uri)
                        viewModel.convertUri(uri, fileName) {}
                    }
                }
            }
            Intent.ACTION_VIEW -> {
                val uri = intent.data
                if (uri != null) {
                    val fileName = getFileNameFromUri(uri)
                    viewModel.convertUri(uri, fileName) {}
                }
            }
        }
    }

    private fun getFileNameFromUri(uri: Uri): String {
        var name = "shared_document"
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex >= 0) {
                    name = it.getString(nameIndex) ?: "shared_document"
                }
            }
        }
        return name
    }
}

@Composable
fun MainApp(viewModel: MarkItDownViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val snackbarHostState = remember { SnackbarHostState() }
    val snackbarMessage by viewModel.snackbarMessage.collectAsState()

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    val items = listOf(
        Screen.Dashboard,
        Screen.Viewer,
        Screen.History,
        Screen.Settings
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                items.forEach { screen ->
                    val isSelected = currentRoute == screen.route
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                                contentDescription = screen.title
                            )
                        },
                        label = { Text(screen.title) },
                        selected = isSelected,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Sky400,
                            indicatorColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        onClick = {
                            if (currentRoute != screen.route) {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToViewer = {
                        navController.navigate(Screen.Viewer.route) {
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable(Screen.Viewer.route) {
                ViewerScreen(
                    viewModel = viewModel,
                    onNavigateBack = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Dashboard.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.History.route) {
                HistoryScreen(
                    viewModel = viewModel,
                    onNavigateToViewer = {
                        navController.navigate(Screen.Viewer.route) {
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    viewModel = viewModel
                )
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}
