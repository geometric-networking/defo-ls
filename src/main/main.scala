package defo

import defo.core.Topology
import defo.core.SegmentDB
import defo.core.Demand
import defo.core.Results
import defo.ls.LinearGuidedSearch
    

object DefoLS extends App {
    if (args.length != 3)
        println("I didn't get proper args (topology and demands file, time limit in ms)")
        return -1  // TODO raise exception

    def makeDemand(demandLine: String) = {
        // TODO 
        val src = None
        val dest = None
        val bw = None
        return Demand(src, dest, bw)
    }

    def makeTopology(topologyLine: String) = {
        // TODO
        val src = None
        val dest = None
        val capacity = None
        val weight = None
        return (src, dest, capacity, weight)
    }

    val topologyInfo: (Array[Int], Array[Int], Array[Double], Array[Int]) = 
        Source.fromFile(args(0)).getLines.toArray.map(makeTopology).unzip
    val edgeSrcs = topologyInfo._1
    val edgeDests = topologyInfo._2
    val capacities = topologyInfo._3
    val weights = topologyInfo._4  // weights ~ delays
    var topology = Topology(edgeSrcs, edgeDests)
    var ecmps = SegmentDB(topology, weights)
    val demands: Array[Demand] = Source.fromFile(args(1)).getLines.toArray.map(makeDemand)

    var lgs = LinearGuidedSearch(topology, capacities, ecmps, demands, verbous = true)
    val timeLimitInMS: Long = args(2)
    var lgsResults: Results = lgs.solve(timeLimitInMS, 0)
    print(lgsResults)
}