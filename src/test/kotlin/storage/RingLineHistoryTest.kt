package org.example.storage

class RingLineHistoryTest : LineHistoryContractTest() {
    override fun createLineHistory(
        width: Int,
        height: Int,
        scrollbackMaxSize: Int
    ): LineHistory {
        return RingLineHistory(width, height, scrollbackMaxSize)
    }
}