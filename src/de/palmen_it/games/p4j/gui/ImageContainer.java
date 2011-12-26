package de.palmen_it.games.p4j.gui;

import java.awt.Container;
import java.awt.Graphics;
import java.awt.Image;

public class ImageContainer extends Container {
	private static final long serialVersionUID = 6570676559355758309L;
	
	private Image _image;
	
	public Image getImage() {
		return _image;
	}
	
	public void setImage(Image value) {
		_image = value;
		repaint();
	}
	
	public ImageContainer() {
		super();
		_image = null;
	}
	
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		if (_image != null) {
			g.drawImage(_image, 0, 0, this);
		}
	}
}
