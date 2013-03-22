package org.neo4j.tutorial;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.neo4j.tutorial.matchers.ContainsOnlySpecificNodes.containsOnlySpecificNodes;
import static org.neo4j.tutorial.matchers.PathsMatcher.consistPreciselyOf;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.graphalgo.GraphAlgoFactory;
import org.neo4j.graphalgo.PathFinder;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.kernel.Traversal;

/**
 * In this Koan we use some of the pre-canned graph algorithms that come with
 * Neo4j to gain more insight into the Doctor's universe.
 */
public class Koan09
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
    public void shouldRevealTheEpisodesWhereRoseTylerFoughtTheDaleks()
    {
        Node rose = universe.getDatabase()
                .index()
                .forNodes( "characters" )
                .get( "character", "Rose Tyler" )
                .getSingle();
        Node daleks = universe.getDatabase()
                .index()
                .forNodes( "species" )
                .get( "species", "Dalek" )
                .getSingle();
        Iterable<Path> paths = null;

        // YOUR CODE GOES HERE

        assertThat( paths, consistPreciselyOf( rose, knownRoseVersusDaleksEpisodes(), daleks ) );
    }

    private HashSet<Node> knownRoseVersusDaleksEpisodes()
    {
        List<String> roseVersusDaleksEpisodeTitles = Arrays.asList( "Dalek", "Army of Ghosts", "Doomsday",
                "The Parting of the Ways", "The Stolen Earth",
                "Bad Wolf", "Journey's End" );
        HashSet<Node> roseVersusDaleksEpisodes = new HashSet<Node>();
        for ( String title : roseVersusDaleksEpisodeTitles )
        {
            roseVersusDaleksEpisodes.add( universe.getDatabase()
                    .index()
                    .forNodes( "episodes" )
                    .get( "title", title )
                    .getSingle() );
        }
        return roseVersusDaleksEpisodes;
    }

    @Test
    public void shouldFindTheNumberOfMasterRegenerationsTheEasyWay()
    {
        Node delgado = universe.getDatabase()
                .index()
                .forNodes( "actors" )
                .get( "actor", "Roger Delgado" )
                .getSingle();
        Node simm = universe.getDatabase()
                .index()
                .forNodes( "actors" )
                .get( "actor", "John Simm" )
                .getSingle();
        Path path = null;

        // YOUR CODE GOES HERE

        assertNotNull( path );
        int numberOfMasterRegenerations = 8;
        int numberOfActorsFound = path.length() + 1;
        assertEquals( numberOfMasterRegenerations, numberOfActorsFound );
    }

    @Test
    public void shouldRevealEpisodeWhenTennantRegeneratedToSmith()
    {
        Node tennant = universe.getDatabase()
                .index()
                .forNodes( "actors" )
                .get( "actor", "David Tennant" )
                .getSingle();
        Node smith = universe.getDatabase()
                .index()
                .forNodes( "actors" )
                .get( "actor", "Matt Smith" )
                .getSingle();
        Path path = null;

        // YOUR CODE GOES HERE

        assertNotNull( path );
        Node endOfTimeEpisode = universe.getDatabase()
                .index()
                .forNodes( "episodes" )
                .get( "title", "The End of Time" )
                .getSingle();
        assertThat( path, containsOnlySpecificNodes( tennant, smith, endOfTimeEpisode ) );
    }
}
