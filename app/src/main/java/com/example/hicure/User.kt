package com.example.hicure

class User {
    var name: String = ""
    var age: Int = 0
    var gender: String = ""
    var survey: Boolean = false
    var surveyResult: SurveyResult? = null
}

class SurveyResult {
    var answers: Map<String, SurveyData> = mutableMapOf()
}

class SurveyData {
    var answers: Map<String, String> = mutableMapOf()
    var date: String? = null
    var time: String? = null
}