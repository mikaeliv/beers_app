package ru.mikaeliv.beers.feature.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import beers.composeds.generated.resources.Res
import beers.composeds.generated.resources.auth_email_label
import beers.composeds.generated.resources.auth_login_button
import beers.composeds.generated.resources.auth_login_title
import beers.composeds.generated.resources.auth_password_label
import beers.composeds.generated.resources.auth_register_button
import beers.composeds.generated.resources.auth_register_title
import beers.composeds.generated.resources.auth_switch_to_login
import beers.composeds.generated.resources.auth_switch_to_register
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import org.jetbrains.compose.resources.stringResource

@Composable
fun AuthScreen(component: AuthComponent) {
    val state by component.state.subscribeAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = if (state.isLoginMode) {
                stringResource(Res.string.auth_login_title)
            } else {
                stringResource(Res.string.auth_register_title)
            },
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = state.email,
            onValueChange = component::onEmailChange,
            label = { Text(stringResource(Res.string.auth_email_label)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isLoading,
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = state.password,
            onValueChange = component::onPasswordChange,
            label = { Text(stringResource(Res.string.auth_password_label)) },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { component.onSubmit() }
            ),
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isLoading,
        )

        if (state.error != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = state.error!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (state.isLoading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = component::onSubmit,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = if (state.isLoginMode) {
                        stringResource(Res.string.auth_login_button)
                    } else {
                        stringResource(Res.string.auth_register_button)
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = component::switchMode) {
                Text(
                    text = if (state.isLoginMode) {
                        stringResource(Res.string.auth_switch_to_register)
                    } else {
                        stringResource(Res.string.auth_switch_to_login)
                    }
                )
            }
        }
    }
}
