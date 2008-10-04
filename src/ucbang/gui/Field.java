package ucbang.gui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;

import ucbang.core.Card;
import ucbang.core.Deck;
import ucbang.network.Client;

public class Field implements MouseListener, MouseMotionListener{
	Client client;
	public BSHashMap<Card, Clickable> cards = new BSHashMap<Card, Clickable>();
	CardDisplayer cd;
	Point pointOnCard;
	Clickable movingCard;
	Card clicked;
	ArrayList<Card> pick;

	ArrayList<HandSpace> handPlacer = new ArrayList<HandSpace>(); //to avoid npe
	ArrayList<CardSpace> characters = new ArrayList<CardSpace>();
	String description;
	Point describeWhere;
	public Field(CardDisplayer cd, Client c) {
		this.cd=cd;
		client = c;
	}
	public void add(Card card, int x, int y, int player){
		cards.put(card, new CardSpace(card, new Rectangle(x,y,60,90), player));
	}
	public void removeLast(int player){
		//System.out.println("Removed "+cards.remove(handPlacer.get(player).removeLast()));
	}
	public void add(Card card, int player){
		int xoffset = (player==client.id?30*(client.player.hand.size()-1):30*(client.players.get(player).hand.size()-1));

		if(card.type==1){//this a character card
			int x=350;
			int y=200;
			cards.put(card, new CardSpace(card, new Rectangle(x, y,60,90), player));
		}else{
			int x=(int) handPlacer.get(player).rect.x+handPlacer.get(player).rect.width+xoffset;
			int y=(int) handPlacer.get(player).rect.y;
			CardSpace cs = new CardSpace(card, new Rectangle(x, y,60,90), player);
			cards.put(card, cs);
			handPlacer.get(player).addCard(cs);
		}
	}
	int textHeight(String message, Graphics2D graphics){
		int lineheight=(int)graphics.getFont().getStringBounds("|", graphics.getFontRenderContext()).getHeight();
		return message.split("\n").length*lineheight;
	}
	int textWidth(String message, Graphics2D graphics){
		String[] lines = message.split("\n");
		int width=0;
		for(int i=0;i<lines.length;i++){
			int w=(int)graphics.getFont().getStringBounds(lines[i], graphics.getFontRenderContext()).getWidth();
			if(width<w)
				width=w;
		}
		return width;
	}
	void improvedDrawString(String message, int x, int y, Graphics2D graphics){
		int lineheight=(int)graphics.getFont().getStringBounds("|", graphics.getFontRenderContext()).getHeight();
		String[] lines = message.split("\n");
		for(int i=0;i<lines.length;i++){
			graphics.drawString(lines[i], x, y+i*lineheight);
		}
	}
	public void paint(Graphics2D graphics){
		for(HandSpace hs : handPlacer)
			graphics.draw(hs.rect);
		Iterator<Clickable> iter = cards.values().iterator();
		while(iter.hasNext()){
			Clickable temp = iter.next();
			if(temp instanceof CardSpace){
				CardSpace crd = (CardSpace)temp;
				Color inner;
				switch(crd.card.location){
				case 0:
					inner=Color.BLACK;
					break;
				case 1:
					if(crd.card.type==5)
						inner=new Color(100,100,200);
					else
						inner=new Color(100,200,100);
					break;
				default:
					inner=new Color(200,100,100);
				}
				Color outer=client.id==1?Color.RED:Color.BLUE;
				cd.paint(crd.card.name, graphics, crd.rect.x, crd.rect.y, crd.rect.width, temp.rect.height, 
							inner,outer);
				if(crd.card.name.equals("BULLETBACK"))crd.rotate(1);
			}else if(temp instanceof HandSpace){
				HandSpace hs = (HandSpace)temp;
				graphics.draw3DRect(hs.rect.x, hs.rect.y, hs.rect.width, hs.rect.height, true);
			}else{
				System.out.println("WTF");
			}
		}
		if(description!=null){
			Rectangle2D bounds=graphics.getFont().getStringBounds(description, graphics.getFontRenderContext());
			Color temp=graphics.getColor();
			graphics.setColor(Color.YELLOW);
			graphics.fill3DRect(describeWhere.x, describeWhere.y-(int)bounds.getHeight()+32, textWidth(description, graphics), textHeight(description, graphics),false);
			graphics.setColor(Color.BLACK);
			improvedDrawString(description, describeWhere.x, describeWhere.y+30,graphics);
			graphics.setColor(temp);
		}
	}
	public Clickable binarySearchCardAtPoint(Point ep){
		//bsearch method
		int start;
		int end;

		ArrayList<Clickable> al = cards.values(); //search the values arrayList for...

		int a = 0, b = al.size(), index = al.size() / 2;

		while (a != b) {
			if (ep.y > al.get(index).rect.y + 85) { // the "start" is the value of the card whose bottom is closest to the cursor (and on the cursor)
				a = index + 1;
			} else {
				b = index;
			}
			index = a + (b - a) / 2;
		}
		start = a;
		a = 0;
		b = al.size();
		index = al.size() / 2;
		while (a != b) {
			if (ep.y > al.get(index).rect.y) { // the "end" is the value of the card whose top is closest to the cursor (and on the cursor)
				a = index + 1;
			} else {
				b = index;
			}
			index = a + (b - a) / 2;
		}
		end = a - 1;
		for (int n = end; n>= start; n--) {
			Clickable s = al.get(n);
			if (s.rect.contains(ep.x, ep.y)) {
				return al.get(n);
			}
		}
		return null;
	}
	public void start2(){
		handPlacer = new ArrayList<HandSpace>(client.numPlayers);
		double theta;
		for(int player = 0; player<client.numPlayers; player++){
			theta = (player-client.id)*(2*Math.PI/client.numPlayers)-Math.PI/2;
			handPlacer.add(new HandSpace(new Rectangle(350+(int)(250*Math.cos(theta)),280-(int)(220*Math.sin(theta)),10,10), player));  
		}
		clear();
		for(int i=0;i<client.players.size();i++){
			if(client.players.get(i).character>=0){
				System.out.println(i+":"+Deck.Characters.values()[client.players.get(i).character]);
				Card chara = new Card(Deck.Characters.values()[client.players.get(i).character]);
				int x=(int) handPlacer.get(i).rect.x-60;
				int y=(int) handPlacer.get(i).rect.y;
				CardSpace csp = new CardSpace(chara,new Rectangle(x,y,60,90), i);
				cards.put(chara, csp);
			}else if(client.id==i){
				System.out.println(i+":"+Deck.Characters.values()[client.player.character]);
				Card chara = new Card(Deck.Characters.values()[client.player.character]);
				int x=(int) handPlacer.get(i).rect.x-60;
				int y=(int) handPlacer.get(i).rect.y;
				CardSpace csp = new CardSpace(chara,new Rectangle(x,y,60,90), i);
				cards.put(chara, csp);
			}
		}
		
	}
	public void clear(){
		Point pointOnCard = null;
		CardSpace movingCard = null;
		cards.clear();
	}

