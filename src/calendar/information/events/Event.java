package calendar.information.events;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Holds data of a BYU-I event. This includes:
 *   title, description, location, time, date, category, and imageLink
 * @author Grant
 */
public class Event {
    private String title = "";
    private String description = "";
    private String location = "";
    private String time = "";
    private String startTime = "";
    private String endTime = "";
    private String date = "";
    private String category = "";
    private List<String> calendars = new ArrayList<>();
    private URL imageLink = null;
    
    /**
     * Checks to make that the event has all the required data to be considered an event.
     * @return boolean
     */
    public boolean hasRequiredInfo() {
        return (!title.equals("") && !location.equals("") && !time.equals("") && !date.equals(""));
    }
    
    /**
     * Adds a calendar as a String to the calendars List.
     * @param calendar 
     */
    public void addCalendar(String calendar) {
        calendars.add(calendar);
    }
    
    /**
     * Returns the calendar List.
     * @return calendars
     */
    public List<String> getCalendarList() {
        return calendars;
    }
    
    /**
     * Sets the calendar list to a new calendar list
     * @param calendars 
     */
    public void setCalendarList(List<String> calendars) {
        this.calendars = calendars;
    }
    
    /**
     * Sets the imageLink to a new URL
     * @param url new URL to be set
     */
    public void setImageLink(URL url) {
        this.imageLink = url;
    }

    /**
     * Returns the imageLink
     * @return imageLink
     */
    public URL getImageLink() {
        return imageLink;
    }
    
    /**
     * Sets the description of the event.
     * @param description new description of an event.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns the description of the event.
     * @return description
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Sets the title of the event to the new title
     * @param title new title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Get the title of the event.
     * @return title
     */
    public String getTitle() {
        return title;
    }
    
    /**
     * Set the location of the event to a new location.
     * @param location new location
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * Returns the location of the event.
     * @return location
     */
    public String getLocation() {
        return location;
    }
    
    /**
     * Sets the time of the event to a new time.
     * @param time new time
     */
    public void setTime(String time) {
        this.time = time;
    }

    /**
     * Returns the time of the event.
     * @return time
     */
    public String getTime() {
        return time;
    }
    
    /**
     * Returns the start time of the event.
     * @return startTime
     */
    public String getStartTime() {
        return startTime;
    }
    
    /**
     * Sets the start time of the event
     * @param time the new start time
     */
    public void setStartTime(String time) {
        this.startTime = time;
    }
    
    /**
     * Returns the end time of the event
     * @return endTime
     */
    public String getEndTime() {
        return endTime;
    }
    
    /**
     * Sets the end time of the event
     * @param time the new end time
     */
    public void setEndTime(String time) {
        this.endTime = time;
    }
    
    /**
     * Sets the date of the event to a new date.
     * @param date new date
     */
    public void setDate(String date) {
        this.date = date;
    }

    /**
     * Returns the date of the event.
     * @return date
     */
    public String getDate() {
        return date;
    }
    
    /**
     * Sets the category of the event to a new category.
     * @param category new category
     */
    public void setCategory(String category) {
        this.category = category;
    }
    
    /**
     * Returns the category of the event.
     * @return category
     */
    public String getCategory() {
        return category;
    }
}