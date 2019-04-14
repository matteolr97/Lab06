package it.polito.tdp.meteo;

import java.time.Month;
import java.util.LinkedList;
import java.util.List;

import it.polito.tdp.meteo.bean.Citta;
import it.polito.tdp.meteo.bean.SimpleCity;
import it.polito.tdp.meteo.db.MeteoDAO;

public class Model {

	private final static int COST = 100;
	private final static int NUMERO_GIORNI_CITTA_CONSECUTIVI_MIN = 3;
	private final static int NUMERO_GIORNI_CITTA_MAX = 6;
	private final static int NUMERO_GIORNI_TOTALI = 15;
	MeteoDAO dao = new MeteoDAO();
	List<Citta> best = new LinkedList<Citta>();
	List<Citta> tutteCitta= new LinkedList<Citta>();
	
	
	public Model() {
		tutteCitta = dao.getAllCitta();
	}
	
//CALCOLO L'UMIDITA MEDIA 

	public String getUmiditaMedia(Month mese) {
		
		MeteoDAO dao = new MeteoDAO();
		
		String umid = "";
		double umidita;
		
		List<Citta> listaCitta = new LinkedList<Citta>();
		listaCitta = dao.getAllCitta();
		
		for(Citta localita: listaCitta) {
			umidita= dao.getAvgRilevamentiLocalitaMese(mese, localita);
			umid +=localita+" "+umidita+"\n";
		}
		

		return umid;
	}
	
	

	public List<Citta> trovaSequenza(Month mese) {
		best = null;
		List<Citta> parziale = new LinkedList<Citta>();

		for( Citta ctemp:tutteCitta) {
			ctemp.setRilevamenti(dao.getAllRilevamentiLocalitaMese(mese, ctemp));
		}
		cercaSequenza(parziale,0);
		return best;
	}

	private void cercaSequenza(List<Citta> parziale, int L) {
		//CONDIZIONE USCITA
		if(L==NUMERO_GIORNI_TOTALI ) {
			Double costo = punteggioSoluzione(parziale);
			if (best == null || costo < punteggioSoluzione(best)) 
			best.addAll(parziale);
			
		}
		else {
			
			for(Citta prova : tutteCitta) {
				if(sequenzaValida(prova, parziale)) 		{
					parziale.add(prova);
					cercaSequenza(parziale, L+1);
					parziale.remove(parziale.size()-1);		}
			 }	
		}
	}
	/**
	 * Verifica se, data la soluzione {@code parziale} già definita, sia lecito
	 * aggiungere la città {@code prova}, rispettando i vincoli sui numeri giorni
	 * minimi e massimi di permanenza.
	 * 
	 * @param prova
	 *            la città che sto cercando di aggiungere
	 * @param parziale
	 *            la sequenza di città già composta
	 * @return {@code true} se {@code prova} è lecita, {@code false} se invece viola
	 *         qualche vincolo (e quindi non è lecita)
	 */
	private boolean sequenzaValida(Citta prova, List<Citta> parziale) {

		//STO VERIFICANDO I GIORNI MASSIMI IN UNA CITTA
		
		int counter=0;
		for(Citta s:parziale)
			if(s.equals(prova))
				counter++;
		if(counter>=NUMERO_GIORNI_CITTA_MAX)
			return false;
		
		//ADESSO VERIFICO I GIORNI MINIMI DI SEGUITO IN UNA CITTA
		
		if(parziale.size()==0)//PRIMO GIORNO IN UNA CITTA
			return true;
		
		if(parziale.size()==1 || parziale.size()==2) // FINO A TERZO GIORNO IN UNA CITTA
			return parziale.get(parziale.size()-1).equals(prova);
		
		if(parziale.get(parziale.size()-1).equals(prova)) //GIORNI SUCCESSIVI AL TERZO IN UNA CITTA
			return true;
		
		if(parziale.get(parziale.size()-1).equals(parziale.get(parziale.size()-2))&&
				parziale.get(parziale.size()-2).equals(parziale.get(parziale.size()-3)))
		return true;//STO CAMBIANDO LA CITTA DI ANALISI
		
		
		return false;
	}


	/**
	 * Calcola il costo di una determinata soluzione (totale)
	 * 
	 * <p>
	 * Attenzione: questa funzione assume che i dati siano <b>tutti</b> presenti nel
	 * database, ma nel nostro esempio ciò non accade (in alcuni giorni il dato è
	 * mancante, per cui il risultato calcolato sarà errato).
	 * 
	 * @param parziale
	 *            la soluzione (totale) proposta
	 * @return il valore del costo, che tiene conto delle umidità nei 15 giorni e
	 *         del costo di cambio città
	 */

	private Double punteggioSoluzione(List<Citta> parziale) {

		double costo = 0.0;

		// sommatoria delle umidità in ciascuna città, considerando il rilevamendo del
		// giorno giusto
		// SOMMA parziale.get(giorno-1).getRilevamenti().get(giorno-1)
		
		for (int giorno = 1; giorno <= NUMERO_GIORNI_TOTALI; giorno++) {
			// dove mi trovo?
			Citta c = parziale.get(giorno - 1);
			// che umidità ho in quel giorno in quella città?
			double umid = c.getRilevamenti().get(giorno - 1).getUmidita();
			costo += umid;

			// ATTENZIONE: c.getRilevamenti().get(giorno-1) assume che siano presenti TUTTI
			// i giorni nel database
			// Se vi fossero dei giorni mancanti (e nel nostro DB ce ne sono!), allora il
			// giorno 'giorno-1' potrebbe
			// non corrispondere al dato giusto!
		}

		// a cui sommo 100 * numero di volte in cui cambio città
		for (int giorno = 2; giorno <= NUMERO_GIORNI_TOTALI; giorno++) {
			if (!parziale.get(giorno - 1).equals(parziale.get(giorno - 2))) {
				costo += COST;
			}
		}

		return costo;

	}

	



}
