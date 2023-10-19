package info.jukov.data.model

class HttpException(val status: Int, override val message: String?): Exception()