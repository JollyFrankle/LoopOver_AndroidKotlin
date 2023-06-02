package com.example.poolover_jinston

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.RelativeLayout
import android.widget.Toast
import com.example.poolover_jinston.databinding.FragmentGameBinding
import kotlin.math.abs
import kotlin.random.Random

/**
 * A simple [Fragment] subclass.
 * Use the [GameFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class GameFragment : Fragment() {

    private var _binding: FragmentGameBinding? = null
    private val binding get() = _binding!!

    private var direction = 0 // -1 = horizontal, 1 = vertical
    private var swipeCount = 0

    private var prevX = 0f
    private var prevY = 0f
    private var initialX = 0f
    private var initialY = 0f

    private var boardSize = 0
    private var boardX = 0f
    private var boardY = 0f

    private var tileSize = 0
    private var tempIndexX = 0
    private var tempIndexY = 0

    private lateinit var tileScrollH: List<MutableList<TileView>>
    private lateinit var tileScrollV: List<MutableList<TileView>>

    private lateinit var boardContent: List<MutableList<Char>>
    private var boardColor: HashMap<Char, Int> = hashMapOf(
        'A' to android.graphics.Color.rgb(250, 130, 130),
        'B' to android.graphics.Color.rgb(240, 140, 130),
        'C' to android.graphics.Color.rgb(230, 150, 130),
        'D' to android.graphics.Color.rgb(220, 160, 130),
        'E' to android.graphics.Color.rgb(210, 170, 130),
        'F' to android.graphics.Color.rgb(200, 180, 130),
        'G' to android.graphics.Color.rgb(190, 190, 130),
        'H' to android.graphics.Color.rgb(180, 200, 130),
        'I' to android.graphics.Color.rgb(170, 210, 130),
        'J' to android.graphics.Color.rgb(160, 220, 130),
        'K' to android.graphics.Color.rgb(150, 230, 130),
        'L' to android.graphics.Color.rgb(140, 240, 130),
        'M' to android.graphics.Color.rgb(130, 250, 130),
        'N' to android.graphics.Color.rgb(130, 240, 140),
        'O' to android.graphics.Color.rgb(130, 230, 150),
        'P' to android.graphics.Color.rgb(130, 220, 160),
        'Q' to android.graphics.Color.rgb(130, 210, 170),
        'R' to android.graphics.Color.rgb(130, 200, 180),
        'S' to android.graphics.Color.rgb(130, 190, 190),
        'T' to android.graphics.Color.rgb(130, 180, 200),
        'U' to android.graphics.Color.rgb(130, 170, 210),
        'V' to android.graphics.Color.rgb(130, 160, 220),
        'W' to android.graphics.Color.rgb(130, 150, 230),
        'X' to android.graphics.Color.rgb(130, 140, 240),
        'Y' to android.graphics.Color.rgb(130, 130, 250),
    )

    private val activity: MainActivity get() = requireActivity() as MainActivity

    private lateinit var board: RelativeLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentGameBinding.inflate(inflater, container, false)

        binding.root.setOnClickListener { }
        binding.root.setOnTouchListener(onTouchEvent)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        board = binding.root

        val globalLayoutListener = object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                setupBoard()
                board.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        }

        board.viewTreeObserver.addOnGlobalLayoutListener(globalLayoutListener)

        boardContent = mutableListOf(
            mutableListOf('A', 'B', 'C', 'D', 'E'),
            mutableListOf('F', 'G', 'H', 'I', 'J'),
            mutableListOf('K', 'L', 'M', 'N', 'O'),
            mutableListOf('P', 'Q', 'R', 'S', 'T'),
            mutableListOf('U', 'V', 'W', 'X', 'Y'),
        )

        tileScrollH = listOf(
            mutableListOf(),
            mutableListOf(),
            mutableListOf(),
            mutableListOf(),
            mutableListOf(),
            mutableListOf(), // Overflow row
        )

        tileScrollV = listOf(
            mutableListOf(),
            mutableListOf(),
            mutableListOf(),
            mutableListOf(),
            mutableListOf(),
            mutableListOf(), // Overflow column
        )

        // Setup tiles:
        for (i in 0..5) {
            for (j in 0..5) {
                val tile = TileView(activity)
                tile.id = View.generateViewId()
                tileScrollH[i].add(tile)
                tileScrollV[j].add(tile)
                board.addView(tile)
            }
        }
    }

    private val onTouchEvent = View.OnTouchListener { v, event ->
        if (event?.action == MotionEvent.ACTION_DOWN) {
            // First touch
//            Log.d("MainActivity", "onTouchEvent: ${event.x}, ${event.y}")
            initialX = event.x
            initialY = event.y

            if (activity.startTime == 0L) {
                activity.startTime = System.currentTimeMillis()
                activity.setupTimer()
            }
        }

        if (event?.action == MotionEvent.ACTION_MOVE) {
//            Log.d("MainActivity", "onTouchEvent: ${event.x}, ${event.y}")
            swipeCount++
            setDirection(event)

            if (swipeCount > 3) {
                if (direction == -1) {
                    moveX(tileScrollH[tempIndexY], tempIndexY, event)
                } else {
                    moveY(tileScrollV[tempIndexX], tempIndexX, event)
                }
            }

            prevX = event.x
            prevY = event.y
        }

        if (event?.action == MotionEvent.ACTION_UP) {
//            Log.e("MainActivity", "onTouchEvent enters ACTION_UP")
            if (direction == -1) {
                snapX(tileScrollH[tempIndexY], tempIndexY)
            } else {
                snapY(tileScrollV[tempIndexX], tempIndexX)
            }
            direction = 0
            swipeCount = 0

            activity.checkIsSolved(boardContent)
        }
        return@OnTouchListener v.performClick()
    }

    private fun setDirection(event: MotionEvent) {
        if (direction == 0 && swipeCount > 3) {
            Log.d("MainActivity", "setDirection: ${event.x}, ${event.y}")
            val deltaX = abs(event.x - initialX)
            val deltaY = abs(event.y - initialY)
            if (deltaX > deltaY) {
                direction = -1
                tempIndexY = (event.y / tileSize).toInt()
            } else {
                direction = 1
                tempIndexX = (event.x / tileSize).toInt()
            }
        }
    }

    private fun moveX(elements: List<TileView>, rowNum: Int, event: MotionEvent) {
        val deltaX = event.x - prevX

        // Handle wrap around
        if (elements[5].x > boardSize) {
            elements.forEach { element ->
                element.x -= tileSize
            }
            shiftX(rowNum, -1)
        } else if (elements[0].x < -tileSize) {
            elements.forEach { element ->
                element.x += tileSize
            }
            shiftX(rowNum, 1)
        } else {
            // Handle normal
            elements.forEach { element ->
                element.x += deltaX
            }
        }
    }

    private fun moveY(elements: List<TileView>, colNum: Int, event: MotionEvent) {
        val deltaY = event.y - prevY

        // Handle wrap around
        if (elements[5].y > boardSize) {
            elements.forEach { element ->
                element.y -= tileSize
            }
            shiftY(colNum, -1)
        } else if (elements[0].y < -tileSize) {
            elements.forEach { element ->
                element.y += tileSize
            }
            shiftY(colNum, 1)
        } else {
            // Handle normal
            elements.forEach { element ->
                element.y += deltaY
            }
        }
    }

    private fun shiftX(rowNum: Int, direction: Int) {
        try {
            val temp = boardContent[rowNum].toMutableList()
            if (direction == 1) {
                boardContent[rowNum].forEachIndexed { i, _ ->
                    if (i == 4) {
                        boardContent[rowNum][i] = temp[0]
                    } else {
                        boardContent[rowNum][i] = temp[i + 1]
                    }
                }
            } else {
                boardContent[rowNum].forEachIndexed { i, _ ->
                    if (i == 0) {
                        boardContent[rowNum][i] = temp[4]
                    } else {
                        boardContent[rowNum][i] = temp[i - 1]
                    }
                }
            }

            drawBoardX(rowNum)
        } catch (e: Exception) {
            Toast.makeText(activity, "Error: $e", Toast.LENGTH_SHORT).show()
        }
    }

    private fun shiftY(colNum: Int, direction: Int) {
        try {
            val temp = boardContent.map { it[colNum] }.toMutableList()
            if (direction == 1) {
                boardContent.forEachIndexed { i, _ ->
                    if (i == 4) {
                        boardContent[i][colNum] = temp[0]
                    } else {
                        boardContent[i][colNum] = temp[i + 1]
                    }
                }
            } else {
                boardContent.forEachIndexed { i, _ ->
                    if (i == 0) {
                        boardContent[i][colNum] = temp[4]
                    } else {
                        boardContent[i][colNum] = temp[i - 1]
                    }
                }
            }

            drawBoardY(colNum)
        } catch (e: Exception) {
            Toast.makeText(activity, "Error: $e", Toast.LENGTH_SHORT).show()
        }
    }

    private fun snapX(elements: List<TileView>, rowNum: Int) {
        val tile0 = elements[0]
        val tile0x = tile0.x

        Log.e("MainActivity", "snapX: $tile0x, ${-tileSize / 2}")
        if (tile0x < -tileSize / 2) {
//            Log.e("MainActivity", "enters if")
            // scroll all to left, shift

            elements.forEach { element ->
                element.x += tileSize
                element.animate().xBy(-tileSize-tile0x).duration = 100
            }

            try {
                val temp = boardContent[rowNum].toMutableList()
                boardContent[rowNum].forEachIndexed { i, _ ->
                    if (i == 4) {
                        boardContent[rowNum][i] = temp[0]
                    } else {
                        boardContent[rowNum][i] = temp[i + 1]
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(activity, "Error [OH NO THIS TIME]: $e", Toast.LENGTH_SHORT).show()
            }

            drawBoardX(rowNum)
        } else {
//            Log.e("MainActivity", "enters else")
            // scroll all to right
            elements.forEach { element ->
                element.animate().xBy(-tile0x).duration = 100
            }
        }
    }

    private fun snapY(elements: List<TileView>, colNum: Int) {
        val tile0 = elements[0]
        val tile0y = tile0.y
//        val tile0yTo0y = abs(tile0y - tileSize / 2)
        Log.e("MainActivity", "snapY: $tile0y, ${-tileSize / 2}")
        if (tile0y < -tileSize / 2) {
//            Log.e("MainActivity", "enters if")
            // scroll all to top, shift

            elements.forEach { element ->
                element.y += tileSize
                element.animate().yBy(-tileSize-tile0y).duration = 100
            }

            val temp = boardContent.map { it[colNum] }.toMutableList()
            boardContent.forEachIndexed { i, _ ->
                if (i == 4) {
                    boardContent[i][colNum] = temp[0]
                } else {
                    boardContent[i][colNum] = temp[i + 1]
                }
            }

            drawBoardY(colNum)
        } else {
//            Log.e("MainActivity", "enters else")
            // scroll all to bottom
            elements.forEach { element ->
                element.animate().yBy(-tile0y).duration = 100
            }
        }
    }

    private fun setupBoard() {
        // Get board size
        boardSize = board.width // Is square
        boardX = board.x
        boardY = board.y

        // Get tile size
        tileSize = boardSize / 5

        tileScrollH.forEachIndexed { i, tileList ->
            tileList.forEachIndexed { j, tile ->
                tile.width = tileSize
                tile.height = tileSize

                tile.x = boardX + tileSize * j
                tile.y = boardY + tileSize * i

                // Text
                if (i != 5) {
                    drawBoardX(i, i)
                } else {
                    drawBoardX(0, 5)
                }
            }
        }
    }

    private fun drawBoardX(rowNumSrc: Int, rowNumDest: Int = rowNumSrc) {
        tileScrollH[rowNumDest].forEachIndexed { i, tile ->
            if (i == 5) {
                // Last column, repeat first column
                tile.text = boardContent[rowNumSrc][0].toString()
                tile.setBackgroundColor(boardColor[boardContent[rowNumSrc][0]]!!)
            } else {
                tile.text = boardContent[rowNumSrc][i].toString()
                tile.setBackgroundColor(boardColor[boardContent[rowNumSrc][i]]!!)
            }
        }

//        binding.tvCurrentState.text = boardContent.toString()
    }

    private fun drawBoardY(colNumSrc: Int, colNumDest: Int = colNumSrc) {
        tileScrollV[colNumDest].forEachIndexed { i, tile ->
            if (i == 5) {
                // Last column, repeat first column
                tile.text = boardContent[0][colNumSrc].toString()
                tile.setBackgroundColor(boardColor[boardContent[0][colNumSrc]]!!)
            } else {
                tile.text = boardContent[i][colNumSrc].toString()
                tile.setBackgroundColor(boardColor[boardContent[i][colNumSrc]]!!)
            }
        }

//        binding.tvCurrentState.text = boardContent.toString()
    }

    fun scramble() {
        val randomChars = generateSequence { Random.nextInt(0, 25) }
            .distinct()
            .take(25)
            .map { it + 'A'.code }
            .map { it.toChar() }
            .toList()
            .shuffled()
            .toCharArray()

        for (i in 0..4) {
            for (j in 0..4) {
                boardContent[i][j] = randomChars[i * 5 + j]
            }
        }

        // Draw the board
        for (i in 0..4) {
            drawBoardX(i)
        }
        drawBoardX(0, 5)
    }
}