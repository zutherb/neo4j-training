package org.neo4j.tutorial.koan12;

import com.sun.jersey.api.NotFoundException;
import org.neo4j.cypher.ExecutionEngine;
import org.neo4j.cypher.ExecutionResult;
import org.neo4j.graphdb.GraphDatabaseService;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import java.util.Collections;
import java.util.Iterator;


@Path("/{character}")
public class HomePlanetUnmanagedExtension {

    @Context
    private GraphDatabaseService graphDatabaseService;

    @GET
    @Path("/homeplanet")
    public String findHomePlanet(@PathParam("character") String character) {
        ExecutionEngine executionEngine = new ExecutionEngine(graphDatabaseService);
        ExecutionResult result = executionEngine.execute(
                "START character = node:characters(character = {character}) \n" +
                "MATCH character-[:COMES_FROM]->planet " +
                "return planet.planet as pl",
                Collections.<String, Object>singletonMap("character", character));

        System.out.println(result.dumpToString());

        Iterator<Object> iterator = result.javaColumnAs("pl");
        if (iterator.hasNext()) {
            return iterator.next().toString();
        }
        throw new NotFoundException("character not found");
    }
}
