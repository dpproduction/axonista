package com.dgroup.testappaxonista

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter
import java.io.InputStream
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class CalcConferenceDatesUseCase {

    suspend fun execute(inputStream: InputStream, filesDir: File) : File{
        return withContext(Dispatchers.IO) {
            val attendees = parseModelsFromStream(inputStream).map {
                prepareDates(it)
            }
            val countryMap = parseCountriesMap(attendees)

            val countriesResultList = calcResult(countryMap)
            val jsonCountriesResultList = serializeToJson(countriesResultList)
            val file = saveToInternalStorage(jsonCountriesResultList, filesDir)
            return@withContext file
        }
    }

    /**
     * calc dates when attendee to be able to start conference
     */
    private fun prepareDates(attendee: Attendee): Attendee {
        val preparedDates = ArrayList<Date>()
        val sortedDates = attendee.availableDates.sorted()
        sortedDates.forEachIndexed { index, date ->
            if (index < sortedDates.size - 1) {
                val nextDate = sortedDates[index + 1]
                val diff: Long = nextDate.time - date.time
                if (diff > 0 && diff <= TimeUnit.DAYS.toMillis(1)) {
                    preparedDates.add(date)
                }
            }
        }
        attendee.availableDates = preparedDates
        return attendee
    }

    private fun parseModelsFromStream(inputStream: InputStream): List<Attendee> {
        val size: Int = inputStream.available()
        val buffer = ByteArray(size)
        inputStream.read(buffer)
        inputStream.close()
        val data = String(buffer)
        return Gson().fromJson(data, Array<Attendee>::class.java).toList()
    }

    private fun parseCountriesMap(attendees: List<Attendee>): HashMap<String, ArrayList<Attendee>> {
        val hashMap = HashMap<String, ArrayList<Attendee>>()
        attendees.forEach { attendee ->
            val key = hashMap[attendee.country]
            key?.add(attendee) ?: run {
                val list = ArrayList<Attendee>()
                list.add(attendee)
                hashMap.put(attendee.country, list)
            }
        }
        return hashMap
    }

    private fun calcResult(countryMap: HashMap<String, java.util.ArrayList<Attendee>>): List<CountryResult> {
        val resultList = ArrayList<CountryResult>()
        countryMap.values.forEach { attendees ->
            val countryResult = calcOptimalDates(attendees)
            if (countryResult.emails != null) {
                resultList.add(countryResult)
            }
        }
        return resultList
    }

    private fun calcOptimalDates(list: List<Attendee>): CountryResult {
        val countryResult = CountryResult(list.first().country)
        val dateEmailsMap = HashMap<Date, ArrayList<String>>()
        list.forEach { attendee ->
            attendee.availableDates.forEach { date ->
                val emails = dateEmailsMap[date]
                emails?.let {
                    emails.add(attendee.email)
                } ?: run {
                    val emailsList = ArrayList<String>()
                    emailsList.add(attendee.email)
                    dateEmailsMap.put(date, emailsList)
                }
            }
        }
        dateEmailsMap.entries.sortedBy { it.key }.forEach {
            if (countryResult.startingDate == null || countryResult.emails!!.size < it.value.size) {
                countryResult.startingDate = it.key
                countryResult.emails = it.value
            }
        }
        return countryResult
    }

    private fun serializeToJson(countriesResultList: List<CountryResult>): String {
        val gson = GsonBuilder()
            .setDateFormat("yyyy-MM-dd").create()
        return gson.toJson(countriesResultList)
    }

    private fun saveToInternalStorage(jsonCountriesResultList: String, filesDir: File): File {
        val resultFile = File(filesDir, "result.txt")
        val writer = FileWriter(resultFile)
        writer.append(jsonCountriesResultList)
        writer.flush()
        writer.close()
        return resultFile
    }

}