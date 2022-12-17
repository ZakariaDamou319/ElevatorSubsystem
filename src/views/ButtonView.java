package views;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JPanel;

public class ButtonView extends JPanel {

	private static final long serialVersionUID = 1L;

	private JLabel buttonLabel = new JLabel();

	/**
	 * Constructs a view of a button with a integer identification (floorNum) and a
	 * variable background colour
	 *
	 * @param floorNum the floor number that the button is representing
	 */
	public ButtonView(int floorNum) {

		buttonLabel.setText(floorNum + "");
		buttonLabel.setForeground(Color.WHITE);
		buttonLabel.setAlignmentX(CENTER_ALIGNMENT);
		setBackground(Color.BLACK);
		setPreferredSize(new Dimension(10, 25));

		this.add(buttonLabel);
	}

	/**
	 * Updates the "light" of a button view. Orange if on, black if off
	 *
	 * @param destination true if the light should be on, false otherwise
	 * @param currFloor   true if the button represents the current floor of the
	 *                    elevator, false otherwise
	 */
	public void updateLight(boolean destination, boolean currFloor) {
		if (currFloor) {
			setCurrentFloor();
		} else if (destination) { // on
			setBackground(Color.ORANGE);
			buttonLabel.setForeground(Color.BLACK);
		} else {
			setBackground(Color.BLACK);
			buttonLabel.setForeground(Color.WHITE);
		}
	}

	/**
	 * Sets the button to display if it is the current floor of the elevator
	 */
	private void setCurrentFloor() {
		setBackground(Color.GREEN);
		buttonLabel.setForeground(Color.BLACK);
		// setBorder(new LineBorder(Color.GREEN/* MAGENTA */, 1));
		// buttonLabel.setForeground(Color.GREEN/* MAGENTA */);
	}

}
