import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.HashMap;

public class Main {

    public static void main(String [] args) throws FileNotFoundException {
        Model model = ModelFactory.createDefaultModel();

        HashMap<String, String> conferences = Utils.getMapFromCSV(Filepaths.CONFERENCE_PATH, 1);
        HashMap<String, String> authors = Utils.getMapFromCSV(Filepaths.AUTHORS_PATH, 1);
        HashMap<String, String> reviews = Utils.getMapFromCSV(Filepaths.REVIEW_PATH, 0);
        HashMap<String, String> articles = Utils.getMapFromCSV(Filepaths.ARTICLE_PATH, 4);
        HashMap<String, String> journals = Utils.getMapFromCSV(Filepaths.JOURNAL_PATH, 1);
        HashMap<String, String> articleTypes = Utils.generateArticleTypes(articles);
        HashMap<String, String> conferenceTypes = Utils.generateConferenceTypes(conferences);
        HashMap<String, String> journalTypes = Utils.generateJournalTypes(journals);
        ABoxDefiner.defineConference(model, conferences, conferenceTypes);
        ABoxDefiner.defineProceedings(model, conferences, conferenceTypes);
        ABoxDefiner.defineJournal(model, journalTypes);
        ABoxDefiner.defineIssue(model, journals, journalTypes);
        ABoxDefiner.defineKeyword(model);
        ABoxDefiner.defineOrganization(model);
        ABoxDefiner.definePerson(model, authors);
        ABoxDefiner.defineArticle(model, articleTypes);
        ABoxDefiner.defineReview(model);
        ABoxDefiner.defineAuthorship(model, authors, articles, articleTypes);
        ABoxDefiner.defineReviewAuthorship(model, authors, reviews);
        ABoxDefiner.defineReviewAbout(model, reviews, articles, articleTypes);
        ABoxDefiner.definePublishedIn(model, articles, journals, articleTypes, journalTypes);
        ABoxDefiner.definePresentedIn(model, articles, conferences, articleTypes, conferenceTypes);

        model.write(new PrintStream(new BufferedOutputStream(
                new FileOutputStream(Filepaths.OUTPUT_FOLDER + "model.nt")), true), "NT");
    }
}
