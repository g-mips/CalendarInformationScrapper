package calendar.information.events;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * An event calendar of BYU-I that holds events. Also holds the location of the
 * event calendar RSS Feed and its name.
 * @author Grant
 */
public class EventCalendar {
    private String nameOfCalendar;
    private List<Event> eventList;
    private URL link;
    
    /**
     * Creates a new EventCalendar with the given name and link.
     * @param name new nameOfCalendar
     * @param link new link
     * @throws MalformedURLException thrown if the link given is bad 
     */
    public EventCalendar(String name, String link) throws MalformedURLException {
        this.eventList = new ArrayList<>();
        this.nameOfCalendar = name;
        this.link = new URL(link);
    }

    /**
     * Adds an event at the end of the eventList List.
     * @param event new event to be added
     */
    public void addEvent(Event event) {
        eventList.add(event);
    }
    
    /**
     * 
     * @param eventToCheck
     * @return 
     */
    public boolean hasEvent(Event eventToCheck) {
        return (findEvent(eventToCheck) != null);
    }
    
    /**
     * 
     * @param eventToFind
     * @return 
     */
    public Event findEvent(Event eventToFind) {
        for (Event event : eventList) {
            if (event.getTitle().equals(eventToFind.getTitle()) && event.getDate().equals(eventToFind.getDate()) && 
                event.getLocation().equals(eventToFind.getLocation()) && event.getTime().equals(eventToFind.getTime())) {
                return event;
            }
        }
        return null;
    }
    
    /**
     * Returns the eventList
     * @return eventList
     */
    public List<Event> getEventList() {
        return eventList;
    }
    
    /**
     * Returns nameOfCalendar
     * @return nameOfCalendar
     */
    public String getNameOfCalendar() {
        return nameOfCalendar;
    }
    
    /**
     * Sets the nameOfCalendar to name
     * @param name new nameOfCalendar
     */
    public void setNameOfCalendar(String name) {
        this.nameOfCalendar = name;
    }

    /**
     * Returns link to the RSS Feed
     * @return link
     */
    public URL getLink() {
        return link;
    }

    /**
     * Sets the link to the RSS Feed to a different URL.
     * @param link new RSS Feed link
     */
    public void setLink(URL link) {
        this.link = link;
    }
    
    /**
     * Sets the link to the RSS Feed a new URL.
     * @param link new link to be set
     * @throws MalformedURLException throws if the link given is a bad link.
     */
    public void setLink(String link) throws MalformedURLException {
        this.link = new URL(link);
    }
}