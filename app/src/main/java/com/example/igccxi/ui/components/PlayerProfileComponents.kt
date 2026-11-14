package com.example.igccxi.ui.components

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.igccxi.domain.model.PlayerProfile
import com.example.igccxi.ui.theme.CricketGold
import com.example.igccxi.ui.theme.CricketGreenPrimary
import java.io.File
import java.util.Locale

/**
 * Loads a local photo bitmap from disk in a memory-efficient manner.
 */
@Composable
fun rememberPlayerBitmap(path: String?): ImageBitmap? {
    return remember(path) {
        if (path.isNullOrBlank()) null
        else {
            try {
                val file = File(path)
                if (file.exists() && file.length() > 0L) {
                    BitmapFactory.decodeFile(file.absolutePath)?.asImageBitmap()
                } else null
            } catch (_: Exception) {
                null
            }
        }
    }
}

/**
 * Circular avatar component that displays either a profile photo, a cricket emoji, or initials.
 */
@Composable
fun PlayerAvatar(
    name: String,
    photoPath: String? = null,
    avatarIcon: String? = null,
    colorHex: Long = 0xFF1B5E20,
    size: Dp = 40.dp,
    showCameraBadge: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val bitmap = rememberPlayerBitmap(photoPath)
    val initials = remember(name) {
        val parts = name.trim().split("\\s+".toRegex()).filter { it.isNotEmpty() }
        when {
            parts.isEmpty() -> "P"
            parts.size == 1 -> parts[0].take(2).uppercase(Locale.ROOT)
            else -> "${parts[0].first().uppercase(Locale.ROOT)}${parts[1].first().uppercase(Locale.ROOT)}"
        }
    }

    Box(
        modifier = Modifier
            .size(size)
            .then(
                if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        when {
            bitmap != null -> {
                Image(
                    bitmap = bitmap,
                    contentDescription = "$name profile photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(size)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
            }
            !avatarIcon.isNullOrBlank() -> {
                Surface(
                    shape = CircleShape,
                    color = Color(colorHex).copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, Color(colorHex).copy(alpha = 0.5f)),
                    modifier = Modifier.size(size)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = avatarIcon,
                            fontSize = (size.value * 0.45f).sp
                        )
                    }
                }
            }
            else -> {
                Surface(
                    shape = CircleShape,
                    color = Color(colorHex),
                    modifier = Modifier.size(size)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = initials,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = (size.value * 0.38f).sp
                        )
                    }
                }
            }
        }

        if (showCameraBadge) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .size((size.value * 0.42f).dp)
                    .align(Alignment.BottomEnd)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Edit photo",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size((size.value * 0.25f).dp)
                    )
                }
            }
        }
    }
}

/**
 * The rectangle box with name and an option to add/edit profile on the player's name.
 * Directly fulfills the user's specification:
 * "basically it should not be a profile but it should be like a reactangle box with name and a option to add profile on his name, basically row of name and profile"
 */
