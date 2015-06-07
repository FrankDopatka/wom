package frontend;

import frontend.menu.MenuTop;
import interfaces.iBackendSpiel;

import java.awt.BorderLayout;
import java.awt.Font;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import daten.D;
import daten.D_Karte;
import daten.Xml;
import backend.BackendSpielStub;

public class Frontend extends JFrame{
	private static final long serialVersionUID = 1L;
	private static final byte POLLZEIT=1; // in Sekunden
	private iBackendSpiel backend;
	private MenuTop menuTop=null;
	private JPanel panel=new JPanel();
	private JTextArea ta=new JTextArea(6,20);
	private JTextField st=new JTextField("");
	private JScrollPane scrollerKarte;
	private Karte karte;
	private int spielfeldGroesse=50;
	private int zoomfaktor=100;
	private int idSpieler=0;
	private Feld feldGewaehlt=null;
	private Updater updater;
	
	public enum Bewegungsrichtung{
		NORD,NORDOST,OST,SUEDOST,SUED,SUEDWEST,WEST,NORDWEST;
		public static Bewegungsrichtung fromOrdinal(int n){
			return values()[n];
		}
	}
	
	public Frontend(String url,int idSpieler){
		super();
		backend=new BackendSpielStub(url);
		this.idSpieler=idSpieler;
		setTitle("WorldOfMKI Spiel, Version 0.90, Spieler "+idSpieler);
		setLayout(new BorderLayout());
		panel.setLayout(new BorderLayout());
    // Logger:
    JPanel logger=new JPanel();
    logger.setLayout(new BorderLayout());
		ta.setFont(new Font("Arial", Font.PLAIN, 11));
		ta.setOpaque(true);
		ta.setEditable(false);
		logger.add(new JScrollPane(ta),BorderLayout.CENTER);
		logger.add(st,BorderLayout.SOUTH);
		panel.add(logger,BorderLayout.SOUTH);
		// rechtes Menu einblenden:
		setMenuTop(new MenuTop(this));
		// Fenster:
		add(panel);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(600,700);
		setVisible(true);
		
		
		menuTop.holenKarte(1);
		menuTop.autoUpdate(true);
	}
	
	public void log(String text){
		if (ta.getText().length()==0)
			ta.setText(" "+text);
		else
			ta.setText(ta.getText()+"\n"+" "+text);
		ta.setCaretPosition(ta.getText().length());
	}
	
	public void setStatus(String text){
		st.setText(" "+text);
	}

	public iBackendSpiel getBackend() {
		return backend;
	}

	public Karte neueKarte(String backendDaten){
		if (this.karte!=null){
			karte.terminate();
			panel.remove(scrollerKarte);
		}
		ArrayList<D> daten=Xml.toArray(backendDaten);
		int i;
		for (i=0;i<daten.size();i++){
			if (daten.get(i) instanceof D_Karte) break;
		}
		this.karte=new Karte(this,(D_Karte)daten.get(i));
		zeichneFelder(daten);
		scrollerKarte=new JScrollPane(karte,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		panel.add(scrollerKarte,BorderLayout.CENTER);
		panel.revalidate();
		panel.repaint();
		return karte;
	}

	public void zeichneFelder(ArrayList<D> daten) {
		if (daten==null){
			//TODO: bislang ist nur ein Spiel mit einer Karte ID=1 erlaubt
			String backendDaten=backend.getKarte(1,getIdSpieler());
			daten=Xml.toArray(backendDaten);
		}	
		if (karte!=null) karte.zeichneFelder(daten);
	}

	public void setMenuTop(MenuTop menu){
		if (menuTop!=null) panel.remove(menuTop);
		menuTop=menu;
		menuTop.setEnabled(true);
		panel.add(menuTop,BorderLayout.NORTH);
		panel.revalidate();
		panel.repaint();
	}
	
	public MenuTop getMenuTop(){
		return menuTop;
	}
	
	public Karte getKarte(){
		return karte;
	}
	
	public void setZoomfaktor(int x){
		zoomfaktor=x;
	}
	
	public int getZoomfaktor(){
		return zoomfaktor;
	}
	
	public void setSpielfeldGroesse(int x){
		spielfeldGroesse=x;
	}
	
	public int getSpielfeldGroesse(){
		return spielfeldGroesse;
	}

	public int getIdSpieler(){
		return idSpieler;
	}

	public void setIdSpieler(int spielerId){
		this.idSpieler=spielerId;
	}

	public Feld getFeldGewaehlt(){
		return feldGewaehlt;
	}

	public void setFeldGewaehlt(Feld feldGewaehlt){
		if (this.feldGewaehlt!=null){
			Feld altGewaehlt=this.feldGewaehlt;
			this.feldGewaehlt=null;
			altGewaehlt.zeichnen();
		}
		this.feldGewaehlt=feldGewaehlt;
		if (this.feldGewaehlt!=null) this.feldGewaehlt.zeichnen(); 
	}

	public Updater getUpdater() {
		return updater;
	}
	public void setUpdater() {
		if (this.updater!=null) killUpdater();
		this.updater=new Updater(this,karte.getId(),POLLZEIT);
	}
	public void killUpdater() {
		if (this.updater!=null) this.updater.terminate();
		this.updater=null;
	}
}
