# DEFO - LS

This is a fork of the original code containing the Link Guided Local Search algorithm described in ["Gay, Steven, Renaud Hartert, and Stefano Vissicchio. *Expect the unexpected: Sub-second optimization for segment routing.* (IEEE INFOCOM 2017)"](https://ieeexplore.ieee.org/abstract/document/8056971) that I have made to improve the algorithm's ease of use.

### Changes made to [Original](https://github.com/rhartert-zz/defo-ls)

- Wrapped algorithm in minimal project structure that can be built to produce an executable.
- Added basic IO handling to invoke optimization with a given topology file and a series of Traffic Matrix files, storing results in a separate `out` folder.
- Improved this README (incl. usage instructions.)


### Installation

1. Install `java`, `scala 2.13` and `sbt` for your platform.
2. `git clone https://github.com/geometric-networking/defo-ls.git`
3. `cd defo-ls/`
4. `./compile` (compiles the package and creates a `.jar` executable)
5. your executable `.jar` file is available at `srls.jar`.

### Usage

You'll need to provide a topology file in the [graphml format](http://graphml.graphdrawing.org) named `graph_attr.graphml` that includes nodes with integer node IDs, as well as edges with "source" and "target" IDs that are attributed with with "datarate" and "delay" values. Also, you'll need a directory `TM/` containing at least one traffic file containing at least one demand line with the following syntax: `demand_<i> <x> <y> <z>`, where `i` is the index of the demand, `x` and `y` source and target IDs, and `z` the demanded amount. An example can be found in `data/example`. 

**Command structure:**

`java -jar srls.jar <data folder> <max. ms per optimization> <output_dir>`

**Example Command:**

`java -jar srls.jar data/example 100 my_out` uses the following config:

- data sits in `data/example`
- optimizing for 100ms per provided traffic matrix
- storing the results in `data/example/my_out`

