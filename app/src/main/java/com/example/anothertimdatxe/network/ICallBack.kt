package com.example.anothertimdatxe.network

interface ICallBack<R> {
    fun onSuccess(result: R?)//result is able to be null
    fun onError(e: ApiException)//return ApiException if error
}