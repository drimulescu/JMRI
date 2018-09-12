package apps.gui3;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import jmri.InstanceManager;
import jmri.ShutDownManager;
import jmri.swing.PreferencesPanel;

/**
 * Provide a preferences dialog.
 * 
 * This dialog must not be used together with TabbedPreferencesFrame.
 */
public final class TabbedPreferencesDialog extends JDialog implements WindowListener {

    @Override
    public String getTitle() {
        return getTabbedPreferences().getTitle();
    }

    public boolean isMultipleInstances() {
        return true;
    }
    
    public static boolean showDialog(TabbedPreferences.FilterPreferences filterPreferences, boolean addQuitButton) {
        TabbedPreferencesDialog dialog = new TabbedPreferencesDialog();
        try {
            while (jmri.InstanceManager.getDefault(TabbedPreferences.class).init(filterPreferences, addQuitButton) != TabbedPreferences.INITIALISED) {
                final Object waiter = new Object();
                synchronized (waiter) {
                    waiter.wait(50);
                }
            }
            SwingUtilities.updateComponentTreeUI(dialog);
            
            // might not be a preferences item set yet
//            if (preferencesItem != null) f.gotoPreferenceItem(preferencesItem, preferenceSubCat);

            dialog.pack();
            dialog.setVisible(true);
            return false;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    public TabbedPreferencesDialog() {
        super();
        setModal(true);
        TabbedPreferences tabbedPreferences = getTabbedPreferences();
        tabbedPreferences.dialog = this;
        add(tabbedPreferences);
//        add(getTabbedPreferences());
//        addHelpMenu("package.apps.TabbedPreferences", true); // NOI18N
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(this);
    }

    public void gotoPreferenceItem(String item, String sub) {
        getTabbedPreferences().gotoPreferenceItem(item, sub);
    }

    @Override
    public void windowClosing(WindowEvent e) {
        ShutDownManager sdm = InstanceManager.getNullableDefault(ShutDownManager.class);
        if (!getTabbedPreferences().isPreferencesValid() && (sdm == null || !sdm.isShuttingDown())) {
            for (PreferencesPanel panel : getTabbedPreferences().getPreferencesPanels().values()) {
                if (!panel.isPreferencesValid()) {
                    switch (JOptionPane.showConfirmDialog(this,
                            Bundle.getMessage("InvalidPreferencesMessage", panel.getTabbedPreferencesTitle()),
                            Bundle.getMessage("InvalidPreferencesTitle"),
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.ERROR_MESSAGE)) {
                        case JOptionPane.YES_OPTION:
                            // abort window closing and return to broken preferences
                            getTabbedPreferences().gotoPreferenceItem(panel.getPreferencesItem(), panel.getTabbedPreferencesTitle());
                            return;
                        default:
                            // do nothing
                            break;
                    }
                }
            }
        }
        if (getTabbedPreferences().isDirty()) {
            switch (JOptionPane.showConfirmDialog(this,
                    Bundle.getMessage("UnsavedChangesMessage", getTabbedPreferences().getTitle()), // NOI18N
                    Bundle.getMessage("UnsavedChangesTitle"), // NOI18N
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE)) {
                case JOptionPane.YES_OPTION:
                    // save preferences
                    getTabbedPreferences().savePressed(getTabbedPreferences().invokeSaveOptions());
                    break;
                case JOptionPane.NO_OPTION:
                    // do nothing
                    break;
                case JOptionPane.CANCEL_OPTION:
                default:
                    // abort window closing
                    return;
            }
        }
        this.setVisible(false);
    }

    /**
     * Ensure a TabbedPreferences instance is always available.
     *
     * @return the default TabbedPreferences instance, creating it if needed
     */
    private TabbedPreferences getTabbedPreferences() {
        return InstanceManager.getOptionalDefault(TabbedPreferences.class).orElseGet(() -> {
            return InstanceManager.setDefault(TabbedPreferences.class, new TabbedPreferences());
        });
    }

    @Override
    public void windowOpened(WindowEvent e) {
    }

    @Override
    public void windowClosed(WindowEvent e) {
    }

    @Override
    public void windowIconified(WindowEvent e) {
    }

    @Override
    public void windowDeiconified(WindowEvent e) {
    }

    @Override
    public void windowActivated(WindowEvent e) {
    }

    @Override
    public void windowDeactivated(WindowEvent e) {
    }
}
