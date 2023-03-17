# DEFO - LS

This is a fork of the original code containing the Link Guided Local Search algorithm described in ["Gay, Steven, Renaud Hartert, and Stefano Vissicchio. *Expect the unexpected: Sub-second optimization for segment routing.* (IEEE INFOCOM 2017)"](https://ieeexplore.ieee.org/abstract/document/8056971) that I have made to improve the algorithm's ease of use.

### Changes made to [Original](https://github.com/rhartert-zz/defo-ls)

- Wrapped algorithm in minimal project structure that can be built to produce an executable.
- Improved this README (incl. usage instructions.)


### Installation

1. Install `java`, `scala` and `sbt` for your platform.
2. `git clone https://github.com/geometric-networking/defo-ls.git`
3. `cd defo-ls/`
4. `sbt clean && sbt assembly` (compiles the package and creates a `.jar` executable)
5. your executable `.jar` file should lie somewhere in `target/scala-<your version>/`.

### Usage

0. you'll need to provide a topology file in the [graphml format](http://graphml.graphdrawing.org) that includes nodes with integer node IDs, sa well as edges with "source" and "target" IDs that are attributed with with "datarate" and "delay" values. Also, you'll need a directory containing at least one traffic file containing oen demand per line with the following syntax: `demand_<i> <x> <y> <z>`, where i is the index of the demand, x and y soruce and target IDs, and z the demanded amount.
1. `java -jar <path-to-the-generated-executable> </path/to/topology.graphml> </path/to/traffic_demands/> <max. ms sent optimizing per demand file: int> <output_filename_prefix>`
2. Files will be generated in your current directory.

**Work in progress, more detailed instructions and convenience improments (e.g. some example input files) may follow!**
