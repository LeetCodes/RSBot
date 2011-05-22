package org.rsbot.gui;

import org.rsbot.Configuration;
import org.rsbot.Configuration.OperatingSystem;
import org.rsbot.service.Monitoring;
import org.rsbot.util.StringUtil;
import org.rsbot.util.io.IniParser;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.HashMap;
import java.util.logging.Logger;

/**
 * @author Paris
 */
public class SettingsManager extends JDialog {
	private static Logger log = Logger.getLogger(SettingsManager.class.getName());
	private static final long serialVersionUID = 1657935322078534422L;
	private Preferences prefs;

	public class Preferences {
		private File store;

		/**
		 * Whether or not to disable ads.
		 */
		public boolean ads = true;

		public boolean confirmations = true;
		public boolean monitoring = true;
		public boolean shutdown = false;
		public int shutdownTime = 10;
		public boolean web = false;
		public String webBind = "localhost:9500";
		public boolean webPassRequire = false;
		public String webPass = "";
		public boolean allowResize = false;

		public Preferences(final File store) {
			this.store = store;
		}

		public void load() {
			HashMap<String, String> keys = null;
			try {
				if (!store.exists()) {
					store.createNewFile();
				}
				final BufferedReader reader = new BufferedReader(new FileReader(store));
				keys = IniParser.deserialise(reader).get(IniParser.emptySection);
				reader.close();
			} catch (final IOException ignored) {
				log.severe("Failed to load preferences");
			}
			if (keys == null || keys.isEmpty()) {
				return;
			}
			if (keys.containsKey("ads")) {
				ads = IniParser.parseBool(keys.get("ads"));
			}
			if (keys.containsKey("confirmations")) {
				confirmations = IniParser.parseBool(keys.get("confirmations"));
			}
			if (keys.containsKey("botresize")) {
				allowResize = IniParser.parseBool(keys.get("botresize"));
			}
			if (keys.containsKey("monitoring")) {
				monitoring = IniParser.parseBool(keys.get("ads"));
			}
			if (keys.containsKey("shutdown")) {
				shutdown = IniParser.parseBool(keys.get("shutdown"));
			}
			if (keys.containsKey("shutdownTime")) {
				shutdownTime = Integer.parseInt(keys.get("shutdownTime"));
				shutdownTime = Math.max(Math.min(shutdownTime, 60), 3);
			}
			if (keys.containsKey("web")) {
				web = IniParser.parseBool(keys.get("web"));
			}
			if (keys.containsKey("webBind")) {
				webBind = keys.get("webBind");
			}
			if (keys.containsKey("webPassRequire")) {
				webPassRequire = IniParser.parseBool(keys.get("webPassRequire"));
			}
			if (keys.containsKey("webPass")) {
				webPass = keys.get("webPass");
			}
		}

		public void save() {
			final HashMap<String, String> keys = new HashMap<String, String>();
			keys.put("ads", Boolean.toString(ads));
			keys.put("confirmations", Boolean.toString(confirmations));
			keys.put("botresize", Boolean.toString(allowResize));
			keys.put("monitoring", Boolean.toString(monitoring));
			keys.put("shutdown", Boolean.toString(shutdown));
			keys.put("shutdownTime", Integer.toString(shutdownTime));
			keys.put("web", Boolean.toString(web));
			keys.put("webBind", webBind);
			keys.put("webPassRequire", Boolean.toString(webPassRequire));
			keys.put("webPass", webPass);
			final HashMap<String, HashMap<String, String>> data = new HashMap<String, HashMap<String, String>>();
			data.put(IniParser.emptySection, keys);
			try {
				final BufferedWriter out = new BufferedWriter(new FileWriter(store));
				IniParser.serialise(data, out);
				out.close();
			} catch (final IOException ignored) {
				log.severe("Could not save preferences");
			}
		}

		public void commit() {
			Monitoring.setEnabled(monitoring);
		}
	}

	public Preferences getPreferences() {
		return prefs;
	}

