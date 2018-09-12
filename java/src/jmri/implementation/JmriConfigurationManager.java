package jmri.implementation;

import apps.AppsBase;
import apps.ConfigBundle;
import apps.gui3.TabbedPreferences;
import apps.gui3.TabbedPreferencesAction;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.ListSelectionModel;
import jmri.ShutDownManager;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import jmri.Application;
import jmri.swing.PreferencesPanel;
import jmri.ConfigureManager;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.configurexml.ConfigXmlManager;
import jmri.configurexml.swing.DialogErrorHandler;
import jmri.jmrit.XmlFile;
import jmri.profile.AddProfileDialog;
import jmri.profile.Profile;
import jmri.profile.ProfileManager;
import jmri.spi.PreferencesManager;
import jmri.swing.PreferencesSubPanel;
import jmri.util.FileUtil;
import jmri.util.ThreadingUtil;
import jmri.util.prefs.HasConnectionButUnableToConnectException;
import jmri.util.prefs.InitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Randall Wood
 */
public class JmriConfigurationManager implements ConfigureManager {

    private final static Logger log = LoggerFactory.getLogger(JmriConfigurationManager.class);
    private final ConfigXmlManager legacy = new ConfigXmlManager();
    private final HashMap<PreferencesManager, InitializationException> initializationExceptions = new HashMap<>();
    private final List<PreferencesManager> initialized = new ArrayList<>();

    @SuppressWarnings("unchecked") // For types in InstanceManager.store()
    public JmriConfigurationManager() {
        ServiceLoader<PreferencesManager> sl = ServiceLoader.load(PreferencesManager.class);
        for (PreferencesManager pp : sl) {
            InstanceManager.store(pp, PreferencesManager.class);
            for (Class provided : pp.getProvides()) { // use raw class so next line can compile
                InstanceManager.store(provided.cast(pp), provided);
            }
        }
        Profile profile = ProfileManager.getDefault().getActiveProfile();
        if (profile != null) {
            this.legacy.setPrefsLocation(new File(profile.getPath(), Profile.CONFIG_FILENAME));
        }
        if (!GraphicsEnvironment.isHeadless()) {
            ConfigXmlManager.setErrorHandler(new DialogErrorHandler());
        }
    }

    @Override
    public void registerPref(Object o) {
        if ((o instanceof PreferencesManager)) {
            InstanceManager.store((PreferencesManager) o, PreferencesManager.class);
        }
        this.legacy.registerPref(o);
    }

    @Override
    public void removePrefItems() {
        this.legacy.removePrefItems();
    }

    @Override
    public void registerConfig(Object o) {
        this.legacy.registerConfig(o);
    }

    @Override
    public void registerConfig(Object o, int x) {
        this.legacy.registerConfig(o, x);
    }

    @Override
    public void registerTool(Object o) {
        this.legacy.registerTool(o);
    }

    @Override
    public void registerUser(Object o) {
        this.legacy.registerUser(o);
    }

    @Override
    public void registerUserPrefs(Object o) {
        this.legacy.registerUserPrefs(o);
    }

    @Override
    public void deregister(Object o) {
        this.legacy.deregister(o);
    }

    @Override
    public Object findInstance(Class<?> c, int index) {
        return this.legacy.findInstance(c, index);
    }

    @Override
    public List<Object> getInstanceList(Class<?> c) {
        return this.legacy.getInstanceList(c);
    }

    @Override
    public boolean storeAll(File file) {
        return this.legacy.storeAll(file);
    }

    /**
     * Save preferences. Preferences are saved using either the
     * {@link jmri.util.prefs.JmriConfigurationProvider} or
     * {@link jmri.util.prefs.JmriPreferencesProvider} as appropriate to the
     * register preferences handler.
     */
    @Override
    public void storePrefs() {
        log.debug("Saving preferences...");
        Profile profile = ProfileManager.getDefault().getActiveProfile();
        InstanceManager.getList(PreferencesManager.class).stream().forEach((o) -> {
            log.debug("Saving preferences for {}", o.getClass().getName());
            o.savePreferences(profile);
        });
    }

    /**
     * Save preferences. This method calls {@link #storePrefs() }.
     *
     * @param file Ignored.
     */
    @Override
    public void storePrefs(File file) {
        this.storePrefs();
    }

    @Override
    public void storeUserPrefs(File file) {
        this.legacy.storeUserPrefs(file);
    }

