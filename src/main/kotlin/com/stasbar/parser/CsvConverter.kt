/*
 * Copyright (c) Stanislaw stasbar Baranski 2017.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.stasbar.parser


import com.stasbar.parser.data.Csvable
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.io.Writer
import java.text.SimpleDateFormat
import java.util.*

object CsvConverter {

    fun saveToFile(csvables: List<Csvable>, fileName: String = fileName(csvables[0])) {
        try {
            val file = File(fileName)
            file.parentFile.mkdirs()
            val writer = FileWriter(file)

            repeat(csvables.size) {
                val csv = csvables[it]
                if (it == 0) CsvConverter.writeLine(writer, csv.getTitle()) // Write also title
                CsvConverter.writeLine(writer, csv.toCsv())
            }

            writer.flush()
            writer.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }


    }

    private fun fileName(data: Csvable) = "outputs/${data.fileNamePrefix}_${SimpleDateFormat("dd_MM_yyyy_hhmmss").format(Date(System.currentTimeMillis()))}.csv"


    private val DEFAULT_SEPARATOR = ';'

    @Throws(IOException::class)
    @JvmOverloads
    fun writeLine(w: Writer, values: List<String>, customQuote: Char = '\"') {
        val separators = DEFAULT_SEPARATOR
        var first = true

        val sb = StringBuilder()
        for (value in values) {
            val formattedValue = value
                    .replace("&nbsp;","")
                    .replace("&nbsp","")
                    .replace("&gt;","")
                    .replace("&amp;","")
                    .replace("=>","-")
                    .replace("->","-")
                    .replace("->","")
                    .replace(";","")

            if (!first) {
                first = false
                sb.append(separators)
            }

            sb.append("$customQuote ${followCVSformat(formattedValue)} $customQuote")

        }
        sb.append("\n")
        w.append(sb.toString())


    }

    //https://tools.ietf.org/html/rfc4180
    private fun followCVSformat(value: String): String {

        var result = value
        if (result.contains("\"")) {
            result = result.replace("\"", "\"\"")
        }
        return result

    }

}