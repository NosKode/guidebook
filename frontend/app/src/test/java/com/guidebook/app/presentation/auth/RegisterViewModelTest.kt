package com.guidebook.app.presentation.auth

import com.guidebook.app.data.remote.ApiResult
import com.guidebook.app.domain.model.AuthToken
import com.guidebook.app.domain.model.User
import com.guidebook.app.domain.model.UserRole
import com.guidebook.app.domain.usecase.auth.RegisterUseCase
import com.guidebook.app.utils.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RegisterViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val registerUseCase: RegisterUseCase = mockk()
    private lateinit var viewModel: RegisterViewModel

    private val fakeToken = AuthToken(
        token = "new-jwt",
        user = User("2", "new@example.com", "New User", UserRole.USER, "2024-01-01", null)
    )

    @Before
    fun setUp() {
        viewModel = RegisterViewModel(registerUseCase)
    }

    @Test
    fun `initial state is clean`() {
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.isSuccess)
        assertNull(state.error)
        assertNull(state.emailError)
        assertNull(state.passwordError)
    }

    @Test
    fun `register with blank email sets emailError`() {
        viewModel.register("", "password123", "Name")
        assertEquals("Email не может быть пустым", viewModel.uiState.value.emailError)
    }

    @Test
    fun `register with whitespace email sets emailError`() {
        viewModel.register("   ", "password123", "Name")
        assertEquals("Email не может быть пустым", viewModel.uiState.value.emailError)
    }

    @Test
    fun `register with short password sets passwordError`() {
        viewModel.register("new@example.com", "abc", "Name")
        assertEquals("Минимум 8 символов", viewModel.uiState.value.passwordError)
    }

    @Test
    fun `register with 7 char password sets passwordError`() {
        viewModel.register("new@example.com", "1234567", "Name")
        assertEquals("Минимум 8 символов", viewModel.uiState.value.passwordError)
    }

    @Test
    fun `register with 8 char password does not set passwordError`() = runTest {
        coEvery { registerUseCase(any(), any(), any()) } returns ApiResult.Success(fakeToken)
        viewModel.register("new@example.com", "12345678", "Name")
        assertNull(viewModel.uiState.value.passwordError)
    }

    @Test
    fun `register with both errors sets both fields`() {
        viewModel.register("", "ab", "")
        val state = viewModel.uiState.value
        assertNotNull(state.emailError)
        assertNotNull(state.passwordError)
    }

    @Test
    fun `register with validation errors does not call use case`() {
        viewModel.register("", "", "")
        coVerify(exactly = 0) { registerUseCase(any(), any(), any()) }
    }

    @Test
    fun `register success sets isSuccess to true`() = runTest {
        coEvery { registerUseCase(any(), any(), any()) } returns ApiResult.Success(fakeToken)
        viewModel.register("new@example.com", "password123", "Name")
        assertTrue(viewModel.uiState.value.isSuccess)
    }

    @Test
    fun `register success clears loading state`() = runTest {
        coEvery { registerUseCase(any(), any(), any()) } returns ApiResult.Success(fakeToken)
        viewModel.register("new@example.com", "password123", "Name")
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `register 409 returns email already taken message`() = runTest {
        coEvery { registerUseCase(any(), any(), any()) } returns ApiResult.Error(409, "Conflict")
        viewModel.register("new@example.com", "password123", "Name")
        assertEquals("Этот email уже занят", viewModel.uiState.value.error)
    }

    @Test
    fun `register network error sets network message`() = runTest {
        coEvery { registerUseCase(any(), any(), any()) } returns ApiResult.NetworkError
        viewModel.register("new@example.com", "password123", "Name")
        assertEquals("Нет подключения к интернету", viewModel.uiState.value.error)
    }

    @Test
    fun `register error clears loading state`() = runTest {
        coEvery { registerUseCase(any(), any(), any()) } returns ApiResult.Error(409, "Conflict")
        viewModel.register("new@example.com", "password123", "Name")
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `register with blank displayName passes null to use case`() = runTest {
        coEvery { registerUseCase("new@example.com", "password123", null) } returns ApiResult.Success(fakeToken)
        viewModel.register("new@example.com", "password123", "   ")
        coVerify { registerUseCase("new@example.com", "password123", null) }
    }

    @Test
    fun `register with whitespace-only displayName passes null to use case`() = runTest {
        coEvery { registerUseCase(any(), any(), null) } returns ApiResult.Success(fakeToken)
        viewModel.register("new@example.com", "password123", "  \t  ")
        coVerify { registerUseCase(any(), any(), null) }
    }

    @Test
    fun `register with non-empty displayName passes trimmed value to use case`() = runTest {
        coEvery { registerUseCase("new@example.com", "password123", "Alice") } returns ApiResult.Success(fakeToken)
        viewModel.register("new@example.com", "password123", "  Alice  ")
        coVerify { registerUseCase("new@example.com", "password123", "Alice") }
    }

    @Test
    fun `clearError removes error from state`() = runTest {
        coEvery { registerUseCase(any(), any(), any()) } returns ApiResult.Error(409, "Conflict")
        viewModel.register("new@example.com", "password123", "Name")
        assertNotNull(viewModel.uiState.value.error)
        viewModel.clearError()
        assertNull(viewModel.uiState.value.error)
    }
}
