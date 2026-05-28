package com.guidebook.app.presentation.auth

import com.guidebook.app.data.remote.ApiResult
import com.guidebook.app.domain.model.AuthToken
import com.guidebook.app.domain.model.User
import com.guidebook.app.domain.model.UserRole
import com.guidebook.app.domain.usecase.auth.LoginUseCase
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
class LoginViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val loginUseCase: LoginUseCase = mockk()
    private lateinit var viewModel: LoginViewModel

    private val fakeToken = AuthToken(
        token = "jwt-token",
        user = User("1", "test@example.com", "Test", UserRole.USER, "2024-01-01", null)
    )

    @Before
    fun setUp() {
        viewModel = LoginViewModel(loginUseCase)
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
    fun `login with blank email sets emailError`() {
        viewModel.login("", "password123")
        assertEquals("Email не может быть пустым", viewModel.uiState.value.emailError)
    }

    @Test
    fun `login with whitespace-only email sets emailError`() {
        viewModel.login("   ", "password123")
        assertEquals("Email не может быть пустым", viewModel.uiState.value.emailError)
    }

    @Test
    fun `login with password shorter than 8 chars sets passwordError`() {
        viewModel.login("test@example.com", "short")
        assertEquals("Минимум 8 символов", viewModel.uiState.value.passwordError)
    }

    @Test
    fun `login with both blank email and short password sets both errors`() {
        viewModel.login("", "abc")
        val state = viewModel.uiState.value
        assertNotNull(state.emailError)
        assertNotNull(state.passwordError)
    }

    @Test
    fun `login with validation errors does not call use case`() {
        viewModel.login("", "")
        coVerify(exactly = 0) { loginUseCase(any(), any()) }
    }

    @Test
    fun `login success sets isSuccess to true`() = runTest {
        coEvery { loginUseCase(any(), any()) } returns ApiResult.Success(fakeToken)
        viewModel.login("test@example.com", "password123")
        assertTrue(viewModel.uiState.value.isSuccess)
    }

    @Test
    fun `login success clears loading state`() = runTest {
        coEvery { loginUseCase(any(), any()) } returns ApiResult.Success(fakeToken)
        viewModel.login("test@example.com", "password123")
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `login success clears validation errors`() = runTest {
        coEvery { loginUseCase(any(), any()) } returns ApiResult.Success(fakeToken)
        viewModel.login("test@example.com", "password123")
        val state = viewModel.uiState.value
        assertNull(state.emailError)
        assertNull(state.passwordError)
    }

    @Test
    fun `login 401 sets Russian error message`() = runTest {
        coEvery { loginUseCase(any(), any()) } returns ApiResult.Error(401, "Unauthorized")
        viewModel.login("test@example.com", "password123")
        assertEquals("Неверный email или пароль", viewModel.uiState.value.error)
    }

    @Test
    fun `login 500 sets server error message`() = runTest {
        coEvery { loginUseCase(any(), any()) } returns ApiResult.Error(500, "Internal Server Error")
        viewModel.login("test@example.com", "password123")
        assertEquals("Ошибка сервера. Попробуйте позже", viewModel.uiState.value.error)
    }

    @Test
    fun `login network error sets network message`() = runTest {
        coEvery { loginUseCase(any(), any()) } returns ApiResult.NetworkError
        viewModel.login("test@example.com", "password123")
        assertEquals("Нет подключения к интернету", viewModel.uiState.value.error)
    }

    @Test
    fun `login error clears loading state`() = runTest {
        coEvery { loginUseCase(any(), any()) } returns ApiResult.Error(500, "err")
        viewModel.login("test@example.com", "password123")
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `clearError removes error from state`() = runTest {
        coEvery { loginUseCase(any(), any()) } returns ApiResult.Error(401, "Unauthorized")
        viewModel.login("test@example.com", "password123")
        assertNotNull(viewModel.uiState.value.error)
        viewModel.clearError()
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `login calls use case with provided credentials`() = runTest {
        coEvery { loginUseCase("user@mail.com", "securepass") } returns ApiResult.Success(fakeToken)
        viewModel.login("user@mail.com", "securepass")
        coVerify(exactly = 1) { loginUseCase("user@mail.com", "securepass") }
    }
}
