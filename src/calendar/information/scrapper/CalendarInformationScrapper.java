package calendar.information.scrapper;

import calendar.information.events.Event;
import calendar.information.events.EventCalendar;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Parses the calendars of the BYU-I Master Calendar, given the information found
 * in the properties file. Sends this information to a database to be used later.
 * @author Grant
 */
public class CalendarInformationScrapper {
    private List<EventCalendar> calendars;
    private EventCalendar masterCalendar;
    private Properties properties;
    private final static String RSS_FEEDS = "/calendar/information/resources/rssFeeds.properties";
    private final static String MASTER_CALENDAR_LINK = "http://calendar.byui.edu/MasterCalendar.aspx";
    
    /**
     * Creates a new CalendarInformationScrapper
     * @throws MalformedURLException
     */
    public CalendarInformationScrapper() throws MalformedURLException {
        calendars = new ArrayList<>();
        properties = new Properties();
        masterCalendar = new EventCalendar("Master Calendar", MASTER_CALENDAR_LINK);
    }
    
    /**
     * Runs through the process of sending information to a database.
     * @throws IOException problem when loading properties file or opening up the link of a calendar
     * @throws SAXException problem when parsing
     * @throws ParserConfigurationException problem when parsing.
     */
    public void run() throws IOException, SAXException, ParserConfigurationException, FileNotFoundException, URISyntaxException {
        loadProperties();
        readProperties();
        parseCalendars();
        prepareDataForDatabase();
        displayInConsole();
        sendToDatabase();
    }
    
    /**
     * Loads the rssFeeds properties file
     * @throws IOException problem loading the properties file.
     */
    public void loadProperties() throws IOException {
        properties.load(CalendarInformationScrapper.class.getResourceAsStream(RSS_FEEDS));
    }
    
    /**
     * Reads and stores the data found in the rssFeeds properties file.
     * @throws MalformedURLException throws if the URL string given is bad.
     */
    public void readProperties() throws MalformedURLException {
        for (Map.Entry calendar : properties.entrySet()) {
            EventCalendar newCalendar = new EventCalendar((String)calendar.getKey(), (String)calendar.getValue());
            calendars.add(newCalendar);
        }
    }

    /**
     * Parses through each calendar XML file and gets each event and its information
     * @throws IOException problem opening the link to the calendar
     * @throws SAXException problem while parsing
     * @throws ParserConfigurationException problem while parsing
     */
    public void parseCalendars() throws IOException, SAXException, ParserConfigurationException {
        for (EventCalendar calendar : calendars) {
            // Sets up a document to be read
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(calendar.getLink().openStream());
            
            document.getDocumentElement().normalize();
        
            System.out.println("Parsing calendar: " + calendar.getNameOfCalendar());
            
            Element documentElement = document.getDocumentElement();
            NodeList nodeList = documentElement.getChildNodes();
            
            // If there are children, lets check them.
            if (nodeList != null && nodeList.getLength() > 0) {
                loopXML(calendar, nodeList);
            }
        }
    }
    
    /**
     * Loops through all the children of the document.
     * @param calendar calendar that will be added events to
     * @param nodeList the children of the document
     * @throws MalformedURLException bad link of an imageLink
     */
    private void loopXML(EventCalendar calendar, NodeList nodeList) throws MalformedURLException {
        for (int i = 0; i < nodeList.getLength(); i++) {
            if (nodeList.item(i).getNodeName().equals("channel")) {
                Element channelList = (Element)nodeList.item(i);
                NodeList channelNodeList = channelList.getChildNodes();

                // Once we found the channel node, lets loop through its children.
                loopChannelNode(calendar, channelNodeList);
            }
        }
    }
    
    /**
     * Loops through all the children of the channel node.
     * @param calendar calendar that will be added events to
     * @param channelNodeList the children of the channel node
     * @throws MalformedURLException bad link of an imageLink
     */
    private void loopChannelNode(EventCalendar calendar, NodeList channelNodeList) throws MalformedURLException {
        for (int j = 0; j < channelNodeList.getLength(); ++j) {
            // Title of the calendar has been found. Set it!
            if (channelNodeList.item(j).getNodeName().equals("title")) {
                calendar.setNameOfCalendar(channelNodeList.item(j).getFirstChild().getNodeValue());
            }
            
            // An event of the calendar has been found. Loop through it's children.
            if (channelNodeList.item(j).getNodeName().equals("item")) {
                Element itemList = (Element)channelNodeList.item(j);
                NodeList itemNodeList = itemList.getChildNodes();
                
                loopItemNode(calendar, itemNodeList);
            }
        }
    }
    
