package com.example.poolover_jinston

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.RelativeLayout
import androidx.fragment.app.Fragment
import com.example.poolover_jinston.databinding.FragmentGameBinding
import kotlin.math.abs

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

    private var tileCount = 2

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

    private val tileScrollH: MutableList<MutableList<TileView>> = mutableListOf()
    private val tileScrollV: MutableList<MutableList<TileView>> = mutableListOf()

    private lateinit var boardContent: List<MutableList<String>>
    private val boardTextContent = mutableListOf<String>()
    private lateinit var boardColor: HashMap<String, Int>

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

        tileCount = arguments?.getInt("tileCount") ?: 5
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

        if (tileCount * tileCount <= 26) {
            for (i in 0 until tileCount * tileCount) {
                boardTextContent.add((i + 'A'.code).toChar().toString())
            }
        } else {
            for (i in 1 .. tileCount * tileCount) {
                boardTextContent.add(i.toString())
            }
        }

        // Partition the boardTextContent into tileCount x tileCount
        boardContent = boardTextContent.chunked(tileCount) { it.toMutableList() }

        // Setup color coding
        boardColor = ColourGenerator.generateTileColours(boardTextContent, tileCount)

        // Setup tileScrollH and tileScrollV
        for (i in 0 .. tileCount) {
            tileScrollH.add(mutableListOf())
            tileScrollV.add(mutableListOf())
            // The last one is for overflow
        }

        // Setup tiles:
        for (i in 0..tileCount) {
            for (j in 0..tileCount) {
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

            if (activity.startTime == 0L) {
                activity.startTime = System.currentTimeMillis()
                activity.setupTimer()
            }

            activity.checkIsSolved(boardContent, boardTextContent)
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
        if (elements[tileCount].x > boardSize) {
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
        if (elements[tileCount].y > boardSize) {
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
        val temp = boardContent[rowNum].toMutableList()
        if (direction == 1) {
            boardContent[rowNum].forEachIndexed { i, _ ->
                if (i == tileCount-1) {
                    boardContent[rowNum][i] = temp[0]
                } else {
                    boardContent[rowNum][i] = temp[i + 1]
                }
            }
        } else {
            boardContent[rowNum].forEachIndexed { i, _ ->
                if (i == 0) {
                    boardContent[rowNum][i] = temp[tileCount-1]
                } else {
                    boardContent[rowNum][i] = temp[i - 1]
                }
            }
        }

        drawBoardX(rowNum)
    }

    private fun shiftY(colNum: Int, direction: Int) {
        val temp = boardContent.map { it[colNum] }.toMutableList()
        if (direction == 1) {
            boardContent.forEachIndexed { i, _ ->
                if (i == tileCount-1) {
                    boardContent[i][colNum] = temp[0]
                } else {
                    boardContent[i][colNum] = temp[i + 1]
                }
            }
        } else {
            boardContent.forEachIndexed { i, _ ->
                if (i == 0) {
                    boardContent[i][colNum] = temp[tileCount-1]
                } else {
                    boardContent[i][colNum] = temp[i - 1]
                }
            }
        }

        drawBoardY(colNum)
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

            val temp = boardContent[rowNum].toMutableList()
            boardContent[rowNum].forEachIndexed { i, _ ->
                if (i == tileCount-1) {
                    boardContent[rowNum][i] = temp[0]
                } else {
                    boardContent[rowNum][i] = temp[i + 1]
                }
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
                if (i == tileCount-1) {
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
        tileSize = boardSize / tileCount

        tileScrollH.forEachIndexed { i, tileList ->
            tileList.forEachIndexed { j, tile ->
                tile.width = tileSize
                tile.height = tileSize

                tile.x = boardX + tileSize * j
                tile.y = boardY + tileSize * i

                tile.textSize = tileSize / 4f

                // Text
                if (i != tileCount) {
                    drawBoardX(i, i)
                } else {
                    drawBoardX(0, tileCount)
                }
            }
        }
    }

    private fun drawBoardX(rowNumSrc: Int, rowNumDest: Int = rowNumSrc) {
        tileScrollH[rowNumDest].forEachIndexed { i, tile ->
            if (i == tileCount) {
                // Last column, repeat first column
                tile.text = boardContent[rowNumSrc][0]
                tile.setBackgroundColor(boardColor[boardContent[rowNumSrc][0]]!!)
            } else {
                tile.text = boardContent[rowNumSrc][i]
                tile.setBackgroundColor(boardColor[boardContent[rowNumSrc][i]]!!)
            }
        }

//        binding.tvCurrentState.text = boardContent.toString()
    }

    private fun drawBoardY(colNumSrc: Int, colNumDest: Int = colNumSrc) {
        tileScrollV[colNumDest].forEachIndexed { i, tile ->
            if (i == tileCount) {
                // Last column, repeat first column
                tile.text = boardContent[0][colNumSrc]
                tile.setBackgroundColor(boardColor[boardContent[0][colNumSrc]]!!)
            } else {
                tile.text = boardContent[i][colNumSrc]
                tile.setBackgroundColor(boardColor[boardContent[i][colNumSrc]]!!)
            }
        }

//        binding.tvCurrentState.text = boardContent.toString()
    }

    fun scramble() {
        val shuffled = boardContent.flatten().shuffled()

        for (i in 0 until tileCount) {
            for (j in 0 until tileCount) {
                boardContent[i][j] = shuffled[i * tileCount + j]
            }
        }

        // Draw the board
        for (i in 0 until tileCount) {
            drawBoardX(i)
        }
        drawBoardX(0, tileCount)
    }
}