import com.fasterxml.jackson.databind.annotation.JsonAppend;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

import javax.rmi.CORBA.Util;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicReference;

public class ABoxDefiner {

    public static void defineConference(Model model, HashMap<String, String> conferences, HashMap<String, String> conferenceTypes) {
        AtomicReference<Integer> databaseConferenceFlag = null;
        Property conferenceNameProperty = model.createProperty("http://xmlns.com/foaf/0.1/name");
        Property typeProperty = model.getProperty(Utils.RDF_TYPE);

        conferences.forEach((k, v) -> {
            try {
                Resource resource = model.createResource(Utils.BASE_URL + "/" + conferenceTypes.get(k) + "/" + URLEncoder.encode(v, "UTF-8"))
                        .addProperty(conferenceNameProperty, v);
                model.add(model.createStatement(resource, typeProperty, model.getResource(Utils.BASE_URL + "#" + conferenceTypes.get(k))));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        });

    }

    public static void defineProceedings(Model model, HashMap<String, String> conferences, HashMap<String, String> conferenceTypes) {
        

        try {
            Reader reader = Files.newBufferedReader(Paths.get(Filepaths.PRESENTED_IN_PATH));

            CSVReader csvReader = new CSVReader(reader);
            csvReader.readNextSilently();

            String[] record;

            String uri = null;
            String conferenceId = null;
            String edition = null;
            String period = null;
            String venue = null;
            String conferenceName = null;

            Property conferenceNameProperty = model.getProperty("http://xmlns.com/foaf/0.1/name");
            Property venueProperty = model.createProperty(Utils.BASE_URL + "#" + "venue");
            Property periodProperty = model.createProperty(Utils.BASE_URL + "#" + "period");
            Property editionProperty = model.createProperty(Utils.BASE_URL + "#" + "edition");
            Property typeProperty = model.getProperty(Utils.RDF_TYPE);
            Property proceedingOf = model.createProperty(Utils.BASE_URL + "/proceedingOf");

            HashSet<String> editionsGenerated = new HashSet<String>();

            while ((record = csvReader.readNext()) != null) {
                conferenceId = record[1];
                venue = record[2];
                period = record[3];
                edition = record[4];
                conferenceName = conferences.get(conferenceId);

                uri = URLEncoder.encode(conferenceName + edition, "UTF-8");

                if (!editionsGenerated.contains(uri)) {
                    editionsGenerated.add(uri);
                    Resource proceeding = model.createResource(Utils.BASE_URL + "/" + "Proceeding/" + uri)
                            .addProperty(conferenceNameProperty, conferenceName)
                            .addProperty(venueProperty, venue)
                            .addProperty(periodProperty, period)
                            .addProperty(editionProperty, edition);
                    model.add(model.createStatement(proceeding, typeProperty, model.getResource(Utils.BASE_URL + "#" + "Proceeding")));
                    model.add(proceeding, proceedingOf, model.getResource(Utils.BASE_URL + "/" + conferenceTypes.get(conferenceId) + "/" + URLEncoder.encode(conferenceName, "UTF-8")));
                }
            }

            csvReader.close();

        } catch (IOException | CsvValidationException e) {
            e.printStackTrace();
        }
    }

    public static void defineJournal(Model model, HashMap<String, String> journalTypes) {
        

        try {
            Reader reader = Files.newBufferedReader(Paths.get(Filepaths.JOURNAL_PATH));

            CSVReader csvReader = new CSVReader(reader);
            csvReader.readNextSilently();

            String[] record;

            String uri = null;
            String journalName = null;
            String journalType = null;

            Integer openAccessJournalFlag = null;

            Property journalNameProperty = model.createProperty("http://xmlns.com/foaf/0.1/name");
            Property typeProperty = model.getProperty(Utils.RDF_TYPE);

            while ((record = csvReader.readNext()) != null) {
                openAccessJournalFlag = ThreadLocalRandom.current().nextInt(0, 10);
                journalName = record[1];
                uri = URLEncoder.encode(journalName, "UTF-8");

                journalType = journalTypes.get(record[0]);
                Resource journal = model.createResource(Utils.BASE_URL + "/" + journalType + "/" + uri)
                        .addProperty(journalNameProperty, journalName);
                model.add(model.createStatement(journal, typeProperty, model.getResource(Utils.BASE_URL + "#" + journalType)));
            }

            csvReader.close();

        } catch (IOException | CsvValidationException e) {
            e.printStackTrace();
        }
    }

    public static void defineKeyword(Model model) {

        try {
            Reader reader = Files.newBufferedReader(Paths.get(Filepaths.KEYWORD_PATH));

            CSVReader csvReader = new CSVReader(reader);
            csvReader.readNextSilently();

            String[] record;

            String uri = null;
            String keyword = null;

            Property keywordProperty = model.getProperty("http://xmlns.com/foaf/0.1/name");
            Property typeProperty = model.getProperty(Utils.RDF_TYPE);

            while ((record = csvReader.readNext()) != null) {
                keyword = record[1];
                uri = URLEncoder.encode(keyword, "UTF-8");
                Resource keywordResource = model.createResource(Utils.BASE_URL + "/" + "Keyword/" + uri)
                        .addProperty(keywordProperty, keyword);
                model.add(model.createStatement(keywordResource, typeProperty,
                        model.getResource(Utils.BASE_URL + "#" + "Keyword")));
            }

            csvReader.close();

        } catch (IOException | CsvValidationException e) {
            e.printStackTrace();
        }
    }

    public static void defineOrganization(Model model) {
        

        try {
            Reader reader = Files.newBufferedReader(Paths.get(Filepaths.AFFILIATION_PATH));

            CSVReader csvReader = new CSVReader(reader);
            csvReader.readNextSilently();

            String[] record;

            String uri = null;
            String organizationName = null;

            Property nameProperty = model.getProperty("http://xmlns.com/foaf/0.1/name");
            Property typeProperty = model.getProperty(Utils.RDF_TYPE);

            while ((record = csvReader.readNext()) != null) {
                organizationName = record[1];
                uri = URLEncoder.encode(organizationName, "UTF-8");
                Resource organization = model.createResource("http://dbpedia.org/ontology/" + record[2] + "/" + uri)
                        .addProperty(nameProperty, organizationName);
                model.add(model.createStatement(organization, typeProperty, model.getResource("http://dbpedia.org/ontology/" + record[2])));
            }

            csvReader.close();

            // model.write(new PrintStream(new BufferedOutputStream(new FileOutputStream(Filepaths.OUTPUT_FOLDER + "organization.nt")), true), "NT");
        } catch (IOException | CsvValidationException e) {
            e.printStackTrace();
        }
    }

    public static void definePerson(Model model, HashMap<String, String> authors) {
        
        Property personNameProperty = model.getProperty("https://dblp.uni-trier.de/rdf/schema#fullPersonName");
        Property typeProperty = model.getProperty(Utils.RDF_TYPE);

        authors.forEach((k, v) -> {
            try {
                Resource person = model.createResource(Utils.BASE_URL + "/Author/" + URLEncoder.encode(v, "UTF-8"))
                        .addProperty(personNameProperty, v);
                model.add(model.createStatement(person, typeProperty, model.getResource(Utils.BASE_URL + "#Author")));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        });

    }

    public static void defineArticle(Model model, HashMap<String, String> articleTypes) {
        

        try {
            Reader reader = Files.newBufferedReader(Paths.get(Filepaths.ARTICLE_PATH));

            CSVReader csvReader = new CSVReader(reader);
            csvReader.readNextSilently();

            String[] record;

            String uri = null;
            String date = null;
            String number = null;
            String pages = null;
            String title = null;
            Integer paperTypeNumber = null;
            String paperType = null;

            Property titleProperty = model.getProperty("https://dblp.uni-trier.de/rdf/schema#title");
            Property pageNumbersProperty = model.getProperty("https://dblp.uni-trier.de/rdf/schema#pageNumbers");
            Property publicationDateProperty = model.getProperty("https://dblp.uni-trier.de/rdf/schema#yearOfPublication");
            Property typeProperty = model.getProperty(Utils.RDF_TYPE);

            while ((record = csvReader.readNext()) != null) {
                date = record[1];
                number = record[2];
                pages = record[3];
                title = record[4];
                paperType = articleTypes.get(record[0]);

                uri = URLEncoder.encode(title, "UTF-8");
                Resource article = model.createResource(Utils.BASE_URL + "/" + paperType + "Paper/" + uri)
                        .addProperty(publicationDateProperty, date)
                        .addProperty(pageNumbersProperty, pages)
                        .addProperty(titleProperty, title);
                model.add(model.createStatement(article, typeProperty, model.getResource(Utils.BASE_URL + "#" + paperType + "Paper")));
            }

            csvReader.close();

        } catch (IOException | CsvValidationException e) {
            e.printStackTrace();
        }
    }

    public static void defineReview(Model model) {
        

        try {
            Reader reader = Files.newBufferedReader(Paths.get(Filepaths.REVIEW_PATH));

            CSVReader csvReader = new CSVReader(reader);
            csvReader.readNextSilently();

            String[] record;

            String uri = null;
            String reviewText = null;
            String decision = null;
            Property typeProperty = model.getProperty(Utils.RDF_TYPE);


            while ((record = csvReader.readNext()) != null) {
                uri = URLEncoder.encode(record[0], "UTF-8");
                reviewText = record[1];
                decision = record[2].equals("True") ? "Accepted" : "Rejected";

                Resource review = model.createResource(Utils.BASE_URL + "/" + "Review/" + uri)
                        .addProperty(model.createProperty(Utils.BASE_URL + "#reviewContent"), reviewText)
                        .addProperty(model.createProperty(Utils.BASE_URL + "#reviewOutcome"), decision);
                model.add(model.createStatement(review, typeProperty, model.getResource(Utils.BASE_URL + "#Review")));
            }

            csvReader.close();

        } catch (IOException | CsvValidationException e) {
            e.printStackTrace();
        }
    }

    public static void defineAuthorship(Model model, HashMap<String, String> authors, HashMap<String, String> articles, HashMap<String, String> articleTypes) {

        try {
            Reader reader = Files.newBufferedReader(Paths.get(Filepaths.WRITES_PATH));

            CSVReader csvReader = new CSVReader(reader);
            csvReader.readNextSilently();

            String[] record;

            String author = null;
            Property writes = model.getProperty("https://dblp.uni-trier.de/rdf/schema#authorOf");
            String article = null;


            while ((record = csvReader.readNext()) != null) {
                try {
                    author = Utils.BASE_URL + "/Author/" + URLEncoder.encode(authors.get(record[1]), "UTF-8");
                    article = Utils.BASE_URL + "/" + articleTypes.get(record[0]) + "Paper/" + URLEncoder.encode(articles.get(record[0]), "UTF-8");
                    model.add(model.createStatement(model.getResource(author), writes, model.getResource(article)));
                } catch (NullPointerException e) {
                    continue;
                }
            }

            csvReader.close();

        } catch (IOException | CsvValidationException e) {
            e.printStackTrace();
        }

    }

    public static void defineReviewAuthorship(Model model, HashMap<String, String> authors, HashMap<String, String> reviews) {

        try {
            Reader reader = Files.newBufferedReader(Paths.get(Filepaths.MAKES_REVIEW_PATH));

            CSVReader csvReader = new CSVReader(reader);
            csvReader.readNextSilently();

            String[] record;

            String author = null;
            Property makesReview = model.getProperty(Utils.BASE_URL + "/reviewerOf");
            Property reviewerProperty = model.getProperty(Utils.BASE_URL + "#Reviewer");
            Property typeProperty = model.getProperty(Utils.RDF_TYPE);

            String article = null;
            String reviewer = null;

            HashSet<String> reviewersEntered = new HashSet<String>();

            while ((record = csvReader.readNext()) != null) {
                try {
                    author = Utils.BASE_URL + "/Author/" + URLEncoder.encode(authors.get(record[0]), "UTF-8");
                    article = Utils.BASE_URL + "/Review/" + URLEncoder.encode(reviews.get(record[1]), "UTF-8");
                    model.add(model.createStatement(model.getResource(author), makesReview, model.getResource(article)));
                    reviewer = Utils.BASE_URL + "/Reviewer/" + URLEncoder.encode(authors.get(record[0]), "UTF-8");
                    Resource reviewerResource = model.createResource(reviewer);
                    model.add(model.createStatement(reviewerResource, typeProperty, model.getResource(Utils.BASE_URL + "#Reviewer")));
                } catch (NullPointerException e) {
                    continue;
                }
            }

            csvReader.close();

        } catch (IOException | CsvValidationException e) {
            e.printStackTrace();
        }
    }


    public static void defineReviewAbout(Model model, HashMap<String, String> reviews, HashMap<String, String> articles, HashMap<String, String> articleTypes) {

        try {
            Reader reader = Files.newBufferedReader(Paths.get(Filepaths.ABOUT_PATH));

            CSVReader csvReader = new CSVReader(reader);
            csvReader.readNextSilently();

            String[] record;

            String review = null;
            Property about = model.getProperty("http://purl.org/ontology/bibo/reviewOf");
            String article = null;


            while ((record = csvReader.readNext()) != null) {
                try {
                    review = Utils.BASE_URL + "/Review/" + URLEncoder.encode(reviews.get(record[0]), "UTF-8");
                    article = Utils.BASE_URL + "/" + articleTypes.get(record[1]) + "Paper/" + URLEncoder.encode(articles.get(record[1]), "UTF-8");
                    model.add(model.createStatement(model.getResource(review), about, model.getResource(article)));
                } catch (NullPointerException e) {
                    continue;
                }
            }

            csvReader.close();

        } catch (IOException | CsvValidationException e) {
            e.printStackTrace();
        }
    }

    public static void definePublishedIn(Model model, HashMap<String, String> articles, HashMap<String, String> journals, HashMap<String, String> articleTypes, HashMap<String, String> journalTypes) {
        try {
            Reader reader = Files.newBufferedReader(Paths.get(Filepaths.PUBLISHED_IN_PATH));

            CSVReader csvReader = new CSVReader(reader);
            csvReader.readNextSilently();

            String[] record;

            String article = null;
            Property publishedIn = model.getProperty(Utils.BASE_URL + "/publishedIn");
            String journal = null;


            while ((record = csvReader.readNext()) != null) {
                try {
                    article = Utils.BASE_URL + "/" + articleTypes.get(record[0]) + "Paper/" + URLEncoder.encode(articles.get(record[0]), "UTF-8");
                    journal = Utils.BASE_URL + "/" + journalTypes.get(record[1]) + "/" + URLEncoder.encode(journals.get(record[1]), "UTF-8");
                    model.add(model.createStatement(model.getResource(article), publishedIn, model.getResource(journal)));
                } catch (NullPointerException e) {
                    continue;
                }
            }

            csvReader.close();

        } catch (IOException | CsvValidationException e) {
            e.printStackTrace();
        }
    }

    public static void definePresentedIn(Model model, HashMap<String, String> articles, HashMap<String, String> conferences, HashMap<String, String> articleTypes, HashMap<String, String> conferenceTypes) {
        try {
            Reader reader = Files.newBufferedReader(Paths.get(Filepaths.PRESENTED_IN_PATH));

            CSVReader csvReader = new CSVReader(reader);
            csvReader.readNextSilently();

            String[] record;

            String article = null;
            Property presentedIn = model.getProperty(Utils.BASE_URL + "/publishedIn");
            String conference = null;


            while ((record = csvReader.readNext()) != null) {
                try {
                    article = Utils.BASE_URL + "/" + articleTypes.get(record[0]) + "Paper/" + URLEncoder.encode(articles.get(record[0]), "UTF-8");
                    conference = Utils.BASE_URL + "/" + conferenceTypes.get(record[1]) + "/" + URLEncoder.encode(conferences.get(record[1]), "UTF-8");
                    model.add(model.createStatement(model.getResource(article), presentedIn, model.getResource(conference)));
                } catch (NullPointerException e) {
                    continue;
                }
            }

            csvReader.close();

        } catch (IOException | CsvValidationException e) {
            e.printStackTrace();
        }
    }

    public static void defineIssue(Model model, HashMap<String, String> journals, HashMap<String, String> journalTypes) {
        try {
            Reader reader = Files.newBufferedReader(Paths.get(Filepaths.PUBLISHED_IN_PATH));

            CSVReader csvReader = new CSVReader(reader);
            csvReader.readNextSilently();

            String[] record;

            String uri = null;
            String journalId = null;
            String issue = null;
            String yearOfPublication = null;
            String journalName = null;

            Property yearOfPublicationProperty = model.getProperty("https://dblp.uni-trier.de/rdf/schema#yearOfPublication");
            Property volumeProperty = model.createProperty(Utils.BASE_URL + "#" + "volume");
            Property typeProperty = model.getProperty(Utils.RDF_TYPE);
            Property issueOf = model.createProperty(Utils.BASE_URL + "/issueOf");

            HashSet<String> volumesGenerated = new HashSet<String>();

            while ((record = csvReader.readNext()) != null) {
                journalId = record[1];
                issue = record[2];
                yearOfPublication = record[3];
                journalName = journals.get(journalId);

                uri = URLEncoder.encode(journalName + issue, "UTF-8");

                if (!volumesGenerated.contains(uri)) {
                    volumesGenerated.add(uri);
                    Resource proceeding = model.createResource("http://purl.org/ontology/bibo/Issue" + uri)
                            .addProperty(yearOfPublicationProperty, yearOfPublication)
                            .addProperty(volumeProperty, issue);
                    model.add(model.createStatement(proceeding, typeProperty, model.getResource(Utils.BASE_URL + "#" + "Proceeding")));
                    model.add(proceeding, issueOf, model.getResource(Utils.BASE_URL + "/" + journalTypes.get(journalId) + "/" + URLEncoder.encode(journalName, "UTF-8")));
                }
            }

            csvReader.close();

        } catch (IOException | CsvValidationException e) {
            e.printStackTrace();
        }
    }
}
