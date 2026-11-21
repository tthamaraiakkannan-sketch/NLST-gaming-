package com.example

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.outlined.HeadsetMic
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PlayCircleOutline
import androidx.compose.material.icons.outlined.Sensors
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.i18n.LocalNlstStrings
import com.example.i18n.stringsFor
import com.example.media.StreamCaptureRegistry
import com.example.media.StreamForegroundService
import com.example.ui.NlstViewModel
import com.example.ui.components.NlstTopBar
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.HubScreen
import com.example.ui.screens.NlstSplashScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.RoomsScreen
import com.example.ui.screens.StreamStudioScreen
import com.example.ui.screens.YouTubeLiveScreen
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonGreenSoft
import com.example.ui.theme.NlstGamingTheme
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.TextSecondary

private object NlstRoutes {
    const val HUB = "hub"
    const val STREAM = "stream"
    const val ROOMS = "rooms"
    const val YOUTUBE = "youtube"
    const val PROFILE = "profile"
}

private data class NavTabItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String,
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(ObsidianBlack.toArgb()),
            navigationBarStyle = SystemBarStyle.dark(ObsidianBlack.toArgb()),
        )
        super.onCreate(savedInstanceState)
        setContent {
            NlstGamingTheme {
                NlstAppRoot()
            }
        }
    }
}

@Composable
fun NlstAppRoot(viewModel: NlstViewModel = viewModel()) {
    val context = LocalContext.current
    val profile by viewModel.profileState.collectAsStateWithLifecycle()
    val strings = stringsFor(profile.language)
    var showSplash by rememberSaveable { mutableStateOf(true) }

    CompositionLocalProvider(LocalNlstStrings provides strings) {
        if (showSplash) {
            NlstSplashScreen(
                onFinished = { showSplash = false },
            )
        } else if (!profile.isLoggedIn) {
            AuthScreen(
                currentLanguage = profile.language,
                initialGamerTag = profile.gamerTag,
                initialDisplayName = profile.displayName,
                initialFreeFireUid = profile.freeFireUid,
                onToggleLanguage = { viewModel.toggleLanguage() },
                onEnterArena = { tag, name, uid ->
                    viewModel.signInLocally(tag, name, uid)
                },
            )
        } else {
            NlstMainShell(viewModel = viewModel, context = context)
        }
    }
}

