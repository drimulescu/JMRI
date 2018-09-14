package jmri.implementation;

import java.util.Arrays;
import java.util.ArrayList;
import jmri.*;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test the DefaultConditional implementation class
 *
 * @author Bob Jacobsen Copyright (C) 2015
 */
public class DefaultConditionalTest {

    /**
     * Operate parent NamedBeanTest tests.
     */
    @Test
    public void createInstance() {
        new DefaultConditional("IXIC 0");
    }

    @Test
    public void testCtor() {
        Assert.assertNotNull("exists",new DefaultConditional("IXIC 1"));
    }

    @Test
    public void testBasicBeanOperations() {
        Conditional ix1 = new DefaultConditional("IXIC 2");

        Conditional ix2 = new DefaultConditional("IXIC 3");

        Assert.assertTrue("object not equals", !ix1.equals(ix2));
        Assert.assertTrue("object not equals reverse", !ix2.equals(ix1));

        Assert.assertTrue("hash not equals", ix1.hashCode() != ix2.hashCode());
    }
    
    @Test
    public void testCalculate() {
        Conditional ix1 = new DefaultConditional("IXIC 1");
        Assert.assertTrue("calculate() returns NamedBean.UNKNOWN", ix1.calculate(false, null) == NamedBean.UNKNOWN);
        
        ArrayList<ConditionalVariable> conditionalVariablesList_TrueTrueTrue
                = new ArrayList<>();
        conditionalVariablesList_TrueTrueTrue.add(new ConditionalVariableStatic(Conditional.TRUE));
        conditionalVariablesList_TrueTrueTrue.add(new ConditionalVariableStatic(Conditional.TRUE));
        conditionalVariablesList_TrueTrueTrue.add(new ConditionalVariableStatic(Conditional.TRUE));
        
        ArrayList<ConditionalVariable> conditionalVariablesList_FalseFalseFalse
                = new ArrayList<>();
        conditionalVariablesList_FalseFalseFalse.add(new ConditionalVariableStatic(Conditional.FALSE));
        conditionalVariablesList_FalseFalseFalse.add(new ConditionalVariableStatic(Conditional.FALSE));
        conditionalVariablesList_FalseFalseFalse.add(new ConditionalVariableStatic(Conditional.FALSE));
        
        ArrayList<ConditionalVariable> conditionalVariablesList_TrueTrueFalse
                = new ArrayList<>();
        conditionalVariablesList_TrueTrueFalse.add(new ConditionalVariableStatic(Conditional.TRUE));
        conditionalVariablesList_TrueTrueFalse.add(new ConditionalVariableStatic(Conditional.TRUE));
        conditionalVariablesList_TrueTrueFalse.add(new ConditionalVariableStatic(Conditional.FALSE));
        
        ArrayList<ConditionalVariable> conditionalVariablesList_FalseTrueTrue
                = new ArrayList<>();
        conditionalVariablesList_FalseTrueTrue.add(new ConditionalVariableStatic(Conditional.FALSE));
        conditionalVariablesList_FalseTrueTrue.add(new ConditionalVariableStatic(Conditional.TRUE));
        conditionalVariablesList_FalseTrueTrue.add(new ConditionalVariableStatic(Conditional.TRUE));

        ArrayList<ConditionalVariable> conditionalVariablesList_TrueFalseTrue
                = new ArrayList<>();
        conditionalVariablesList_TrueFalseTrue.add(new ConditionalVariableStatic(Conditional.TRUE));
        conditionalVariablesList_TrueFalseTrue.add(new ConditionalVariableStatic(Conditional.FALSE));
        conditionalVariablesList_TrueFalseTrue.add(new ConditionalVariableStatic(Conditional.TRUE));
        
        ArrayList<ConditionalVariable> conditionalVariablesList_FalseTrueFalse
                = new ArrayList<>();
        conditionalVariablesList_FalseTrueFalse.add(new ConditionalVariableStatic(Conditional.FALSE));
        conditionalVariablesList_FalseTrueFalse.add(new ConditionalVariableStatic(Conditional.TRUE));
        conditionalVariablesList_FalseTrueFalse.add(new ConditionalVariableStatic(Conditional.FALSE));
        
        
        
        ix1 = new DefaultConditional("IXIC 1");
        ix1.setLogicType(Conditional.ALL_AND, "");
        ix1.setStateVariables(conditionalVariablesList_TrueTrueTrue);
        Assert.assertTrue("calculate() returns NamedBean.TRUE", ix1.calculate(false, null) == Conditional.TRUE);
        
        ix1 = new DefaultConditional("IXIC 1");
        ix1.setLogicType(Conditional.ALL_AND, "");
        ix1.setStateVariables(conditionalVariablesList_TrueTrueFalse);
        Assert.assertTrue("calculate() returns NamedBean.FALSE", ix1.calculate(false, null) == Conditional.FALSE);
    }

    
    // from here down is testing infrastructure

    // The minimal setup for log4J
    @Before
    public void setUp() throws Exception {
        jmri.util.JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.initInternalTurnoutManager();
        jmri.util.JUnitUtil.initInternalLightManager();
        jmri.util.JUnitUtil.initInternalSensorManager();
        jmri.util.JUnitUtil.initIdTagManager();
    }

    @After
    public void tearDown() throws Exception {
        jmri.util.JUnitUtil.tearDown();
    }

    
    private final class ConditionalVariableStatic extends ConditionalVariable {
        
        ConditionalVariableStatic(int state) {
            super();
            super.setState(state);
        }
        
        @Override
        public boolean evaluate() {
            return this.getState() == Conditional.TRUE;
        }
        
    }
    
}
