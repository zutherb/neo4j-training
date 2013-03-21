package org.neo4j.tutorial;

import static org.neo4j.tutorial.ActorBuilder.actor;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;

public class Actors
{

    private final GraphDatabaseService db;

    public Actors( GraphDatabaseService db )
    {
        this.db = db;
    }

    public void insert()
    {
        Transaction tx = db.beginTx();
        try
        {
            actor( "David Tennant" ).wikipedia( "http://en.wikipedia.org/wiki/David_Tennant" ).played(
                    "Doctor" ).salary( 1000000 ).fact( db );
            actor( "Matt Smith" ).wikipedia( "http://en.wikipedia.org/wiki/Matt_Smith_(actor)" ).played(
                    "Doctor" ).salary( 200000 ).fact( db );
            actor( "Alex Kingston" ).wikipedia( "http://en.wikipedia.org/wiki/Alex_Kingston" ).played(
                    "River Song" ).fact( db );
            actor( "Karen Gillan" ).played( "Amy Pond" ).fact( db );
            actor( "Arthur Darvill" ).played( "Rory Williams" ).fact( db );
            actor( "Freema Agyeman" ).played( "Martha Jones", "Adeola Oshodi" ).fact( db );
            actor( "Jenna-Louise Coleman" ).played( "Oswin Oswald" ).fact( db );
            actor( "Sophie Aldred" ).played( "Ace" ).fact( db );
            actor( "Timothy Dalton" ).played( "Rassilon" ).fact( db );
            actor( "Richard Mathews" ).played( "Rassilon" ).fact( db );

            tx.success();
        }
        finally
        {
            tx.finish();
        }
    }
}