@Composable
private fun NlstMainShell(
    viewModel: NlstViewModel,
    context: Context,
) {
    val strings = LocalNlstStrings.current
    val profile by viewModel.profileState.collectAsStateWithLifecycle()
    val youtubeConfig by viewModel.youtubeConfigState.collectAsStateWithLifecycle()
    val rooms by viewModel.roomsState.collectAsStateWithLifecycle()
    val friends by viewModel.friendsState.collectAsStateWithLifecycle()
    val activeRoom by viewModel.activeRoomState.collectAsStateWithLifecycle()
    val messages by viewModel.activeRoomMessages.collectAsStateWithLifecycle()
    val voiceState by viewModel.voiceEngineState.collectAsStateWithLifecycle()
    val captureState by viewModel.captureState.collectAsStateWithLifecycle()
    val rtmpProbe by viewModel.rtmpProbeResult.collectAsStateWithLifecycle()
    val youtubeMeta by viewModel.youtubeMetadataState.collectAsStateWithLifecycle()
    val freeFireStatus by viewModel.freeFireStatus.collectAsStateWithLifecycle()
    val snackbarMessage by viewModel.snackbarMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    var permissionTick by remember { mutableIntStateOf(0) }
    val hasMicPermission = remember(permissionTick) {
        ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED
    }
    val hasNotificationPermission = remember(permissionTick) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    val permissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) {
        permissionTick++
    }

    val requestCorePermissions = {
        val toRequest = buildList {
            add(Manifest.permission.RECORD_AUDIO)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }.toTypedArray()
        permissionsLauncher.launch(toRequest)
    }

    val mediaProjectionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        val data = result.data
        if (result.resultCode == Activity.RESULT_OK && data != null) {
            val intent = StreamForegroundService.startIntent(context, result.resultCode, data)
            ContextCompat.startForegroundService(context, intent)
        } else {
            viewModel.showMessage("Screen capture permission declined.")
        }
    }

    val startScreenCapture = {
        val mpm = context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        mediaProjectionLauncher.launch(mpm.createScreenCaptureIntent())
    }

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: NlstRoutes.HUB

    val tabs = listOf(
        NavTabItem(NlstRoutes.HUB, strings.navHub, Icons.Filled.Home, Icons.Outlined.Home, "nav_hub"),
        NavTabItem(NlstRoutes.STREAM, strings.navStream, Icons.Filled.Sensors, Icons.Outlined.Sensors, "nav_stream"),
        NavTabItem(NlstRoutes.ROOMS, strings.navRooms, Icons.Filled.HeadsetMic, Icons.Outlined.HeadsetMic, "nav_rooms"),
        NavTabItem(NlstRoutes.YOUTUBE, strings.navYouTube, Icons.Filled.PlayCircleFilled, Icons.Outlined.PlayCircleOutline, "nav_youtube"),
        NavTabItem(NlstRoutes.PROFILE, strings.navProfile, Icons.Filled.Person, Icons.Outlined.Person, "nav_profile"),
    )

    val navigateToTab: (String) -> Unit = { route ->
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBlack),
    ) {
        val useNavRail = maxWidth >= 600.dp

        Scaffold(
            containerColor = ObsidianBlack,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                NlstTopBar(
                    language = profile.language,
                    isCapturing = captureState.isCapturing,
                    measuredFps = captureState.measuredFps,
                    onToggleLanguage = { viewModel.toggleLanguage() },
                )
            },
            bottomBar = {
                if (!useNavRail) {
                    NavigationBar(
                        containerColor = ObsidianSurface,
                        modifier = Modifier.navigationBarsPadding(),
                    ) {
                        tabs.forEach { tab ->
                            val selected = currentRoute == tab.route
                            NavigationBarItem(
                                selected = selected,
                                onClick = { navigateToTab(tab.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tab.label,
                                    )
                                },
                                label = { Text(tab.label, maxLines = 1) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = NeonGreen,
                                    selectedTextColor = NeonGreen,
                                    indicatorColor = NeonGreenSoft,
                                    unselectedIconColor = TextSecondary,
                                    unselectedTextColor = TextSecondary,
                                ),
                                modifier = Modifier.testTag(tab.testTag),
                            )
                        }
                    }
                }
            },
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
                if (useNavRail) {
                    NavigationRail(
                        containerColor = ObsidianSurface,
                        modifier = Modifier.fillMaxHeight(),
                    ) {
                        tabs.forEach { tab ->
                            val selected = currentRoute == tab.route
                            NavigationRailItem(
                                selected = selected,
                                onClick = { navigateToTab(tab.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tab.label,
                                    )
                                },
                                label = { Text(tab.label) },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = NeonGreen,
                                    selectedTextColor = NeonGreen,
                                    indicatorColor = NeonGreenSoft,
                                    unselectedIconColor = TextSecondary,
                                    unselectedTextColor = TextSecondary,
                                ),
                                modifier = Modifier.testTag(tab.testTag),
                            )
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f).fillMaxSize()) {
                    NavHost(
                        navController = navController,
                        startDestination = NlstRoutes.HUB,
                    ) {
                        composable(NlstRoutes.HUB) {
                            HubScreen(
                                profile = profile,
                                freeFireStatus = freeFireStatus,
                                rooms = rooms,
                                friends = friends,
                                onStartLiveClick = { navigateToTab(NlstRoutes.STREAM) },
                                onVoiceChatClick = { navigateToTab(NlstRoutes.ROOMS) },
                                onYouTubeClick = { navigateToTab(NlstRoutes.YOUTUBE) },
                                onProfileClick = { navigateToTab(NlstRoutes.PROFILE) },
                                onCreateRoomAndOpen = { title, game, slots ->
                                    viewModel.createRoom(title, game, slots)
                                    navigateToTab(NlstRoutes.ROOMS)
                                },
                                onJoinRoomCodeAndOpen = { code ->
                                    viewModel.joinRoomByCode(code) {
                                        navigateToTab(NlstRoutes.ROOMS)
                                    }
                                },
                                onOpenRoom = { room ->
                                    viewModel.selectRoom(room)
                                    navigateToTab(NlstRoutes.ROOMS)
                                },
                                onAddFriend = { tag, game, uid ->
                                    viewModel.addFriend(tag, game, uid)
                                },
                                onDeleteFriend = { id -> viewModel.deleteFriend(id) },
                                onInviteFriendToVoiceChat = { friend ->
                                    viewModel.inviteFriendToVoiceRoom(friend)
                                    navigateToTab(NlstRoutes.ROOMS)
                                },
                            )
                        }

                        composable(NlstRoutes.STREAM) {
                            StreamStudioScreen(
                                captureState = captureState,
                                youtubeConfig = youtubeConfig,
                                hasNotificationPermission = hasNotificationPermission,
                                hasMicPermission = hasMicPermission,
                                localRoomCount = if (activeRoom != null) 1 else 0,
                                onRequestPermissions = requestCorePermissions,
                                onUpdateConfig = { target, quality, orientation ->
                                    viewModel.updateStreamConfig(target, quality, orientation)
                                },
                                onStartCapture = startScreenCapture,
                                onStopCapture = { StreamCaptureRegistry.stopCapture(context) },
                            )
                        }

                        composable(NlstRoutes.ROOMS) {
                            RoomsScreen(
                                profile = profile,
                                rooms = rooms,
                                activeRoom = activeRoom,
                                messages = messages,
                                voiceState = voiceState,
                                hasMicPermission = hasMicPermission,
                                onRequestMicPermission = requestCorePermissions,
                                onCreateRoom = { title, game, slots ->
                                    viewModel.createRoom(title, game, slots)
                                },
                                onJoinByCode = { code -> viewModel.joinRoomByCode(code) },
                                onSelectRoom = { room -> viewModel.selectRoom(room) },
                                onDeleteRoom = { id -> viewModel.deleteRoom(id) },
                                onLeaveRoom = { viewModel.leaveRoom() },
                                onSelectVoiceEffect = { effect -> viewModel.selectVoiceEffect(effect) },
                                onToggleMicMonitor = { viewModel.toggleMicMonitor() },
                                onSendChat = { msg -> viewModel.sendChatMessage(msg) },
                            )
                        }

                        composable(NlstRoutes.YOUTUBE) {
                            YouTubeLiveScreen(
                                config = youtubeConfig,
                                rtmpProbeResult = rtmpProbe,
                                metadataState = youtubeMeta,
                                onSaveConfig = { url, key, broadcast ->
                                    viewModel.saveYouTubeConfig(url, key, broadcast)
                                    viewModel.showMessage(strings.savedToast)
                                },
                                onTestRtmpHandshake = { url, key ->
                                    viewModel.testRtmpHandshake(url, key)
                                },
                                onFetchMetadata = { url -> viewModel.fetchYouTubeMetadata(url) },
                            )
                        }

                        composable(NlstRoutes.PROFILE) {
                            ProfileScreen(
                                profile = profile,
                                hasMicPermission = hasMicPermission,
                                hasNotificationPermission = hasNotificationPermission,
                                isFreeFireInstalled = freeFireStatus.isAnyInstalled,
                                onRequestPermissions = requestCorePermissions,
                                onSetLanguage = { lang -> viewModel.setLanguage(lang) },
                                onSaveProfile = { tag, name, uid, rank ->
                                    viewModel.updateProfile(tag, name, uid, rank)
                                    viewModel.showMessage(strings.savedToast)
                                },
                                onSignOut = { viewModel.signOut() },
                            )
                        }
                    }
                }
            }
        }
    }
}
