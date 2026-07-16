package com.example.cpe3323_capstone_cookbook.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Success) onRegisterSuccess()
    }

    fun validate(): String? {
        if (name.isBlank()) return "Name is required"
        if (email.isBlank()) return "Email is required"
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) return "Enter a valid email address"
        if (password.length < 6) return "Password must be at least 6 characters"
        return null
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Create Account", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(16.dp))

        if (validationError != null) {
            Text(validationError!!, color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(8.dp))
        } else if (uiState is AuthUiState.Error) {
            Text((uiState as AuthUiState.Error).message, color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(8.dp))
        }

        Button(
            onClick = {
                val error = validate()
                validationError = error
                if (error == null) viewModel.register(name, email, password)
            },
            enabled = uiState !is AuthUiState.Loading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (uiState is AuthUiState.Loading) CircularProgressIndicator(modifier = Modifier.size(20.dp))
            else Text("Register")
        }
        TextButton(onClick = onNavigateToLogin) { Text("Already have an account? Log in") }
    }
}