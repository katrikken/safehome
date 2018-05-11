package cz.kuryshee.safehome.safehomeclient;

import com.kuryshee.safehome.appcommunicationconsts.AppCommunicationConsts;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Class representing beans of events from Raspberry Pi.
 *
 * @author Ekaterina Kurysheva.
 */
public class RpiEventItem {

    private SimpleDateFormat formatterGUI = new SimpleDateFormat(AppConstants.DATE_USER_FORMAT);
    private SimpleDateFormat formatter = new SimpleDateFormat(AppCommunicationConsts.DATE_FORMAT_APP);

    private Date date;

    private String eventInfo;

    private String eventLevel;

    /**
     * Converts the date to string with milliseconds in Unix Epoch format.
     * @return string with milliseconds in Unix Epoch format.
     */
    public String getStringUnixEpochDate(){
        return date.getTime() + "";
    }

    /**
     * Getter for the property containing date of the event.
     * @return date.
     */
    public Date getDate() {
        return date;
    }

    /**
     * Getter for the property date of the event
     * @return formatted date in {@link AppConstants#DATE_USER_FORMAT}.
     */
    public String getDateStringGUI(){
        return formatterGUI.format(date);
    }

    /**
     * Setter for the property date
     * @param date
     */
    public void setDate(Date date) {
        this.date = date;
    }

    /**
     * Converts string with milliseconds in Unix Epoch format to date.
     * @param date is a sting containing milliseconds in Unix Epoch format.
     */
    public void setDateFromUnixEpochString(String date) {
        this.date = new Date(Long.parseLong(date));
    }

    /**
     * Sets the date from string containing date in {@link AppCommunicationConsts#DATE_FORMAT_APP} format.
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
     * Getter for the property containing description of the event.
     * @return description of the event.
     */
    public String getEventInfo() {
        return eventInfo;
    }

    /**
     * Setter for the property containing description of the event.
     * @param eventInfo description of the event.
     */
    public void setEventInfo(String eventInfo) {
        this.eventInfo = eventInfo;
    }

    /**
     * Getter for the property containing level of the event.
     * The level is defined by constants {@link AppCommunicationConsts#DANGEROUS} and {@link AppCommunicationConsts#NORMAL}.
     * @return level of the event.
     */
    public String getEventLevel() {
        return eventLevel;
    }

    /**
     * Setter for the property containing level of the event.
     * @param eventLevel one of constants {@link AppCommunicationConsts#DANGEROUS} and {@link AppCommunicationConsts#NORMAL}.
     */
    public void setEventLevel(String eventLevel) {
        this.eventLevel = eventLevel;
    }
}
