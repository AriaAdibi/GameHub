package gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JTextField;
import javax.swing.text.Document;

public class HintedTextField extends JTextField {

	private static final long serialVersionUID = 74177123132L;

	private String hint;

	public HintedTextField(String hint) {
		super();
		setHint(hint);
	}

	public HintedTextField(int columns, String hint) {
		super(columns);
		setHint(hint);
	}

	public HintedTextField(String text, String hint) {
		super(text);
		setHint(hint);
	}

	public HintedTextField(String text, int columns, String hint) {
		super(text, columns);
		setHint(hint);
	}

	public HintedTextField(Document doc, String text, int columns, String hint) {
		super(doc, text, columns);
		setHint(hint);
	}

	public void setHint(String hint) {
		this.hint = hint;
	}

	public String getHint() {
		return hint;
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		if (getText() == null || getText().equals("")) {
			g.setColor(Color.darkGray);
			g.setFont(getFont());
			g.drawString(hint, -1 + getMargin().left + getInsets().left,
					getHeight() - getMargin().bottom - getInsets().bottom - 1);
		}
	}

}