@Composable
fun PlayerProfileRowBox(
    name: String,
    profile: PlayerProfile? = null,
    subtitle: String? = null,
    onSelect: (() -> Unit)? = null,
    onAddOrEditProfile: () -> Unit,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    highlightNew: Boolean = false
) {
    val photoPath = profile?.photoPath
    val avatarIcon = profile?.avatarIcon
    val colorHex = profile?.avatarColorHex ?: 0xFF1B5E20
    val hasPhoto = !photoPath.isNullOrBlank() || !avatarIcon.isNullOrBlank()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onSelect != null) Modifier.clickable(onClick = onSelect) else Modifier
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isSelected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                highlightNew -> CricketGreenPrimary.copy(alpha = 0.08f)
                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            }
        ),
        border = BorderStroke(
            1.dp,
            when {
                isSelected -> MaterialTheme.colorScheme.primary
                highlightNew -> CricketGreenPrimary.copy(alpha = 0.4f)
                else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile Avatar with tap to add/edit photo
            PlayerAvatar(
                name = name,
                photoPath = photoPath,
                avatarIcon = avatarIcon,
                colorHex = colorHex,
                size = 40.dp,
                showCameraBadge = true,
                onClick = onAddOrEditProfile
            )

            Spacer(modifier = Modifier.width(10.dp))

            // Player Name and helper text in the row
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle ?: if (hasPhoto) "Saved Player • Profile set" else "Saved Player • Tap 📷 to add photo",
                    fontSize = 11.sp,
                    color = if (hasPhoto) CricketGreenPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Dedicated "Option to add/edit profile on his name"
            Surface(
                onClick = onAddOrEditProfile,
                shape = RoundedCornerShape(8.dp),
                color = if (hasPhoto) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                border = BorderStroke(
                    1.dp,
                    if (hasPhoto) MaterialTheme.colorScheme.outlineVariant else MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (hasPhoto) Icons.Default.Edit else Icons.Default.AddAPhoto,
                        contentDescription = "Profile option",
                        tint = if (hasPhoto) MaterialTheme.colorScheme.primary else CricketGreenPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (hasPhoto) "Edit Photo" else "+ Profile",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (hasPhoto) MaterialTheme.colorScheme.primary else CricketGreenPrimary
                    )
                }
            }
        }
    }
}

/**
 * Autocomplete Player Input Field that automatically suggests stored players
 * as soon as the user enters even the first letter, displaying them as styled
 * rectangle rows with name and profile.
 */
