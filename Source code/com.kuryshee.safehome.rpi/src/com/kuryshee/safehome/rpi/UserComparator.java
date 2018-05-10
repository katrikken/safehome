package com.kuryshee.safehome.rpi;

import java.util.Comparator;

/**
 * Class implements comparator of instances of {@link UserBean}.
 * 
 * @author Ekaterina Kurysheva
 */
public class UserComparator implements Comparator<UserBean> {
    /**
     * Compares of instances of {@link UserBean}.
     * @param a
     * @param b
     * @return 0, if users are associated with the same RFID tag. 
     * Returns -1 if the a's tag is alphanumerically less than b's tag.
     */
    @Override
    public int compare(UserBean a, UserBean b) {
        return a.getTag().compareTo(b.getTag());
    }
}
