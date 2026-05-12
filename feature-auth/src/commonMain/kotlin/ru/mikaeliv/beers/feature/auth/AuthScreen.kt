package ru.mikaeliv.beers.feature.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import beers.composeds.generated.resources.Res
import beers.composeds.generated.resources.auth_email_label
import beers.composeds.generated.resources.auth_confirm_password_label
import beers.composeds.generated.resources.auth_confirm_password_placeholder
import beers.composeds.generated.resources.auth_error_fill_fields
import beers.composeds.generated.resources.auth_login_password_placeholder
import beers.composeds.generated.resources.auth_login_button
import beers.composeds.generated.resources.auth_login_subtitle
import beers.composeds.generated.resources.auth_login_title
import beers.composeds.generated.resources.auth_login_username_placeholder
import beers.composeds.generated.resources.auth_password_label
import beers.composeds.generated.resources.auth_register_password_placeholder
import beers.composeds.generated.resources.auth_register_button
import beers.composeds.generated.resources.auth_register_subtitle
import beers.composeds.generated.resources.auth_register_title
import beers.composeds.generated.resources.auth_register_username_placeholder
import beers.composeds.generated.resources.auth_switch_to_login
import beers.composeds.generated.resources.auth_switch_to_register
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import org.jetbrains.compose.resources.stringResource
import ru.mikaeliv.beers.composeDS.components.BeerLogo
import ru.mikaeliv.beers.composeDS.components.BeersButton
import ru.mikaeliv.beers.composeDS.components.BeersPillTextField
import ru.mikaeliv.beers.composeDS.icons.VisibilityIcon
import ru.mikaeliv.beers.composeDS.icons.VisibilityOffIcon

@Composable
fun AuthScreen(component: AuthComponent) {
    val state by component.state.subscribeAsState()
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPassword by remember(state.isLoginMode) { mutableStateOf("") }
    val canSubmit = !state.isLoading &&
        state.email.isNotBlank() &&
        state.password.isNotBlank() &&
        (state.isLoginMode || confirmPassword == state.password)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 32.dp, vertical = 56.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        BeerLogo(size = 112.dp)
        Spacer(modifier = Modifier.height(34.dp))
        Text(
            text = if (state.isLoginMode) stringResource(Res.string.auth_login_title) else stringResource(Res.string.auth_register_title),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )
        Text(
            text = if (state.isLoginMode) stringResource(Res.string.auth_login_subtitle) else stringResource(Res.string.auth_register_subtitle),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 12.dp)
        )

        Spacer(modifier = Modifier.height(54.dp))

        AuthFieldLabel(stringResource(Res.string.auth_email_label))
        BeersPillTextField(
            value = state.email,
            onValueChange = component::onEmailChange,
            placeholder = if (state.isLoginMode) stringResource(Res.string.auth_login_username_placeholder) else stringResource(Res.string.auth_register_username_placeholder),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isLoading,
        )

        Spacer(modifier = Modifier.height(28.dp))

        AuthFieldLabel(stringResource(Res.string.auth_password_label))
        BeersPillTextField(
            value = state.password,
            onValueChange = component::onPasswordChange,
            placeholder = if (state.isLoginMode) stringResource(Res.string.auth_login_password_placeholder) else stringResource(Res.string.auth_register_password_placeholder),
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = if (passwordVisible) KeyboardType.Text else KeyboardType.Password,
                imeAction = if (state.isLoginMode) ImeAction.Done else ImeAction.Next
            ),
            keyboardActions = KeyboardActions(onDone = { if (canSubmit) component.onSubmit() }),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    if (passwordVisible) {
                        VisibilityOffIcon(tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        VisibilityIcon(tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isLoading,
        )

        if (!state.isLoginMode) {
            Spacer(modifier = Modifier.height(28.dp))
            AuthFieldLabel(stringResource(Res.string.auth_confirm_password_label))
            BeersPillTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                placeholder = stringResource(Res.string.auth_confirm_password_placeholder),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { if (canSubmit) component.onSubmit() }),
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading,
            )
        }

        if (state.error != null) {
            Text(
                text = when (val error = state.error) {
                    AuthError.FillFields -> stringResource(Res.string.auth_error_fill_fields)
                    is AuthError.Message -> error.value
                    null -> ""
                },
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 18.dp),
                textAlign = TextAlign.Center,
            )
        }

        Spacer(modifier = Modifier.height(44.dp))

        if (state.isLoading) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        } else {
            BeersButton(
                text = if (state.isLoginMode) stringResource(Res.string.auth_login_button) else stringResource(Res.string.auth_register_button),
                onClick = component::onSubmit,
                enabled = canSubmit,
                modifier = Modifier.fillMaxWidth(),
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        }

        Spacer(modifier = Modifier.height(26.dp))

        TextButton(onClick = component::switchMode, enabled = !state.isLoading) {
            Text(
                text = if (state.isLoginMode) stringResource(Res.string.auth_switch_to_register) else stringResource(Res.string.auth_switch_to_login),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AuthFieldLabel(text: String) {
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 2.dp, bottom = 12.dp),
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.82f),
        style = MaterialTheme.typography.labelLarge,
    )
}
