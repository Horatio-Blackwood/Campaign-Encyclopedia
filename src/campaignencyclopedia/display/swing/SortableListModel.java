package campaignencyclopedia.display.swing;

import campaignencyclopedia.display.DataFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.swing.AbstractListModel;

/**
 * A List model that supports being sorted, (or not) as well as applying a DataFilter to show or hide data.
 *
 * @author adam
 * @param <E>
 */
public class SortableListModel<E extends Comparable> extends AbstractListModel<E> {

    /** The filter, if any, on this model. */
    private DataFilter<E> m_filter;

    /** The items in this model. */
    private final List<E> m_items;

    /** The items in this model which have passed the filter. */
    private final List<E> m_filteredItems;

    /** True if this model should maintain sorting, false otherwise. */
    private final boolean m_sort;

    /** Creates a new instance of SortedListModel with sorting active. */
    public SortableListModel() {
        this(true);
    }

    /**
     * Creates a new instance of SortableListModel with sorting either active or not if param 'sort' is true or false.
     * @param sort true if this model should maintain sorting.
     */
    public SortableListModel(boolean sort) {
        m_sort = sort;
        m_items = new ArrayList<>();
        m_filteredItems = new ArrayList<>();
    }

    @Override
    public int getSize() {
        if (m_filter != null) {
            return m_filteredItems.size();
        } else {
            return m_items.size();
        }

    }

    @Override
    public E getElementAt(int i) {
        // if the filter is set, return the filtered items index, otherwise return the 'real' item.
        if (m_filter != null) {
            return m_filteredItems.get(i);
        } else {
            return m_items.get(i);
        }
    }

    /**
     * Returns a copy of the items in this model as a List.  THIS METHOD ALWAYS RETURNS ALL DATA, NEVER FILTERED DATA.
     * @return a copy of the items in this model.
     */
    public List<E> getAllElements() {
        return new ArrayList<>(m_items);
    }

    public void addAllElements(Collection<E> items) {
        // If the filter is set
        if (m_filter != null) {
            // for each of the items to add
            for (E e : items) {
                // if it passes the filter
                if (m_filter.accept(e)) {
                    // add it to the filter
                    m_filteredItems.add(e);
                }
            }
            // Then sort and fire the UI update event
            if (m_sort) {
                Collections.sort(m_filteredItems);
            }
            fireContentsChanged(this, 0, m_filteredItems.size());
            // Then add all of the items to the 'rea' data list.
            m_items.addAll(items);
        } else {
            // If no filter is set, just add them all, sort them and update the UI.
            m_items.addAll(items);
            if (m_sort) {
                Collections.sort(m_items);
            }
            fireContentsChanged(this, 0, m_items.size());
        }
    }

    /**
     * Adds an element to this model and fires necessary events to update the UI.
     * @param e the Element to add.
     */
    public void addElement(E e) {
        // If the filter is set
        if (m_filter != null) {
            // and the item passes the filter
            if (m_filter.accept(e)) {
                // add it and fire the update event
                m_filteredItems.add(e);
                // Then sort and fire the UI update event
                if (m_sort) {
                    Collections.sort(m_filteredItems);
                }
                fireContentsChanged(this, m_filteredItems.indexOf(e), getSize());
            }
            // Also add it to the 'real' data collection.
            m_items.add(e);
        } else {
            // If no filter is set, just update the 'real' data and fire the udpate event.
            m_items.add(e);
            if (m_sort) {
                Collections.sort(m_items);
            }
            fireContentsChanged(this, m_items.indexOf(e), getSize());
        }
    }

    /** Clears all data from this SortedListModel and fires necessary events. */
    public void clear() {
        int size = getSize();
        m_items.clear();
        m_filteredItems.clear();
        fireIntervalRemoved(this, 0, size);
    }

    public void removeElement(E e) {
        // If the filter is set, update the filtered list.
        if (m_filter != null) {
            if (m_filteredItems.contains(e)) {
                int index = m_filteredItems.indexOf(e);
                m_filteredItems.remove(e);
                fireIntervalRemoved(this, index, index);
            }
            // Also maintain the 'real' data map.
            m_items.remove(e);
        } else {
            // If no filter is set, just work over the 'real' data.
            if (m_items.contains(e)) {
                int index = m_items.indexOf(e);
                m_items.remove(e);
                fireIntervalRemoved(this, index, index);
            }
        }
    }

    /**
     * Returns true if this model already contains the supplied element.
     * @param element the Element to check.
     * @return true if this model already contains the supplied element, false otherwise.  Always returns falls if E the
     * element is null.
     */
    public boolean contains(E element) {
        if (element == null) {
            return false;
        }
        return m_items.contains(element);
    }


    /**
     * Sets a new DataFilter on this model.  Providing a null filter clears the current filter.
     * @param filter the filter to set, null clears out the existing filter.
     */
    public void setFilter(DataFilter<E> filter) {
        m_filteredItems.clear();
        if (filter != null) {
            m_filter = filter;
            for (E item : m_items) {
                if (filter.accept(item)) {
                    m_filteredItems.add(item);
                }
            }
            fireContentsChanged(this, 0, m_filteredItems.size() - 1);
        } else {
            m_filter = null;
            if (m_sort) {
                Collections.sort(m_items);
            }
            fireContentsChanged(this, 0, m_items.size() - 1);
        }
    }
}