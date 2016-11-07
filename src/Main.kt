import java.util.*
val shops = Shops(
        Shop(
                IngredientItem(Ingredient.BACON, 200.0, Price(10.00)),
                IngredientItem(Ingredient.SPAGHETTI, 1000.0, Price(10.00)),
                IngredientItem(Ingredient.OST, 250.0, Price(14.00)),
                IngredientItem(Ingredient.TOMATER, 10.0, Price(16.00)),
                IngredientItem(Ingredient.LØG, 7.0, Price(10.00)),
                IngredientItem(Ingredient.HVIDLØG, 7.0, Price(10.00)),
                IngredientItem(Ingredient.PORRE, 3.0, Price(15.00)),
                IngredientItem(Ingredient.GULEROD, 10.0, Price(13.00)),
                IngredientItem(Ingredient.BROCCOLI, 300.0, Price(14.00)),
                IngredientItem(Ingredient.HVIDE_BØNNER, 200.0, Price(12.00)),
                IngredientItem(Ingredient.PARMESAN, 300.0,Price(34.00)),
                IngredientItem(Ingredient.PERSILLE, 100.0,Price(24.00)),
                IngredientItem(Ingredient.RØGET_SKINKE_TERNINGER, 200.0,Price(10.00)),
                IngredientItem(Ingredient.RODSELLERI_TERNINGER, 150.0,Price(16.00)),
                IngredientItem(Ingredient.HVIDKÅL, 500.0,Price(20.00)),
                IngredientItem(Ingredient.KARTOFLER, 1000.0,Price(12.00)),
                IngredientItem(Ingredient.SMØR, 500.0,Price(9.00)),
                IngredientItem(Ingredient.URTEBULLION, 7.0,Price(11.00)),
                IngredientItem(Ingredient.TOMATPURRE, 70.0,Price(10.00)),
                IngredientItem(Ingredient.ÆRTER, 500.0,Price(16.00)),
                IngredientItem(Ingredient.PASTASNEGLE, 500.0,Price(16.00)),
                IngredientItem(Ingredient.HAKKEDE_TOMATER, 200.0,Price(13.00)),
                IngredientItem(Ingredient.BLADSELLERI, 100.0,Price(16.00))

        )
)

fun main(args: Array<String>) {

    val recipes = listOf(
            Recipe("Tomatsupe med pasta",
                    IngredientAmount(Ingredient.TOMATER, 6.0),
                    IngredientAmount(Ingredient.LØG, 1.0),
                    IngredientAmount(Ingredient.HVIDLØG, 1.0),
                    IngredientAmount(Ingredient.SPAGHETTI, 100.0),
                    IngredientAmount(Ingredient.PORRE, 1.0),
                    IngredientAmount(Ingredient.PARMESAN, 50.0),
                    IngredientAmount(Ingredient.PERSILLE, 15.0)
            ),
            Recipe("Urte-børnesuppe med pasta",
                    IngredientAmount(Ingredient.LØG, 1.0),
                    IngredientAmount(Ingredient.HVIDLØG, 1.0),
                    IngredientAmount(Ingredient.GULEROD, 2.0),
                    IngredientAmount(Ingredient.BLADSELLERI, 10.0),
                    IngredientAmount(Ingredient.URTEBULLION, 1.0),
                    IngredientAmount(Ingredient.SPAGHETTI, 150.0),
                    IngredientAmount(Ingredient.BROCCOLI, 150.0),
                    IngredientAmount(Ingredient.HVIDE_BØNNER, 100.0),
                    IngredientAmount(Ingredient.PARMESAN, 50.0)
            ),
            Recipe("Minestrone",
                    IngredientAmount(Ingredient.RØGET_SKINKE_TERNINGER, 100.0),
                    IngredientAmount(Ingredient.LØG, 1.0),
                    IngredientAmount(Ingredient.RODSELLERI_TERNINGER, 25.0),
                    IngredientAmount(Ingredient.GULEROD, 1.0),
                    IngredientAmount(Ingredient.HVIDKÅL, 150.0),
                    IngredientAmount(Ingredient.KARTOFLER, 100.0),
                    IngredientAmount(Ingredient.SMØR, 25.0),
                    IngredientAmount(Ingredient.URTEBULLION, 1.0),
                    IngredientAmount(Ingredient.TOMATPURRE, 70.0),
                    IngredientAmount(Ingredient.HVIDLØG, 2.0),
                    IngredientAmount(Ingredient.ÆRTER, 250.0),
                    IngredientAmount(Ingredient.TOMATER, 2.0),
                    IngredientAmount(Ingredient.PASTASNEGLE, 70.0)
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
        if (g.recipes().count() < 3) {
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
fun List<IngredientAmount>.price(shops: Shops): Price = this.groupBy { it.ingredient }.map { IngredientAmount(it.key, it.value.sumByDouble { x -> x.amount }) }.map { shops.price(it) }.sum()


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
data class IngredientAmount(val ingredient: Ingredient, val amount: Double)
enum class Ingredient {
    BACON,
    SPAGHETTI,
    OST,
    TOMATER,
    LØG,
    HVIDLØG,
    PORRE,
    GULEROD,
    BROCCOLI,
    HVIDE_BØNNER,
    PARMESAN,
    PERSILLE,
    RØGET_SKINKE_TERNINGER,
    RODSELLERI_TERNINGER,
    HVIDKÅL,
    KARTOFLER,
    SMØR,
    URTEBULLION,
    TOMATPURRE,
    ÆRTER,
    PASTASNEGLE,
    HAKKEDE_TOMATER,
    BLADSELLERI
}