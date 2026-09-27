package com.aerotech.aerocode.core.project

data class ProjectTemplate(
    val id: String,
    val name: String,
    val description: String,
    val iconName: String,
    val isXmlViews: Boolean = false,
    val initialComposeContent: (packageName: String, appName: String) -> String,
    val initialLayoutXml: ((packageName: String, appName: String) -> String)? = null
)

object ProjectTemplates {

    val EMPTY_COMPOSE = ProjectTemplate(
        id = "empty_compose",
        name = "Empty Activity (Compose M3)",
        description = "Modern Android starter with Jetpack Compose Material 3 and Scaffold",
        iconName = "Code",
        isXmlViews = false,
        initialComposeContent = { packageName, appName ->
            """
package $packageName

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    App(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun App(modifier: Modifier = Modifier) {
    var greeting by remember { mutableStateOf("Welcome to $appName") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = greeting,
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                greeting = "Hello from AeroCode Studio!"
            }
        ) {
            Text("Click Me")
        }
    }
}
            """.trimIndent()
        }
    )

    val EMPTY_VIEWS = ProjectTemplate(
        id = "empty_views",
        name = "Empty Views Activity (XML Layouts)",
        description = "Traditional Android Architecture using AppCompatActivity and XML layouts",
        iconName = "Layers",
        isXmlViews = true,
        initialComposeContent = { packageName, appName ->
            """
package $packageName

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}
            """.trimIndent()
        },
        initialLayoutXml = { packageName, appName ->
            """
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:padding="24dp"
    android:gravity="center"
    tools:context=".MainActivity">

    <TextView
        android:id="@+id/tvTitle"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="$appName"
        android:textSize="24sp"
        android:textStyle="bold"
        android:textColor="#1E1F22" />

    <TextView
        android:id="@+id/tvSubtitle"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginTop="8dp"
        android:text="Native Android XML Views Layout"
        android:textSize="14sp"
        android:textColor="#5A5D63" />

    <Button
        android:id="@+id/btnAction"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginTop="20dp"
        android:text="Get Started" />

</LinearLayout>
            """.trimIndent()
        }
    )

    val BOTTOM_NAVIGATION = ProjectTemplate(
        id = "bottom_navigation",
        name = "Bottom Navigation Activity",
        description = "Standard multi-screen navigation using Bottom Navigation Bar",
        iconName = "Navigation",
        isXmlViews = false,
        initialComposeContent = { packageName, appName ->
            """
package $packageName

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                BottomNavApp()
            }
        }
    }
}

@Composable
fun BottomNavApp() {
    var selectedIndex by remember { mutableIntStateOf(0) }
    val items = listOf("Home" to Icons.Default.Home, "Profile" to Icons.Default.Person, "Settings" to Icons.Default.Settings)

    Scaffold(
        bottomBar = {
            NavigationBar {
                items.forEachIndexed { index, (label, icon) ->
                    NavigationBarItem(
                        selected = selectedIndex == index,
                        onClick = { selectedIndex = index },
                        icon = { Icon(icon, contentDescription = label) },
                        label = { Text(label) }
                    )
                }
            }
        }
    ) { pad ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${'$'}{items[selectedIndex].first} Screen",
                style = MaterialTheme.typography.headlineMedium
            )
        }
    }
}
            """.trimIndent()
        }
    )

    val NAVIGATION_DRAWER = ProjectTemplate(
        id = "navigation_drawer",
        name = "Navigation Drawer Activity",
        description = "Collapsible side drawer navigation for complex multi-view apps",
        iconName = "Folder",
        isXmlViews = false,
        initialComposeContent = { packageName, appName ->
            """
package $packageName

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                DrawerApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawerApp() {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var currentScreen by remember { mutableStateOf("Dashboard") }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text("App Menu", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                listOf("Dashboard", "Projects", "Settings").forEach { item ->
                    NavigationDrawerItem(
                        label = { Text(item) },
                        selected = currentScreen == item,
                        onClick = {
                            currentScreen = item
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(currentScreen) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    }
                )
            }
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(text = "Viewing ${'$'}currentScreen", style = MaterialTheme.typography.titleLarge)
            }
        }
    }
}
            """.trimIndent()
        }
    )

    val RESPONSIVE_LIST_DETAIL = ProjectTemplate(
        id = "responsive_list_detail",
        name = "Responsive / Adaptive Activity",
        description = "Adaptive layout supporting phones, foldables, and tablets seamlessly",
        iconName = "Devices",
        isXmlViews = false,
        initialComposeContent = { packageName, appName ->
            """
package $packageName

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                AdaptiveApp()
            }
        }
    }
}

@Composable
fun AdaptiveApp() {
    val items = listOf("Overview", "Metrics", "Build Outputs", "Settings")
    var selectedItem by remember { mutableStateOf(items.first()) }

    Scaffold { padding ->
        Row(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Master List Pane
            Column(modifier = Modifier.weight(1f).padding(12.dp)) {
                Text("Navigation", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn {
                    items(items) { item ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { selectedItem = item },
                            colors = CardDefaults.cardColors(
                                containerColor = if (item == selectedItem) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Text(item, modifier = Modifier.padding(14.dp))
                        }
                    }
                }
            }

            // Detail Content Pane
            Surface(modifier = Modifier.weight(1.5f).fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
                Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = selectedItem, style = MaterialTheme.typography.headlineMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Details and inspector for ${'$'}selectedItem")
                    }
                }
            }
        }
    }
}
            """.trimIndent()
        }
    )

    val COUNTER_APP = ProjectTemplate(
        id = "counter_app",
        name = "Interactive Counter Activity",
        description = "State-driven counter with increment/decrement and resets",
        iconName = "AddCircle",
        isXmlViews = false,
        initialComposeContent = { packageName, appName ->
            """
package $packageName

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    CounterApp()
                }
            }
        }
    }
}

@Composable
fun CounterApp() {
    var count by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Counter", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "${'$'}count", style = MaterialTheme.typography.displayLarge, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(24.dp))
        Row {
            Button(onClick = { count-- }) { Text("-") }
            Spacer(modifier = Modifier.width(16.dp))
            Button(onClick = { count++ }) { Text("+") }
        }
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(onClick = { count = 0 }) { Text("Reset") }
    }
}
            """.trimIndent()
        }
    )

    val SHOP_UI = ProjectTemplate(
        id = "shop_ui",
        name = "Store Showcase Activity",
        description = "Product cards, badges, and buy buttons in Material 3",
        iconName = "ShoppingBag",
        isXmlViews = false,
        initialComposeContent = { packageName, appName ->
            """
package $packageName

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    StoreScreen()
                }
            }
        }
    }
}

@Composable
fun StoreScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(text = "Featured Products", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        ProductCard(title = "Aero Flight Controller", price = "$149.00", category = "Hardware")
        Spacer(modifier = Modifier.height(12.dp))
        ProductCard(title = "Jetpack Pro License", price = "$49.99", category = "Software")
    }
}

@Composable
fun ProductCard(title: String, price: String, category: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = category, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = price, style = MaterialTheme.typography.titleLarge)
                Button(onClick = {}) { Text("Buy") }
            }
        }
    }
}
            """.trimIndent()
        }
    )

    val ALL = listOf(
        EMPTY_COMPOSE,
        EMPTY_VIEWS,
        BOTTOM_NAVIGATION,
        NAVIGATION_DRAWER,
        RESPONSIVE_LIST_DETAIL,
        COUNTER_APP,
        SHOP_UI
    )
}