@Composable
fun PlayerAutocompleteTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String = "",
    allProfiles: List<PlayerProfile>,
    onRequestAddProfile: (playerName: String) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    supportingText: (@Composable () -> Unit)? = null,
    singleLine: Boolean = true
) {
    var isFocused by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val query = value.trim()

    // Filter matching player profiles
    val suggestions = remember(query, allProfiles) {
        if (query.isEmpty()) {
            emptyList()
        } else {
            allProfiles.filter {
                it.name.contains(query, ignoreCase = true)
            }.take(5)
        }
    }

    // Exact match if any
    val exactMatch = remember(query, allProfiles) {
        if (query.isEmpty()) null
        else allProfiles.find { it.name.equals(query, ignoreCase = true) }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            singleLine = singleLine,
            isError = isError,
            supportingText = supportingText,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { isFocused = it.isFocused },
            leadingIcon = {
                val activeProfile = exactMatch
                if (activeProfile != null) {
                    PlayerAvatar(
                        name = activeProfile.name,
                        photoPath = activeProfile.photoPath,
                        avatarIcon = activeProfile.avatarIcon,
                        colorHex = activeProfile.avatarColorHex,
                        size = 28.dp,
                        onClick = { onRequestAddProfile(activeProfile.name) }
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            },
            trailingIcon = {
                if (value.isNotEmpty()) {
                    IconButton(onClick = { onValueChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
        )

        // Suggestion Dropdown List (Rectangle boxes with name and profile)
        AnimatedVisibility(
            visible = isFocused && query.isNotEmpty() && suggestions.isNotEmpty(),
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier.padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "SUGGESTED PLAYERS (TAP TO AUTOFILL)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )

                    suggestions.forEach { prof ->
                        PlayerProfileRowBox(
                            name = prof.name,
                            profile = prof,
                            subtitle = "Saved in app • Tap to select",
                            onSelect = {
                                onValueChange(prof.name)
                                focusManager.clearFocus()
                            },
                            onAddOrEditProfile = {
                                onRequestAddProfile(prof.name)
                            }
                        )
                    }
                }
            }
        }

        // If user entered a name that is not yet saved, show a quick rectangle box to save & add profile
        if (query.isNotEmpty() && exactMatch == null && suggestions.none { it.name.equals(query, ignoreCase = true) }) {
            Spacer(modifier = Modifier.height(4.dp))
            PlayerProfileRowBox(
                name = query,
                profile = null,
                subtitle = "New Player • Add profile photo or avatar",
                onSelect = null,
                onAddOrEditProfile = { onRequestAddProfile(query) },
                highlightNew = true
            )
        }
    }
}

/**
 * Dialog to add/change a player's profile photo or select a cricket preset avatar.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddProfilePhotoDialog(
    playerName: String,
    currentProfile: PlayerProfile?,
    onDismiss: () -> Unit,
    onPhotoSelected: (Uri) -> Unit,
    onAvatarPresetSelected: (String) -> Unit,
    onRemovePhoto: () -> Unit
) {
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            onPhotoSelected(uri)
            onDismiss()
        }
    }

    val cricketIcons = listOf("🏏", "⚾", "👑", "🧤", "⚡", "🔥", "🎯", "🏆")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                PlayerAvatar(
                    name = playerName,
                    photoPath = currentProfile?.photoPath,
                    avatarIcon = currentProfile?.avatarIcon,
                    colorHex = currentProfile?.avatarColorHex ?: 0xFF1B5E20,
                    size = 42.dp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Player Profile Photo",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                    Text(
                        text = playerName,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = "Add or update the profile photo for $playerName. This will be stored and remembered for every match.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Option 1: Gallery Upload
                Button(
                    onClick = { galleryLauncher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Upload Photo from Gallery", fontWeight = FontWeight.Bold)
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                // Option 2: Cricket Preset Avatars
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Or Choose a Cricket Avatar Icon:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        cricketIcons.forEach { icon ->
                            val isSelected = currentProfile?.avatarIcon == icon
                            Surface(
                                shape = CircleShape,
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                                ),
                                modifier = Modifier
                                    .size(42.dp)
                                    .clickable {
                                        onAvatarPresetSelected(icon)
                                        onDismiss()
                                    }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(text = icon, fontSize = 20.sp)
                                }
                            }
                        }
                    }
                }

                // Option 3: Remove photo if set
                if (!currentProfile?.photoPath.isNullOrBlank() || !currentProfile?.avatarIcon.isNullOrBlank()) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    OutlinedButton(
                        onClick = {
                            onRemovePhoto()
                            onDismiss()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Remove Profile Photo", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Full directory dialog allowing the user to view all stored players in the app,
 * see their rectangle boxes with name and profile, and add/edit profiles anytime.
 */
@Composable
fun PlayerProfilesDirectoryDialog(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    profiles: List<PlayerProfile>,
    onRequestAddProfile: (playerName: String) -> Unit,
    onAddNewPlayer: (String) -> Unit
) {
    if (!isOpen) return

    var searchQuery by remember { mutableStateOf("") }
    var newPlayerName by remember { mutableStateOf("") }

    val filtered = remember(searchQuery, profiles) {
        if (searchQuery.trim().isEmpty()) profiles
        else profiles.filter { it.name.contains(searchQuery.trim(), ignoreCase = true) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Player Profiles Directory", fontWeight = FontWeight.Black, fontSize = 18.sp)
                    Text(
                        text = "${profiles.size} players stored in app",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Search bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search stored player...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Quick add new player
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newPlayerName,
                        onValueChange = { newPlayerName = it },
                        placeholder = { Text("New player name") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Button(
                        onClick = {
                            if (newPlayerName.trim().isNotEmpty()) {
                                onAddNewPlayer(newPlayerName.trim())
                                newPlayerName = ""
                            }
                        },
                        enabled = newPlayerName.trim().isNotEmpty(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add")
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                // Stored Player list
                if (filtered.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (searchQuery.isNotEmpty()) "No player matching '$searchQuery'" else "No players stored yet. Enter a player in any match!",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 350.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filtered, key = { it.id }) { prof ->
                            PlayerProfileRowBox(
                                name = prof.name,
                                profile = prof,
                                onAddOrEditProfile = { onRequestAddProfile(prof.name) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}