    /**
     * Loops through all the children of the item (event) node.
     * @param calendar calendar that will be added events to
     * @param itemNodeList children of the item node
     * @throws MalformedURLException bad link of an imageLink
     */
    private void loopItemNode(EventCalendar calendar, NodeList itemNodeList) throws MalformedURLException {        
        Event event = new Event();

        // Loops through all the children of the item node and gathers data
        for (int k = 0; k < itemNodeList.getLength(); ++k) {
            Node itemNode = itemNodeList.item(k);
            Node textNode = itemNode.getFirstChild();

            // Title of the event
            if (itemNode.getNodeName().equals("title")) {
                event.setTitle(textNode.getNodeValue());
            }

            // Description of the event (note description holds the time and location)
            // Syntax:
            //    [TIME] - [LOCATION]: [DESCRIPTION]
            //    [TIME] - [LOCATION]
            if (itemNode.getNodeName().equals("description")) {
                String description = textNode.getNodeValue();
                String location = "";
                String time = "";
                int indexOfDash = description.indexOf("-");
                
                // Is there a location and description?
                if (indexOfDash > 0) {
                    // Set the time.
                    time = description.substring(0, indexOfDash);
        
                    // Delete the time from the description
                    description = description.substring(description.indexOf("-") + 1);
                    
                    int indexOfColon = description.indexOf(":");
        
                    // Is there a description?
                    if (indexOfColon > 0) {
                        // Set the location.
                        location = description.substring(0, indexOfColon);
                        
                        // Deletes the location from the description
                        description = description.substring(indexOfColon + 1);
                    } else {
                        location = description.substring(0);
                        description = "";
                    }
                } else {
                    description = "";
                }
                
                // Trim up the strings
                time = time.trim();
                location = location.trim();
                description = description.trim();

                // Sets the information to an event.
                event.setDescription(description);
                event.setTime(time);
                event.setLocation(location);
            }

            // Date that the event will happen. Scraps the time (should already
            // have it in MST)
            // Syntax:
            //    [DAY OF WEEK], [DAY] [MONTH] [YEAR] [HOUR]:[MINUTES]:[SECONDS] GMT
            if (itemNode.getNodeName().equals("pubDate")) {
                String date = textNode.getNodeValue().substring(textNode.getNodeValue().indexOf(",")+2);
                
                String[] dateParts = date.split("[\\s:]");
                String day = dateParts[0];
                String month = dateParts[1];
                String year = dateParts[2];
                String hour = dateParts[3];
                String minutes = dateParts[4];
                String seconds = dateParts[5];
        
                SimpleDateFormat monthAsDateFormat = new SimpleDateFormat("MMM");
                Date monthAsDate = new Date();

                try {
                    monthAsDate = monthAsDateFormat.parse(month);

                } catch (ParseException ex) {
                    Logger.getLogger(CalendarInformationScrapper.class.getName()).log(Level.SEVERE, null, ex);
                }

                Calendar monthAsCalendar = new GregorianCalendar();
                monthAsCalendar.setTime(monthAsDate);

                Calendar dateOfEvent = new GregorianCalendar(TimeZone.getTimeZone("MST"));

                dateOfEvent.set(Integer.parseInt(year), monthAsCalendar.get(Calendar.MONTH), Integer.parseInt(day),
                                Integer.parseInt(hour), Integer.parseInt(minutes), Integer.parseInt(seconds));
        
                dateOfEvent.add(Calendar.HOUR_OF_DAY, -7);
                
                event.setDate(dateOfEvent.get(Calendar.YEAR) + "-" + (dateOfEvent.get(Calendar.MONTH)+1) + "-" +
                              dateOfEvent.get(Calendar.DAY_OF_MONTH));
            }
            
            // Category of the event
            if (itemNode.getNodeName().equals("category")) {
                event.setCategory(textNode.getNodeValue());
            }
            
            // Link to the image of the event.
            if (itemNode.getNodeName().equals("enclosure")) {
                if (itemNode.getAttributes().getNamedItem("url") != null) {
                    event.setImageLink(new URL(itemNode.getAttributes().getNamedItem("url").getNodeValue()));
                }
            }
        }

        // If all the required information was found, it is a legal new event to be added to the calendar.
        if (event.hasRequiredInfo() && !masterCalendar.hasEvent(event)) {
            event.addCalendar(calendar.getNameOfCalendar());
            masterCalendar.addEvent(event);
        } else if (masterCalendar.hasEvent(event)) {
            masterCalendar.findEvent(event).addCalendar(calendar.getNameOfCalendar());
        }
    }

