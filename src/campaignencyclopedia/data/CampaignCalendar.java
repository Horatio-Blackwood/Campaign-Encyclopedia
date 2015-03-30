package campaignencyclopedia.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 *
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

    public List<Month> getMonths() {
        return m_months;
    }

    public void update(List<Month> months) {
        processNewMonths(months);
    }

    public void updateMonths(List<Month> months) {
        m_months.clear();
        if (!months.contains(Month.UNSPECIFIED)) {
            m_months.add(Month.UNSPECIFIED);
        }
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
        // If the Unspecified month is already in this calendar remove it and re-add it to ensure it is at index '0'
        while (months.contains(Month.UNSPECIFIED)) {
            months.remove(Month.UNSPECIFIED);
        }
        m_months.add(Month.UNSPECIFIED);
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