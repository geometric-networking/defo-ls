package defo

import core.Topology
import core.SegmentDB
import core.Demand
import util.Results
import ls.LinkGuidedSearch

import scala.io.Source
import scala.collection.mutable.ArrayBuffer

object Main extends App {

    assert(args.length == 3, "I didn't get proper args (topology and demands file, time limit in ms)")
    Console.println(System.getProperty("user.dir"))
    Console.println(args(0))
    Console.println(args(1))
    Console.println(args(2))
    System.exit(0)

    var edgeSrcs = ArrayBuffer[Int]()
    var edgeDests = ArrayBuffer[Int]()
    var capacities = ArrayBuffer[Double]()
    var weights = ArrayBuffer[Int]()  // weights ~ delays
    for (tLine <- Source.fromFile(args(0)).getLines.toArray) {
        val tLineTokens = tLine.split(",")
        edgeSrcs += tLineTokens(0).toInt
        edgeDests += tLineTokens(1).toInt
        capacities += tLineTokens(2).toDouble
        weights += tLineTokens(3).toInt
    }
    var topology = Topology(edgeSrcs.toArray, edgeDests.toArray)
    var ecmps = SegmentDB(topology, weights.toArray)

    def makeDemand(dLine: String): Demand = {
        val dLineTokens = dLine.split(",")
        return new Demand(dLineTokens(0).toInt, dLineTokens(1).toInt, dLineTokens(2).toDouble)
    }

    val demands: Array[Demand] = Source.fromFile(args(1)).getLines.toArray.map(makeDemand)
    val verbose = true
    var lgs = new LinkGuidedSearch(topology, capacities.toArray, ecmps, demands, verbose)
    val timeLimitInMS: Long = args(2).toLong
    val objective: Double = 0
    var lgsResults: Results = lgs.solve(timeLimitInMS, objective)

    Console.println(lgsResults)
}