	public void mouseClicked(MouseEvent e) {
		Point ep=e.getPoint();
		////the ugly proxy skip turn button
		if(new Rectangle(760, 560, 40, 40).contains(ep)){
			if(client.prompting&&!client.forceDecision){
				client.outMsgs.add("Prompt:-1");
				client.prompting = false;
			}
			return;
		}
		Clickable cl = binarySearchCardAtPoint(ep);

		if (cl instanceof CardSpace) {
			CardSpace cs = (CardSpace) cl;
			if (cs != null && cs.card != null){
				System.out.println("Clicked on " + cs.card.name + "whose index is " + pick.indexOf(cs.card));
                        }
			else
				return;
			if (e.getButton() == MouseEvent.BUTTON3) {
				System.out.println(cs.card.description);
				description = cs.card.description;
				describeWhere = ep;
			} else if (client.prompting && pick.contains(cs.card)) {
				System.out.println("sending prompt...");
				if (cs.card.type == 1) {
					client.outMsgs.add("Prompt:"
							+ pick.indexOf(cs.card));
					client.player.hand.clear(); //you just picked a character card
					clear();
				} else {
					client.outMsgs.add("Prompt:"
							+ pick.indexOf(cs.card));
				}
				client.prompting = false;
			} else { //TODO: debug stuff
				if (client.prompting) {
					System.out.println("i was prompting");
					if (!client.player.hand.contains(cs.card)) {
						System.out
						.println("but the arraylist didn't contain the card i was looking for!");
						System.out.println(cs.card + " "
								+ client.player.hand);
					}
				}
			}
		}
	}

	public void mousePressed(MouseEvent e) {
		movingCard = binarySearchCardAtPoint(e.getPoint());

		if(movingCard==null){//placer handler
			for(HandSpace cs : handPlacer)
				if(cs.rect.contains(e.getPoint())){
					movingCard=cs;
				}
		}
		if(movingCard!=null){
			pointOnCard = new Point(e.getPoint().x-movingCard.rect.x, e.getPoint().y-movingCard.rect.y);
			//System.out.println("picked up card");
		}
	}
	public void mouseReleased(MouseEvent e) {
		if(movingCard!=null){
			//System.out.println("card dropped");
		}
		movingCard = null;
		description = null;
	}

