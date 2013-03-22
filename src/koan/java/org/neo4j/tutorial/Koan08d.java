package org.neo4j.tutorial;

import static junit.framework.Assert.assertEquals;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.cypher.ExecutionEngine;
import org.neo4j.cypher.ExecutionResult;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

public class Koan08d
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
    public void shouldRemoveCaptainKirkFromTheDatabase()
    {
        polluteUniverseWithStarTrekData();
        ExecutionEngine engine = new ExecutionEngine( universe.getDatabase() );
        String cql = null;

        // YOUR CODE GOES HERE

        engine.execute( cql );

        final ExecutionResult executionResult = engine.execute( deletedKirkQuery );

        assertEquals( 0, executionResult.size() );
    }

    @Test
    public void shouldRemoveSalaryDataFromDoctorActors()
    {
        polluteUniverseWithStarTrekData();
        ExecutionEngine engine = new ExecutionEngine( universe.getDatabase() );
        String cql = null;

        // YOUR CODE GOES HERE

        engine.execute( cql );

        final ExecutionResult executionResult = engine.execute(
                "START doctor=node:characters(character='Doctor') " +
                        "MATCH doctor<-[:PLAYED]-actor " +
                        "WHERE has(actor.salary) " +
                        "RETURN actor.salary" );

        assertEquals( 0, executionResult.size() );
    }


    private void polluteUniverseWithStarTrekData()
    {
        GraphDatabaseService db = universe.getDatabase();
        Transaction tx = db.beginTx();
        Node captainKirk = null;
        try
        {
            Node theDoctor = universe.theDoctor();

            captainKirk = db.createNode();
            captainKirk.setProperty( "firstname", "James" );
            captainKirk.setProperty( "initial", "T" );
            captainKirk.setProperty( "lastname", "Kirk" );

            captainKirk.createRelationshipTo( theDoctor, DynamicRelationshipType.withName( "COMPANION_OF" ) );

            tx.success();
        }
        finally
        {
            tx.finish();
        }
    }

    private static final String deletedKirkQuery = "START doctor=node:characters(character='Doctor') " +
            "MATCH doctor<-[:COMPANION_OF]-companion " +
            "WHERE has(companion.firstname) AND companion.firstname='James' " +
            "AND has(companion.initial) AND companion.initial='T' " +
            "AND has(companion.lastname) AND companion.lastname='Kirk' " +
            "RETURN companion";
}
