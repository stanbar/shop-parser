package com.stasbar.parser.data

data class Product(val id: String, val name: String, var category: Category, var price : Double = 0.0,var imgSrc: String = "", var description : String = "")