package com.dgroup.testappaxonista

data class Resource<T>(val data: T? = null, val throwable: Throwable?=null)