package com.example.technewshub.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.technewshub.data.model.Article
import com.example.technewshub.ui.viewmodel.NewsCategory
import com.example.technewshub.ui.viewmodel.NewsViewModel
import com.example.technewshub.utils.ThemeManager
import kotlinx.coroutines.launch
import androidx.compose.runtime.remember

@Composable
fun MainScreen(
    viewModel: NewsViewModel,
    onAllNewsClick: () -> Unit,
    onFavoritesClick: () -> Unit,
    onArticleClick: (Article) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val articles = when (uiState) {
        is com.example.technewshub.ui.viewmodel.NewsUiState.Success -> (uiState as com.example.technewshub.ui.viewmodel.NewsUiState.Success).articles
        else -> emptyList()
    }

    val context = LocalContext.current
    val themeManager = remember { ThemeManager(context) }
    val isDarkTheme by themeManager.isDarkTheme.collectAsState(initial = false)
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Добро пожаловать в TechNews Hub!",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.weight(1f, fill = false)
            )
            IconButton(onClick = {
                coroutineScope.launch {
                    themeManager.setDarkTheme(!isDarkTheme)
                }
            }) {
                Text(
                    text = if (isDarkTheme) "🌙" else "☀️",
                    style = MaterialTheme.typography.headlineMedium
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.horizontalScroll(rememberScrollState())
        ) {
            NewsCategory.values().forEach { category ->
                FilterChip(
                    selected = category == selectedCategory,
                    onClick = { viewModel.setCategory(category) },
                    label = { Text(category.displayName) }
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onAllNewsClick,
                modifier = Modifier.weight(1f)
            ) {
                Text("Все новости")
            }
            
            Button(
                onClick = onFavoritesClick,
                modifier = Modifier.weight(1f)
            ) {
                Text("Избранное")
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Свежие статьи",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))

        when (uiState) {
            is com.example.technewshub.ui.viewmodel.NewsUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is com.example.technewshub.ui.viewmodel.NewsUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = (uiState as com.example.technewshub.ui.viewmodel.NewsUiState.Error).message,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            is com.example.technewshub.ui.viewmodel.NewsUiState.Success -> {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.heightIn(max = 300.dp)
                ) {
                    articles.take(3).forEach { article ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onArticleClick(article) },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(8.dp)
                            ) {
                                AsyncImage(
                                    model = article.urlToImage,
                                    contentDescription = article.title,
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = article.title,
                                    style = MaterialTheme.typography.bodyLarge,
                                    maxLines = 2
                                )
                            }
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onAllNewsClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Перейти ко всем новостям")
        }
    }
} 