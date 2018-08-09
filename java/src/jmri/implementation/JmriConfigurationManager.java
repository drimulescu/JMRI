package jmri.implementation;

import apps.AppsBase;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import apps.gui3.TabbedPreferencesAction;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import jmri.Application;
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
import jmri.util.FileUtil;
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
                            
                            ErrorDialog dialog = new ErrorDialog(errorList);
                            
                            switch (dialog.result) {
                                case NEW_PROFILE:
                                    AddProfileDialog apd = new AddProfileDialog(null, true, false);
                                    apd.setLocationRelativeTo(null);
                                    apd.setVisible(true);
                                    // Restart program
                                    AppsBase.handleRestart();
                                    break;
                                    
                                case EDIT_CONNECTIONS:
                                    // Show preferences dialog, but only the connections panel
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



    private static class ErrorDialog extends JDialog {
        
        enum Result {
            EXIT_PROGRAM,
            NEW_PROFILE,
            EDIT_CONNECTIONS,
        }
        
        
        Result result = Result.EXIT_PROGRAM;

        ErrorDialog(List<String> list) {
            super();
            setTitle("JMRI is unable to connect");
            setModal(true);
            JPanel contentPanel = new JPanel();
            contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
            JPanel panel = new JPanel();
            panel.add(new JLabel("Errors occurred when JMRI tried to connect"));
            contentPanel.add(panel);

            JPanel marginPanel = new JPanel();
            marginPanel.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
            marginPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(10,10,10,10));
            contentPanel.add(marginPanel);
            panel = new JPanel();
            panel.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.black));
            for (String s : list) {
                // Remove html
                s = s.replaceAll("\\<html\\>.*\\<\\/html\\>", "");
                JLabel label = new JLabel(s);
                label.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
                panel.add(label);
            }
            marginPanel.add(panel);

            panel = new JPanel();
            JButton button = new JButton("Exit program");
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent a) {
                    result = Result.EXIT_PROGRAM;
                    dispose();
                }
            });
            panel.add(button);
            
            button = new JButton("New profile");
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent a) {
                    result = Result.NEW_PROFILE;
                    dispose();
                }
            });
            panel.add(button);
            
            button = new JButton("Edit connections");
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

}
