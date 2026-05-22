package com.example.financeflow.ui.dashboard

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.financeflow.navigation.Routes
import com.example.financeflow.ui.components.Home.BottomNavigationBar
import com.example.financeflow.ui.expenses.ExpensesScreen
import com.example.financeflow.ui.goals.GoalsScreen
import com.example.financeflow.ui.income.IncomeScreen
import com.example.financeflow.ui.insights.DailyReportScreen
import com.example.financeflow.ui.insights.InsightsScreen
import com.example.financeflow.ui.insights.MonthlyReportScreen
import com.example.financeflow.ui.insights.WeeklyReportScreen
import com.example.financeflow.ui.profile.ProfileScreen
import com.example.financeflow.ui.savings.AddSavingScreen
import com.example.financeflow.ui.savings.GoalDetailsScreen
import com.example.financeflow.ui.savings.SavingsScreen

@Composable
fun DashboardScreen(
    rootNavController: NavHostController,
    isDarkTheme: Boolean,
    onThemeToggle: () -> Unit,
    userName: String,
    onLogout: () -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    fun navigateDashboardTab(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            BottomNavigationBar(
                currentDestination = currentDestination,
                onItemClick = { item ->
                    navigateDashboardTab(item.route)
                }
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            enterTransition = {
                fadeIn(animationSpec = tween(180)) + slideInHorizontally { it / 8 }
            },
            exitTransition = {
                fadeOut(animationSpec = tween(120)) + slideOutHorizontally { -it / 12 }
            },
            popEnterTransition = {
                fadeIn(animationSpec = tween(180)) + slideInHorizontally { -it / 8 }
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(120)) + slideOutHorizontally { it / 12 }
            },
            modifier = Modifier
                .padding(bottom = innerPadding.calculateBottomPadding())
                .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Top))
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    userName = userName,
                    onAddIncomeClick = { rootNavController.navigate(Routes.ADD_INCOME) },
                    onAddExpenseClick = { navigateDashboardTab(Routes.EXPENSES) },
                    onIncomeClick = { navigateDashboardTab(Routes.INCOME) },
                    onGoalsClick = { navigateDashboardTab(Routes.GOALS) },
                    onExpensesClick = { navigateDashboardTab(Routes.EXPENSES) },
                    onSavingsClick = { navigateDashboardTab(Routes.SAVINGS) },
                    onGoalCardClick = { navigateDashboardTab(Routes.GOALS) },
                    onThemeClick = onThemeToggle,
                    onProfileClick = { navController.navigate(Routes.PROFILE) }
                )
            }

            composable(Routes.INCOME) {
                IncomeScreen(navController = rootNavController)
            }

            composable(Routes.EXPENSES) {
                ExpensesScreen()
            }

            composable(Routes.SAVINGS) {
                SavingsScreen(navController = rootNavController)
            }

            composable(Routes.GOALS) {
                GoalsScreen(
                    onNavigateToDetail = { }
                )
            }

            composable(Routes.INSIGHTS) {
                InsightsScreen(
                    onViewReports = { navController.navigate(Routes.DAILY_REPORT) }
                )
            }

            composable(Routes.PROFILE) {
                ProfileScreen(
                    isDarkTheme = isDarkTheme,
                    onThemeToggle = onThemeToggle,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToLogout = onLogout
                )
            }

            composable(Routes.GOAL_DETAILS) {
                GoalDetailsScreen(
                    onAddContribution = { navController.navigate(Routes.ADD_SAVING) }
                )
            }

            composable(Routes.ADD_SAVING) {
                AddSavingScreen(onNavigateBack = { navController.popBackStack() })
            }

            composable(Routes.DAILY_REPORT) {
                DailyReportScreen(
                    onNavigateUp = { navController.popBackStack() },
                    onClose = { navController.popBackStack(Routes.INSIGHTS, false) },
                    onTabSelected = { tab ->
                        when (tab) {
                            "Weekly" -> navController.navigate(Routes.WEEKLY_REPORT) {
                                popUpTo(Routes.DAILY_REPORT) { inclusive = true }
                            }
                            "Monthly" -> navController.navigate(Routes.MONTHLY_REPORT) {
                                popUpTo(Routes.DAILY_REPORT) { inclusive = true }
                            }
                        }
                    }
                )
            }

            composable(Routes.WEEKLY_REPORT) {
                WeeklyReportScreen(
                    onNavigateUp = { navController.popBackStack() },
                    onClose = { navController.popBackStack(Routes.INSIGHTS, false) },
                    onTabSelected = { tab ->
                        when (tab) {
                            "Daily" -> navController.navigate(Routes.DAILY_REPORT) {
                                popUpTo(Routes.WEEKLY_REPORT) { inclusive = true }
                            }
                            "Monthly" -> navController.navigate(Routes.MONTHLY_REPORT) {
                                popUpTo(Routes.WEEKLY_REPORT) { inclusive = true }
                            }
                        }
                    }
                )
            }

            composable(Routes.MONTHLY_REPORT) {
                MonthlyReportScreen(
                    onNavigateUp = { navController.popBackStack() },
                    onClose = { navController.popBackStack(Routes.INSIGHTS, false) },
                    onTabSelected = { tab ->
                        when (tab) {
                            "Daily" -> navController.navigate(Routes.DAILY_REPORT) {
                                popUpTo(Routes.MONTHLY_REPORT) { inclusive = true }
                            }
                            "Weekly" -> navController.navigate(Routes.WEEKLY_REPORT) {
                                popUpTo(Routes.MONTHLY_REPORT) { inclusive = true }
                            }
                        }
                    }
                )
            }
        }
    }
}
