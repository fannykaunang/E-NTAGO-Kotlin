package com.kominfo_mkq.entago

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.kominfo_mkq.entago.data.local.AppDatabase
import com.kominfo_mkq.entago.data.local.PrefManager
import com.kominfo_mkq.entago.data.remote.ApiService
import com.kominfo_mkq.entago.data.remote.RetrofitClient
import com.kominfo_mkq.entago.ui.login.LoginScreen
import com.kominfo_mkq.entago.ui.login.LoginViewModel
import com.kominfo_mkq.entago.ui.login.LoginViewModelFactory
import com.kominfo_mkq.entago.ui.screens.*
import com.kominfo_mkq.entago.ui.theme.ENTAGOTheme
import com.kominfo_mkq.entago.ui.viewmodel.*
import com.kominfo_mkq.entago.utils.SessionManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : FragmentActivity() {

    private var isCheckingSession = true
    private var startRoute = "login"

    // Properti class untuk menampung referensi NavController agar bisa diakses onNewIntent
    private var globalNavController: NavHostController? = null

    @SuppressLint("ContextCastToActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        checkForUpdates()

        splashScreen.setKeepOnScreenCondition { isCheckingSession }

        val prefManager = PrefManager(this)
        lifecycleScope.launch {
            startRoute = if (prefManager.isLoggedIn()) "dashboard" else "login"
            delay(1000)
            isCheckingSession = false
        }

        enableEdgeToEdge()

        setContent {
            // Gunakan val controller di dalam scope setContent
            val navController = rememberNavController()

            // Simpan ke properti class untuk kebutuhan onNewIntent
            globalNavController = navController

            val context = LocalContext.current
            val owner = context as ViewModelStoreOwner
            val prefManagerInstance = remember { PrefManager(context) }
            var isDarkMode by remember { mutableStateOf(prefManagerInstance.isDarkMode()) }

            // 1. Handle Sesi Habis
            LaunchedEffect(Unit) {
                SessionManager.unauthorizedEvent.collect {
                    Toast.makeText(context, "Sesi Anda telah berakhir. Silakan login kembali.", Toast.LENGTH_LONG).show()
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }

            LaunchedEffect(Unit) {
                val id = intent?.getStringExtra("TARGET_NOTIF_ID")
                if (!id.isNullOrEmpty()) {
                    delay(800) // Beri waktu NavHost benar-benar siap
                    navController.navigate("notification_detail/$id")
                }
            }

            LaunchedEffect(intent) {
                delay(500)
                handleIntent(intent, navController)
            }

            ENTAGOTheme(darkTheme = isDarkMode) {
                val apiService = remember {
                    RetrofitClient.getClient(prefManagerInstance.sharedPrefs).create(ApiService::class.java)
                }
                val database = remember { AppDatabase.getDatabase(context) }
                val tugasLuarDao = remember { database.tugasLuarDao() }

                val factoryLuar = TugasLuarViewModelFactory(apiService, tugasLuarDao)
                val sharedTugasViewModel: TugasLuarViewModel = viewModel(
                    viewModelStoreOwner = owner,
                    factory = factoryLuar
                )

                NavHost(
                    navController = navController, // Gunakan controller non-nullable di sini
                    startDestination = startRoute
                ) {
                    composable("login") {
                        val loginViewModel: LoginViewModel = viewModel(
                            factory = LoginViewModelFactory(apiService, prefManagerInstance)
                        )
                        LoginScreen(loginViewModel, navController)
                    }

                    composable("dashboard") {
                        val dashboardViewModel: DashboardViewModel = viewModel(
                            factory = DashboardViewModelFactory(apiService, prefManagerInstance)
                        )
                        val riwayatViewModel: RiwayatViewModel = viewModel(
                            factory = RiwayatViewModelFactory(apiService, prefManagerInstance)
                        )
                        DashboardScreen(
                            viewModel = dashboardViewModel,
                            riwayatViewModel = riwayatViewModel,
                            prefManager = prefManagerInstance,
                            navController = navController,
                            isDarkMode = isDarkMode,
                            onThemeToggle = {
                                isDarkMode = !isDarkMode
                                prefManagerInstance.setDarkMode(isDarkMode)
                            }
                        )
                    }

                    composable("riwayat") {
                        val riwayatViewModel: RiwayatViewModel = viewModel(
                            factory = RiwayatViewModelFactory(apiService, prefManagerInstance)
                        )
                        RiwayatScreen(navController, riwayatViewModel, isDarkMode)
                    }

                    composable("izin") {
                        val izinViewModel: IzinViewModel = viewModel(
                            factory = IzinViewModelFactory(apiService, prefManagerInstance)
                        )
                        IzinScreen(navController, izinViewModel, isDarkMode)
                    }

                    composable("kantor") {
                        OfficeMapScreen(navController, prefManagerInstance, isDarkMode)
                    }

                    composable("profile") {
                        val profileViewModel: ProfileViewModel = viewModel(
                            factory = ProfileViewModelFactory(apiService, prefManagerInstance)
                        )
                        ProfileScreen(
                            navController = navController,
                            viewModel = profileViewModel,
                            prefManager = prefManagerInstance,
                            onLogout = {
                                prefManagerInstance.logout()
                                navController.navigate("login") {
                                    popUpTo("dashboard") { inclusive = true }
                                }
                            }
                        )
                    }

                    composable("rekapitulasibulanan") {
                        val rekapViewModel: RekapBulananViewModel = viewModel(
                            factory = RekapBulananViewModelFactory(apiService)
                        )
                        RekapBulananScreen(navController, rekapViewModel)
                    }

                    composable("tugas_luar") {
                        TugasLuarScreen(navController, sharedTugasViewModel)
                    }

                    composable("add_tugas_luar") {
                        val addViewModel: TugasLuarAddViewModel = viewModel(
                            factory = TugasLuarAddViewModelFactory(apiService)
                        )
                        TugasLuarAddScreen(navController, addViewModel, prefManagerInstance)
                    }

                    composable("detail_tugas_luar") {
                        sharedTugasViewModel.selectedTugas?.let {
                            TugasLuarDetailScreen(navController, it)
                        }
                    }

                    composable("edit_tugas_luar") {
                        sharedTugasViewModel.selectedTugas?.let {
                            TugasLuarEditScreen(
                                navController = navController,
                                viewModel = viewModel(factory = TugasLuarEditViewModelFactory(apiService)),
                                sharedViewModel = sharedTugasViewModel,
                                tugasAsal = it
                            )
                        }
                    }

                    composable("sebaran_tugas") {
                        TugasLuarSebaranMapScreen(navController, sharedTugasViewModel)
                    }

                    composable("settings") {
                        val settingsViewModel: SettingsViewModel = viewModel(
                            factory = SettingsViewModelFactory(apiService)
                        )
                        SettingsScreen(
                            navController = navController,
                            isDarkMode = isDarkMode,
                            prefManager = prefManagerInstance,
                            onThemeToggle = {
                                isDarkMode = !isDarkMode
                                prefManagerInstance.setDarkMode(isDarkMode)
                            },
                            viewModel = settingsViewModel
                        )
                    }

                    composable("panduan") {
                        PanduanScreen(navController)
                    }

                    composable("status_mesin") {
                        val dashboardViewModel: DashboardViewModel = viewModel(
                            viewModelStoreOwner = owner,
                            factory = DashboardViewModelFactory(apiService, prefManagerInstance)
                        )
                        StatusMesinScreen(
                            navController = navController,
                            prefManager = prefManagerInstance,
                            viewModel = dashboardViewModel
                        )
                    }

                    composable(
                        route = "notification_detail/{notifId}",
                        arguments = listOf(navArgument("notifId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val id = backStackEntry.arguments?.getString("notifId")
                        val notifViewModel: NotificationDetailViewModel = viewModel(
                            factory = NotificationDetailViewModelFactory(apiService)
                        )
                        NotificationDetailScreen(id, navController, viewModel = notifViewModel)
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)

        val notifId = intent.getStringExtra("notification_id")

        if (!notifId.isNullOrEmpty()) {
            lifecycleScope.launch {
                delay(300)
                globalNavController?.navigate("notification_detail/$notifId") {
                    launchSingleTop = true
                }
            }
        }
    }

    // Tambahkan juga pengecekan di handleIntent yang dipanggil LaunchedEffect
    private fun handleIntent(intent: Intent?, navController: NavHostController) {
        // GANTI "TARGET_NOTIF_ID" MENJADI "notification_id" sesuai Logcat
        val notifId = intent?.getStringExtra("notification_id")

        android.util.Log.d("NOTIF_DEBUG", "Mencoba navigasi dengan ID: $notifId")

        if (!notifId.isNullOrEmpty()) {
            navController.navigate("notification_detail/$notifId") {
                launchSingleTop = true
            }
            // Hapus extra setelah digunakan
            intent.removeExtra("notification_id")
        }
    }

    private fun checkForUpdates() {
        val appUpdateManager = AppUpdateManagerFactory.create(this)
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
            ) {
                appUpdateManager.startUpdateFlowForResult(appUpdateInfo, AppUpdateType.IMMEDIATE, this, 1001)
            }
        }
    }
}