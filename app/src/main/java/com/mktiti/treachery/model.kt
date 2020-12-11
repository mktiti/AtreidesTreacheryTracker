package com.mktiti.treachery

import android.graphics.Color.parseColor
import com.google.gson.Gson

enum class CardType(
        val id: String,
        val niceName: String,
        val lightColor: Int,
        val darkColor: Int,
) {

    LASGUN("lasgun", "Weapon - Special", "#8e2d00"),
    PROJECTILE("projectile", "Weapon - Projectile", "#8e2d00"),
    SHIELD("shield", "Defense - Projectile", "#142e53", "#056DFF"),
    POISON("poison", "Weapon - Poison", "#8e2d00"),
    SNOOPER("snooper", "Defense - Poison", "#142e53", "#056DFF"),
    CHEAP_HERO("cheap_hero", "Special - Leader", "#404a10", "#A1B636"),
    WORTHLESS("worthless", "Worthless Card", "#988e5d"),
    SPECIAL("special", "Special", "#404a10");

    constructor(id: String, name: String, lightColor: String, darkColor: String) : this(id, name, parseColor(lightColor), parseColor(darkColor))

    constructor(id: String, name: String, color: String) : this(id, name, color, color)

    companion object {
        fun fromId(id: String) = values().first { it.id == id }
    }

}

data class Card(
    val name: String,
    val type: CardType
)

enum class Player(
    val id: String,
    val niceName: String,
    val maxCards: Int = 4,
    private val startingCards: Int = 1
) {

    EMPEROR("emperor", "Emperor"),
    HARKONNEN("harkonnen", "Harkonnen", 8, 2),
    GUILD("guild", "Spacing Guild"),
    BENE_GESSERIT("benegesserit", "Bene Gesserit"),
    FREMEN("fremen", "Fremen");

    fun emptyHand() = PlayerHand(
        player = this,
        cards = emptyList(),
        note = ""
    )

    fun startHand() = PlayerHand(
        player = this,
        cards = (0 until startingCards).map { null },
        note = ""
    )

}

data class PlayerHand(
        val player: Player,
        val cards: List<Card?>,
        val note: String
) {

    companion object {
        val gson = Gson()

        fun parse(jsonValue: String): PlayerHand = gson.fromJson(jsonValue, PlayerHand::class.java)
    }

    operator fun plus(card: Card) = copy(cards = cards + card)

    operator fun minus(card: Card) = copy(cards = cards - card)

    fun json(): String = gson.toJson(this)

}

data class HandState(
    val hands: List<PlayerHand>
) {
    companion object {
        fun parse(jsonValue: String): HandState = PlayerHand.gson.fromJson(jsonValue, HandState::class.java)
    }

    fun json(): String = PlayerHand.gson.toJson(this)

}
