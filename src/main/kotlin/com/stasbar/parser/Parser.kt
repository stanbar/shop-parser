package com.stasbar.parser

import com.stasbar.parser.data.Category
import com.stasbar.parser.data.Product
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.io.UnsupportedEncodingException
import java.net.URLDecoder
import java.util.*
import kotlin.collections.HashSet
import kotlin.system.measureTimeMillis


val MODE = Modes.DEBUG
val BASE_URL = "http://www.esc.pl"
fun main(args: Array<String>) {

    val categories = fetchCategories()
    val time = measureTimeMillis {
        categories.parallelStream().forEach {
            it.products = fetchProductsFor(it)
            log("Added ${it.products.size} elements to ${it.name}")
        }
    }

    log("Parsed $BASE_URL in $time sec")

}

fun fetchCategories(): Set<Category> {
    val doc = Jsoup.connect("$BASE_URL/advanced_search.php").get()
    val categoriesOptions = doc.select("#contentLT > form > table > tbody > tr:nth-child(7) > td > table > tbody > tr > td > table > tbody > tr:nth-child(1) > td.fieldValue > select > option")

    return categoriesOptions.filter {
        it.attr("value").isNotEmpty() && isSubcategory(it)
    }.map {
        val id = it.attr("value").toInt()
        val name = it.childNode(0).toString()
        Category(id, name)
    }.toSet()
}

fun isSubcategory(element: Element): Boolean {
    val spaces = element.childNode(0).toString().substringBeforeLast(" ").count()
    if (element.nextElementSibling() == null) return true // Is the last category
    val spacesNext = element.nextElementSibling().childNode(0).toString().substringBeforeLast(" ").count()
    return spacesNext <= spaces //It has no subcategories
}

fun fetchProductsFor(category: Category): HashSet<Product> {

    var page = 1
    val url = "$BASE_URL/index.php?cPath=${category.id}&page=$page"
    log(url)

    val products = HashSet<Product>()

    var doc = Jsoup.connect(url).get()
    var upperBound: Int
    var totalElements: Int
    var hasNextPage: Boolean
    try {
        hasNextPage = isCategoryNotEmpty(doc)
    } catch (e: NullPointerException) {
        log("No products found for ${category.name}, skipping")
        return products
    }

    while (hasNextPage) {

        val elements = doc.select(".productListing-even")
        val odd = doc.select(".productListing-odd")
        elements.addAll(odd)
        elements.forEach {
            try {
                val params = splitQuery(it.select("#productListing-pic > a").first().attr("href"))
                val id = params["products_id"].toString()
                val name = it.select("#productListing-pic > a").first().childNode(0).toString()
                val priceSelect = it.select(".productListing-data > b").first()
                val priceString = priceSelect.childNode(0).toString()
                val replaced = priceString.replace(",", ".").replace("[^.0123456789]".toRegex(), "")
                val priceDouble = replaced.toDouble()
                val product = Product(id, name, category, priceDouble)
                fetchAndfillWithDetails(product)
                products.add(product)
            } catch (e: NullPointerException) {
                println("Failed to parse ${it.baseUri()}")
                e.printStackTrace()
            }
        }
        products.forEach { log(it) }

        upperBound = getUpperBound(doc)
        totalElements = getTotalElements(doc)
        if (upperBound < totalElements) {
            doc = Jsoup.connect("$BASE_URL/index.php?cPath=${category.id}&page=${++page}").get()
            hasNextPage = true
        } else
            hasNextPage = false

    }
    return products
}

fun isCategoryNotEmpty(doc: Document) = try {
    doc.select("#contentLT > table > tbody > tr:nth-child(4) > td > table:nth-child(3) > tbody > tr > td:nth-child(1) > b:nth-child(2)").first()
    true
} catch (e: NullPointerException) {
    false
}

fun getUpperBound(doc: Document): Int {
    return doc.select("#contentLT > table > tbody > tr:nth-child(4) > td > table:nth-child(3) > tbody > tr > td:nth-child(1) > b:nth-child(2)").first().text().toInt()
}

fun getTotalElements(doc: Document): Int {
    return doc.select("#contentLT > table > tbody > tr:nth-child(4) > td > table:nth-child(3) > tbody > tr > td:nth-child(1) > b:nth-child(3)").first().text().toInt()
}

fun fetchAndfillWithDetails(product: Product) {

    val doc = Jsoup.connect("$BASE_URL/product_info.php?cPath=${product.category.id}&products_id=${product.id}").get()
    try {
        val selection1 = doc.select("#contentLT")
        val sel2 = selection1.select("form")
        val sel3 = sel2.select("table")
        val sel4 = sel3.select("tbody")
        val sel5 = sel4.select("tr:nth-child(3)")
        val sel6 = sel5.select("td")
        val sel7 = sel6.select("table")
        val sel8 = sel7.select("tbody")
        val sel9 = sel8.select("tr")
        val sel10 = sel9.select("td")
        val sel11a = sel10.select("a")
        if (sel11a.isNotEmpty())
            product.imgSrc = sel11a[0].attr("href")
        val descSelection = doc.select("#contentLT > form > table > tbody > tr:nth-child(3) > td")
        if (descSelection.isNotEmpty())
            product.description = descSelection[0].child(6).html()
    } catch (e: NullPointerException) {
        println("Failed to parse ${doc.baseUri()}")
        e.printStackTrace()
    }
}


@Throws(UnsupportedEncodingException::class)
fun splitQuery(query: String): Map<String, String> {
    val queryPairs = LinkedHashMap<String, String>()
    val pairs = query.split("&".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
    for (pair in pairs) {
        val idx = pair.indexOf("=")
        queryPairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"), URLDecoder.decode(pair.substring(idx + 1), "UTF-8"))
    }
    return queryPairs
}


enum class Modes { DEBUG, PROD }

fun log(message: String) {
    if (MODE == Modes.DEBUG)
        println(message)
}

fun log(item: Any) {
    if (MODE == Modes.DEBUG)
        println(item.toString())
}
