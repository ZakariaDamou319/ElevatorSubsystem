package views;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import elevator_subsystem.ElevatorSubsystem;
import floor_subsystem.FloorSubsystem;
import scheduler.Scheduler;

public class SystemView {

	JFrame frame = new JFrame("Elevators");
	JTextField numberOfElevators, numberOfFloors, requestFile;

	/**
	 * Constructs the initial view of the System, allowing the user to configure
	 * properties such as number of elevators, number of floors and an optional path
	 * to a custom request file
	 */
	public SystemView() {
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		numberOfElevators = new JTextField();
		numberOfElevators.setPreferredSize(new Dimension(50, 30));
		numberOfFloors = new JTextField();
		numberOfFloors.setPreferredSize(new Dimension(50, 30));
		requestFile = new JTextField();
		requestFile.setPreferredSize(new Dimension(50, 30));

		JButton submit = new JButton("Submit");
		submit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (validateInput())
					setupSubsystemViews();

			}
		});

		JPanel optionPanel = new JPanel();
		optionPanel.setLayout(new GridLayout(4, 1));

		optionPanel.add(new JLabel("How many elevators would you like?"));
		optionPanel.add(numberOfElevators);
		optionPanel.add(new JLabel("How many floors would you like?"));
		optionPanel.add(numberOfFloors);
		optionPanel.add(new JLabel("Input path to request file (a default will be used if there is no input)"));
		optionPanel.add(requestFile);
		optionPanel.add(submit);

		frame.add(optionPanel);
		frame.setResizable(true);
		frame.pack();
		frame.setVisible(true);
	}

	/**
	 * Validates the input of number of elevators, floors and input file
	 *
	 * @return true if elevators and floors are purely numbers and the file is
	 *         valid, false otherwise
	 */
	private boolean validateInput() {
		if (!numberOfElevators.getText().trim().matches("[0-9]+") || numberOfElevators.getText().trim().isEmpty())
			return false;
		if (!numberOfFloors.getText().trim().matches("[0-9]+") || numberOfFloors.getText().trim().isEmpty())
			return false;

		File f = new File(requestFile.getText().trim());
		if (!requestFile.getText().trim().isEmpty() && (!f.isFile() || !f.canRead()))
			return false;

		return true;
	}

	/**
	 * Sets up the subsytems and appropriate views and starts the individual threads
	 */
	private void setupSubsystemViews() {
		int numElevators = Integer.parseInt(numberOfElevators.getText());
		int numFloors = Integer.parseInt(numberOfFloors.getText());
		frame.getContentPane().removeAll();

		SchedulerView sv = new SchedulerView();
		Scheduler scheduler = new Scheduler(sv);

		new Thread(new Runnable() {
			@Override
			public void run() {
				scheduler.run();
			}
		}).start();

		JPanel subsystemPanel = new JPanel();
		for (int i = 0; i < numElevators; i++) {
			ElevatorView ev = new ElevatorView(i, numFloors);
			subsystemPanel.add(ev);
			ElevatorSubsystem elevatorSubsystem = new ElevatorSubsystem(i, numFloors, ev);
			Thread elevatorSubsystemThread = new Thread(elevatorSubsystem);
			elevatorSubsystemThread.start();
		}

		FloorSubsystem fs = new FloorSubsystem(requestFile.getText());
		new Thread(new Runnable() {
			@Override
			public void run() {
				fs.run();
			}
		}).start();

		subsystemPanel.add(sv);
		frame.setMinimumSize(new Dimension(650, 625)); // width of scheduler + height of 1 elevator + scheduler
		frame.setPreferredSize(
				new Dimension(160 * (int) Math.ceil(((subsystemPanel.getComponentCount() + 3) / 2.0)), 625));
		frame.add(subsystemPanel);
		frame.pack();
	}

	public static void main(String[] args) {
		new SystemView();
	}
}
