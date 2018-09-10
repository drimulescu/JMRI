/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jmri.jmrix;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

/**
 *
 */
public class AA_Daniel_GridbagLayout extends GridBagLayout {

    @Override
    public void setConstraints(Component comp, GridBagConstraints constraints) {
        String classInfo = getClassInfo(comp);
        System.out.format("AA_Daniel_GridbagLayout: %s: %s%n", classInfo, getStr(constraints));
//        System.out.format("AA_Daniel_GridbagLayout: %s: %s%n", comp.getClass().getName(), getStr(constraints));
//        if (constraints.gridwidth == -1)
//            getStackTrace();
        super.setConstraints(comp, constraints);
    }
    
    String getStr(GridBagConstraints cns) {
        return String.format("anchor: %d, fill: %d, gx: %s, gy: %s, gw: %d, gh: %d, ipadx: %d, ipady: %d, wx: %1.2f, wy: %1.2f",
                cns.anchor, cns.fill,
                (cns.gridx != -1) ? Integer.toString(cns.gridx) : "Relative", (cns.gridy != -1) ? Integer.toString(cns.gridy) : "Relative",
                cns.gridwidth, cns.gridheight, cns.ipadx, cns.ipady, cns.weightx, cns.weighty);
    }
    
    String getClassInfo(Object o) {
        String className = o.getClass().getName();
        switch (className) {
            case "javax.swing.JCheckBox":
                return String.format("%s (%s)", className, ((javax.swing.JCheckBox)o).getText());
            default:
                return className;
        }
    }
    
    void getStackTrace() {
        System.out.println("QQQQQQQQQQQQQQQQ");
        try {
            throw new Exception();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
