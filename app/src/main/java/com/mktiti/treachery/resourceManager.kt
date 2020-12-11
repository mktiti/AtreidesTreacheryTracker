package com.mktiti.treachery

import android.graphics.drawable.Drawable

interface IconManager {

    fun unknownCard(): Drawable

    fun noCard(): Drawable

    operator fun get(cardType: CardType): Drawable

    operator fun get(player: Player): Drawable

}

class MapIconManager(
    private val unknownCard: Drawable,
    private val noCard: Drawable,
    private val cardTypeIcons: Map<CardType, Drawable>,
    private val playerIcons: Map<Player, Drawable>
) : IconManager {

    override fun unknownCard() = unknownCard

    override fun noCard() = noCard

    override fun get(cardType: CardType) = cardTypeIcons.getValue(cardType)

    override fun get(player: Player) = playerIcons.getValue(player)

}
