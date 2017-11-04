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

package com.stasbar.parser.data

import java.util.*


data class Product(val id: String, val name: String, var category: Category, var price: Double = 0.0,
                   var imgSrc: String = "", var description: String = "") : Csvable {
    /**
     * Schema
     * ID , Active(0/1) , Name , Categories (x,y,z), Price , Description, ImgSrc
     */
    override fun getTitle(): List<String> {
        return Arrays.asList<String>("ID", "Active(0/1)", "Name", "Categories (xyz)", "Price", "Description", "ImgSrc")
    }

    override fun toCsv(): List<String> {
        return Arrays.asList<String>(id, 1.toString(), name, category.id.toString(), price.toString(), description, imgSrc)
    }
    override val fileNamePrefix = "products"

}