	public void mouseDragged(MouseEvent e) {
		//System.out.println("dragging");
		if(movingCard!=null){
			movingCard.move(Math.max(0, Math.min(e.getPoint().x-pointOnCard.x,745)),Math.max(0, Math.min(e.getPoint().y-pointOnCard.y,515))); //replace boundaries with width()/height() of frame?
		}
		else{
			//System.out.println("not dragging");
		}
	}

	public class BSHashMap<K,V> extends HashMap<K,V>{
		ArrayList<V> occupied = new ArrayList<V>();

		public V put(K key, V value){
			occupied.add(value);
			return super.put(key, value);
		}

		public ArrayList<V> values(){
			ArrayList<V> al = new ArrayList<V>();
			Collections.sort(occupied, new Comparator(){
				public int compare(Object o1, Object o2) {
					return ((Comparable<Object>)o1).compareTo(o2);
				}
			});
			al.addAll(occupied);
			return al;
		}
		public void clear(){
			occupied.clear();
			super.clear();
		}
		public V remove(Object o){
			occupied.remove(get(o));
			V oo = super.remove(o);
			return oo;
		}
	}

	/*
	 * Contains a card and a rectangle
	 */
	private class CardSpace extends Clickable{
		public Card card;

		public CardSpace(Card c, Rectangle r, int player){
			card = c;
			rect = r;
			playerid = player;
		}

	}
	private class HandSpace extends Clickable{
		ArrayList<CardSpace> cards = new ArrayList<CardSpace>();
		public HandSpace(Rectangle r, int player){
			rect=r;
			playerid=player;
		}
		public void addCard(CardSpace card){
			cards.add(card);
		}
		public CardSpace removeLast(){
			return cards.remove(cards.size()-1);
		}
		public void move(int x, int y){
			int dx = x-rect.x;
			int dy = y-rect.y;
			super.move(x, y);
			Iterator<CardSpace> iter = cards.iterator();
			while(iter.hasNext()){
				iter.next().translate(dx, dy);
			}
		}
	}
	private abstract class Clickable implements Comparable<Clickable>{
		public Rectangle rect;
		public int location; //position of card on field or in hand
		public int playerid;
		public AffineTransform at;
		int oldrotation=0;
		public boolean draggable=true;
		public int compareTo(Clickable o) {
			if(o.rect.getLocation().y!=rect.getLocation().y)
				return ((Integer)rect.getLocation().y).compareTo(o.rect.getLocation().y);
			else
				return ((Integer)rect.getLocation().x).compareTo(o.rect.getLocation().x);
		}
		public void move(int x, int y){
			if(at!=null)at.translate(rect.x-x, rect.y-y);
			rect.setLocation(x, y);
		}

		public void rotate(int quadrant){//rotates in terms of 90 degree increments. call with 0 to reset.
			int realrotation=quadrant-oldrotation;
			if(realrotation>0 && realrotation<4){
				if(this instanceof CardSpace){
					cd.rotateImage(((CardSpace)this).card.name, quadrant);
				}
				at = AffineTransform.getQuadrantRotateInstance(realrotation, rect.x+rect.width/2, rect.y+rect.height/2);
				oldrotation=quadrant;
				PathIterator iter = rect.getPathIterator(at);
				int i=0;
				float[] pts= new float[6];
				int newx=-1, newy=-1, newwidth=-1, newheight=-1;
				while(!iter.isDone()){
					int type = iter.currentSegment(pts);
					switch(type){
					case PathIterator.SEG_MOVETO :
						//temp.add((int)pts[0],(int)pts[1]);
						//System.out.println(pts[0]+","+pts[1]);
						break;
					case PathIterator.SEG_LINETO :
						if(i==1){
							newx=(int) pts[0];//misnomers for this part lol.
							newy=(int) pts[1];
						}else if(i==3){
							newwidth=(int)Math.abs(newx-(int)pts[0]);
							newheight=(int)Math.abs(newy-(int)pts[1]);
							newx=(int) pts[0];
							newy=(int) pts[1];
						}
						break;
					}
					i++;
					iter.next();
				}
				rect = new Rectangle(newx, newy, newwidth, newheight);
				System.out.println(rect);
				at=null;
			}else{
				//at=null;
			}
		}
		public void translate(int dx, int dy){
			rect.translate(dx, dy);
		}
	}

	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mouseMoved(MouseEvent e) {}
}
