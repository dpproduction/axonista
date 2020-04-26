package com.dgroup.testappaxonista

import java.util.*

data class Attendee(val firstName: String,
                    val lastName: String,
                    val country: String,
                    val email: String,
                    var availableDates: List<Date>)