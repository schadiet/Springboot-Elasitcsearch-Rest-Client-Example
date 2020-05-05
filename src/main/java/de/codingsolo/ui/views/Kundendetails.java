package de.codingsolo.ui.views;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

import org.apache.http.HttpHost;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.primefaces.PrimeFaces;
import org.primefaces.event.SelectEvent;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.codingsolo.ui.model.Bestellposten;
import de.codingsolo.ui.model.Bestellung;
import de.codingsolo.ui.model.Kundendaten;

@Named
@ViewScoped
public class Kundendetails {

	private final Logger LOGGER = LogManager.getLogger("de.codingsolo.ui.ElasticSearchSpringApplication");

	private String id;
	private Kundendaten kundendaten;
	private List<Bestellung> offeneBestellungen;
	private List<Bestellposten> posten;

	@PostConstruct
	private void init() {
		Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		this.id = params.get("id");
		this.getData();
	}

	public void getData() {
		LOGGER.info("getData für die ID: " + this.id);

		GetRequest getRequest = new GetRequest("kundendaten", this.id);

		RestHighLevelClient client = new RestHighLevelClient(
				RestClient.builder(new HttpHost("localhost", 9200, "http")));

		GetResponse getResponse = null;
		try {
			LOGGER.info("Öffne den Rest-Client (getData)");

			getResponse = client.get(getRequest, RequestOptions.DEFAULT);
		} catch (IOException ioe) {
			LOGGER.error("Fehler beim öffnen des Rest-Client (getData)", ioe);
		}

		try {
			LOGGER.info("Umwandlung der Daten (getData)");

			System.out.println(getResponse.getSourceAsString());

			this.kundendaten = new ObjectMapper().readValue(getResponse.getSourceAsString(), Kundendaten.class);
			this.sortOffeneBestellungen();

			PrimeFaces.current().ajax().update();

		} catch (IOException ioe) {
			LOGGER.error("Fehler beim öffnen des Rest-Client (getData)", ioe);
		}

		try {
			LOGGER.info("Schließe den Rest-Client (getData)");
			client.close();
		} catch (IOException ioe) {
			LOGGER.error("Fehler beim schließen des Rest-Client (getData)", ioe);
		}
	}

	private void sortOffeneBestellungen() {
		this.offeneBestellungen = new ArrayList<Bestellung>();
		for (Bestellung bestellung : this.kundendaten.getKunde_bestellung()) {
			if (bestellung.getBestellung_status().equals("In Bearbeitung")
					|| bestellung.getBestellung_status().equals("Verzögert")) {
				this.offeneBestellungen.add(bestellung);
			}
		}
	}

	public double berechneGesamtWert(List<Bestellposten> posten) {
		double wert = 0.0;
		for (Bestellposten bestellposten : posten) {
			wert += bestellposten.getArtikel_preis();
		}
		return wert;
	}

	public void selectedBestellung(SelectEvent event) {
		this.posten = new ArrayList<Bestellposten>();
		Bestellung bestellung = (Bestellung) event.getObject();
		for (Bestellposten posten : bestellung.getBestellung_posten()) {
			this.posten.add(posten);
		}
	}

	/*
	 * Getter and Setter here!
	 */

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Kundendaten getKundendaten() {
		return kundendaten;
	}

	public void setKundendaten(Kundendaten kundendaten) {
		this.kundendaten = kundendaten;
	}

	public List<Bestellung> getOffeneBestellungen() {
		return offeneBestellungen;
	}

	public void setOffeneBestellungen(List<Bestellung> offeneBestellungen) {
		this.offeneBestellungen = offeneBestellungen;
	}

	public List<Bestellposten> getPosten() {
		return posten;
	}

	public void setPosten(List<Bestellposten> posten) {
		this.posten = posten;
	}

}
