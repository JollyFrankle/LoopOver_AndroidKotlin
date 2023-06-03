package com.example.poolover_jinston

import android.util.Log
import kotlin.math.abs

object ColourGenerator {
    private const val maxColor = 255
    private const val minColor = 100

    fun generateTileColours(listOfElements: List<String>, boardSize: Int): HashMap<String, Int> {
        // Left: rgb(255, 0, 0)
        // Right: rgb(0, 255, 0)
        // Bottom: rgb(0, 0, 255)

        val colorStepSize = (maxColor - minColor) / ((boardSize - 1)*2)
        val colourMap = mutableListOf<MutableList<ColourHolder>>()

        // Initialise list
        for (i in 0 until boardSize) {
            colourMap.add(mutableListOf())
            for (j in 0 until boardSize) {
                colourMap[i].add(ColourHolder())
            }
        }

        // Generate for red (top to bottom, left to right)
        for (i in 0 until boardSize) {
            val rowColour = maxColor - (i * colorStepSize)
            for (j in 0 until boardSize) {
                colourMap[i][j].r = (rowColour - (j * colorStepSize*2)).coerceAtLeast(0)
            }
        }

        // Generate for blue (top to bottom, right to left)
        for (i in 0 until boardSize) {
            val rowColour = maxColor - (i * colorStepSize)
            for (j in 0 until boardSize) {
                colourMap[i][boardSize-1-j].b = (rowColour - (j * colorStepSize*2)).coerceAtLeast(0)
            }
        }

        // Generate for blue (bottom-center to top)
        for (i in 0 until boardSize) {
            val rowColour = maxColor - (i * colorStepSize*2)
            for (j in 0 until boardSize) {
                colourMap[boardSize-1-i][j].g = (rowColour - abs(j - (boardSize-1)/2) * colorStepSize*2).coerceAtLeast(0)
            }
        }

        Log.d("ColourGenerator", "colourMap: $colourMap")

        // Assign colours to elements
        val colourMapFlattened = colourMap.flatten()
        val elementColourHash = HashMap<String, Int>()
        for (i in listOfElements.indices) {
            elementColourHash[listOfElements[i]] = colourMapFlattened[i].toRGB()
        }

        return elementColourHash
    }

    class ColourHolder {
        var r: Int = 0
        var g: Int = 0
        var b: Int = 0
        fun toRGB(): Int {
            return android.graphics.Color.rgb(r, g, b)
        }

        override fun toString(): String {
            return "[$r, $g, $b]"
        }
    }
}