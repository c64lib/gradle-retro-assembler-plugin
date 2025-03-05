/*
MIT License

Copyright (c) 2018-2025 c64lib: The Ultimate Commodore 64 Library
Copyright (c) 2018-2025 Maciej Małecki

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/
package com.github.c64lib.rbt.flows.usecase

import com.github.c64lib.rbt.flows.domain.Flow

data class FlowGraphNode(val flow: Flow, val followUps: List<FlowGraphNode>)

class BuildFlowsGraphService {
  /**
   * Builds a graph of flows. Only flows that are not followed up by any other flow are considered
   * as roots. Cycles are not allowed.
   *
   * @param flows list of flows
   * @return list of nodes representing flows and their follow-ups
   * @throws IllegalArgumentException if a flow is not found
   */
  fun build(flows: List<Flow>): List<FlowGraphNode> {
    val flowMap = flows.associateBy { it.name }
    val stack = mutableSetOf<Flow>()

    fun buildNode(flow: Flow): FlowGraphNode {
      if (flow in stack) {
        throw IllegalArgumentException("Cycle detected at flow ${flow.name}")
      }

      stack.add(flow)
      val followUps =
          flow.feed.map { feed ->
            flowMap[feed] ?: throw IllegalArgumentException("Flow $feed not found")
          }
      val node = FlowGraphNode(flow, followUps.map { buildNode(it) })
      stack.remove(flow)
      return node
    }

    val nodes = flows.map { buildNode(it) }

    val roots = nodes.filter { node -> nodes.none { it.followUps.contains(node) } }

    if (flows.isNotEmpty() && roots.isEmpty()) {
      throw IllegalArgumentException("No roots found")
    }

    return roots
  }
}
