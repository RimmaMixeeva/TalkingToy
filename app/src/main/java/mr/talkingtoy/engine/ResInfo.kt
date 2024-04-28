package mr.talkingtoy.engine

import mr.talkingtoy.R

object ResInfo {
   fun getTalesChoice(): Map<String, Int> {
      val map = mutableMapOf<String, Int>()
      map["Царевна-лягушка"] =  R.raw.carevna_lyagushka
      map["Гуси-лебеди"] =  R.raw.gusi_lebedi
      map["Колобок"] = R.raw.kolobok
      map["Маша и медведь"] =  R.raw.masha_i_medved
      map["Теремок"] =  R.raw.teremok
      map["Три медведя"] =  R.raw.tri_medvedya
      map["Репка"] =  R.raw.repka
      map["Рандом"] =  -1
      return map
   }

   fun getSongsChoice(): Map<String, Int> {
      val map = mutableMapOf<String, Int>()
      map["Спи, моя радость, усни"] = R.raw.spi_moya_radost
      map["Голубой вагон"] =  R.raw.blue_vagon
      map["Крылатые качели"] =  R.raw.winged_swing
      map["Песня шапокляк"] =  R.raw.shapoklyak_song
      map["Песня красной шапочки"] =  R.raw.red_riding_hood
      map["Песня про доктора Айболита"] =  R.raw.doctor_aibolit
      map["Танец маленьких утят"] =  R.raw.dance_of_little_ducks
      map["Чунга-чанга"] =  R.raw.chunga_changa
      map["Лето"] =  R.raw.summer
      map["Рандом"] =  -1
      return map
   }


}