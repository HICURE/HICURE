package com.example.hicure

class User {
    var id:String = ""
    var name:String = ""
    var age:Int = 0
    var gender: String = ""

    constructor()

    constructor(id:String, name:String, age:Int, gender:String){
        this.id = id
        this.name = name
        this.age = age
        this.gender = gender
    }
}