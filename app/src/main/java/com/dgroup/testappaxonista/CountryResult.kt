package com.dgroup.testappaxonista

import java.util.*

data class CountryResult(val country: String,
                         var startingDate: Date?=null,
                         var emails: List<String>?=null)