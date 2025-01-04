package com.plcoding.biometricauth

import androidx.lifecycle.ViewModel
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State

/*class SharedViewModel : ViewModel() {
    private val _count = mutableStateOf(0)
    val count: State<Int> = _count

    private val _voterId = mutableStateOf<String?>(null) // Store voterId
    val voterId: State<String?> = _voterId

    fun incrementCount() {
        _count.value += 1
    }

    fun setVoterId(id: String) {
        _voterId.value = id // Save the voterId
    }

    fun isVoterAlreadyVerified(id: String): Boolean {
        return _voterId.value == id // Check if the stored voterId matches the input
    }
}*/
class SharedViewModel : ViewModel() {
    private val _count = mutableStateOf(0)
    val count: State<Int> = _count

    private val _verifiedVoterIds = mutableStateOf<List<String>>(emptyList()) // Store all voterIds
    val verifiedVoterIds: State<List<String>> = _verifiedVoterIds

    fun incrementCount() {
        _count.value += 1
    }

    // Add voterId to the list of verified voters
    fun addVoterId(id: String) {
        _verifiedVoterIds.value = _verifiedVoterIds.value + id // Add voterId to the list
    }

    // Check if the voterId is already in the list of verified voters
    fun isVoterAlreadyVerified(id: String): Boolean {
        return _verifiedVoterIds.value.contains(id) // Check if the id is in the list
    }
}
