package com.wifisense.motiontracker.domain.usecase

import com.wifisense.motiontracker.data.processing.SignalProcessor
import javax.inject.Inject

/**
 * Use case that stops the motion monitoring pipeline by resetting
 * the signal processor state. The calling coroutine scope cancellation
 * is responsible for stopping the Flow collection.
 */
class StopMonitoringUseCase @Inject constructor(
    private val signalProcessor: SignalProcessor
) {
    operator fun invoke() {
        signalProcessor.reset()
    }
}
