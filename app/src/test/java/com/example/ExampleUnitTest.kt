package com.example

import com.example.model.CablePreset
import com.example.model.CatenaryCalculation
import com.example.ui.HyperbolicViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.cosh
import kotlin.math.sinh

class ExampleUnitTest {

    @Test
    fun testCatenaryParameterScaling() {
        // Tension T0 = 20,000 N, mass density = 2.0 kg/m, g = 9.80665 m/s²
        // w = 2.0 * 9.80665 = 19.6133 N/m
        // a = T0 / w = 20000 / 19.6133 = 1019.7162 m
        val calc = CatenaryCalculation(
            horizontalTensionN = 20000.0,
            spanM = 200.0,
            linearMassDensityKgPerM = 2.0
        )
        val expectedW = 2.0 * 9.80665
        val expectedA = 20000.0 / expectedW

        assertEquals(expectedW, calc.linearWeightDensityNpm, 1e-4)
        assertEquals(expectedA, calc.parameterA, 1e-4)
    }

    @Test
    fun testCatenaryHyperbolicCosineEquation() {
        val calc = CatenaryCalculation(
            horizontalTensionN = 10000.0,
            spanM = 100.0,
            linearMassDensityKgPerM = 1.0
        )
        val a = calc.parameterA

        // y(x) = a * cosh(x / a)
        // At x = 0 (lowest point / vertex): y(0) = a * cosh(0) = a * 1 = a
        assertEquals(a, calc.evaluateY(0.0), 1e-6)

        // At x = 50 (right support): y(50) = a * cosh(50 / a)
        val expectedY50 = a * cosh(50.0 / a)
        assertEquals(expectedY50, calc.evaluateY(50.0), 1e-6)

        // Symmetry: y(-50) == y(50)
        assertEquals(calc.evaluateY(-50.0), calc.evaluateY(50.0), 1e-6)
    }

    @Test
    fun testCatenarySagAndArcLength() {
        val calc = CatenaryCalculation(
            horizontalTensionN = 25000.0,
            spanM = 300.0,
            linearMassDensityKgPerM = 1.5
        )
        val a = calc.parameterA
        val halfL = 150.0

        // h = a * (cosh(L / 2a) - 1)
        val expectedSag = a * (cosh(halfL / a) - 1.0)
        assertEquals(expectedSag, calc.maxSagM, 1e-5)

        // S = 2 * a * sinh(L / 2a)
        val expectedArcLength = 2.0 * a * sinh(halfL / a)
        assertEquals(expectedArcLength, calc.arcLengthM, 1e-5)

        // Cable arc length S must be strictly greater than horizontal span L
        assertTrue(calc.arcLengthM > calc.spanM)
        assertTrue(calc.slackPercent > 0.0)
    }

    @Test
    fun testCatenaryTensionAndReactions() {
        val calc = CatenaryCalculation(
            horizontalTensionN = 30000.0,
            spanM = 250.0,
            linearMassDensityKgPerM = 2.0
        )
        val a = calc.parameterA
        val halfL = 125.0

        // Maximum tension at support: T_max = T0 * cosh(L / 2a) = T0 + w * h
        val expectedTmax = 30000.0 * cosh(halfL / a)
        val expectedTmaxEnergy = 30000.0 + calc.linearWeightDensityNpm * calc.maxSagM
        assertEquals(expectedTmax, calc.maxTensionN, 1e-4)
        assertEquals(expectedTmaxEnergy, calc.maxTensionN, 1e-4)

        // Vertical reaction: V = T0 * sinh(L / 2a) = (w * S) / 2
        val expectedV = 30000.0 * sinh(halfL / a)
        val expectedVWeight = (calc.linearWeightDensityNpm * calc.arcLengthM) / 2.0
        assertEquals(expectedV, calc.verticalReactionN, 1e-4)
        assertEquals(expectedVWeight, calc.verticalReactionN, 1e-4)
    }

    @Test
    fun testViewModelCatenaryCalculations() {
        val vm = HyperbolicViewModel()

        // Test default state calculation
        val initialCalc = vm.uiState.value.catenaryCalculation
        assertNotNull(initialCalc)
        assertTrue(initialCalc.parameterA > 0.0)

        // Update cable tension
        vm.updateCableTension(50000.0)
        assertEquals(50000.0, vm.uiState.value.catenaryCalculation.horizontalTensionN, 1e-6)

        // Update cable span
        vm.updateCableSpan(400.0)
        assertEquals(400.0, vm.uiState.value.catenaryCalculation.spanM, 1e-6)

        // Update linear mass density
        vm.updateCableMassDensity(3.5)
        assertEquals(3.5, vm.uiState.value.catenaryCalculation.linearMassDensityKgPerM, 1e-6)

        // Test preset application
        vm.applyCablePreset(CablePreset.SUSPENSION_BRIDGE)
        assertEquals(CablePreset.SUSPENSION_BRIDGE.tensionN, vm.uiState.value.catenaryCalculation.horizontalTensionN, 1e-6)
        assertEquals(CablePreset.SUSPENSION_BRIDGE.spanM, vm.uiState.value.catenaryCalculation.spanM, 1e-6)
        assertEquals(CablePreset.SUSPENSION_BRIDGE.massKgPerM, vm.uiState.value.catenaryCalculation.linearMassDensityKgPerM, 1e-6)

        // Test point calculation
        val point = vm.calculateCatenaryPoint(0.0)
        assertEquals(0.0, point.x, 1e-6)
        assertEquals(0.0, point.sag, 1e-6)
        assertEquals(0.0, point.slope, 1e-6)

        // Test curve sample generation
        val curve = vm.getSampledCatenaryCurve(50)
        assertEquals(51, curve.size)
    }
}
