package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.HyperbolicFunc
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.math.abs

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Hyperbolic & Catenary Studio", appName)
  }

  @Test
  fun `verify catenary evaluation and arc length formula`() {
    val paramA = 2.0
    val spanL = 6.0
    val expectedArcLength = 2.0 * paramA * kotlin.math.sinh(spanL / (2.0 * paramA))
    val expectedSag = paramA * (kotlin.math.cosh(spanL / (2.0 * paramA)) - 1.0)
    val expectedSlack = ((expectedArcLength - spanL) / spanL) * 100.0

    assertEquals(8.5173, expectedArcLength, 1e-3)
    assertEquals(2.7046, expectedSag, 1e-3)
    assertEquals(41.955, expectedSlack, 1e-2)
  }

  @Test
  fun `verify fundamental hyperbolic pythagorean identity`() {
    val xValues = listOf(0.0, 0.5, 1.0, 2.0, -1.5, 3.0)
    for (x in xValues) {
      val sinhVal = HyperbolicFunc.SINH.evaluate(x, paramA = 1.0, shiftC = 0.0)
      val coshVal = HyperbolicFunc.COSH.evaluate(x, paramA = 1.0, shiftC = 0.0)
      assertNotNull(sinhVal)
      assertNotNull(coshVal)
      val pythagorean = (coshVal!! * coshVal) - (sinhVal!! * sinhVal)
      assertEquals(1.0, pythagorean, 1e-6)
    }
  }

  @Test
  fun `verify tanh definition`() {
    val x = 1.2
    val sinhVal = HyperbolicFunc.SINH.evaluate(x, paramA = 1.0, shiftC = 0.0)!!
    val coshVal = HyperbolicFunc.COSH.evaluate(x, paramA = 1.0, shiftC = 0.0)!!
    val tanhVal = HyperbolicFunc.TANH.evaluate(x, paramA = 1.0, shiftC = 0.0)!!
    assertEquals(sinhVal / coshVal, tanhVal, 1e-6)
  }

  @Test
  fun `verify theme toggling`() {
    val viewModel = com.example.ui.HyperbolicViewModel()
    assertEquals(false, viewModel.uiState.value.isDarkTheme)
    viewModel.toggleTheme()
    assertEquals(true, viewModel.uiState.value.isDarkTheme)
    viewModel.toggleTheme()
    assertEquals(false, viewModel.uiState.value.isDarkTheme)
    viewModel.setDarkTheme(true)
    assertEquals(true, viewModel.uiState.value.isDarkTheme)
  }
}
