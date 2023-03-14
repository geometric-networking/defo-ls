package defo

import core.Topology
import core.SegmentDB
import core.Demand
import util.Results
import ls.LinkGuidedSearch
import ls.SRPath

import scala.io.Source
import scala.collection.mutable.ArrayBuffer
import java.io.FileNotFoundException
import java.io.IOException
import java.io.PrintWriter
import java.io.File
import defo.ls.SRPath

object Main extends App {

    assert(args.length == 4, "I didn't get proper args (topology file, demands file, time limit in ms, out file path)")

    def readFileLines(filename: String): Array[String] = {
        var fileLines: Array[String] = Array[String]()
        try {
            fileLines = Source.fromFile(filename).getLines.toArray
        } catch {
            case e: FileNotFoundException => Console.println("can't find the file '%s'".format(filename))
            case e: IOException => Console.println("error reading file '%s'".format(filename))
        }
        if (fileLines.length < 1) {
            Console.println("reading file '%s' resulted in 0 read lines".format(filename))
            sys.exit(1)
        }
        return fileLines
    }

    var edgeSrcs = ArrayBuffer[Int]()
    var edgeDests = ArrayBuffer[Int]()
    var capacities = ArrayBuffer[Double]()
    var weights = ArrayBuffer[Int]()  // weights ~ delays
    for (tLine <- readFileLines(args(0))) {
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

    val demands: Array[Demand] = readFileLines(args(1)).map(makeDemand)
    val verbose = true
    var lgs = new LinkGuidedSearch(topology, capacities.toArray, ecmps, demands, verbose)
    val timeLimitInMS: Long = args(2).toLong
    val objective: Double = 0
    var lgsResults: Results = lgs.solve(timeLimitInMS, objective)
    var srPaths: Array[SRPath] = lgs.srPaths

    // result handling
    val pw = new PrintWriter(new File(args(3)))
    for (srPath <- srPaths) {
        pw.write(srPath.toString() + "\n")
    }
    pw.close()
}