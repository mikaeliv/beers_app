package ru.mikaeliv.beers.feature.camera

interface CameraComponent {
    fun onBack()
    fun onPhotoCaptured(bytes: ByteArray)

    interface Output {
        fun back()
        fun photoCaptured(bytes: ByteArray)
    }
}

class DefaultCameraComponent(
    private val output: CameraComponent.Output,
) : CameraComponent {
    override fun onBack() {
        output.back()
    }

    override fun onPhotoCaptured(bytes: ByteArray) {
        output.photoCaptured(bytes)
    }
}
