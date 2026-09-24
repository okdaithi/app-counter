package com.okdaithi.daycounter.widget

/** Widget styles from the handoff (1a Tile, 1b Ring, 1c Signed). */
enum class WidgetStyle { TILE, RING, SIGNED }

object WidgetConfig {
    /** Product decision still open; change here and rebuild. */
    val style: WidgetStyle = WidgetStyle.TILE
}
