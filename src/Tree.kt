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