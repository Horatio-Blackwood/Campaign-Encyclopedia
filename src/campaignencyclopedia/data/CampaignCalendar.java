package campaignencyclopedia.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A class to represent the Campaign's calendar.
 * @author adam
 */
public class CampaignCalendar {

    private final List<Month> m_months;

    /** Creates a new CampainCalendar with default values.      */
    public CampaignCalendar() {
        this(Season.getAsMonths());
    }

    /**
     * Creates a new CampaignCalendar with the specified values.  The required month value for 'Unspecified' will be
     * added for you
     * @param months
     */
    public CampaignCalendar(List<Month> months) {
        if (months == null) {
            throw new IllegalArgumentException("Parameter 'months' cannot be null.");
        }
        if (months.isEmpty()) {
            months = new ArrayList<>(Season.getAsMonths());
        }
        m_months = new ArrayList<>();
        processNewMonths(months);
    }

    /**
     * Returns an unmodifiable list of the months in this calendar.
     * @return an unmodifiable list of the months in this calendar.
     */
    public List<Month> getMonths() {
        return Collections.unmodifiableList(m_months);
    }

    /**
     * Returns the number of months in this calendar.
     * @return the number of months in this calendar.
     */
    public int getMonthCount() {
        return m_months.size();
    }

    /**
     * Returns the month at the given index.  If the index is out of range, exceptions will be thrown.
     * @param index the index of the month to retrieve.
     * @return the month at the given index.
     */
    public Month getMonthForIndex(int index) {
        return m_months.get(index);
    }

    /**
     * Returns the month after the supplied one.
     * @param month the month to get the following month for.
     * @return the month after the supplied one.
     */
    public Month getMonthAfter(Month month) {
        if (hasMonth(month)) {
            // if not the last month...
            if (month.getIndex() != m_months.size() - 1) {
                // return the one after this one.
                return m_months.get(month.getIndex() + 1);
            } else {
                // return the first month,.
                return m_months.get(0);
            }
        } else {
            throw new IllegalStateException("Supplied month does not exist in calendar.");
        }
    }

    /**
     * Returns the month before the supplied one.
     * @param month the month to get the previous month for.
     * @return the month before the supplied one.
     */
    public Month getMonthBefore(Month month) {
        if (hasMonth(month)) {
            // If not the first month...
            if (month.getIndex() != 0) {
                // Return the month previous to this one
                return m_months.get(month.getIndex() - 1);
            } else {
                // Return the last month
                return m_months.get(m_months.size() - 1);
            }
        } else {
            throw new IllegalStateException("Supplied month does not exist in calendar.");
        }
    }

    public void update(List<Month> months) {
        processNewMonths(months);
    }

    public void updateMonths(List<Month> months) {
        m_months.clear();
        m_months.addAll(months);
        Collections.sort(m_months);
    }

    /**
     * Adds the supplied month in its indicated index.  NOTE:  this may shift other months to new indices!
     * @param month the Month to add.
     */
    public void addMonth(Month month) {
        m_months.add(month.getIndex(), month);
    }

    /**
     * Returns true if this calendar has a month of the given name, ignoring case.
     * @param month the month to check for.
     * @return true if this calendar has a month of the given name, ignoring case, and false
     * otherwise or if the supplied name is empty or null.
     */
    public boolean hasMonth(Month month) {
        return m_months.contains(month);
    }

    /**
     *
     * @param months
     */
    private void processNewMonths(List<Month> months) {
        m_months.clear();
        m_months.addAll(months);
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + Objects.hashCode(this.m_months);
        return hash;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CampaignCalendar other = (CampaignCalendar) obj;
        if (!Objects.equals(this.m_months, other.m_months)) {
            return false;
        }
        return true;
    }


}