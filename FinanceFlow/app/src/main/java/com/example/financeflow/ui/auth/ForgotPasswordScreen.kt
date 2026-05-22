package com.example.financeflow.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.financeflow.viewmodel.auth.AuthUiState

private val PrimaryPurple = Color(0xFF7C4DFF)
private val TextDark = Color(0xFF1A1A1A)
private val TextHint = Color(0xFFAAAAAA)

@Composable
fun ForgotPasswordScreen(
    authState: AuthUiState = AuthUiState(),
    onSendResetEmail: (String) -> Unit = {},
    onVerify: () -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Reset password",
                fontSize = 30.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextDark
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = "Enter your account email and we will send a secure reset link.",
                fontSize = 14.sp,
                color = TextHint,
                lineHeight = 20.sp
            )

            Spacer(Modifier.height(28.dp))

            AuthTextField(
                value = email,
                onValueChange = {
                    email = it.trim()
                    emailError = null
                },
                placeholder = "Enter your email",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                isError = emailError != null,
                supportingText = emailError,
                trailingIcon = {
                    Icon(
                        Icons.Default.Mail,
                        contentDescription = null,
                        tint = TextHint
                    )
                }
            )

            authState.errorMessage?.let {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = it,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.error
                )
            }

            if (authState.passwordResetSent) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Reset email sent. Check your inbox.",
                    fontSize = 12.sp,
                    color = PrimaryPurple
                )
            }

            Spacer(Modifier.height(28.dp))

            AuthPrimaryButton(
                text = if (authState.isLoading) "Sending..." else "Send reset email",
                enabled = !authState.isLoading,
                onClick = {
                    emailError = validateResetEmail(email)
                    if (emailError == null) {
                        onSendResetEmail(email)
                    }
                }
            )

            Spacer(Modifier.height(14.dp))

            AuthPrimaryButton(
                text = "Back to login",
                enabled = !authState.isLoading,
                onClick = onVerify
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ForgotPasswordScreenPreview() {
    MaterialTheme {
        ForgotPasswordScreen()
    }
}

private fun validateResetEmail(email: String): String? {
    if (email.isBlank()) return "Email is required"
    return if (android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) null else "Enter a valid email"
}
