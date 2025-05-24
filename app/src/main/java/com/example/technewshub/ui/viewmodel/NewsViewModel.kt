package com.example.technewshub.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.technewshub.data.api.NewsApiService
import com.example.technewshub.data.model.Article
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NewsViewModel(
    private val newsApiService: NewsApiService,
) : ViewModel() {
    private val _uiState = MutableStateFlow<NewsUiState>(NewsUiState.Loading)
    val uiState: StateFlow<NewsUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow(NewsCategory.TECHNOLOGY)
    val selectedCategory: StateFlow<NewsCategory> = _selectedCategory.asStateFlow()

    private val _favoriteArticles = MutableStateFlow<Set<Article>>(emptySet())
    val favoriteArticles: StateFlow<Set<Article>> = _favoriteArticles.asStateFlow()

    private val _selectedSortBy = MutableStateFlow(NewsSortBy.PUBLISHED_AT)
    val selectedSortBy: StateFlow<NewsSortBy> = _selectedSortBy.asStateFlow()

    private var currentPage = 1
    private var isLoadingMore = false
    private var hasMorePages = true

    fun toggleFavorite(article: Article) {
        val currentFavorites = _favoriteArticles.value.toMutableSet()
        if (currentFavorites.contains(article)) {
            currentFavorites.remove(article)
        } else {
            currentFavorites.add(article)
        }
        _favoriteArticles.value = currentFavorites
    }

    fun isFavorite(article: Article): Boolean {
        return _favoriteArticles.value.contains(article)
    }

    fun setCategory(category: NewsCategory) {
        _selectedCategory.value = category
        currentPage = 1
        hasMorePages = true
        loadNews("98ea5308d8064992b16870b857f734b2")
    }

    fun setSortBy(sortBy: NewsSortBy) {
        _selectedSortBy.value = sortBy
        currentPage = 1
        hasMorePages = true
        if (_searchQuery.value.isBlank()) {
            loadNews("98ea5308d8064992b16870b857f734b2")
        }
    }

    fun loadNews(apiKey: String) {
        if (isLoadingMore) return
        isLoadingMore = true
        
        viewModelScope.launch {
            try {
                if (currentPage == 1) {
                    _uiState.value = NewsUiState.Loading
                }
                
                val response = if (_selectedCategory.value == NewsCategory.ALL) {
                    newsApiService.getTopHeadlines(
                        apiKey = apiKey,
                        language = "en",
                        pageSize = 20
                    )
                } else {
                    newsApiService.searchNews(
                        query = _selectedCategory.value.query,
                        apiKey = apiKey,
                        language = "en",
                        sortBy = _selectedSortBy.value.query,
                        pageSize = 20
                    )
                }
                
                val currentArticles = if (_uiState.value is NewsUiState.Success) {
                    (uiState.value as NewsUiState.Success).articles
                } else {
                    emptyList()
                }
                
                val newArticles = if (currentPage == 1) {
                    response.articles
                } else {
                    currentArticles + response.articles
                }
                
                hasMorePages = response.articles.isNotEmpty()
                _uiState.value = NewsUiState.Success(newArticles)
                currentPage++
            } catch (e: Exception) {
                _uiState.value = NewsUiState.Error(e.message ?: "Unknown error occurred")
            } finally {
                isLoadingMore = false
            }
        }
    }

    fun searchNews(query: String, apiKey: String) {
        _searchQuery.value = query
        currentPage = 1
        hasMorePages = true
        viewModelScope.launch {
            try {
                _uiState.value = NewsUiState.Loading
                val response = newsApiService.searchNews(
                    query = query,
                    apiKey = apiKey,
                    language = "en",
                    sortBy = _selectedSortBy.value.query,
                    pageSize = 20
                )
                _uiState.value = NewsUiState.Success(response.articles)
                currentPage++
                hasMorePages = response.articles.isNotEmpty()
            } catch (e: Exception) {
                _uiState.value = NewsUiState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    fun loadMoreNews(apiKey: String) {
        if (!isLoadingMore && hasMorePages) {
            loadNews(apiKey)
        }
    }
}

sealed class NewsUiState {
    data object Loading : NewsUiState()
    data class Success(val articles: List<Article>) : NewsUiState()
    data class Error(val message: String) : NewsUiState()
}

enum class NewsCategory(val query: String, val displayName: String) {
    ALL("", "📰 Все новости"),
    TECHNOLOGY("technology", "💻 Технологии"),
    AI("artificial intelligence", "🧠 AI"),
    BUSINESS("business technology", "📈 Бизнес"),
    SCIENCE("science technology", "🔬 Наука")
}

enum class NewsSortBy(val query: String, val displayName: String) {
    PUBLISHED_AT("publishedAt", "По дате"),
    POPULARITY("popularity", "По популярности"),
    RELEVANCY("relevancy", "По релевантности")
} 