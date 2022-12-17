package views;

import java.awt.Color;
import java.awt.Dimension;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;

import messages.FloorRequest;

public class SchedulerView extends JPanel {

	private static final long serialVersionUID = 1L;

	private static final int PREFERRED_WIDTH = 600;

	private static final int PREFERRED_HEIGHT = 300;

	private JTable waitlistTable, elevatorStateTable;

	/**
	 * Constructs a scheduler view with a waitlist displayed
	 */
	public SchedulerView() {

		JPanel namePanel = new JPanel();
		namePanel.setAlignmentX(CENTER_ALIGNMENT);
		namePanel.setBackground(new Color(0, 0, 0));
		namePanel.setMaximumSize(new Dimension(PREFERRED_WIDTH, 30));
		namePanel.setPreferredSize(new Dimension(PREFERRED_WIDTH, 30));
		namePanel.setMinimumSize(new Dimension(PREFERRED_WIDTH, 30));
		JLabel idLabel = new JLabel();
		idLabel.setText("SCHEDULER");
		idLabel.setAlignmentX(CENTER_ALIGNMENT);
		idLabel.setForeground(new Color(255, 255, 255));
		namePanel.add(idLabel);

		waitlistTable = new JTable(1, 3) {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isCellEditable(int nRow, int nCol) {
				return false;
			}
		};

		JScrollPane waitScrollPane = new JScrollPane(waitlistTable);
		waitlistTable.setFillsViewportHeight(true);

		DefaultTableModel waitTM = (DefaultTableModel) waitlistTable.getModel();

		String[] waitColNames = { "Timestamp", "Source Floor", "Destination Floor" };

		waitTM.setColumnIdentifiers(waitColNames);
		waitlistTable.setAlignmentX(CENTER_ALIGNMENT);
		waitlistTable.setVisible(true);
		waitlistTable.getColumn("Timestamp").setMinWidth(90);
		waitlistTable.getColumn("Timestamp").setPreferredWidth(90);
		waitlistTable.getColumn("Timestamp").setMaxWidth(90);
		waitlistTable.getColumn("Source Floor").setMinWidth(75);
		waitlistTable.getColumn("Source Floor").setPreferredWidth(75);
		waitlistTable.getColumn("Source Floor").setMaxWidth(75);

		elevatorStateTable = new JTable(1, 4) {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isCellEditable(int nRow, int nCol) {
				return false;
			}
		};

		JScrollPane stateScrollPane = new JScrollPane(elevatorStateTable);
		elevatorStateTable.setFillsViewportHeight(true);

		DefaultTableModel stateTM = (DefaultTableModel) elevatorStateTable.getModel();

		String[] stateColNames = { "Elevator ID", "Passengers", "Requests in Service", "Waiting Requests" };

		stateTM.setColumnIdentifiers(stateColNames);
		elevatorStateTable.setAlignmentX(CENTER_ALIGNMENT);
		elevatorStateTable.setVisible(true);

		elevatorStateTable.getColumn("Elevator ID").setMinWidth(75);
		elevatorStateTable.getColumn("Elevator ID").setPreferredWidth(75);
		elevatorStateTable.getColumn("Elevator ID").setMaxWidth(75);
		elevatorStateTable.getColumn("Passengers").setMinWidth(75);
		elevatorStateTable.getColumn("Passengers").setPreferredWidth(75);
		elevatorStateTable.getColumn("Passengers").setMaxWidth(75);

		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.setBorder(new LineBorder(new Color(255, 255, 255), 1, true));
		this.setMinimumSize(new Dimension(PREFERRED_WIDTH, PREFERRED_HEIGHT));
		this.setPreferredSize(new Dimension(PREFERRED_WIDTH, PREFERRED_HEIGHT));
		this.setMaximumSize(new Dimension(PREFERRED_WIDTH, PREFERRED_HEIGHT));

		this.add(namePanel);
		JLabel waitlistTitle = new JLabel("Waitlist");
		waitlistTitle.setAlignmentX(CENTER_ALIGNMENT);
		this.add(waitlistTitle);
		this.add(waitScrollPane);
		JLabel elevatorTitle = new JLabel("Elevators");
		elevatorTitle.setAlignmentX(CENTER_ALIGNMENT);
		this.add(elevatorTitle);
		this.add(stateScrollPane);
	}

	/**
	 * Updates the waitlist table with the current waitlist
	 *
	 * @param list the most current representation of the waitlist
	 */
	@SuppressWarnings("unchecked")
	public void updateWaitlist(List<FloorRequest> list) {
		DefaultTableModel tm = (DefaultTableModel) waitlistTable.getModel();

		ArrayList<FloorRequest> sortedList = new ArrayList<>(list);
		Collections.sort((List<FloorRequest>) sortedList);

		tm.setRowCount(0);
		// add rows
		for (FloorRequest fr : sortedList) {
			String[] data = new String[3];

			Date date = new Date(fr.getTimestamp());
			DateFormat formatter = new SimpleDateFormat("HH:mm:ss.SS");
			formatter.setTimeZone(TimeZone.getTimeZone("UTC"));

			data[0] = formatter.format(date);
			data[1] = fr.getSourceFloor() + "";
			data[2] = fr.getDestinationFloor() + "";
			tm.addRow(data);

		}

		this.revalidate();
	}

	@SuppressWarnings("unchecked")
	public void updateElevators(Map<Integer, List<FloorRequest>> elevatorFloorRequestsInService,
			Map<Integer, List<FloorRequest>> elevatorAssignedFloorRequests) {
		DefaultTableModel tm = (DefaultTableModel) elevatorStateTable.getModel();

		tm.setRowCount(0);
		// add rows
		for (Integer elevatorID : elevatorFloorRequestsInService.keySet()) {
			String[] data = new String[4];

			data[0] = elevatorID + "";
			data[1] = elevatorFloorRequestsInService.get(elevatorID).size() + "";
			data[2] = elevatorFloorRequestsInService.get(elevatorID) + "";
			data[3] = elevatorAssignedFloorRequests.get(elevatorID) + "";
			tm.addRow(data);
		}

		this.revalidate();
	}

}
