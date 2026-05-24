package CruiseShipModel.microbiome;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Manages evidence-based microbial source profiles for shipboard environments.
 *
 * Loads 9 source profiles from expanded_maritime_profiles.json:
 *   Human_Gut_Healthy, Human_Respiratory_Healthy, Human_Skin,
 *   Sink_P_Traps, Showers, Toilets, Outdoor_Air,
 *   Human_Gut_Infected, Human_Respiratory_Infected
 *
 * Each profile maps genus-level taxa to relative abundances summing to 1.0.
 */
public class SourceProfiles {

    private final Map<String, Map<String, Double>> profiles;
    private final List<String> sourceNames;
    private final List<String> allTaxa;

    public SourceProfiles() {
        this(null);
    }

    /**
     * @param jsonFilePath path to profiles JSON; if null, loads bundled resource
     */
    public SourceProfiles(String jsonFilePath) {
        profiles = new LinkedHashMap<>();
        if (jsonFilePath != null) {
            loadFromFile(jsonFilePath);
        } else {
            loadFromResource();
        }
        sourceNames = Collections.unmodifiableList(new ArrayList<>(profiles.keySet()));
        allTaxa = computeAllTaxa();
    }

    private void loadFromResource() {
        try (InputStream is = getClass().getResourceAsStream("/CruiseShipModel/microbiome/expanded_maritime_profiles.json")) {
            if (is == null) {
                throw new RuntimeException("expanded_maritime_profiles.json not found on classpath");
            }
            parseJson(readStreamFully(is));
        } catch (IOException e) {
            throw new RuntimeException("Failed to load profiles from classpath", e);
        }
    }

    private void loadFromFile(String path) {
        try (InputStream is = new FileInputStream(path)) {
            parseJson(readStreamFully(is));
        } catch (IOException e) {
            throw new RuntimeException("Failed to load profiles from " + path, e);
        }
    }

    private String readStreamFully(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        char[] buf = new char[4096];
        int n;
        while ((n = reader.read(buf)) != -1) {
            sb.append(buf, 0, n);
        }
        return sb.toString();
    }

    /**
     * Minimal JSON object parser — handles the flat { "profile": { "taxon": double } } structure.
     * No external dependencies required.
     */
    private void parseJson(String json) {
        json = json.trim();
        if (!json.startsWith("{") || !json.endsWith("}")) {
            throw new RuntimeException("Invalid JSON: expected top-level object");
        }
        json = json.substring(1, json.length() - 1).trim();

        while (!json.isEmpty()) {
            // Read profile name
            int nameStart = json.indexOf('"');
            if (nameStart < 0) break;
            int nameEnd = json.indexOf('"', nameStart + 1);
            String profileName = json.substring(nameStart + 1, nameEnd);

            // Find the inner object
            int objStart = json.indexOf('{', nameEnd);
            int objEnd = findMatchingBrace(json, objStart);
            String innerJson = json.substring(objStart + 1, objEnd).trim();

            Map<String, Double> profile = new LinkedHashMap<>();
            parseInnerObject(innerJson, profile);
            profiles.put(profileName, profile);

            json = json.substring(objEnd + 1).trim();
            if (json.startsWith(",")) {
                json = json.substring(1).trim();
            }
        }
    }

    private void parseInnerObject(String inner, Map<String, Double> profile) {
        while (!inner.isEmpty()) {
            int keyStart = inner.indexOf('"');
            if (keyStart < 0) break;
            int keyEnd = inner.indexOf('"', keyStart + 1);
            String taxon = inner.substring(keyStart + 1, keyEnd);

            int colonPos = inner.indexOf(':', keyEnd);
            int nextComma = inner.indexOf(',', colonPos);
            String valStr;
            if (nextComma < 0) {
                valStr = inner.substring(colonPos + 1).trim();
                inner = "";
            } else {
                valStr = inner.substring(colonPos + 1, nextComma).trim();
                inner = inner.substring(nextComma + 1).trim();
            }
            profile.put(taxon, Double.parseDouble(valStr));
        }
    }

    private int findMatchingBrace(String s, int openPos) {
        int depth = 0;
        for (int i = openPos; i < s.length(); i++) {
            if (s.charAt(i) == '{') depth++;
            else if (s.charAt(i) == '}') {
                depth--;
                if (depth == 0) return i;
            }
        }
        throw new RuntimeException("Unmatched brace at position " + openPos);
    }

    private List<String> computeAllTaxa() {
        Set<String> taxa = new TreeSet<>();
        for (Map<String, Double> p : profiles.values()) {
            taxa.addAll(p.keySet());
        }
        return Collections.unmodifiableList(new ArrayList<>(taxa));
    }

    public List<String> getSourceNames() {
        return sourceNames;
    }

    public List<String> getAllTaxa() {
        return allTaxa;
    }

    public int getNumSources() {
        return sourceNames.size();
    }

    public int getNumTaxa() {
        return allTaxa.size();
    }

    /**
     * Returns a copy of the profile for the given source.
     */
    public Map<String, Double> getProfile(String sourceName) {
        Map<String, Double> p = profiles.get(sourceName);
        if (p == null) {
            throw new IllegalArgumentException("Unknown source: " + sourceName + ". Available: " + sourceNames);
        }
        return new LinkedHashMap<>(p);
    }

    /**
     * Returns the profile as a vector ordered by {@link #getAllTaxa()}.
     * Taxa absent from the profile are 0.0. The vector is renormalized to sum to 1.
     */
    public double[] getProfileVector(String sourceName) {
        return getProfileVector(sourceName, allTaxa);
    }

    public double[] getProfileVector(String sourceName, List<String> taxaOrder) {
        Map<String, Double> profile = getProfile(sourceName);
        double[] vec = new double[taxaOrder.size()];
        double sum = 0.0;
        for (int i = 0; i < taxaOrder.size(); i++) {
            Double val = profile.get(taxaOrder.get(i));
            vec[i] = (val != null) ? val : 0.0;
            sum += vec[i];
        }
        if (sum > 0) {
            for (int i = 0; i < vec.length; i++) {
                vec[i] /= sum;
            }
        }
        return vec;
    }

    /**
     * Returns all profiles as a matrix [numTaxa][numSources].
     */
    public double[][] getAllProfilesMatrix() {
        double[][] matrix = new double[allTaxa.size()][sourceNames.size()];
        for (int j = 0; j < sourceNames.size(); j++) {
            double[] vec = getProfileVector(sourceNames.get(j));
            for (int i = 0; i < vec.length; i++) {
                matrix[i][j] = vec[i];
            }
        }
        return matrix;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("SourceProfiles: ").append(sourceNames.size()).append(" sources, ")
          .append(allTaxa.size()).append(" total taxa\n");
        for (String name : sourceNames) {
            Map<String, Double> p = profiles.get(name);
            sb.append("  ").append(name).append(": ").append(p.size()).append(" taxa\n");
        }
        return sb.toString();
    }
}
