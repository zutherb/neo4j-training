package org.neo4j.tutorial;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.neo4j.tutorial.matchers.ContainsOnlySpecificSpecies.containsOnlySpecies;
import static org.neo4j.tutorial.matchers.ContainsSpecificCompanions.contains;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;

/**
 * This Koan will introduce indexing based on the built-in index framework based
 * on Lucene. It'll give you a feeling for the wealth of bad guys the Doctor has
 * faced.
 */
public class Koan03
{

    private static EmbeddedDoctorWhoUniverse universe;
    private static GraphDatabaseService database;

    @BeforeClass
    public static void createDatabase() throws Exception
    {
        universe = new EmbeddedDoctorWhoUniverse( new DoctorWhoUniverseGenerator() );
        database = universe.getDatabase();
    }

    @AfterClass
    public static void closeTheDatabase()
    {
        universe.stop();
    }

    @Test
    public void shouldRetrieveCharactersIndexFromTheDatabase()
    {
        Index<Node> characters = null;

        Transaction tx = database.beginTx();
        try
        {
            characters = database.index().forNodes("characters");
            tx.success();
        }
        finally
        {
            tx.finish();
        }

        // YOUR CODE GOES HERE

        assertNotNull( characters );
        assertThat(
                characters,
                contains( "Master", "River Song", "Rose Tyler", "Adam Mitchell", "Jack Harkness", "Mickey Smith",
                        "Donna Noble", "Martha Jones" ) );
    }

    @Test
    public void addingToAnIndexShouldBeHandledAsAMutatingOperation()
    {
        Node abigailPettigrew = createAbigailPettigrew( database );

        assertNull( database.index()
                .forNodes( "characters" )
                .get( "character", "Abigail Pettigrew" )
                .getSingle() );

        Transaction tx = database.beginTx();
        try
        {
            Index<Node> characters = database.index().forNodes("characters");
            characters.add(abigailPettigrew, "character", "Abigail Pettigrew");
            tx.success();
        }
        finally
        {
            tx.finish();
        }

        assertNotNull( database.index()
                .forNodes( "characters" )
                .get( "character", "Abigail Pettigrew" )
                .getSingle() );
    }

    @Test
    public void shouldFindSpeciesBeginningWithCapitalLetterSAndEndingWithLowerCaseLetterNUsingLuceneQuery() throws Exception
    {
        IndexHits<Node> species = null;

        Transaction tx = database.beginTx();
        try
        {
            species = database.index().forNodes("species").query("species", "S*n");
            tx.success();
        }
        finally
        {
            tx.finish();
        }

        assertThat( species, containsOnlySpecies( "Silurian", "Slitheen", "Sontaran", "Skarasen" ) );
    }

    /**
     * In this example, it's more important to understand what you *don't* have
     * to do, rather than the work you explicitly have to do. Sometimes indexes
     * just do the right thing...
     */
    @Test
    public void shouldEnsureDatabaseAndIndexInSyncWhenCyberleaderIsDeleted() throws Exception
    {
        GraphDatabaseService db = universe.getDatabase();
        Node cyberleader = retriveCyberleaderFromIndex( db );

        Transaction tx = database.beginTx();
        try
        {
            database.index().forNodes("characters").remove(cyberleader, "character", "Cyberleader");
            for(Relationship relationship : cyberleader.getRelationships()){
                relationship.delete();
            }
            cyberleader.delete();
            tx.success();
        }
        finally
        {
            tx.finish();
        }

        assertNull( "Cyberleader has not been deleted from the characters index.", retriveCyberleaderFromIndex( db ) );

        try
        {
            db.getNodeById( cyberleader.getId() );
            fail( "Cyberleader has not been deleted from the database." );
        }
        catch ( NotFoundException nfe )
        {
        }
    }

    private Node retriveCyberleaderFromIndex( GraphDatabaseService db )
    {
        return db.index()
                .forNodes( "characters" )
                .get( "character", "Cyberleader" )
                .getSingle();
    }

    private Node createAbigailPettigrew( GraphDatabaseService db )
    {
        Node abigailPettigrew;
        Transaction tx = db.beginTx();
        try
        {
            abigailPettigrew = db.createNode();
            abigailPettigrew.setProperty( "character", "Abigail Pettigrew" );
            tx.success();
        }
        finally
        {
            tx.finish();
        }
        return abigailPettigrew;
    }
}
