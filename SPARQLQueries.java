package de.lueschow.masterarbeit.queries;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;

import de.lueschow.masterarbeit.Utils;
import static de.lueschow.masterarbeit.PropertiesAndResources.MODEL_PLUS_VIAF_FILENAME;
import static de.lueschow.masterarbeit.PropertiesAndResources.QUERY_PREFIX;


/**
 * Class for defining and executing SPARQL queries.
 *
 * @author Andreas Lüschow
 * @version 0.1
 */
public class SPARQLQueries {

    /**
     * Creates a new instance and invokes the querying process.
     *
     * @param args Main args
     */
    public static void main(String[] args) {
        new SPARQLQueries().run();
    }


    /**
     * Defines queries and executes them.
     */
    public void run() {
        OntModel m = Utils.getModel();
        Utils.loadData(m , MODEL_PLUS_VIAF_FILENAME);

        // Show all Titles
        showQuery( m,
                QUERY_PREFIX +
                        "SELECT DISTINCT ?title WHERE {?a dcterms:title ?title} " +
                        "ORDER by ?title");

        // Number of tripels in model
        showQuery( m,
                QUERY_PREFIX +
                        "select (count(?s) as ?tripel) where { ?s ?p ?o . }");

        // Show all formats
        showQuery( m,
                QUERY_PREFIX +
                        "SELECT ?o WHERE { ?s rdam:P30197 ?o . }");


        // Show ID, Author, Title and Keywords for all entries with 'fée' in the keywords
        showQuery( m,
                QUERY_PREFIX +
                        "SELECT DISTINCT ?id ?au ?ti ?key " +
                        "WHERE {" +
                            "?entry fabio:hasSequenceIdentifier ?id . " +
                            "?entry co:itemContent ?rec . " +
                            "?rec biro:references ?man . " +
                            "?man frbr:embodimentOf ?exp . " +
                            "?exp dcterms:creator ?au . " +
                            "?exp dcterms:title ?ti . " +
                            "?man prism:keyword ?key . " +
                            "FILTER(regex(?key, 'fée'))" +
                        "} " +
                        "ORDER by ?id ");

        // Show VIAF URI next to authors name
        showQuery( m,
                QUERY_PREFIX +
                        "SELECT ?viaf ?au " +
                        "WHERE { " +
                            "?s dcterms:creator ?au . " +
                            "?s dcterms:creator ?viaf . " +
                            "FILTER(isURI(?viaf))\n" +
                            "FILTER(!isURI(?au))" +
                        "}");

        // Count VIAF URIs
        showQuery( m,
                QUERY_PREFIX +
                        "SELECT (count(?viaf) as ?viafids)" +
                        "WHERE { " +
                        "?s dcterms:creator ?viaf . " +
                        "FILTER(isURI(?viaf))" +
                        "}");

        // Count IDs
        showQuery( m,
                QUERY_PREFIX +
                        "SELECT (count(?id) as ?ids)" +
                        "WHERE { " +
                        "?s fabio:hasSequenceIdentifier ?id . " +
                        "}");

        // Count entries with unknown author
        showQuery( m,
                QUERY_PREFIX +
                        "SELECT ?au (count(?au) as ?unknown_authors)" +
                        "WHERE { " +
                        "?exp dcterms:creator ?au . " +
                        "FILTER CONTAINS(str(?au), '?') ." +
                        "} GROUP BY ?au");

        // Get authors with most novels
        showQuery( m,
                QUERY_PREFIX +
                        "SELECT DISTINCT ?au (count(?exp) as ?entries)" +
                        "WHERE { " +
                        "?exp dcterms:creator ?au . " +
                        "}" +
                        "GROUP BY ?au ORDER BY ?entries");

        // Count keywords
        showQuery( m,
                QUERY_PREFIX +
                        "SELECT (count(?key) as ?keywords)" +
                        "WHERE { " +
                        "?man prism:keyword ?key . " +
                        "}");

        // Count keywords with personne
        showQuery( m,
                QUERY_PREFIX +
                        "SELECT (count(?key) as ?keywords)" +
                        "WHERE { " +
                        "?man prism:keyword ?key . " +
                        "FILTER CONTAINS(str(?key), '1re personne') ." +
                        "}");
    }


    /**
     * Prints results of a query to the console.
     *
     * @param m Model
     * @param query Query string
     */
    private void showQuery(Model m, String query) {
        Query q = QueryFactory.create(query);
        QueryExecution qexec = QueryExecutionFactory.create(q, m);
        try {
            ResultSet results = qexec.execSelect();
            ResultSetFormatter.out(results, m);
        }
        finally {
            qexec.close();
        }
    }

}
