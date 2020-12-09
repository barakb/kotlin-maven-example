package com.github.barakb

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

//https://stackoverflow.com/questions/53651519/how-to-make-field-required-in-kotlin-dsl-builders
// https://kotlinexpertise.com/create-dsl-with-kotlin/

// Sealed class is used here instead of an interface
// to forbid inheriting it without implementing Named
sealed class PersonBuilder {
    var _name: String? = null
    var age: Int? = null

    // Marker interface which indicates that this PersonBuilder has an initialized name
    interface Named

    // Now we know that each PersonBuilder implements Named
    private class Impl : PersonBuilder(), Named

    companion object {
        // This function invocation looks like constructor invocation
        operator fun invoke(): PersonBuilder = Impl()
    }
}

// Receiver object will be smart casted to <PersonBuilder & Named>
@ExperimentalContracts
fun PersonBuilder.name(name: String) {
    contract {
        returns() implies (this@name is PersonBuilder.Named)
    }
    _name = name
}

// Extension property for <PersonBuilder & Named>
val <S> S.name where
        S : PersonBuilder,
        S : PersonBuilder.Named
    get() = _name!!

// This method can be called only if the builder has been named
fun <S> S.build(): Person where
        S : PersonBuilder,
        S : PersonBuilder.Named = Person(name, age)

data class Person(val name:String, val age: Int?)

fun person(init: PersonBuilder.() -> Person) : Person{
   return PersonBuilder().init()
}

@ExperimentalContracts
fun main(){
    person {
        // Explicit "this." is required in current Kotlin version for smart casting.
        // It must be a bug which will most likely be fixed later.
        this.name("John Doe") // will not compile without this line
        age = 25
        build()
    }
}