package cz.kuryshee.safehome.safehomeclient;


import java.util.Comparator;

/**
 * Sorts {@link PhotoItem} in descending order by date.
 * @author Ekaterina Kurysheva
 */
public class PhotoItemComparator implements Comparator<PhotoItem> {
    /**
     * Compares dates of photo creation
     * @param a
     * @param b
     * @return 0, if values are equal, -1, if a is more recent, than b.
     */
    @Override
    public int compare(PhotoItem a, PhotoItem b) {
        return a.getDate().compareTo(b.getDate()) * (-1);
    }
}
