package com.example.technewshub.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.technewshub.data.model.Article
import com.example.technewshub.di.NetworkModule
import com.example.technewshub.ui.screens.MainScreen
import com.example.technewshub.ui.screens.NewsDetailScreen
import com.example.technewshub.ui.screens.NewsListScreen
import com.example.technewshub.ui.screens.FavoritesScreen
import com.example.technewshub.ui.viewmodel.NewsViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

sealed class Screen(val route: String) {
    data object Main : Screen("main")
    data object NewsList : Screen("news_list")
    data object NewsDetail : Screen("news_detail/{articleUrl}") {
        fun createRoute(article: Article) = "news_detail/${Uri.encode(article.url)}"
    }
    data object Favorites : Screen("favorites")
}

@Composable
fun AppNavigation(navController: NavHostController) {
    val viewModel: NewsViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            return NewsViewModel(
                NetworkModule.newsApiService,
            ) as T
        }
    })

    NavHost(
        navController = navController,
        startDestination = Screen.Main.route
    ) {
        composable(Screen.Main.route) {
            MainScreen(
                viewModel = viewModel,
                onAllNewsClick = {
                    navController.navigate(Screen.NewsList.route)
                },
                onFavoritesClick = {
                    navController.navigate(Screen.Favorites.route)
                },
                onArticleClick = { article ->
                    navController.currentBackStackEntry?.savedStateHandle?.set("article", article)
                    navController.navigate(Screen.NewsDetail.route)
                }
            )
        }
        
        composable(Screen.NewsList.route) {
            NewsListScreen(
                viewModel = viewModel,
                onNewsClick = { article ->
                    navController.currentBackStackEntry?.savedStateHandle?.set("article", article)
                    navController.navigate(Screen.NewsDetail.route)
                },
                onNavigateHome = { navController.popBackStack() }
            )
        }
        
        composable(Screen.NewsDetail.route) { backStackEntry ->
            val article = navController.previousBackStackEntry?.savedStateHandle?.get<Article>("article")
            if (article != null) {
                NewsDetailScreen(
                    article = article,
                    viewModel = viewModel,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            } else {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Статья не найдена")
                }
            }
        }

        composable(Screen.Favorites.route) {
            FavoritesScreen(
                viewModel = viewModel,
                onArticleClick = { article ->
                    navController.currentBackStackEntry?.savedStateHandle?.set("article", article)
                    navController.navigate(Screen.NewsDetail.route)
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
} 