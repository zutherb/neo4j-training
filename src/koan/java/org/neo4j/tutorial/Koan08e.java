package org.neo4j.tutorial;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Iterator;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.cypher.ExecutionEngine;
import org.neo4j.cypher.ExecutionResult;

/**
 * In this Koan we use the Cypher graph pattern matching language to investigate
 * the regenerations and the history of the Dalek props, with a focus on longer matches
 * and using aggregates to process the returned data.
 */
public class Koan08e
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
    public void shouldFindTheHardestWorkingPropPartInShowbiz() throws Exception
    {
        ExecutionEngine engine = new ExecutionEngine( universe.getDatabase() );
        String cql = null;

        // YOUR CODE GOES HERE

        ExecutionResult result = engine.execute( cql );

        assertHardestWorkingPropParts( result.javaIterator(), "Dalek 1", "shoulder", 15l );

    }

    private void assertHardestWorkingPropParts( Iterator<Map<String, Object>> results, Object... partsAndCounts )
    {
        for ( int index = 0; index < partsAndCounts.length; index = index + 3 )
        {
            Map<String, Object> row = results.next();
            assertEquals( partsAndCounts[index], row.get( "originalprop.prop" ) );
            assertEquals( partsAndCounts[index + 1], row.get( "part.part" ) );
            assertEquals( partsAndCounts[index + 2], row.get( "count(episode.title)" ) );
        }

        assertFalse( results.hasNext() );
    }
}
