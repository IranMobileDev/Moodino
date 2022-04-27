package com.iranmobiledev.moodino.repository.more

interface MoreRepository {

    suspend fun savePINLock(pin : String)

    fun isActivePINLock() : Boolean

    fun isActiveFingerPrintLock() : Boolean

    suspend fun setActivePINLock(active : Boolean)

    suspend fun setActiveFingerPrintLock(active : Boolean)
}