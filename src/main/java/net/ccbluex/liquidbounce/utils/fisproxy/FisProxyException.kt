package net.ccbluex.liquidbounce.utils.fisproxy

class FisProxyException(val status: Int, val code: String?, message: String) : Exception(message)
