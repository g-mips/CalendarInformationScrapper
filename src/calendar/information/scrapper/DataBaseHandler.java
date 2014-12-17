package calendar.information.scrapper;
import calendar.information.events.Event;
import calendar.information.events.EventCalendar;
import java.io.InputStream;
import java.sql.*;
import java.util.List;

/**
 * Writes and Reads from the database of byui_events
 * @author Grant Merrill
 */
public class DataBaseHandler {
    private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";  
    private static final String DB_URL = "jdbc:mysql://71.209.33.122:3306/byui_events";

    //  Database credentials
    private static final String USER = "sam";
    private static final String PASS = "hibbard";
    
    private EventCalendar masterCalendar;
    
    public DataBaseHandler(EventCalendar masterCalendar) {
        this.masterCalendar = masterCalendar;
    }
    
    /**
     * Writes data of an event to the database specified by the Strings in the class
     */
    public void writeToDataBase() {
        Connection connection = null;
        PreparedStatement statement = null;
        
        try {
            // Register JDBC driver
            Class.forName(JDBC_DRIVER);

            // Open a connection
            System.out.println("Connecting to database...");
            connection = DriverManager.getConnection(DB_URL,USER,PASS);
            
            /**
             * DELETION OF OLD EVENTS!
             */
            String sqlDelete = "DELETE FROM common_lookup WHERE event_id IN(SELECT event_id FROM event " +
                               "WHERE date < curdate());";
            statement = connection.prepareStatement(sqlDelete);
            statement.executeUpdate();
            sqlDelete = "DELETE FROM event WHERE date < curdate();";
            statement = connection.prepareStatement(sqlDelete);
            statement.executeUpdate();
            
            /**
             * INSERTION OF NEW EVENTS AND UPDATING OTHER EVENTS!
             */
            // Go through all the events to insert them
            for (Event event : masterCalendar.getEventList()) {
                String sqlSelect = "SELECT event_id FROM event WHERE (name = \"" + event.getTitle() + 
                            "\" AND date = \"" + event.getDate() + "\" AND start_time = \"" + event.getStartTime() + 
                            "\" AND end_time = \"" + event.getEndTime() + "\" AND location = \"" +
                            event.getLocation() + "\")";
                statement = connection.prepareStatement(sqlSelect);
                ResultSet executeQuery = statement.executeQuery();
                
                // If the event isn't in the database, insert it into it
                if (!executeQuery.next()) {
                    String sqlInsert = "INSERT INTO event(name, date, start_time, end_time, " +
                                       "location, description, category, picture) VALUES(?, ?, ?, ?, ?, ?, ?, ?)";
                    statement = connection.prepareStatement(sqlInsert);

                    System.out.println("Inserting event: " + event.getTitle());

                    // Sets all these parameters as strings
                    statement.setString(1, event.getTitle());
                    statement.setString(2, event.getDate());
                    statement.setString(3, event.getStartTime());
                    statement.setString(4, event.getEndTime());
                    statement.setString(5, event.getLocation());
                    statement.setString(6, event.getDescription());
                    statement.setString(7, event.getCategory());

                    // Sets the image as an InputStream
                    InputStream urlStream;
                    if (event.getImageLink() != null) {
                        urlStream = event.getImageLink().openStream();
                        statement.setBlob(8, urlStream);
                    } else {
                        statement.setNull(8, java.sql.Types.BLOB);
                    }
                    
                    statement.executeUpdate();
                    
                    // Inserts all the calendars of the current event into the common lookup
                    for (String calendar : event.getCalendarList()) {
                        sqlInsert = "INSERT INTO common_lookup (event_id, calendar_name) VALUES ((SELECT event_id " +
                                    "FROM event WHERE (name = \"" + event.getTitle() +
                                    "\" AND date = \"" + event.getDate() + "\" AND start_time = \"" + event.getStartTime() + 
                                    "\" AND end_time = \"" + event.getEndTime() + "\" AND location = \"" +
                                    event.getLocation() + "\")), \"" + calendar + "\")";
                        statement = connection.prepareStatement(sqlInsert);

                        System.out.println("Inserting into common_lookup...");
                        statement.executeUpdate();
                    }
                } else {
                    // If the event exists, "update" it even if it needs no updating
                    InputStream urlStream;
                    if (event.getImageLink() != null) {
                        urlStream = event.getImageLink().openStream();
                    } else {
                        urlStream = null;
                    }
                    
                    String sqlUpdate = "UPDATE event SET description = \"" + event.getDescription() + "\", category = \"" +
                                       event.getCategory() + "\" WHERE (name = \"" +
                                       event.getTitle() + "\" AND date = \"" + event.getDate() + "\" AND start_time = \"" +
                                       event.getStartTime() + "\" AND end_time = \"" + event.getEndTime() + "\" AND location = \"" +
                                       event.getLocation() + "\");";
                
                    statement = connection.prepareStatement(sqlUpdate);
                    
                    System.out.println("Updating Event: " + event.getTitle());
                    statement.executeUpdate();    
                }
            } 
            
            if (statement != null) {
                statement.close();
            }
                       
            connection.close();
        } catch(SQLException se) {
           se.printStackTrace();
        } catch(Exception e) {
           e.printStackTrace();
        } finally {
            try{
               if (statement != null) {
                  statement.close();
               }
            } catch(SQLException se2) {
            }
            try {
               if (connection != null) {
                  connection.close();
               }
            } catch (SQLException se) {
               se.printStackTrace();
            }
        }
    }
    
    /**
     * Adds all the possible calendars to the calendar table on the database.
     * @param calendars 
     */
    public void addCalendars(List<EventCalendar> calendars) {
        Connection connection = null;
        PreparedStatement statement = null;
        PreparedStatement selectStatement = null;
        
        try {
            // Register JDBC driver
            Class.forName("com.mysql.jdbc.Driver");

            // Open a connection
            System.out.println("Connecting to database...");
            connection = DriverManager.getConnection(DB_URL,USER,PASS);

            System.out.println("Creating statement...");
            

            
            String sqlInsert = "INSERT INTO calendar VALUES(?)";
            statement = connection.prepareStatement(sqlInsert);

            // Inserts all the possible calendars that an event can have into the calendar table
            for (EventCalendar calendar : calendars) { 
                
                //check if the calendar already exists in the database
                String sqlSelect = "SELECT name FROM calendar WHERE name = \"" + calendar.getNameOfCalendar() + "\"";
                selectStatement = connection.prepareStatement(sqlSelect);
                ResultSet executeQuery = selectStatement.executeQuery();
                
                if(!executeQuery.next()){
                    System.out.println("Inserting calendar: " + calendar.getNameOfCalendar());

                    statement.setString(1, calendar.getNameOfCalendar());
                    statement.executeUpdate();
                }
            }

            statement.close();
            connection.close();
        } catch(SQLException se) {
           se.printStackTrace();
        } catch(Exception e) {
           e.printStackTrace();
        } finally {
            try{
               if (statement != null) {
                  statement.close();
               }
            } catch(SQLException se2) {
            }
            try {
               if (connection != null) {
                  connection.close();
               }
            } catch (SQLException se) {
               se.printStackTrace();
            }
        }    
    }
}