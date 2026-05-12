package ru.mikaeliv.beers.feature.add

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import beers.composeds.generated.resources.Res
import beers.composeds.generated.resources.add_beer_abv_label
import beers.composeds.generated.resources.add_beer_abv_placeholder
import beers.composeds.generated.resources.add_beer_choose_gallery_subtitle
import beers.composeds.generated.resources.add_beer_choose_gallery_title
import beers.composeds.generated.resources.add_beer_comment_label
import beers.composeds.generated.resources.add_beer_comment_placeholder
import beers.composeds.generated.resources.add_beer_name_label
import beers.composeds.generated.resources.add_beer_name_placeholder
import beers.composeds.generated.resources.add_beer_photo_label
import beers.composeds.generated.resources.add_beer_photo_sheet_title
import beers.composeds.generated.resources.add_beer_rating_label
import beers.composeds.generated.resources.add_beer_take_photo_subtitle
import beers.composeds.generated.resources.add_beer_take_photo_title
import beers.composeds.generated.resources.add_beer_tap_photo
import beers.composeds.generated.resources.add_beer_title
import beers.composeds.generated.resources.ic_add
import beers.composeds.generated.resources.save
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import ru.mikaeliv.beers.composeDS.components.BeerPhoto
import ru.mikaeliv.beers.composeDS.components.BeersButton
import ru.mikaeliv.beers.composeDS.components.BeersPillTextField
import ru.mikaeliv.beers.composeDS.components.BeersTopAppBar
import ru.mikaeliv.beers.composeDS.components.RoundIconSurface
import ru.mikaeliv.beers.composeDS.components.StarRating
import ru.mikaeliv.beers.feature.camera.CameraComponent
import ru.mikaeliv.beers.feature.camera.CameraScreen
import ru.mikaeliv.beers.feature.camera.DefaultCameraComponent
import ru.mikaeliv.beers.feature.camera.isCustomCameraAvailable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBeerScreen(component: AddBeerComponent) {
    val state by component.state.subscribeAsState()
    val imagePicker = rememberImagePicker(component::onPhotoSelected)
    val isCameraAvailable = remember { isCustomCameraAvailable() }
    var showPhotoSourceSheet by remember { mutableStateOf(false) }
    var showCamera by remember { mutableStateOf(false) }
    val cameraComponent = remember(component) {
        DefaultCameraComponent(
            object : CameraComponent.Output {
                override fun back() {
                    showCamera = false
                }

                override fun photoCaptured(bytes: ByteArray) {
                    component.onPhotoSelected(bytes)
                    showCamera = false
                }
            }
        )
    }

    if (showCamera) {
        CameraScreen(cameraComponent)
        return
    }

    if (showPhotoSourceSheet) {
        ModalBottomSheet(
            onDismissRequest = { showPhotoSourceSheet = false },
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            PhotoSourceSheet(
                onCameraClick = {
                    showPhotoSourceSheet = false
                    showCamera = true
                },
                onGalleryClick = {
                    showPhotoSourceSheet = false
                    imagePicker.launch()
                }
            )
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            BeersTopAppBar(
                title = stringResource(Res.string.add_beer_title),
                onBack = component::onBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(start = 24.dp, end = 24.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(26.dp)
        ) {
            PhotoPickerCard(
                photoBytes = state.photoBytes,
                onClick = {
                    if (isCameraAvailable) {
                        showPhotoSourceSheet = true
                    } else {
                        imagePicker.launch()
                    }
                },
                enabled = !state.isSaving
            )

            LabeledField(stringResource(Res.string.add_beer_name_label)) {
                BeersPillTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = state.name,
                    onValueChange = component::onNameChange,
                    placeholder = stringResource(Res.string.add_beer_name_placeholder),
                    singleLine = true,
                    enabled = !state.isSaving
                )
            }

            LabeledField(stringResource(Res.string.add_beer_abv_label)) {
                BeersPillTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = state.abv,
                    onValueChange = { raw ->
                        val cleaned = buildString {
                            var hasSeparator = false
                            for (ch in raw) {
                                when {
                                    ch.isDigit() -> append(ch)
                                    (ch == '.' || ch == ',') && !hasSeparator -> {
                                        append('.')
                                        hasSeparator = true
                                    }
                                }
                            }
                        }
                        component.onAbvChange(cleaned)
                    },
                    placeholder = stringResource(Res.string.add_beer_abv_placeholder),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    enabled = !state.isSaving
                )
            }

            LabeledField(stringResource(Res.string.add_beer_rating_label).trimEnd(':')) {
                StarRating(
                    rating = state.rating,
                    starSize = 40.dp,
                    onRatingChange = component::onRatingChange,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }

            LabeledField(stringResource(Res.string.add_beer_comment_label)) {
                BeersPillTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = state.comment,
                    onValueChange = component::onCommentChange,
                    placeholder = stringResource(Res.string.add_beer_comment_placeholder),
                    singleLine = false,
                    minLines = 4,
                    enabled = !state.isSaving
                )
            }

            BeersButton(
                text = stringResource(Res.string.save),
                onClick = component::onSave,
                enabled = state.isValid && !state.isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 36.dp),
                leadingIcon = if (state.isSaving) {
                    {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                } else {
                    null
                }
            )
        }
    }
}

@Composable
private fun PhotoSourceSheet(
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 12.dp, end = 12.dp, bottom = 28.dp),
    ) {
        Text(
            text = stringResource(Res.string.add_beer_photo_sheet_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp)
        )
        PhotoSourceItem(
            title = stringResource(Res.string.add_beer_take_photo_title),
            subtitle = stringResource(Res.string.add_beer_take_photo_subtitle),
            onClick = onCameraClick
        )
        PhotoSourceItem(
            title = stringResource(Res.string.add_beer_choose_gallery_title),
            subtitle = stringResource(Res.string.add_beer_choose_gallery_subtitle),
            onClick = onGalleryClick
        )
    }
}

@Composable
private fun PhotoSourceItem(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    ListItem(
        headlineContent = {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
        },
        supportingContent = {
            Text(text = subtitle, style = MaterialTheme.typography.bodyMedium)
        },
        leadingContent = {
            RoundIconSurface(size = 44.dp) {
                androidx.compose.foundation.Image(
                    painter = painterResource(Res.drawable.ic_add),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurfaceVariant),
                    modifier = Modifier.size(22.dp)
                )
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
    )
}

@Composable
private fun PhotoPickerCard(
    photoBytes: ByteArray?,
    onClick: () -> Unit,
    enabled: Boolean,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .clip(RoundedCornerShape(32.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(interactionSource = interactionSource, indication = null, enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (photoBytes != null) {
            BeerPhoto(
                photoBytes = photoBytes,
                contentDescription = stringResource(Res.string.add_beer_photo_label),
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(18.dp)) {
                RoundIconSurface(size = 76.dp) {
                    androidx.compose.foundation.Image(
                        painter = painterResource(Res.drawable.ic_add),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurfaceVariant),
                        modifier = Modifier.size(34.dp)
                    )
                }
                Text(
                    text = stringResource(Res.string.add_beer_tap_photo),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun LabeledField(
    label: String,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.82f)
        )
        content()
    }
}
