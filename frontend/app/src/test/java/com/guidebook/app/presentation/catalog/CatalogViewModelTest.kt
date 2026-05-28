package com.guidebook.app.presentation.catalog

import com.guidebook.app.data.remote.ApiResult
import com.guidebook.app.domain.model.Category
import com.guidebook.app.domain.model.PagedData
import com.guidebook.app.domain.model.Place
import com.guidebook.app.domain.model.PlaceStatus
import com.guidebook.app.domain.repository.CategoryRepository
import com.guidebook.app.domain.repository.PlaceRepository
import com.guidebook.app.utils.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
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
class CatalogViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(testDispatcher)

    private val placeRepository: PlaceRepository = mockk()
    private val categoryRepository: CategoryRepository = mockk()

    private val fakeCategories = listOf(
        Category(1, "Парки", null),
        Category(2, "Музеи", "Культурные места")
    )

    private fun makePlace(id: String, name: String = "Place $id") = Place(
        id = id, name = name, address = null, latitude = null, longitude = null,
        categoryId = null, categoryName = null, description = null, coverUrl = null,
        uploadedBy = null, status = PlaceStatus.APPROVED, rejectionReason = null,
        averageRating = 0.0, reviewsCount = 0, photosCount = 0, createdAt = "2024-01-01"
    )

    private fun pagedData(
        items: List<Place> = emptyList(),
        page: Int = 1,
        totalPages: Int = 1
    ) = PagedData(
        items = items, page = page, pageSize = 20, totalItems = items.size.toLong(), totalPages = totalPages
    )

    @Before
    fun setUp() {
        coEvery { categoryRepository.getCategories() } returns ApiResult.Success(fakeCategories)
        coEvery { placeRepository.getPlaces(any(), any(), any(), any(), any()) } returns ApiResult.Success(pagedData())
    }

    private fun createViewModel() = CatalogViewModel(placeRepository, categoryRepository)

    // ── Начальное состояние ───────────────────────────────────────────────────

    @Test
    fun `initial state has correct defaults before load`() {
        val vm = createViewModel()
        val state = vm.uiState.value
        assertNull(state.selectedCategoryId)
        assertEquals("", state.searchQuery)
        assertEquals(SortOption.NEWEST, state.sortOption)
        assertEquals(1, state.currentPage)
        assertNull(state.error)
    }

    @Test
    fun `init loads categories from repository`() = runTest(testDispatcher) {
        val vm = createViewModel()
        advanceUntilIdle()
        assertEquals(fakeCategories, vm.uiState.value.categories)
    }

    @Test
    fun `init loads places from repository`() = runTest(testDispatcher) {
        val places = listOf(makePlace("1"), makePlace("2"))
        coEvery { placeRepository.getPlaces(any(), any(), any(), any(), any()) } returns ApiResult.Success(pagedData(places))
        val vm = createViewModel()
        advanceUntilIdle()
        assertEquals(places, vm.uiState.value.places)
    }

    @Test
    fun `after init isLoading is false`() = runTest(testDispatcher) {
        val vm = createViewModel()
        advanceUntilIdle()
        assertFalse(vm.uiState.value.isLoading)
    }

    // ── Категории ─────────────────────────────────────────────────────────────

    @Test
    fun `filterByCategory sets selectedCategoryId and triggers reload`() = runTest(testDispatcher) {
        val vm = createViewModel()
        advanceUntilIdle()
        vm.filterByCategory(1)
        advanceUntilIdle()
        assertEquals(1, vm.uiState.value.selectedCategoryId)
    }

    @Test
    fun `filterByCategory resets page to 1`() = runTest(testDispatcher) {
        val vm = createViewModel()
        advanceUntilIdle()
        vm.filterByCategory(1)
        advanceUntilIdle()
        assertEquals(1, vm.uiState.value.currentPage)
    }

    @Test
    fun `filterByCategory with same id does not trigger reload`() = runTest(testDispatcher) {
        val vm = createViewModel()
        advanceUntilIdle()
        vm.filterByCategory(1)
        advanceUntilIdle()
        val callsAfterFirst = 2 // init + filterByCategory(1)
        vm.filterByCategory(1)  // same → should be ignored
        advanceUntilIdle()
        coVerify(exactly = callsAfterFirst) { placeRepository.getPlaces(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `filterByCategory with null clears selection`() = runTest(testDispatcher) {
        val vm = createViewModel()
        advanceUntilIdle()
        vm.filterByCategory(1)
        advanceUntilIdle()
        vm.filterByCategory(null)
        advanceUntilIdle()
        assertNull(vm.uiState.value.selectedCategoryId)
    }

    // ── Сортировка ────────────────────────────────────────────────────────────

    @Test
    fun `setSortOption updates sortOption in state`() = runTest(testDispatcher) {
        val vm = createViewModel()
        advanceUntilIdle()
        vm.setSortOption(SortOption.NAME_AZ)
        advanceUntilIdle()
        assertEquals(SortOption.NAME_AZ, vm.uiState.value.sortOption)
    }

    @Test
    fun `setSortOption with same option does not trigger reload`() = runTest(testDispatcher) {
        val vm = createViewModel()
        advanceUntilIdle()
        // Default is NEWEST — calling setSortOption(NEWEST) again must be a no-op
        vm.setSortOption(SortOption.NEWEST)
        advanceUntilIdle()
        coVerify(exactly = 1) { placeRepository.getPlaces(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `setSortOption resets page to 1`() = runTest(testDispatcher) {
        val vm = createViewModel()
        advanceUntilIdle()
        vm.setSortOption(SortOption.NAME_AZ)
        advanceUntilIdle()
        assertEquals(1, vm.uiState.value.currentPage)
    }

    // ── Поиск ─────────────────────────────────────────────────────────────────

    @Test
    fun `search updates searchQuery immediately`() = runTest(testDispatcher) {
        val vm = createViewModel()
        advanceUntilIdle()
        vm.search("coffee")
        assertEquals("coffee", vm.uiState.value.searchQuery)
    }

    @Test
    fun `search triggers reload after 300ms debounce`() = runTest(testDispatcher) {
        val vm = createViewModel()
        advanceUntilIdle()
        vm.search("park")
        advanceTimeBy(300)
        advanceUntilIdle()
        // 1 from init, 1 after debounce fires
        coVerify(atLeast = 2) { placeRepository.getPlaces(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `rapid search calls only fire once after debounce window`() = runTest(testDispatcher) {
        val vm = createViewModel()
        advanceUntilIdle()
        val callsAfterInit = 1
        vm.search("p")
        advanceTimeBy(100)
        vm.search("pa")
        advanceTimeBy(100)
        vm.search("par")
        advanceTimeBy(100)
        // 300ms haven't passed since last emit — debounce not yet fired
        coVerify(exactly = callsAfterInit) { placeRepository.getPlaces(any(), any(), any(), any(), any()) }
        advanceTimeBy(300)
        advanceUntilIdle()
        // Now only one extra call should have happened (for "par")
        coVerify(exactly = callsAfterInit + 1) { placeRepository.getPlaces(any(), any(), any(), any(), any()) }
    }

    // ── Пагинация ─────────────────────────────────────────────────────────────

    @Test
    fun `loadNextPage does nothing when already on last page`() = runTest(testDispatcher) {
        coEvery { placeRepository.getPlaces(any(), any(), any(), any(), any()) } returns
            ApiResult.Success(pagedData(page = 1, totalPages = 1))
        val vm = createViewModel()
        advanceUntilIdle()
        vm.loadNextPage()
        advanceUntilIdle()
        coVerify(exactly = 1) { placeRepository.getPlaces(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `loadNextPage appends places from second page`() = runTest(testDispatcher) {
        val firstPage = listOf(makePlace("1"), makePlace("2"))
        val secondPage = listOf(makePlace("3"), makePlace("4"))
        coEvery { placeRepository.getPlaces(any(), any(), any(), any(), any()) } returnsMany listOf(
            ApiResult.Success(pagedData(firstPage, page = 1, totalPages = 2)),
            ApiResult.Success(pagedData(secondPage, page = 2, totalPages = 2))
        )
        val vm = createViewModel()
        advanceUntilIdle()
        vm.loadNextPage()
        advanceUntilIdle()
        assertEquals(firstPage + secondPage, vm.uiState.value.places)
    }

    @Test
    fun `loadNextPage advances currentPage`() = runTest(testDispatcher) {
        coEvery { placeRepository.getPlaces(any(), any(), any(), any(), any()) } returnsMany listOf(
            ApiResult.Success(pagedData(listOf(makePlace("1")), page = 1, totalPages = 3)),
            ApiResult.Success(pagedData(listOf(makePlace("2")), page = 2, totalPages = 3))
        )
        val vm = createViewModel()
        advanceUntilIdle()
        vm.loadNextPage()
        advanceUntilIdle()
        assertEquals(2, vm.uiState.value.currentPage)
    }

    // ── Обновление (refresh) ──────────────────────────────────────────────────

    @Test
    fun `refresh replaces places list instead of appending`() = runTest(testDispatcher) {
        val initial = listOf(makePlace("1"), makePlace("2"))
        val refreshed = listOf(makePlace("3"))
        coEvery { placeRepository.getPlaces(any(), any(), any(), any(), any()) } returnsMany listOf(
            ApiResult.Success(pagedData(initial, page = 1, totalPages = 1)),
            ApiResult.Success(pagedData(refreshed, page = 1, totalPages = 1))
        )
        val vm = createViewModel()
        advanceUntilIdle()
        vm.refresh()
        advanceUntilIdle()
        assertEquals(refreshed, vm.uiState.value.places)
    }

    @Test
    fun `refresh resets currentPage to 1`() = runTest(testDispatcher) {
        val vm = createViewModel()
        advanceUntilIdle()
        vm.refresh()
        advanceUntilIdle()
        assertEquals(1, vm.uiState.value.currentPage)
    }

    // ── Обработка ошибок ──────────────────────────────────────────────────────

    @Test
    fun `places load error sets error message in state`() = runTest(testDispatcher) {
        coEvery { placeRepository.getPlaces(any(), any(), any(), any(), any()) } returns
            ApiResult.Error(500, "Server error")
        val vm = createViewModel()
        advanceUntilIdle()
        assertNotNull(vm.uiState.value.error)
    }

    @Test
    fun `places network error sets Russian network message`() = runTest(testDispatcher) {
        coEvery { placeRepository.getPlaces(any(), any(), any(), any(), any()) } returns ApiResult.NetworkError
        val vm = createViewModel()
        advanceUntilIdle()
        assertEquals("Нет подключения к интернету", vm.uiState.value.error)
    }

    @Test
    fun `categories load failure does not crash viewmodel`() = runTest(testDispatcher) {
        coEvery { categoryRepository.getCategories() } returns ApiResult.Error(500, "err")
        coEvery { placeRepository.getPlaces(any(), any(), any(), any(), any()) } returns ApiResult.Success(pagedData())
        val vm = createViewModel()
        advanceUntilIdle()
        // Categories are non-critical — ViewModel should stay alive
        assertTrue(vm.uiState.value.categories.isEmpty())
        assertFalse(vm.uiState.value.isLoading)
    }

    @Test
    fun `successful load clears previous error`() = runTest(testDispatcher) {
        coEvery { placeRepository.getPlaces(any(), any(), any(), any(), any()) } returnsMany listOf(
            ApiResult.NetworkError,
            ApiResult.Success(pagedData(listOf(makePlace("1"))))
        )
        val vm = createViewModel()
        advanceUntilIdle()
        assertNotNull(vm.uiState.value.error)
        vm.refresh()
        advanceUntilIdle()
        assertNull(vm.uiState.value.error)
    }
}
