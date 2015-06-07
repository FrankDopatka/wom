package backend;

import daten.D_Position;
import backend.karte.Feld;
import backend.karte.Karte;
import backend.spiel.Einheit;
import backend.spiel.Spiel;
import backend.spiel.Spiel.Bewegungsrichtung;

public class Regelwerk {
	public Spiel spiel=null;
	
	public Regelwerk(Spiel spiel){
		this.spiel=spiel;
	}
	
	public D_Position bewegeEinheit(int idSpieler,int idKarte,int xAlt,int yAlt,int richtung) {
		Karte karte=spiel.getKarte(idKarte);
		Feld feldAlt=karte.getFeld(xAlt,yAlt);
		Einheit einheit=feldAlt.getEinheit();
		int[] neu=getNeueKoordinaten(xAlt,yAlt,richtung);
		int xNeu=neu[0]; 
		int yNeu=neu[1];
		if((yNeu<1)||(yNeu>karte.getGroesseY()))
			throw new RuntimeException("Man kann den Kartenrand nicht verlassen!");
		if (spiel.getSpielerAmZug()!=idSpieler)
			throw new RuntimeException("Sie sind nicht am Zug!");
		if (xNeu<1) xNeu=karte.getGroesseX();
		if (xNeu>karte.getGroesseX()) xNeu=1;
		Feld feldNeu=karte.getFeld(xNeu,yNeu);
		if (einheit.istLandeinheit()&&feldNeu.istWasserfeld())
			throw new RuntimeException("Landeinheiten koennen nicht auf Wasser bewegt werden!");

		int punkte=getNoetigeBewegungspunkte(feldAlt,feldNeu);	
		if (einheit.getDaten().getInt("bewegungAktuell")<punkte)
			throw new RuntimeException("Diese Einheit kann sich in dieser Runde nicht mehr auf dieses Feld bewegen!");

		Einheit einheitFeldNeu=feldNeu.getEinheit();
		if (einheitFeldNeu!=null){ // AUF DEM ZIELFELD IST SCHON EINE EINHEIT
			if (einheitFeldNeu.getIdSpieler()==idSpieler)
				throw new RuntimeException("Auf dem Zielfeld der Einheit steht bereits eine Einheit von Ihnen!");
			if (einheit.getDaten().getInt("angriffAktuell")<=0)
				throw new RuntimeException("Ihre Einheit kann nicht angreifen!");
			
			//TODO Kampf
				
		}

		einheit.getDaten().setInt("bewegungAktuell",einheit.getDaten().getInt("bewegungAktuell")-punkte);
		feldAlt.setEinheit(null);
		feldNeu.setEinheit(einheit);

		D_Position posNeu=new D_Position();
		posNeu.setInt("x",xNeu);
		posNeu.setInt("y",yNeu);
		karte.setUpdate(feldAlt.toDatenArray(),einheit.getIdSpieler());
		karte.setUpdate(feldNeu.toDatenArray(),einheit.getIdSpieler());
		return posNeu;
	}
	
	private int getNoetigeBewegungspunkte(Feld feldAlt,Feld feldNeu) {
		// TODO ggf. komplexere Berechnung noetig in Abhaengigkeit der Felduebergaenge und der Einheiten
		return feldNeu.getBewegungspunkte();
	}

	private int[] getNeueKoordinaten(int xAlt,int yAlt,int richtung){
		int xNeu=xAlt;
		int yNeu=yAlt;
		switch (Bewegungsrichtung.fromOrdinal(richtung)){
		case NORD:
			yNeu--;
			break;
		case NORDOST:
			xNeu++;
			yNeu--;
			break;
		case OST:
			xNeu++;
			break;
		case SUEDOST:
			xNeu++;
			yNeu++;
			break;
		case SUED:
			yNeu++;
			break;
		case SUEDWEST:
			xNeu--;
			yNeu++;
			break;
		case WEST:
			xNeu--;
			break;
		case NORDWEST:
			xNeu--;
			yNeu--;
			break;
		}
		return new int[]{xNeu,yNeu};
	}
}
