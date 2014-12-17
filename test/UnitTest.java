/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import calendar.information.events.Event;
import org.testng.Assert;
import org.testng.annotations.Test;
import static org.testng.Assert.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

/**
 *
 * @author Justin Hurley
 */
public class UnitTest {
    
    public UnitTest() {
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    // @Test
    // public void hello() {}

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }
    
    @Test
    public void eventTest(){
        Event event = new Event();
        event.setDescription("This is a test description");
        event.setTime("8:30:00 - 10:30:00");
        event.setStartTime("8:30:00");
        event.setEndTime("10:30:00");
        event.setLocation("Smith Building");
        event.setTitle("Test Event");
        
        Assert.assertEquals(event.hasRequiredInfo(), true);
        Assert.assertEquals(event.getDescription(), "This is a test description");
        Assert.assertEquals(event.getStartTime(), "8:30:00");
        Assert.assertEquals(event.getEndTime(), "10:30:00");
        Assert.assertEquals(event.getLocation(), "Smith Building");
        Assert.assertEquals(event.getTitle(), "Test Event");       
    }
    
    @Test
    public void eventTest2(){
        Event event = new Event();
        Assert.assertEquals(event.hasRequiredInfo(), false);
        
        event.setTime("5:30:00 - 6:30:00");
        Assert.assertEquals(event.hasRequiredInfo(), false);
        
        event.setTitle("Test event");
        Assert.assertEquals(event.hasRequiredInfo(), false);
        
        event.setDate("12/16/2014");
        Assert.assertEquals(event.hasRequiredInfo(), false);
        
        event.setLocation("someplace");
        Assert.assertEquals(event.hasRequiredInfo(), true);
    
    }
}
