package mr.talkingtoy.engine

object TalkHandler {
    var name: String? = null
    var gender: String? = null //male-female
    var currentLevel = 1

    var levelOnePhrase = "Привет! Как тебя зовут?"

    var levelFourPhrase = "Классно! Твои хобби звучат интересно. А есть ли у тебя любимый цвет?"

    var levelSixPhrase = "Какое совпадение, я тоже люблю этот цвет!"

    fun generateLevelTwoPhrase(answer: String): String? {
         val allWords = answer.split(" ")
         for (word in allWords) {
             if(NamesList.maleNames.contains(word.trim())) {
                 gender = "m"
                 name = word
             } else if (NamesList.femaleNames.contains(word.trim())){
                 gender = "f"
                 name = word
             }
         }
        return if (name != null) {
            "Привет, $name. Рад познакомиться. А сколько тебе лет?"
        } else null
    }

    fun generateLevelThree(): String? {
        var phrase: String? = null
        if (gender == "m") {
            phrase = "Ого, ты уже такой взрослый. А что ты любишь делать в свободное время?"
        } else if (gender == "f"){
            phrase = "Ого, ты уже такая взрослая. А что ты любишь делать в свободное время?"
        }
        return phrase
    }

    fun generateFifthLevel(answer: String): String? {
        var phrase: String? = null
        var positiveAnswers = arrayListOf("да", "есть")
        var negativeAnswers = arrayListOf("нет", "нету")
        val allWords = answer.split(" ")
        for (word in allWords) {
            if (positiveAnswers.contains(word)) {
                phrase = "Расскажешь мне какой твой любимый цвет?"
            } else if (negativeAnswers.contains(word)) {
                phrase = "Я думаю ты ещё найдёшь свой любимый цвет."
            }
        }
        return phrase
    }

    fun processAnswer(answer: String, stopDialogue: () -> Unit): String {
        var phrase: String? = null
        when (currentLevel){
            1-> phrase = levelOnePhrase
            2-> phrase = generateLevelTwoPhrase(answer)
            3 -> phrase = generateLevelThree()
            4 -> phrase = levelFourPhrase
            5 -> phrase = generateFifthLevel(answer)
            6 -> phrase = levelSixPhrase
        }
        currentLevel++
        if (phrase == null) {
            phrase = "Я ещё маленький и не всё понимаю, мне надо время, чтобы обдумать твой ответ. Давай пока закончим разговор."
            stopDialogue.invoke()
            currentLevel = 1
        }
        return phrase
    }
}