    @Override
    public boolean storeConfig(File file) {
        return this.legacy.storeConfig(file);
    }

    @Override
    public boolean storeUser(File file) {
        return this.legacy.storeUser(file);
    }

    @Override
    public boolean load(File file) throws JmriException {
        return this.load(file, false);
    }

    @Override
    public boolean load(URL url) throws JmriException {
        return this.load(url, false);
    }

    @Override
    public boolean load(File file, boolean registerDeferred) throws JmriException {
        return this.load(FileUtil.fileToURL(file), registerDeferred);
    }

    @Override
    public boolean load(URL url, boolean registerDeferred) throws JmriException {
        log.debug("loading {} ...", url);
        try {
            if (url == null
                    || (new File(url.toURI())).getName().equals("ProfileConfig.xml") //NOI18N
                    || (new File(url.toURI())).getName().equals(Profile.CONFIG)) {
                Profile profile = ProfileManager.getDefault().getActiveProfile();
                List<PreferencesManager> providers = new ArrayList<>(InstanceManager.getList(PreferencesManager.class));
                providers.stream().forEach((provider) -> {
                    this.initializeProvider(provider, profile);
                });
                if (!this.initializationExceptions.isEmpty()) {
                    if (!GraphicsEnvironment.isHeadless()) {
                        
                        AtomicBoolean isUnableToConnect = new AtomicBoolean(false);
                        
                        List<String> errors = new ArrayList<>();
                        this.initialized.forEach((provider) -> {
                            List<Exception> exceptions = provider.getInitializationExceptions(profile);
                            if (!exceptions.isEmpty()) {
                                exceptions.forEach((exception) -> {
                                    if (exception instanceof HasConnectionButUnableToConnectException) {
                                        isUnableToConnect.set(true);
                                    }
                                    errors.add(exception.getLocalizedMessage());
                                });
                            } else if (this.initializationExceptions.get(provider) != null) {
                                errors.add(this.initializationExceptions.get(provider).getLocalizedMessage());
                            }
                        });
                        Object list;
                        if (errors.size() == 1) {
                            list = errors.get(0);
                        } else {
                            list = new JList<>(errors.toArray(new String[errors.size()]));
                        }
                        
                        List<String> errorList = errors;
                        
                        if (isUnableToConnect.get()) {
                            if (errors.size() > 1) {
                                errorList.add(0, Bundle.getMessage("InitExMessageListHeader"));
                            }
                            errorList.add("");
                            errorList.add(Bundle.getMessage("InitExMessageLogs")); // NOI18N
                            
//                            ErrorDialog dialog = new ErrorDialog(errorList);
                            ErrorDialog dialog = new ErrorDialog(ErrorDialog.Result.EDIT_CONNECTIONS);
                            
                            switch (dialog.result) {
                                case NEW_PROFILE:
                                    AddProfileDialog apd = new AddProfileDialog(null, true, false);
                                    apd.setLocationRelativeTo(null);
                                    apd.setVisible(true);
                                    // Restart program
                                    AppsBase.handleRestart();
                                    break;
                                    
                                case EDIT_CONNECTIONS:
                                    final Object waiter = new Object();
                                    System.out.format("Show preferences - init%n");
                                    try {
                                        TabbedPreferences.FilterPreferences filterPreferences
                                                = (PreferencesPanel panel) -> (panel instanceof jmri.jmrix.swing.ConnectionsPreferencesPanel);
//                                        while (jmri.InstanceManager.getDefault(TabbedPreferences.class).init() != TabbedPreferences.INITIALISED) {
                                        while (jmri.InstanceManager.getDefault(TabbedPreferences.class).init(filterPreferences) != TabbedPreferences.INITIALISED) {
                                            synchronized (waiter) {
                                                waiter.wait(50);
                                            }
                                        }
                                    } catch (InterruptedException ex) {
                                        Thread.currentThread().interrupt();
                                    }
                                    
                                    System.out.format("Show preferences%n");
//                                    (new TabbedPreferencesAction()).actionPerformed();
//                                    (new TabbedPreferencesAction()).showDialog();
////                                    try { Thread.sleep(10000); } catch (InterruptedException e) { };
                                    new ConnectionsPreferencesDialog();
                                    System.out.format("Show preferences done%n");
                                    
                                    // For testing only
                                    // Quit program
                                    AppsBase.handleQuit();
                                    
                                    // Restart program
                                    AppsBase.handleRestart();
                                    break;
                                    
                                case EXIT_PROGRAM:
                                default:
                                    // Exit program
                                    AppsBase.handleQuit();
                            }
                        }
                        
                        JOptionPane.showMessageDialog(null,
                                new Object[]{
                                    (list instanceof JList) ? Bundle.getMessage("InitExMessageListHeader") : null,
                                    list,
                                    "<html><br></html>", // Add a visual break between list of errors and notes // NOI18N
                                    Bundle.getMessage("InitExMessageLogs"), // NOI18N
                                    Bundle.getMessage("InitExMessagePrefs"), // NOI18N
                                },
                                Bundle.getMessage("InitExMessageTitle", Application.getApplicationName()), // NOI18N
                                JOptionPane.ERROR_MESSAGE);
                        (new TabbedPreferencesAction()).actionPerformed();
                    }
                }
                if (url != null && (new File(url.toURI())).getName().equals("ProfileConfig.xml")) { // NOI18N
                    log.debug("Loading legacy configuration...");
                    return this.legacy.load(url, registerDeferred);
                }
                return this.initializationExceptions.isEmpty();
            }
        } catch (URISyntaxException ex) {
            log.error("Unable to get File for {}", url);
            throw new JmriException(ex.getMessage(), ex);
        }
        // make this url the default "Save Panels..." file
        JFileChooser ufc = jmri.configurexml.StoreXmlUserAction.getUserFileChooser();
        ufc.setSelectedFile(new File(FileUtil.urlToURI(url)));

        return this.legacy.load(url, registerDeferred);
        // return true; // always return true once legacy support is dropped
    }

