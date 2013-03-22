package org.neo4j.tutorial;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;
import org.neo4j.cypher.ExecutionEngine;
import org.neo4j.cypher.ExecutionResult;

/**
 * In this Koan we learn how to create, update, and delete nodes and relationships in the
 * database using the Cypher language.
 */
public class Koan08a
{
    @Test
    public void shouldCreateASingleNode()
    {
        ExecutionEngine engine = new ExecutionEngine( DatabaseHelper.createDatabase() );
        String cql = null;

        // YOUR CODE GOES HERE

        engine.execute( cql );

        final ExecutionResult executionResult = engine.execute( "START n=node(*) return n" );

        final int oneNewNodePlusTheReferenceNodeExpected = 2;
        assertEquals( oneNewNodePlusTheReferenceNodeExpected, executionResult.size() );
    }

    @Test
    public void shouldCreateASingleNodeWithSomeProperties()
    {
        ExecutionEngine engine = new ExecutionEngine( DatabaseHelper.createDatabase() );
        String cql = null;

        // YOUR CODE GOES HERE

        engine.execute( cql );

        final ExecutionResult executionResult = engine.execute(
                "START n=node(*) WHERE has(n.firstname) AND n.firstname = 'Tom' AND  has(n.lastname) AND n.lastname = 'Baker' return n" );

        assertEquals( 1, executionResult.size() );
    }

    @Test
    public void shouldCreateASimpleConnectedGraph()
    {
        ExecutionEngine engine = new ExecutionEngine( DatabaseHelper.createDatabase() );
        String cql = null;

        // YOUR CODE GOES HERE

        engine.execute( cql );

        final ExecutionResult executionResult = engine.execute(
                "START a=node(*) \n" +
                        "MATCH a<-[:ENEMY_OF]-b \n" +
                        "WHERE has(a.character) AND a.character='Doctor' AND has(b.character) AND b.character = 'Master' \n" +
                        "RETURN a, b \n" );

        assertFalse( executionResult.isEmpty() );
    }
}
