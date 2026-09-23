package com.example.features.ai

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.data.DemoDataRepository
import com.example.core.localization.AppLanguage
import com.example.core.localization.AppStrings
import com.example.core.model.ChatMessage
import com.example.features.offline_ai.model.ModelInferenceState
import com.example.features.offline_ai.service.OfflineAiService
import com.example.ui.components.AppCard
import com.example.ui.components.AppSegmentedControl
import com.example.ui.components.AppStatusBadge
import com.example.ui.components.glassEffect
import com.example.ui.components.isAppDarkTheme
import com.example.ui.theme.AppComponentSize
import com.example.ui.theme.AppIconSize
import com.example.ui.theme.AppRadius
import com.example.ui.theme.AppSpacing
import com.example.ui.theme.AppTypography
import com.example.ui.theme.PrimaryBrand
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusSuccessContainer

data class QuickPromptItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val prompt: String
)

@Composable
fun DedicatedAiScreen(
    strings: AppStrings,
    currentLanguage: AppLanguage,
    repository: DemoDataRepository,
    offlineService: OfflineAiService,
    modifier: Modifier = Modifier
) {
    // 0 = Offline AI, 1 = Online AI
    var selectedAiMode by rememberSaveable { mutableIntStateOf(0) }
    var inputText by remember { mutableStateOf("") }
    val offlineListState = rememberLazyListState()
    val onlineListState = rememberLazyListState()
    val listState = if (selectedAiMode == 0) offlineListState else onlineListState
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    // API Key Dialog state
    var showApiKeyDialog by remember { mutableStateOf(false) }
    var apiKeyInput by remember { mutableStateOf("") }
    var isKeyVisible by remember { mutableStateOf(false) }
    val customApiKey by repository.customGeminiApiKey.collectAsState()
    val isGeminiConfigured = repository.isGeminiKeyConfigured()

    // Offline AI flows
    val offlineMessages by offlineService.messages.collectAsState()
    val offlineInferenceState by offlineService.inferenceState.collectAsState()
    val isOfflineThinking = offlineInferenceState in listOf(
        ModelInferenceState.RETRIEVING_CONTEXT,
        ModelInferenceState.GENERATING,
        ModelInferenceState.LOADING_WEIGHTS
    )

    // Online AI flows
    val onlineMessages by repository.chatMessages.collectAsState()
    val isOnlineThinking by repository.isAiThinking.collectAsState()

    // Active state
    val isThinking = if (selectedAiMode == 0) isOfflineThinking else isOnlineThinking
    val totalMessageCount = if (selectedAiMode == 0) offlineMessages.size else onlineMessages.size

    val promptSuggestions = listOf(
        QuickPromptItem(
            title = "Ertirki sapaklar",
            subtitle = "Sapak tertibi we wagtlary",
            icon = Icons.Default.CalendarMonth,
            prompt = "Ertir haýsy sapaklar bar?"
        ),
        QuickPromptItem(
            title = "Akademik ýagdaý",
            subtitle = "GPA we gazanylan kreditler",
            icon = Icons.Default.School,
            prompt = "Akademik ýagdaýym we bahalarym nähili?"
        ),
        QuickPromptItem(
            title = "Ýakynlaşýan synaglar",
            subtitle = "Ekzamenler we otaglar",
            icon = Icons.Default.EventNote,
            prompt = "Indiki ekzamenim haçan we nirede?"
        ),
        QuickPromptItem(
            title = "Programmirleme kömegi",
            subtitle = "Python, Kotlin, kiberhowpsuzlyk",
            icon = Icons.Default.Code,
            prompt = "Kiberhowpsuzlyk we tor howpsuzlygy boýunça barlag soraglaryny ber"
        )
    )

    val imeBottomPadding = WindowInsets.ime.asPaddingValues().calculateBottomPadding()
    val isKeyboardOpen = imeBottomPadding > 0.dp

    // Auto-scroll on new message or keyboard open
    LaunchedEffect(selectedAiMode, totalMessageCount, isThinking, isKeyboardOpen) {
        if (totalMessageCount > 0) {
            val targetIndex = totalMessageCount + if (isThinking) 1 else 0
            listState.animateScrollToItem(targetIndex)
        }
    }

    val navBarBottomPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val bottomNavOffset = navBarBottomPadding + AppComponentSize.bottomNavHeight + AppComponentSize.bottomNavBottomSpacing

    // When keyboard is open, pad by exact keyboard height (imeBottomPadding) so the input bar rises flush above the keyboard.
    // When keyboard is closed, pad by bottomNavOffset so the input bar sits cleanly above the floating bottom nav bar.
    val currentBottomPadding = if (isKeyboardOpen) imeBottomPadding else bottomNavOffset

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(bottom = currentBottomPadding)
            .testTag("dedicated_ai_screen")
    ) {
        // Top Header with Glass Effect
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .glassEffect(
                    shape = RoundedCornerShape(bottomStart = AppRadius.card, bottomEnd = AppRadius.card),
                    elevation = 4.dp
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppSpacing.screenHorizontal, vertical = AppSpacing.s),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = PrimaryBrand.copy(alpha = 0.12f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = PrimaryBrand,
                                modifier = Modifier.size(AppIconSize.small)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(AppSpacing.xs))

                    Column {
                        Text(
                            text = "Akylly Talyp AI",
                            style = AppTypography.Title.copy(fontSize = 17.sp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (selectedAiMode == 0) "Offline enjam intellekti" else "Online Gemini AI kömekçisi",
                            style = AppTypography.Caption,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (selectedAiMode == 1) {
                        IconButton(
                            onClick = {
                                apiKeyInput = customApiKey
                                showApiKeyDialog = true
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Key,
                                contentDescription = "API Açary",
                                tint = if (isGeminiConfigured) StatusSuccess else PrimaryBrand,
                                modifier = Modifier.size(AppIconSize.small)
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            if (selectedAiMode == 0) {
                                offlineService.clearConversation()
                            } else {
                                repository.clearAiChat()
                            }
                            Toast.makeText(context, "Gürrüňdeşlik arassalandy", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Arassala",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(AppIconSize.small)
                        )
                    }
                }
            }
        }

        // Segmented Control & Status Banner
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.screenHorizontal, vertical = AppSpacing.xs)
        ) {
            AppSegmentedControl(
                items = listOf("Offline AI (Enjamda)", "Online AI (Gemini)"),
                selectedIndex = selectedAiMode,
                onItemSelected = { selectedAiMode = it },
                testTag = "ai_mode_segmented"
            )

            Spacer(modifier = Modifier.height(AppSpacing.xs))

            // Active Mode Status Pill with Glass Effect
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassEffect(shape = RoundedCornerShape(AppRadius.medium), elevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = AppSpacing.s, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (selectedAiMode == 0) {
                        Surface(
                            shape = CircleShape,
                            color = StatusSuccess,
                            modifier = Modifier.size(8.dp)
                        ) {}
                        Spacer(modifier = Modifier.width(AppSpacing.xs))
                        Icon(
                            imageVector = Icons.Default.Memory,
                            contentDescription = null,
                            tint = StatusSuccess,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(AppSpacing.xxs))
                        Text(
                            text = "Offline režim • 100% awtonom, maglumatlaryňyz telefonyňyzda",
                            style = AppTypography.Caption.copy(fontSize = 11.5.sp, fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Surface(
                            shape = CircleShape,
                            color = if (isGeminiConfigured) StatusSuccess else PrimaryBrand,
                            modifier = Modifier.size(8.dp)
                        ) {}
                        Spacer(modifier = Modifier.width(AppSpacing.xs))
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = PrimaryBrand,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(AppSpacing.xxs))
                        Text(
                            text = if (isGeminiConfigured) "Online režim • Gemini API işjeň" else "Online režim • Gemini API açary gerek",
                            style = AppTypography.Caption.copy(fontSize = 11.5.sp, fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        Surface(
                            color = PrimaryBrand.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.clickable {
                                apiKeyInput = customApiKey
                                showApiKeyDialog = true
                            }
                        ) {
                            Text(
                                text = if (isGeminiConfigured) "Açar ✓" else "+ Açar",
                                style = AppTypography.Caption.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                                color = PrimaryBrand,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }
        }

        // Chat Messages Area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            val isEmpty = if (selectedAiMode == 0) offlineMessages.isEmpty() else onlineMessages.isEmpty()

            if (isEmpty) {
                // Polished Empty State with rich action cards
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = AppSpacing.screenHorizontal),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    item {
                        Surface(
                            shape = CircleShape,
                            color = PrimaryBrand.copy(alpha = 0.10f),
                            modifier = Modifier.size(72.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (selectedAiMode == 0) Icons.Default.SmartToy else Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = PrimaryBrand,
                                    modifier = Modifier.size(34.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(AppSpacing.m))

                        Text(
                            text = if (selectedAiMode == 0) "Offline Talyp AI taýyn" else "Online Talyp AI taýyn",
                            style = AppTypography.Headline.copy(fontSize = 18.sp),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(AppSpacing.xxs))

                        Text(
                            text = "Okuw tertibi, bahalar, synaglar we konspektler barada sorap bilersiňiz.",
                            style = AppTypography.BodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(AppSpacing.l))
                    }

                    items(promptSuggestions) { item ->
                        AppCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    if (selectedAiMode == 0) {
                                        offlineService.sendMessage(item.prompt, currentLanguage.code)
                                    } else {
                                        repository.sendAiMessage(item.prompt)
                                    }
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = AppSpacing.m, vertical = AppSpacing.s),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(AppRadius.small),
                                        color = PrimaryBrand.copy(alpha = 0.10f),
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = item.icon,
                                                contentDescription = null,
                                                tint = PrimaryBrand,
                                                modifier = Modifier.size(AppIconSize.small)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(AppSpacing.s))

                                    Column {
                                        Text(
                                            text = item.title,
                                            style = AppTypography.Body.copy(fontWeight = FontWeight.SemiBold, fontSize = 14.sp),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = item.subtitle,
                                            style = AppTypography.Caption,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(AppIconSize.small)
                                )
                            }
                        }
                    }
                }
            } else {
                // Messages List
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = AppSpacing.screenHorizontal),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.s)
                ) {
                    item { Spacer(modifier = Modifier.height(AppSpacing.xs)) }

                    if (selectedAiMode == 0) {
                        items(offlineMessages) { msg ->
                            AiMessageBubble(
                                isUser = msg.isUser,
                                text = msg.text,
                                timestamp = msg.timestamp,
                                onCopy = {
                                    clipboardManager.setText(AnnotatedString(msg.text))
                                    Toast.makeText(context, "Göçürildi", Toast.LENGTH_SHORT).show()
                                },
                                onRegenerate = if (!msg.isUser && msg == offlineMessages.lastOrNull { !it.isUser }) {
                                    {
                                        val lastUserQuery = offlineMessages.lastOrNull { it.isUser }?.text
                                        if (!lastUserQuery.isNullOrBlank()) {
                                            offlineService.sendMessage(lastUserQuery, currentLanguage.code)
                                        }
                                    }
                                } else null
                            )
                        }
                    } else {
                        items(onlineMessages) { msg ->
                            AiMessageBubble(
                                isUser = msg.isUser,
                                text = msg.text,
                                timestamp = msg.timestamp,
                                onCopy = {
                                    clipboardManager.setText(AnnotatedString(msg.text))
                                    Toast.makeText(context, "Göçürildi", Toast.LENGTH_SHORT).show()
                                },
                                onRegenerate = null
                            )
                        }
                    }

                    // Typing indicator bubble
                    if (isThinking) {
                        item {
                            TypingIndicatorBubble()
                        }
                    }

                    item { Spacer(modifier = Modifier.height(AppSpacing.s)) }
                }
            }
        }

        // Bottom Input Field Bar with Glassmorphism
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .glassEffect(
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                    elevation = 6.dp
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppSpacing.screenHorizontal, vertical = AppSpacing.xs),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = {
                        Text(
                            text = "Soragyňyzy ýazyň...",
                            style = AppTypography.BodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("ai_input_field"),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryBrand,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.12f)
                    ),
                    maxLines = 4,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (inputText.isNotBlank() && !isThinking) {
                                val query = inputText.trim()
                                inputText = ""
                                if (selectedAiMode == 0) {
                                    offlineService.sendMessage(query, currentLanguage.code)
                                } else {
                                    repository.sendAiMessage(query)
                                }
                            }
                        }
                    )
                )

                Spacer(modifier = Modifier.width(AppSpacing.xs))

                // Send Button with glass specular reflection
                val canSend = inputText.isNotBlank() && !isThinking
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .shadow(if (canSend) 4.dp else 0.dp, CircleShape)
                        .clip(CircleShape)
                        .background(
                            if (canSend) {
                                Brush.verticalGradient(
                                    listOf(
                                        PrimaryBrand,
                                        Color(0xFF0284C7)
                                    )
                                )
                            } else {
                                Brush.verticalGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.surfaceVariant,
                                        MaterialTheme.colorScheme.surfaceVariant
                                    )
                                )
                            }
                        )
                        .border(
                            1.dp,
                            if (canSend) Color.White.copy(alpha = 0.35f) else Color.Transparent,
                            CircleShape
                        )
                        .clickable(enabled = canSend) {
                            val query = inputText.trim()
                            inputText = ""
                            if (selectedAiMode == 0) {
                                offlineService.sendMessage(query, currentLanguage.code)
                            } else {
                                repository.sendAiMessage(query)
                            }
                        }
                        .testTag("ai_send_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = if (canSend) Color.White else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(19.dp)
                    )
                }
            }
        }

        // Gemini API Key Dialog
        if (showApiKeyDialog) {
            AlertDialog(
                onDismissRequest = { showApiKeyDialog = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = null,
                            tint = PrimaryBrand,
                            modifier = Modifier.size(AppIconSize.medium)
                        )
                        Spacer(modifier = Modifier.width(AppSpacing.xs))
                        Text(
                            text = "Gemini API Açary",
                            style = AppTypography.Headline,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Google AI Studio-dan alan şahsy Gemini API açaryňyzy girizip bilersiňiz. Açaryňyz diňe öz telefonyňyzda howpsuz ýatda saklanar.",
                            style = AppTypography.BodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(AppSpacing.m))
                        OutlinedTextField(
                            value = apiKeyInput,
                            onValueChange = { apiKeyInput = it },
                            placeholder = { Text("AIzaSy...", style = AppTypography.BodySmall) },
                            singleLine = true,
                            visualTransformation = if (isKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { isKeyVisible = !isKeyVisible }) {
                                    Icon(
                                        imageVector = if (isKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = if (isKeyVisible) "Hide key" else "Show key",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(AppRadius.medium)
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            repository.setCustomGeminiApiKey(apiKeyInput)
                            showApiKeyDialog = false
                            Toast.makeText(context, "API açary ýatda saklandy", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Text("Ýatda sakla", fontWeight = FontWeight.Bold, color = PrimaryBrand)
                    }
                },
                dismissButton = {
                    Row {
                        if (customApiKey.isNotBlank()) {
                            TextButton(
                                onClick = {
                                    repository.setCustomGeminiApiKey("")
                                    apiKeyInput = ""
                                    showApiKeyDialog = false
                                    Toast.makeText(context, "API açary aýryldy", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Text("Açary aýyr", color = MaterialTheme.colorScheme.error)
                            }
                        }
                        TextButton(onClick = { showApiKeyDialog = false }) {
                            Text("Ýap", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun AiMessageBubble(
    isUser: Boolean,
    text: String,
    timestamp: String,
    onCopy: () -> Unit,
    onRegenerate: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        if (!isUser) {
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth(0.92f)
            ) {
                Surface(
                    shape = CircleShape,
                    color = PrimaryBrand.copy(alpha = 0.12f),
                    modifier = Modifier
                        .size(28.dp)
                        .padding(top = 2.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = PrimaryBrand,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(AppSpacing.xs))

                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .glassEffect(
                                shape = RoundedCornerShape(
                                    topStart = 4.dp,
                                    topEnd = 16.dp,
                                    bottomStart = 16.dp,
                                    bottomEnd = 16.dp
                                ),
                                elevation = 2.dp
                            )
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = text,
                                style = AppTypography.Body.copy(lineHeight = 22.sp, fontSize = 14.5.sp),
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(AppSpacing.xxs))

                            Text(
                                text = timestamp,
                                style = AppTypography.Caption.copy(fontSize = 10.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.align(Alignment.End)
                            )
                        }
                    }

                    // Action icons row under AI response
                    Row(
                        modifier = Modifier.padding(top = 2.dp, start = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onCopy,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy message",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        if (onRegenerate != null) {
                            IconButton(
                                onClick = onRegenerate,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Regenerate answer",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // User bubble with gradient glass style
            Box(
                modifier = Modifier
                    .widthIn(max = 300.dp)
                    .shadow(
                        elevation = 3.dp,
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = 16.dp,
                            bottomEnd = 4.dp
                        )
                    )
                    .clip(
                        RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = 16.dp,
                            bottomEnd = 4.dp
                        )
                    )
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                PrimaryBrand.copy(alpha = 0.95f),
                                Color(0xFF0284C7).copy(alpha = 0.90f)
                            )
                        )
                    )
                    .border(
                        1.dp,
                        Brush.verticalGradient(
                            listOf(
                                Color.White.copy(alpha = 0.45f),
                                Color.White.copy(alpha = 0.15f)
                            )
                        ),
                        RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = 16.dp,
                            bottomEnd = 4.dp
                        )
                    )
            ) {
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                    Text(
                        text = text,
                        style = AppTypography.Body.copy(lineHeight = 21.sp, fontSize = 14.5.sp),
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = timestamp,
                        style = AppTypography.Caption.copy(fontSize = 10.sp),
                        color = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }
        }
    }
}

@Composable
fun TypingIndicatorBubble() {
    val transition = rememberInfiniteTransition(label = "typing_anim")
    val alpha1 by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(600), repeatMode = RepeatMode.Reverse),
        label = "dot1"
    )
    val alpha2 by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(600, delayMillis = 200), repeatMode = RepeatMode.Reverse),
        label = "dot2"
    )
    val alpha3 by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(600, delayMillis = 400), repeatMode = RepeatMode.Reverse),
        label = "dot3"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = AppSpacing.xxs)
    ) {
        Surface(
            shape = CircleShape,
            color = PrimaryBrand.copy(alpha = 0.12f),
            modifier = Modifier.size(28.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = PrimaryBrand,
                    modifier = Modifier.size(15.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(AppSpacing.xs))

        Box(
            modifier = Modifier.glassEffect(shape = RoundedCornerShape(16.dp), elevation = 2.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = AppSpacing.m, vertical = AppSpacing.xs),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.xxs)
            ) {
                Surface(
                    shape = CircleShape,
                    color = PrimaryBrand.copy(alpha = alpha1),
                    modifier = Modifier.size(AppSpacing.xs)
                ) {}
                Surface(
                    shape = CircleShape,
                    color = PrimaryBrand.copy(alpha = alpha2),
                    modifier = Modifier.size(AppSpacing.xs)
                ) {}
                Surface(
                    shape = CircleShape,
                    color = PrimaryBrand.copy(alpha = alpha3),
                    modifier = Modifier.size(AppSpacing.xs)
                ) {}
                Spacer(modifier = Modifier.width(AppSpacing.xxs))
                Text(
                    text = "Jogap taýýarlanýar...",
                    style = AppTypography.Caption,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
