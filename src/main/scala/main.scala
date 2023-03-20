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
import java.io.{File => JFile}

object Main extends App {

    def getListOfFiles(dir: JFile): Array[(String, String)] = {
        val files = dir.listFiles.filter(_.isFile)
        val filepaths = files.map(_.getPath).toArray
        val filenames = files.map(_.getName).toArray
        return filepaths zip filenames
    }

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

    def makeDemand(dLine: String): Demand = {
        val dLineTokens = dLine.split(",")
        return new Demand(dLineTokens(0).toInt, dLineTokens(1).toInt, dLineTokens(2).toDouble)
    }

    def getTopologyInfo(topologyFileLines: Array[String]): (Topology, Array[Int], Array[Double]) = {
        var edgeSrcs = ArrayBuffer[Int]()
        var edgeDests = ArrayBuffer[Int]()
        var weights = ArrayBuffer[Int]()  // weights ~ delays
        var capacities = ArrayBuffer[Double]()

        def allIndicesOf(str: String, c: Char): List[Int] = {
            return str.zipWithIndex.filter(pair => pair._1 == c).map(pair => pair._2).toList
        }

        def getKeyId(tLine: String): String = {
            val idIdx = tLine.indexOf("id")
            val allQuotesFromId = allIndicesOf(tLine.substring(idIdx), '"')
            val res: String = tLine.slice(idIdx + allQuotesFromId(0) + 1, idIdx + allQuotesFromId(1))
            return res
        }

        def extractNodes(tLine: String): (Int, Int) = {
            val srcIdx: Int = tLine.indexOf("source")
            val allQuotesFromSrc = allIndicesOf(tLine.substring(srcIdx), '"')
            val src: Int = tLine.slice(srcIdx + allQuotesFromSrc(0) + 1, srcIdx + allQuotesFromSrc(1)).toInt

            val dstIdx: Int = tLine.indexOf("target")
            val allQuotesFromDst = allIndicesOf(tLine.substring(dstIdx), '"')
            val dst: Int = tLine.slice(dstIdx + allQuotesFromDst(0) + 1, dstIdx + allQuotesFromDst(1)).toInt
            
            return (src, dst)
        }

        def extractValueStr(tLine: String): String = {
            val rangleIdx: Int = tLine.indexOf(">")
            val langleIdx: Int = tLine.substring(rangleIdx).indexOf("<")
            val res: String = tLine.substring(rangleIdx + 1, rangleIdx + langleIdx)
            return res
        }

        var delayKey: String = "NO_KEY"
        var datarateKey: String = "NO_KEY"

        for (tLine <- topologyFileLines) {
            tLine match {
                case tl if tLine contains "delay" => {
                    val delayKeyId = getKeyId(tl)
                    delayKey = s"key=${'"'}${delayKeyId}${'"'}"
                }
                case tl if tLine contains "datarate" => {
                    val datarateKeyId = getKeyId(tl)
                    datarateKey = s"key=${'"'}${datarateKeyId}${'"'}"
                }
                case tl if tLine contains "<edge" => {
                    val srcAndDst = extractNodes(tl)
                    edgeSrcs += srcAndDst._1
                    edgeDests += srcAndDst._2
                }
                case tl if tLine contains delayKey => {
                    weights += extractValueStr(tl).toInt
                }
                case tl if tLine contains datarateKey => {
                    capacities += extractValueStr(tl).toDouble
                }
                case _ => {}
            }
        }
        assert(edgeSrcs.length == edgeDests.length 
            && edgeSrcs.length == weights.length 
            && edgeSrcs.length == capacities.length, 
            "error reading topology file: mismatching amount of src/dest nodes, weights and capacities")

        var topology = Topology(edgeSrcs.toArray, edgeDests.toArray)
        return (topology, weights.toArray, capacities.toArray)
    }

    def getDemands(demandFileLines: Array[String]): Array[Demand] = {
        var demands = ArrayBuffer[Demand]()
        for (dLine <- demandFileLines) {
            if (dLine.startsWith("demand_")) {
                val dLineTokens = dLine.split(" ")  // ("demand_i", "src", "dest", "amount")
                demands += new Demand(dLineTokens(1).toInt, dLineTokens(2).toInt, dLineTokens(3).toDouble)
            }
        }
        assert(demands.length > 0, "I have read 0 demands from demand file")
        return demands.toArray
    }

    // arg parsing
    assert(args.length == 3, "I didn't get proper args (path to data folder, time limit in ms, out subdir name in data folder)")
    val dataDirStr: String = args(0)
    val topologyFile: String = dataDirStr + "/graph_attr.graphml"
    val demandsDir: JFile = new JFile(dataDirStr + "/TM")
    val outDir: JFile = new JFile(dataDirStr + "/" + args(2))
    assert(!outDir.exists(), "outDir already exists, please choose a different name")
    outDir.mkdir()
    val timeLimitInMS: Long = args(1).toLong

    // get information for solver
    var topologyFileLines: Array[String] = readFileLines(topologyFile)
    var topologyInfo: (Topology, Array[Int], Array[Double]) = getTopologyInfo(topologyFileLines)
    var topology: Topology = topologyInfo._1
    var weights: Array[Int] = topologyInfo._2
    var capacities: Array[Double] = topologyInfo._3
    var ecmps: SegmentDB = SegmentDB(topology, weights.toArray)
    val verbose: Boolean = true
    val objective: Double = 0

    // solve for each demand file in demands given directory
    for ((demandFilepath, demandFilename) <- getListOfFiles(demandsDir)) {
        val demands: Array[Demand] = getDemands(readFileLines(demandFilepath))

        // solve
        var lgs = new LinkGuidedSearch(topology, capacities.toArray, ecmps, demands, verbose)
        var lgsResults: Results = lgs.solve(timeLimitInMS, objective)
        var srPaths: Array[SRPath] = lgs.srPaths

        // write results
        val pw = new PrintWriter((outDir + "/" + demandFilename).toString())
        for (srPath <- srPaths) {
            pw.write(srPath.toString() + "\n")
        }
        pw.close()
    }
}