    /**
     * Displays the information of each calendar and each event in the console.
     */
    public void displayInConsole() throws URISyntaxException, FileNotFoundException, IOException {
        System.out.println();
        int i = 0;
        for (Event event : masterCalendar.getEventList()) {
            System.out.println("  "   + event.getTitle());
                System.out.println("    " + event.getCategory());
                System.out.println("    " + event.getDate());
                System.out.println("    " + event.getLocation());
                System.out.println("    " + event.getTime());
                System.out.println("    " + event.getStartTime());
                System.out.println("    " + event.getEndTime());
                System.out.println("    " + event.getDescription());
                
                if (event.getImageLink() != null) {
                    System.out.println("    " + event.getImageLink().toURI());
                }
                
                for (String calendar : event.getCalendarList()) {
                    System.out.println("    " + calendar);
                }
                
                System.out.println();
        }
    }
    
    /**
     * This will prepare the data to be sent to a database. Format of date and time
     * need to be changed.
     */
    public void prepareDataForDatabase() {
        for (Event event : masterCalendar.getEventList()) {
            prepareTime(event);            
        }
    }
    
    /**
     * Prepares the time of an event to be in the correct format.
     * @param event 
     */
    private void prepareTime(Event event) {
        String time = event.getTime();
        
        // Format of Time:
        //   [HOUR]:[MINTUES] [AM/PM] to [HOUR]:[MINUTES] [AM/PM]
        // Format that is needed:
        //   Military Time, two variables (start time and end time):
        //   [HOUR]:[MINUTES]:[SECONDS]
        String[] timeAsArray = time.split("to");
        String[] startTimeParts = timeAsArray[0].trim().split(" ");
        String[] endTimeParts = timeAsArray[1].trim().split(" ");
        String startTime = "";
        String endTime = "";

        // Are we dealing the a PM or AM time for start and end times?
        switch (startTimeParts[1]) {
            case "PM":
                String hour = "";
                
                // Save the hour as string
                for (int i = 0; i < startTimeParts[0].length() && startTimeParts[0].charAt(i) != ':'; ++i) {
                    hour += startTimeParts[0].charAt(i);
                }
                
                int hourAsInt;
                
                // 12 is just 12.. Everything else adds 12..
                if (startTimeParts[0].startsWith("12:")) {
                    hourAsInt = Integer.parseInt(hour);
                } else {
                    hourAsInt = Integer.parseInt(hour) + 12;
                }
                
                startTimeParts[0] = startTimeParts[0].replace(hour + ":", Integer.toString(hourAsInt) + ":");
                break;
            case "AM":
                // Midnight is a special case
                if (startTimeParts[0].startsWith("12:")) {
                    startTimeParts[0] = startTimeParts[0].replace("12:", "00:");
                }
                break;
        }
        
        switch (endTimeParts[1]) {
            case "PM":
                String hour = "";
                for (int i = 0; i < endTimeParts[0].length() && endTimeParts[0].charAt(i) != ':'; ++i) {
                    hour += endTimeParts[0].charAt(i);
                }
                
                int hourAsInt;
                
                if (endTimeParts[0].startsWith("12:")) {
                    hourAsInt = Integer.parseInt(hour);
                } else {
                    hourAsInt = Integer.parseInt(hour) + 12;
                }
                
                endTimeParts[0] = endTimeParts[0].replace(hour + ":", Integer.toString(hourAsInt) + ":");
                
                break;
            case "AM":
                if (endTimeParts[0].startsWith("12:")) {
                    endTimeParts[0] = endTimeParts[0].replace("12:", "00:");
                }
                break;
        }
        
        event.setStartTime(startTimeParts[0] + ":00");
        event.setEndTime(endTimeParts[0] + ":00");
    }
    
    /**
     * Sends data from the master calendar to the database.. other line is commented out on purpose.
     * If it is needed to be redone.. Uncomment it out.
     * @throws FileNotFoundException
     * @throws URISyntaxException 
     */
    public void sendToDatabase() throws FileNotFoundException, URISyntaxException {        
        DataBaseHandler eventAppDataBase = new DataBaseHandler(masterCalendar);
        eventAppDataBase.addCalendars(calendars);
        eventAppDataBase.writeToDataBase();
    }
    
    /**
     * Runs the program!
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            new CalendarInformationScrapper().run();
        } catch (IOException | SAXException | ParserConfigurationException | URISyntaxException ex) {
            Logger.getLogger(CalendarInformationScrapper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }    
}
