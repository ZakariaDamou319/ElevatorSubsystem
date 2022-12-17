package views;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

import elevator_subsystem.DoorState;
import elevator_subsystem.ElevatorButton;
import elevator_subsystem.MotorState;
import elevator_subsystem.Status;

public class ElevatorView extends JPanel {

	private static final long serialVersionUID = 1L;

	private static final int PREFERRED_WIDTH = 150;

	private JLabel idLabel, floorLabel, motorStateLabel, motorStateImg, doorStateLabel;
	private JPanel idPanel, buttonPanel;
	private ArrayList<ButtonView> buttonViews = new ArrayList<>();

	/**
	 * Constructs an ElevatorView with the identification number, id, and numFloors
	 * number of button views
	 *
	 * @param id        the ID of the elevator (should be unique)
	 * @param numFloors the number of floors that the elevator has access to
	 */
	public ElevatorView(int id, int numFloors) {

		idPanel = new JPanel();
		idPanel.setAlignmentX(CENTER_ALIGNMENT);
		idPanel.setBackground(new Color(0, 0, 0));
		idPanel.setMaximumSize(new Dimension(PREFERRED_WIDTH, 30));
		idPanel.setPreferredSize(new Dimension(PREFERRED_WIDTH, 30));
		idPanel.setMinimumSize(new Dimension(PREFERRED_WIDTH, 30));
		idLabel = new JLabel();
		idLabel.setText("Elevator-" + id);
		idLabel.setAlignmentX(CENTER_ALIGNMENT);
		idLabel.setForeground(new Color(255, 255, 255));
		idPanel.add(idLabel);

		floorLabel = new JLabel();
		floorLabel.setAlignmentX(CENTER_ALIGNMENT);

		motorStateLabel = new JLabel();
		motorStateLabel.setAlignmentX(CENTER_ALIGNMENT);

		try {
			BufferedImage motorIcon = ImageIO.read(Objects.requireNonNull(getClass().getResource("/images/idle.PNG")));
			motorStateImg = new JLabel(new ImageIcon(motorIcon.getScaledInstance(20, 20, Image.SCALE_SMOOTH)));
			motorStateImg.setAlignmentX(CENTER_ALIGNMENT);
		} catch (IOException e) {
			e.printStackTrace();
		}

		doorStateLabel = new JLabel();
		doorStateLabel.setAlignmentX(CENTER_ALIGNMENT);

		buttonPanel = new JPanel(new GridLayout(0, PREFERRED_WIDTH % 12, 2, 2));
		for (int i = 1; i <= numFloors; i++) {
			ButtonView bv = new ButtonView(i);
			bv.setAlignmentX(CENTER_ALIGNMENT);
			buttonPanel.add(bv);
			buttonViews.add(bv);
		}

		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.setBorder(new LineBorder(new Color(255, 255, 255), 1, true));

		this.add(idPanel);
		this.add(Box.createRigidArea(new Dimension(0, 5)));
		this.add(floorLabel);

		this.add(Box.createRigidArea(new Dimension(0, 5)));
		JLabel motorTitle = new JLabel("Motor State:");
		motorTitle.setAlignmentX(CENTER_ALIGNMENT);
		this.add(motorTitle);
		this.add(motorStateLabel);
		this.add(motorStateImg);

		this.add(Box.createRigidArea(new Dimension(0, 5)));
		JLabel doorTitle = new JLabel("Door State:");
		doorTitle.setAlignmentX(CENTER_ALIGNMENT);
		this.add(doorTitle);
		this.add(doorStateLabel);

		this.add(Box.createRigidArea(new Dimension(0, 5)));
		JLabel buttonTitle = new JLabel("Buttons:");
		buttonTitle.setAlignmentX(CENTER_ALIGNMENT);
		this.add(buttonTitle);
		this.add(buttonPanel);

		this.revalidate();
	}

	/*
	 * Setter methods
	 */
	public void setFloor(int floor, Status status, Map<Integer, ElevatorButton> buttons) {
		floorLabel.setText(status + (status == Status.APPROACHING ? " floor " : " at floor ") + floor);

		for (int i = 0; i < buttonViews.size(); i++) {
			updateButtons(floor, buttons);
			// buttonViews.get(i).setCurrentFloor(i + 1 == floor);
		}

		if (status == Status.BROKEN) {
			idPanel.setBackground(Color.RED);
			idLabel.setForeground(Color.BLACK);
			try {
				BufferedImage motorIcon = ImageIO
						.read(Objects.requireNonNull(getClass().getResource("/images/broken.PNG")));
				motorStateImg.setIcon(new ImageIcon(motorIcon.getScaledInstance(20, 20, Image.SCALE_SMOOTH)));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		revalidate();
	}

	public void setMotorState(MotorState ms) {
		motorStateLabel.setText("" + ms);

		BufferedImage motorIcon = null;
		try {
			switch (ms) {
			case UP:
				motorIcon = ImageIO.read(Objects.requireNonNull(getClass().getResource("/images/up.PNG")));
				break;
			case DOWN:
				motorIcon = ImageIO.read(Objects.requireNonNull(getClass().getResource("/images/down.PNG")));
				break;
			case STOPPED:
				motorIcon = ImageIO.read(Objects.requireNonNull(getClass().getResource("/images/idle.PNG")));
				break;
			}
			motorStateImg.setIcon(new ImageIcon(motorIcon.getScaledInstance(20, 20, Image.SCALE_SMOOTH)));
		} catch (IOException e) {
			e.printStackTrace();
		}
		revalidate();
	}

	public void setDoorState(DoorState ds) {
		doorStateLabel.setText("" + ds);

		switch (ds) {
		case OPEN:
			doorStateLabel.setBorder(new LineBorder(Color.GREEN, 2));
			break;
		case CLOSED:
			doorStateLabel.setBorder(new LineBorder(Color.ORANGE, 2));
			break;
		}
		revalidate();
	}

	private void updateButtons(int currFloor, Map<Integer, ElevatorButton> buttons) {
		for (ElevatorButton button : buttons.values()) {
			buttonViews.get(button.getButtonNumber() - 1).updateLight(button.isLit(),
					button.getButtonNumber() == currFloor);
		}
		revalidate();
	}

	public void terminate(int floor) {
		idPanel.setBackground(Color.GREEN);
		idLabel.setForeground(Color.BLACK);
		floorLabel.setText("TERMINATED at floor " + floor);
		revalidate();
	}

}