    @Override
    public boolean loadDeferred(File file) throws JmriException {
        return this.legacy.loadDeferred(file);
    }

    @Override
    public boolean loadDeferred(URL file) throws JmriException {
        return this.legacy.loadDeferred(file);
    }

    @Override
    public URL find(String filename) {
        return this.legacy.find(filename);
    }

    @Override
    public boolean makeBackup(File file) {
        return this.legacy.makeBackup(file);
    }

    private void initializeProvider(PreferencesManager provider, Profile profile) {
        if (!provider.isInitialized(profile) && !provider.isInitializedWithExceptions(profile)) {
            log.debug("Initializing provider {}", provider.getClass());
            for (Class<? extends PreferencesManager> c : provider.getRequires()) {
                InstanceManager.getList(c).stream().forEach((p) -> {
                    this.initializeProvider(p, profile);
                });
            }
            try {
                provider.initialize(profile);
            } catch (InitializationException ex) {
                // log all initialization exceptions, but only retain for GUI display the
                // first initialization exception for a provider
                InitializationException put = this.initializationExceptions.putIfAbsent(provider, ex);
                log.error("Exception initializing {}: {}", provider.getClass().getName(), ex.getMessage());
                if (put != null) {
                    log.error("Additional exception initializing {}: {}", provider.getClass().getName(), ex.getMessage());
                }
            }
            this.initialized.add(provider);
            log.debug("Initialized provider {}", provider.getClass());
        }
    }

    public HashMap<PreferencesManager, InitializationException> getInitializationExceptions() {
        return new HashMap<>(initializationExceptions);
    }

    @Override
    public void setValidate(XmlFile.Validate v) {
        legacy.setValidate(v);
    }

    @Override
    public XmlFile.Validate getValidate() {
        return legacy.getValidate();
    }



    private static final class ErrorDialog extends JDialog {
        
        enum Result {
            EXIT_PROGRAM,
            NEW_PROFILE,
            EDIT_CONNECTIONS,
        }
        
        
        Result result = Result.EXIT_PROGRAM;

        ErrorDialog(Result result) {
            this.result = result;
        }
        
