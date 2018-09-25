package jmri.jmrit.display.layoutEditor;

import java.awt.GraphicsEnvironment;
import java.awt.geom.Point2D;
import jmri.InstanceManager;
import jmri.implementation.VirtualSignalHead;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;
import org.junit.*;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.operators.JButtonOperator;

/**
 * Test private methods of LayoutEditorTools
 *
 * @author Daniel Bergqvist 2018
 */
public class LayoutEditorToolsPrivateTest {
    
    private LayoutEditor le = null;
    private LayoutEditorTools let = null;
    
    @Test
    public void testAAA() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("exists", let);
    }
    
    // from here down is testing infrastructure
    @Before
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        if(!GraphicsEnvironment.isHeadless()) {
            jmri.util.JUnitUtil.resetProfileManager();
            le = new LayoutEditor();
            let = new LayoutEditorTools(le);
        }
    }
    
    @After
    public void tearDown() throws Exception {
        if(!GraphicsEnvironment.isHeadless()) {
            JUnitUtil.dispose(le);
        }
        le = null;
        let = null;
        JUnitUtil.tearDown();
    }
    
}
