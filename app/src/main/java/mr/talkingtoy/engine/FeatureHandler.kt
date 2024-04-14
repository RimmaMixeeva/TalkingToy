package mr.talkingtoy.engine

object FeatureHandler {
    var mp3FeatureIsOn = false
    var talkFeatureIsOn = false
    var repeatFeatureIsOn = false

    fun stop(){
        mp3FeatureIsOn = false
        talkFeatureIsOn = false
        repeatFeatureIsOn = false
    }
}