        ErrorDialog(List<String> list) {
            super();
            setTitle("JMRI is unable to connect");
            setModal(true);
            JPanel contentPanel = new JPanel();
            contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
            JPanel panel = new JPanel();
            panel.add(new JLabel(Bundle.getMessage("InitExMessageListHeader")));
            contentPanel.add(panel);

            JPanel marginPanel = new JPanel();
            marginPanel.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
            marginPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(5,5,5,5));
            contentPanel.add(marginPanel);
            JPanel borderPanel = new JPanel();
            borderPanel.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
            borderPanel.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.black));
            marginPanel.add(borderPanel);
            panel = new JPanel();
            panel.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.setBorder(javax.swing.BorderFactory.createEmptyBorder(5,5,5,5));
            for (String s : list) {
                // Remove html
                s = s.replaceAll("\\<html\\>.*\\<\\/html\\>", "");
                JLabel label = new JLabel(s);
                label.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
                panel.add(label);
            }
            borderPanel.add(panel);

            panel = new JPanel();
            JButton button = new JButton(Bundle.getMessage("ErrorDialogButtonExitProgram"));
            button.addActionListener((ActionEvent a) -> {
                result = Result.EXIT_PROGRAM;
                dispose();
            });
            panel.add(button);
            
            panel.add(javax.swing.Box.createRigidArea(new Dimension(5,0)));
            
            button = new JButton(Bundle.getMessage("ErrorDialogButtonNewProfile"));
            button.addActionListener((ActionEvent a) -> {
                result = Result.NEW_PROFILE;
                dispose();
            });
            panel.add(button);
            
            button = new JButton(Bundle.getMessage("ErrorDialogButtonEditConnections"));
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent a) {
                    result = Result.EDIT_CONNECTIONS;
                    dispose();
                }
            });
            panel.add(button);
            
            contentPanel.add(panel);
            
            setContentPane(contentPanel);
            pack();
            
            // Center dialog on screen
            setLocationRelativeTo(null);
            setVisible(true);
        }
    }
    
    
    
//    private static final class ConnectionsPreferencesDialog extends JDialog {
    private static final class ConnectionsPreferencesDialog extends JDialog {
        
        ConnectionsPreferencesDialog() {
            super();
            System.out.format("AAA%n");
            setTitle("Connections preferences");
            setModal(true);
            
            System.out.format("BBB%n");
            ConnectionsPanel contentPanel = new ConnectionsPanel();
            System.out.format("CCC%n");
            
            setContentPane(contentPanel);
            
            System.out.format("DDD%n");
            pack();
            
            System.out.format("EEE%n");
            // Center dialog on screen
            setLocationRelativeTo(null);
            System.out.format("FFF%n");
            
//            contentPanel.addPrefencePanel();
            System.out.format("FFFFFFF%n");
            setVisible(true);
            System.out.format("GGG%n");
            
//            contentPanel.addPrefencePanel();
//            SwingUtilities.updateComponentTreeUI(this);
//            this.revalidate();
            System.out.format("HHH%n");
        }
        
    }
    
    
    
    private static final class ConnectionsPanel extends apps.AppConfigBase {
        
        /**
         * All preferences panels.
         */
        private final List<PreferencesPanel> prefPanels = new ArrayList<>();
        
        private final JPanel detailpanel;
        
        ConnectionsPanel() {
            
            System.out.format("aaa%n");
            JList list = new JList<>();
            JScrollPane listScroller = new JScrollPane(list);
            listScroller.setPreferredSize(new Dimension(100, 100));
            JPanel buttonpanel = new JPanel();
            buttonpanel.setLayout(new BoxLayout(buttonpanel, BoxLayout.Y_AXIS));
            buttonpanel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 3));
            
            System.out.format("bbb%n");
            buttonpanel.removeAll();
            List<String> choices = new ArrayList();
            choices.add("Connections");
            list = new JList<>(choices.toArray(new String[choices.size()]));
            listScroller = new JScrollPane(list);
            listScroller.setPreferredSize(new Dimension(100, 100));
            list.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
            list.setLayoutOrientation(JList.VERTICAL);
            buttonpanel.add(listScroller);
            
            
            System.out.format("ccc%n");
            detailpanel = new JPanel();
            detailpanel.setLayout(new CardLayout());
            detailpanel.setBorder(BorderFactory.createEmptyBorder(6, 3, 6, 6));
            detailpanel.setPreferredSize(new Dimension(700, 400));
            JButton save = new JButton(
                    ConfigBundle.getMessage("ButtonSave"),
                    new ImageIcon(FileUtil.findURL("program:resources/icons/misc/gui3/SaveIcon.png", FileUtil.Location.INSTALLED)));
            save.addActionListener((ActionEvent e) -> {
                savePressed(invokeSaveOptions());
            });
            buttonpanel.add(save);
            
            //DANIEL******************
            JButton danielButton = new JButton("Daniel");
            danielButton.addActionListener((ActionEvent e) -> {
                danielButtonPressed(detailpanel);
            });

            buttonpanel.add(danielButton);
            //DANIEL******************
            
            setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            
            System.out.format("ddd%n");
            
            JTabbedPane tabbedPane = new JTabbedPane();
            for (PreferencesPanel panel : ServiceLoader.load(PreferencesPanel.class)) {
//            for (PreferencesPanel panel : InstanceManager.getList(jmri.swing.PreferencesPanel.class)) {
                System.out.format("dddddd%n");
                if (panel instanceof jmri.jmrix.swing.ConnectionsPreferencesPanel) {
                    System.out.format("eee%n");
                    prefPanels.add(panel);
                    detailpanel.add(panel.getPreferencesComponent());
//                    detailpanel.add(panel.getPreferencesComponent(), "DANIEL");
//                    ThreadingUtil.runOnGUI(() -> {
//DANIEL                        detailpanel.add(panel.getPreferencesComponent());
//                    });
//                    JScrollPane scroller = new JScrollPane(panel.getPreferencesComponent());
//                    scroller.setBorder(BorderFactory.createEmptyBorder());
//                    detailpanel.add(scroller);
                }
            }
            
            System.out.format("hhh%n");
            add(buttonpanel);
            add(new JSeparator(JSeparator.VERTICAL));
            add(detailpanel);
            System.out.format("iii%n");
            list.setSelectedIndex(0);
            
