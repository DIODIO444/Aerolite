/*
 * liuli-pathfinding (MIT)
 * A* pathfinding implementation adapted for FDPClient.
 */
package me.liuli.path

import net.ccbluex.liquidbounce.utils.block.MinecraftWorldProvider
import kotlin.math.abs

/**
 * Grid based A* pathfinder over the Minecraft world.
 *
 * @param start the starting cell
 * @param end the goal cell
 * @param neighbors the neighbour offsets to expand (use [COMMON_NEIGHBORS] or [DIAGONAL_NEIGHBORS])
 * @param worldProvider world accessor used to test whether a cell is blocked
 */
class Pathfinder(
    private val start: Cell,
    private val end: Cell,
    private val neighbors: Array<Cell>,
    private val worldProvider: MinecraftWorldProvider
) {

    companion object {
        /** The six orthogonal directions. */
        val COMMON_NEIGHBORS = arrayOf(
            Cell(0, 0, 1), Cell(0, 0, -1),
            Cell(1, 0, 0), Cell(-1, 0, 0),
            Cell(0, 1, 0), Cell(0, -1, 0)
        )

        /** Orthogonal directions plus horizontal/vertical diagonals (26-neighbour). */
        val DIAGONAL_NEIGHBORS = COMMON_NEIGHBORS + arrayOf(
            Cell(1, 0, 1), Cell(1, 0, -1), Cell(-1, 0, 1), Cell(-1, 0, -1),
            Cell(1, 1, 0), Cell(-1, 1, 0), Cell(0, 1, 1), Cell(0, 1, -1),
            Cell(1, -1, 0), Cell(-1, -1, 0), Cell(0, -1, 1), Cell(0, -1, -1),
            Cell(1, 1, 1), Cell(1, 1, -1), Cell(-1, 1, 1), Cell(-1, 1, -1),
            Cell(1, -1, 1), Cell(1, -1, -1), Cell(-1, -1, 1), Cell(-1, -1, -1)
        )
    }

    /**
     * Runs the search, capped at [maxLoops] expansions. Returns the path (inclusive of both endpoints)
     * or an empty list when no path is found within the budget.
     */
    fun findPath(maxLoops: Int = 3000): ArrayList<Cell> {
        val open = ArrayList<Cell>()
        val closed = HashSet<Cell>()
        val cameFrom = HashMap<Cell, Cell>()
        val gScore = HashMap<Cell, Int>()
        val fScore = HashMap<Cell, Int>()

        open.add(start)
        gScore[start] = 0
        fScore[start] = heuristic(start, end)

        var loops = 0
        while (open.isNotEmpty() && loops < maxLoops) {
            loops++
            val current = open.minByOrNull { fScore[it] ?: Int.MAX_VALUE } ?: break

            if (current == end) {
                return reconstruct(cameFrom, current)
            }

            open.remove(current)
            closed.add(current)

            for (n in neighbors) {
                val neighbor = Cell(current.x + n.x, current.y + n.y, current.z + n.z)
                if (closed.contains(neighbor)) continue
                if (worldProvider.isBlocked(neighbor.x, neighbor.y, neighbor.z)) continue

                val tentativeG = (gScore[current] ?: Int.MAX_VALUE) + 1
                if (tentativeG < (gScore[neighbor] ?: Int.MAX_VALUE)) {
                    cameFrom[neighbor] = current
                    gScore[neighbor] = tentativeG
                    fScore[neighbor] = tentativeG + heuristic(neighbor, end)
                    if (!open.contains(neighbor)) open.add(neighbor)
                }
            }
        }
        return ArrayList()
    }

    private fun heuristic(a: Cell, b: Cell): Int =
        abs(a.x - b.x) + abs(a.y - b.y) + abs(a.z - b.z)

    private fun reconstruct(cameFrom: HashMap<Cell, Cell>, current: Cell): ArrayList<Cell> {
        val path = ArrayList<Cell>()
        var c: Cell? = current
        while (c != null) {
            path.add(c)
            c = cameFrom[c]
        }
        path.reverse()
        return path
    }
}
