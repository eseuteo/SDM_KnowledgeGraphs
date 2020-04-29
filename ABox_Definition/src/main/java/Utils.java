import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

public class Utils {

    public static final String BASE_URL = "http://www.semanticweb.org/sdm/lab3";
    public static final String RDF_TYPE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";

    public static HashMap<String, String> getMapFromCSV(String path, int index) {
        HashMap<String, String> map = new HashMap<String, String>();
        try {
            Reader reader = Files.newBufferedReader(Paths.get(path));
            CSVReader csvReader = new CSVReader(reader);

            String[] record;
            String id = null;
            String name = null;

            while ((record = csvReader.readNext()) != null) {
                id = record[0];
                name = record[index];
                map.put(id, name);
            }
        } catch (IOException | CsvValidationException e) {
            e.printStackTrace();
        }

        return map;
    }

    public static HashMap<String, String> generateArticleTypes(HashMap<String, String> articles) {
        HashMap<String, String> articleTypes = new HashMap<String, String>();

        Integer paperTypeNumber = null;
        String paperType = null;

        for (String s : articles.keySet()) {
            paperTypeNumber = ThreadLocalRandom.current().nextInt(0, 4);
            paperType = paperTypeNumber == 0 ? "Demo" : paperTypeNumber == 1 ? "Full" : paperTypeNumber == 2 ? "Short" : "Survey";
            articleTypes.put(s, paperType);
        }

        return articleTypes;
    }

    public static HashMap<String, String> generateConferenceTypes(HashMap<String, String> conferences) {
        HashMap<String, String> conferenceTypes = new HashMap<String, String>();

        Integer conferenceTypeNumber = null;
        String conferenceType = null;

        for (String s : conferences.keySet()) {
            conferenceType = ThreadLocalRandom.current().nextInt(0, 10) == 0 ? "DatabaseConference" : "Conference";
            conferenceTypes.put(s, conferenceType);
        }

        return conferenceTypes;
    }

    public static HashMap<String, String> generateJournalTypes(HashMap<String, String> journals) {
        HashMap<String, String> journalTypes = new HashMap<String, String>();

        Integer journalTypeNumber = null;
        String journalType = null;

        for (String s : journals.keySet()) {
            journalType = ThreadLocalRandom.current().nextInt(0, 10) == 0 ? "OpenAccessJournal" : "Journal";
            journalTypes.put(s, journalType);
        }

        return journalTypes;
    }
}
