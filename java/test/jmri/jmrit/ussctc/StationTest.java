package jmri.jmrit.ussctc;

import org.junit.*;

import jmri.util.*;

/**
 * Tests for Station classes in the jmri.jmrit.ussctc package
 *
 * @author	Bob Jacobsen Copyright 2007
  */
public class StationTest {

    @Test
    public void testConstruction() {
        Station s = new Station(codeline, button);
        s.add(new TurnoutSection());
    }

    @Test
    public void testSendCode() {
        Station s = new Station(codeline, button);
        s.add(new Section(){
            public Station.Value  codeSendStart() { countCodeSend++; return Station.Value.Double00; }
            public void codeValueDelivered(Station.Value value) { }
            public Station.Value indicationStart() { return Station.Value.Double00; }
            public void indicationComplete(Station.Value value) {}
        });
        
        countCodeSend = 0;
        
        s.codeSendRequest();
        
        Assert.assertEquals("count of operations", 1, countCodeSend);
    }

    @Test
    public void testSendCodeSendAndImplementMultiSection() {
        Station s = new Station(codeline, button);
        s.add(new Section(){
            public Station.Value  codeSendStart() { countCodeSend++; return Station.Value.Double10; }
            public void codeValueDelivered(Station.Value value) { 
                Assert.assertEquals("deliver 10", Station.Value.Double10, value);
                countCodeSend = 0;
            }
            public Station.Value indicationStart() { return Station.Value.Double00; }
            public void indicationComplete(Station.Value value) {}
        });
        s.add(new Section(){
            public Station.Value  codeSendStart() { countCodeSend2++; return Station.Value.Double01; }
            public void codeValueDelivered(Station.Value value) { 
                Assert.assertEquals("deliver 01", Station.Value.Double01, value);
                countCodeSend2 = 0;
            }
            public Station.Value indicationStart() { return Station.Value.Double00; }
            public void indicationComplete(Station.Value value) {}
        });
        
        countCodeSend = 0;
        countCodeSend2 = 0;
        
        s.codeSendRequest();
        
        Assert.assertEquals("count of operations 1", 1, countCodeSend);
        Assert.assertEquals("count of operations 2", 1, countCodeSend2);

        s.codeValueDelivered();
        
        Assert.assertEquals("delivered OK 1", 0, countCodeSend);
        Assert.assertEquals("delivered OK 2", 0, countCodeSend2);
    }

    int countCodeSend;
    int countCodeSend2;
    
    CodeLine codeline = new CodeLine("Code Sequencer Start", "IT101", "IT102", "IT103", "IT104");
    CodeButton button = new CodeButton("IS21", "IS22");
        
    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initInternalSensorManager();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

}
