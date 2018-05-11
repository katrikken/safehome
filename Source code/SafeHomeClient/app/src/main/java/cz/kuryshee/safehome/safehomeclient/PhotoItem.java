package cz.kuryshee.safehome.safehomeclient;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.kuryshee.safehome.appcommunicationconsts.AppCommunicationConsts;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Class implements bean containing data about photo from server.
 *
 * @author Ekaterina Kurysheva.
 */
public class PhotoItem {

    Date date;
    Bitmap imageBitmap;
    String name;

    private SimpleDateFormat formatter = new SimpleDateFormat(AppCommunicationConsts.DATE_FORMAT_APP);
    private SimpleDateFormat formatterGUI = new SimpleDateFormat(AppConstants.DATE_USER_FORMAT);

    /**
     * Constructor.
     */
    public PhotoItem(){
        this.date = null;
        this.imageBitmap = null;
        this.name = null;
    }

    /**
     * Getter for the property containing date of the photo creation.
     * @return date.
     */
    public Date getDate() {
        return date;
    }

    /**
     * Getter for the date in Unix Epoch format.
     * @return string containing milliseconds in Unix Epoch format.
     */
    public String getStringUnixEpochDate(){
        return date.getTime() + "";
    }

    /**
     * Getter for the date in format {@link AppConstants#DATE_USER_FORMAT}.
     * @return string containing formatted date.
     */
    public String getDateStringGUI(){
        return formatterGUI.format(date);
    }

    /**
     * Setter for the date property.
     * @param date when the photo was created.
     */
    public void setDate(Date date) {
        this.date = date;
    }

    /**
     * Setter for the date from string containing milliseconds in Unix Epoch format.
     * @param date is a string containing milliseconds in Unix Epoch format.
     */
    public void setDateFromUnixEpochFormatString(String date) {
        this.date = new Date(Long.parseLong(date));
    }

    /**
     * Setter the date from string containing date in {@link AppCommunicationConsts#DATE_FORMAT_APP} format.
     * Cuts off precision of milliseconds to avoid ambiguity.
     * @param date is a string containing date in {@link AppCommunicationConsts#DATE_FORMAT_APP} format.
     * @throws ParseException
     */
    public void setDateFromDateFormatString(String date) throws ParseException{
        if(date.length() > AppCommunicationConsts.DATE_FORMAT_APP.length()){
            date = date.substring(0, AppCommunicationConsts.DATE_FORMAT_APP.length());
        }
        this.date = formatter.parse(date);
    }

    /**
     * Getter for the property containing bitmap of the photo.
     * @return bitmap of the photo.
     */
    public Bitmap getImageBitmap() {
        return imageBitmap;
    }

    /**
     * Setter for the property containing bitmap of the photo.
     * @param imageBitmap bitmap of the photo.
     */
    public void setImageBitmap(Bitmap imageBitmap) {
        this.imageBitmap = imageBitmap;
    }

    /**
     * Setter for the property containing bitmap of the photo.
     * @param bytes bitmap of the photo as byte array.
     */
    public void setImageBitmap(byte[] bytes){
        imageBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    /**
     * Getter for the property containing name of the photo.
     * @return name of the photo.
     */
    public String getName() {
        return name;
    }

    /**
     * Setter for the property containing name of the photo.
     * @param name name of the photo.
     */
    public void setName(String name) {
        this.name = name;
    }
}
