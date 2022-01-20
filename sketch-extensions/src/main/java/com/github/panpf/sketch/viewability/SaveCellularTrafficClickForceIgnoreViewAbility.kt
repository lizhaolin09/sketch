package com.github.panpf.sketch.viewability

import android.view.View
import com.github.panpf.sketch.request.DisplayRequest
import com.github.panpf.sketch.request.DisplayResult.Error
import com.github.panpf.sketch.request.DisplayResult.Success
import com.github.panpf.sketch.request.ignoreSaveCellularTraffic
import com.github.panpf.sketch.request.isCausedBySaveCellularTraffic
import com.github.panpf.sketch.viewability.ViewAbility.ClickObserver
import com.github.panpf.sketch.viewability.ViewAbility.RequestListenerObserver

class SaveCellularTrafficClickForceIgnoreViewAbility
    : ViewAbility, ClickObserver, RequestListenerObserver {

    private var errorFromSaveCellularTraffic = false
    private var request: DisplayRequest? = null

    override var host: Host? = null

    override val canIntercept: Boolean
        get() = host != null && errorFromSaveCellularTraffic && request != null

    override fun onClick(v: View): Boolean {
        if (!canIntercept) return false
        val host = host ?: return false
        val request = request ?: return false
        val newRequest = request.newDisplayRequest {
            ignoreSaveCellularTraffic(true)
        }
        host.submitRequest(newRequest)
        return true
    }

    override fun onRequestStart(request: DisplayRequest) {
        errorFromSaveCellularTraffic = false
        this.request = null
    }

    override fun onRequestError(request: DisplayRequest, result: Error) {
        errorFromSaveCellularTraffic = result.exception.isCausedBySaveCellularTraffic
        if (errorFromSaveCellularTraffic) {
            this.request = request
        } else {
            this.request = null
        }
    }

    override fun onRequestSuccess(request: DisplayRequest, result: Success) {
        errorFromSaveCellularTraffic = false
        this.request = null
    }
}

fun ViewAbilityOwner.setClickRedisplayAndIgnoreSaveCellularTraffic(enabled: Boolean) {
    viewAbilityList
        .find { it is SaveCellularTrafficClickForceIgnoreViewAbility }
        ?.let { removeViewAbility(it) }
    if (enabled) {
        addViewAbility(SaveCellularTrafficClickForceIgnoreViewAbility())
    }
}