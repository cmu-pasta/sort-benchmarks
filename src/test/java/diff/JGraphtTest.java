/*
 * Copyright (c) 2017-2018 The Regents of the University of California
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package diff;

import com.pholser.junit.quickcheck.From;
import diff.jgrapht.GraphModel;
import diff.jgrapht.ModelBasedGraphGenerator;
import edu.berkeley.cs.jqf.fuzz.Fuzz;
import edu.berkeley.cs.jqf.fuzz.JQF;
import org.jgrapht.Graph;
import org.jgrapht.GraphTests;
import org.jgrapht.alg.shortestpath.BellmanFordShortestPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.runner.RunWith;

/**
 * @author Rohan Padhye
 */
@RunWith(JQF.class)
public class JGraphtTest {

    //shortest path tests

    @Fuzz
    public void bellmanFord(@From(ModelBasedGraphGenerator.class) @GraphModel(nodes=10, weighted=true) Graph graph) {
        new BellmanFordShortestPath<>(graph).getPaths(1);
    }

    @Fuzz
    public void dijkstra(@From(ModelBasedGraphGenerator.class) @GraphModel(nodes=10, weighted=true) Graph graph) {
        new DijkstraShortestPath<>(graph).getPaths(1);
    }

    //simple graph tests

    @Fuzz
    public void simple(@From(ModelBasedGraphGenerator.class) @GraphModel(nodes=20, edges=1) Graph graph) {
        Assume.assumeFalse(GraphTests.isEmpty(graph));
        Assert.assertTrue(GraphTests.isSimple(graph));
    }

    @Fuzz
    public void empty(@From(ModelBasedGraphGenerator.class) @GraphModel(nodes=20, edges=0, p=0.0) Graph graph) {
        Assert.assertTrue(GraphTests.isEmpty(graph));
    }

    @Fuzz
    public void nonEmpty(@From(ModelBasedGraphGenerator.class) @GraphModel(nodes=20, edges=100) Graph graph) {
        //System.out.println(graph.edgeSet().size()); //TODO this is 190 = 20 choose 2, which is why edgeCount is breaking
        Assert.assertFalse(GraphTests.isEmpty(graph));
    }

    //@Fuzz
    public void edgeCount(@From(ModelBasedGraphGenerator.class) @GraphModel(nodes=20, edges=100) Graph graph) {
        Assert.assertTrue(graph.edgeSet().size() == 100);
    }

    //weighted graph test

    @Fuzz
    public void checkWeights(@From(ModelBasedGraphGenerator.class) @GraphModel(nodes=4, weighted=true) Graph graph) {
        graph.edgeSet().forEach((e) ->
                Assert.assertTrue(graph.getEdgeWeight(e) >= 0 &&
                        graph.getEdgeWeight(e) < 1.0));
    }
}
