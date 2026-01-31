package com.kominfo_mkq.entago

import android.annotation.SuppressLint
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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
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

    // State untuk mengontrol durasi Splash Screen
    private var isCheckingSession = true
    private var startRoute = "login"

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
            val navController = rememberNavController()
            val context = LocalContext.current

            val owner = context as ViewModelStoreOwner

            val prefManager = remember { PrefManager(context) }
            var isDarkMode by remember { mutableStateOf(prefManager.isDarkMode()) }

            LaunchedEffect(Unit) {
                SessionManager.unauthorizedEvent.collect {
                    android.widget.Toast.makeText(
                        context, // Menggunakan Context murni
                        "Sesi Anda telah berakhir. Silakan login kembali.",
                        android.widget.Toast.LENGTH_LONG
                    ).show()

                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }

            ENTAGOTheme(darkTheme = isDarkMode) {
                val apiService = remember {
                    RetrofitClient.getClient(prefManager.sharedPrefs).create(ApiService::class.java)
                }
                val database = remember { AppDatabase.getDatabase(context) }
                val tugasLuarDao = remember { database.tugasLuarDao() }

                val factoryLuar = TugasLuarViewModelFactory(apiService, tugasLuarDao)
                val sharedTugasViewModel: TugasLuarViewModel = viewModel(
                    viewModelStoreOwner = owner,
                    factory = factoryLuar
                )

                NavHost(
                    navController = navController,
                    startDestination = startRoute // Menggunakan hasil cek sesi
                ) {

                    composable("login") {
                        val loginViewModel: LoginViewModel = viewModel(
                            factory = LoginViewModelFactory(apiService, prefManager)
                        )
                        LoginScreen(loginViewModel, navController)
                    }

                    composable("dashboard") {
                        val dashboardViewModel: DashboardViewModel = viewModel(
                            factory = DashboardViewModelFactory(apiService, prefManager)
                        )
                        val riwayatViewModel: RiwayatViewModel = viewModel(
                            factory = RiwayatViewModelFactory(apiService, prefManager)
                        )

                        DashboardScreen(
                            viewModel = dashboardViewModel,
                            riwayatViewModel = riwayatViewModel,
                            prefManager = prefManager,
                            navController = navController,
                            isDarkMode = isDarkMode,
                            onThemeToggle = {
                                isDarkMode = !isDarkMode
                                prefManager.setDarkMode(isDarkMode)
                            }
                        )
                    }

                    composable("riwayat") {
                        val riwayatViewModel: RiwayatViewModel = viewModel(
                            factory = RiwayatViewModelFactory(apiService, prefManager)
                        )
                        RiwayatScreen(navController, riwayatViewModel, isDarkMode)
                    }

                    composable("izin") {
                        val izinViewModel: IzinViewModel = viewModel(
                            factory = IzinViewModelFactory(apiService, prefManager)
                        )
                        IzinScreen(navController, izinViewModel, isDarkMode)
                    }

                    composable("kantor") {
                        OfficeMapScreen(navController, prefManager, isDarkMode)
                    }

                    composable("profile") {
                        val profileViewModel: ProfileViewModel = viewModel(
                            factory = ProfileViewModelFactory(apiService, prefManager)
                        )
                        ProfileScreen(
                            navController = navController,
                            viewModel = profileViewModel,
                            prefManager = prefManager,
                            onLogout = {
                                prefManager.logout()
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
                        TugasLuarAddScreen(navController, addViewModel, prefManager)
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
                            prefManager = prefManager,
                            onThemeToggle = {
                                isDarkMode = !isDarkMode
                                prefManager.setDarkMode(isDarkMode)
                            },
                            viewModel = settingsViewModel
                        )
                    }

                    composable("panduan") {
                        PanduanScreen(navController)
                    }

                    composable("status_mesin") {
                        // Mengambil ViewModel yang SAMA dengan Dashboard
                        val dashboardViewModel: DashboardViewModel = viewModel(
                            viewModelStoreOwner = context as ViewModelStoreOwner,
                            factory = DashboardViewModelFactory(apiService, prefManager)
                        )

                        StatusMesinScreen(
                            navController = navController,
                            prefManager = prefManager,
                            viewModel = dashboardViewModel // Pass seluruh ViewModel-nya
                        )
                    }
                }
            }
        }
    }

    private fun checkForUpdates() {
        val appUpdateManager = AppUpdateManagerFactory.create(this)
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo

        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
            ) {
                // Jika ada update tersedia, minta user melakukan update segera
                appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    AppUpdateType.IMMEDIATE, // Atau gunakan FLEXIBLE
                    this,
                    1001 // Request Code
                )
            }
        }
    }
}