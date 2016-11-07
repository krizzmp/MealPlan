import java.util.*
val shops = Shops(
        Shop(
                IngredientItem(Ingredient.BACON, 200.00, Price(10.00)),
                IngredientItem(Ingredient.SPAGHETTI, 1000.00, Price(10.00)),
                IngredientItem(Ingredient.CHEESE, 250.00, Price(14.00)),
                IngredientItem(Ingredient.TOMATOES, 10.00, Price(16.00)),
                IngredientItem(Ingredient.ONION, 7.00, Price(10.00)),
                IngredientItem(Ingredient.GARLIC, 7.00, Price(10.00)),
                IngredientItem(Ingredient.PORRE, 3.00, Price(15.00)),
                IngredientItem(Ingredient.CARROT, 10.00, Price(13.00)),
                IngredientItem(Ingredient.BROCCOLI, 300.00, Price(14.00)),
                IngredientItem(Ingredient.HVIDE_BØNNER, 200.00, Price(12.00))
        )
)
fun main(args: Array<String>) {

    val recipes = listOf<Recipe>(
            Recipe("spagetti w/ bacon",
                    IngredientAmount(Ingredient.BACON, 200.0),
                    IngredientAmount(Ingredient.SPAGHETTI, 100.0),
                    IngredientAmount(Ingredient.CHEESE, 125.0)
            ),
            Recipe("Tomatsupe med pasta",
                    IngredientAmount(Ingredient.TOMATOES, 6.0),
                    IngredientAmount(Ingredient.ONION, 1.0),
                    IngredientAmount(Ingredient.GARLIC, 1.0),
                    IngredientAmount(Ingredient.SPAGHETTI, 100.0),
                    IngredientAmount(Ingredient.PORRE, 1.0)
            ),
            Recipe("Urte-børnesuppe med pasta",
                    IngredientAmount(Ingredient.CARROT, 2.0),
                    IngredientAmount(Ingredient.ONION, 1.0),
                    IngredientAmount(Ingredient.GARLIC, 1.0),
                    IngredientAmount(Ingredient.SPAGHETTI, 150.0),
                    IngredientAmount(Ingredient.BROCCOLI, 150.0),
                    IngredientAmount(Ingredient.HVIDE_BØNNER, 100.0)
            )
    )
    val mealPlan = app(shops, recipes)
    print(mealPlan)
    println(mealPlan.map { listOf(it).price(shops) }.sum())
    println(mealPlan.price(shops))
}

fun app(shops: Shops, recipes: List<Recipe>): List<Recipe> {
    val pq = PriorityQueue<Tree>(recipes.map { Leaf(it, shops) })
    while (true) {
        val g = pq.poll()
        if (g.recipes().count() < 2) {
            val j = expand(g, recipes.subtract(g.recipes()).toList(), shops)
            pq.addAll(j)
        } else {
            return g.recipes()
        }
    }
}

fun expand(g: Tree, recipes: List<Recipe>, shops: Shops): Collection<Tree> = recipes.map { Node(it, g, shops) }

@JvmName("ListRecipePrice")
fun List<Recipe>.price(shops: Shops): Price = this.flatMap { it.ingredients.asList() }.price(shops)

@JvmName("ListIngredientPrice")
fun List<IngredientAmount>.price(shops: Shops): Price = this.groupBy { it.ingredient }.map { IngredientAmount(it.key,it.value.sumByDouble { x->x.amount }) }.map { shops.price(it) }.sum()

data class IngredientAmount(val ingredient: Ingredient, val amount: Double)

enum class Ingredient {
    BACON,
    SPAGHETTI,
    CHEESE,
    TOMATOES,
    ONION,
    GARLIC,
    PORRE,
    CARROT,
    BROCCOLI,
    HVIDE_BØNNER
}

interface Summable<A> {
    infix operator fun plus(other: A): A
}

private fun <T : Summable<T>> Iterable<T>.sum(): T = this.reduce { sum, el -> sum + el }

class Price(val price: Double) : Comparable<Price>, Summable<Price> {
    override fun compareTo(other: Price): Int {
        return (this.price).compareTo(other.price)
    }

    override infix operator fun plus(other: Price): Price {
        return Price(this.price + other.price)
    }

    override fun toString(): String {
        return price.toString()
    }
}

class Recipe(val name: String, vararg val ingredients: IngredientAmount) {
    override fun toString(): String {
        return "$name (${listOf(this).price(shops)})"
    }
}

class Shops(vararg val shops: Shop) {
    fun findCheapestIngredientItem(ingredient: IngredientAmount): IngredientItem {
        return shops.map {
            it.findCheapestIngredientItem(ingredient)
        }.sortedBy {
            it.price.price * Math.ceil(ingredient.amount / it.amount)
        }.first()
    }

    fun price(ingredient: IngredientAmount): Price = findCheapestIngredientItem(ingredient).price
}

class Shop(vararg val items: IngredientItem) {
    fun findCheapestIngredientItem(ingredient: IngredientAmount): IngredientItem {
        return items.filter {
            it.ingredient == ingredient.ingredient
        }.sortedBy {
            it.price.price * Math.ceil(ingredient.amount / it.amount)
        }.first()
    }
}

data class IngredientItem(val ingredient: Ingredient, val amount: Double, val price: Price)

class Leaf(val recipe: Recipe, shops: Shops) : Tree(shops) {
    override fun recipes(): List<Recipe> = listOf(recipe)
}

abstract class Tree(val shops: Shops) : Comparable<Tree> {
    abstract fun recipes(): List<Recipe>
    override fun compareTo(other: Tree): Int {
        return (this.price()).compareTo(other.price())
    }

    fun price() = this.recipes().price(shops)
}

open class Node(val recipe: Recipe, val child: Tree, shops: Shops) : Tree(shops) {
    override fun recipes(): List<Recipe> = child.recipes().plus(recipe)
}
