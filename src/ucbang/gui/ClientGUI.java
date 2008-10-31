package ucbang.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferStrategy;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import ucbang.core.Card;
import ucbang.core.Player;
import ucbang.network.Client;

public class ClientGUI extends JFrame implements KeyListener, ComponentListener{

	private static final long serialVersionUID = 4377855794895936467L;
	BufferStrategy strategy;
	int p;
	StringBuilder chat;
	boolean chatting = false;
	public int width = 800;
	public int height = 600;
	ArrayList<String> text = new ArrayList<String>();
	ArrayList<Color> textColor = new ArrayList<Color>();
	int textIndex = -1; // the bottom line of the text
	public Client client;
	public ClientGUI(int p, Client client) {
		this.client = client;
		this.p = p;
		chat = new StringBuilder();
		// set window sizes
		setPreferredSize(new Dimension(width, height));
		setSize(new Dimension(width, height));
		//setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		addKeyListener(this);
		this.setIgnoreRepaint(true);
		this.setVisible(true);
		this.requestFocus(true);
		this.createBufferStrategy(2);
		strategy = this.getBufferStrategy();
		this.setTitle("UCBang");
		addWindowListener(new WindowAdapter() {
			public void windowActivated(WindowEvent e) {
				paint(getGraphics());
			}
			public void windowClosing(WindowEvent e){
				((ClientGUI)(e.getWindow())).client.running=false;
			}
		});
		this.addComponentListener(this); //<-- lol wtf
	}


	public void paint(Graphics g) {
		Graphics2D graphics;
		try {
			graphics = (Graphics2D) strategy.getDrawGraphics();
		} catch (Exception e) {
			return;
		}
		// fill background w/ tan
		graphics.setColor(new Color(175, 150, 50));
		graphics.fillRect(0, 0, width, height);

		//the ugly proxy skip turn button: this is coded for in Field.java
		graphics.setColor(new Color(255, 255, 255));
		graphics.fillRect(760, 560, 40, 40);
		graphics.setColor(new Color(0, 0, 0));
		graphics.drawString("Skip", 770, 580);

		if (chatting) {
			graphics.setColor(new Color(185, 160, 60));
			graphics.draw3DRect(17, 407, 760, 18, true);
			graphics.setColor(Color.WHITE);
			graphics.drawString("Chatting: " + chat.toString(), 20, 420);
		}
		if (textIndex >= 0) { // there is text to display, must draw it
			for (int n = textIndex; n >= (textIndex < 9 ? 0 : textIndex - 9); n--) {
				graphics.setColor(textColor.get(n));
				graphics.drawString(text.get(n), 20, 580 - 15 * (textIndex - n));
				graphics.setColor(Color.WHITE);
			}
		}
		if(client.field!=null)
                    client.field.paint(graphics);
		graphics.setColor(Color.DARK_GRAY);
		graphics.drawString("Players", 25, 40);
		Iterator<Player> iter = client.players.iterator();
		int n = 0;
		while (iter.hasNext()) {
			Player temp = iter.next();
                        graphics.drawString(temp.name, 30, 60 + 15 * n++);
		}
		graphics.dispose();
		// paint backbuffer to window
		strategy.show();
	}

	/**
	 * appendText with default color of black
	 * 
	 * @param str
	 * @param c
	 */
	public void appendText(String str) {
		appendText(str, Color.WHITE);
	}

	/**
	 * adds text to the bottom of the text area
	 * 
	 * @param str
	 * @param c
	 */
	public void appendText(String str, Color c) {
		// TODO: actually do something with color
		textIndex++;
		text.add(str);
		textColor.add(c);
		paint(getGraphics());
	}

	public void update() {
		paint(this.getGraphics());
	}

	/**
	 * Prompts the player to choose a name
	 * @return the name the player chose
	 */
	public String promptChooseName() {
		String s = "";
		while (s == null || s.length() == 0) {
			s = (String) JOptionPane
			.showInputDialog(this, "What is your name?");
		}
		return s;
	}


