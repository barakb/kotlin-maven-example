package intro

import java.util.*

data class Person(val name: String,
                  val age: Int? = null)

fun main(args: Array<String>) {
    val persons = listOf(Person("Alice"),
        Person("Bob", age = 29))

    val oldest = persons.maxBy { it.age ?: 0 }
    println("The oldest is: $oldest")
}

// The oldest is: Person(name=Bob, age=29)
interface FooIfc{

}

enum class MyEnum{
    Vale1,
    Vale2
}



// functions
fun max(a: Int, b: Int): Int {
    return if (a > b) a else b
}

// println(max(1, 2))

// in case function body is only 1 expression we can use this consise syntax


fun max1(a: Int, b: Int): Int =
    if (a > b) a else b

// we can simplify more by ommiting the return type

fun max2(a: Int, b: Int) =
    if (a > b) a else b

// variable

var a = 1
var a1: Int = 1

// the compiler check that val is initial exactly once, but it does not have to be right when it defined.
fun f1(): Int {
    val foo: Int
    if (a > 1) {
        foo = 2
    } else {
        foo = 3
    }
    return foo
}

// string template
fun f2() {
    println("a=$a a-1=${a-1}")
}

// classes and properties, no 'new' constractor is just a function
class Person1(
    val name: String,
    var isMarried: Boolean
)

fun use1() {
    val person = Person1("Bob", true)
    println(person.name)
    println(person.isMarried)
}

class Foo : (Int) -> Int {

    override fun invoke(p1: Int): Int {
        TODO("Not yet implemented")
    }
}

val g1 = Foo()
val gg = g1(1)

data class Foo1(val foo: String, val goo: Int){
    fun foo(): Int = 5
}

fun foo6(a :Int)
   = if(a > 0) "big" else "small"


fun r5() : Int {
    val v1 = Foo1("a", 1)
    val v2 = v1.copy(goo=1)
    val (_, g) = v2
    return 1
}

val lam1 = { a: Int ->
    a + 1
    ""
    }

fun ho(adder: (Int) -> Int) : Int =  adder(3)

//val useHo2 = ho { i: Int -> i + 1}
val useHo2 = ho {it + it}
val adder = fun (i: Int): Int = i + 1
val useHo = ho(adder)
val useHo1 = ho(fun (i: Int): Int = i + 1)

inline fun inlineFoo(i: Int)  = i + i

inline fun <reified T> create(): T {
    return T::class.java.newInstance()
}

fun Int.addLong(l: Long): Long{
    return this + l
}

fun <T> Collection<T>?.nullOrEmpty(): Boolean {
    return this?.isEmpty() ?: true
}

//val val2: List<Int>? = Arrays.asList(1, 3, 4)
val val2: List<Int>? = null
val foor = val2.nullOrEmpty()



val h = create<Int>()




//when
val f = when (a){
    1  -> a + 1
//    else -> 5
    else -> 1
}

//when with smart cast
val b : Any? =  null

val f1 = when(b){
    is Int -> b
    else -> 5
}

// ad hoc when

val f2 = when{
    1 == b -> "Hi"
    h == 1 -> "Foo"
    else -> "There"
}


sealed class Base
class C1 : Base()
class C2 : Base()
class C3: Base()

fun process(c: Base): Int{
    return when (c){
        is C1 -> 1
        is C2-> 2
        is C3 -> 3
    }
}

fun whileDemo(){
    var a = 0
    while(a < 100){
        a++
        print("hi")
    }
}

fun doWhileDemo(){
    var a = 0
    do {
        print("hi")
        a++
    }while(a < 100)
}

fun forRange(){
    for(a in 1 .. 100){
        println("hi")
    }
}

fun testMembership(s: Set<Int>) : Boolean{
    return 5 in s
}

// data classes have distcactors automatically
fun iterateOverMap(m : Map<String, Int>){

    for((k, v) in m){
        print("key is " + k + " value is " + v)
    }
}

// manual distract
class Dis(private val a: Int, private val b: String){
    operator fun component1() = 2
    operator fun component2() = b
    operator fun plus(other: Int) : String{
        return "fdd"
    }
}

val c = Dis(1, "1")
fun useDis(dis: Dis): String {
    val (n,s) = c
    val f = c + 1
    return n.toString(10) + " " + s
}

/*
/* using from Java */
>>> Person1 person = new Person1("Bob", true);
>>> System.out.println(person.getName());
Bob
>>> System.out.println(person.isMarried());
true
 */


//custom accessor
// virtual properties, properties that does not have storage
class Rectangle(val height: Int = 3, val width: Int, var color:Int = 0) {
    var isSquare: Boolean
        get() {
            return height == width
        }
        set(bool) {

        }



}
//val rec1 = Rectangle(2)
val rec2 = Rectangle(width = 2)

val str = "${ rec2 + 1 }"
val str1 = """
   foo
   bar 
   1   
""".trimIndent()


val a = try{ 1 } catch(e: Exception ){ 2 }