	public SettingsManager(final Frame owner, final File store) {
		super(owner, Messages.OPTIONS, true);
		prefs = new Preferences(store);
		prefs.load();
		owner.setMinimumSize(prefs.allowResize ? new Dimension(300, 300) : owner.getPreferredSize());
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		setIconImage(Configuration.getImage(Configuration.Paths.Resources.ICON_WRENCH));

		final JPanel panelOptions = new JPanel(new GridLayout(0, 1));
		panelOptions.setBorder(BorderFactory.createTitledBorder("Display"));
		final JPanel panelInternal = new JPanel(new GridLayout(0, 1));
		panelInternal.setBorder(BorderFactory.createTitledBorder("Internal"));
		final JPanel panelWeb = new JPanel(new GridLayout(2, 1));
		panelWeb.setBorder(BorderFactory.createTitledBorder("Web UI"));

		final JCheckBox checkAds = new JCheckBox(Messages.DISABLEADS);
		checkAds.setToolTipText("Show advertisment on startup");
		checkAds.setSelected(prefs.ads);

		final JCheckBox checkConf = new JCheckBox(Messages.DISABLECONFIRMATIONS);
		checkConf.setToolTipText("Supress confirmation messages");
		checkConf.setSelected(prefs.confirmations);

		final JCheckBox allowResize = new JCheckBox(Messages.ALLOWGUIRESIZE);
		allowResize.setToolTipText("Allow resizing of the bot gui");
		allowResize.setSelected(prefs.allowResize);

		final JCheckBox checkMonitor = new JCheckBox(Messages.DISABLEMONITORING);
		checkMonitor.setToolTipText("Monitor system information to improve development");
		checkMonitor.setSelected(prefs.monitoring);

		final JPanel panelShutdown = new JPanel(new GridLayout(1, 2));
		final JCheckBox checkShutdown = new JCheckBox(Messages.AUTOSHUTDOWN);
		checkShutdown.setToolTipText("Automatic system shutdown after specified period of inactivty");
		checkShutdown.setSelected(prefs.shutdown);
		panelShutdown.add(checkShutdown);
		final SpinnerNumberModel modelShutdown = new SpinnerNumberModel(prefs.shutdownTime, 3, 60, 1);
		final JSpinner valueShutdown = new JSpinner(modelShutdown);
		panelShutdown.add(valueShutdown);
		checkShutdown.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent arg0) {
				valueShutdown.setEnabled(((JCheckBox) arg0.getSource()).isSelected());
			}
		});
		checkShutdown.setEnabled(Configuration.getCurrentOperatingSystem() == OperatingSystem.WINDOWS);
		valueShutdown.setEnabled(checkShutdown.isEnabled() && checkShutdown.isSelected());

		final JPanel[] panelWebOptions = new JPanel[2];
		for (int i = 0; i < panelWebOptions.length; i++) {
			panelWebOptions[i] = new JPanel(new GridLayout(1, 2));
		}
		final JCheckBox checkWeb = new JCheckBox(Messages.BINDTO);
		checkWeb.setToolTipText("Remote control via web interface");
		checkWeb.setSelected(prefs.web);
		panelWebOptions[0].add(checkWeb);
		final JFormattedTextField textWebBind = new JFormattedTextField(prefs.webBind);
		textWebBind.setToolTipText("Example: localhost:9500");
		panelWebOptions[0].add(textWebBind);
		final JCheckBox checkWebPass = new JCheckBox(Messages.USEPASSWORD);
		checkWebPass.setSelected(prefs.webPassRequire);
		panelWebOptions[1].add(checkWebPass);
		final JPasswordField textWebPass = new JPasswordField("\0\0\0\0\0\0\0\0");
		textWebPass.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(final FocusEvent e) {
				textWebPass.setText("");
			}
		});
		panelWebOptions[1].add(textWebPass);
		checkWebPass.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				textWebPass.setEnabled(checkWebPass.isSelected() && checkWebPass.isEnabled());
			}
		});
		checkWeb.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				final boolean enabled = checkWeb.isSelected();
				textWebBind.setEnabled(enabled);
				checkWebPass.setEnabled(enabled);
				for (final ActionListener action : checkWebPass.getActionListeners()) {
					action.actionPerformed(null);
				}
			}
		});
		for (final ActionListener action : checkWeb.getActionListeners()) {
			action.actionPerformed(null);
		}

		panelOptions.add(checkAds);
		panelOptions.add(checkConf);
		panelOptions.add(allowResize);
		panelInternal.add(checkMonitor);
		panelInternal.add(panelShutdown);
		panelWeb.add(panelWebOptions[0]);
		panelWeb.add(panelWebOptions[1]);

		final GridLayout gridAction = new GridLayout(1, 2);
		gridAction.setHgap(5);
		final JPanel panelAction = new JPanel(gridAction);
		final int pad = 6;
		panelAction.setBorder(BorderFactory.createEmptyBorder(pad, pad, pad, pad));
		panelAction.add(Box.createHorizontalGlue());

		final JButton buttonOk = new JButton("OK");
		buttonOk.setPreferredSize(new Dimension(85, 30));
		buttonOk.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				setVisible(false);
				prefs.ads = checkAds.isSelected();
				prefs.confirmations = checkConf.isSelected();
				prefs.allowResize = allowResize.isSelected();
				owner.setMinimumSize(prefs.allowResize ? new Dimension(300, 300) : owner.getPreferredSize());
				prefs.monitoring = checkMonitor.isSelected();
				prefs.shutdown = checkShutdown.isSelected();
				prefs.shutdownTime = modelShutdown.getNumber().intValue();
				prefs.web = checkWeb.isSelected();
				prefs.webBind = textWebBind.getText();
				final char[] pass = textWebPass.getPassword();
				if (pass.length > 0 && pass[0] != '\0') {
					prefs.webPass = StringUtil.sha1sum(new String(pass));
				}
				prefs.webPassRequire = checkWebPass.isSelected() && checkWebPass.isEnabled();
				prefs.commit();
				prefs.save();
				dispose();
			}
		});
		final JButton buttonCancel = new JButton("Cancel");
		buttonCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				setVisible(false);
				checkAds.setSelected(prefs.ads);
				checkConf.setSelected(prefs.confirmations);
				allowResize.setSelected(prefs.allowResize);
				owner.setMinimumSize(prefs.allowResize ? new Dimension(300, 300) : owner.getPreferredSize());
				checkMonitor.setSelected(prefs.monitoring);
				checkShutdown.setSelected(prefs.shutdown);
				modelShutdown.setValue(prefs.shutdownTime);
				dispose();
			}
		});

		panelAction.add(buttonOk);
		panelAction.add(buttonCancel);

		final JPanel panel = new JPanel(new GridLayout(0, 1));
		panel.setBorder(panelAction.getBorder());
		panel.add(panelOptions);
		panel.add(panelInternal);

		if (!Configuration.RUNNING_FROM_JAR) {
			panel.add(panelWeb); // hide web options from non-development builds for now
		}

		add(panel);
		add(panelAction, BorderLayout.SOUTH);

		getRootPane().setDefaultButton(buttonOk);
		buttonOk.requestFocus();

		pack();
		setLocationRelativeTo(getOwner());
		setResizable(false);

		addWindowListener(new WindowListener() {
			public void windowClosing(WindowEvent arg0) {
				buttonCancel.doClick();
			}

			public void windowActivated(WindowEvent arg0) {
			}

			public void windowClosed(WindowEvent arg0) {
			}

			public void windowDeactivated(WindowEvent arg0) {
			}

			public void windowDeiconified(WindowEvent arg0) {
			}

			public void windowIconified(WindowEvent arg0) {
			}

			public void windowOpened(WindowEvent arg0) {
			}
		});
	}

	public void display() {
		setVisible(true);
	}
}