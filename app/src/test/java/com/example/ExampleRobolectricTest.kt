package com.example

import android.app.Application
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.core.app.ApplicationProvider
import com.example.ui.screens.LoginScreen
import com.example.ui.viewmodel.MasViewModel
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun testViewModelInitializationAndLifecycle() = runTest {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = MasViewModel(app)
    assertNotNull(viewModel)
  }

  @Test
  fun testLoginScreenRendering() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = MasViewModel(app)
    composeTestRule.setContent {
      LoginScreen(viewModel = viewModel, onLoginSuccess = {})
    }
    composeTestRule.onRoot().assertExists()
  }

  @Test
  fun testMainAppScaffoldRendering() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = MasViewModel(app)
    composeTestRule.setContent {
      MainAppScaffold(viewModel = viewModel)
    }
    composeTestRule.onRoot().assertExists()
  }
}


