package cz.kuryshee.safehome.safehomeclient;

import java.util.Comparator;

/**
 * Sorts {@link RpiEventItem} in descending order by Date.
 * @author Ekaterina Kurysheva
 */
public class RpiEventItemComparator implements Comparator<RpiEventItem> {
    /**
     * Compares two {@link RpiEventItem} instances.
     * @param a
     * @param b
     * @return Returns 0 if the events happened at the same time by millisecond, returns -1, if a is more recent than b, 1 otherwise.
     */
    @Override
    public int compare(RpiEventItem a, RpiEventItem b) {
        return a.getDate().compareTo(b.getDate()) * (-1);
    }
}
