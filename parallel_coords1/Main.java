package parallel_coords1;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.UIManager;

public class Main extends JFrame implements ActionListener {

	private MainPanel mainPanel;
	private Model db;


	private Main() {
		db = new Model();
		mainPanel = new MainPanel();
		JMenuBar mb = setupMenu();
		setJMenuBar(mb);
		setContentPane(mainPanel);

		setPreferredSize(new Dimension(800,600));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("Parallel Coordinates");
		pack();
		setVisible(true);
	}

	public static void main(String[] args) {

		//this makes the GUI adopt the look-n-feel of the windowing system (Windows/X/Mac)
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) { }

		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new Main();
			}
		});
	}

	public static void say(Object o) {
		System.out.println(o);
	}

	private JMenuBar setupMenu() {
		//instantiate menubar, menus, and menu options
		JMenuBar menuBar = new JMenuBar();
		JMenu fileMenu = new JMenu("Table");
		JMenuItem marathon = new JMenuItem("Marathon");
		JMenuItem cis = new JMenuItem("CIS Students 2007 - 2012");
		JMenuItem cis2012 = new JMenuItem("CIS Students in 2012");
		JMenu reset = new JMenu("Reset");
		JMenuItem resetButton = new JMenuItem("yes do it");

		//setup action listeners
		marathon.setActionCommand("marathon");
		cis.setActionCommand("cis");
		cis2012.setActionCommand("cis2012");
		marathon.addActionListener(this);
		cis.addActionListener(this);
		cis2012.addActionListener(this);
		resetButton.setActionCommand("reset");
		resetButton.addActionListener(this);

		//now hook them all together
		fileMenu.add(marathon);
		fileMenu.add(cis);
		fileMenu.add(cis2012);
		reset.add(resetButton);

		menuBar.add(fileMenu);
		menuBar.add(reset);
		return menuBar;
	}

	@Override
	public void actionPerformed(ActionEvent a) {
		if(a.getActionCommand().equals("reset")){
			mainPanel.resetLines();
		} else {
			String table = a.getActionCommand();
			List<Axis> axes = db.performReconnaissanceQuery(table);
			int count = db.performFullQuery(table, axes);
			mainPanel.setAxes(axes, count);
		}
	}

}
