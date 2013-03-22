package org.neo4j.tutorial;

import static org.junit.Assert.assertEquals;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.cypher.ExecutionEngine;
import org.neo4j.cypher.ExecutionResult;

/**
 * In this Koan we focus on paths in Cypher.
 */
public class Koan08g
{
    private static EmbeddedDoctorWhoUniverse universe;

    @BeforeClass
    public static void createDatabase() throws Exception
    {
        universe = new EmbeddedDoctorWhoUniverse( new DoctorWhoUniverseGenerator() );
    }

    @AfterClass
    public static void closeTheDatabase()
    {
        universe.stop();
    }

    @Test
    public void shouldFindTheNumberOfEpisodesUsingShortestPath() throws Exception
    {
        // Some free domain knowledge here :-)
        final int first = 1;
        final int mostRecent = 231;

        ExecutionEngine engine = new ExecutionEngine( universe.getDatabase() );
        String cql = null;

        // YOUR CODE GOES HERE

        ExecutionResult result = engine.execute( cql );

        assertEquals( 251, result.javaColumnAs( "episodes" ).next() );
    }
}
