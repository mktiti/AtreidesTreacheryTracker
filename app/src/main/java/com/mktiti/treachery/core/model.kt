package com.mktiti.treachery.core

import android.graphics.Color.parseColor
import com.google.gson.Gson

@Suppress("unused")
enum class CardType(
        val id: String,
        val niceName: String,
        val lightColor: Int,
        val darkColor: Int,
) {

    SPECIAL_WEAPON("special_weapon", "Weapon - Special", "#8e2d00"),
    PROJECTILE("projectile", "Weapon - Projectile", "#8e2d00"),
    SHIELD("shield", "Defense - Projectile", "#142e53", "#056DFF"),
    POISON("poison", "Weapon - Poison", "#8e2d00"),
    SNOOPER("snooper", "Defense - Poison", "#142e53", "#056DFF"),
    CHEAP_HERO("cheap_hero", "Special - Leader", "#404a10", "#A1B636"),
    WORTHLESS("worthless", "Worthless Card", "#988e5d"),
    SPECIAL("special", "Special", "#404a10"),
    SPECIAL_DEFENSE("special_defense", "Defense - Special", "#404a10"),
    GHOLA("ghola", "Special", "#8e2d00"),
    FAMILY_ATOMICS("truthtrance", "Special", "#8e2d00"),
    HAJR("hajr", "Special - Movement", "#8e2d00"),
    TRUTHTRANCE("truthtrance", "Special", "#8e2d00"),
    KARAMA("karama", "Karama", "#404a10"),
    RICHESE("richese", "Richese", "#ad2434");

    constructor(id: String, name: String, lightColor: String, darkColor: String) : this(id, name, parseColor(lightColor), parseColor(darkColor))

    constructor(id: String, name: String, color: String) : this(id, name, color, color)

    companion object {
        fun fromId(id: String) = values().first { it.id == id }
    }

}

data class Card(
    val name: String,
    val type: CardType,
    val tags: String,
    val description: String,
    val choam: String?
){

    companion object {
        val gson = Gson()

        fun parse(jsonValue: String): Card = gson.fromJson(jsonValue, Card::class.java)
    }

    fun json(): String = Card.gson.toJson(this)
}


enum class Player(
    val id: String,
    val niceName: String,
    val maxCards: Int = 4,
    private val startingCards: Int = 1
) {
    BIDDING("bidding", "Bidding", 8),
    DISCARD_PILE("discard_pile", "Discard Pile", 200),
    EMPEROR("emperor", "Emperor"),
    HARKONNEN("harkonnen", "Harkonnen", 8, 2),
    GUILD("guild", "Spacing Guild"),
    BENE_GESSERIT("benegesserit", "Bene Gesserit"),
    FREMEN("fremen", "Fremen"),
    IXIANS("ixians", "Ixians"),
    TLEILAXU("tleilaxu", "Tleilaxu"),
    CHOAM("choam", "Choam", 5),
    RICHESE("richese", "Richese"),
    ECAZ("ecaz", "Ecaz"),
    MORITANI("moritani", "Moritani");



    @Suppress("unused")
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

data class CardTransfer(
    var previousOwner: Player,
    val newOwner: Player,
    val card: Card
)

data class PlayerHand(
    val player: Player,
    val cards: List<Card?>,
    val note: String,
    var cardTransfers: MutableList<CardTransfer> = mutableListOf<CardTransfer>()
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

data class CardState(
    val card: Card,
    val cardPosition: Int,
    var owner: Player,
    val newOwner: Player? = null // when card was transferred
) {
    companion object {
        val gson = Gson()

        fun parse(jsonValue: String): CardState = gson.fromJson(jsonValue, CardState::class.java)
    }

    fun json(): String = gson.toJson(this)

}
