package org.neo4j.tutorial;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;

import java.util.HashSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.neo4j.tutorial.matchers.ContainsOnlyHumanCompanions.containsOnlyHumanCompanions;
import static org.neo4j.tutorial.matchers.ContainsOnlySpecificTitles.containsOnlyTitles;

/**
 * In this Koan we start to mix indexing and core API to perform more targeted
 * graph operations. We'll mix indexes and core graph operations to explore the
 * Doctor's universe.
 */
public class Koan05 {

    private static EmbeddedDoctorWhoUniverse universe;

    @BeforeClass
    public static void createDatabase() throws Exception {
        universe = new EmbeddedDoctorWhoUniverse(new DoctorWhoUniverseGenerator());
    }

    @AfterClass
    public static void closeTheDatabase() {
        universe.stop();
    }

    @Test
    public void shouldCountTheNumberOfDoctorsRegeneratedForms() {

        Index<Node> actorsIndex = universe.getDatabase()
                .index()
                .forNodes("actors");
        int numberOfRegenerations = 1;

        // YOUR CODE GOES HERE
        IndexHits<Node> indexHits = actorsIndex.get("actor", "William Hartnell");
        Node currentActor = indexHits.getSingle();

        while (currentActor.hasRelationship(DoctorWhoRelationships.REGENERATED_TO, Direction.OUTGOING)) {
            currentActor = currentActor.getSingleRelationship(DoctorWhoRelationships.REGENERATED_TO, Direction.OUTGOING).getEndNode();
            numberOfRegenerations++;
        }
        assertEquals(11, numberOfRegenerations);
    }

    @Test
    public void shouldFindHumanCompanionsUsingCoreApi() {
        HashSet<Node> humanCompanions = new HashSet<Node>();

        Node doctor = universe.theDoctor();

        for (Relationship companionRel : doctor.getRelationships(DoctorWhoRelationships.COMPANION_OF, Direction.INCOMING)) {
            Node companion = companionRel.getStartNode();
            if (isHuman(companion)) {
                humanCompanions.add(companion);
            }
        }

        int numberOfKnownHumanCompanions = 40;
        assertEquals(numberOfKnownHumanCompanions, humanCompanions.size());
        assertThat(humanCompanions, containsOnlyHumanCompanions());
    }

    private boolean isHuman(Node companion) {
        for (Relationship relationship : companion.getRelationships(DoctorWhoRelationships.IS_A, Direction.OUTGOING)) {
            if (relationship.getEndNode().getProperty("species").equals("Human")) {
                return true;
            }
        }
        return false;
    }

    @Test
    public void shouldFindAllEpisodesWhereRoseTylerFoughtTheDaleks() {
        Index<Node> friendliesIndex = universe.getDatabase()
                .index()
                .forNodes("characters");
        Index<Node> speciesIndex = universe.getDatabase()
                .index()
                .forNodes("species");
        HashSet<Node> episodesWhereRoseFightsTheDaleks = new HashSet<Node>();

        // YOUR CODE GOES HERE
        Node rose = friendliesIndex.get("character", "Rose Tyler").getSingle();

        for (Relationship episodeRel : rose.getRelationships(DoctorWhoRelationships.APPEARED_IN, Direction.OUTGOING)) {
            Node episodeNode = episodeRel.getEndNode();
            if (areDaleksInEpisode(episodeNode)) {
                episodesWhereRoseFightsTheDaleks.add(episodeNode);
            }
        }

        assertThat(
                episodesWhereRoseFightsTheDaleks,
                containsOnlyTitles("Army of Ghosts", "The Stolen Earth", "Doomsday", "Journey's End", "Bad Wolf",
                        "The Parting of the Ways", "Dalek"));
    }

    private boolean areDaleksInEpisode(Node episodeNode) {
        for (Relationship relationship : episodeNode.getRelationships(DoctorWhoRelationships.APPEARED_IN, Direction.INCOMING)) {
            if (relationship.getStartNode().hasProperty("species") && relationship.getStartNode().getProperty("species").equals("Dalek")) {
                return true;
            }
        }
        return false;
    }
}