//            CardLayout cl = (CardLayout) (detailpanel.getLayout());
//            cl.show(detailpanel, "DANIEL");
            
            System.out.format("jjj%n");
//            detailpanel.revalidate();
//            test(detailpanel.getComponent(0));
            
            // For testing only! Must be removed!
//            Object comboBox = ((java.awt.Container)((java.awt.Container)((java.awt.Container)((java.awt.Container)((java.awt.Container)detailpanel.getComponent(0)).getComponent(0)).getComponent(0)).getComponent(0)).getComponent(3)).getComponent(0);
//            ((javax.swing.JComboBox)comboBox).setSelectedIndex(((javax.swing.JComboBox)comboBox).getSelectedIndex());
        }
        
        private void addPrefencePanel() {
            if (prefPanels.size() != 1) {
                throw new RuntimeException("There must be one and only one preferencepanel");
            }
            detailpanel.removeAll();
            detailpanel.add(prefPanels.get(0).getPreferencesComponent());
        }
        
        private void test(java.awt.Component c) {
            if (c instanceof java.awt.Container) {
                for (java.awt.Component child : ((java.awt.Container) c).getComponents()) {
                    test(child);
                }
            }
        }
        
        private void danielButtonPressed(JPanel detailpanel) {
//            desiredObject = ((java.awt.Container)((java.awt.Container)((java.awt.Container)((java.awt.Container)((java.awt.Container)detailpanel.getComponent(0)).getComponent(0)).getComponent(0)).getComponent(0)).getComponent(3)).getComponent(0);
//            ((javax.swing.JComboBox)desiredObject).setSelectedIndex(((javax.swing.JComboBox)desiredObject).getSelectedIndex());
            showComponent("1", "", detailpanel);
        }
        
        private Object desiredObject = null;
        
        private void showComponent(String id, String padding, java.awt.Component c) {
            
            if (desiredObject == c)
                System.out.println("Daniel AAAA match");
            
            switch (c.getClass().getName()) {
                case "javax.swing.JLabel":
                    System.out.format("%s%s: %s - %s%n", padding, id, c.getClass().getName(), ((javax.swing.JLabel)c).getText());
                    break;
                    
                case "javax.swing.JButton":
                    System.out.format("%s%s: %s - %s%n", padding, id, c.getClass().getName(), ((javax.swing.JButton)c).getText());
                    break;
                    
                case "javax.swing.JCheckBox":
                    System.out.format("%s%s: %s - %s%n", padding, id, c.getClass().getName(), ((javax.swing.JCheckBox)c).getText());
                    break;
                    
                case "javax.swing.JTextField":
                    System.out.format("%s%s: %s - %s%n", padding, id, c.getClass().getName(), ((javax.swing.JTextField)c).getText());
                    break;
                    
//                case "jmri.swing.JTitledSeparator":
//                    System.out.format("%s%s - %s%n", padding, c.getClass().getName(), ((jmri.swing.JTitledSeparator)c).getName());
//                    break;
                    
                case "javax.swing.JComboBox":
                    String s = "";
                    for (int i=0; i < ((javax.swing.JComboBox)c).getItemCount(); i++) {
                        s = s + ((javax.swing.JComboBox)c).getItemAt(i) + ", ";
                    }
                    System.out.format("%s%s: %s - %s%n", padding, id, c.getClass().getName(), s);
                    
                    if ("1.1.1.1.1.4.1".equals(id)) {
                        if (desiredObject == c)
                            System.out.println("Daniel match");
                        else
                            System.out.println("Daniel ERROR");
//                        ((javax.swing.JComboBox)c).setSelectedIndex(((javax.swing.JComboBox)c).getSelectedIndex());
                    }
                    
                    break;
                    
                case "javax.swing.JViewport":
                    break;
                    
                case "javax.swing.JPanel":
                    System.out.format("%s%s: %s%n", padding, id, c.getClass().getName());
                    
                    if ("1.1.1.1.2.1.1".equals(id)) {
                        java.awt.LayoutManager l = ((javax.swing.JPanel)c).getLayout();
                        System.out.format("%s%n", l.getClass().getName());
                        
                        int count = 1;
                        for (java.awt.Component child : ((java.awt.Container)c).getComponents()) {
                            java.awt.GridBagConstraints cns = ((java.awt.GridBagLayout)l).getConstraints(child);
                            System.out.format("gx: %d, gy: %d, gw: %d, gh: %d, ipadx: %d, ipady: %d, wx: %d, wy: %d%n", cns.anchor, cns.fill, cns.gridx, cns.gridy, cns.gridwidth, cns.gridheight, cns.ipadx, cns.ipady, cns.weightx, cns.weighty);
                            count++;
//                            showComponent(id+"."+Integer.toString(count), padding+"   ", child);
                        }
                        
                        System.out.format("%s%n", l.toString());
                    }
                    break;
                    
                default:
                    System.out.format("%s%s: %s%n", padding, id, c.getClass().getName());
            }
            
            if (c instanceof java.awt.Container) {
                int count = 1;
                for (java.awt.Component child : ((java.awt.Container)c).getComponents()) {
                    showComponent(id+"."+Integer.toString(count++), padding+"   ", child);
                }
            }
        }
        
        public boolean isPreferencesValid() {
            return prefPanels.stream().allMatch((panel) -> (panel.isPreferencesValid()));
        }
        
        @Override
        public void savePressed(boolean restartRequired) {
            
            ShutDownManager sdm = InstanceManager.getNullableDefault(ShutDownManager.class);
            if (!this.isPreferencesValid() && (sdm == null || !sdm.isShuttingDown())) {
                for (PreferencesPanel panel : prefPanels) {
                    if (!panel.isPreferencesValid()) {
                        switch (JOptionPane.showConfirmDialog(this,
                                Bundle.getMessage("InvalidPreferencesMessage", panel.getTabbedPreferencesTitle()),
                                Bundle.getMessage("InvalidPreferencesTitle"),
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.ERROR_MESSAGE)) {
                            case JOptionPane.YES_OPTION:
                                // abort save
                                return;
                            default:
                                // do nothing
                                break;
                        }
                    }
                }
            }
            super.savePressed(restartRequired);
        }
        // package only - for TabbedPreferencesFrame
        boolean invokeSaveOptions() {
            boolean restartRequired = false;
            for (PreferencesPanel panel : prefPanels) {
                // wrapped in isDebugEnabled test to prevent overhead of assembling message
                if (log.isDebugEnabled()) {
                    log.debug("PreferencesPanel {} ({}) is {}.",
                            panel.getClass().getName(),
                            (panel.getTabbedPreferencesTitle() != null) ? panel.getTabbedPreferencesTitle() : panel.getPreferencesItemText(),
                            (panel.isDirty()) ? "dirty" : "clean");
                }
                panel.savePreferences();
                // wrapped in isDebugEnabled test to prevent overhead of assembling message
                if (log.isDebugEnabled()) {
                    log.debug("PreferencesPanel {} ({}) restart is {}required.",
                            panel.getClass().getName(),
                            (panel.getTabbedPreferencesTitle() != null) ? panel.getTabbedPreferencesTitle() : panel.getPreferencesItemText(),
                            (panel.isRestartRequired()) ? "" : "not ");
                }
                if (!restartRequired) {
                    restartRequired = panel.isRestartRequired();
                }
            }
            return restartRequired;
        }
        
    }
    
}
