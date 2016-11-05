package campaignencyclopedia.data;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import toolbox.file.FileTools;

/**
 * A configuration/manager for Relationships.
 * @author adam
 */
public class RelationshipOptionManager {

    /** A Logger. */
    private static final Logger LOGGER = Logger.getLogger(RelationshipOptionManager.class.getName());
    
    /** The location of the relationships.dat file. */
    private static final String RELATIONSHIP_FILE = "./config/relationships.dat";

    /** The Relationship options. */
    private final static List<String> RELATIONSHIPS = new ArrayList<>();

    // Perform initial setup.
    static {
        // If it exists, load the relationships file
        File relationshipFile = new File(RELATIONSHIP_FILE);
        if (relationshipFile.exists()) {
            try {
                RELATIONSHIPS.addAll(FileTools.readAllLinesFromFile(RELATIONSHIP_FILE));
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, "Failed to read relationships from '" + RELATIONSHIP_FILE + "'.", ex);
            }
        } else {
            LOGGER.config("No relationship file detected, using default relationship list.");
            for (RelationshipType rt : RelationshipType.values()) {
                RELATIONSHIPS.add(rt.getDisplayString());
            }
            writeRelationshipFile();
        }
        Collections.sort(RELATIONSHIPS);
    }

    /**
     * Adds the supplied relationship and updates the relationship file.
     * @param relationship the relationship to add.
     */
    public static void addRelationship(String relationship) {
        if (!RELATIONSHIPS.contains(relationship) && relationship != null && !relationship.isEmpty()) {
            RELATIONSHIPS.add(relationship);
            writeRelationshipFile();
        }
    }

    /**
     * Adds the supplied relationships and updates the relationship file.
     * @param relationships the relationships to add any null or empty relationships or those which are already in the
     * configuration are ignored.
     */
    public static void addRelationships(List<String> relationships) {
        boolean changeMade = false;
        // Add any valid previously unknown relationships.
        for (String rel : relationships) {
            if (!RELATIONSHIPS.contains(rel) && rel != null && !rel.isEmpty()) {
                RELATIONSHIPS.add(rel);
                changeMade = true;
            }
        }

        // Update the file if any changes were made.
        if (changeMade) {
            writeRelationshipFile();
        }
    }

    /**
     * Removes the supplied relationship.
     * @param relationship
     */
    public static void removeRelationship(String relationship) {
        RELATIONSHIPS.remove(relationship);
    }

    /**
     * Returns a sorted list of the currently stored relationships.
     * @return a sorted list of the currently stored relationships.
     */
    public static List<String> getRelationships() {
        Collections.sort(RELATIONSHIPS);
        return RELATIONSHIPS;
    }

    /**
     * Clears all relationships and adds the new ones.
     * @param newRelationships the new relationships.
     */
    public static void replaceAllRelationships(List<String> newRelationships) {
        RELATIONSHIPS.clear();
        RELATIONSHIPS.addAll(newRelationships);
        writeRelationshipFile();
    }

    /** Writes the current relationships to the relationship file. */
    private static void writeRelationshipFile() {
        try {
            FileTools.writeFile(RELATIONSHIP_FILE, RELATIONSHIPS);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Failed to write relationships to file '" + RELATIONSHIP_FILE + "'.", ex);
        }
    }
}