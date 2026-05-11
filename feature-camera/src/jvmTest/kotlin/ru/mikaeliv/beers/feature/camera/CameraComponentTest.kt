package ru.mikaeliv.beers.feature.camera

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class CameraComponentTest {

    /**
     * Проверяет, что back пробрасывается наружу через output.
     */
    @Test
    fun onBackCallsOutputBack() {
        val output = FakeOutput()
        val component = DefaultCameraComponent(output)

        component.onBack()

        assertEquals(1, output.backCalls)
    }

    /**
     * Проверяет, что захваченные байты фото передаются наружу без изменений.
     */
    @Test
    fun onPhotoCapturedCallsOutputWithBytes() {
        val output = FakeOutput()
        val component = DefaultCameraComponent(output)
        val bytes = byteArrayOf(1, 2, 3)

        component.onPhotoCaptured(bytes)

        assertContentEquals(bytes, output.photoBytes.single())
    }

    private class FakeOutput : CameraComponent.Output {
        var backCalls = 0
        val photoBytes = mutableListOf<ByteArray>()
        override fun back() { backCalls += 1 }
        override fun photoCaptured(bytes: ByteArray) { photoBytes += bytes }
    }
}
