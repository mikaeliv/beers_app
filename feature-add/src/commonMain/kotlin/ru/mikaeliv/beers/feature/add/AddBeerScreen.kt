package ru.mikaeliv.beers.feature.add

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import beers.composeds.generated.resources.Res
import beers.composeds.generated.resources.add_beer_abv_label
import beers.composeds.generated.resources.add_beer_comment_label
import beers.composeds.generated.resources.add_beer_name_label
import beers.composeds.generated.resources.add_beer_rating_label
import beers.composeds.generated.resources.add_beer_title
import beers.composeds.generated.resources.save
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import org.jetbrains.compose.resources.stringResource
import ru.mikaeliv.beers.composeDS.components.BeersTopAppBar
import ru.mikaeliv.beers.composeDS.components.StarRating

@Composable
fun AddBeerScreen(component: AddBeerComponent) {
    val state by component.state.subscribeAsState()
    Scaffold(
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
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = state.name,
                onValueChange = component::onNameChange,
                label = { Text(stringResource(Res.string.add_beer_name_label)) },
                singleLine = true
            )
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = state.abv,
                onValueChange = { raw ->
                    // Разрешаем только цифры и один разделитель ('.' или ',').
                    // Нормализуем ',' -> '.' чтобы дальше корректно парсилось через toDoubleOrNull().
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
                label = { Text(stringResource(Res.string.add_beer_abv_label)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(stringResource(Res.string.add_beer_rating_label), style = MaterialTheme.typography.bodyLarge)
                StarRating(
                    rating = state.rating,
                    starSize = 32.dp,
                    onRatingChange = component::onRatingChange
                )
            }
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = state.comment,
                onValueChange = component::onCommentChange,
                label = { Text(stringResource(Res.string.add_beer_comment_label)) },
                minLines = 3
            )

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = component::onSave,
                enabled = state.isValid && !state.isSaving
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(end = 12.dp),
                        strokeWidth = 2.dp
                    )
                }
                Text(stringResource(Res.string.save))
            }
        }
    }
}
