package com.stasbar.parser.data

data class Category(val id : Int, val name :String ,var products : Set<Product> = HashSet())