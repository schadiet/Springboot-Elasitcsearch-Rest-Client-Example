package de.codingsolo.ui.views;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

import org.apache.http.HttpHost;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.primefaces.event.SelectEvent;
import org.primefaces.json.JSONArray;
import org.primefaces.json.JSONObject;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.codingsolo.ui.model.Kundendaten;

@Named
@ViewScoped
public class Kundensuche {

	private final Logger LOGGER = LogManager.getLogger("de.codingsolo.ui.ElasticSearchSpringApplication");

	private String feldSchnellSuche;
	private String name;
	private String vorname;
	private String kundenNummer;
	private List<Kundendaten> suchErgebnisse = new ArrayList<Kundendaten>();

	public void schnellSuche() {
		LOGGER.info("Schnellsuche für den Wert: " + this.feldSchnellSuche);
		this.suchErgebnisse = new ArrayList<Kundendaten>();

		RestClient restClient = RestClient.builder(new HttpHost("localhost", 9200, "http")).build();

		Request request = new Request("GET", "/kundendaten/_search?q=" + this.feldSchnellSuche);
		ObjectMapper objectMapper = new ObjectMapper();

		Response response = null;
		try {
			LOGGER.info("Öffne den Rest-Client (Schnellsuche)");
			response = restClient.performRequest(request);
		} catch (IOException ioe) {
			LOGGER.error("Fehler beim öffnen des Rest-Client (Schnellsuche)", ioe);
		}
		try {
			LOGGER.info("Umwandlung der Daten (Schnellsuche)");

			JSONObject jsonHitsObject = new JSONObject(EntityUtils.toString(response.getEntity()))
					.getJSONObject("hits");
			JSONArray jsonHitsArray = jsonHitsObject.getJSONArray("hits");

			for (Object object : jsonHitsArray) {
				JSONObject kundendaten = (JSONObject) object;
				Kundendaten kunde = objectMapper.readValue(kundendaten.get("_source").toString(), Kundendaten.class);
				this.suchErgebnisse.add(kunde);
			}
		} catch (Exception e) {
			LOGGER.error("Fehler beim umwandeln der Daten (Schnellsuche)", e);
		}

		try {
			LOGGER.info("Schließe den Rest-Client (Schnellsuche)");
			restClient.close();
		} catch (IOException ioe) {
			LOGGER.error("Fehler beim schließen des Rest-Client (Schnellsuche)", ioe);
		}
	}

	public void suche() {
		LOGGER.info("Normale Suche für die Werte: Kundennummer:" + this.kundenNummer + ", Vorname: " + this.vorname
				+ ", Name:" + this.name);
		this.suchErgebnisse = new ArrayList<Kundendaten>();

		RestHighLevelClient client = new RestHighLevelClient(
				RestClient.builder(new HttpHost("localhost", 9200, "http")));

		SearchRequest searchRequest = new SearchRequest("kundendaten");
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

		BoolQueryBuilder boolList = QueryBuilders.boolQuery()
				.should(QueryBuilders.matchQuery("kunde_kundennummer", this.kundenNummer))
				.should(QueryBuilders.matchPhrasePrefixQuery("kunde_vorname", this.vorname))
				.should(QueryBuilders.matchPhrasePrefixQuery("kunde_name", this.name)).minimumShouldMatch(1);

		searchSourceBuilder.query(boolList);
		searchRequest.source(searchSourceBuilder);

		SearchResponse searchResponse = null;
		try {
			LOGGER.info("Öffne den Rest-Client (normale Suche)");

			searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
		} catch (IOException ioe) {
			LOGGER.error("Fehler beim öffnen des Rest-Client (normale Suche)", ioe);
		}

		try {
			LOGGER.info("Umwandlung der Daten (normale Suche)");
			SearchHits hits = searchResponse.getHits();
			SearchHit[] searchHits = hits.getHits();
			for (SearchHit hit : searchHits) {
				// do something with the SearchHit
				Kundendaten kunde = new ObjectMapper().readValue(hit.getSourceAsString(), Kundendaten.class);
				this.suchErgebnisse.add(kunde);
			}

		} catch (IOException ioe) {
			LOGGER.error("Fehler beim umwandeln der Daten (Schnellsuche)", ioe);
		}

		try {
			LOGGER.info("Schließe den Rest-Client (normale Suche)");
			client.close();
		} catch (IOException ioe) {
			LOGGER.error("Fehler beim schließen des Rest-Client (normale Suche)", ioe);
		}
	}

	public void selectKunde(SelectEvent event) {
		Kundendaten kunde = (Kundendaten) event.getObject();
		FacesContext context = FacesContext.getCurrentInstance();
		try {
			context.getExternalContext().redirect("/kundendetails.xhtml?id=" + kunde.getId());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
	 * Getter and Setter here!
	 */

	public String getFeldSchnellSuche() {
		return feldSchnellSuche;
	}

	public void setFeldSchnellSuche(String feldSchnellSuche) {
		this.feldSchnellSuche = feldSchnellSuche;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getVorname() {
		return vorname;
	}

	public void setVorname(String vorname) {
		this.vorname = vorname;
	}

	public String getKundenNummer() {
		return kundenNummer;
	}

	public void setKundenNummer(String kundenNummer) {
		this.kundenNummer = kundenNummer;
	}

	public List<Kundendaten> getSuchErgebnisse() {
		return suchErgebnisse;
	}

	public void setSuchErgebnisse(List<Kundendaten> suchErgebnisse) {
		this.suchErgebnisse = suchErgebnisse;
	}

}
