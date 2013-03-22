package org.neo4j.tutorial;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

/**
 * This first Koan will introduce you to the tool support available for Neo4j.
 * It will also introduce you to the Doctor Who universe.
 */
public class Koan01
{
    @Test
    public void justEmitsThePathToTheDatabase() throws Exception
    {
        EmbeddedDoctorWhoUniverse universe = new EmbeddedDoctorWhoUniverse( new DoctorWhoUniverseGenerator() );
        assertNotNull( universe.getDatabase() );
        universe.stop();
    }
}
