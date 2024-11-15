package com.cuifs;

internal class IORejectionException(@JvmField val code: String, message: String?) : Exception(message)