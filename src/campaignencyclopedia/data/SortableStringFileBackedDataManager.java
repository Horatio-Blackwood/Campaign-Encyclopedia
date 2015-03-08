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
 *
 * @author adam
 */
public class SortableStringFileBackedDataManager {

    protected final List<String> DATA = new ArrayList<>();
    private final File m_dataFile;
    private final StringDataManagerDefaultProvider m_provider;
    private final boolean m_sort;
    private static final Logger LOGGER = Logger.getLogger(SortableStringFileBackedDataManager.class.getName());


    public SortableStringFileBackedDataManager(String file, StringDataManagerDefaultProvider provider, boolean sort) {
        m_provider = provider;
        m_dataFile = new File(file);
        m_sort = sort;
        loadDataFile();
    }


    /**
     * Adds the supplied item and updates the relationship file.
     * @param item the item to add.
     */
    public void addItem(String item) {
        if (!DATA.contains(item) && item != null && !item.isEmpty()) {
            DATA.add(item);
            writeDataFile();
        }
    }

    public void addItems(List<String> toAdd) {
        boolean changeMade = false;
        // Add any valid previously unknown items.
        for (String item : toAdd) {
            if (!DATA.contains(item) && item != null && !item.isEmpty()) {
                DATA.add(item);
                changeMade = true;
            }
        }

        // Update the file if any changes were made.
        if (changeMade) {
            writeDataFile();
        }
    }

    public void removeItem(String toRemove) {
        DATA.remove(toRemove);
    }

    public List<String> getData() {
        if (m_sort) {
            Collections.sort(DATA);
        }
        return DATA;
    }

    public void replaceAllItems(List<String> newData) {
        DATA.clear();
        DATA.addAll(newData);
        writeDataFile();
    }

    /** Loads any data from the file if it exists. */
    private void loadDataFile() {
        // If it exists, load the relationships file
        if (m_dataFile.exists()) {
            try {
                DATA.addAll(FileTools.readAllLinesFromFile(m_dataFile.getAbsolutePath()));
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, "Failed to read data from " + m_dataFile + ".", ex);
            }
        } else {
            // File did not exist.
            LOGGER.config("No data file detected, using default data list.");
            DATA.addAll(m_provider.getDefaultData());
            writeDataFile();
        }
        if (m_sort) {
            Collections.sort(DATA);
        }
    }

    /** Writes the current relationships to the relationship file. */
    private void writeDataFile() {
        try {
            FileTools.writeFile(m_dataFile.getAbsolutePath(), DATA);
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Failed to write relationships from " + m_dataFile.getAbsolutePath() + ".", ex);
        }
    }
}