	/**
	 * Creates a yes or no prompt with the desired question and title
	 * @param message The message to ask
	 * @param title The title of the message prompt
	 * @return whether the client agrees to start; 0 is yes and 1 is no
	 */
	public int promptYesNo(String message, String title) {
		// I think it's visually more intuitive to have Yes on the left, but keep in
		// mind that this means 0 is yes and 1 is no!
		int r = -1;
		while (r == -1) {
			r = JOptionPane.showOptionDialog(this, message, title,
					JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
					null, new String[] { "Yes", "No" }, "Yes");
		}
		return r;
	}


	/**
	 * Asks the player to choose a card. This is used for many instances. TODO:
	 * replace al with ID of the player.
	 * 
	 * @param al
	 * @return
	 */
	public void promptChooseCard(ArrayList<Card> al, String str1, String str2, boolean force) {
		client.field.pick = al;
		client.prompting = true;
		client.forceDecision = force;
	}
	/**
	 * Adds one bool, then does promptChooseCard
	 * @param str1
	 * @param str2
	 * @param force
	 */
	public void promptTargetCard(String str1, String str2, boolean force) {
		promptChooseCard(null, str1, str2, force);
	}

	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub

	}

	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub

	}

	public void keyTyped(KeyEvent e) {
		if((int)e.getKeyChar()==27){
			if(chatting){
				chatting = false;
				if (chat.length() > 0) {
					chat.delete(0, chat.length());
				}
			}
			else{
				if(client.prompting&&!client.forceDecision){
					client.outMsgs.add("Prompt:-1");
                                        client.nextPrompt = -2;
					client.prompting = false;
				}
				return;
			}
		}
		else if (e.getKeyChar() == '\n') {
			chatting = !chatting;
			if (!chatting && chat.length() > 0) {
				client.addChat(chat.toString());
				chat.delete(0, chat.length());
			}
		} else if (chatting) {
			if ((e.getKeyChar()) == 8 && chat.length() > 0)
				chat.deleteCharAt(chat.length() - 1);
			else
				chat.append(e.getKeyChar());
		} else{
			/*if(Character.isDigit(e.getKeyChar())){
				int f = ((Character)e.getKeyChar())%48;
				appendText(client.players.get(f).name);
			}
			if((char)e.getKeyChar()=='a'){
				appendText(String.valueOf(client.numPlayers));
			}
			if((char)e.getKeyChar()=='s'){
				appendText(String.valueOf(client.players.size()));
			}
			if((char)e.getKeyChar()=='d'){
				appendText(String.valueOf(client.id));
			}
			if((char)e.getKeyChar()=='f'){
				appendText(String.valueOf(client.player.id));
			}
			if((char)e.getKeyChar()=='g'){
				appendText(client.players.get(client.id)+" "+client.player+" "+client.id);
			}
			if((char)e.getKeyChar()=='h'){
				appendText(client.players.get(client.id).hand.size()+""+client.player.hand.size());
			}
			if((char)e.getKeyChar()=='j'){
				String s = "";
				for(Card c:client.player.hand){
					s+=c.name+" ";
				}
				appendText(s);
			}
                        */
		}
		//paint(getGraphics());
	}


	public void componentHidden(ComponentEvent e) {
		// TODO Auto-generated method stub

	}


	public void componentMoved(ComponentEvent e) {
		// TODO Auto-generated method stub

	}

	public void componentResized(ComponentEvent e){
		int oldw = width;
		int oldh = height;

		height = e.getComponent().getHeight();
		width = e.getComponent().getWidth();
		if(height<600)
			height=600;
		if(width<800)
			width=800;
		e.getComponent().setSize(width, height);
		if(client.field!=null)client.field.resize(oldw, oldh, width, height);
	}


	public void componentShown(ComponentEvent e) {
		// TODO Auto-generated method stub

	}
}
