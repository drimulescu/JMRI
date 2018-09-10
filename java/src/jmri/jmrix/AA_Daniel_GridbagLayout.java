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
        return String.format("anchor: %s, fill: %d, gx: %s, gy: %s, gw: %d, gh: %d, ipadx: %d, ipady: %d, wx: %1.2f, wy: %1.2f",
                getAnchor(cns.anchor), cns.fill,
                (cns.gridx != -1) ? Integer.toString(cns.gridx) : "Relative", (cns.gridy != -1) ? Integer.toString(cns.gridy) : "Relative",
                cns.gridwidth, cns.gridheight, cns.ipadx, cns.ipady, cns.weightx, cns.weighty);
    }
    
    String getClassInfo(Object o) {
        String className = o.getClass().getName();
        switch (className) {
            case "javax.swing.JCheckBox":
                return String.format("%s(%s)", className, ((javax.swing.JCheckBox)o).getText());
            case "javax.swing.JComboBox":
                return String.format("%s(%s)", className, ((javax.swing.JComboBox)o).getSelectedItem());
            case "javax.swing.JLabel":
                return String.format("%s(%s)", className, ((javax.swing.JLabel)o).getText());
            case "javax.swing.JTextField":
                return String.format("%s(%s)", className, ((javax.swing.JTextField)o).getText());
            default:
                return className;
        }
    }
    
    String getAnchor(int anchor) {
        switch (anchor) {
            case GridBagConstraints.CENTER:
                return "CENTER";
            case GridBagConstraints.NORTH:
                return "NORTH";
            case GridBagConstraints.NORTHEAST:
                return "NORTHEAST";
            case GridBagConstraints.EAST:
                return "EAST";
            case GridBagConstraints.SOUTHEAST:
                return "SOUTHEAST";
            case GridBagConstraints.SOUTH:
                return "SOUTH";
            case GridBagConstraints.SOUTHWEST:
                return "SOUTHWEST";
            case GridBagConstraints.WEST:
                return "WEST";
            case GridBagConstraints.NORTHWEST:
                return "NORTHWEST";
            case GridBagConstraints.PAGE_START:
                return "PAGE_START";
            case GridBagConstraints.PAGE_END:
                return "PAGE_END";
            case GridBagConstraints.LINE_START:
                return "LINE_START";
            case GridBagConstraints.LINE_END:
                return "LINE_END";
            case GridBagConstraints.FIRST_LINE_START:
                return "FIRST_LINE_START";
            case GridBagConstraints.FIRST_LINE_END:
                return "FIRST_LINE_END";
            case GridBagConstraints.LAST_LINE_START:
                return "LAST_LINE_START";
            case GridBagConstraints.LAST_LINE_END:
                return "LAST_LINE_END";
            case GridBagConstraints.BASELINE:
                return "BASELINE";
            case GridBagConstraints.BASELINE_LEADING:
                return "BASELINE_LEADING";
            case GridBagConstraints.BASELINE_TRAILING:
                return "BASELINE_TRAILING";
            case GridBagConstraints.ABOVE_BASELINE:
                return "ABOVE_BASELINE";
            case GridBagConstraints.ABOVE_BASELINE_LEADING:
                return "ABOVE_BASELINE_LEADING";
            case GridBagConstraints.ABOVE_BASELINE_TRAILING:
                return "ABOVE_BASELINE_TRAILING";
            case GridBagConstraints.BELOW_BASELINE:
                return "BELOW_BASELINE";
            case GridBagConstraints.BELOW_BASELINE_LEADING:
                return "BELOW_BASELINE_LEADING";
            case GridBagConstraints.BELOW_BASELINE_TRAILING:
                return "BELOW_BASELINE_TRAILING";
            default:
                return "__UNKNOWN__";
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
